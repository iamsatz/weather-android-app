package com.kosmos.android.ui.designsystem.organisms

enum class TimeOfDay {
    Dawn,
    Day,
    Dusk,
    Night,
}

fun resolveTimeOfDay(
    currentHour: Int,
    isDay: Boolean,
    sunriseHour: Int,
    sunsetHour: Int,
): TimeOfDay {
    if (!isDay) return TimeOfDay.Night
    val dawnStart = (sunriseHour - 1).coerceAtLeast(0)
    val dawnEnd = sunriseHour + 1
    val duskStart = (sunsetHour - 1).coerceAtLeast(0)
    val duskEnd = (sunsetHour + 1).coerceAtMost(23)
    return when {
        currentHour in dawnStart until dawnEnd -> TimeOfDay.Dawn
        currentHour in duskStart..duskEnd -> TimeOfDay.Dusk
        else -> TimeOfDay.Day
    }
}
