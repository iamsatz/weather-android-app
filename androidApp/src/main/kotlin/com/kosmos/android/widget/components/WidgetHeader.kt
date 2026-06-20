package com.kosmos.android.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun WidgetHeader(
    locationLine: String,
    temp: Int,
    conditionLabel: String,
    aqiLabel: String,
    aqiColor: Long,
    modifier: GlanceModifier = GlanceModifier,
    showConditionRow: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Text(
                text = "$locationLine  $temp°",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.White),
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
        if (showConditionRow) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start,
            ) {
                Text(
                    text = "$conditionLabel  ●$aqiLabel",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                )
            }
        }
    }
}
