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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
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
import com.kosmos.android.i18n.localized
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.KosmosFab
import com.kosmos.android.ui.designsystem.atoms.KosmosIconButton
import com.kosmos.android.ui.designsystem.atoms.SectionLabel
import com.kosmos.android.ui.designsystem.molecules.VerdictRow
import com.kosmos.android.ui.designsystem.organisms.HeroBlock
import com.kosmos.android.ui.designsystem.organisms.WeatherBackground
import com.kosmos.android.ui.designsystem.tokens.ElderTypography
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ElderHomeScreen(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topVerdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .take(3)
        .ifEmpty { snapshot.verdicts.take(3) }

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
                    Text(
                        text = snapshot.locationLine,
                        style = ElderTypography.cityPill,
                        color = KosmosColor.textOnGradient,
                        modifier = Modifier.weight(1f),
                    )
                    KosmosIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onSettingsClick,
                    )
                }

                HeroBlock(
                    snapshot = snapshot,
                    showNumbers = showNumbers,
                    elderMode = true,
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel(localized("today_summary"))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topVerdicts.forEach { verdict ->
                        VerdictRow(
                            verdict = verdict,
                            expanded = true,
                            onClick = {},
                            elderMode = true,
                        )
                    }
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = KosmosDimens.fabBottom)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KosmosFab(
                label = localized("read_aloud"),
                onClick = onReadAloud,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Read today's brief aloud" },
            )
        }
    }
}
