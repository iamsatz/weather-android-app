package com.kosmos.android.ui.designsystem.organisms

import com.kosmos.android.model.Verdict
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.tokens.WeatherPalette

fun WeatherCondition.toPalette(isDay: Boolean = true, timeOfDay: TimeOfDay = if (isDay) TimeOfDay.Day else TimeOfDay.Night): WeatherPalette = when (this) {
    WeatherCondition.CLEAR_MORNING -> when (timeOfDay) {
        TimeOfDay.Night -> WeatherPalette.ClearNight
        TimeOfDay.Dawn, TimeOfDay.Dusk -> WeatherPalette.ClearMorning
        TimeOfDay.Day -> WeatherPalette.ClearMorning
    }
    WeatherCondition.CLEAR_MIDDAY -> when (timeOfDay) {
        TimeOfDay.Night -> WeatherPalette.ClearNight
        TimeOfDay.Dawn, TimeOfDay.Dusk -> WeatherPalette.ClearEvening
        TimeOfDay.Day -> WeatherPalette.ClearMidday
    }
    WeatherCondition.PARTLY_CLOUDY -> when (timeOfDay) {
        TimeOfDay.Night -> WeatherPalette.PartlyCloudyNight
        else -> WeatherPalette.PartlyCloudyDay
    }
    WeatherCondition.OVERCAST -> when (timeOfDay) {
        TimeOfDay.Night -> WeatherPalette.OvercastNight
        else -> WeatherPalette.OvercastDay
    }
    WeatherCondition.FOG -> WeatherPalette.Foggy
    WeatherCondition.HAZE -> WeatherPalette.HazyAqi
    WeatherCondition.LIGHT_RAIN -> WeatherPalette.Drizzle
    WeatherCondition.HEAVY_RAIN -> WeatherPalette.HeavyRain
    WeatherCondition.THUNDERSTORM -> WeatherPalette.Thunderstorm
    WeatherCondition.SNOW -> WeatherPalette.LightSnow
}

fun WeatherSnapshot.toMoodPalette(): WeatherPalette {
    val timeOfDay = resolveTimeOfDay(currentHour, isDay, sunriseHour, sunsetHour)

    if (!isDay || timeOfDay == TimeOfDay.Night) {
        return when (condition) {
            WeatherCondition.LIGHT_RAIN -> WeatherPalette.HeavyRain
            WeatherCondition.HEAVY_RAIN -> WeatherPalette.HeavyRain
            WeatherCondition.THUNDERSTORM -> WeatherPalette.Thunderstorm
            WeatherCondition.PARTLY_CLOUDY -> WeatherPalette.PartlyCloudyNight
            WeatherCondition.OVERCAST -> WeatherPalette.OvercastNight
            WeatherCondition.FOG -> WeatherPalette.Foggy
            WeatherCondition.HAZE -> WeatherPalette.HazyAqi
            WeatherCondition.SNOW -> WeatherPalette.HeavySnow
            else -> WeatherPalette.ClearNight
        }
    }

    val top = verdicts.firstOrNull {
        it.priority == VerdictPriority.SEVERE || it.priority == VerdictPriority.ACTION
    }
    top?.let { return verdictToMood(it, timeOfDay) }

    return when (condition) {
        WeatherCondition.CLEAR_MIDDAY -> when {
            timeOfDay == TimeOfDay.Dawn -> WeatherPalette.ClearMorning
            timeOfDay == TimeOfDay.Dusk -> WeatherPalette.GoldenHour
            temp >= 35 -> WeatherPalette.HeatPeak
            (aqiValue ?: 0) >= 100 -> WeatherPalette.HazyAqi
            else -> WeatherPalette.PleasantDay
        }
        WeatherCondition.CLEAR_MORNING -> when (timeOfDay) {
            TimeOfDay.Dawn -> WeatherPalette.ClearMorning
            TimeOfDay.Dusk -> WeatherPalette.GoldenHour
            else -> WeatherPalette.ColdMorning
        }
        WeatherCondition.PARTLY_CLOUDY -> when (timeOfDay) {
            TimeOfDay.Dusk -> WeatherPalette.GoldenHour
            else -> WeatherPalette.PartlyCloudyDay
        }
        WeatherCondition.OVERCAST -> WeatherPalette.OvercastDay
        WeatherCondition.FOG -> WeatherPalette.Foggy
        WeatherCondition.HAZE -> WeatherPalette.HazyAqi
        WeatherCondition.LIGHT_RAIN -> WeatherPalette.MonsoonTeal
        WeatherCondition.HEAVY_RAIN -> WeatherPalette.HeavyRain
        WeatherCondition.THUNDERSTORM -> WeatherPalette.Thunderstorm
        WeatherCondition.SNOW -> WeatherPalette.LightSnow
    }
}

private fun verdictToMood(verdict: Verdict, timeOfDay: TimeOfDay): WeatherPalette {
    val base = verdict.id.substringBefore('.')
    return when (base) {
        "heat", "hydration", "avoidHours" -> WeatherPalette.HeatPeak
        "raincoat", "umbrella" -> WeatherPalette.MonsoonTeal
        "cold" -> WeatherPalette.ColdMorning
        "bestWalk", "canJog", "easy", "vitaminD" -> WeatherPalette.PleasantDay
        "goldenHour" -> if (timeOfDay == TimeOfDay.Night) WeatherPalette.ClearNight else WeatherPalette.GoldenHour
        else -> if (timeOfDay == TimeOfDay.Night) WeatherPalette.PartlyCloudyNight else WeatherPalette.PartlyCloudyDay
    }
}
