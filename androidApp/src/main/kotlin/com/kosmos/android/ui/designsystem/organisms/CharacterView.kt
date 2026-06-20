package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosMotion
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun CharacterView(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    val shouldAnimate = !reduceMotion

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldAnimate) 1.005f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(KosmosMotion.breatheDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    Canvas(modifier = modifier.size(KosmosDimens.characterSize)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val bodyRadius = 28.dp.toPx() * scale

        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = bodyRadius,
            center = Offset(centerX, centerY + 8.dp.toPx()),
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = 18.dp.toPx() * scale,
            center = Offset(centerX, centerY - 22.dp.toPx()),
        )

        drawCircle(
            color = Color(0xFF2A2A3E),
            radius = 2.5.dp.toPx(),
            center = Offset(centerX - 6.dp.toPx(), centerY - 24.dp.toPx()),
        )
        drawCircle(
            color = Color(0xFF2A2A3E),
            radius = 2.5.dp.toPx(),
            center = Offset(centerX + 6.dp.toPx(), centerY - 24.dp.toPx()),
        )

        when (condition) {
            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.CLEAR_MIDDAY -> {
                drawArc(
                    color = Color(0xFF2A2A3E),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(centerX - 8.dp.toPx(), centerY - 30.dp.toPx()),
                    size = Size(16.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            WeatherCondition.LIGHT_RAIN, WeatherCondition.THUNDERSTORM -> {
                drawLine(
                    color = Color(0xFF3380C7),
                    start = Offset(centerX - 30.dp.toPx(), centerY - 40.dp.toPx()),
                    end = Offset(centerX - 10.dp.toPx(), centerY - 10.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                )
                drawArc(
                    color = Color(0xFF3380C7),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - 35.dp.toPx(), centerY - 55.dp.toPx()),
                    size = Size(30.dp.toPx(), 20.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
            else -> {
                drawArc(
                    color = Color(0xFF2A2A3E),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - 8.dp.toPx(), centerY - 28.dp.toPx()),
                    size = Size(16.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun CharacterViewPreview() {
    KosmosTheme {
        CharacterView(condition = WeatherCondition.PARTLY_CLOUDY)
    }
}
