package com.kosmos.android.ui.designsystem.atoms

import androidx.annotation.DrawableRes
import com.kosmos.android.R
import com.kosmos.android.model.WeatherCondition

object VerdictArt {
    @DrawableRes
    fun listOrIcon(verdictId: String): Int {
        listArt(verdictId)?.let { return it }
        return icon(verdictId)
    }

    @DrawableRes
    fun listArt(verdictId: String): Int? = when {
        verdictId.startsWith("vitaminD") -> R.drawable.list_vitamin_d_icon
        verdictId.startsWith("goldenHour") -> R.drawable.list_golden_hour_icon
        verdictId.startsWith("raincoat") || verdictId.startsWith("umbrella") -> R.drawable.list_raincoat
        verdictId.startsWith("laundry") -> R.drawable.list_laundry
        verdictId.startsWith("bestWalk") || verdictId.startsWith("canJog") || verdictId.startsWith("outdoor") ->
            R.drawable.list_walk
        verdictId.startsWith("mosquito") -> R.drawable.list_mosquito
        else -> null
    }

    @DrawableRes
    fun heroArt(verdictId: String): Int? = when {
        verdictId.startsWith("vitaminD") -> R.drawable.hero_vitamin_d
        verdictId.startsWith("goldenHour") -> R.drawable.hero_golden_hour
        verdictId.startsWith("umbrella.light") -> R.drawable.hero_umbrella_light
        verdictId.startsWith("umbrella") -> R.drawable.hero_umbrella
        verdictId.startsWith("raincoat") -> R.drawable.hero_raincoat
        verdictId.startsWith("heat.caution") -> R.drawable.hero_heat_caution
        verdictId.startsWith("heat") -> R.drawable.hero_heat
        verdictId.startsWith("sunProtection") -> R.drawable.hero_sun_protection
        verdictId.startsWith("cold") -> R.drawable.hero_cold
        verdictId.startsWith("coolerTomorrow") -> R.drawable.hero_cooler_tomorrow
        verdictId.startsWith("avoidHours") -> R.drawable.hero_avoid_hours
        verdictId.startsWith("hydration") -> R.drawable.hero_hydration
        verdictId.startsWith("bestWalk.evening") || verdictId.startsWith("outdoor.evening") ->
            R.drawable.hero_outdoor_evening
        verdictId.startsWith("bestWalk") || verdictId.startsWith("outdoor.morning") ->
            R.drawable.hero_outdoor_morning
        verdictId.startsWith("air") && verdictId.contains("hazard") -> R.drawable.hero_air_hazard
        verdictId.startsWith("air") && (verdictId.contains("rough") || verdictId.contains("unhealthy")) ->
            R.drawable.hero_air_rough
        verdictId.startsWith("air") -> R.drawable.hero_air
        verdictId.startsWith("openWindows") -> R.drawable.hero_open_windows
        verdictId.startsWith("closeWindows") -> R.drawable.hero_close_windows
        verdictId.startsWith("laundry") -> R.drawable.hero_laundry
        verdictId.startsWith("mosquito") -> R.drawable.hero_mosquito
        verdictId.startsWith("wind") -> R.drawable.hero_wind
        verdictId.startsWith("commute") -> R.drawable.hero_commute_rain
        verdictId.contains("thunder") -> R.drawable.hero_thunderstorm
        else -> null
    }

    @DrawableRes
    fun heroForSnapshot(verdictId: String?, condition: WeatherCondition): Int {
        verdictId?.let { heroArt(it) }?.let { return it }
        return when (condition) {
            WeatherCondition.THUNDERSTORM -> R.drawable.hero_thunderstorm
            WeatherCondition.HEAVY_RAIN -> R.drawable.hero_raincoat
            WeatherCondition.LIGHT_RAIN -> R.drawable.hero_umbrella
            WeatherCondition.SNOW -> R.drawable.hero_cold
            WeatherCondition.HAZE -> R.drawable.hero_air_rough
            WeatherCondition.FOG -> R.drawable.hero_air
            WeatherCondition.CLEAR_MORNING, WeatherCondition.CLEAR_MIDDAY -> R.drawable.hero_outdoor
            else -> R.drawable.hero_outdoor
        }
    }

    @DrawableRes
    fun icon(verdictId: String): Int = when {
        verdictId.startsWith("umbrella") -> R.drawable.ic_umbrella
        verdictId.startsWith("raincoat") -> R.drawable.ic_cloud_rain
        verdictId.startsWith("heat") || verdictId.startsWith("sunProtection") || verdictId.startsWith("easy") ->
            R.drawable.ic_sun
        verdictId.startsWith("vitaminD") || verdictId.startsWith("goldenHour") -> R.drawable.ic_sun_horizon
        verdictId.startsWith("hydration") -> R.drawable.ic_drop
        verdictId.startsWith("cold") || verdictId.startsWith("coolerTomorrow") -> R.drawable.ic_thermometer
        verdictId.startsWith("openWindows") || verdictId.startsWith("wind") -> R.drawable.ic_wind
        verdictId.startsWith("closeWindows") -> R.drawable.ic_door
        verdictId.startsWith("laundry") -> R.drawable.ic_t_shirt
        verdictId.startsWith("mosquito") -> R.drawable.ic_leaf
        verdictId.startsWith("avoidHours") -> R.drawable.ic_prohibit
        verdictId.startsWith("air") -> R.drawable.ic_mask_happy
        verdictId.startsWith("bestWalk") || verdictId.startsWith("canJog") || verdictId.startsWith("outdoor") ->
            R.drawable.ic_person_simple_walk
        verdictId.startsWith("commute") -> R.drawable.ic_motorcycle
        verdictId.startsWith("map") -> R.drawable.ic_map_pin
        else -> R.drawable.ic_clock
    }
}
