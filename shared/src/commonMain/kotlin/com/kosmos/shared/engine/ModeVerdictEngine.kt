package com.kosmos.shared.engine

import com.kosmos.shared.models.HourlyData
import com.kosmos.shared.models.TimeWindow
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot
import com.kosmos.shared.models.upcomingHours
import com.kosmos.shared.mode.EmployeeProfile

object ModeVerdictEngine {

    private val heavyRainCodes = setOf(65, 67, 75, 82, 95, 96, 99)
    private val drizzleCodes = setOf(51, 53, 55, 61, 63, 80, 81)

    fun employeeVerdicts(
        snapshot: WeatherSnapshot,
        hidden: Set<String>,
        profile: EmployeeProfile? = null,
        workWeather: WeatherSnapshot? = null,
    ): List<Verdict> {
        val next12 = snapshot.upcomingHours(12)
        val workNext12 = workWeather?.upcomingHours(12) ?: emptyList()
        val workLabel = profile?.workLabel ?: "office"
        val verdicts = mutableListOf<Verdict>()

        if ("workAir" !in hidden && workWeather != null) {
            val homeAqi = snapshot.aqi ?: 50
            val workAqi = workWeather.aqi ?: homeAqi
            if (workAqi >= 100 || workAqi >= homeAqi + 25) {
                verdicts += Verdict(
                    id = "workAir",
                    emoji = "😷",
                    title = "Rough air at $workLabel — WFH if you can",
                    detail = "Air at work is worse than home today — mask on commute or work from home.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFFE64D4D,
                )
            }
        }

        if ("workRainEvening" !in hidden && workNext12.isNotEmpty()) {
            val workEveningRain = workNext12.filter { it.hour in 16..19 }.maxOfOrNull { it.precipProbability } ?: 0
            if (workEveningRain >= 45) {
                verdicts += Verdict(
                    id = "workRainEvening",
                    emoji = "🌧",
                    title = "Rain at $workLabel by 6 PM — leave early",
                    detail = "Work-area rain builds 4–6 PM — wrap up early or keep cover at your desk.",
                    priority = VerdictPriority.SEVERE,
                    accentColor = 0xFF1A5CB3,
                    timeWindow = TimeWindow(16, 18, "4–6 PM"),
                )
            }
        }

        if ("commuteOut" !in hidden) {
            val morningRain = if (workNext12.isNotEmpty()) {
                workNext12.filter { it.hour in 7..11 }.maxOfOrNull { it.precipProbability } ?: 0
            } else {
                next12.filter { it.hour in 7..11 }.maxOfOrNull { it.precipProbability } ?: 0
            }
            if (morningRain < 50) {
                val dest = if (profile?.hasWork == true) workLabel else "work"
                verdicts += Verdict(
                    id = "commuteOut",
                    emoji = "🛵",
                    title = "Leave by 9:10 AM — dry commute to $dest",
                    detail = "Clear till noon. Evening rain may mess up the ride back.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3380C7,
                    timeWindow = TimeWindow(9, 10, "9:10 AM"),
                )
            }
        }

        if ("deskSun" !in hidden && snapshot.isDay && snapshot.uvIndex in 3.0..7.0) {
            verdicts += Verdict(
                id = "deskSun",
                emoji = "🪟",
                title = "Sit near a window 9–11 AM",
                detail = "Soft morning sun = Vitamin D without leaving your desk.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD98C33,
                timeWindow = TimeWindow(9, 11, "9–11 AM"),
            )
        }

        if ("lunchWalk" !in hidden) {
            val lunch = next12.filter { it.hour in 12..14 }
            val comfy = lunch.filter { it.temp in 24.0..34.0 && it.precipProbability < 40 }
            if (comfy.isNotEmpty()) {
                verdicts += Verdict(
                    id = "lunchWalk",
                    emoji = "🚶",
                    title = "Lunch walk: 1–1:30 PM",
                    detail = "Last comfortable window before the afternoon peak — 15 min is plenty.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3FA67F,
                    timeWindow = TimeWindow(13, 14, "1–1:30 PM"),
                )
            }
        }

        if ("screenBreak" !in hidden && snapshot.feelsLike >= 30) {
            verdicts += Verdict(
                id = "screenBreak",
                emoji = "👀",
                title = "Screen break + water at 3 PM",
                detail = "Dry AC air. Step out for 5 min, refill your bottle, look far.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3F8CD9,
                timeWindow = TimeWindow(15, 16, "3 PM"),
            )
        }

        if ("commuteBack" !in hidden) {
            val eveningRain = next12.filter { it.hour in 16..19 }.maxOfOrNull { it.precipProbability } ?: 0
            if (eveningRain >= 40) {
                verdicts += Verdict(
                    id = "commuteBack",
                    emoji = "☔",
                    title = "Carry a raincoat for 6 PM ride",
                    detail = "Rain likely 4–6 PM — keep cover at your desk.",
                    priority = VerdictPriority.SEVERE,
                    accentColor = 0xFF1A5CB3,
                    timeWindow = TimeWindow(16, 18, "4–6 PM"),
                )
            }
        }

        if ("gymWindow" !in hidden) {
            val evening = snapshot.hourly.filter { it.hour in 18..20 }
            val clear = evening.any { it.precipProbability < 30 && it.temp in 24.0..32.0 }
            if (clear) {
                verdicts += Verdict(
                    id = "gymWindow",
                    emoji = "🏋️",
                    title = "Gym / jog: after 6:30 PM",
                    detail = "Rain passes, air clears — best slot once you log off.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF4DB371,
                    timeWindow = TimeWindow(18, 20, "6:30–8 PM"),
                )
            }
        }

        if ("laundryWeekend" !in hidden) {
            val damp = next12.any { it.precipProbability >= 50 }
            if (damp) {
                verdicts += Verdict(
                    id = "laundryWeekend",
                    emoji = "🧺",
                    title = "Save laundry for Saturday",
                    detail = "Weekday evenings stay damp — Saturday morning dries fastest.",
                    priority = VerdictPriority.NORMAL,
                    accentColor = 0xFF8C7366,
                )
            }
        }

        return verdicts.sortedBy { it.priority.ordinal }
    }

