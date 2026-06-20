package com.kosmos.android.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kosmos.android.MainActivity
import com.kosmos.android.R
import com.kosmos.android.data.PreferencesRepository
import com.kosmos.android.data.WeatherRepository
import com.kosmos.shared.engine.WeeklyInsightsEngine

class WeeklyInsightsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesRepository(applicationContext)
        if (!prefs.isWeeklyDigestEnabled()) return Result.success()

        val weatherRepo = WeatherRepository(applicationContext)
        weatherRepo.refresh(hasLocationPermission = false)
        val shared = weatherRepo.lastSharedSnapshotOrNull() ?: return Result.success()

        val body = WeeklyInsightsEngine.digestBody(shared)
        showNotification(body)
        return Result.success()
    }

    private fun showNotification(body: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext,
            3001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, "morning_brief")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your week in weather")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(3001, notification)
    }
}
