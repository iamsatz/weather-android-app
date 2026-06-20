package com.kosmos.android.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kosmos.android.data.PreferencesRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

object WeatherRefreshScheduler {

    const val WORK_NAME = "weather_refresh"

    fun schedule(context: Context) {
        val intervalMinutes = runBlocking {
            PreferencesRepository(context).getRefreshIntervalMinutes()
        }
        schedule(context, intervalMinutes)
    }

    fun schedule(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        if (intervalMinutes <= 0) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val safeInterval = intervalMinutes.coerceAtLeast(15).toLong()
        val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(safeInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
