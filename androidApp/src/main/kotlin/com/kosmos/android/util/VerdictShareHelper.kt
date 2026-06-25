package com.kosmos.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.kosmos.android.model.Verdict
import com.kosmos.android.ui.share.VerdictShareCard
import java.io.File
import java.io.FileOutputStream

object VerdictShareHelper {

    fun formatShareText(locationLine: String, verdict: Verdict): String =
        "Komos · $locationLine\n${verdict.emoji} ${verdict.title}\n${verdict.detail}"

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Komos verdict", text))
    }

    fun shareViaWhatsApp(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.getOrElse {
            genericShare(context, text)
        }
    }

    fun genericShare(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share verdict").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun shareAsImage(
        activity: Activity,
        locationLine: String,
        verdict: Verdict,
    ) {
        val bitmap = renderShareCard(activity, locationLine, verdict)
        val file = File(activity.cacheDir, "kosmos_verdict_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri: Uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, formatShareText(locationLine, verdict))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(intent, "Share verdict card"))
    }

    private fun renderShareCard(
        activity: Activity,
        locationLine: String,
        verdict: Verdict,
    ): Bitmap {
        val composeView = ComposeView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setContent {
                VerdictShareCard(locationLine = locationLine, verdict = verdict)
            }
        }
        val width = (activity.resources.displayMetrics.widthPixels * 0.85f).toInt()
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            composeView.measuredWidth,
            composeView.measuredHeight,
            Bitmap.Config.ARGB_8888,
        )
        composeView.draw(Canvas(bitmap))
        return bitmap
    }
}
