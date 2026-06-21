package com.kosmos.android.notification

data class NotificationEntry(
    val id: String,
    val title: String,
    val body: String,
    val timestampMs: Long,
    val type: String,
)

object NotificationLogger {

    suspend fun log(
        context: android.content.Context,
        title: String,
        body: String,
        type: String,
    ) {
        com.kosmos.android.data.PreferencesRepository(context).appendNotification(
            NotificationEntry(
                id = "${type}_${System.currentTimeMillis()}",
                title = title,
                body = body,
                timestampMs = System.currentTimeMillis(),
                type = type,
            ),
        )
    }
}
