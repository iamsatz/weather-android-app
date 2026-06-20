package com.kosmos.android.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.HourlyForecast

@Composable
fun WidgetHourlyRow(
    hourly: List<HourlyForecast>,
    modifier: GlanceModifier = GlanceModifier,
    maxHours: Int = 8,
) {
    val cells = hourly.take(maxHours)
    Row(modifier = modifier.fillMaxWidth()) {
        cells.forEach { hour ->
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "${hour.label} ${hour.temp}° ${hour.emoji}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                    ),
                )
            }
        }
    }
}
