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
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot

class TodaysCallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) TodaysCallContent(snapshot)
                else WidgetPlaceholder("Tap to open Komos")
            }
        }
    }
}

@Composable
fun TodaysCallContent(snapshot: WeatherSnapshot) {
    val top = snapshot.verdicts.firstOrNull { it.priority != VerdictPriority.NORMAL }
        ?: snapshot.verdicts.firstOrNull()
    val bg = snapshot.condition.widgetBackgroundColor(snapshot.isDay)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(14.dp)
            .clickable(if (top != null) widgetVerdictAction(top.id) else widgetOpenAppAction()),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (top != null) {
            Text(
                text = top.emoji,
                style = TextStyle(fontSize = 30.sp, color = ColorProvider(Color.White)),
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = top.title,
                style = TextStyle(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White),
                ),
            )
        } else {
            Text(
                text = "${snapshot.temp}°  ${snapshot.conditionLabel}",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White),
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Easy day — nothing urgent",
                style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
            )
        }
    }
}

class TodaysCallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodaysCallWidget()
}
