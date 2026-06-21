package com.kosmos.android.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kosmos.android.HomeViewModel
import com.kosmos.android.data.LocationRepository
import com.kosmos.android.data.WeatherUiState
import com.kosmos.android.i18n.LocalAppLocale
import com.kosmos.android.i18n.localized
import com.kosmos.android.ui.chat.ChatSheet
import com.kosmos.android.ui.components.CitySearchSheet
import com.kosmos.android.ui.farmer.FarmerHomeScreen
import com.kosmos.android.ui.farmer.FarmerSetupScreen
import com.kosmos.android.ui.employee.EmployeeSetupScreen
import com.kosmos.android.ui.home.ElderHomeScreen
import com.kosmos.android.ui.home.HomeScreen
import com.kosmos.android.ui.home.HomeSkeleton
import com.kosmos.android.ui.onboarding.OnboardingScreen
import com.kosmos.android.ui.plus.KosmosPlusScreen
import com.kosmos.android.ui.settings.SettingsScreen
import com.kosmos.android.ui.modes.AlphaModesPlaceholderScreen
import com.kosmos.android.ui.timeline.TimelineScreen
import com.kosmos.android.ui.travel.HyperLocalScreen
import com.kosmos.android.ui.diary.WeatherDiaryScreen
import com.kosmos.android.ui.notifications.NotificationInboxScreen
import com.kosmos.android.ui.radar.RadarScreen
import com.kosmos.android.ui.travel.TravelDashboardScreen
import com.kosmos.android.ui.travel.TripPlanTab
import com.kosmos.android.ui.widget.WidgetPreviewScreen
import com.kosmos.shared.mode.UserMode

object Routes {
    const val HOME = "home"
    const val TIMELINE = "timeline"
    const val MODES = "modes"
    const val SETTINGS = "settings"
    const val WIDGET = "widget"
    const val PLUS = "plus"
    const val TRAVEL = "travel"
    const val TRAVEL_DASHBOARD = "travel_dashboard"
    const val HYPER_LOCAL = "hyper_local"
    const val FARMER_SETUP = "farmer_setup"
    const val EMPLOYEE_SETUP = "employee_setup"
    const val RADAR = "radar"
    const val DIARY = "weather_diary"
    const val NOTIFICATIONS = "notifications"
}

