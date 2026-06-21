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
import com.kosmos.android.alert.RainAlertActivity
import com.kosmos.android.data.PreferencesRepository
import com.kosmos.android.data.WeatherAlertEngine
import com.kosmos.android.data.WeatherRepository
import com.kosmos.android.data.WeatherUiState
import com.kosmos.shared.api.OpenMeteoClient
import com.kosmos.shared.api.createHttpClient
import com.kosmos.shared.models.WeatherSnapshot as SharedSnapshot

class AlertCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        return try {
            val repo = WeatherRepository(applicationContext)
            repo.refresh(hasLocationPermission = false)
            val uiSnapshot = when (val state = repo.state.value) {
                is WeatherUiState.Ready -> state.snapshot
                else -> return Result.retry()
            }

            val prefs = com.kosmos.android.data.PreferencesRepository(applicationContext)
            val saved = prefs.getSavedLocation() ?: return Result.success()
            val client = OpenMeteoClient(createHttpClient())
            val shared = client.fetchWeather(saved.latitude, saved.longitude, saved.city)

            checkRain(shared, uiSnapshot.locationLine)
            checkSevere(shared, uiSnapshot.locationLine)
            checkFeedAlerts(uiSnapshot)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkRain(shared: SharedSnapshot, locationLine: String) {
        val message = AlertLogic.rainAlertMessage(shared.hourly, shared.nowIndex) ?: return
        val rainHour = shared.hourly.getOrNull(shared.nowIndex + 1)?.takeIf {
            it.precipProbability >= 60
        } ?: return

        val key = AlertLogic.rainAlertKey(shared.nowIndex, rainHour.index)
        if (!AlertLogic.shouldSendRainAlert(applicationContext, key)) return

        postAlert(
            channelId = "rain_alert",
            notificationId = 2001,
            title = "Rain in about an hour · $locationLine",
            body = message,
            fullScreen = true,
        )
    }

    private suspend fun checkFeedAlerts(snapshot: com.kosmos.android.model.WeatherSnapshot) {
        val candidates = WeatherAlertEngine.notificationCandidates(snapshot)
        candidates.forEach { alert ->
            val key = "feed_${alert.id}"
            if (!AlertLogic.shouldSendFeedAlert(applicationContext, key)) return@forEach
            postAlert(
                channelId = "weather_feed",
                notificationId = alert.id.hashCode().and(0xFFFF),
                title = "${alert.emoji} ${alert.title}",
                body = alert.detail,
                fullScreen = alert.severity == com.kosmos.android.model.VerdictPriority.SEVERE &&
                    alert.id.startsWith("heat"),
            )
        }
    }

    private suspend fun checkSevere(shared: SharedSnapshot, locationLine: String) {
        val message = AlertLogic.severeAlertMessage(shared) ?: return
        val type = when {
            shared.feelsLike >= 40 -> "heat"
            (shared.aqi ?: 0) > 200 -> "aqi"
            else -> return
        }
        val key = AlertLogic.severeAlertKey(type)
        if (!AlertLogic.shouldSendSevereAlert(applicationContext, key)) return

        postAlert(
            channelId = "severe_weather",
            notificationId = 2002,
            title = "Weather alert · $locationLine",
            body = message,
            fullScreen = type == "heat",
        )
    }

    private suspend fun postAlert(
        channelId: String,
        notificationId: Int,
        title: String,
        body: String,
        fullScreen: Boolean,
    ) {
        val openApp = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val fullScreenIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId + 100,
            RainAlertActivity.intent(applicationContext, title, body),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)

        if (fullScreen) {
            builder.setFullScreenIntent(fullScreenIntent, true)
        }

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())

        NotificationLogger.log(applicationContext, title = title, body = body, type = channelId)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val WORK_NAME = "alert_check"
    }
}
