package com.kosmos.android.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.VerdictCategory
import com.kosmos.android.i18n.localized
import com.kosmos.android.ui.designsystem.molecules.CommuteChipRow
import com.kosmos.android.ui.designsystem.molecules.SettingsLinkRow
import com.kosmos.android.ui.designsystem.molecules.SettingsRow
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.i18n.AppLocale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    verdictToggles: List<Pair<String, String>>,
    verdictCategories: List<VerdictCategory>,
    enabledVerdicts: Set<String>,
    useCelsius: Boolean,
    use24Hour: Boolean,
    useDarkMode: Boolean,
    showNumbers: Boolean,
    appLocale: AppLocale,
    commuteModes: Set<String>,
    morningBriefEnabled: Boolean,
    workPlaceLabel: String = "",
    onVerdictToggle: (String) -> Unit,
    onCelsiusToggle: () -> Unit,
    on24HourToggle: () -> Unit,
    onDarkModeToggle: () -> Unit,
    onShowNumbersToggle: () -> Unit,
    onLocaleSelect: (AppLocale) -> Unit,
    onCommuteToggle: (String) -> Unit,
    onMorningBriefToggle: () -> Unit,
    onWorkPlaceClick: () -> Unit = {},
    onWidgetPreviewClick: () -> Unit,
    onRadarClick: () -> Unit = {},
    onDiaryClick: () -> Unit = {},
    onWallpaperClick: () -> Unit = {},
    onTripPlannerClick: () -> Unit = {},
    onEmployeeSetupClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSendTestNotification: () -> Unit = {},
    refreshIntervalMinutes: Int = 30,
    onRefreshIntervalSelect: (Int) -> Unit = {},
    weeklyDigestEnabled: Boolean = true,
    onWeeklyDigestToggle: () -> Unit = {},
    sensitivityAsthma: Boolean = false,
    sensitivityKids: Boolean = false,
    sensitivityWoman: Boolean = false,
    onSensitivityAsthmaToggle: () -> Unit = {},
    onSensitivityKidsToggle: () -> Unit = {},
    onSensitivityWomanToggle: () -> Unit = {},
    locationLine: String = "",
    locationSourceLabel: String = "",
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(localized("settings"), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            item {
                if (locationLine.isNotBlank()) {
                    SectionHeader("Location")
                    Text(
                        text = locationLine,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    if (locationSourceLabel.isNotBlank()) {
                        Text(
                            text = "Source: $locationSourceLabel",
                            style = KosmosTextStyles.settingsSubtitle,
                            color = KosmosThemeExt.colors.textSecondary,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                }

                SectionHeader("Daily brief")
                SettingsLinkRow(
                    title = localized("notifications_title"),
                    subtitle = localized("notifications_empty_sub"),
                    onClick = onNotificationsClick,
                )
                SettingsLinkRow(
                    title = localized("send_test_notification"),
                    subtitle = localized("send_test_notification_sub"),
                    onClick = onSendTestNotification,
                )
                Text(
                    text = "Every day around 7 AM, Komos sends a short weather brief — rain timing, heat, air, and what to plan. Same insights as Home, in your notification shade.",
                    style = KosmosTextStyles.settingsSubtitle,
                    color = KosmosThemeExt.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SettingsRow(
                    title = "Morning brief at 7 AM",
                    subtitle = if (morningBriefEnabled) "On · notification around 7 AM" else "Off",
                    checked = morningBriefEnabled,
                    onToggle = onMorningBriefToggle,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader(localized("language"))
                AppLocale.entries.forEach { locale ->
                    SettingsRow(
                        title = locale.nativeName,
                        subtitle = locale.displayName,
                        checked = appLocale == locale,
                        onToggle = { if (appLocale != locale) onLocaleSelect(locale) },
                    )
                }
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader("Your day")
                SettingsLinkRow(
                    title = if (workPlaceLabel.isNotBlank()) "Work: $workPlaceLabel" else "Add your work place",
                    subtitle = if (workPlaceLabel.isNotBlank()) {
                        "Commute insights use your office location"
                    } else {
                        "Optional — unlock commute timing for your route"
                    },
                    onClick = onWorkPlaceClick,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader(localized("commute"))
                CommuteChipRow(
                    selected = commuteModes,
                    onToggle = onCommuteToggle,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader("About you (optional)")
                Text(
                    text = "Turn on only what applies — Komos adds extra insights on top of your day.",
                    style = KosmosTextStyles.settingsSubtitle,
                    color = KosmosThemeExt.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SettingsRow(
                    title = "I have kids",
                    subtitle = if (sensitivityKids) "School run, playground, kids' air" else "Standard outdoor limits",
                    checked = sensitivityKids,
                    onToggle = onSensitivityKidsToggle,
                )
                SettingsRow(
                    title = "I am a woman",
                    subtitle = if (sensitivityWoman) "SPF, humidity, evening safety" else "Standard day insights",
                    checked = sensitivityWoman,
                    onToggle = onSensitivityWomanToggle,
                )
                SettingsRow(
                    title = "Asthma / allergy",
                    subtitle = if (sensitivityAsthma) "Stricter air-quality alerts" else "Standard air alerts",
                    checked = sensitivityAsthma,
                    onToggle = onSensitivityAsthmaToggle,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader("Plan a getaway")
                SettingsLinkRow(
                    title = "Trip planner",
                    subtitle = "When you're going, group size, transport — weather-first picks",
                    onClick = onTripPlannerClick,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader("Updates")
                Text(
                    text = "How often Komos refreshes weather in the background. Pull down on Home anytime for an instant update.",
                    style = KosmosTextStyles.settingsSubtitle,
                    color = KosmosThemeExt.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                RefreshIntervalRow(
                    selectedMinutes = refreshIntervalMinutes,
                    onSelect = onRefreshIntervalSelect,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionHeader(localized("appearance"))
                SettingsRow(
                    title = localized("dark_mode"),
                    subtitle = if (useDarkMode) localized("dark_mode_on") else localized("dark_mode_off"),
                    checked = useDarkMode,
                    onToggle = onDarkModeToggle,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader(localized("units_format"))
                SettingsRow(
                    title = localized("temperature"),
                    subtitle = if (useCelsius) localized("celsius") else localized("fahrenheit"),
                    checked = useCelsius,
                    onToggle = onCelsiusToggle,
                )
                SettingsRow(
                    title = localized("time_format"),
                    subtitle = if (use24Hour) localized("hour_24") else localized("hour_12"),
                    checked = use24Hour,
                    onToggle = on24HourToggle,
                )
                SettingsRow(
                    title = localized("show_numbers"),
                    subtitle = if (showNumbers) localized("show_numbers_on") else localized("show_numbers_off"),
                    checked = showNumbers,
                    onToggle = onShowNumbersToggle,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader(localized("alerts"))
                SettingsRow(
                    title = localized("weekly_digest"),
                    subtitle = if (weeklyDigestEnabled) localized("weekly_digest_on") else localized("weekly_digest_off"),
                    checked = weeklyDigestEnabled,
                    onToggle = onWeeklyDigestToggle,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader(localized("weather_depth"))
                SettingsLinkRow(
                    title = localized("rain_radar"),
                    subtitle = localized("rain_radar_sub"),
                    onClick = onRadarClick,
                )
                SettingsLinkRow(
                    title = localized("weather_diary"),
                    subtitle = localized("weather_diary_sub"),
                    onClick = onDiaryClick,
                )
                SettingsLinkRow(
                    title = localized("weather_wallpaper"),
                    subtitle = localized("weather_wallpaper_sub"),
                    onClick = onWallpaperClick,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader("Data & privacy")
                Text(
                    text = "Komos sends your coordinates to Open-Meteo for weather and air quality. AI chat goes to Pollinations.ai. Location is only used for forecasts — we do not sell or track you.",
                    style = KosmosTextStyles.settingsSubtitle,
                    color = KosmosThemeExt.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader(localized("widget"))
                SettingsLinkRow(
                    title = localized("widget_preview"),
                    subtitle = localized("widget_preview_sub"),
                    onClick = onWidgetPreviewClick,
                )
                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
                SectionHeader("${localized("customize")} · ${enabledVerdicts.size}/${verdictToggles.size}")
                Text(
                    text = localized("customize_sub"),
                    style = KosmosTextStyles.settingsSubtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            if (verdictCategories.isNotEmpty()) {
                items(verdictCategories) { category ->
                    val expanded = expandedCategories[category.title] ?: true
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(bottom = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCategories[category.title] = !expanded }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = category.title,
                                style = KosmosTextStyles.settingsTitle,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                            )
                        }
                        if (expanded) {
                            category.toggles.forEach { (id, label) ->
                                SettingsRow(
                                    title = label,
                                    subtitle = if (id in enabledVerdicts) localized("toggle_on") else localized("toggle_off"),
                                    checked = id in enabledVerdicts,
                                    onToggle = { onVerdictToggle(id) },
                                )
                            }
                        }
                    }
                }
            } else {
                items(verdictToggles) { (id, label) ->
                    SettingsRow(
                        title = label,
                        subtitle = if (id in enabledVerdicts) localized("toggle_on") else localized("toggle_off"),
                        checked = id in enabledVerdicts,
                        onToggle = { onVerdictToggle(id) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RefreshIntervalRow(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(
        15 to "15 min",
        30 to "30 min",
        60 to "1 hr",
        180 to "3 hr",
        0 to "Manual only",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        options.forEach { (minutes, label) ->
            val selected = selectedMinutes == minutes
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) com.kosmos.android.ui.designsystem.tokens.KosmosColor.primary
                else KosmosThemeExt.colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (selected) com.kosmos.android.ui.designsystem.tokens.KosmosColor.primary
                        else KosmosThemeExt.colors.border,
                        RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(minutes) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 12.dp),
    )
    HorizontalDivider(color = KosmosThemeExt.colors.border)
}
