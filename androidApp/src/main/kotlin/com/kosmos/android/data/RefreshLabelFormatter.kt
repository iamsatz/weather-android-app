package com.kosmos.android.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object RefreshLabelFormatter {
    fun format(epochMs: Long, use24Hour: Boolean, nowMs: Long = System.currentTimeMillis()): String {
        if (epochMs <= 0L) return ""
        val ageMs = nowMs - epochMs
        if (ageMs < 60_000L) return "refreshed just now"

        val zone = ZoneId.systemDefault()
        val at = Instant.ofEpochMilli(epochMs).atZone(zone)
        val now = Instant.ofEpochMilli(nowMs).atZone(zone)
        val pattern = if (use24Hour) "H:mm" else "h:mm a"
        val time = DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(at)

        return when {
            at.toLocalDate() == now.toLocalDate().minusDays(1) -> "refreshed yesterday $time"
            else -> "refreshed $time"
        }
    }
}
