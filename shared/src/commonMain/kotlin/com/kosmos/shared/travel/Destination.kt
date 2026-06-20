package com.kosmos.shared.travel

enum class DestinationVibe(val id: String) {
    COOL("cool"),
    BEACH("beach"),
    WARM("warm"),
    ;

    companion object {
        fun fromId(id: String): DestinationVibe? = entries.firstOrNull { it.id == id }
    }
}

data class Destination(
    val id: String,
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val vibe: DestinationVibe,
    val audienceTags: Set<String>,
    val seasonNote: String,
)

data class DestinationWeather(
    val latitude: Double,
    val longitude: Double,
    val temp: Double,
    val weatherCode: Int,
    val daily: List<com.kosmos.shared.models.DailyData>,
)

data class DestinationOutlook(
    val summary: String,
    val score: Int,
)

data class DestinationResult(
    val destination: Destination,
    val distanceKm: Int,
    val weather: DestinationWeather,
    val outlook: DestinationOutlook,
    val whyGo: String? = null,
)

data class TravelFilters(
    val vibes: Set<String> = setOf("any"),
    val audiences: Set<String> = setOf("all"),
    val distanceMaxKm: Int? = 100,
    val region: String = "All India",
    val tripDayOffset: Int = 0,
    val groupSize: String = "2",
    val transport: String = "car",
) {
    companion object {
        val distancePresets = listOf(10, 50, 100, 500, 800, 1000)
        val tripWhenOptions = listOf(
            0 to "Today",
            1 to "Tomorrow",
            3 to "In 3 days",
            7 to "Next week",
            14 to "In 2 weeks",
        )
        val groupOptions = listOf(
            "solo" to "Just me",
            "2" to "2 people",
            "3-5" to "3–5 people",
            "6+" to "6+ people",
        )
        val transportOptions = listOf(
            "car" to "Car / bike",
            "bus" to "Bus",
            "train" to "Train",
            "flight" to "Flight",
        )
        val regions = listOf(
            "All India",
            "Telangana",
            "Andhra Pradesh",
            "Tamil Nadu",
            "Kerala",
            "Karnataka",
            "Goa",
            "Maharashtra",
            "Uttarakhand",
            "Rajasthan",
            "West Bengal",
        )
        val vibeOptions = listOf(
            "any" to "Anything",
            "cool" to "Cooler / Hills",
            "beach" to "Beach / Sea",
        )
        val audienceOptions = listOf(
            "all" to "Everyone",
            "family" to "Family",
            "kids" to "Kids",
            "friends" to "Friends",
            "couples" to "Couples",
        )
    }
}
