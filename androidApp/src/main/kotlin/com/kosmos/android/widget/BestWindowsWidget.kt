package com.kosmos.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.data.PreferencesRepository
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.notification.ReminderScheduler

class BestWindowsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataLoader.loadSnapshot(context)
        val reminded = PreferencesRepository(context).getReminderIds()
        provideContent {
            GlanceTheme {
                if (snapshot != null) BestWindowsContent(snapshot, reminded)
                else WidgetPlaceholder("Tap to open Komos")
            }
        }
    }
}

@Composable
fun BestWindowsContent(snapshot: WeatherSnapshot, remindedIds: Set<String>) {
    val bg = snapshot.condition.widgetBackgroundColor(snapshot.isDay)
    val windows = snapshot.verdicts
        .filter { it.timeWindow != null }
        .sortedBy { it.timeWindow!!.startHour }
        .take(4)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .padding(12.dp)
            .clickable(widgetOpenAppAction()),
    ) {
        Text(
            text = "Today's best windows",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White),
            ),
        )
        Spacer(GlanceModifier.height(6.dp))
        if (windows.isEmpty()) {
            Text(
                text = "No timed windows today — tap to open",
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
            )
        } else {
            windows.forEach { verdict ->
                WindowChip(verdict, isFuture = verdict.timeWindow!!.startHour > snapshot.currentHour, reminded = verdict.id in remindedIds)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun WindowChip(verdict: Verdict, isFuture: Boolean, reminded: Boolean) {
    val window = verdict.timeWindow ?: return
    val suffix = when {
        reminded -> " 🔔"
        isFuture -> " ⏰"
        else -> ""
    }
    val mod = if (isFuture && !reminded) {
        GlanceModifier.fillMaxWidth().clickable(
            actionRunCallback<RemindActionCallback>(
                actionParametersOf(
                    RemindActionCallback.idKey to verdict.id,
                    RemindActionCallback.emojiKey to verdict.emoji,
                    RemindActionCallback.titleKey to verdict.title,
                    RemindActionCallback.detailKey to verdict.detail,
                    RemindActionCallback.startKey to window.startHour,
                ),
            ),
        )
    } else {
        GlanceModifier.fillMaxWidth()
    }
    Text(
        text = "${verdict.emoji} ${verdict.title} · ${window.label}$suffix",
        style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White)),
        modifier = mod,
    )
}

class RemindActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[idKey] ?: return
        val start = parameters[startKey] ?: return
        val scheduled = ReminderScheduler.schedule(
            context = context,
            verdictId = id,
            emoji = parameters[emojiKey] ?: "⏰",
            title = parameters[titleKey] ?: "Komos reminder",
            detail = parameters[detailKey] ?: "Your window is coming up.",
            startHour = start,
        )
        if (scheduled) {
            val allowed = PreferencesRepository(context).addReminder(id)
            if (!allowed) ReminderScheduler.cancel(context, id)
        }
        BestWindowsWidget().updateAll(context)
    }

    companion object {
        val idKey = ActionParameters.Key<String>("remind_id")
        val emojiKey = ActionParameters.Key<String>("remind_emoji")
        val titleKey = ActionParameters.Key<String>("remind_title")
        val detailKey = ActionParameters.Key<String>("remind_detail")
        val startKey = ActionParameters.Key<Int>("remind_start")
    }
}

class BestWindowsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BestWindowsWidget()
}
