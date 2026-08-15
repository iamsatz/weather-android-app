package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

private enum class RhythmBand { Go, Caution, Stay }

@Composable
fun DayRhythm(
    hourly: List<HourlyForecast>,
    modifier: Modifier = Modifier,
) {
    val colors = KosmosThemeExt.colors
    val bands = rhythmBands(hourly)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "TODAY'S RHYTHM",
            style = KosmosTextStyles.compactLabel.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.08.em,
            ),
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = KosmosDimens.sm),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            bands.forEach { band ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (band) {
                                RhythmBand.Go -> KosmosColor.bandGo
                                RhythmBand.Caution -> KosmosColor.bandCaution
                                RhythmBand.Stay -> KosmosColor.bandStay
                            },
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = KosmosDimens.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("5a", "9a", "1p", "5p", "9p").forEach { label ->
                Text(label, style = KosmosTextStyles.caption, color = colors.textSecondary)
            }
        }
    }
}

private fun rhythmBands(hourly: List<HourlyForecast>): List<RhythmBand> {
    val byHour = hourly.associateBy { it.hour }
    return (5..20).map { hour ->
        val cell = byHour[hour]
        val precip = cell?.precipPercent ?: 0
        val temp = cell?.temp ?: 28
        when {
            precip >= 50 || temp >= 36 -> RhythmBand.Stay
            precip >= 25 || temp >= 33 -> RhythmBand.Caution
            else -> RhythmBand.Go
        }
    }
}
