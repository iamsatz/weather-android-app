package com.kosmos.shared.farmer

import com.kosmos.shared.models.TimeWindow
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot
import com.kosmos.shared.models.upcomingHours

object FarmerVerdictEngine {

    fun evaluate(
        snapshot: WeatherSnapshot,
        soil: SoilData,
        profile: FarmerProfile,
        region: String,
        month: Int,
        day: Int,
    ): List<Verdict> {
        val verdicts = mutableListOf<Verdict>()
        val next24 = snapshot.upcomingHours(24)
        val maxRain24 = next24.maxOfOrNull { it.precipProbability } ?: 0
        val avgHumidity = next24.map { it.humidity }.average().takeIf { it.isFinite() } ?: snapshot.humidity.toDouble()
        val maxWind = next24.maxOfOrNull { it.windSpeedKmh } ?: snapshot.windSpeedKmh
        val maxGust = next24.maxOfOrNull { it.windGustKmh } ?: snapshot.windGustKmh
        val dryDaysAhead = snapshot.daily.take(5).count { it.precipProbabilityMax < 30 }

        if (maxRain24 < 30 && maxWind < 15 && maxGust < 25) {
            verdicts += Verdict(
                id = "farmer.spray",
                emoji = "🧴",
                title = "Good spray window today",
                detail = "Low rain · wind ~${maxWind.toInt()} km/h — spray before noon while it's calm",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF4A8C4D,
            )
        } else if (maxRain24 < 30 && (maxWind >= 15 || maxGust >= 25)) {
            verdicts += Verdict(
                id = "farmer.spray.hold",
                emoji = "💨",
                title = "Hold spray — too windy",
                detail = "Gusts to ${maxGust.toInt()} km/h — drift risk, wait for calmer hours",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF8C6D33,
            )
        }

        if (soil.moisture != null && soil.moisture < 0.25 && dryDaysAhead >= 3) {
            verdicts += Verdict(
                id = "farmer.harvest",
                emoji = "🌾",
                title = "Harvest window opening",
                detail = "Soil drying · $dryDaysAhead dry days ahead — good time to plan harvest",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD4A017,
            )
        }

        soil.temperature?.let { soilTemp ->
            if (soilTemp in 18.0..25.0 && profile.crops.any { it in listOf("rice", "cotton", "maize") }) {
                verdicts += Verdict(
                    id = "farmer.sowing",
                    emoji = "🌱",
                    title = "Sowing conditions look right",
                    detail = "Soil temp ${soilTemp.toInt()}° — in the sweet spot for ${profile.crops.first()}",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF5A8C4D,
                )
            }
        }

        if (avgHumidity > 80 && snapshot.temp in 25.0..35.0) {
            val cropHint = profile.crops.firstOrNull()?.let { CropType.fromId(it)?.label } ?: "crop"
            verdicts += Verdict(
                id = "farmer.pest",
                emoji = "🐛",
                title = "Pest pressure rising",
                detail = "Humid ${avgHumidity.toInt()}° + ${snapshot.temp.toInt()}° — watch $cropHint for bollworm/blight",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFF8C4D33,
            )
        }

        if (snapshot.humidity < 40 && snapshot.temp >= 30) {
            verdicts += Verdict(
                id = "farmer.stubble",
                emoji = "🔥",
                title = "Stubble burn risk",
                detail = "Dry and warm — avoid burning toward villages; smoke hangs low",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFFC74D33,
            )
        }

        MonsoonCalendar.daysUntilOnset(region, month, day)?.let { days ->
            if (days in 1..14) {
                val label = MonsoonCalendar.onsetLabel(region) ?: "soon"
                verdicts += Verdict(
                    id = "farmer.monsoon",
                    emoji = "🌧",
                    title = "Monsoon ~$days days out",
                    detail = "IMD onset around $label — prep drainage and seed stock",
                    priority = VerdictPriority.ACTION,
                    accentColor = 0xFF3F8CD9,
                    timeWindow = TimeWindow(0, 24, "$days days"),
                )
            }
        }

        if (verdicts.isEmpty()) {
            verdicts += Verdict(
                id = "farmer.calm",
                emoji = "🌤",
                title = "Field day looks steady",
                detail = "No urgent crop calls — routine work is fine",
                priority = VerdictPriority.NORMAL,
                accentColor = 0xFF5A8C4D,
            )
        }

        return verdicts.sortedBy { it.priority.ordinal }
    }
}
