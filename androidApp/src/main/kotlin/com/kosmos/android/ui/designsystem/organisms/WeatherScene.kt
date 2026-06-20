package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.tokens.WeatherPalette
import com.kosmos.android.ui.designsystem.tokens.gradient
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled
import kotlin.random.Random

private data class CloudSpec(val x: Float, val y: Float, val scale: Float, val alpha: Float)

@Composable
fun WeatherScene(
    snapshot: WeatherSnapshot,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = snapshot.toMoodPalette()
    val timeOfDay = resolveTimeOfDay(snapshot.currentHour, snapshot.isDay, snapshot.sunriseHour, snapshot.sunsetHour)
    WeatherScene(
        palette = palette,
        condition = snapshot.condition,
        timeOfDay = timeOfDay,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun WeatherScene(
    palette: WeatherPalette,
    condition: WeatherCondition,
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val infinite = rememberInfiniteTransition(label = "weatherScene")
    val cloudDrift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloudDrift",
    )
    val rainOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rain",
    )
    val snowOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "snow",
    )
    val starTwinkle by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = if (reduceMotion) 0.7f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stars",
    )
    val (top, bottom) = palette.gradient()
    val isOvercast = condition == WeatherCondition.OVERCAST
    val cloudCount = if (isOvercast) 8 else 5
    val clouds = remember(condition, timeOfDay) {
        List(cloudCount) { i ->
            CloudSpec(
                x = 0.04f + i * (if (isOvercast) 0.12f else 0.19f),
                y = if (isOvercast) 0.08f + (i % 4) * 0.05f else 0.12f + (i % 3) * 0.06f,
                scale = if (isOvercast) 1.0f + (i % 3) * 0.12f else 0.85f + (i % 3) * 0.15f,
                alpha = if (isOvercast) 0.65f + (i % 2) * 0.12f else 0.55f + (i % 2) * 0.15f,
            )
        }
    }
    val stars = remember {
        List(28) {
            Offset(
                x = Random(42 + it).nextFloat(),
                y = Random(17 + it).nextFloat() * 0.45f,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(listOf(top, bottom)),
                size = size,
            )

            when (timeOfDay) {
                TimeOfDay.Night -> drawNightSky(stars, starTwinkle)
                TimeOfDay.Dawn, TimeOfDay.Dusk -> drawSunOrMoon(
                    isMoon = false,
                    x = if (timeOfDay == TimeOfDay.Dawn) size.width * 0.18f else size.width * 0.82f,
                    y = size.height * 0.22f,
                    warm = true,
                )
                TimeOfDay.Day -> drawSunOrMoon(
                    isMoon = false,
                    x = size.width * 0.78f,
                    y = size.height * 0.16f,
                    warm = false,
                )
            }

            if (timeOfDay == TimeOfDay.Night) {
                drawSunOrMoon(
                    isMoon = true,
                    x = size.width * 0.72f,
                    y = size.height * 0.14f,
                    warm = false,
                )
            }

            val showClouds = condition == WeatherCondition.PARTLY_CLOUDY ||
                condition == WeatherCondition.OVERCAST ||
                condition == WeatherCondition.LIGHT_RAIN ||
                condition == WeatherCondition.HEAVY_RAIN ||
                condition == WeatherCondition.THUNDERSTORM ||
                condition == WeatherCondition.FOG ||
                timeOfDay == TimeOfDay.Night ||
                timeOfDay == TimeOfDay.Dawn ||
                timeOfDay == TimeOfDay.Dusk
            if (showClouds) {
                clouds.forEachIndexed { index, cloud ->
                    drawCloud(
                        center = Offset(
                            x = size.width * (cloud.x + cloudDrift * 0.08f + index * 0.02f) % size.width,
                            y = size.height * cloud.y,
                        ),
                        scale = cloud.scale,
                        alpha = cloud.alpha,
                        dark = timeOfDay == TimeOfDay.Night || isOvercast,
                    )
                }
            }

            when (condition) {
                WeatherCondition.LIGHT_RAIN -> drawRain(rainOffset, intensity = 0.6f)
                WeatherCondition.HEAVY_RAIN -> drawRain(rainOffset, intensity = 0.95f)
                WeatherCondition.THUNDERSTORM -> {
                    drawRain(rainOffset, intensity = 1f)
                    if (!reduceMotion && rainOffset > 0.92f) {
                        drawRect(Color.White.copy(alpha = 0.12f), size = size)
                    }
                }
                WeatherCondition.FOG -> drawFog(cloudDrift)
                WeatherCondition.HAZE -> drawHaze()
                WeatherCondition.SNOW -> drawSnow(snowOffset)
                else -> Unit
            }
        }
        content()
    }
}

