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
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.widget.components.WidgetVerdictRow

class SmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) {
                    SmallWidgetContent(snapshot)
                } else {
                    WidgetPlaceholder("Loading…")
                }
            }
        }
    }
}

@Composable
fun SmallWidgetContent(snapshot: com.kosmos.android.model.WeatherSnapshot) {
    val topVerdict = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .firstOrNull()
    val bg = snapshot.condition.widgetBackgroundColor()
    val topHour = snapshot.hourly.firstOrNull { !it.isNow } ?: snapshot.hourly.firstOrNull()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(10.dp)
            .clickable(
                if (topVerdict != null) widgetVerdictAction(topVerdict.id) else widgetOpenAppAction(),
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = topHour?.emoji ?: "🌤",
            style = TextStyle(fontSize = 18.sp, color = ColorProvider(Color.White)),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "${snapshot.temp}°",
            style = TextStyle(fontSize = 28.sp, color = ColorProvider(Color.White)),
        )
        Spacer(GlanceModifier.height(6.dp))
        if (topVerdict != null) {
            WidgetVerdictRow(verdicts = listOf(topVerdict), compact = true)
        }
    }
}

class SmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}
