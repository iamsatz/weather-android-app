package com.kosmos.android.notification

import kotlinx.serialization.Serializable

@Serializable
data class ScheduledReminder(
    val verdictId: String,
    val emoji: String,
    val title: String,
    val detail: String,
    val fireTimeMs: Long,
    val createdAtMs: Long = System.currentTimeMillis(),
)
