package com.kosmos.shared.models

enum class VerdictPriority {
    SEVERE,
    ACTION,
    NORMAL,
}

data class TimeWindow(
    val startHour: Int,
    val endHour: Int,
    val label: String,
) {
    fun contains(hour: Int): Boolean = hour in startHour..endHour
}

data class Verdict(
    val id: String,
    val emoji: String,
    val title: String,
    val detail: String,
    val priority: VerdictPriority,
    val accentColor: Long,
    val timeWindow: TimeWindow? = null,
)
