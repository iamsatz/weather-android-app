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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.model.WeatherSnapshot

private fun HourlyForecast.isRainy(): Boolean =
    emoji.contains("☔") || emoji.contains("🌧") || emoji.contains("⛈") || emoji.contains("🌦")

class NextRainWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) NextRainContent(snapshot)
                else WidgetPlaceholder("Tap to open Kosmos")
            }
        }
    }
}

@Composable
fun NextRainContent(snapshot: WeatherSnapshot) {
    val bg = snapshot.condition.widgetBackgroundColor(snapshot.isDay)
    val future = snapshot.hourly
    val rainIndex = future.indexOfFirst { it.isRainy() }

    val (headline, sub) = when {
        snapshot.nowcastIsWet -> "🌧 Rain now" to snapshot.nowcast
        rainIndex == 0 -> "🌧 Rain this hour" to "Carry cover if you head out"
        rainIndex > 0 -> {
            val label = future[rainIndex].label
            "🌧 Rain ${label}" to "in about $rainIndex hr — keep cover handy"
        }
        else -> {
            val dryHours = future.size
            "☀ Dry" to "No rain for the next $dryHours hrs"
        }
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(14.dp)
            .clickable(widgetOpenAppAction()),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = headline,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White),
            ),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = sub,
            style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.White.copy(alpha = 0.9f))),
        )
    }
}

class NextRainWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextRainWidget()
}
