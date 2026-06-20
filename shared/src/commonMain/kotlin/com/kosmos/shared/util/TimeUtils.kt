package com.kosmos.shared.util

object TimeUtils {

    fun hourFromIso(isoTime: String): Int {
        val timePart = isoTime.substringAfter('T').substringBefore(':')
        return timePart.toIntOrNull() ?: 0
    }

    fun minuteFromIso(isoTime: String): Int {
        val parts = isoTime.substringAfter('T').split(':')
        return parts.getOrNull(1)?.toIntOrNull() ?: 0
    }

    fun dateLabelFromIso(isoDate: String): String {
        val parts = isoDate.split('-')
        if (parts.size < 3) return isoDate
        val month = parts[1].toIntOrNull() ?: return isoDate
        val day = parts[2].toIntOrNull() ?: return isoDate
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
        val monthName = monthNames.getOrElse(month - 1) { "???" }
        return "$monthName $day"
    }
}