    fun familyVerdicts(snapshot: WeatherSnapshot, hidden: Set<String>): List<Verdict> {
        val next12 = snapshot.upcomingHours(12)
        val aqi = snapshot.aqi ?: 50
        val familyAqiThreshold = 80
        val verdicts = mutableListOf<Verdict>()

        if ("schoolUniform" !in hidden) {
            verdicts += Verdict(
                id = "schoolUniform",
                emoji = "🎒",
                title = "School: light layers at 7 AM",
                detail = "${snapshot.temp.toInt()}° at drop-off · cotton tee + light jacket if cool.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3F8CD9,
                timeWindow = TimeWindow(7, 8, "7 AM"),
            )
        }

        if ("sunscreen" !in hidden && snapshot.uvIndex >= 5) {
            verdicts += Verdict(
                id = "sunscreen",
                emoji = "🧴",
                title = "Sunscreen before school",
                detail = "UV climbs fast by 10 AM — SPF 30 on the kids before they leave.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFEB9933,
                timeWindow = TimeWindow(7, 8, "7 AM"),
            )
        }

        if ("peClass" !in hidden && aqi < familyAqiThreshold && snapshot.uvIndex < 8) {
            verdicts += Verdict(
                id = "peClass",
                emoji = "🏫",
                title = "PE class OK at 10 AM",
                detail = "AQI good · UV moderate — sunscreen and caps.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF4DB371,
                timeWindow = TimeWindow(10, 11, "10 AM"),
            )
        }

        if ("kidsHydration" !in hidden && snapshot.feelsLike >= 32) {
            verdicts += Verdict(
                id = "kidsHydration",
                emoji = "🧃",
                title = "Pack an extra water bottle",
                detail = "${snapshot.feelsLike.toInt()}° feels-like by noon — kids dehydrate faster.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3F8CD9,
            )
        }

        if ("pickup" !in hidden) {
            val afternoonRain = next12.filter { it.hour in 15..17 }.maxOfOrNull { it.precipProbability } ?: 0
            if (afternoonRain >= 40) {
                verdicts += Verdict(
                    id = "pickup",
                    emoji = "🚗",
                    title = "Pickup: carry umbrella 3:30 PM",
                    detail = "Rain starts ~4 PM — foldable in the car before school run.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3380C7,
                    timeWindow = TimeWindow(15, 16, "3:30 PM"),
                )
            }
        }

        if ("playtime" !in hidden) {
            val evening = snapshot.hourly.filter { it.hour in 18..19 }
            if (evening.any { it.precipProbability < 30 && it.temp in 24.0..32.0 && aqi < familyAqiThreshold }) {
                verdicts += Verdict(
                    id = "playtime",
                    emoji = "🛝",
                    title = "Evening play: 6–7 PM",
                    detail = "Rain passes · AQI good — park window after homework.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3FA67F,
                    timeWindow = TimeWindow(18, 19, "6–7 PM"),
                )
            }
        }

        if ("kidsAir" !in hidden) {
            if (aqi < familyAqiThreshold) {
                verdicts += Verdict(
                    id = "kidsAir",
                    emoji = "👶",
                    title = "Kids outdoors OK till 4 PM",
                    detail = "Family threshold: AQI under $familyAqiThreshold · stays good till rain.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF4DC85A,
                    timeWindow = TimeWindow(9, 16, "Till 4 PM"),
                )
            } else {
                verdicts += Verdict(
                    id = "kidsAir",
                    emoji = "😷",
                    title = "Keep kids indoors today",
                    detail = "AQI above family threshold ($aqi) — indoor play instead.",
                    priority = VerdictPriority.SEVERE,
                    accentColor = 0xFFE64D4D,
                )
            }
        }

        if ("tiffin" !in hidden && snapshot.feelsLike >= 32) {
            verdicts += Verdict(
                id = "tiffin",
                emoji = "🍱",
                title = "Tiffin: skip hot soup",
                detail = "${snapshot.feelsLike.toInt()}° feels-like by noon — fruit + water, not heavy lunch.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFEB9933,
            )
        }

        return verdicts.sortedBy { it.priority.ordinal }
    }

