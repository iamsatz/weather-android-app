package com.kosmos.android.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.kosmos.android.model.WeatherCondition

object WeatherWallpaperHelper {

    fun applyFromSnapshot(context: Context, condition: WeatherCondition, temp: Int): Boolean {
        return runCatching {
            val wallpaper = WallpaperManager.getInstance(context)
            val bitmap = gradientBitmap(condition, temp)
            wallpaper.setBitmap(bitmap)
            true
        }.getOrDefault(false)
    }

    private fun gradientBitmap(condition: WeatherCondition, temp: Int): Bitmap {
        val (top, bottom) = when (condition) {
            WeatherCondition.CLEAR_MORNING -> 0xFF87CEEB.toInt() to 0xFFFFE4B5.toInt()
            WeatherCondition.CLEAR_MIDDAY -> 0xFF4A90D9.toInt() to 0xFFFFB347.toInt()
            WeatherCondition.PARTLY_CLOUDY -> 0xFF78909C.toInt() to 0xFFB0BEC5.toInt()
            WeatherCondition.OVERCAST -> 0xFF888E9C.toInt() to 0xFF535869.toInt()
            WeatherCondition.FOG -> 0xFFD5D5D5.toInt() to 0xFF9A9A9A.toInt()
            WeatherCondition.HAZE -> 0xFFC9AC7E.toInt() to 0xFF947A5E.toInt()
            WeatherCondition.LIGHT_RAIN -> 0xFF546E7A.toInt() to 0xFF90A4AE.toInt()
            WeatherCondition.HEAVY_RAIN -> 0xFF3A4252.toInt() to 0xFF13161F.toInt()
            WeatherCondition.THUNDERSTORM -> 0xFF263238.toInt() to 0xFF455A64.toInt()
            WeatherCondition.SNOW -> 0xFFD8DEE8.toInt() to 0xFF969FB2.toInt()
        }
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                top, bottom,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textSize = 72f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$temp°", width / 2f, height * 0.45f, textPaint)
        return bitmap
    }
}
