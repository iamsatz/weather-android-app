package com.kosmos.android.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kosmos.android.MainActivity
import com.kosmos.android.R

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        val emoji = inputData.getString(KEY_EMOJI) ?: "⏰"
        val title = inputData.getString(KEY_TITLE) ?: "Kosmos reminder"
        val detail = inputData.getString(KEY_DETAIL) ?: "Your window is coming up."
        val verdictId = inputData.getString(KEY_VERDICT_ID) ?: "reminder"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$emoji $title")
            .setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(verdictId.hashCode(), notification)

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "verdict_reminder"
        const val KEY_EMOJI = "emoji"
        const val KEY_TITLE = "title"
        const val KEY_DETAIL = "detail"
        const val KEY_VERDICT_ID = "verdict_id"
    }
}