    fun photographerVerdicts(snapshot: WeatherSnapshot, hidden: Set<String>): List<Verdict> {
        val verdicts = mutableListOf<Verdict>()
        val next12 = snapshot.upcomingHours(12)

        val sunriseLabel = PlainLanguage.formatPreciseTime(snapshot.sunriseHour, snapshot.sunriseMinute)
        val sunsetLabel = PlainLanguage.formatPreciseTime(snapshot.sunsetHour, snapshot.sunsetMinute)

        if ("goldenHour.sunrise" !in hidden) {
            verdicts += Verdict(
                id = "goldenHour.sunrise",
                emoji = "🌄",
                title = "Sunrise golden hour: $sunriseLabel",
                detail = "Soft warm light from the east — best for calm, misty frames.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFE8A35C,
                timeWindow = TimeWindow(snapshot.sunriseHour, snapshot.sunriseHour + 1, sunriseLabel),
            )
        }

        if ("goldenHour.sunset" !in hidden) {
            verdicts += Verdict(
                id = "goldenHour.sunset",
                emoji = "🌅",
                title = "Sunset golden hour: $sunsetLabel",
                detail = "Warm side light for portraits — 18 min before sunset to dusk.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFF28C4D,
                timeWindow = TimeWindow(snapshot.sunsetHour, snapshot.sunsetHour + 1, sunsetLabel),
            )
        }

        if ("blueHour" !in hidden) {
            val blueStart = snapshot.sunsetHour + 1
            verdicts += Verdict(
                id = "blueHour",
                emoji = "🌆",
                title = "Blue hour: ${PlainLanguage.formatHourLabel(blueStart)}",
                detail = "Twilight glow — best for cityscapes after sunset.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF7399BF,
                timeWindow = TimeWindow(blueStart, blueStart + 1, PlainLanguage.formatHourLabel(blueStart)),
            )
        }

        if ("cumulus" !in hidden && snapshot.temp >= 28) {
            val cloudy = next12.filter { it.hour in 15..17 }.any { it.weatherCode in 1..3 }
            if (cloudy) {
                verdicts += Verdict(
                    id = "cumulus",
                    emoji = "☁️",
                    title = "Cumulus building 3–5 PM",
                    detail = "Partly cloudy + heat — dramatic sky textures for landscapes.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF80B3D9,
                    timeWindow = TimeWindow(15, 17, "3–5 PM"),
                )
            }
        }

        if ("fog" !in hidden && snapshot.humidity >= 70 && snapshot.temp in 15.0..25.0) {
            verdicts += Verdict(
                id = "fog",
                emoji = "🌫",
                title = "Fog likely 5:30–7 AM",
                detail = "Cool night + humid air — low fog for moody shots.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF9AA7B5,
                timeWindow = TimeWindow(5, 7, "5:30–7 AM"),
            )
        }

        if ("rainbow" !in hidden) {
            val hasRain = next12.any { it.precipProbability >= 50 }
            if (hasRain && snapshot.isDay) {
                verdicts += Verdict(
                    id = "rainbow",
                    emoji = "🌈",
                    title = "Rainbow watch: 5:30 PM",
                    detail = "Sun angle + post-rain showers — look east after rain stops.",
                    priority = VerdictPriority.NORMAL,
                    accentColor = 0xFFBFB259,
                    timeWindow = TimeWindow(17, 18, "5:30 PM"),
                )
            }
        }

        if ("stars" !in hidden) {
            val nightClear = snapshot.upcomingHours().filter { it.hour >= 21 }.any {
                it.precipProbability < 20 && it.weatherCode in 0..2
            }
            if (nightClear) {
                verdicts += Verdict(
                    id = "stars",
                    emoji = "🌌",
                    title = "Stargazing: maybe after 9 PM",
                    detail = "Clouds clear after 9 PM — try once the rain passes.",
                    priority = VerdictPriority.NORMAL,
                    accentColor = 0xFF2B3158,
                    timeWindow = TimeWindow(21, 23, "9 PM+"),
                )
            }
        }

        return verdicts.sortedBy { it.priority.ordinal }
    }

