package com.kosmos.shared.travel

import com.kosmos.shared.models.DailyData

data class PackItem(
    val emoji: String,
    val item: String,
    val reason: String,
)

object PackListEngine {

    fun generate(
        homeTemp: Double,
        destTemp: Double,
        destDaily: List<DailyData>,
        destUv: Double = 0.0,
        distanceKm: Int = 0,
    ): List<PackItem> {
        val items = mutableListOf<PackItem>()
        val delta = homeTemp - destTemp
        val maxRain = destDaily.maxOfOrNull { it.precipProbabilityMax } ?: 0
        val minLow = destDaily.minOfOrNull { it.low } ?: destTemp

        if (delta >= 5 || minLow < 18) {
            items += PackItem("🧥", "Light jacket or sweater", "Nights drop to ${minLow.toInt()}° — much cooler than home")
        }
        if (maxRain >= 40) {
            items += PackItem("☔", "Rain layer", "Rain likely on ${rainDays(destDaily)} — don't get caught without cover")
        }
        if (destTemp >= 32 || homeTemp >= 32) {
            items += PackItem("💧", "Extra water", "Heat at destination — sip every hour outdoors")
        }
        if (destUv >= 6) {
            items += PackItem("🧴", "SPF 50+", "Strong sun at destination — reapply before noon")
        }
        if (distanceKm > 300) {
            items += PackItem("🔌", "Power bank", "Long trip — keep phone charged for weather updates")
        }
        if (items.isEmpty()) {
            items += PackItem("👟", "Comfortable shoes", "Weather looks manageable — light pack is fine")
        }
        return items
    }

    private fun rainDays(daily: List<DailyData>): String =
        daily.filter { it.precipProbabilityMax >= 40 }
            .take(2)
            .joinToString(", ") { it.dateLabel }
            .ifEmpty { "the next few days" }
}
