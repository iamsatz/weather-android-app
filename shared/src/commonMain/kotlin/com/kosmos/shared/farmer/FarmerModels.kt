package com.kosmos.shared.farmer

enum class CropType(val id: String, val label: String) {
    RICE("rice", "Rice"),
    COTTON("cotton", "Cotton"),
    WHEAT("wheat", "Wheat"),
    CHILI("chili", "Chili"),
    SUGARCANE("sugarcane", "Sugarcane"),
    MAIZE("maize", "Maize"),
    TURMERIC("turmeric", "Turmeric"),
    ONION("onion", "Onion"),
    ;

    companion object {
        fun fromId(id: String): CropType? = entries.firstOrNull { it.id == id }
    }
}

data class FarmerProfile(
    val crops: Set<String> = emptySet(),
    val sowingDateIso: String? = null,
    val plotLat: Double? = null,
    val plotLon: Double? = null,
    val plotCity: String? = null,
    val languageCode: String = "te",
) {
    val isComplete: Boolean
        get() = crops.isNotEmpty() && plotLat != null && plotLon != null
}

data class SoilData(
    val moisture: Double?,
    val temperature: Double?,
)

object MonsoonCalendar {
    fun daysUntilOnset(state: String, month: Int, day: Int): Int? {
        val onset = stateOnsetDays[state] ?: return null
        val today = month * 31 + day
        val onsetDay = onset.first * 31 + onset.second
        return if (onsetDay >= today) onsetDay - today else null
    }

    fun onsetLabel(state: String): String? = stateOnsetLabels[state]

    private val stateOnsetLabels = mapOf(
        "Telangana" to "Jun 10",
        "Andhra Pradesh" to "Jun 5",
        "Karnataka" to "Jun 5",
        "Tamil Nadu" to "Jun 1",
        "Kerala" to "Jun 1",
        "Maharashtra" to "Jun 10",
        "Rajasthan" to "Jul 1",
        "West Bengal" to "Jun 10",
    )

    private val stateOnsetDays = mapOf(
        "Telangana" to (6 to 10),
        "Andhra Pradesh" to (6 to 5),
        "Karnataka" to (6 to 5),
        "Tamil Nadu" to (6 to 1),
        "Kerala" to (6 to 1),
        "Maharashtra" to (6 to 10),
        "Rajasthan" to (7 to 1),
        "West Bengal" to (6 to 10),
    )
}
