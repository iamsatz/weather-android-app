package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.android.ui.designsystem.molecules.HourlyCell
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled
import kotlinx.coroutines.delay

@Composable
fun HourlyStrip(
    hourly: List<HourlyForecast>,
    modifier: Modifier = Modifier,
    onGradient: Boolean = true,
) {
    val reduceMotion = rememberReduceMotionEnabled()
    var visibleCount by remember(hourly) { mutableStateOf(if (reduceMotion) hourly.size else 0) }

    LaunchedEffect(hourly, reduceMotion) {
        if (reduceMotion) {
            visibleCount = hourly.size
        } else {
            visibleCount = 0
            hourly.indices.forEach { index ->
                delay(40)
                visibleCount = index + 1
            }
        }
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        hourly.take(visibleCount).forEachIndexed { index, hour ->
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(300),
                label = "hourlyAlpha$index",
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (onGradient) {
                    val dotColor = when {
                        hour.isNow -> KosmosColor.textOnGradient
                        hour.temp >= 35 -> KosmosColor.Gradients.heatPeak.first
                        hour.temp <= 20 -> KosmosColor.Gradients.coldMorning.first
                        else -> KosmosColor.textOnGradient.copy(alpha = 0.55f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(if (hour.isNow) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                }
                HourlyCell(
                    hour = hour,
                    onGradient = onGradient,
                    modifier = Modifier.alpha(alpha),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun HourlyStripPreview() {
    KosmosTheme {
        HourlyStrip(hourly = PrototypeData.hyderabadSummerDay.hourly)
    }
}
