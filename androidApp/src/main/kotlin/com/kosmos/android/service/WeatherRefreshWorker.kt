package com.kosmos.android.service

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kosmos.android.data.WeatherRepository
import com.kosmos.android.widget.BestWindowsWidget
import com.kosmos.android.widget.LargeWidget
import com.kosmos.android.widget.MediumWidget
import com.kosmos.android.widget.ModeAwareWidget
import com.kosmos.android.widget.NextRainWidget
import com.kosmos.android.widget.SmallWidget
import com.kosmos.android.widget.TodaysCallWidget
import com.kosmos.android.widget.XlWidget

class WeatherRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = WeatherRepository(applicationContext)
            repo.refresh(hasLocationPermission = false)
            SmallWidget().updateAll(applicationContext)
            MediumWidget().updateAll(applicationContext)
            LargeWidget().updateAll(applicationContext)
            XlWidget().updateAll(applicationContext)
            TodaysCallWidget().updateAll(applicationContext)
            NextRainWidget().updateAll(applicationContext)
            ModeAwareWidget().updateAll(applicationContext)
            BestWindowsWidget().updateAll(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
