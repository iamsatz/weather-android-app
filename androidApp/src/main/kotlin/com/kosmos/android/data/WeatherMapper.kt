package com.kosmos.android.data

import com.kosmos.android.model.DailyForecast
import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.mode.UserMode
import com.kosmos.shared.engine.NowcastEngine
import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.models.WeatherSnapshot as SharedSnapshot
import com.kosmos.shared.models.upcomingHours
import com.kosmos.shared.models.Verdict as SharedVerdict
import com.kosmos.shared.models.VerdictPriority as SharedPriority
import kotlin.math.max

object WeatherMapper {

    fun toUiSnapshot(
        shared: SharedSnapshot,
        verdicts: List<SharedVerdict>,
        neighborhood: String?,
        country: String?,
        useCelsius: Boolean,
        use24Hour: Boolean,
        updatedMinutesAgo: Int,
        hasLiveLocation: Boolean,
        locationSourceLabel: String = "",
        locale: AppLocale = AppLocale.EN,
        userMode: UserMode = UserMode.DEFAULT,
    ): WeatherSnapshot {
        val next12 = shared.upcomingHours(12)

        val areaName = PlainLanguage.formatLocationLine(neighborhood, shared.cityName, country)
        val nowcast = NowcastEngine.generate(shared.hourly, shared.nowIndex, areaName)
        val fusionSuffix = shared.fusionMeta?.sources?.takeIf { it.size > 1 }?.let {
            " · Fused: ${it.joinToString(" + ")}"
        } ?: ""

        return WeatherSnapshot(
            city = shared.cityName,
            locationLine = areaName,
            dateLabel = shared.dateLabel,
            temp = PlainLanguage.toDisplayTemp(shared.temp, useCelsius),
            tempCelsius = shared.temp,
            latitude = shared.latitude,
            longitude = shared.longitude,
            feelsLike = PlainLanguage.toDisplayTemp(shared.feelsLike, useCelsius),
            feelsLikePlain = PlainLanguage.feelsLikeDescription(shared.feelsLike),
            high = PlainLanguage.toDisplayTemp(shared.high, useCelsius),
            low = PlainLanguage.toDisplayTemp(shared.low, useCelsius),
            condition = weatherCodeToCondition(shared.weatherCode, shared.isDay, shared.temp, shared.aqi),
            conditionLabel = PlainLanguage.weatherConditionLabel(shared.weatherCode),
            aqiLabel = PlainLanguage.aqiLabel(shared.aqi),
            aqiColor = PlainLanguage.aqiColor(shared.aqi),
            uvIndex = shared.uvIndex,
            aqiValue = shared.aqi,
            humidity = shared.humidity,
            hourly = next12.map { hour ->
                HourlyForecast(
                    hour = hour.hour,
                    label = if (hour.isNow) {
                        "Now"
                    } else {
                        PlainLanguage.formatHourLabel(hour.hour, use24Hour)
                    },
                    temp = PlainLanguage.toDisplayTemp(hour.temp, useCelsius),
                    emoji = PlainLanguage.weatherEmoji(hour.weatherCode, hour.isDay),
                    isNow = hour.isNow,
                    uvPlain = PlainLanguage.uvDescription(hour.uvIndex),
                )
            },
            daily = shared.daily.map { day ->
                DailyForecast(
                    dateLabel = day.dateLabel,
                    high = PlainLanguage.toDisplayTemp(day.high, useCelsius),
                    low = PlainLanguage.toDisplayTemp(day.low, useCelsius),
                    precipMax = day.precipProbabilityMax,
                )
            },
            verdicts = verdicts.map { toUiVerdict(it) },
            nowcast = nowcast.message + fusionSuffix,
            nowcastIsWet = nowcast.isWet,
            updatedMinutesAgo = max(0, updatedMinutesAgo),
            hasLiveLocation = hasLiveLocation,
            locationSourceLabel = locationSourceLabel,
            userModeId = userMode.id,
            localeCode = locale.code,
            fusionSources = shared.fusionMeta?.sources ?: emptyList(),
            isDay = shared.isDay,
            currentHour = shared.currentHour,
            weatherCode = shared.weatherCode,
            sunriseHour = shared.sunriseHour,
            sunriseMinute = shared.sunriseMinute,
            sunsetHour = shared.sunsetHour,
            sunsetMinute = shared.sunsetMinute,
            windSpeedKmh = shared.windSpeedKmh,
        )
    }

    private fun toUiVerdict(verdict: SharedVerdict): Verdict = Verdict(
        id = verdict.id,
        emoji = verdict.emoji,
        title = verdict.title,
        detail = verdict.detail,
        priority = when (verdict.priority) {
            SharedPriority.SEVERE -> VerdictPriority.SEVERE
            SharedPriority.ACTION -> VerdictPriority.ACTION
            SharedPriority.NORMAL -> VerdictPriority.NORMAL
        },
        accentColor = verdict.accentColor,
        timeWindow = verdict.timeWindow?.let {
            com.kosmos.android.model.TimeWindow(it.startHour, it.endHour, it.label)
        },
    )

    fun toUiVerdicts(verdicts: List<SharedVerdict>): List<Verdict> =
        verdicts.map { toUiVerdict(it) }

    fun weatherCodeToCondition(code: Int, isDay: Boolean, temp: Double, aqi: Int? = null): WeatherCondition {
        val aqiValue = aqi ?: 50
        return when {
            code in listOf(95, 96, 99) -> WeatherCondition.THUNDERSTORM
            code in listOf(45, 48) -> WeatherCondition.FOG
            code in listOf(71, 73, 75, 77, 85, 86) -> WeatherCondition.SNOW
            code in listOf(65, 67, 82) -> WeatherCondition.HEAVY_RAIN
            code in listOf(51, 53, 55, 61, 63, 80, 81) -> WeatherCondition.LIGHT_RAIN
            code in listOf(2, 3) -> WeatherCondition.OVERCAST
            code in listOf(0, 1) && aqiValue >= 100 -> WeatherCondition.HAZE
            code in listOf(0, 1) && isDay && temp < 30 -> WeatherCondition.CLEAR_MORNING
            code in listOf(0, 1) && isDay -> WeatherCondition.CLEAR_MIDDAY
            else -> WeatherCondition.PARTLY_CLOUDY
        }
    }
}
