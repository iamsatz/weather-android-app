package com.kosmos.shared.travel

object DestinationCatalog {

    val all: List<Destination> = listOf(
        Destination("pocharam", "Pocharam Lake", "Telangana", 17.884, 78.472, DestinationVibe.WARM, setOf("family", "kids", "friends"), "Best Nov–Feb"),
        Destination("ananthagiri", "Ananthagiri Hills", "Telangana", 17.372, 78.067, DestinationVibe.COOL, setOf("friends", "couples", "family"), "Year-round"),
        Destination("nagarjuna", "Nagarjuna Sagar", "Telangana", 16.574, 79.312, DestinationVibe.WARM, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("vijayawada", "Vijayawada", "Andhra Pradesh", 16.506, 80.648, DestinationVibe.WARM, setOf("family", "friends"), "Best Nov–Feb"),
        Destination("hampi", "Hampi", "Karnataka", 15.335, 76.460, DestinationVibe.WARM, setOf("friends", "couples"), "Best Oct–Feb"),
        Destination("coorg", "Coorg", "Karnataka", 12.424, 75.738, DestinationVibe.COOL, setOf("couples", "friends", "family"), "Best Oct–Mar"),
        Destination("araku", "Araku Valley", "Andhra Pradesh", 18.327, 82.878, DestinationVibe.COOL, setOf("family", "couples", "friends"), "Year-round"),
        Destination("vizag", "Vizag · RK Beach", "Andhra Pradesh", 17.706, 83.320, DestinationVibe.BEACH, setOf("family", "kids", "friends"), "Best Oct–Mar"),
        Destination("gokarna", "Gokarna", "Karnataka", 14.547, 74.319, DestinationVibe.BEACH, setOf("friends", "couples"), "Best Oct–Mar"),
        Destination("ooty", "Ooty", "Tamil Nadu", 11.410, 76.695, DestinationVibe.COOL, setOf("family", "kids", "couples"), "Year-round"),
        Destination("mahabalipuram", "Mahabalipuram", "Tamil Nadu", 12.617, 80.193, DestinationVibe.BEACH, setOf("family", "kids"), "Best Oct–Mar"),
        Destination("wayanad", "Wayanad", "Kerala", 11.652, 76.131, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–May"),
        Destination("munnar", "Munnar", "Kerala", 10.089, 77.060, DestinationVibe.COOL, setOf("couples", "family"), "Best Sep–Mar"),
        Destination("goa", "Goa", "Goa", 15.299, 74.124, DestinationVibe.BEACH, setOf("friends", "couples", "family"), "Best Nov–Feb"),
        Destination("mussoorie", "Mussoorie", "Uttarakhand", 30.459, 78.066, DestinationVibe.COOL, setOf("family", "couples"), "Best Mar–Jun"),
        Destination("lonavala", "Lonavala", "Maharashtra", 18.754, 73.406, DestinationVibe.COOL, setOf("friends", "couples", "family"), "Best Jun–Feb"),
        Destination("mahabaleshwar", "Mahabaleshwar", "Maharashtra", 17.925, 73.658, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–Jun"),
        Destination("pondicherry", "Pondicherry", "Tamil Nadu", 11.941, 79.808, DestinationVibe.BEACH, setOf("couples", "friends"), "Best Oct–Mar"),
        Destination("kodaikanal", "Kodaikanal", "Tamil Nadu", 10.238, 77.489, DestinationVibe.COOL, setOf("family", "couples"), "Year-round"),
        Destination("shimla", "Shimla", "Uttarakhand", 31.104, 77.173, DestinationVibe.COOL, setOf("family", "couples"), "Best Mar–Jun"),
        Destination("manali", "Manali", "Uttarakhand", 32.244, 77.189, DestinationVibe.COOL, setOf("friends", "couples"), "Best Mar–Jun"),
        Destination("rishikesh", "Rishikesh", "Uttarakhand", 30.086, 78.268, DestinationVibe.COOL, setOf("friends", "family"), "Best Sep–Apr"),
        Destination("jaipur", "Jaipur", "Rajasthan", 26.912, 75.787, DestinationVibe.WARM, setOf("family", "couples"), "Best Oct–Mar"),
        Destination("udaipur", "Udaipur", "Rajasthan", 24.585, 73.713, DestinationVibe.WARM, setOf("couples", "family"), "Best Oct–Mar"),
        Destination("kochi", "Kochi", "Kerala", 9.931, 76.267, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("alleppey", "Alleppey", "Kerala", 9.498, 76.339, DestinationVibe.BEACH, setOf("couples", "family"), "Best Oct–Mar"),
        Destination("kanyakumari", "Kanyakumari", "Tamil Nadu", 8.088, 77.538, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("darjeeling", "Darjeeling", "West Bengal", 27.041, 88.266, DestinationVibe.COOL, setOf("couples", "family"), "Best Mar–May"),
        Destination("srisailam", "Srisailam", "Telangana", 16.074, 78.869, DestinationVibe.COOL, setOf("family", "friends"), "Best Oct–Feb"),
        Destination("papikondalu", "Papikondalu", "Andhra Pradesh", 17.250, 81.680, DestinationVibe.COOL, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("lambasingi", "Lambasingi", "Andhra Pradesh", 18.102, 83.020, DestinationVibe.COOL, setOf("couples", "friends"), "Best Nov–Feb"),
        Destination("yercaud", "Yercaud", "Tamil Nadu", 11.775, 78.209, DestinationVibe.COOL, setOf("family", "couples"), "Year-round"),
        Destination("kodaikanal2", "Kodaikanal Lake", "Tamil Nadu", 10.238, 77.489, DestinationVibe.COOL, setOf("family"), "Year-round"),
        Destination("chikmagalur", "Chikmagalur", "Karnataka", 13.316, 75.774, DestinationVibe.COOL, setOf("friends", "couples"), "Best Sep–Mar"),
        Destination("sakleshpur", "Sakleshpur", "Karnataka", 12.944, 75.784, DestinationVibe.COOL, setOf("friends", "couples"), "Best Oct–Feb"),
        Destination("agumbe", "Agumbe", "Karnataka", 13.512, 75.096, DestinationVibe.COOL, setOf("friends"), "Best Oct–Feb"),
        Destination("dandeli", "Dandeli", "Karnataka", 15.266, 74.618, DestinationVibe.COOL, setOf("friends", "family"), "Best Oct–May"),
        Destination("kabini", "Kabini", "Karnataka", 11.987, 76.336, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–May"),
        Destination("varkala", "Varkala", "Kerala", 8.737, 76.716, DestinationVibe.BEACH, setOf("friends", "couples"), "Best Oct–Mar"),
        Destination("kovalam", "Kovalam", "Kerala", 8.366, 76.978, DestinationVibe.BEACH, setOf("family", "couples"), "Best Oct–Mar"),
        Destination("thekkady", "Thekkady", "Kerala", 9.603, 77.161, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–Mar"),
        Destination("athirapally", "Athirapally", "Kerala", 10.285, 76.569, DestinationVibe.COOL, setOf("family", "friends"), "Best Jun–Jan"),
        Destination("tarkarli", "Tarkarli", "Maharashtra", 16.012, 73.480, DestinationVibe.BEACH, setOf("friends", "couples"), "Best Oct–Mar"),
        Destination("alibag", "Alibag", "Maharashtra", 18.641, 72.872, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("matheran", "Matheran", "Maharashtra", 18.986, 73.265, DestinationVibe.COOL, setOf("couples", "family"), "Best Oct–Jun"),
        Destination("panchgani", "Panchgani", "Maharashtra", 17.924, 73.800, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–Jun"),
        Destination("saputara", "Saputara", "Maharashtra", 20.580, 73.750, DestinationVibe.COOL, setOf("family", "kids"), "Best Oct–Mar"),
        Destination("mountabu", "Mount Abu", "Rajasthan", 24.593, 72.718, DestinationVibe.COOL, setOf("family", "couples"), "Best Oct–Mar"),
        Destination("pushkar", "Pushkar", "Rajasthan", 26.489, 74.551, DestinationVibe.WARM, setOf("friends", "couples"), "Best Oct–Mar"),
        Destination("jodhpur", "Jodhpur", "Rajasthan", 26.238, 73.024, DestinationVibe.WARM, setOf("family", "couples"), "Best Oct–Mar"),
        Destination("nainital", "Nainital", "Uttarakhand", 29.380, 79.463, DestinationVibe.COOL, setOf("family", "couples"), "Best Mar–Jun"),
        Destination("auli", "Auli", "Uttarakhand", 30.537, 79.566, DestinationVibe.COOL, setOf("friends", "couples"), "Best Dec–Mar"),
        Destination("spiti", "Spiti Valley", "Uttarakhand", 32.246, 78.035, DestinationVibe.COOL, setOf("friends"), "Best May–Oct"),
        Destination("gangtok", "Gangtok", "West Bengal", 27.338, 88.606, DestinationVibe.COOL, setOf("family", "couples"), "Best Mar–May"),
        Destination("kalimpong", "Kalimpong", "West Bengal", 27.070, 88.474, DestinationVibe.COOL, setOf("couples", "family"), "Best Mar–May"),
        Destination("puri", "Puri", "West Bengal", 19.813, 85.831, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("konark", "Konark", "West Bengal", 19.887, 86.094, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("tawang", "Tawang", "West Bengal", 27.586, 91.866, DestinationVibe.COOL, setOf("friends", "couples"), "Best Mar–Oct"),
        Destination("andaman", "Port Blair", "Andaman", 11.623, 92.726, DestinationVibe.BEACH, setOf("couples", "family"), "Best Oct–May"),
        Destination("lakshadweep", "Agatti", "Lakshadweep", 10.823, 72.194, DestinationVibe.BEACH, setOf("couples", "friends"), "Best Oct–May"),
        Destination("diu", "Diu", "Goa", 20.714, 70.987, DestinationVibe.BEACH, setOf("friends", "couples"), "Best Oct–Mar"),
        Destination("daman", "Daman", "Maharashtra", 20.397, 72.832, DestinationVibe.BEACH, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("sundarbans", "Sundarbans", "West Bengal", 21.949, 88.924, DestinationVibe.COOL, setOf("family", "friends"), "Best Nov–Feb"),
        Destination("kutch", "Rann of Kutch", "Rajasthan", 23.733, 69.859, DestinationVibe.WARM, setOf("family", "friends"), "Best Nov–Feb"),
        Destination("statueunity", "Statue of Unity", "Maharashtra", 21.838, 73.719, DestinationVibe.WARM, setOf("family", "kids"), "Best Oct–Mar"),
        Destination("srisailam2", "Srisailam Hills", "Telangana", 16.072, 78.868, DestinationVibe.COOL, setOf("family"), "Best Oct–Feb"),
        Destination("warangal", "Warangal", "Telangana", 17.978, 79.594, DestinationVibe.WARM, setOf("family", "friends"), "Best Oct–Feb"),
        Destination("bidar", "Bidar", "Karnataka", 17.910, 77.520, DestinationVibe.WARM, setOf("family", "friends"), "Best Oct–Feb"),
        Destination("badami", "Badami", "Karnataka", 15.915, 75.676, DestinationVibe.WARM, setOf("friends", "couples"), "Best Oct–Feb"),
        Destination("pachmarhi", "Pachmarhi", "Maharashtra", 22.467, 78.433, DestinationVibe.COOL, setOf("family", "friends"), "Best Oct–Jun"),
        Destination("jabalpur", "Bhedaghat", "Maharashtra", 23.132, 79.802, DestinationVibe.COOL, setOf("family", "friends"), "Best Oct–Mar"),
        Destination("sikkim", "Pelling", "West Bengal", 27.300, 88.240, DestinationVibe.COOL, setOf("couples", "family"), "Best Mar–May"),
    )

    fun matchByQuery(query: String): List<Destination> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val lower = trimmed.lowercase()
        return all.filter { dest ->
            dest.name.lowercase().startsWith(lower) ||
                dest.region.lowercase().startsWith(lower) ||
                (lower.length >= 4 && dest.name.lowercase().contains(lower))
        }
    }

    fun filter(
        fromLat: Double,
        fromLon: Double,
        filters: TravelFilters,
        distanceFn: (Double, Double, Double, Double) -> Double,
    ): List<Pair<Destination, Int>> {
        val maxKm = filters.distanceMaxKm ?: Int.MAX_VALUE

        val baseList = when {
            filters.destinationLat != null && filters.destinationLon != null -> {
                val lat = filters.destinationLat
                val lon = filters.destinationLon
                val label = filters.destinationLabel.ifBlank { filters.destinationQuery }.ifBlank { "Your pick" }
                val vibe = when {
                    filters.vibes.contains("cool") -> DestinationVibe.COOL
                    filters.vibes.contains("beach") -> DestinationVibe.BEACH
                    else -> DestinationVibe.WARM
                }
                val custom = Destination(
                    id = "custom_${lat}_${lon}",
                    name = label,
                    region = "Custom",
                    latitude = lat,
                    longitude = lon,
                    vibe = vibe,
                    audienceTags = setOf("all", "family", "friends", "couples", "kids"),
                    seasonNote = "",
                )
                listOf(custom)
            }
            filters.destinationQuery.isNotBlank() -> {
                val matched = matchByQuery(filters.destinationQuery)
                if (matched.isNotEmpty()) matched else all
            }
            else -> all
        }

        return baseList
            .map { dest ->
                val km = distanceFn(fromLat, fromLon, dest.latitude, dest.longitude).toInt()
                dest to km
            }
            .filter { (dest, km) -> dest.id.startsWith("custom_") || km <= maxKm }
            .filter { (dest, _) ->
                dest.id.startsWith("custom_") ||
                    filters.region == "All India" ||
                    dest.region == filters.region
            }
            .filter { (dest, _) ->
                if (dest.id.startsWith("custom_")) {
                    true
                } else {
                    val vibes = filters.vibes
                    vibes.contains("any") ||
                        vibes.contains(dest.vibe.id) ||
                        (vibes.contains("cool") && dest.vibe == DestinationVibe.COOL) ||
                        (vibes.contains("beach") && dest.vibe == DestinationVibe.BEACH)
                }
            }
            .filter { (dest, _) ->
                if (dest.id.startsWith("custom_")) {
                    true
                } else {
                    val audiences = filters.audiences
                    audiences.contains("all") ||
                        dest.audienceTags.any { it in audiences }
                }
            }
            .filter { (_, km) ->
                when (filters.transport) {
                    "flight" -> true
                    "train" -> km <= 800
                    "bus" -> km <= 500
                    else -> km <= 400 || filters.destinationLat != null
                }
            }
            .sortedBy { (_, km) -> km }
    }
}
