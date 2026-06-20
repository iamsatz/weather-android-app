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
import com.kosmos.android.data.WeatherRepository
import com.kosmos.android.data.WeatherUiState
import com.kosmos.android.model.VerdictPriority

class MorningBriefWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        return try {
            val repo = WeatherRepository(applicationContext)
            repo.refresh(hasLocationPermission = false)

            val snapshot = when (val state = repo.state.value) {
                is WeatherUiState.Ready -> state.snapshot
                else -> return Result.retry()
            }

            val lines = snapshot.verdicts
                .filter { it.priority != VerdictPriority.NORMAL }
                .take(3)
                .map { "${it.emoji} ${it.title}" }

            val body = if (lines.isEmpty()) {
                "${snapshot.conditionLabel} · ${snapshot.temp}° — open Kosmos for your day plan."
            } else {
                lines.joinToString("\n")
            }

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Kosmos · ${snapshot.locationLine}")
                .setContentText(lines.firstOrNull() ?: "Your morning weather brief")
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent())
                .build()

            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID, notification)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
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
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "morning_brief"
        const val WORK_NAME = "morning_brief"
        private const val NOTIFICATION_ID = 1001
    }
}
