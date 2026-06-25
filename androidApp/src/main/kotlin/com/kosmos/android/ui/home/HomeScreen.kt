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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
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
import com.kosmos.android.i18n.localized

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean,
    insightsExpanded: Boolean,
    insightsLabel: String = "INSIGHTS",
    travelState: com.kosmos.android.data.TravelState = com.kosmos.android.data.TravelState.Inactive,
    isRefreshing: Boolean,
    isLocating: Boolean = false,
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
    notificationUnread: Int = 0,
    onNotificationsClick: () -> Unit = {},
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

                HomeLocationHeader(
                    locationLabel = snapshot.headerLocationLabel(),
                    locationSubtitle = snapshot.headerSubtitle(),
                    onLocationClick = onLocationClick,
                    isLocating = isLocating,
                    onNotificationsClick = onNotificationsClick,
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    notificationUnread = notificationUnread,
                )

                HeroBlock(
                    snapshot = snapshot,
                    showNumbers = showNumbers,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "Your day",
                        style = KosmosTextStyles.settingsTitle,
                        color = KosmosColor.textOnGradient,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                    if (snapshot.refreshLabel.isNotBlank()) {
                        Text(
                            text = snapshot.refreshLabel,
                            style = KosmosTextStyles.dateHeader,
                            color = KosmosColor.textOnGradient.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                        )
                    }
                }
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
                    onClick = onTimelineClick,
                    modifier = Modifier.padding(top = KosmosDimens.cardGap),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "See your day timeline",
                            style = KosmosTextStyles.settingsTitle,
                            color = KosmosThemeExt.colors.textPrimary,
                        )
                        Text(
                            text = "Verdicts placed hour by hour",
                            style = KosmosTextStyles.settingsSubtitle,
                            color = KosmosThemeExt.colors.textMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "📅",
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

                SectionLabel("NEXT 4 HOURS")
                HourlyStrip(
                    hourly = snapshot.hourly.take(4),
                    modifier = Modifier.padding(vertical = KosmosDimens.grid),
                )

                Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))

                SectionLabel("UPDATES")
                Spacer(modifier = Modifier.height(4.dp))
                UpdatesFeed(snapshot = snapshot)

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
            label = "Ask Komos",
            onClick = onChatClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = KosmosDimens.fabBottom)
                .semantics { contentDescription = "Ask Komos, open chat" },
        )
    }
}
