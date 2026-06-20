package com.kosmos.android.data

data class HomeLocation(
    val city: String,
    val neighborhood: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
) {
    val label: String
        get() = com.kosmos.shared.engine.PlainLanguage.formatLocationLine(neighborhood, city, country)
}

data class TravelState(
    val isActive: Boolean,
    val distanceKm: Int,
    val home: HomeLocation?,
) {
    companion object {
        val Inactive = TravelState(isActive = false, distanceKm = 0, home = null)
    }
}

object TravelDetector {
    const val TRAVEL_THRESHOLD_KM = 100

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    fun evaluate(
        currentLat: Double,
        currentLon: Double,
        home: HomeLocation?,
        hasGps: Boolean,
    ): TravelState {
        if (!hasGps || home == null) return TravelState.Inactive
        val km = distanceKm(currentLat, currentLon, home.latitude, home.longitude).toInt()
        return TravelState(
            isActive = km > TRAVEL_THRESHOLD_KM,
            distanceKm = km,
            home = home,
        )
    }
}