    private val thunderstormCodes = setOf(95, 96, 99)

    fun homemakerVerdicts(snapshot: WeatherSnapshot, hidden: Set<String>): List<Verdict> {
        val verdicts = mutableListOf<Verdict>()
        val next12 = snapshot.upcomingHours(12)

        if ("homeStormPrep" !in hidden) {
            stormPrepVerdict(next12)?.let { verdicts += it }
        }
        if ("homePreserve" !in hidden) {
            preserveWindowVerdict(snapshot)?.let { verdicts += it }
        }
        if ("homeDrying" !in hidden) {
            dryingVerdict(snapshot)?.let { verdicts += it }
        }
        if ("homeSpoilage" !in hidden && snapshot.feelsLike >= 32 && snapshot.humidity >= 70) {
            verdicts += Verdict(
                id = "homeSpoilage",
                emoji = "🥛",
                title = "Food spoils fast today",
                detail = "Hot + humid — milk, curd and leftovers turn quickly. Store cool, cook fresh.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD98C33,
            )
        }
        if ("homeVegShopping" !in hidden) {
            vegShoppingVerdict(snapshot)?.let { verdicts += it }
        }
        if ("homeMosquito" !in hidden) {
            mosquitoVerdict(snapshot)?.let { verdicts += it }
        }

        return verdicts.sortedBy { it.priority.ordinal }
    }

