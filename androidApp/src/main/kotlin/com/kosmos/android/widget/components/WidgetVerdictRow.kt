package com.kosmos.android.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kosmos.android.model.Verdict
import com.kosmos.android.widget.widgetVerdictAction

@Composable
fun WidgetVerdictRow(
    verdicts: List<Verdict>,
    modifier: GlanceModifier = GlanceModifier,
    compact: Boolean = true,
) {
    if (verdicts.isEmpty()) {
        Text(
            text = "Easy day ahead",
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White)),
            modifier = modifier,
        )
        return
    }

    if (verdicts.size == 1 && !compact) {
        val verdict = verdicts.first()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable(widgetVerdictAction(verdict.id)),
        ) {
            Text(
                text = "${verdict.emoji} ${verdict.title}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.White),
                ),
            )
            Text(
                text = verdict.detail,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                ),
            )
        }
        return
    }

    val line = verdicts.joinToString("   ") { "${it.emoji} ${it.title}" }
    Text(
        text = line,
        style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(widgetVerdictAction(verdicts.first().id)),
    )
}

@Composable
fun WidgetVerdictList(
    verdicts: List<Verdict>,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        verdicts.forEach { verdict ->
            Text(
                text = "${verdict.emoji} ${verdict.title}",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.White),
                ),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(widgetVerdictAction(verdict.id)),
            )
        }
    }
}
