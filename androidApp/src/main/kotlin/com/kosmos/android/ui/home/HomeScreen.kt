package com.kosmos.android.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.KosmosCardRow
import com.kosmos.android.ui.designsystem.atoms.KosmosFab
import com.kosmos.android.ui.designsystem.atoms.KosmosIconButton
import com.kosmos.android.ui.designsystem.atoms.SectionLabel
import com.kosmos.android.ui.designsystem.molecules.UpdatesFeed
import com.kosmos.android.ui.designsystem.organisms.HeroBlock
import com.kosmos.android.ui.designsystem.organisms.HourlyStrip
import com.kosmos.android.ui.designsystem.organisms.InsightsDropdown
import com.kosmos.android.ui.designsystem.organisms.WeatherBackground
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.travel.TravelModeBanner
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean,
    insightsExpanded: Boolean,
    insightsLabel: String = "INSIGHTS",
    travelState: com.kosmos.android.data.TravelState = com.kosmos.android.data.TravelState.Inactive,
    isRefreshing: Boolean,
    onInsightsToggle: () -> Unit,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit = onSearchClick,
    onTimelineClick: () -> Unit,
    onChatClick: () -> Unit,
    onTravelClick: () -> Unit = {},
    onHyperLocalClick: () -> Unit = {},
    onCopyShare: ((com.kosmos.android.model.Verdict) -> Unit)? = null,
    onWhatsAppShare: ((com.kosmos.android.model.Verdict) -> Unit)? = null,
    onImageShare: ((com.kosmos.android.model.Verdict) -> Unit)? = null,
    onRemind: ((com.kosmos.android.model.Verdict) -> Unit)? = null,
    remindedIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    val visibleVerdicts = snapshot.verdicts.filter {
        it.priority != VerdictPriority.NORMAL
    }
    val view = LocalView.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onRefresh()
        },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
    ) {
        WeatherBackground(snapshot = snapshot) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KosmosDimens.screenHorizontal),
            ) {
                Spacer(modifier = Modifier.height(KosmosDimens.screenTop))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onLocationClick),
                    ) {
                        if (snapshot.hasLiveLocation) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KosmosColor.aqiGood),
                            )
                        }
                    }
                    Row {
                        KosmosIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Search city",
                            onClick = onSearchClick,
                        )
                        KosmosIconButton(
                            icon = Icons.Default.Menu,
                            contentDescription = "Settings",
                            onClick = onSettingsClick,
                        )
                    }
                }

                HeroBlock(
                    snapshot = snapshot,
                    showNumbers = showNumbers,
                    modifier = Modifier.clickable(onClick = onLocationClick),
                )

                SectionLabel(insightsLabel)
                Spacer(modifier = Modifier.height(4.dp))
                InsightsDropdown(
                    verdicts = snapshot.verdicts,
                    expanded = insightsExpanded,
                    onToggle = onInsightsToggle,
                    onCopyShare = onCopyShare,
                    onWhatsAppShare = onWhatsAppShare,
                    onImageShare = onImageShare,
                    onRemind = onRemind,
                    remindedIds = remindedIds,
                    currentHour = snapshot.currentHour,
                )

                KosmosCardRow(
                    onClick = onTravelClick,
                    modifier = Modifier.padding(top = KosmosDimens.cardGap),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Plan a getaway",
                            style = KosmosTextStyles.settingsTitle,
                            color = KosmosThemeExt.colors.textPrimary,
                        )
                        Text(
                            text = "Weather-first trip picks near you",
                            style = KosmosTextStyles.settingsSubtitle,
                            color = KosmosThemeExt.colors.textMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "🧭",
                        fontSize = 22.sp,
                    )
                }

                if (travelState.isActive) {
                    TravelModeBanner(
                        travelState = travelState,
                        onClick = onTravelClick,
                    )
                    Spacer(modifier = Modifier.height(KosmosDimens.cardGap))
                }

                SectionLabel("UPDATES")
                Spacer(modifier = Modifier.height(4.dp))
                UpdatesFeed(snapshot = snapshot)

                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionLabel("NEXT 12 HOURS")
                HourlyStrip(
                    hourly = snapshot.hourly,
                    modifier = Modifier.padding(vertical = KosmosDimens.grid),
                )

                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionLabel("TODAY")
                Spacer(modifier = Modifier.height(KosmosDimens.cardGap))
                KosmosCardRow(onClick = onTimelineClick) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "See your day timeline",
                            style = KosmosTextStyles.settingsTitle,
                            color = KosmosThemeExt.colors.textPrimary,
                        )
                        Text(
                            text = "${visibleVerdicts.count { it.timeWindow != null }} timed windows today",
                            style = KosmosTextStyles.settingsSubtitle,
                            color = KosmosThemeExt.colors.textMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "→",
                        fontSize = 22.sp,
                        color = KosmosColor.primary,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding))
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = KosmosColor.textOnGradient,
        )

        KosmosFab(
            label = "Ask Kosmos",
            onClick = onChatClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = KosmosDimens.fabBottom)
                .semantics { contentDescription = "Ask Kosmos, open chat" },
        )
    }
}
