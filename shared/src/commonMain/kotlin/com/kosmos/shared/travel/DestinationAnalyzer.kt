package com.kosmos.shared.travel

import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.models.DailyData

object DestinationAnalyzer {

    fun analyze(
        homeTemp: Double,
        destination: Destination,
        weather: DestinationWeather,
        targetDayOffset: Int = 0,
        tripPurposes: Set<String> = setOf("any"),
    ): DestinationOutlook {
        val daily = weather.daily
        val targetDay = daily.getOrNull(targetDayOffset.coerceIn(0, daily.lastIndex.coerceAtLeast(0)))
        val targetTemp = targetDay?.high ?: weather.temp
        val delta = homeTemp - targetTemp
        val parts = mutableListOf<String>()
        var score = 50

        if (targetDayOffset >= daily.size) {
            parts.add("Based on season (${destination.seasonNote}), not live forecast")
            score += 5
        } else if (targetDay != null) {
            parts.add("For ${targetDay.dateLabel}")
        }

        when {
            delta >= 8 -> {
                parts.add("${delta.toInt()}° cooler than home — much needed relief")
                score += 30
            }
            delta >= 4 -> {
                parts.add("${delta.toInt()}° cooler than home")
                score += 20
            }
            delta <= -5 -> {
                parts.add("${(-delta).toInt()}° warmer than home")
                score -= 10
            }
            else -> {
                parts.add("Similar temps to home")
            }
        }

        val drizzleDays = daily.filter { it.precipProbabilityMax in 20..60 }
        val rainDays = daily.filter { it.precipProbabilityMax > 60 }
        val targetRain = targetDay?.precipProbabilityMax ?: 0

        if (targetDay != null && targetRain > 60) {
            parts.add("rain likely on your travel day — pack a layer")
            score -= 20
        } else if (targetDay != null && targetRain < 25) {
            score += 8
        }

        if (drizzleDays.isNotEmpty()) {
            val labels = drizzleDays.take(2).joinToString(", ") { it.dateLabel }
            parts.add("light drizzle likely $labels")
            if (destination.vibe == DestinationVibe.COOL) score += 8
        }

        if (rainDays.isNotEmpty() && destination.vibe == DestinationVibe.BEACH) {
            val labels = rainDays.take(2).joinToString(", ") { it.dateLabel }
            parts.add("rain likely $labels — pack a rain layer")
            score -= 15
        }

        val avgLow = daily.map { it.low }.average()
        if (destination.vibe == DestinationVibe.COOL && avgLow < homeTemp - 6) {
            parts.add("crisp evenings — great for walks")
            score += 10
        }

        if (destination.vibe == DestinationVibe.BEACH && delta >= 2) {
            parts.add("sea breeze should feel easier than home")
            score += 12
        }

        if ("pilgrimage" in tripPurposes) {
            val targetLow = targetDay?.low ?: weather.temp
            if (targetRain < 30) {
                parts.add("dry enough for temple visits")
                score += 10
            }
            if (targetTemp in 22.0..34.0 && targetLow >= 18.0) {
                parts.add("comfortable heat for long walks")
                score += 8
            }
            if (targetRain > 60) score -= 12
        }

        val bestDay = findBestDay(homeTemp, daily)
        val firstDayLabel = daily.firstOrNull()?.dateLabel
        if (bestDay != null && bestDay.dateLabel != firstDayLabel) {
            parts.add("sweet spot around ${bestDay.dateLabel}")
            score += 5
        }

        val condition = PlainLanguage.weatherConditionLabel(weather.weatherCode).lowercase()
        if (condition.contains("clear") || condition.contains("partly")) {
            score += 5
        }

        val summary = parts.distinct().joinToString(" · ")
        return DestinationOutlook(summary = summary.ifEmpty { "Decent getaway weather this week" }, score = score.coerceIn(0, 100))
    }

    private fun findBestDay(homeTemp: Double, daily: List<DailyData>): DailyData? {
        if (daily.isEmpty()) return null
        return daily.maxByOrNull { day ->
            var dayScore = 0
            val delta = homeTemp - day.high
            if (delta >= 5) dayScore += 20
            if (day.precipProbabilityMax < 30) dayScore += 10
            if (day.low in 18.0..26.0) dayScore += 8
            dayScore
        }
    }

    fun contextLine(homeTemp: Double, filters: TravelFilters): String {
        val whenLabel = TravelFilters.tripWhenOptions
            .firstOrNull { it.first == filters.tripDayOffset }?.second ?: "your trip day"
        val vibeHint = when {
            filters.vibes.contains("cool") && !filters.vibes.contains("any") -> "cooler hill escapes"
            filters.vibes.contains("beach") && !filters.vibes.contains("any") -> "beach and sea getaways"
            homeTemp >= 35 -> "cooler hills and breezy coasts"
            homeTemp >= 30 -> "cooler hills, beaches, and more"
            else -> "getaways near you"
        }
        return "It's ${homeTemp.toInt()}° where you are — $whenLabel · ${filters.tripDays}-day trip · $vibeHint."
    }
}
