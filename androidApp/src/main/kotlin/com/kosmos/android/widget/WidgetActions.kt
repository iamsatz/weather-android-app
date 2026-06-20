package com.kosmos.android.widget

import android.content.ComponentName
import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity

fun widgetOpenAppAction(): Action =
    actionStartActivity(mainActivityIntent())

fun widgetVerdictAction(verdictId: String): Action =
    actionStartActivity(mainActivityIntent(verdictId))

private fun mainActivityIntent(verdictId: String? = null): Intent =
    Intent(Intent.ACTION_MAIN).apply {
        component = ComponentName("com.kosmos.android", "com.kosmos.android.MainActivity")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        verdictId?.let { putExtra(WidgetConstants.EXTRA_VERDICT_ID, it) }
    }
