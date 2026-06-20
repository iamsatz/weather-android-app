package com.kosmos.android.model

enum class VerdictPriority { SEVERE, ACTION, NORMAL }

enum class WeatherCondition {
    CLEAR_MORNING,
    CLEAR_MIDDAY,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    HAZE,
    LIGHT_RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
}

data class TimeWindow(
    val startHour: Int,
    val endHour: Int,
    val label: String,
) {
    fun contains(hour: Int): Boolean = hour in startHour..endHour
}

data class Verdict(
    val id: String,
    val emoji: String,
    val title: String,
    val detail: String,
    val priority: VerdictPriority,
    val accentColor: Long,
    val timeWindow: TimeWindow? = null,
)

data class HourlyForecast(
    val hour: Int,
    val label: String,
    val temp: Int,
    val emoji: String,
    val isNow: Boolean = false,
    val uvPlain: String? = null,
)

data class DailyForecast(
    val dateLabel: String,
    val high: Int,
    val low: Int,
    val precipMax: Int,
)

data class WeatherAlert(
    val id: String,
    val emoji: String,
    val title: String,
    val detail: String,
    val severity: VerdictPriority,
)

data class WeatherSnapshot(
    val city: String,
    val locationLine: String,
    val dateLabel: String,
    val temp: Int,
    val tempCelsius: Double,
    val latitude: Double,
    val longitude: Double,
    val feelsLike: Int,
    val feelsLikePlain: String,
    val high: Int,
    val low: Int,
    val condition: WeatherCondition,
    val conditionLabel: String,
    val aqiLabel: String,
    val aqiColor: Long,
    val uvIndex: Double,
    val aqiValue: Int? = null,
    val humidity: Int,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast> = emptyList(),
    val verdicts: List<Verdict>,
    val nowcast: String,
    val nowcastIsWet: Boolean,
    val updatedMinutesAgo: Int,
    val hasLiveLocation: Boolean = false,
    val locationSourceLabel: String = "",
    val userModeId: String = "default",
    val localeCode: String = "en",
    val fusionSources: List<String> = emptyList(),
    val isDay: Boolean = true,
    val currentHour: Int = 12,
    val weatherCode: Int = 0,
    val sunriseHour: Int = 6,
    val sunriseMinute: Int = 0,
    val sunsetHour: Int = 18,
    val sunsetMinute: Int = 0,
    val windSpeedKmh: Double = 0.0,
)
