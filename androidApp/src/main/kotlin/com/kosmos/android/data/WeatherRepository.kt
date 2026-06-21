package com.kosmos.android.data

import android.content.Context
import com.kosmos.android.BuildConfig
import com.kosmos.shared.engine.EvaluateConfig
import com.kosmos.shared.engine.KosmosEngine
import com.kosmos.shared.engine.VerdictEngine
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.i18n.VerdictLocalizer
import com.kosmos.shared.mode.UserMode
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.shared.api.MultiSourceClient
import com.kosmos.shared.api.OpenMeteoClient
import com.kosmos.shared.api.TomorrowClient
import com.kosmos.shared.api.createHttpClient
import com.kosmos.shared.models.WeatherSnapshot as SharedSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max
import java.time.LocalDate

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    @Deprecated("Use Ready + isLocating flag; kept for compatibility")
    data object Locating : WeatherUiState()
    data class Ready(val snapshot: WeatherSnapshot) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherRepository(context: Context) {

    private val openMeteo = OpenMeteoClient(createHttpClient())
    private val multiSource = MultiSourceClient(
        openMeteo,
        BuildConfig.TOMORROW_API_KEY.takeIf { it.isNotBlank() }?.let {
            TomorrowClient(createHttpClient(), it)
        },
    )
    private val preferences = PreferencesRepository(context)
    private val locationRepo = LocationRepository(context, createHttpClient())
    private val travelRepo = TravelRepository(context)
    private val refreshMutex = Mutex()

    private val _travelState = MutableStateFlow(TravelState.Inactive)
    val travelState: StateFlow<TravelState> = _travelState.asStateFlow()

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private var lastSharedSnapshot: SharedSnapshot? = null
    private var lastNeighborhood: String? = null
    private var lastSubArea: String? = null
    private var lastCountry: String? = null
    private var lastSoil: com.kosmos.shared.farmer.SoilData? = null
    private var lastUpdatedAt: Long = 0L
    private var lastHasLiveLocation: Boolean = false
    private var lastLocationSource: LocationSource = LocationSource.SAVED_CITY
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    suspend fun startup(hasLocationPermission: Boolean) {
        val hadCache = refreshMutex.withLock { seedFromCache() }
        val wantsLiveGps = hasLocationPermission && preferences.isUsingLiveGps()
        if (wantsLiveGps) {
            refresh(hasLocationPermission, pullToRefresh = hadCache)
            return
        }
        if (hadCache) {
            val cacheAge = System.currentTimeMillis() - lastUpdatedAt
            if (cacheAge > 30 * 60_000L) {
                refresh(hasLocationPermission, pullToRefresh = true)
            }
            return
        }
        refresh(hasLocationPermission, pullToRefresh = false)
    }

    suspend fun refresh(hasLocationPermission: Boolean, pullToRefresh: Boolean = false): Result<Unit> =
        refreshMutex.withLock {
            val hasReady = _state.value is WeatherUiState.Ready
            val keepContent = pullToRefresh && hasReady
            val wantsGps = hasLocationPermission && preferences.isUsingLiveGps()

            if (keepContent) {
                _isRefreshing.value = true
                _isLocating.value = wantsGps
            } else if (!hasReady) {
                _state.value = WeatherUiState.Loading
                _isLocating.value = wantsGps
            }

            return try {
                val ok = resolveAndLoad(
                    hasLocationPermission,
                    forceLiveGps = wantsGps,
                )
                if (ok == true) {
                    Result.success(Unit)
                } else if (_state.value is WeatherUiState.Ready) {
                    Result.success(Unit)
                } else {
                    _state.value = WeatherUiState.Error(
                        if (!locationRepo.isLocationServicesEnabled()) {
                            "Turn on Location in phone settings, then pull to refresh."
                        } else {
                            "Could not find your location. Try outdoors or pick a city manually."
                        },
                    )
                    Result.failure(IllegalStateException("Location unresolved"))
                }
            } catch (e: Exception) {
                if (_state.value !is WeatherUiState.Ready && !seedFromCache()) {
                    _state.value = WeatherUiState.Error(e.message ?: "Could not load weather")
                }
                Result.failure(e)
            } finally {
                _isRefreshing.value = false
                _isLocating.value = false
            }
        }

    suspend fun refreshCurrentLocation(hasLocationPermission: Boolean): Result<Unit> =
        refreshMutex.withLock {
            if (!hasLocationPermission) {
                return Result.failure(IllegalStateException("Location permission not granted"))
            }
            if (!locationRepo.isLocationServicesEnabled()) {
                return Result.failure(
                    IllegalStateException("Turn on Location in phone settings (GPS or network), then try again."),
                )
            }

            val hadContent = _state.value is WeatherUiState.Ready
            if (hadContent) {
                _isRefreshing.value = true
            } else {
                _state.value = WeatherUiState.Loading
            }
            _isLocating.value = true

            return try {
                preferences.setUsingLiveGps(true)
                val ok = resolveAndLoad(hasLocationPermission, forceLiveGps = true)
                if (ok == true) {
                    Result.success(Unit)
                } else if (hadContent) {
                    Result.failure(IllegalStateException("Could not get GPS location"))
                } else {
                    _state.value = WeatherUiState.Error(
                        "GPS fix failed. Step outside or pick a city from Change location.",
                    )
                    Result.failure(IllegalStateException("Could not get GPS location"))
                }
            } catch (e: Exception) {
                if (!hadContent) {
                    _state.value = WeatherUiState.Error(e.message ?: "Could not get current location")
                }
                Result.failure(e)
            } finally {
                _isRefreshing.value = false
                _isLocating.value = false
            }
        }

    suspend fun refreshAt(geo: GeoLocation, hasLiveLocation: Boolean = false): Result<Unit> =
        refreshMutex.withLock {
            val hadReady = _state.value is WeatherUiState.Ready
            if (!hadReady) {
                _state.value = WeatherUiState.Loading
            } else {
                _isRefreshing.value = true
            }
            return try {
                if (!hasLiveLocation) {
                    preferences.setUsingLiveGps(false)
                }
                loadWeather(geo, hasLiveLocation, if (hasLiveLocation) LocationSource.GPS else LocationSource.SAVED_CITY)
                Result.success(Unit)
            } catch (e: Exception) {
                if (!hadReady) {
                    _state.value = WeatherUiState.Error(e.message ?: "Could not load weather")
                }
                Result.failure(e)
            } finally {
                _isRefreshing.value = false
            }
        }

    private suspend fun resolveAndLoad(hasLocationPermission: Boolean, forceLiveGps: Boolean): Boolean? {
        val resolved = locationRepo.resolveForWeather(hasLocationPermission, forceLiveGps) ?: return null
        if (LocationRepository.isDefaultLocation(resolved.geo) && hasLocationPermission) return null
        loadWeather(resolved.geo, resolved.isLiveGps, resolved.source, refineLabels = resolved.isLiveGps)
        return true
    }

    private suspend fun loadWeather(
        geo: GeoLocation,
        hasLiveLocation: Boolean,
        locationSource: LocationSource,
        refineLabels: Boolean = false,
    ) {
        val saved = preferences.getSavedLocation()
        val stableGeo = if (hasLiveLocation && locationSource == LocationSource.GPS) {
            locationRepo.stabilizeForDisplay(geo, saved)
        } else {
            geo
        }
        applyGeo(stableGeo, hasLiveLocation, locationSource)
        _travelState.value = travelRepo.evaluateFromGeo(stableGeo, hasLiveLocation)
        val isPlus = preferences.isKosmosPlusActive()
        val activeModes = preferences.getActiveModes()
        val forecastDays = if (isPlus || activeModes.contains(UserMode.FARMER)) 10 else 2
        val shared = multiSource.fetchWeather(stableGeo.latitude, stableGeo.longitude, stableGeo.city, forecastDays)
        if (activeModes.contains(UserMode.FARMER)) {
            val profile = preferences.getFarmerProfile()
            val plotLat = profile.plotLat ?: stableGeo.latitude
            val plotLon = profile.plotLon ?: stableGeo.longitude
            lastSoil = runCatching {
                openMeteo.fetchSoil(plotLat, plotLon, shared.currentHour)
            }.getOrNull()
        } else {
            lastSoil = null
        }
        if (!LocationRepository.isDefaultLocation(stableGeo)) {
            preferences.saveLocation(stableGeo)
        }
        publish(shared)

        if (refineLabels && needsLabelRefinement(stableGeo)) {
            refineLabelsFromCoords(stableGeo.latitude, stableGeo.longitude, hasLiveLocation, locationSource)
        }
    }

    private fun needsLabelRefinement(geo: GeoLocation): Boolean =
        geo.neighborhood.isNullOrBlank() ||
            geo.city.equals("Near you", ignoreCase = true)

    private suspend fun refineLabelsFromCoords(
        lat: Double,
        lon: Double,
        hasLiveLocation: Boolean,
        locationSource: LocationSource,
    ) {
        val refined = locationRepo.reverseGeocode(lat, lon, freshGps = true) ?: return
        val enriched = IndiaPlacesCatalog.enrichGeo(refined)
        val saved = preferences.getSavedLocation()
        val stable = locationRepo.stabilizeForDisplay(enriched, saved)
        val labelChanged = stable.neighborhood != lastNeighborhood ||
            stable.city != lastSharedSnapshot?.cityName ||
            stable.subArea != lastSubArea
        if (!labelChanged) return
        applyGeo(stable, hasLiveLocation, locationSource)
        if (!LocationRepository.isDefaultLocation(stable)) {
            preferences.saveLocation(stable)
        }
        remapDisplay()
    }

    private fun applyGeo(geo: GeoLocation, hasLiveLocation: Boolean, locationSource: LocationSource) {
        lastNeighborhood = geo.neighborhood
        lastSubArea = geo.subArea
        lastCountry = geo.country
        lastHasLiveLocation = hasLiveLocation
        lastLocationSource = locationSource
        lastLat = geo.latitude
        lastLon = geo.longitude
    }

    private suspend fun publish(shared: SharedSnapshot) {
        lastSharedSnapshot = shared
        lastUpdatedAt = System.currentTimeMillis()
        preferences.saveCachedWeather(
            CachedWeatherPayload(
                snapshot = shared,
                neighborhood = lastNeighborhood,
                subArea = lastSubArea,
                country = lastCountry,
                hasLiveLocation = lastHasLiveLocation,
                locationSource = lastLocationSource.name,
                savedAtMillis = lastUpdatedAt,
            ),
        )
        _state.value = WeatherUiState.Ready(buildUiSnapshot(shared, 0))
    }

    private fun snapshotEpochMs(minutesAgo: Int): Long =
        System.currentTimeMillis() - minutesAgo * 60_000L

    suspend fun seedFromCache(): Boolean {
        val cached = preferences.getCachedWeather() ?: return false
        if (LocationRepository.isDefaultCoords(cached.snapshot.latitude, cached.snapshot.longitude)) {
            return false
        }
        lastSharedSnapshot = cached.snapshot
        lastNeighborhood = cached.neighborhood
        lastSubArea = cached.subArea
        lastCountry = cached.country
        lastHasLiveLocation = cached.hasLiveLocation
        lastLocationSource = runCatching {
            LocationSource.valueOf(cached.locationSource)
        }.getOrDefault(
            if (cached.hasLiveLocation) LocationSource.GPS else LocationSource.SAVED_CITY,
        )
        lastLat = cached.snapshot.latitude
        lastLon = cached.snapshot.longitude
        lastUpdatedAt = cached.savedAtMillis
        val minutesAgo = max(0, ((System.currentTimeMillis() - cached.savedAtMillis) / 60_000L).toInt())
        _state.value = WeatherUiState.Ready(buildUiSnapshot(cached.snapshot, minutesAgo))
        return true
    }

    suspend fun reEvaluateWithPreferences() {
        remapDisplay()
    }

    suspend fun remapDisplay() {
        val shared = lastSharedSnapshot ?: return
        val minutesAgo = max(0, ((System.currentTimeMillis() - lastUpdatedAt) / 60_000L).toInt())
        _state.value = WeatherUiState.Ready(buildUiSnapshot(shared, minutesAgo))
    }

    private suspend fun buildUiSnapshot(shared: SharedSnapshot, minutesAgo: Int): WeatherSnapshot {
        val activeModes = preferences.getActiveModes().ifEmpty { listOf(UserMode.DEFAULT) }
        val primaryMode = activeModes.first()
        val locale = preferences.getAppLocale()
        val disabled = preferences.getDisabledVerdictIds()
        val commute = preferences.getCommuteModes()
        val useCelsius = preferences.useCelsius.first()
        val use24Hour = preferences.use24Hour.first()
        val today = LocalDate.now()
        val farmerProfile = if (activeModes.contains(UserMode.FARMER)) preferences.getFarmerProfile() else null
        val employeeProfile = preferences.getEmployeeProfile().takeIf { it.hasWork }
        val workWeather = if (employeeProfile?.hasWork == true) {
            runCatching {
                openMeteo.fetchWeather(
                    employeeProfile.workLat!!,
                    employeeProfile.workLon!!,
                    employeeProfile.workLabel ?: "Work",
                    forecastDays = 1,
                )
            }.getOrNull()
        } else null
        val region = lastCountry?.takeIf { it != "India" } ?: shared.cityName.let { inferRegion(it) }
        val sensitivityAsthma = preferences.isSensitivityAsthmaEnabled()
        val sensitivityKids = preferences.isSensitivityKidsEnabled()
        val sensitivityWoman = preferences.isSensitivityWomanEnabled()
        val sensitivityPregnancy = sensitivityWoman
        val sensitivityNightSafety = sensitivityWoman

        val configs = activeModes.map { mode ->
            EvaluateConfig(
                mode = mode,
                locale = locale,
                disabledBaseIds = if (mode.usesBaseIdToggles) disabled else emptySet(),
                hiddenExactIds = if (mode.usesBaseIdToggles) emptySet() else preferences.getHiddenVerdictIdsForMode(mode.id),
                commuteModes = commute,
                month = today.monthValue,
                day = today.dayOfMonth,
                farmerProfile = farmerProfile,
                employeeProfile = employeeProfile,
                workWeather = workWeather,
                soilData = lastSoil,
                region = region,
                isKosmosPlus = preferences.isKosmosPlusActive(),
                sensitivityAsthma = sensitivityAsthma,
                sensitivityKids = sensitivityKids,
                sensitivityPregnancy = sensitivityPregnancy,
                sensitivityNightSafety = sensitivityNightSafety,
                sensitivityWoman = sensitivityWoman,
            )
        }

        val verdicts = if (configs.size == 1) {
            val config = configs.first()
            var result = KosmosEngine.evaluate(shared, config)
            if (config.mode == UserMode.DEFAULT) {
                val extras = mutableListOf<com.kosmos.shared.models.Verdict>()
                if (employeeProfile != null) {
                    extras += com.kosmos.shared.engine.ModeVerdictEngine.employeeVerdicts(
                        shared,
                        emptySet(),
                        employeeProfile,
                        workWeather,
                    )
                }
                if (sensitivityKids) {
                    extras += com.kosmos.shared.engine.ModeVerdictEngine.familyVerdicts(shared, emptySet())
                }
                if (extras.isNotEmpty()) {
                    result = KosmosEngine.mergeVerdictExtras(result, extras)
                    result = VerdictEngine.filterForDisplay(result)
                    result = VerdictEngine.sortChronologically(
                        VerdictLocalizer.localizeAll(result, locale),
                    )
                }
            }
            result
        } else {
            KosmosEngine.evaluateStacked(shared, configs)
        }

        return WeatherMapper.toUiSnapshot(
            shared = shared,
            verdicts = verdicts,
            neighborhood = lastNeighborhood,
            subArea = lastSubArea,
            country = lastCountry,
            useCelsius = useCelsius,
            use24Hour = use24Hour,
            updatedMinutesAgo = minutesAgo,
            lastUpdatedAtEpochMs = if (lastUpdatedAt > 0L) lastUpdatedAt else snapshotEpochMs(minutesAgo),
            hasLiveLocation = lastHasLiveLocation,
            locationSource = lastLocationSource,
            locale = locale,
            userMode = primaryMode,
        )
    }

    fun currentSnapshotOrNull(): WeatherSnapshot? =
        (_state.value as? WeatherUiState.Ready)?.snapshot

    fun lastSharedSnapshotOrNull(): SharedSnapshot? = lastSharedSnapshot

    private fun inferRegion(city: String): String = when {
        city.contains("Hyderabad", ignoreCase = true) -> "Telangana"
        city.contains("Bengaluru", ignoreCase = true) || city.contains("Bangalore", ignoreCase = true) -> "Karnataka"
        city.contains("Chennai", ignoreCase = true) -> "Tamil Nadu"
        city.contains("Mumbai", ignoreCase = true) -> "Maharashtra"
        city.contains("Delhi", ignoreCase = true) -> "Delhi"
        city.contains("Kochi", ignoreCase = true) -> "Kerala"
        city.contains("Bolangir", ignoreCase = true) || city.contains("Balangir", ignoreCase = true) -> "Odisha"
        else -> "India"
    }
}
