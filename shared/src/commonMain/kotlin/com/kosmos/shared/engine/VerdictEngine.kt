package com.kosmos.shared.engine

import com.kosmos.shared.models.HourlyData
import com.kosmos.shared.models.TimeWindow
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot
import com.kosmos.shared.models.upcomingHours

object VerdictEngine {

    private val heavyRainCodes = setOf(65, 67, 75, 82, 95, 96, 99)
    private val drizzleCodes = setOf(51, 53, 55, 61, 63, 80, 81)

    fun evaluate(
        snapshot: WeatherSnapshot,
        disabledBaseIds: Set<String> = emptySet(),
    ): List<Verdict> {
        val next12 = snapshot.upcomingHours(12)
        val verdicts = mutableListOf<Verdict>()

        verdicts += rainVerdicts(next12, disabledBaseIds)
        windVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        verdicts += heatVerdicts(snapshot, disabledBaseIds)
        sunProtectionVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        coldVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        airVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        coolerTomorrowVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        verdicts += vitaminDVerdicts(snapshot, disabledBaseIds)
        verdicts += bestWalkVerdicts(snapshot, disabledBaseIds)
        avoidHoursVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        canJogVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        hydrationVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        mosquitoVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        openWindowsVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        closeWindowsVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        laundryVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        pleasantDayVerdict(snapshot, disabledBaseIds)?.let { verdicts += it }
        verdicts += goldenHourVerdicts(snapshot, disabledBaseIds)

        if (verdicts.isEmpty() && !disabledBaseIds.contains("easy")) {
            verdicts += easyDayVerdict(snapshot)
        }

        return verdicts.filter { verdict ->
            val baseId = verdict.id.substringBefore(".")
            baseId !in disabledBaseIds
        }.sortedBy { it.priority.ordinal }
    }

    fun filterForDisplay(verdicts: List<Verdict>): List<Verdict> {
        val hasSevereOrAction = verdicts.any {
            it.priority == VerdictPriority.SEVERE || it.priority == VerdictPriority.ACTION
        }
        val filtered = if (hasSevereOrAction) {
            verdicts.filter { it.priority != VerdictPriority.NORMAL }
        } else {
            verdicts
        }
        return filtered.ifEmpty { verdicts.filter { it.id == "easy" }.ifEmpty { verdicts.take(1) } }
    }

    fun sortChronologically(verdicts: List<Verdict>): List<Verdict> =
        verdicts.sortedWith(
            compareBy(
                { verdict ->
                    when {
                        verdict.id.startsWith("imd.") -> 0
                        verdict.timeWindow == null && verdict.priority == VerdictPriority.SEVERE -> 1
                        verdict.timeWindow != null -> 2
                        else -> 3
                    }
                },
                { it.timeWindow?.startHour ?: Int.MAX_VALUE },
                { it.priority.ordinal },
            ),
        )

    private fun rainVerdicts(next12: List<HourlyData>, disabled: Set<String>): List<Verdict> {
        if ("raincoat" in disabled && "umbrella" in disabled) return emptyList()

        val maxPrecip = next12.maxOfOrNull { it.precipProbability } ?: 0
        val hasHeavyCode = next12.any { it.weatherCode in heavyRainCodes }
        val hasDrizzleCode = next12.any { it.weatherCode in drizzleCodes }

        if (maxPrecip >= 70 || hasHeavyCode) {
            if ("raincoat" in disabled) return emptyList()
            val window = contiguousPrecipWindow(next12, 70) ?: peakHourWindow(next12)
            return listOf(
                Verdict(
                    id = "raincoat",
                    emoji = "☔",
                    title = "Take a raincoat",
                    detail = "$maxPrecip% rain · ${window.label} — carry it before you head out",
                    priority = VerdictPriority.SEVERE,
                    accentColor = 0xFF1A5CB3,
                    timeWindow = window,
                ),
            )
        }

        if (maxPrecip in 40..69 || hasDrizzleCode) {
            if ("umbrella" in disabled) return emptyList()
            val window = contiguousPrecipWindow(next12, 40) ?: peakHourWindow(next12)
            return listOf(
                Verdict(
                    id = "umbrella",
                    emoji = "☂",
                    title = "Take an umbrella",
                    detail = "$maxPrecip% rain · ${window.label}",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3F8CD9,
                    timeWindow = window,
                ),
            )
        }

        if (maxPrecip in 30..39 && "umbrella" !in disabled) {
            val peak = next12.maxByOrNull { it.precipProbability } ?: return emptyList()
            return listOf(
                Verdict(
                    id = "umbrella.light",
                    emoji = "🌂",
                    title = "Light chance of rain",
                    detail = "${peak.precipProbability}% around ${peak.label}. Carry one if you'll be out long.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF5AA0D9,
                    timeWindow = TimeWindow(peak.hour, peak.hour, peak.label),
                ),
            )
        }

        return emptyList()
    }

