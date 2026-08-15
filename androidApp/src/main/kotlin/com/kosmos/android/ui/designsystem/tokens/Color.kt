package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.ui.graphics.Color

object KosmosColor {
    val accent = Color(0xFFEB3228)
    val temp = Color(0xFFC6110C)
    val primary = accent
    val primaryLight = Color(0xFFEB3228)
    val nowcastAccent = Color(0xFF3380C7)

    val bgSurface = Color(0xFFFFFFFF)
    val insightTime = Color(0xA314141A)
    val bgSurfaceDark = Color(0xFF161616)
    val bgSubtle = Color(0xFFF8F8F8)
    val bgSubtleDark = Color(0xFF222222)
    val bgPill = Color(0xFFEDEBE9)
    val bgPillDark = Color(0xFF222222)

    val chipWalk = Color(0xFFFFDDFC)
    val chipExercise = Color(0xFFFFEBD4)
    val chipOutdoor = Color(0xFFE7F2B9)

    val ribbonGo = Color(0xFF4A7C32)
    val ribbonCaution = Color(0xFF8A5A12)
    val ribbonAvoid = Color(0xFFC6110C)
    val ribbonEmpty = Color(0xFF6B6B72)

    val bandGo = Color(0xFF4DC85A)
    val bandGoDark = Color(0xFF3D8C4A)
    val bandCaution = Color(0xFFF7BF33)
    val bandCautionDark = Color(0xFFB8921A)
    val bandStay = Color(0xFFEB4D4D)
    val bandStayDark = Color(0xFFC43D3D)

    val textPrimary = Color(0xFF14141A)
    val textSecondary = Color(0xFF5C5C63)
    val textTertiary = Color(0xFF8A8A90)
    val textMuted = textTertiary
    val textOnGradient = Color.White
    val border = Color(0xFFEDEBEB)
    val borderDark = Color(0xFF2E2E32)
    val textPrimaryDark = Color(0xFFF3F3F4)
    val textSecondaryDark = Color(0xFFA8A8AE)
    val textTertiaryDark = Color(0xD9A8A8AE)

    val cardBackground = bgSurface
    val cardBackgroundDark = bgSurfaceDark
    val glassBorder = border
    val glassBorderDark = borderDark

    val surfaceLight = bgSubtle
    val surfaceDark = bgSurfaceDark
    val surfaceVariantDark = bgSubtleDark

    val aqiGood = Color(0xFF4DC85A)
    val aqiModerate = Color(0xFFF7BF33)
    val aqiUnhealthySg = Color(0xFFFA8C33)
    val aqiUnhealthy = Color(0xFFEB4D4D)
    val aqiVeryUnhealthy = Color(0xFFA653CC)
    val aqiHazardous = Color(0xFF8C1F33)

    object Gradients {
        val clearMorning = Color(0xFFFFE0B2) to Color(0xFFF8B98A)
        val clearMidday = Color(0xFF7BBAF4) to Color(0xFF3D87D4)
        val clearEvening = Color(0xFFFFB07A) to Color(0xFFC46B82)
        val clearNight = Color(0xFF1A1B3A) to Color(0xFF050618)
        val partlyCloudyDay = Color(0xFFA8C7E5) to Color(0xFF5E89B8)
        val partlyCloudyNight = Color(0xFF2B3158) to Color(0xFF0D1029)
        val overcastDay = Color(0xFF888E9C) to Color(0xFF535869)
        val overcastNight = Color(0xFF3D4255) to Color(0xFF0F1219)
        val foggy = Color(0xFFD5D5D5) to Color(0xFF9A9A9A)
        val drizzle = Color(0xFF7A8B98) to Color(0xFF454D5A)
        val lightRain = Color(0xFF5A6B7A) to Color(0xFF2A3340)
        val heavyRain = Color(0xFF3A4252) to Color(0xFF13161F)
        val thunderstorm = Color(0xFF2D2E45) to Color(0xFF0A0B1A)
        val lightSnow = Color(0xFFD8DEE8) to Color(0xFF969FB2)
        val heavySnow = Color(0xFFA5B0C0) to Color(0xFF525C70)
        val sleet = Color(0xFF6E7889) to Color(0xFF363D4B)
        val hazyAqi = Color(0xFFC9AC7E) to Color(0xFF947A5E)
        val goldenHour = Color(0xFFFFB67A) to Color(0xFF9B6E8A)
        val heatPeak = Color(0xFFFF8C42) to Color(0xFFE63946)
        val pleasantDay = Color(0xFF7BC47F) to Color(0xFF2D936C)
        val monsoonTeal = Color(0xFF4ECDC4) to Color(0xFF2B6CB0)
        val coldMorning = Color(0xFFB8D4E8) to Color(0xFF6B9AC4)
    }

