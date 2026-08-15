package com.kosmos.android.ui.designsystem.tokens

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.kosmos.android.R

object KosmosFonts {
    private const val TAG = "KosmosFonts"

    var solway: FontFamily = FontFamily.Serif
        private set
    var publicSans: FontFamily = FontFamily.SansSerif
        private set
    var usingBundledFonts: Boolean = false
        private set

    val playfair: FontFamily
        get() = solway
    val inter: FontFamily
        get() = publicSans

    fun init(context: Context) {
        val appContext = context.applicationContext
        if (!loadBundled(appContext)) {
            useSystemDefaults()
            Log.w(TAG, "Using system fonts — bundled fonts missing or invalid")
        }
    }

    private fun loadBundled(context: Context): Boolean {
        val entries = listOf(
            R.font.solway_regular,
            R.font.solway_bold,
            R.font.publicsans_variable,
        )
        for (resId in entries) {
            if (!isValidFontResource(context, resId)) return false
        }
        return try {
            entries.forEach { resId ->
                ResourcesCompat.getFont(context, resId) ?: return false
            }
            solway = FontFamily(
                Font(R.font.solway_regular, FontWeight.Normal),
                Font(R.font.solway_bold, FontWeight.Bold),
                Font(R.font.solway_bold, FontWeight.SemiBold),
            )
            publicSans = FontFamily(
                Font(R.font.publicsans_variable, FontWeight.Normal),
                Font(R.font.publicsans_variable, FontWeight.Medium),
                Font(R.font.publicsans_variable, FontWeight.SemiBold),
                Font(R.font.publicsans_variable, FontWeight.Bold),
            )
            usingBundledFonts = true
            Log.i(TAG, "Bundled Solway + Public Sans loaded")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Bundled font load failed", e)
            false
        }
    }

    private fun isValidFontResource(context: Context, resId: Int): Boolean {
        return try {
            context.resources.openRawResource(resId).use { stream ->
                val header = ByteArray(4)
                if (stream.read(header) != 4) return false
                isTrueTypeOrOpenType(header)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Font header check failed for res $resId", e)
            false
        }
    }

    private fun isTrueTypeOrOpenType(header: ByteArray): Boolean {
        if (header.size < 4) return false
        val isTrueType = header[0] == 0x00.toByte() && header[1] == 0x01.toByte()
        val isOpenType = header[0] == 'O'.code.toByte() && header[1] == 'T'.code.toByte() &&
            header[2] == 'T'.code.toByte() && header[3] == 'O'.code.toByte()
        val isTrue = header[0] == 0x74.toByte() && header[1] == 0x72.toByte() &&
            header[2] == 0x75.toByte() && header[3] == 0x65.toByte()
        return isTrueType || isOpenType || isTrue
    }

    private fun useSystemDefaults() {
        solway = FontFamily.Serif
        publicSans = FontFamily.SansSerif
        usingBundledFonts = false
    }
}
