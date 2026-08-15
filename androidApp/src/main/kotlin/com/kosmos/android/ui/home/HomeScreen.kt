package com.kosmos.android.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import com.kosmos.android.R
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.InsightCard
import com.kosmos.android.ui.designsystem.atoms.KosmosIconButton
import com.kosmos.android.ui.designsystem.organisms.DayRhythm
import com.kosmos.android.ui.designsystem.organisms.HeroBlock
import com.kosmos.android.ui.designsystem.organisms.NowStats
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean,
    insightsExpanded: Boolean,
    insightsLabel: String = "INSIGHTS",
    daySummary: String? = null,
    travelState: com.kosmos.android.data.TravelState = com.kosmos.android.data.TravelState.Inactive,
    isRefreshing: Boolean,
    isLocating: Boolean = false,
    onInsightsToggle: () -> Unit,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit = onSearchClick,
    onTimelineClick: () -> Unit,
    onTravelClick: () -> Unit = {},
    onHyperLocalClick: () -> Unit = {},
    onCopyShare: ((List<com.kosmos.android.model.Verdict>) -> Unit)? = null,
    onWhatsAppShare: ((List<com.kosmos.android.model.Verdict>) -> Unit)? = null,
    onImageShare: ((List<com.kosmos.android.model.Verdict>) -> Unit)? = null,
    onRemind: ((com.kosmos.android.model.Verdict) -> Unit)? = null,
    remindedIds: Set<String> = emptySet(),
    notificationUnread: Int = 0,
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onRefresh()
        },
    )
    val heroVerdict = snapshot.verdicts.firstOrNull {
        it.priority == VerdictPriority.SEVERE || it.priority == VerdictPriority.ACTION
    } ?: snapshot.verdicts.firstOrNull()
    val insightVerdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .ifEmpty { snapshot.verdicts }
        .filter { it.id != heroVerdict?.id }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KosmosColor.bgSurface)
            .pullRefresh(pullRefreshState),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(KosmosDimens.screenTop))

            HomeLocationHeader(
                locationLabel = snapshot.headerLocationLabel(),
                locationSubtitle = snapshot.headerSubtitle(),
                onLocationClick = onLocationClick,
                isLocating = isLocating,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                notificationUnread = notificationUnread,
                showChrome = false,
                modifier = Modifier.padding(horizontal = KosmosDimens.screenHorizontal),
            )

            HeroBlock(
                snapshot = snapshot,
                showNumbers = showNumbers,
                modifier = Modifier.padding(horizontal = KosmosDimens.screenHorizontal),
            )

            NowStats(
                snapshot = snapshot,
                modifier = Modifier.padding(
                    start = KosmosDimens.screenHorizontal,
                    end = KosmosDimens.screenHorizontal,
                    top = KosmosDimens.md,
                ),
            )

            Column(
                modifier = Modifier.padding(
                    start = KosmosDimens.screenHorizontal,
                    end = KosmosDimens.screenHorizontal,
                    top = KosmosDimens.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(KosmosDimens.sm),
            ) {
                insightVerdicts.forEach { verdict ->
                    InsightCard(verdict = verdict)
                }
            }

            DayRhythm(
                hourly = snapshot.hourly.ifEmpty { snapshot.timelineHourly },
                modifier = Modifier.padding(
                    start = KosmosDimens.screenHorizontal,
                    end = KosmosDimens.screenHorizontal,
                    top = KosmosDimens.xl,
                ),
            )

            HomeStatusBar(
                label = snapshot.updatedLabel(),
                onRefresh = onRefresh,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                onNotificationsClick = onNotificationsClick,
                notificationUnread = notificationUnread,
                modifier = Modifier.padding(top = KosmosDimens.xl),
            )
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = KosmosColor.accent,
        )
    }
}

@Composable
private fun HomeStatusBar(
    label: String,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationUnread: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KosmosColor.bgSubtle)
            .padding(horizontal = KosmosDimens.screenHorizontal, vertical = KosmosDimens.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = KosmosTextStyles.caption,
            color = KosmosColor.textPrimary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            KosmosIconButton(
                painter = painterResource(R.drawable.ic_arrows_clockwise),
                contentDescription = "Refresh",
                onClick = onRefresh,
                tint = KosmosColor.textPrimary,
            )
            KosmosIconButton(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = "Notifications",
                onClick = onNotificationsClick,
                badgeCount = notificationUnread,
                tint = KosmosColor.textPrimary,
            )
            KosmosIconButton(
                painter = painterResource(R.drawable.ic_magnifying_glass),
                contentDescription = "Search city",
                onClick = onSearchClick,
                tint = KosmosColor.textPrimary,
            )
            KosmosIconButton(
                painter = painterResource(R.drawable.ic_gear),
                contentDescription = "Settings",
                onClick = onSettingsClick,
                tint = KosmosColor.textPrimary,
            )
        }
    }
}

private fun WeatherSnapshot.updatedLabel(): String {
    val raw = refreshLabel.ifBlank {
        if (updatedMinutesAgo <= 0) "Updated just now" else "Updated ${updatedMinutesAgo}m ago"
    }
    return raw.replaceFirst("refreshed", "Updated", ignoreCase = true)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
