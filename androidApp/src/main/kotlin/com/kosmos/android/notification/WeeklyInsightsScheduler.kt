package com.kosmos.android.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WeeklyInsightsScheduler {

    fun schedule(context: Context) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<WeeklyInsightsWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_insights",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
