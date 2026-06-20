package com.kosmos.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kosmos.android.notification.AlertScheduler
import com.kosmos.android.ui.designsystem.tokens.KosmosFonts
import com.kosmos.android.notification.MorningBriefScheduler
import com.kosmos.android.notification.WeeklyInsightsScheduler
import com.kosmos.android.service.WeatherRefreshScheduler

class KosmosApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KosmosFonts.init(this)
        createNotificationChannels()
        WeatherRefreshScheduler.schedule(this)
        MorningBriefScheduler.scheduleDaily(this)
        AlertScheduler.scheduleHourly(this)
        WeeklyInsightsScheduler.schedule(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        listOf(
            "morning_brief" to "Morning Brief",
            "rain_alert" to "Rain Alert",
            "severe_weather" to "Severe Weather",
            "weather_feed" to "Weather Updates",
            "verdict_reminder" to "Insight Reminders",
        ).forEach { (id, name) ->
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH),
            )
        }
    }
}
