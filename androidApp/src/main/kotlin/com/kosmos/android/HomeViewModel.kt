package com.kosmos.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kosmos.android.data.DestinationRepository
import com.kosmos.android.data.HyperLocalRepository
import com.kosmos.android.data.HyperLocalResult
import com.kosmos.android.data.KosmosPlusRepository
import com.kosmos.android.data.TravelDashboardData
import com.kosmos.android.data.TravelDashboardRepository
import com.kosmos.android.data.PreferencesRepository
import com.kosmos.android.data.WeatherRepository
import com.kosmos.android.data.WeatherUiState
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.data.GeoLocation
import com.kosmos.android.data.LocationRepository
import android.content.Intent
import com.kosmos.android.i18n.LocaleManager
import com.kosmos.android.notification.ReminderScheduler
import com.kosmos.android.service.BriefReader
import com.kosmos.android.service.WeatherRefreshScheduler
import com.kosmos.android.service.WeatherWallpaperHelper
import com.kosmos.android.ui.designsystem.molecules.ModeCardData
import com.kosmos.android.widget.WidgetConstants
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.i18n.LocaleStrings
import com.kosmos.shared.mode.ModeCatalog
import com.kosmos.shared.mode.ModeAvailability
import com.kosmos.shared.mode.UserMode
import com.kosmos.shared.travel.DestinationResult
import com.kosmos.android.util.VerdictShareHelper
import com.kosmos.shared.farmer.FarmerProfile
import com.kosmos.shared.mode.EmployeeProfile
import com.kosmos.shared.travel.TravelFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepo = WeatherRepository(application)
    private val prefsRepo = PreferencesRepository(application)
    private val destinationRepo = DestinationRepository()
    private val travelDashboardRepo = TravelDashboardRepository()
    private val plusRepo = KosmosPlusRepository(application, prefsRepo, viewModelScope)
    private val briefReader = BriefReader(application)
    private val locationRepo = LocationRepository(
        application,
        com.kosmos.shared.api.createHttpClient(),
    )

    private val hyperLocalRepo = HyperLocalRepository(locationRepo)

    val weatherState: StateFlow<WeatherUiState> = weatherRepo.state

    val useCelsius: StateFlow<Boolean> = prefsRepo.useCelsius
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val use24Hour: StateFlow<Boolean> = prefsRepo.use24Hour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val useDarkMode: StateFlow<Boolean> = prefsRepo.useDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showNumbers: StateFlow<Boolean> = prefsRepo.showNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val appLocale: StateFlow<AppLocale> = prefsRepo.appLocale
        .mapToLocale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLocale.EN)

    val userMode: StateFlow<UserMode> = prefsRepo.userMode
        .mapToMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserMode.DEFAULT)

    val activeModeIds: StateFlow<List<String>> = prefsRepo.activeModeOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf(UserMode.DEFAULT.id))

    val commuteModes: StateFlow<Set<String>> = prefsRepo.commuteModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val isKosmosPlus: StateFlow<Boolean> = plusRepo.isPlus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val addedModes: StateFlow<Set<String>> = prefsRepo.addedModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val reminderIds: StateFlow<Set<String>> = prefsRepo.reminderIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val isRefreshing: StateFlow<Boolean> = weatherRepo.isRefreshing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val refreshIntervalMinutes: StateFlow<Int> = prefsRepo.refreshIntervalMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)

    val travelState: StateFlow<com.kosmos.android.data.TravelState> = weatherRepo.travelState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.kosmos.android.data.TravelState.Inactive)

    private val _insightsExpanded = MutableStateFlow(true)
    val insightsExpanded: StateFlow<Boolean> = _insightsExpanded.asStateFlow()

    private val _showCitySearch = MutableStateFlow(false)
    val showCitySearch: StateFlow<Boolean> = _showCitySearch.asStateFlow()

    private val _showChat = MutableStateFlow(false)
    val showChat: StateFlow<Boolean> = _showChat.asStateFlow()

    private val _showPlus = MutableStateFlow(false)
    val showPlus: StateFlow<Boolean> = _showPlus.asStateFlow()

    private val _enabledVerdicts = MutableStateFlow<Set<String>>(emptySet())
    val enabledVerdicts: StateFlow<Set<String>> = _enabledVerdicts.asStateFlow()

    private val _verdictToggles = MutableStateFlow<List<Pair<String, String>>>(PreferencesRepository.allVerdictToggles)
    val verdictToggles: StateFlow<List<Pair<String, String>>> = _verdictToggles.asStateFlow()

    private val _chatRemaining = MutableStateFlow(PreferencesRepository.FREE_CHAT_DAILY)
    val chatRemaining: StateFlow<Int> = _chatRemaining.asStateFlow()

    private var locationPermissionGranted = false

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    val isLocating: StateFlow<Boolean> = weatherRepo.isLocating
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _locationMessage = MutableStateFlow<String?>(null)
    val locationMessage: StateFlow<String?> = _locationMessage.asStateFlow()

    private var permissionRequester: (() -> Unit)? = null

    private val _travelFilters = MutableStateFlow(TravelFilters())
    val travelFilters: StateFlow<TravelFilters> = _travelFilters.asStateFlow()

    private val _travelResults = MutableStateFlow<List<DestinationResult>>(emptyList())
    val travelResults: StateFlow<List<DestinationResult>> = _travelResults.asStateFlow()

    private val _showTripResults = MutableStateFlow(false)
    val showTripResults: StateFlow<Boolean> = _showTripResults.asStateFlow()

    private val _travelLoading = MutableStateFlow(false)
    val travelLoading: StateFlow<Boolean> = _travelLoading.asStateFlow()

    private val _travelContextLine = MutableStateFlow("")
    val travelContextLine: StateFlow<String> = _travelContextLine.asStateFlow()

    private val _manualKm = MutableStateFlow("")
    val manualKm: StateFlow<String> = _manualKm.asStateFlow()

    private val _travelDashboard = MutableStateFlow<TravelDashboardData?>(null)
    val travelDashboard: StateFlow<TravelDashboardData?> = _travelDashboard.asStateFlow()

    private val _travelDashboardVerdicts = MutableStateFlow<List<com.kosmos.android.model.Verdict>>(emptyList())
    val travelDashboardVerdicts: StateFlow<List<com.kosmos.android.model.Verdict>> = _travelDashboardVerdicts.asStateFlow()

    private val _travelDashboardLoading = MutableStateFlow(false)
    val travelDashboardLoading: StateFlow<Boolean> = _travelDashboardLoading.asStateFlow()

    private val _hyperLocalResult = MutableStateFlow<HyperLocalResult?>(null)
    val hyperLocalResult: StateFlow<HyperLocalResult?> = _hyperLocalResult.asStateFlow()

    private val _hyperLocalLoading = MutableStateFlow(false)
    val hyperLocalLoading: StateFlow<Boolean> = _hyperLocalLoading.asStateFlow()

    private val _farmerProfile = MutableStateFlow(FarmerProfile())
    val farmerProfile: StateFlow<FarmerProfile> = _farmerProfile.asStateFlow()

    private val _employeeProfile = MutableStateFlow(EmployeeProfile())
    val employeeProfile: StateFlow<EmployeeProfile> = _employeeProfile.asStateFlow()

    private val _pendingEmployeeSetup = MutableStateFlow(false)
    val pendingEmployeeSetup: StateFlow<Boolean> = _pendingEmployeeSetup.asStateFlow()

    private val _radarResult = MutableStateFlow<com.kosmos.android.data.RainViewerClient.RadarResult?>(null)
    val radarResult: StateFlow<com.kosmos.android.data.RainViewerClient.RadarResult?> = _radarResult.asStateFlow()

    private val _radarLoading = MutableStateFlow(false)
    val radarLoading: StateFlow<Boolean> = _radarLoading.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val diaryEntries: StateFlow<List<Pair<String, String>>> = _diaryEntries.asStateFlow()

    private val _weeklyDigestEnabled = MutableStateFlow(true)
    val weeklyDigestEnabled: StateFlow<Boolean> = _weeklyDigestEnabled.asStateFlow()

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    private val _morningBriefEnabled = MutableStateFlow(true)
    val morningBriefEnabled: StateFlow<Boolean> = _morningBriefEnabled.asStateFlow()

    private val _savedPlaces = MutableStateFlow<List<com.kosmos.android.data.SavedPlace>>(emptyList())
    val savedPlaces: StateFlow<List<com.kosmos.android.data.SavedPlace>> = _savedPlaces.asStateFlow()

    private val _verdictCategories = MutableStateFlow<List<com.kosmos.android.data.VerdictCategory>>(emptyList())
    val verdictCategories: StateFlow<List<com.kosmos.android.data.VerdictCategory>> = _verdictCategories.asStateFlow()

    private val _notifications = MutableStateFlow<List<com.kosmos.android.notification.NotificationEntry>>(emptyList())
    val notifications: StateFlow<List<com.kosmos.android.notification.NotificationEntry>> = _notifications.asStateFlow()

    private val _scheduledReminders = MutableStateFlow<List<com.kosmos.android.notification.ScheduledReminder>>(emptyList())
    val scheduledReminders: StateFlow<List<com.kosmos.android.notification.ScheduledReminder>> = _scheduledReminders.asStateFlow()

    private val _placeSearchResults = MutableStateFlow<List<com.kosmos.android.data.GeocodeSearchResult>>(emptyList())
    val placeSearchResults: StateFlow<List<com.kosmos.android.data.GeocodeSearchResult>> = _placeSearchResults.asStateFlow()

    private val _homeSearchResults = MutableStateFlow<List<com.kosmos.android.data.GeocodeSearchResult>>(emptyList())
    val homeSearchResults: StateFlow<List<com.kosmos.android.data.GeocodeSearchResult>> = _homeSearchResults.asStateFlow()

    private var tripSearchJob: Job? = null
    private var homeSearchJob: Job? = null

    private val _tripWizardDraft = MutableStateFlow<com.kosmos.android.data.TripWizardDraft?>(null)
    val tripWizardDraft: StateFlow<com.kosmos.android.data.TripWizardDraft?> = _tripWizardDraft.asStateFlow()

    private val _sensitivityAsthma = MutableStateFlow(false)
    val sensitivityAsthma: StateFlow<Boolean> = _sensitivityAsthma.asStateFlow()

    private val _sensitivityKids = MutableStateFlow(false)
    val sensitivityKids: StateFlow<Boolean> = _sensitivityKids.asStateFlow()

    private val _sensitivityWoman = MutableStateFlow(false)
    val sensitivityWoman: StateFlow<Boolean> = _sensitivityWoman.asStateFlow()

    private val _sensitivityPregnancy = MutableStateFlow(false)
    val sensitivityPregnancy: StateFlow<Boolean> = _sensitivityPregnancy.asStateFlow()

    private val _sensitivityNightSafety = MutableStateFlow(false)
    val sensitivityNightSafety: StateFlow<Boolean> = _sensitivityNightSafety.asStateFlow()

    private val _notificationUnread = MutableStateFlow(0)
    val notificationUnread: StateFlow<Int> = _notificationUnread.asStateFlow()

    private val rainViewer = com.kosmos.android.data.RainViewerClient(
        com.kosmos.shared.api.createHttpClient(),
    )

    init {
        viewModelScope.launch {
            LocaleManager.applyLocale(getApplication(), prefsRepo.getAppLocale())
            plusRepo.refreshStatus()
            refreshEnabledVerdicts()
            _farmerProfile.value = prefsRepo.getFarmerProfile()
            _employeeProfile.value = prefsRepo.getEmployeeProfile()
            _weeklyDigestEnabled.value = prefsRepo.isWeeklyDigestEnabled()
            _morningBriefEnabled.value = prefsRepo.isMorningBriefEnabled()
            _onboardingDone.value = prefsRepo.isOnboardingDone()
            _savedPlaces.value = prefsRepo.getSavedPlaces()
            refreshVerdictCategories()
            _diaryEntries.value = prefsRepo.getDiaryEntries()
            loadNotifications()
            _tripWizardDraft.value = prefsRepo.getTripWizardDraft()
            _sensitivityAsthma.value = prefsRepo.isSensitivityAsthmaEnabled()
            _sensitivityKids.value = prefsRepo.isSensitivityKidsEnabled()
            _sensitivityWoman.value = prefsRepo.isSensitivityWomanEnabled()
            _sensitivityPregnancy.value = prefsRepo.isSensitivityPregnancyEnabled()
            _sensitivityNightSafety.value = prefsRepo.isSensitivityNightSafetyEnabled()
            if (prefsRepo.getActiveModeIds().any { it != UserMode.DEFAULT.id }) {
                prefsRepo.setUserMode(UserMode.DEFAULT.id)
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = prefsRepo.getNotifications()
            _notificationUnread.value = prefsRepo.getNotificationsUnreadCount()
            _scheduledReminders.value = prefsRepo.getScheduledReminders()
        }
    }

    fun cancelScheduledReminder(verdictId: String) {
        viewModelScope.launch {
            prefsRepo.removeReminder(verdictId)
            ReminderScheduler.cancel(getApplication(), verdictId)
            loadNotifications()
        }
    }

    fun searchTripDestinations(query: String) {
        tripSearchJob?.cancel()
        if (query.trim().length < 2) {
            _placeSearchResults.value = emptyList()
            return
        }
        tripSearchJob = viewModelScope.launch {
            delay(300)
            _placeSearchResults.value = locationRepo.searchPlaces(query)
        }
    }

    fun searchHomePlaces(query: String) {
        homeSearchJob?.cancel()
        if (query.trim().length < 2) {
            _homeSearchResults.value = emptyList()
            return
        }
        homeSearchJob = viewModelScope.launch {
            delay(300)
            _homeSearchResults.value = locationRepo.searchPlaces(query)
        }
    }

    fun saveTripWizardDraft(draft: com.kosmos.android.data.TripWizardDraft) {
        viewModelScope.launch {
            prefsRepo.saveTripWizardDraft(draft)
            _tripWizardDraft.value = draft
        }
    }

    fun toggleSensitivityAsthma() {
        viewModelScope.launch {
            val next = !_sensitivityAsthma.value
            prefsRepo.setSensitivityAsthma(next)
            _sensitivityAsthma.value = next
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun toggleSensitivityKids() {
        viewModelScope.launch {
            val next = !_sensitivityKids.value
            prefsRepo.setSensitivityKids(next)
            _sensitivityKids.value = next
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun toggleSensitivityWoman() {
        viewModelScope.launch {
            val next = !_sensitivityWoman.value
            prefsRepo.setSensitivityWoman(next)
            _sensitivityWoman.value = next
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun toggleSensitivityPregnancy() {
        viewModelScope.launch {
            val next = !_sensitivityPregnancy.value
            prefsRepo.setSensitivityPregnancy(next)
            _sensitivityPregnancy.value = next
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun toggleSensitivityNightSafety() {
        viewModelScope.launch {
            val next = !_sensitivityNightSafety.value
            prefsRepo.setSensitivityNightSafety(next)
            _sensitivityNightSafety.value = next
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun markNotificationsSeen() {
        viewModelScope.launch {
            prefsRepo.markNotificationsSeen()
            _notificationUnread.value = 0
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val snap = currentSnapshot()
            val body = snap.verdicts.take(2).joinToString(" · ") { it.title }.ifBlank {
                "${snap.conditionLabel} · ${snap.temp}°"
            }
            com.kosmos.android.notification.NotificationLogger.log(
                getApplication(),
                title = "Kosmos · ${snap.appBarTitle.ifBlank { snap.city }}",
                body = body,
                type = "test",
            )
            loadNotifications()
        }
    }

    private var hasBootstrapped = false

    fun bootstrap(hasLocationPermission: Boolean) {
        if (hasBootstrapped) return
        hasBootstrapped = true
        locationPermissionGranted = hasLocationPermission
        _hasLocationPermission.value = hasLocationPermission
        viewModelScope.launch {
            if (hasLocationPermission) {
                prefsRepo.setUsingLiveGps(true)
            }
            weatherRepo.startup(hasLocationPermission)
        }
    }

    private suspend fun refreshVerdictCategories() {
        _verdictCategories.value = prefsRepo.verdictCategories(prefsRepo.getUserMode())
    }

    fun setLocationPermission(granted: Boolean) {
        locationPermissionGranted = granted
        _hasLocationPermission.value = granted
        viewModelScope.launch {
            if (granted) {
                prefsRepo.setUsingLiveGps(true)
                if (!hasBootstrapped) {
                    bootstrap(true)
                } else {
                    refreshCurrentLocationInternal(showErrors = true)
                }
            } else {
                if (!hasBootstrapped) {
                    bootstrap(false)
                } else {
                    refreshWeather()
                }
            }
        }
    }

    fun registerPermissionRequester(requester: () -> Unit) {
        permissionRequester = requester
    }

    fun requestLocationPermission() {
        permissionRequester?.invoke()
    }

    fun clearLocationMessage() {
        _locationMessage.value = null
    }

    fun recheckLocationPermission(hasPermission: Boolean) {
        val wasGranted = locationPermissionGranted
        locationPermissionGranted = hasPermission
        _hasLocationPermission.value = hasPermission
        viewModelScope.launch {
            if (hasPermission && !wasGranted) {
                prefsRepo.setUsingLiveGps(true)
                if (!hasBootstrapped) {
                    bootstrap(true)
                } else {
                    refreshCurrentLocationInternal(showErrors = false)
                }
            }
        }
    }

    fun refreshWeather(pullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (locationPermissionGranted) {
                refreshCurrentLocationInternal(showErrors = false)
            } else {
                weatherRepo.refresh(locationPermissionGranted, pullToRefresh)
            }
        }
    }

    fun currentSnapshot(): WeatherSnapshot = when (val state = weatherState.value) {
        is WeatherUiState.Ready -> state.snapshot
        else -> PrototypeData.hyderabadSummerDay
    }

    private fun currentSnapshotOrNull(): WeatherSnapshot? = weatherRepo.currentSnapshotOrNull()

    fun insightsSectionLabel(): String {
        val locale = appLocale.value
        val modes = activeModeIds.value.map { UserMode.fromId(it) }
        val lenses = modes.filter { it.isStackableLens && it != UserMode.DEFAULT }
        return when {
            lenses.size >= 2 -> lenses.joinToString(" + ") { modeShortInsightsLabel(it, locale) }
            modes.size == 1 -> LocaleStrings.ui(modes.first().insightsLabelKey, locale)
            else -> LocaleStrings.ui(UserMode.DEFAULT.insightsLabelKey, locale)
        }
    }

    private fun modeShortInsightsLabel(mode: UserMode, locale: AppLocale): String = when (mode) {
        UserMode.EMPLOYEE -> LocaleStrings.ui("insights_short_work", locale)
        UserMode.FAMILY -> LocaleStrings.ui("insights_short_family", locale)
        UserMode.PHOTOGRAPHER -> LocaleStrings.ui("insights_short_photo", locale)
        UserMode.HOMEMAKER -> LocaleStrings.ui("insights_short_home", locale)
        UserMode.DEFAULT -> LocaleStrings.ui("insights_short_default", locale)
        UserMode.ELDER -> LocaleStrings.ui("insights_short_elder", locale)
        UserMode.FARMER -> LocaleStrings.ui("insights_short_farmer", locale)
    }

    fun modeCards(): List<ModeCardData> {
        val locale = appLocale.value
        return catalogToCards(ModeCatalog.live, locale)
    }

    fun comingSoonModeCards(): List<ModeCardData> {
        val locale = appLocale.value
        return catalogToCards(ModeCatalog.comingSoon, locale)
    }

    private fun catalogToCards(
        entries: List<com.kosmos.shared.mode.ModeCatalogEntry>,
        locale: com.kosmos.shared.i18n.AppLocale,
    ): List<ModeCardData> = entries.map { entry ->
        ModeCardData(
            id = entry.id,
            emoji = entry.emoji,
            name = LocaleStrings.ui(entry.nameKey, locale),
            description = LocaleStrings.ui(entry.descKey, locale),
            phase = if (entry.availability == ModeAvailability.COMING_SOON) {
                LocaleStrings.ui("coming_soon", locale)
            } else {
                LocaleStrings.ui("phase_v1", locale)
            },
            locked = false,
            isComingSoon = entry.availability == ModeAvailability.COMING_SOON,
            plannedInsights = entry.insightKeys.map { LocaleStrings.ui(it, locale) },
        )
    }

    fun toggleInsights() {
        _insightsExpanded.value = !_insightsExpanded.value
    }

    fun onCitySelected(cityLabel: String) {
        viewModelScope.launch {
            val geo = LocationRepository.presetCities
                .firstOrNull { it.first == cityLabel }?.second
                ?: locationRepo.geocodeCity(cityLabel.substringBefore(",").trim())
            if (geo != null) {
                loadWeatherAt(geo)
            } else {
                _locationMessage.value = "Could not find that city"
            }
            _showCitySearch.value = false
            _homeSearchResults.value = emptyList()
        }
    }

    fun onPlaceSelected(result: com.kosmos.android.data.GeocodeSearchResult) {
        viewModelScope.launch {
            val country = result.detail.substringAfter(" · ", "").takeIf { it.isNotBlank() && it != result.detail }
            val geo = GeoLocation(
                city = result.label,
                neighborhood = null,
                country = country,
                latitude = result.latitude,
                longitude = result.longitude,
            )
            loadWeatherAt(geo)
            _showCitySearch.value = false
            _homeSearchResults.value = emptyList()
        }
    }

    private suspend fun loadWeatherAt(geo: GeoLocation) {
        val result = weatherRepo.refreshAt(geo, hasLiveLocation = false)
        if (result.isFailure) {
            _locationMessage.value = result.exceptionOrNull()?.message ?: "Could not load that city"
        }
    }

    fun useCurrentLocation() {
        if (!locationPermissionGranted) {
            requestLocationPermission()
            return
        }
        viewModelScope.launch {
            refreshCurrentLocationInternal(showErrors = true)
            loadHyperLocalCurrent()
        }
    }

    private suspend fun refreshCurrentLocationInternal(showErrors: Boolean) {
        val result = weatherRepo.refreshCurrentLocation(locationPermissionGranted)
        if (result.isFailure && showErrors) {
            _locationMessage.value = result.exceptionOrNull()?.message ?: "Could not get current location"
        } else if (result.isSuccess) {
            _showCitySearch.value = false
        }
    }

    fun toggleVerdict(baseId: String) {
        viewModelScope.launch {
            val mode = prefsRepo.getUserMode()
            if (mode.usesBaseIdToggles) {
                prefsRepo.toggleVerdict(baseId)
            } else {
                prefsRepo.toggleModeVerdict(mode.id, baseId)
            }
            refreshEnabledVerdicts()
            refreshVerdictCategories()
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    private suspend fun refreshEnabledVerdicts() {
        val mode = prefsRepo.getUserMode()
        _verdictToggles.value = prefsRepo.modeVerdictToggles(mode)
        val toggles = _verdictToggles.value.map { it.first }
        if (mode.usesBaseIdToggles) {
            val disabled = prefsRepo.getDisabledVerdictIds()
            _enabledVerdicts.value = toggles.filter { it !in disabled }.toSet()
        } else {
            val hidden = prefsRepo.getHiddenVerdictIdsForMode(mode.id)
            _enabledVerdicts.value = toggles.filter { it !in hidden }.toSet()
        }
    }

    fun setLocale(locale: AppLocale) {
        viewModelScope.launch {
            prefsRepo.setAppLocale(locale.code)
            LocaleManager.applyLocale(getApplication(), locale)
            weatherRepo.remapDisplay()
        }
    }

    fun setUserMode(modeId: String) {
        toggleActiveMode(modeId, forceActivate = true)
    }

    fun toggleActiveMode(modeId: String, forceActivate: Boolean = false) {
        if (!UserMode.isActivatable(modeId)) return
        viewModelScope.launch {
            if (forceActivate) {
                prefsRepo.setUserMode(modeId)
                if (modeId != UserMode.DEFAULT.id) prefsRepo.addMode(modeId)
            } else {
                prefsRepo.toggleActiveMode(modeId)
            }
            refreshEnabledVerdicts()
            refreshVerdictCategories()
            weatherRepo.refresh(locationPermissionGranted)
            val active = prefsRepo.getActiveModeIds()
            if (UserMode.EMPLOYEE.id in active && !prefsRepo.getEmployeeProfile().hasWork) {
                _pendingEmployeeSetup.value = true
            }
        }
    }

    fun activateTakeoverMode(modeId: String) {
        if (!UserMode.isActivatable(modeId)) return
        viewModelScope.launch {
            prefsRepo.activateTakeoverMode(modeId)
            prefsRepo.addMode(modeId)
            refreshEnabledVerdicts()
            refreshVerdictCategories()
            weatherRepo.refresh(locationPermissionGranted)
        }
    }

    fun clearPendingEmployeeSetup() {
        _pendingEmployeeSetup.value = false
    }

    fun useCurrentLocationAsEmployeeHome() {
        val snap = currentSnapshotOrNull() ?: return
        _employeeProfile.value = _employeeProfile.value.copy(
            homeLabel = snap.locationHeadline.ifBlank { snap.locationLine },
            homeLat = snap.latitude,
            homeLon = snap.longitude,
        )
    }

    fun saveEmployeeProfile(profile: EmployeeProfile) {
        viewModelScope.launch {
            var updated = profile
            profile.workLabel?.let { work ->
                locationRepo.geocodeCity(work)?.let { geo ->
                    updated = updated.copy(
                        workLat = geo.latitude,
                        workLon = geo.longitude,
                        workLabel = geo.city,
                    )
                }
            }
            if (!updated.hasHome) {
                val snap = currentSnapshotOrNull()
                if (snap != null) {
                    updated = updated.copy(
                        homeLabel = snap.locationHeadline.ifBlank { snap.locationLine },
                        homeLat = snap.latitude,
                        homeLon = snap.longitude,
                    )
                }
            }
            prefsRepo.saveEmployeeProfile(updated)
            _employeeProfile.value = updated
            _pendingEmployeeSetup.value = false
            weatherRepo.refresh(locationPermissionGranted)
        }
    }

    fun addMode(modeId: String) {
        if (!UserMode.isActivatable(modeId)) return
        viewModelScope.launch {
            val ok = prefsRepo.addMode(modeId)
            if (!ok) {
                _locationMessage.value = LocaleStrings.ui("mode_cap_reached", appLocale.value)
            }
        }
    }

    fun removeMode(modeId: String) {
        viewModelScope.launch {
            val wasActive = modeId in prefsRepo.getActiveModeIds()
            prefsRepo.removeMode(modeId)
            if (wasActive) {
                refreshEnabledVerdicts()
                refreshVerdictCategories()
                weatherRepo.refresh(locationPermissionGranted)
            }
        }
    }

    fun toggleReminder(verdict: Verdict) {
        val window = verdict.timeWindow ?: return
        viewModelScope.launch {
            if (verdict.id in reminderIds.value) {
                prefsRepo.removeReminder(verdict.id)
                ReminderScheduler.cancel(getApplication(), verdict.id)
                return@launch
            }
            val scheduled = ReminderScheduler.schedule(
                context = getApplication(),
                verdictId = verdict.id,
                emoji = verdict.emoji,
                title = verdict.title,
                detail = verdict.detail,
                startHour = window.startHour,
            )
            if (!scheduled) {
                _locationMessage.value = "That window has already passed today"
                return@launch
            }
            val allowed = prefsRepo.addReminder(verdict.id)
            if (!allowed) {
                ReminderScheduler.cancel(getApplication(), verdict.id)
                _locationMessage.value = LocaleStrings.ui("reminder_capped", appLocale.value)
                return@launch
            }
            ReminderScheduler.fireTimeMs(window.startHour)?.let { fireTime ->
                prefsRepo.upsertScheduledReminder(
                    com.kosmos.android.notification.ScheduledReminder(
                        verdictId = verdict.id,
                        emoji = verdict.emoji,
                        title = verdict.title,
                        detail = verdict.detail,
                        fireTimeMs = fireTime,
                    ),
                )
                loadNotifications()
            }
        }
    }

    fun toggleCommute(commuteId: String) {
        viewModelScope.launch {
            prefsRepo.toggleCommute(commuteId)
            weatherRepo.reEvaluateWithPreferences()
        }
    }

    fun readBriefAloud() {
        val snapshot = currentSnapshot()
        briefReader.readBrief(snapshot)
    }

    fun openVerdictDeepLink(verdictId: String) {
        _insightsExpanded.value = true
    }

    fun openCitySearch() {
        _homeSearchResults.value = emptyList()
        _showCitySearch.value = true
    }
    fun closeCitySearch() {
        _showCitySearch.value = false
        _homeSearchResults.value = emptyList()
    }
    fun openChat() {
        viewModelScope.launch {
            _chatRemaining.value = prefsRepo.getChatMessagesRemaining()
            _showChat.value = true
        }
    }
    fun closeChat() { _showChat.value = false }
    fun openPlus() { _showPlus.value = true }
    fun closePlus() { _showPlus.value = false }

    fun launchPurchase(activity: android.app.Activity) {
        plusRepo.connect(activity)
        plusRepo.launchPurchase(activity)
    }

    fun refreshChatRemaining() {
        viewModelScope.launch {
            _chatRemaining.value = prefsRepo.getChatMessagesRemaining()
        }
    }

    fun handleWidgetIntent(intent: Intent?) {
        intent?.getStringExtra(WidgetConstants.EXTRA_VERDICT_ID)?.let { openVerdictDeepLink(it) }
    }

    fun toggleCelsius() {
        viewModelScope.launch {
            prefsRepo.setUseCelsius(!useCelsius.value)
            weatherRepo.remapDisplay()
        }
    }

    fun toggle24Hour() {
        viewModelScope.launch {
            prefsRepo.setUse24Hour(!use24Hour.value)
            weatherRepo.remapDisplay()
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            prefsRepo.setUseDarkMode(!useDarkMode.value)
        }
    }

    fun toggleShowNumbers() {
        viewModelScope.launch {
            prefsRepo.setShowNumbers(!showNumbers.value)
        }
    }

    fun loadDestinations() {
        val snapshot = currentSnapshot()
        _travelContextLine.value = destinationRepo.contextLine(snapshot.tempCelsius, _travelFilters.value)
        viewModelScope.launch {
            _travelLoading.value = true
            try {
                val results = destinationRepo.loadDestinations(
                    fromLat = snapshot.latitude,
                    fromLon = snapshot.longitude,
                    homeTemp = snapshot.tempCelsius,
                    filters = _travelFilters.value,
                )
                _travelResults.value = results
            } finally {
                _travelLoading.value = false
            }
        }
    }

    fun toggleTravelVibe(vibeId: String) {
        val current = _travelFilters.value.vibes.toMutableSet()
        val exclusive = "any"
        if (vibeId == exclusive) {
            if (current == setOf(exclusive)) return
            _travelFilters.value = _travelFilters.value.copy(vibes = setOf(exclusive))
        } else {
            current.remove(exclusive)
            if (vibeId in current) {
                if (current.size > 1) current.remove(vibeId)
            } else {
                current.add(vibeId)
            }
            if (current.isEmpty()) current.add(exclusive)
            _travelFilters.value = _travelFilters.value.copy(vibes = current)
        }
        loadDestinations()
    }

    fun toggleTravelAudience(audienceId: String) {
        val current = _travelFilters.value.audiences.toMutableSet()
        val exclusive = "all"
        if (audienceId == exclusive) {
            if (current == setOf(exclusive)) return
            _travelFilters.value = _travelFilters.value.copy(audiences = setOf(exclusive))
        } else {
            current.remove(exclusive)
            if (audienceId in current) {
                if (current.size > 1) current.remove(audienceId)
            } else {
                current.add(audienceId)
            }
            if (current.isEmpty()) current.add(exclusive)
            _travelFilters.value = _travelFilters.value.copy(audiences = current)
        }
        loadDestinations()
    }

    fun selectTravelDistance(km: Int?) {
        _travelFilters.value = _travelFilters.value.copy(distanceMaxKm = km)
        _manualKm.value = ""
        loadDestinations()
    }

    fun setManualKm(value: String) {
        _manualKm.value = value.filter { it.isDigit() }.take(4)
        val km = _manualKm.value.toIntOrNull()
        if (km != null && km > 0) {
            _travelFilters.value = _travelFilters.value.copy(distanceMaxKm = km)
            loadDestinations()
        }
    }

    fun selectTravelRegion(region: String) {
        _travelFilters.value = _travelFilters.value.copy(region = region)
        loadDestinations()
    }

    fun selectTripDayOffset(offset: Int) {
        _travelFilters.value = _travelFilters.value.copy(tripDayOffset = offset)
        loadDestinations()
    }

    fun selectTripGroup(group: String) {
        _travelFilters.value = _travelFilters.value.copy(groupSize = group)
        loadDestinations()
    }

    fun selectTripTransport(transport: String) {
        _travelFilters.value = _travelFilters.value.copy(transport = transport)
        loadDestinations()
    }

    fun applyTravelFilters(filters: TravelFilters, manualKm: String) {
        _travelFilters.value = filters
        _manualKm.value = manualKm.filter { it.isDigit() }.take(4)
        loadDestinations()
    }

    fun completeTripWizard(filters: TravelFilters) {
        _travelFilters.value = filters
        _showTripResults.value = true
        viewModelScope.launch { prefsRepo.clearTripWizardDraft() }
        _tripWizardDraft.value = null
        loadDestinations()
    }

    fun editTripPlan() {
        _showTripResults.value = false
    }

    fun resetTravelFilters() {
        _travelFilters.value = TravelFilters()
        _manualKm.value = ""
        _showTripResults.value = false
        viewModelScope.launch { prefsRepo.clearTripWizardDraft() }
        _tripWizardDraft.value = null
        loadDestinations()
    }

    fun refreshOnForeground() {
        viewModelScope.launch {
            if (locationPermissionGranted) {
                refreshCurrentLocationInternal(showErrors = false)
            } else {
                weatherRepo.refresh(locationPermissionGranted, pullToRefresh = true)
            }
        }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.setRefreshIntervalMinutes(minutes)
            WeatherRefreshScheduler.schedule(getApplication(), minutes)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefsRepo.setUserMode(UserMode.DEFAULT.id)
            refreshEnabledVerdicts()
            refreshVerdictCategories()
            prefsRepo.setOnboardingDone(true)
            _onboardingDone.value = true
            if (!hasBootstrapped) {
                weatherRepo.startup(locationPermissionGranted)
            } else {
                weatherRepo.refresh(locationPermissionGranted)
            }
        }
    }

    fun toggleMorningBrief() {
        viewModelScope.launch {
            val next = !prefsRepo.isMorningBriefEnabled()
            prefsRepo.setMorningBriefEnabled(next)
            _morningBriefEnabled.value = next
        }
    }

    fun loadHyperLocalCurrent() {
        viewModelScope.launch {
            _hyperLocalLoading.value = true
            try {
                val snapshot = currentSnapshot()
                val geo = GeoLocation(
                    city = snapshot.city,
                    neighborhood = snapshot.locationLine.substringBefore(",").trim().takeIf { it != snapshot.city },
                    country = null,
                    latitude = snapshot.latitude,
                    longitude = snapshot.longitude,
                )
                _hyperLocalResult.value = hyperLocalRepo.analyzeGeo(geo, useCelsius.value)
            } finally {
                _hyperLocalLoading.value = false
            }
        }
    }

    fun addSavedPlaceFromQuery(query: String) {
        viewModelScope.launch {
            val geo = locationRepo.geocodeCity(query) ?: return@launch
            val label = geo.neighborhood ?: geo.city
            val place = com.kosmos.android.data.SavedPlace(
                id = "${geo.latitude},${geo.longitude}",
                label = label,
                latitude = geo.latitude,
                longitude = geo.longitude,
                city = geo.city,
            )
            prefsRepo.addSavedPlace(place)
            _savedPlaces.value = prefsRepo.getSavedPlaces()
        }
    }

    fun selectSavedPlace(place: com.kosmos.android.data.SavedPlace) {
        viewModelScope.launch {
            val geo = GeoLocation(
                city = place.city,
                neighborhood = place.label,
                latitude = place.latitude,
                longitude = place.longitude,
            )
            weatherRepo.refreshAt(geo, hasLiveLocation = false)
            loadHyperLocalForGeo(geo)
        }
    }

    fun removeSavedPlace(place: com.kosmos.android.data.SavedPlace) {
        viewModelScope.launch {
            prefsRepo.removeSavedPlace(place.id)
            _savedPlaces.value = prefsRepo.getSavedPlaces()
        }
    }

    private suspend fun loadHyperLocalForGeo(geo: GeoLocation) {
        _hyperLocalLoading.value = true
        try {
            _hyperLocalResult.value = hyperLocalRepo.analyzeGeo(geo, useCelsius.value)
        } finally {
            _hyperLocalLoading.value = false
        }
    }

    fun loadTravelDashboard() {
        val travel = travelState.value
        if (!travel.isActive) return
        val shared = weatherRepo.lastSharedSnapshotOrNull() ?: return
        viewModelScope.launch {
            _travelDashboardLoading.value = true
            try {
                val useCelsius = useCelsius.value
                val data = travelDashboardRepo.load(shared, travel, useCelsius)
                _travelDashboard.value = data
                _travelDashboardVerdicts.value = data?.travelVerdicts?.let {
                    com.kosmos.android.data.WeatherMapper.toUiVerdicts(it)
                } ?: emptyList()
            } finally {
                _travelDashboardLoading.value = false
            }
        }
    }

    fun searchHyperLocal(query: String) {
        viewModelScope.launch {
            _hyperLocalLoading.value = true
            try {
                _hyperLocalResult.value = hyperLocalRepo.analyze(query, useCelsius.value)
            } finally {
                _hyperLocalLoading.value = false
            }
        }
    }

    fun saveFarmerProfile(profile: FarmerProfile) {
        viewModelScope.launch {
            var updated = profile
            profile.plotCity?.let { city ->
                locationRepo.geocodeCity(city)?.let { geo ->
                    updated = profile.copy(
                        plotLat = geo.latitude,
                        plotLon = geo.longitude,
                        plotCity = geo.city,
                    )
                }
            }
            prefsRepo.saveFarmerProfile(updated)
            _farmerProfile.value = updated
            weatherRepo.refresh(locationPermissionGranted)
        }
    }

    fun copyVerdictShare(verdict: Verdict) {
        val snapshot = currentSnapshot()
        val text = VerdictShareHelper.formatShareText(snapshot.locationLine, verdict)
        VerdictShareHelper.copyToClipboard(getApplication(), text)
    }

    fun whatsAppVerdictShare(verdict: Verdict) {
        val snapshot = currentSnapshot()
        val text = VerdictShareHelper.formatShareText(snapshot.locationLine, verdict)
        VerdictShareHelper.shareViaWhatsApp(getApplication(), text)
    }

    fun imageVerdictShare(verdict: Verdict, activity: android.app.Activity) {
        val snapshot = currentSnapshot()
        VerdictShareHelper.shareAsImage(activity, snapshot.locationLine, verdict)
    }

    fun loadRadar() {
        viewModelScope.launch {
            _radarLoading.value = true
            val snap = currentSnapshotOrNull() ?: currentSnapshot()
            _radarResult.value = runCatching {
                rainViewer.loadRadar(snap.latitude, snap.longitude)
            }.getOrNull()
            _radarLoading.value = false
        }
    }

    fun radarCaption(): String {
        val snap = currentSnapshotOrNull() ?: return LocaleStrings.ui("radar_clear", appLocale.value)
        val rainSoon = snap.nowcastIsWet || snap.verdicts.any {
            it.id.startsWith("rain") || it.id.startsWith("umbrella")
        }
        return LocaleStrings.ui(
            if (rainSoon) "radar_approaching" else "radar_clear",
            appLocale.value,
        )
    }

    fun loadDiary() {
        viewModelScope.launch {
            _diaryEntries.value = prefsRepo.getDiaryEntries()
        }
    }

    fun saveDiaryEntry(text: String) {
        viewModelScope.launch {
            prefsRepo.addDiaryEntry(text)
            _diaryEntries.value = prefsRepo.getDiaryEntries()
        }
    }

    fun toggleWeeklyDigest() {
        viewModelScope.launch {
            val next = !_weeklyDigestEnabled.value
            prefsRepo.setWeeklyDigestEnabled(next)
            _weeklyDigestEnabled.value = next
        }
    }

    fun applyWeatherWallpaper(): Boolean {
        val snapshot = currentSnapshot()
        return WeatherWallpaperHelper.applyFromSnapshot(
            getApplication(),
            snapshot.condition,
            snapshot.temp,
        )
    }

    override fun onCleared() {
        briefReader.shutdown()
        super.onCleared()
    }

    private fun kotlinx.coroutines.flow.Flow<String>.mapToLocale(): kotlinx.coroutines.flow.Flow<AppLocale> =
        map { AppLocale.fromCode(it) }

    private fun kotlinx.coroutines.flow.Flow<String>.mapToMode(): kotlinx.coroutines.flow.Flow<UserMode> =
        map { UserMode.fromId(it) }
}
