package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.WeatherPalette
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled

@Composable
fun WeatherBackground(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    isDay: Boolean = true,
    content: @Composable () -> Unit,
) {
    WeatherBackground(
        palette = condition.toPalette(isDay),
        condition = condition,
        timeOfDay = if (isDay) TimeOfDay.Day else TimeOfDay.Night,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun WeatherBackground(
    palette: WeatherPalette,
    modifier: Modifier = Modifier,
    condition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY,
    timeOfDay: TimeOfDay = TimeOfDay.Day,
    content: @Composable () -> Unit,
) {
    WeatherScene(
        palette = palette,
        condition = condition,
        timeOfDay = timeOfDay,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun WeatherBackground(
    snapshot: WeatherSnapshot,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    WeatherScene(snapshot = snapshot, modifier = modifier, content = content)
}

@Preview(showBackground = true)
@Composable
private fun WeatherBackgroundPreview() {
    KosmosTheme {
        WeatherBackground(snapshot = PrototypeData.hyderabadSummerDay) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