    object VerdictAccent {
        val raincoat = Color(0xFF1A5CB3)
        val umbrella = Color(0xFF3380C7)
        val umbrellaLight = Color(0xFF5294D6)
        val heat = Color(0xFFD96B33)
        val sunProtection = Color(0xFFEB9933)
        val cold = Color(0xFF66A8D1)
        val airGood = Color(0xFF4DC85A)
        val airModerate = Color(0xFFF2C633)
        val airUnhealthySg = Color(0xFFFA8C33)
        val airUnhealthy = Color(0xFFEB4747)
        val airHazardous = Color(0xFF9E2A2A)
        val vitaminD = Color(0xFFEB3228)
        val bestWalk = Color(0xFF3FA67F)
        val avoidHours = Color(0xFFC74D33)
        val canJog = Color(0xFF4DB371)
        val hydration = Color(0xFF3F8CD9)
        val mosquito = Color(0xFF8C4D80)
        val openWindows = Color(0xFF80B3D9)
        val closeWindows = Color(0xFF8C7366)
        val laundry = Color(0xFFBFB259)
        val coolerTomorrow = Color(0xFF7399BF)
        val goldenHour = Color(0xFFF28C4D)
        val easyDay = Color(0xFF3F8C66)
        val commute = Color(0xFF7B5EA7)
        val wind = Color(0xFF5A7C9E)
    }
}

enum class WeatherPalette {
    ClearMorning,
    ClearMidday,
    ClearEvening,
    ClearNight,
    PartlyCloudyDay,
    PartlyCloudyNight,
    OvercastDay,
    OvercastNight,
    Foggy,
    Drizzle,
    LightRain,
    HeavyRain,
    Thunderstorm,
    LightSnow,
    HeavySnow,
    Sleet,
    HazyAqi,
    GoldenHour,
    HeatPeak,
    PleasantDay,
    MonsoonTeal,
    ColdMorning,
}

fun WeatherPalette.gradient(): Pair<Color, Color> = when (this) {
    WeatherPalette.ClearMorning -> KosmosColor.Gradients.clearMorning
    WeatherPalette.ClearMidday -> KosmosColor.Gradients.clearMidday
    WeatherPalette.ClearEvening -> KosmosColor.Gradients.clearEvening
    WeatherPalette.ClearNight -> KosmosColor.Gradients.clearNight
    WeatherPalette.PartlyCloudyDay -> KosmosColor.Gradients.partlyCloudyDay
    WeatherPalette.PartlyCloudyNight -> KosmosColor.Gradients.partlyCloudyNight
    WeatherPalette.OvercastDay -> KosmosColor.Gradients.overcastDay
    WeatherPalette.OvercastNight -> KosmosColor.Gradients.overcastNight
    WeatherPalette.Foggy -> KosmosColor.Gradients.foggy
    WeatherPalette.Drizzle -> KosmosColor.Gradients.drizzle
    WeatherPalette.LightRain -> KosmosColor.Gradients.lightRain
    WeatherPalette.HeavyRain -> KosmosColor.Gradients.heavyRain
    WeatherPalette.Thunderstorm -> KosmosColor.Gradients.thunderstorm
    WeatherPalette.LightSnow -> KosmosColor.Gradients.lightSnow
    WeatherPalette.HeavySnow -> KosmosColor.Gradients.heavySnow
    WeatherPalette.Sleet -> KosmosColor.Gradients.sleet
    WeatherPalette.HazyAqi -> KosmosColor.Gradients.hazyAqi
    WeatherPalette.GoldenHour -> KosmosColor.Gradients.goldenHour
    WeatherPalette.HeatPeak -> KosmosColor.Gradients.heatPeak
    WeatherPalette.PleasantDay -> KosmosColor.Gradients.pleasantDay
    WeatherPalette.MonsoonTeal -> KosmosColor.Gradients.monsoonTeal
    WeatherPalette.ColdMorning -> KosmosColor.Gradients.coldMorning
}
