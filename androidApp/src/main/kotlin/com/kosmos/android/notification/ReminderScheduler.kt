package com.kosmos.android.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val LEAD_MINUTES = 10

    /**
     * Schedules a one-time reminder ~10 min before [startHour] today.
     * Returns false when the window is already in the past (nothing scheduled).
     */
    fun schedule(
        context: Context,
        verdictId: String,
        emoji: String,
        title: String,
        detail: String,
        startHour: Int,
    ): Boolean {
        val delayMs = millisUntil(startHour)
        if (delayMs <= 0) return false

        val data = Data.Builder()
            .putString(ReminderWorker.KEY_VERDICT_ID, verdictId)
            .putString(ReminderWorker.KEY_EMOJI, emoji)
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_DETAIL, detail)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(verdictId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return true
    }

    fun cancel(context: Context, verdictId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(verdictId))
    }

    private fun workName(verdictId: String) = "reminder_$verdictId"

    private fun millisUntil(startHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -LEAD_MINUTES)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
