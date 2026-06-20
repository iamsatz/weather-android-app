package com.kosmos.shared.engine

object PlainLanguage {

    fun aqiLabel(aqi: Int?): String = when {
        aqi == null -> "Unknown"
        aqi < 50 -> "Good"
        aqi < 100 -> "OK"
        aqi < 150 -> "Rough"
        else -> "Bad"
    }

    fun aqiColor(aqi: Int?): Long = when {
        aqi == null -> 0xFF888888
        aqi < 50 -> 0xFF4DC85A
        aqi < 100 -> 0xFFE6C033
        aqi < 150 -> 0xFFE68C33
        else -> 0xFFE64D4D
    }

    fun uvDescription(uv: Double): String = when {
        uv >= 11 -> "Extreme"
        uv >= 8 -> "Very strong"
        uv >= 6 -> "Strong"
        uv >= 3 -> "Moderate"
        else -> "Low"
    }

    fun feelsLikeDescription(feelsLike: Double): String = when {
        feelsLike >= 40 -> "Dangerous heat"
        feelsLike >= 35 -> "Very hot"
        feelsLike >= 30 -> "Hot"
        feelsLike >= 20 -> "Warm"
        feelsLike >= 10 -> "Cool"
        else -> "Cold"
    }

    fun weatherConditionLabel(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Cloudy"
    }

    fun weatherEmoji(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1, 2, 3 -> if (isDay) "🌤" else "☁️"
        45, 48 -> "🌫"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "☔"
        66, 67, 71, 73, 75, 77, 85, 86 -> "❄️"
        95, 96, 99 -> "⛈"
        else -> if (isDay) "☁️" else "🌙"
    }

    fun formatHourLabel(hour: Int, use24Hour: Boolean = false): String {
        if (use24Hour) {
            return "${hour.toString().padStart(2, '0')}:00"
        }
        return when {
            hour == 0 || hour == 24 -> "12AM"
            hour < 12 -> "${hour}AM"
            hour == 12 -> "12PM"
            else -> "${hour - 12}PM"
        }
    }

    fun toDisplayTemp(celsius: Double, useCelsius: Boolean): Int =
        if (useCelsius) celsius.toInt() else ((celsius * 9.0 / 5.0) + 32.0).toInt()

    fun formatLocationLine(neighborhood: String?, city: String, country: String? = null): String {
        val parts = mutableListOf<String>()
        neighborhood
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals(city, ignoreCase = true) }
            ?.let { parts.add(it) }
        city.trim().takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        country
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals(city, ignoreCase = true) }
            ?.let { parts.add(it) }
        return parts.distinctBy { it.lowercase() }.joinToString(", ")
    }

    fun formatTimeRange(startHour: Int, endHour: Int): String {
        val start = formatHourLabel(startHour)
        val end = formatHourLabel(endHour)
        return "$start–$end"
    }

    fun formatPreciseTime(hour: Int, minute: Int): String {
        val h12 = when {
            hour == 0 || hour == 24 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"
        return if (minute == 0) "$h12 $amPm" else "$h12:${minute.toString().padStart(2, '0')} $amPm"
    }
}
