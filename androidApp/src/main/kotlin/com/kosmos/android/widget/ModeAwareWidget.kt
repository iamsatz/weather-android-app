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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.widget.components.WidgetVerdictList
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.i18n.LocaleStrings
import com.kosmos.shared.mode.ModeCatalog

class ModeAwareWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        provideContent {
            GlanceTheme {
                if (snapshot != null) ModeAwareContent(snapshot)
                else WidgetPlaceholder("Tap to open Komos")
            }
        }
    }
}

@Composable
fun ModeAwareContent(snapshot: WeatherSnapshot) {
    val bg = snapshot.condition.widgetBackgroundColor(snapshot.isDay)
    val locale = AppLocale.fromCode(snapshot.localeCode)
    val meta = ModeCatalog.all.firstOrNull { it.mode.id == snapshot.userModeId }
    val modeEmoji = meta?.emoji ?: "🧩"
    val modeName = meta?.let { LocaleStrings.ui(it.nameKey, locale) } ?: "Komos"

    val verdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .ifEmpty { snapshot.verdicts.take(1) }
        .take(3)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(12.dp)
            .clickable(widgetOpenAppAction()),
    ) {
        Text(
            text = "$modeEmoji $modeName · ${snapshot.temp}°",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color.White),
            ),
        )
        Spacer(GlanceModifier.height(8.dp))
        WidgetVerdictList(verdicts = verdicts)
    }
}

class ModeAwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ModeAwareWidget()
}
