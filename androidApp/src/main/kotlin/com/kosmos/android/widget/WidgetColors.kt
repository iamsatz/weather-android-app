package com.kosmos.android.widget

import androidx.compose.ui.graphics.Color
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.ui.designsystem.organisms.toPalette
import com.kosmos.android.ui.designsystem.tokens.gradient

fun WeatherCondition.widgetBackgroundColor(isDay: Boolean = true): Color {
    val palette = toPalette(isDay)
    return palette.gradient().second
}
