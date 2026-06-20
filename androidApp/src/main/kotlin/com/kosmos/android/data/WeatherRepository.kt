package com.kosmos.android.data

import android.content.Context
import com.kosmos.android.BuildConfig
import com.kosmos.shared.engine.EvaluateConfig
import com.kosmos.shared.engine.KosmosEngine
import com.kosmos.shared.i18n.AppLocale
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

    private var lastSharedSnapshot: SharedSnapshot? = null
    private var lastNeighborhood: String? = null
    private var lastCountry: String? = null
    private var lastSoil: com.kosmos.shared.farmer.SoilData? = null
    private var lastUpdatedAt: Long = 0L
    private var lastHasLiveLocation: Boolean = false
    private var lastLocationSource: LocationSource = LocationSource.SAVED_CITY
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    suspend fun startup(hasLocationPermission: Boolean) = refreshMutex.withLock {
        if (hasLocationPermission && preferences.isUsingLiveGps()) {
            _state.value = WeatherUiState.Locating
            resolveAndLoad(hasLocationPermission, forceLiveGps = true)?.let { return@withLock }
        }
        if (seedFromCache()) return@withLock
        resolveAndLoad(hasLocationPermission, forceLiveGps = hasLocationPermission)
    }

    suspend fun refresh(hasLocationPermission: Boolean, pullToRefresh: Boolean = false): Result<Unit> =
        refreshMutex.withLock {
            val keepContent = pullToRefresh && _state.value is WeatherUiState.Ready
            if (keepContent) {
                _isRefreshing.value = true
            } else if (hasLocationPermission && preferences.isUsingLiveGps()) {
                _state.value = WeatherUiState.Locating
            } else if (!keepContent) {
                _state.value = WeatherUiState.Loading
            }

            return try {
                val ok = resolveAndLoad(
                    hasLocationPermission,
                    forceLiveGps = hasLocationPermission && preferences.isUsingLiveGps(),
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
            _state.value = WeatherUiState.Locating
            _isRefreshing.value = hadContent

            return try {
                preferences.setUsingLiveGps(true)
                val ok = resolveAndLoad(hasLocationPermission, forceLiveGps = true)
                if (ok == true) {
                    Result.success(Unit)
                } else {
                    _state.value = WeatherUiState.Error(
                        "GPS fix failed. Step outside or pick Bolangir from Change location.",
                    )
                    Result.failure(IllegalStateException("Could not get GPS location"))
                }
            } catch (e: Exception) {
                _state.value = WeatherUiState.Error(e.message ?: "Could not get current location")
                Result.failure(e)
            } finally {
                _isRefreshing.value = false
            }
        }

    suspend fun refreshAt(geo: GeoLocation, hasLiveLocation: Boolean = false): Result<Unit> =
        refreshMutex.withLock {
            _state.value = WeatherUiState.Loading
            return try {
                if (!hasLiveLocation) {
                    preferences.setUsingLiveGps(false)
                }
                loadWeather(geo, hasLiveLocation, if (hasLiveLocation) LocationSource.GPS else LocationSource.SAVED_CITY)
                Result.success(Unit)
            } catch (e: Exception) {
                _state.value = WeatherUiState.Error(e.message ?: "Could not load weather")
                Result.failure(e)
            }
        }

    private suspend fun resolveAndLoad(hasLocationPermission: Boolean, forceLiveGps: Boolean): Boolean? {
        val resolved = locationRepo.resolveForWeather(hasLocationPermission, forceLiveGps) ?: return null
        if (LocationRepository.isDefaultLocation(resolved.geo) && hasLocationPermission) return null
        loadWeather(resolved.geo, resolved.isLiveGps, resolved.source)
        return true
    }

    private suspend fun loadWeather(
        geo: GeoLocation,
        hasLiveLocation: Boolean,
        locationSource: LocationSource,
    ) {
        applyGeo(geo, hasLiveLocation, locationSource)
        _travelState.value = travelRepo.evaluateFromGeo(geo, hasLiveLocation)
        val isPlus = preferences.isKosmosPlusActive()
        val mode = preferences.getUserMode()
        val forecastDays = if (isPlus || mode == UserMode.FARMER) 10 else 2
        val shared = multiSource.fetchWeather(geo.latitude, geo.longitude, geo.city, forecastDays)
        if (mode == UserMode.FARMER) {
            val profile = preferences.getFarmerProfile()
            val plotLat = profile.plotLat ?: geo.latitude
            val plotLon = profile.plotLon ?: geo.longitude
            lastSoil = runCatching {
                openMeteo.fetchSoil(plotLat, plotLon, shared.currentHour)
            }.getOrNull()
        } else {
            lastSoil = null
        }
        if (!LocationRepository.isDefaultLocation(geo)) {
            preferences.saveLocation(geo)
        }
        publish(shared)
    }

    private fun applyGeo(geo: GeoLocation, hasLiveLocation: Boolean, locationSource: LocationSource) {
        lastNeighborhood = geo.neighborhood
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
                country = lastCountry,
                hasLiveLocation = lastHasLiveLocation,
                locationSource = lastLocationSource.name,
                savedAtMillis = lastUpdatedAt,
            ),
        )
        _state.value = WeatherUiState.Ready(buildUiSnapshot(shared, 0))
    }

    suspend fun seedFromCache(): Boolean {
        val cached = preferences.getCachedWeather() ?: return false
        if (LocationRepository.isDefaultCoords(cached.snapshot.latitude, cached.snapshot.longitude)) {
            return false
        }
        lastSharedSnapshot = cached.snapshot
        lastNeighborhood = cached.neighborhood
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
        val mode = preferences.getUserMode()
        val locale = preferences.getAppLocale()
        val disabled = preferences.getDisabledVerdictIds()
        val hidden = preferences.getHiddenVerdictIdsForMode(mode.id)
        val commute = preferences.getCommuteModes()
        val useCelsius = preferences.useCelsius.first()
        val use24Hour = preferences.use24Hour.first()
        val today = LocalDate.now()
        val farmerProfile = if (mode == UserMode.FARMER) preferences.getFarmerProfile() else null
        val region = lastCountry?.takeIf { it != "India" } ?: shared.cityName.let { inferRegion(it) }

        val config = EvaluateConfig(
            mode = mode,
            locale = locale,
            disabledBaseIds = if (mode.usesBaseIdToggles) disabled else emptySet(),
            hiddenExactIds = if (mode.usesBaseIdToggles) emptySet() else hidden,
            commuteModes = commute,
            month = today.monthValue,
            day = today.dayOfMonth,
            farmerProfile = farmerProfile,
            soilData = lastSoil,
            region = region,
            isKosmosPlus = preferences.isKosmosPlusActive(),
        )

        val verdicts = KosmosEngine.evaluate(shared, config)

        return WeatherMapper.toUiSnapshot(
            shared = shared,
            verdicts = verdicts,
            neighborhood = lastNeighborhood,
            country = lastCountry,
            useCelsius = useCelsius,
            use24Hour = use24Hour,
            updatedMinutesAgo = minutesAgo,
            hasLiveLocation = lastHasLiveLocation,
            locationSourceLabel = lastLocationSource.displayLabel(),
            locale = locale,
            userMode = mode,
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