    private fun windVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("wind" in disabled) return null
        val next12 = snapshot.upcomingHours(12)
        val maxGust = next12.maxOfOrNull { it.windGustKmh } ?: snapshot.windGustKmh
        if (maxGust < 35) return null
        val gustHour = next12.maxByOrNull { it.windGustKmh }?.hour ?: snapshot.currentHour
        return Verdict(
            id = "wind",
            emoji = "💨",
            title = "Gusty — hold umbrellas and spray",
            detail = "Gusts to ${maxGust.toInt()} km/h around ${PlainLanguage.formatHourLabel(gustHour)} — secure loose items",
            priority = if (maxGust >= 50) VerdictPriority.SEVERE else VerdictPriority.ACTION,
            accentColor = 0xFF5A8C9E,
            timeWindow = TimeWindow(gustHour, gustHour + 2, PlainLanguage.formatHourLabel(gustHour)),
        )
    }

    private fun heatVerdicts(snapshot: WeatherSnapshot, disabled: Set<String>): List<Verdict> {
        if ("heat" in disabled) return emptyList()
        val feels = snapshot.feelsLike
        return when {
            feels >= 38 -> listOf(
                Verdict(
                    id = "heat",
                    emoji = "🔥",
                    title = "Brutal heat — protect yourself",
                    detail = "Umbrella for shade + sunscreen SPF 30+ · Drink 3L water · Avoid 11 AM–4 PM sun",
                    priority = VerdictPriority.SEVERE,
                    accentColor = 0xFFC74D33,
                ),
            )
            feels >= 35 -> listOf(
                Verdict(
                    id = "heat",
                    emoji = "☀",
                    title = "Hot day — stay hydrated",
                    detail = "Feels ${feels.toInt()}°. Carry water, light clothes. Sunscreen + hat if going out.",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFFE68C33,
                ),
            )
            else -> emptyList()
        }
    }

    private fun sunProtectionVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("sunProtection" in disabled) return null
        if (snapshot.uvIndex >= 8 && snapshot.feelsLike < 35 && snapshot.isDay) {
            return Verdict(
                id = "sunProtection",
                emoji = "🧴",
                title = "Sun is strong — protect your skin",
                detail = "Sunscreen SPF 30+, sun hat or umbrella for shade. UV is ${snapshot.uvIndex.toInt()}.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD98C33,
            )
        }
        return null
    }

    private fun coldVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("cold" in disabled) return null
        if (snapshot.temp < 10) {
            return Verdict(
                id = "cold",
                emoji = "🧥",
                title = "Bundle up — chilly day",
                detail = "Feels like ${snapshot.feelsLike.toInt()}°.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF5A8CD9,
            )
        }
        return null
    }

    private fun airVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("air" in disabled) return null
        val aqi = snapshot.aqi ?: return null
        return when {
            aqi >= 200 -> Verdict(
                id = "air",
                emoji = "😷",
                title = "Air is hazardous — stay indoors today",
                detail = "Outdoor air is unsafe. Keep windows shut and run a purifier if you have one.",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFFE64D4D,
            )
            aqi >= 150 -> Verdict(
                id = "air",
                emoji = "😷",
                title = "Air is bad — wear a mask outside",
                detail = "Skip the jog. Mask up if you must go out.",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFFE64D4D,
            )
            aqi >= 100 -> Verdict(
                id = "air",
                emoji = "🌫",
                title = "Air is rough — skip the jog",
                detail = "Outdoor exercise isn't ideal today.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFE68C33,
            )
            aqi >= 50 -> Verdict(
                id = "air",
                emoji = "●",
                title = "Air is okay",
                detail = "Fine for most outdoor activity.",
                priority = VerdictPriority.NORMAL,
                accentColor = 0xFFE6C033,
            )
            else -> Verdict(
                id = "air",
                emoji = "●",
                title = "Air is excellent",
                detail = "Clean air today — great for being outside.",
                priority = VerdictPriority.NORMAL,
                accentColor = 0xFF4DC85A,
            )
        }
    }

    private fun coolerTomorrowVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("coolerTomorrow" in disabled) return null
        val tomorrow = snapshot.daily.getOrNull(1) ?: return null
        val drop = snapshot.high - tomorrow.high
        if (drop >= 6) {
            return Verdict(
                id = "coolerTomorrow",
                emoji = "🧥",
                title = "Tomorrow's much cooler",
                detail = "Dropping ~${drop.toInt()}°. Layer up — sniffles weather.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF5A8CD9,
            )
        }
        return null
    }

    private fun vitaminDVerdicts(snapshot: WeatherSnapshot, disabled: Set<String>): List<Verdict> {
        if ("vitaminD" in disabled) return emptyList()
        val verdicts = mutableListOf<Verdict>()

        findVitaminDWindow(snapshot, 8..11, "vitaminD.morning")?.let { verdicts += it }
        findVitaminDWindow(snapshot, 15..17, "vitaminD.afternoon")?.let { verdicts += it }

        return verdicts
    }

    private fun findVitaminDWindow(
        snapshot: WeatherSnapshot,
        hourRange: IntRange,
        id: String,
    ): Verdict? {
        val qualifying = snapshot.hourly.filter { hour ->
            hour.hour in hourRange &&
                hour.uvIndex in 2.0..7.0 &&
                hour.precipProbability < 50 &&
                hour.weatherCode !in heavyRainCodes &&
                hour.weatherCode !in drizzleCodes
        }
        if (qualifying.isEmpty()) return null

        val qualifyingHours = qualifying.map { it.hour }.toSet()
        val contiguous = mutableListOf<HourlyData>()
        var bestRange: List<HourlyData> = emptyList()
        for (hour in snapshot.hourly.filter { it.hour in hourRange }) {
            if (hour.hour in qualifyingHours) {
                contiguous += hour
            } else if (contiguous.isNotEmpty()) {
                if (contiguous.size > bestRange.size) bestRange = contiguous.toList()
                contiguous.clear()
            }
        }
        if (contiguous.size > bestRange.size) bestRange = contiguous

        val range = bestRange.ifEmpty { listOf(qualifying.first()) }
        val startHour = range.first().hour
        val endHour = range.last().hour
        val startLabel = PlainLanguage.formatHourLabel(startHour)
        val endLabel = PlainLanguage.formatHourLabel(endHour)
        val rangeLabel = if (startHour == endHour) startLabel else "$startLabel–$endLabel"

        return Verdict(
            id = id,
            emoji = "🌤",
            title = if (endHour < snapshot.currentHour) {
                "Vitamin D window was $rangeLabel"
            } else {
                "Vitamin D window: $rangeLabel"
            },
            detail = "15 minutes is enough · UV ${range.map { it.uvIndex.toInt() }.average().toInt()} in this window",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFFD98C33,
            timeWindow = TimeWindow(startHour, endHour, rangeLabel),
        )
    }

    private fun bestWalkVerdicts(snapshot: WeatherSnapshot, disabled: Set<String>): List<Verdict> {
        if ("bestWalk" in disabled) return emptyList()
        val verdicts = mutableListOf<Verdict>()

        findBestWalkWindow(snapshot, 5..9, "bestWalk.morning")?.let { verdicts += it }
        findBestWalkWindow(snapshot, 16..20, "bestWalk.evening")?.let { verdicts += it }

        return verdicts
    }

    private fun comfortScore(hour: HourlyData): Double {
        val heatPenalty = maxOf(0.0, hour.temp - 26) * 1.5
        val coldPenalty = maxOf(0.0, 18 - hour.temp) * 1.2
        val uvPenalty = maxOf(0.0, hour.uvIndex - 4) * 1.8
        val windPenalty = maxOf(0.0, hour.windSpeedKmh - 15) * 0.8 + maxOf(0.0, hour.windGustKmh - 25) * 1.2
        return heatPenalty + coldPenalty + uvPenalty + windPenalty
    }

    private fun isRainyHour(hour: HourlyData): Boolean =
        hour.precipProbability >= 40 ||
            hour.weatherCode in heavyRainCodes ||
            hour.weatherCode in drizzleCodes

    private fun findBestWalkWindow(
        snapshot: WeatherSnapshot,
        hourRange: IntRange,
        id: String,
    ): Verdict? {
        val aqi = snapshot.aqi ?: 50
        if (aqi >= 100) return null

        val candidates = snapshot.upcomingHours()
            .filter { it.hour in hourRange && !isRainyHour(it) }
            .filter { it.temp in 18.0..32.0 }
            .filter { it.windSpeedKmh < 25 && it.windGustKmh < 35 }
        if (candidates.isEmpty()) return null

        val best = candidates.minByOrNull { comfortScore(it) } ?: return null
        val endHour = best.hour + 1
        val timeLabel = if (endHour > hourRange.last) {
            "around ${PlainLanguage.formatHourLabel(best.hour)}"
        } else {
            PlainLanguage.formatTimeRange(best.hour, endHour)
        }

        return Verdict(
            id = id,
            emoji = "🚶",
            title = "Best walk: $timeLabel",
            detail = "~${best.temp.toInt()}°, gentlest air",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF3FA67F,
            timeWindow = TimeWindow(
                best.hour,
                if (endHour > hourRange.last) best.hour else endHour,
                timeLabel,
            ),
        )
    }

    private fun avoidHoursVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("avoidHours" in disabled) return null
        val badHours = snapshot.upcomingHours()
            .filter { it.hour <= 16 }
            .filter { it.temp >= 36 || it.uvIndex >= 9 }
        if (badHours.isEmpty()) return null

        val start = badHours.minOf { it.hour }.coerceAtLeast(11)
        val end = badHours.maxOf { it.hour }.coerceAtMost(16)

        return Verdict(
            id = "avoidHours",
            emoji = "🔥",
            title = "Avoid outdoors ${PlainLanguage.formatHourLabel(start)}–${PlainLanguage.formatHourLabel(end)}",
            detail = "${snapshot.feelsLike.toInt()}° + climbing. Plan errands before or after.",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFFC74D33,
            timeWindow = TimeWindow(start, end, PlainLanguage.formatTimeRange(start, end)),
        )
    }

    private fun canJogVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("canJog" in disabled) return null
        val aqi = snapshot.aqi ?: return null
        if (snapshot.feelsLike >= 32 || aqi >= 100) return null
        if (snapshot.upcomingHours().any { isRainyHour(it) }) return null

        val best = snapshot.upcomingHours()
            .filter { it.hour in 5..9 && !isRainyHour(it) }
            .minByOrNull { it.temp } ?: return null

        return Verdict(
            id = "canJog",
            emoji = "🏃",
            title = "Good day to jog",
            detail = "Best window: ${PlainLanguage.formatHourLabel(best.hour)} at ~${best.temp.toInt()}°.",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF3FA67F,
        )
    }

    private fun hydrationVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("hydration" in disabled) return null
        if (snapshot.feelsLike in 26.0..34.9) {
            return Verdict(
                id = "hydration",
                emoji = "💧",
                title = "Drink 3L water today",
                detail = "Hot. Keep a bottle close, sip every hour.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3F8CD9,
            )
        }
        return null
    }

    private fun mosquitoVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("mosquito" in disabled) return null
        val hour = snapshot.currentHour
        if (hour !in 17..21) return null
        if (snapshot.temp !in 20.0..32.0) return null
        if (snapshot.humidity < 60) return null

        val maxPrecipToday = snapshot.hourly.maxOfOrNull { it.precipProbability } ?: 0
        if (maxPrecipToday < 40) return null

        return Verdict(
            id = "mosquito",
            emoji = "🦟",
            title = "Mosquito hour — cover up",
            detail = "Sleeves, repellent, screens. Peak biting till 9 PM.",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF8C4D80,
            timeWindow = TimeWindow(17, 21, "5–9 PM"),
        )
    }

    private fun openWindowsVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("openWindows" in disabled) return null
        val aqi = snapshot.aqi ?: return null
        if (snapshot.temp in 18.0..26.0 && aqi < 80 && !isRainyHour(snapshot.hourly.firstOrNull { it.isNow } ?: return null)) {
            return Verdict(
                id = "openWindows",
                emoji = "🪟",
                title = "Open the windows",
                detail = "Outdoor is cool + clean. Let fresh air in for an hour.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3FA67F,
            )
        }
        return null
    }

    private fun closeWindowsVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("closeWindows" in disabled) return null
        val aqi = snapshot.aqi
        return when {
            aqi != null && aqi >= 120 -> Verdict(
                id = "closeWindows",
                emoji = "🪟",
                title = "Keep windows shut",
                detail = "Air outside is rough. Run an air purifier if you have one.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFE68C33,
            )
            snapshot.temp >= 32 -> Verdict(
                id = "closeWindows",
                emoji = "🪟",
                title = "Keep windows shut",
                detail = "Outdoors is ${snapshot.temp.toInt()}° + climbing. Draw curtains, run AC.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFE68C33,
            )
            else -> null
        }
    }

    private fun laundryVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("laundry" in disabled) return null
        val next8 = snapshot.upcomingHours(8)
        val maxPrecip = next8.maxOfOrNull { it.precipProbability } ?: 100
        val sunnyHours = next8.count { it.uvIndex >= 2 && it.isDay }
        if (snapshot.humidity < 75 && maxPrecip < 35 && sunnyHours >= 2) {
            return Verdict(
                id = "laundry",
                emoji = "👕",
                title = "Good drying weather",
                detail = "Hang clothes out today — dry air, little rain expected. ~$sunnyHours hours of sun.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF5AA0D9,
            )
        }
        return null
    }

    private fun pleasantDayVerdict(snapshot: WeatherSnapshot, disabled: Set<String>): Verdict? {
        if ("pleasant" in disabled) return null
        if (snapshot.feelsLike > 34 || snapshot.feelsLike < 16) return null
        val next6 = snapshot.upcomingHours(6)
        if (next6.any { it.precipProbability >= 50 || it.weatherCode in heavyRainCodes }) return null
        return Verdict(
            id = "pleasant",
            emoji = "🌿",
            title = "Pleasant day outside",
            detail = "Comfortable air around ${snapshot.temp.toInt()}° — good for errands and short walks.",
            priority = VerdictPriority.NORMAL,
            accentColor = 0xFF3FA67F,
        )
    }

    private fun goldenHourVerdicts(snapshot: WeatherSnapshot, disabled: Set<String>): List<Verdict> {
        if ("goldenHour" in disabled) return emptyList()
        val verdicts = mutableListOf<Verdict>()

        val sunriseMinutes = snapshot.sunriseHour * 60 + snapshot.sunriseMinute
        val sunsetMinutes = snapshot.sunsetHour * 60 + snapshot.sunsetMinute
        val nowMinutes = snapshot.currentHour * 60

        val sunriseWindowStart = sunriseMinutes - 15
        val sunriseWindowEnd = sunriseMinutes + 60
        if (nowMinutes in sunriseWindowStart..sunriseWindowEnd) {
            val label = PlainLanguage.formatPreciseTime(snapshot.sunriseHour, snapshot.sunriseMinute)
            verdicts += Verdict(
                id = "goldenHour.sunrise",
                emoji = "🌅",
                title = "Golden hour (sunrise): $label",
                detail = "Best light for photos — 60 min after sunrise",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFF28C4D,
                timeWindow = TimeWindow(snapshot.sunriseHour, snapshot.sunriseHour + 1, label),
            )
        }

        val sunsetWindowStart = sunsetMinutes - 60
        val sunsetWindowEnd = sunsetMinutes + 15
        if (nowMinutes in sunsetWindowStart..sunsetWindowEnd && nowMinutes <= sunsetMinutes + 720) {
            val label = PlainLanguage.formatPreciseTime(snapshot.sunsetHour, snapshot.sunsetMinute)
            verdicts += Verdict(
                id = "goldenHour.sunset",
                emoji = "🌇",
                title = "Golden hour: $label",
                detail = "Best light for photos — 60 min before sunset",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFF28C4D,
                timeWindow = TimeWindow(snapshot.sunsetHour, snapshot.sunsetHour, label),
            )
        }

        return verdicts
    }

    private fun easyDayVerdict(snapshot: WeatherSnapshot): Verdict {
        val condition = PlainLanguage.weatherConditionLabel(snapshot.weatherCode)
        return Verdict(
            id = "easy",
            emoji = "✨",
            title = "Easy day — go about your business",
            detail = "${snapshot.temp.toInt()}° · ${condition.lowercase()}",
            priority = VerdictPriority.NORMAL,
            accentColor = 0xFF3FA67F,
        )
    }

    private fun contiguousPrecipWindow(hours: List<HourlyData>, threshold: Int): TimeWindow? {
        val rainy = hours.filter { it.precipProbability >= threshold }
        if (rainy.isEmpty()) return null
        val start = rainy.minOf { it.hour }
        val end = rainy.maxOf { it.hour }
        return TimeWindow(start, end, PlainLanguage.formatTimeRange(start, end))
    }

    private fun peakHourWindow(hours: List<HourlyData>): TimeWindow {
        val peak = hours.maxByOrNull { it.precipProbability } ?: hours.first()
        return TimeWindow(peak.hour, peak.hour + 1, peak.label)
    }
}