private fun DrawScope.drawNightSky(stars: List<Offset>, twinkle: Float) {
    stars.forEach { star ->
        drawCircle(
            color = Color.White.copy(alpha = 0.35f + twinkle * 0.45f * star.x),
            radius = 1.2f + star.y * 2f,
            center = Offset(star.x * size.width, star.y * size.height),
        )
    }
}

private fun DrawScope.drawSunOrMoon(isMoon: Boolean, x: Float, y: Float, warm: Boolean) {
    val radius = if (isMoon) 22f else 34f
    val core = when {
        isMoon -> Color(0xFFE8ECF5)
        warm -> Color(0xFFFFD080)
        else -> Color(0xFFFFF4C2)
    }
    drawCircle(
        color = core.copy(alpha = if (isMoon) 0.95f else 0.92f),
        radius = radius,
        center = Offset(x, y),
    )
    if (!isMoon) {
        drawCircle(
            color = core.copy(alpha = 0.25f),
            radius = radius * 2.2f,
            center = Offset(x, y),
        )
    } else {
        drawCircle(
            color = Color(0xFF8890A8).copy(alpha = 0.35f),
            radius = radius * 0.55f,
            center = Offset(x - radius * 0.25f, y - radius * 0.1f),
        )
    }
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, alpha: Float, dark: Boolean) {
    val base = if (dark) Color(0xFF3A4460) else Color.White
    val puff = base.copy(alpha = alpha)
    listOf(
        Offset(-28f, 4f) to 22f,
        Offset(-8f, -6f) to 26f,
        Offset(16f, 0f) to 24f,
        Offset(34f, 6f) to 18f,
    ).forEach { (offset, r) ->
        drawCircle(
            color = puff,
            radius = r * scale,
            center = center + offset * scale,
        )
    }
}

private fun DrawScope.drawRain(offset: Float, intensity: Float) {
    val streakCount = (60 * intensity).toInt()
    repeat(streakCount) { i ->
        val x = (i * 37 % size.width.toInt()).toFloat() + offset * 18f
        val y = ((i * 53 + offset * size.height) % size.height)
        rotate(12f, pivot = Offset(x, y)) {
            drawLine(
                color = Color.White.copy(alpha = 0.18f + intensity * 0.12f),
                start = Offset(x, y),
                end = Offset(x + 6f, y + 18f * intensity),
                strokeWidth = 1.4f,
            )
        }
    }
}

private fun DrawScope.drawFog(drift: Float) {
    repeat(6) { band ->
        val y = size.height * (0.18f + band * 0.12f + drift * 0.02f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = 0.22f - band * 0.02f),
                    Color.White.copy(alpha = 0f),
                ),
            ),
            topLeft = Offset(0f, y),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.14f),
        )
    }
}

private fun DrawScope.drawHaze() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFC9AC7E).copy(alpha = 0.28f),
                Color(0xFF947A5E).copy(alpha = 0.18f),
            ),
        ),
        size = size,
    )
}

private fun DrawScope.drawSnow(offset: Float) {
    repeat(55) { i ->
        val x = ((i * 47 + offset * size.width * 0.3f) % size.width)
        val y = ((i * 61 + offset * size.height) % size.height)
        drawCircle(
            color = Color.White.copy(alpha = 0.55f + (i % 3) * 0.12f),
            radius = 1.5f + (i % 4),
            center = Offset(x, y),
        )
    }
}