    private fun stormPrepVerdict(next12: List<HourlyData>): Verdict? {
        val stormHour = next12.firstOrNull {
            it.weatherCode in heavyRainCodes || it.weatherCode in thunderstormCodes || it.windGustKmh >= 35
        } ?: return null
        val lightning = next12.any { it.weatherCode in thunderstormCodes }
        val strong = lightning || (next12.maxOfOrNull { it.windGustKmh } ?: 0.0) >= 50
        val label = PlainLanguage.formatHourLabel(stormHour.hour)
        val detail = if (lightning) {
            "Charge the inverter + phones, fill water now, and unplug the TV/fridge before it hits around $label."
        } else {
            "Charge the inverter + phones and fill water now — rough weather likely around $label."
        }
        return Verdict(
            id = "homeStormPrep",
            emoji = "🔌",
            title = "Prep before the storm",
            detail = detail,
            priority = if (strong) VerdictPriority.SEVERE else VerdictPriority.ACTION,
            accentColor = 0xFF5A6CA7,
            timeWindow = TimeWindow(stormHour.hour, stormHour.hour, label),
        )
    }

    private fun preserveWindowVerdict(snapshot: WeatherSnapshot): Verdict? {
        val days = snapshot.daily
        if (days.size < 3) return null

        var bestStart = -1
        var bestLen = 0
        var run = 0
        var runStart = 0
        for (i in days.indices) {
            val dry = days[i].precipProbabilityMax < 25 && days[i].high >= 32
            if (dry) {
                if (run == 0) runStart = i
                run++
                if (run > bestLen) {
                    bestLen = run
                    bestStart = runStart
                }
            } else {
                run = 0
            }
        }
        if (bestLen < 3) return null

        val startLabel = if (bestStart == 0) "today" else days[bestStart].dateLabel
        val endLabel = days[bestStart + bestLen - 1].dateLabel
        val rangeLabel = "$startLabel–$endLabel"
        return Verdict(
            id = "homePreserve",
            emoji = "🌶",
            title = "Good drying spell: $rangeLabel",
            detail = "$bestLen dry, hot days in a row — set out pickles, vadiyalu, appadalu or red chillies.",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFFC8742D,
        )
    }

    private fun dryingVerdict(snapshot: WeatherSnapshot): Verdict? {
        val next8 = snapshot.upcomingHours(8)
        val maxPrecip = next8.maxOfOrNull { it.precipProbability } ?: 100
        val sunnyHours = next8.count { it.uvIndex >= 2 && it.isDay }
        if (snapshot.humidity < 75 && maxPrecip < 35 && sunnyHours >= 2) {
            return Verdict(
                id = "homeDrying",
                emoji = "👕",
                title = "Clothes will dry fast",
                detail = "Dry air, little rain — hang the washing early, dry in ~$sunnyHours hours of sun.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF5AA0D9,
            )
        }
        return null
    }

    private fun vegShoppingVerdict(snapshot: WeatherSnapshot): Verdict? {
        val tomorrow = snapshot.daily.getOrNull(1) ?: return null
        val todayPrecip = snapshot.upcomingHours().maxOfOrNull { it.precipProbability } ?: 0
        if (todayPrecip < 40 && tomorrow.precipProbabilityMax >= 60) {
            return Verdict(
                id = "homeVegShopping",
                emoji = "🧺",
                title = "Buy veggies today",
                detail = "Rain likely tomorrow — stock up now before prices jump and quality drops.",
                priority = VerdictPriority.NORMAL,
                accentColor = 0xFF4DB371,
            )
        }
        return null
    }

    private fun mosquitoVerdict(snapshot: WeatherSnapshot): Verdict? {
        if (snapshot.currentHour !in 17..21) return null
        if (snapshot.temp !in 20.0..32.0) return null
        if (snapshot.humidity < 60) return null
        val maxPrecipToday = snapshot.hourly.maxOfOrNull { it.precipProbability } ?: 0
        if (maxPrecipToday < 40) return null
        return Verdict(
            id = "homeMosquito",
            emoji = "🦟",
            title = "Mosquito hour — cover up",
            detail = "Recent rain breeds mosquitoes — sleeves, repellent, screens. Empty any standing water.",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF8C4D80,
            timeWindow = TimeWindow(17, 21, "5–9 PM"),
        )
    }

    private fun isRainyHour(hour: HourlyData): Boolean =
        hour.precipProbability >= 40 ||
            hour.weatherCode in heavyRainCodes ||
            hour.weatherCode in drizzleCodes
}
