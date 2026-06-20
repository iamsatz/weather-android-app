package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.WeatherAlert
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.data.WeatherAlertEngine
import com.kosmos.android.ui.designsystem.atoms.KosmosCard
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun UpdatesFeed(
    snapshot: WeatherSnapshot,
    modifier: Modifier = Modifier,
) {
    val alerts = WeatherAlertEngine.buildFeed(snapshot)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        alerts.forEach { alert ->
            UpdateAlertCard(
                alert = alert,
                updatedMinutesAgo = if (alert.id == "nowcast") snapshot.updatedMinutesAgo else null,
            )
        }
    }
}

@Composable
private fun UpdateAlertCard(
    alert: WeatherAlert,
    updatedMinutesAgo: Int?,
    modifier: Modifier = Modifier,
) {
    val colors = KosmosThemeExt.colors
    val accent = when (alert.severity) {
        com.kosmos.android.model.VerdictPriority.SEVERE -> KosmosColor.aqiUnhealthy
        com.kosmos.android.model.VerdictPriority.ACTION -> KosmosColor.nowcastAccent
        com.kosmos.android.model.VerdictPriority.NORMAL -> KosmosColor.primary
    }

    KosmosCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = alert.emoji, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = KosmosTextStyles.verdictTitle,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = alert.detail,
                    style = KosmosTextStyles.verdictDetail,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                updatedMinutesAgo?.let { mins ->
                    Text(
                        text = when {
                            mins <= 0 -> "● Updated just now"
                            mins == 1 -> "● Updated 1 min ago"
                            else -> "● Updated $mins min ago"
                        },
                        style = KosmosTextStyles.settingsSubtitle.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = accent,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