@Composable
fun KosmosNavHost(
    viewModel: HomeViewModel,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()
    val insightsExpanded by viewModel.insightsExpanded.collectAsState()
    val showCitySearch by viewModel.showCitySearch.collectAsState()
    val showChat by viewModel.showChat.collectAsState()
    val showPlus by viewModel.showPlus.collectAsState()
    val useCelsius by viewModel.useCelsius.collectAsState()
    val use24Hour by viewModel.use24Hour.collectAsState()
    val useDarkMode by viewModel.useDarkMode.collectAsState()
    val showNumbers by viewModel.showNumbers.collectAsState()
    val appLocale by viewModel.appLocale.collectAsState()
    val userMode by viewModel.userMode.collectAsState()
    val activeModeIds by viewModel.activeModeIds.collectAsState()
    val commuteModes by viewModel.commuteModes.collectAsState()
    val isKosmosPlus by viewModel.isKosmosPlus.collectAsState()
    val addedModes by viewModel.addedModes.collectAsState()
    val reminderIds by viewModel.reminderIds.collectAsState()
    val enabledVerdicts by viewModel.enabledVerdicts.collectAsState()
    val verdictToggles by viewModel.verdictToggles.collectAsState()
    val chatRemaining by viewModel.chatRemaining.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val locationMessage by viewModel.locationMessage.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val travelState by viewModel.travelState.collectAsState()
    val travelFilters by viewModel.travelFilters.collectAsState()
    val travelResults by viewModel.travelResults.collectAsState()
    val travelLoading by viewModel.travelLoading.collectAsState()
    val travelContextLine by viewModel.travelContextLine.collectAsState()
    val manualKm by viewModel.manualKm.collectAsState()
    val travelDashboard by viewModel.travelDashboard.collectAsState()
    val travelDashboardVerdicts by viewModel.travelDashboardVerdicts.collectAsState()
    val travelDashboardLoading by viewModel.travelDashboardLoading.collectAsState()
    val showTripResults by viewModel.showTripResults.collectAsState()
    val hyperLocalResult by viewModel.hyperLocalResult.collectAsState()
    val hyperLocalLoading by viewModel.hyperLocalLoading.collectAsState()
    val farmerProfile by viewModel.farmerProfile.collectAsState()
    val employeeProfile by viewModel.employeeProfile.collectAsState()
    val pendingEmployeeSetup by viewModel.pendingEmployeeSetup.collectAsState()
    val radarResult by viewModel.radarResult.collectAsState()
    val radarLoading by viewModel.radarLoading.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val weeklyDigestEnabled by viewModel.weeklyDigestEnabled.collectAsState()
    val onboardingDone by viewModel.onboardingDone.collectAsState()
    val morningBriefEnabled by viewModel.morningBriefEnabled.collectAsState()
    val refreshIntervalMinutes by viewModel.refreshIntervalMinutes.collectAsState()
    val savedPlaces by viewModel.savedPlaces.collectAsState()
    val verdictCategories by viewModel.verdictCategories.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val scheduledReminders by viewModel.scheduledReminders.collectAsState()
    val homeSearchResults by viewModel.homeSearchResults.collectAsState()
    val placeSearchResults by viewModel.placeSearchResults.collectAsState()
    val tripWizardDraft by viewModel.tripWizardDraft.collectAsState()
    val sensitivityAsthma by viewModel.sensitivityAsthma.collectAsState()
    val sensitivityKids by viewModel.sensitivityKids.collectAsState()
    val sensitivityWoman by viewModel.sensitivityWoman.collectAsState()
    val notificationUnread by viewModel.notificationUnread.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    if (!onboardingDone) {
        com.kosmos.android.ui.designsystem.tokens.KosmosTheme {
            OnboardingScreen(
                onComplete = viewModel::completeOnboarding,
                onRequestLocation = viewModel::requestLocationPermission,
                onRequestNotifications = { /* requested at first launch in MainActivity */ },
            )
        }
        return
    }

    LaunchedEffect(locationMessage) {
        locationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearLocationMessage()
        }
    }

    LaunchedEffect(pendingEmployeeSetup) {
        if (pendingEmployeeSetup) {
            navController.navigate(Routes.EMPLOYEE_SETUP)
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalAppLocale provides appLocale) {
        when (val state = weatherState) {
            is WeatherUiState.Loading, is WeatherUiState.Locating -> {
                HomeSkeleton(
                    message = if (isLocating || hasLocationPermission || state is WeatherUiState.Locating) {
                        "Finding your area…"
                    } else {
                        "Loading weather…"
                    },
                )
            }
            is WeatherUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = { viewModel.refreshWeather() }) {
                            Text(localized("try_again"))
                        }
                    }
                }
            }
            is WeatherUiState.Ready -> {
                val snapshot = state.snapshot
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
                val topLevelRoutes = setOf(Routes.HOME, Routes.TRAVEL)
                val showBottomBar = currentRoute in topLevelRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (showBottomBar) {
                            KosmosBottomBar(currentRoute) { route ->
                                if (route != currentRoute) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        }
                    },
                ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                ) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        when (userMode) {
                            UserMode.ELDER -> ElderHomeScreen(
                                snapshot = snapshot,
                                showNumbers = showNumbers,
                                isRefreshing = isRefreshing,
                                isLocating = isLocating,
                                onRefresh = { viewModel.refreshWeather(pullToRefresh = true) },
                                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                                onReadAloud = viewModel::readBriefAloud,
                                onLocationClick = viewModel::openCitySearch,
                            )
                            UserMode.FARMER -> FarmerHomeScreen(
                                snapshot = snapshot,
                                daily = snapshot.daily,
                                showNumbers = showNumbers,
                                plotLabel = farmerProfile.plotCity,
                                onSettingsClick = { navController.navigate(Routes.FARMER_SETUP) },
                                onReadAloud = viewModel::readBriefAloud,
                            )
                            else -> HomeScreen(
                                snapshot = snapshot,
                                showNumbers = showNumbers,
                                insightsExpanded = insightsExpanded,
                                insightsLabel = viewModel.insightsSectionLabel(),
                                travelState = travelState,
                                isRefreshing = isRefreshing,
                                isLocating = isLocating,
                                onInsightsToggle = viewModel::toggleInsights,
                                onRefresh = { viewModel.refreshWeather(pullToRefresh = true) },
                                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                                onSearchClick = viewModel::openCitySearch,
                                onLocationClick = viewModel::openCitySearch,
                                onTimelineClick = { navController.navigate(Routes.TIMELINE) },
                                onChatClick = viewModel::openChat,
                                onTravelClick = {
                                    if (travelState.isActive) {
                                        viewModel.loadTravelDashboard()
                                        navController.navigate(Routes.TRAVEL_DASHBOARD)
                                    } else {
                                        viewModel.loadDestinations()
                                        navController.navigate(Routes.TRAVEL)
                                    }
                                },
                                onHyperLocalClick = {
                                    viewModel.loadHyperLocalCurrent()
                                    navController.navigate(Routes.HYPER_LOCAL)
                                },
                                onCopyShare = viewModel::copyVerdictShare,
                                onWhatsAppShare = viewModel::whatsAppVerdictShare,
                                onImageShare = { verdict ->
                                    (context as? android.app.Activity)?.let {
                                        viewModel.imageVerdictShare(verdict, it)
                                    }
                                },
                                onRemind = viewModel::toggleReminder,
                                remindedIds = reminderIds,
                                notificationUnread = notificationUnread,
                                onNotificationsClick = {
                                    viewModel.loadNotifications()
                                    navController.navigate(Routes.NOTIFICATIONS)
                                },
                            )
                        }
                    }

                    composable(Routes.TRAVEL) {
                        TripPlanTab(
                            filters = travelFilters,
                            results = travelResults,
                            contextLine = travelContextLine,
                            isLoading = travelLoading,
                            manualKm = manualKm,
                            useCelsius = useCelsius,
                            showResults = showTripResults,
                            savedDraft = tripWizardDraft,
                            searchResults = placeSearchResults,
                            onSearchQueryChange = viewModel::searchTripDestinations,
                            onDraftChange = viewModel::saveTripWizardDraft,
                            onWizardComplete = viewModel::completeTripWizard,
                            onEditPlan = viewModel::editTripPlan,
                            onApplyFilters = viewModel::applyTravelFilters,
                            onResetFilters = viewModel::resetTravelFilters,
                        )
                    }

                    composable(Routes.TIMELINE) {
                        TimelineScreen(
                            snapshot = snapshot,
                            showTenDay = isKosmosPlus && snapshot.daily.size > 2,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.MODES) {
                        AlphaModesPlaceholderScreen()
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            verdictToggles = verdictToggles,
                            verdictCategories = verdictCategories,
                            enabledVerdicts = enabledVerdicts,
                            useCelsius = useCelsius,
                            use24Hour = use24Hour,
                            useDarkMode = useDarkMode,
                            showNumbers = showNumbers,
                            appLocale = appLocale,
                            commuteModes = commuteModes,
                            morningBriefEnabled = morningBriefEnabled,
                            workPlaceLabel = employeeProfile.workLabel.orEmpty(),
                            onVerdictToggle = viewModel::toggleVerdict,
                            onCelsiusToggle = viewModel::toggleCelsius,
                            on24HourToggle = viewModel::toggle24Hour,
                            onDarkModeToggle = viewModel::toggleDarkMode,
                            onShowNumbersToggle = viewModel::toggleShowNumbers,
                            onLocaleSelect = viewModel::setLocale,
                            onCommuteToggle = viewModel::toggleCommute,
                            onMorningBriefToggle = viewModel::toggleMorningBrief,
                            onWorkPlaceClick = { navController.navigate(Routes.EMPLOYEE_SETUP) },
                            onWidgetPreviewClick = { navController.navigate(Routes.WIDGET) },
                            onRadarClick = {
                                viewModel.loadRadar()
                                navController.navigate(Routes.RADAR)
                            },
                            onDiaryClick = {
                                viewModel.loadDiary()
                                navController.navigate(Routes.DIARY)
                            },
                            onWallpaperClick = { viewModel.applyWeatherWallpaper() },
                            onTripPlannerClick = {
                                viewModel.editTripPlan()
                                navController.navigate(Routes.TRAVEL)
                            },
                            onEmployeeSetupClick = { navController.navigate(Routes.EMPLOYEE_SETUP) },
                            onNotificationsClick = {
                                viewModel.loadNotifications()
                                navController.navigate(Routes.NOTIFICATIONS)
                            },
                            onSendTestNotification = viewModel::sendTestNotification,
                            refreshIntervalMinutes = refreshIntervalMinutes,
                            onRefreshIntervalSelect = viewModel::setRefreshInterval,
                            weeklyDigestEnabled = weeklyDigestEnabled,
                            onWeeklyDigestToggle = viewModel::toggleWeeklyDigest,
                            sensitivityAsthma = sensitivityAsthma,
                            sensitivityKids = sensitivityKids,
                            sensitivityWoman = sensitivityWoman,
                            onSensitivityAsthmaToggle = viewModel::toggleSensitivityAsthma,
                            onSensitivityKidsToggle = viewModel::toggleSensitivityKids,
                            onSensitivityWomanToggle = viewModel::toggleSensitivityWoman,
                            locationLine = snapshot.appBarTitle.ifBlank { snapshot.locationLine },
                            locationSourceLabel = snapshot.locationSourceLabel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.WIDGET) {
                        WidgetPreviewScreen(
                            snapshot = snapshot,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.PLUS) {
                        KosmosPlusScreen(
                            isPlus = isKosmosPlus,
                            onSubscribe = {
                                (context as? android.app.Activity)?.let { viewModel.launchPurchase(it) }
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.TRAVEL_DASHBOARD) {
                        TravelDashboardScreen(
                            snapshot = snapshot,
                            travelState = travelState,
                            dashboard = travelDashboard,
                            travelVerdicts = travelDashboardVerdicts,
                            isLoading = travelDashboardLoading,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.HYPER_LOCAL) {
                        HyperLocalScreen(
                            result = hyperLocalResult,
                            isLoading = hyperLocalLoading,
                            currentLocationLabel = snapshot.locationLine,
                            savedPlaces = savedPlaces,
                            onBack = { navController.popBackStack() },
                            onSearch = viewModel::searchHyperLocal,
                            onUseCurrentLocation = {
                                viewModel.useCurrentLocation()
                                viewModel.loadHyperLocalCurrent()
                            },
                            onSelectPlace = viewModel::selectSavedPlace,
                            onRemovePlace = viewModel::removeSavedPlace,
                            onAddPlaceFromSearch = viewModel::addSavedPlaceFromQuery,
                        )
                    }

                    composable(Routes.FARMER_SETUP) {
                        FarmerSetupScreen(
                            profile = farmerProfile,
                            onBack = { navController.popBackStack() },
                            onSave = { profile ->
                                viewModel.saveFarmerProfile(profile)
                                navController.popBackStack()
                            },
                        )
                    }

                    composable(Routes.EMPLOYEE_SETUP) {
                        EmployeeSetupScreen(
                            profile = employeeProfile,
                            currentLocationLine = snapshot.locationLine,
                            onBack = {
                                viewModel.clearPendingEmployeeSetup()
                                navController.popBackStack()
                            },
                            onUseCurrentLocation = viewModel::useCurrentLocationAsEmployeeHome,
                            onSave = { profile ->
                                viewModel.saveEmployeeProfile(profile)
                                navController.popBackStack()
                            },
                            onSkip = {
                                viewModel.clearPendingEmployeeSetup()
                                navController.popBackStack()
                            },
                        )
                    }

                    composable(Routes.RADAR) {
                        RadarScreen(
                            radarResult = radarResult,
                            isLoading = radarLoading,
                            locationLine = snapshot.locationLine,
                            caption = viewModel.radarCaption(),
                            onBack = { navController.popBackStack() },
                            onRetry = viewModel::loadRadar,
                        )
                    }

                    composable(Routes.DIARY) {
                        WeatherDiaryScreen(
                            entries = diaryEntries,
                            onBack = { navController.popBackStack() },
                            onSaveEntry = viewModel::saveDiaryEntry,
                        )
                    }

                    composable(Routes.NOTIFICATIONS) {
                        NotificationInboxScreen(
                            entries = notifications,
                            upcomingReminders = scheduledReminders,
                            onBack = {
                                viewModel.markNotificationsSeen()
                                navController.popBackStack()
                            },
                            onCancelReminder = viewModel::cancelScheduledReminder,
                        )
                    }
                }

            if (showCitySearch) {
                CitySearchSheet(
                    searchResults = homeSearchResults,
                    currentLocationLine = snapshot.locationLine,
                    hasLocationPermission = hasLocationPermission,
                    isLocating = isLocating,
                    onQueryChange = viewModel::searchHomePlaces,
                    onUseCurrentLocation = viewModel::useCurrentLocation,
                    onPlaceSelected = viewModel::onPlaceSelected,
                    onDismiss = viewModel::closeCitySearch,
                )
            }

                if (showChat) {
                    ChatSheet(
                        weatherContext = "${snapshot.locationLine}: ${snapshot.temp}°, ${snapshot.conditionLabel}, ${snapshot.verdicts.take(3).joinToString("; ") { it.title }}",
                        chatRemaining = chatRemaining,
                        localeCode = appLocale.code,
                        isKosmosPlus = isKosmosPlus,
                        onDismiss = viewModel::closeChat,
                        onRemainingChanged = { viewModel.refreshChatRemaining() },
                    )
                }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                    )
                }
                }
            }
        }
    }
}

@Composable
private fun KosmosBottomBar(
    currentRoute: String,
    onSelect: (String) -> Unit,
) {
    data class Tab(val route: String, val emoji: String, val labelKey: String)
    val tabs = listOf(
        Tab(Routes.HOME, "🏠", "tab_today"),
        Tab(Routes.TRAVEL, "🧭", "tab_plan"),
    )
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onSelect(tab.route) },
                icon = { Text(tab.emoji, fontSize = 20.sp) },
                label = { Text(localized(tab.labelKey)) },
            )
        }
    }
}
