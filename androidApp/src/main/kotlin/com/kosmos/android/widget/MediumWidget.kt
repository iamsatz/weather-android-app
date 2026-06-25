package com.kosmos.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.widget.components.WidgetHeader
import com.kosmos.android.widget.components.WidgetVerdictRow

class MediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) {
                    MediumWidgetContent(snapshot)
                } else {
                    WidgetPlaceholder("Tap to open Komos")
                }
            }
        }
    }
}

@Composable
fun MediumWidgetContent(snapshot: com.kosmos.android.model.WeatherSnapshot) {
    val topVerdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .take(2)
    val bg = snapshot.condition.widgetBackgroundColor()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(12.dp)
            .clickable(widgetOpenAppAction()),
    ) {
        WidgetHeader(
            locationLine = snapshot.locationLine,
            temp = snapshot.temp,
            conditionLabel = snapshot.conditionLabel,
            aqiLabel = snapshot.aqiLabel,
            aqiColor = snapshot.aqiColor,
        )
        Spacer(GlanceModifier.height(8.dp))
        WidgetVerdictRow(verdicts = topVerdicts)
    }
}

@Composable
fun WidgetPlaceholder(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF3D87D4)))
            .padding(12.dp)
            .clickable(widgetOpenAppAction()),
    ) {
        Text(
            text = "Komos",
            style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White)),
        )
        Text(
            text = message,
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f))),
        )
    }
}

class MediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}
