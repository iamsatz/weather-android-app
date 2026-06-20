package com.kosmos.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
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
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.widget.components.WidgetHeader
import com.kosmos.android.widget.components.WidgetHourlyRow
import com.kosmos.android.widget.components.WidgetVerdictList

class XlWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) {
                    XlWidgetContent(snapshot)
                } else {
                    WidgetPlaceholder("Tap to open Kosmos")
                }
            }
        }
    }
}

@Composable
fun XlWidgetContent(snapshot: com.kosmos.android.model.WeatherSnapshot) {
    val topVerdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .take(6)
    val bg = snapshot.condition.widgetBackgroundColor()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(androidx.glance.unit.ColorProvider(bg))
            .padding(14.dp)
            .clickable(widgetOpenAppAction()),
    ) {
        WidgetHeader(
            locationLine = "${snapshot.locationLine} · ${snapshot.dateLabel}",
            temp = snapshot.temp,
            conditionLabel = snapshot.conditionLabel,
            aqiLabel = snapshot.aqiLabel,
            aqiColor = snapshot.aqiColor,
        )
        Spacer(GlanceModifier.height(12.dp))
        WidgetVerdictList(verdicts = topVerdicts.take(4))
        Spacer(GlanceModifier.height(10.dp))
        WidgetHourlyRow(hourly = snapshot.hourly, maxHours = 10)
        Spacer(GlanceModifier.height(8.dp))
        WidgetVerdictList(verdicts = topVerdicts.drop(4).take(2))
    }
}

class XlWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = XlWidget()
}
