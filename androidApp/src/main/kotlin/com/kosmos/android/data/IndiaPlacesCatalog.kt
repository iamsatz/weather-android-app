package com.kosmos.android.data

/**
 * Curated India neighborhoods for stable labels when reverse geocoders return vague city-only names.
 * Snap within [SNAP_RADIUS_KM] — engineering curation, not a global LocationKey API.
 */
object IndiaPlacesCatalog {

    const val SNAP_RADIUS_KM = 2.0
    const val STABLE_LABEL_RADIUS_KM = 0.5

    data class CuratedPlace(
        val neighborhood: String,
        val city: String,
        val country: String = "India",
        val latitude: Double,
        val longitude: Double,
    )

    val curatedPlaces: List<CuratedPlace> = listOf(
        CuratedPlace("Gachibowli", "Hyderabad", latitude = 17.4400, longitude = 78.3489),
        CuratedPlace("Banjara Hills", "Hyderabad", latitude = 17.4156, longitude = 78.4347),
        CuratedPlace("Jubilee Hills", "Hyderabad", latitude = 17.4239, longitude = 78.4070),
        CuratedPlace("Madhapur", "Hyderabad", latitude = 17.4486, longitude = 78.3908),
        CuratedPlace("HITEC City", "Hyderabad", latitude = 17.4435, longitude = 78.3772),
        CuratedPlace("Kondapur", "Hyderabad", latitude = 17.4600, longitude = 78.3640),
        CuratedPlace("Miyapur", "Hyderabad", latitude = 17.4967, longitude = 78.3574),
        CuratedPlace("Secunderabad", "Hyderabad", latitude = 17.4399, longitude = 78.4983),
        CuratedPlace("Biramguda", "Hyderabad", latitude = 17.3120, longitude = 78.5340),
        CuratedPlace("Uppal", "Hyderabad", latitude = 17.4058, longitude = 78.5591),
        CuratedPlace("LB Nagar", "Hyderabad", latitude = 17.3660, longitude = 78.5520),
        CuratedPlace("Charminar", "Hyderabad", latitude = 17.3616, longitude = 78.4747),
        CuratedPlace("Indiranagar", "Bengaluru", latitude = 12.9784, longitude = 77.6408),
        CuratedPlace("Koramangala", "Bengaluru", latitude = 12.9352, longitude = 77.6245),
        CuratedPlace("Whitefield", "Bengaluru", latitude = 12.9698, longitude = 77.7500),
        CuratedPlace("Bandra", "Mumbai", latitude = 19.0596, longitude = 72.8295),
        CuratedPlace("Andheri", "Mumbai", latitude = 19.1197, longitude = 72.8468),
        CuratedPlace("Connaught Place", "Delhi", latitude = 28.6315, longitude = 77.2167),
        CuratedPlace("Dwarka", "Delhi", latitude = 28.5921, longitude = 77.0460),
        CuratedPlace("T Nagar", "Chennai", latitude = 13.0418, longitude = 80.2341),
        CuratedPlace("RK Beach", "Visakhapatnam", latitude = 17.6868, longitude = 83.2185),
        CuratedPlace("Bolangir", "Bolangir", latitude = 20.7075, longitude = 83.4848),
    )

    fun snapToNearest(lat: Double, lon: Double): CuratedPlace? {
        var best: CuratedPlace? = null
        var bestKm = SNAP_RADIUS_KM
        for (place in curatedPlaces) {
            val km = LocationRepository.distanceKm(lat, lon, place.latitude, place.longitude)
            if (km <= bestKm) {
                bestKm = km
                best = place
            }
        }
        return best
    }

    fun enrichGeo(geo: GeoLocation): GeoLocation {
        val snap = snapToNearest(geo.latitude, geo.longitude) ?: return geo
        val neighborhood = geo.neighborhood?.takeIf { it.isNotBlank() }
            ?: snap.neighborhood
        return geo.copy(
            city = if (geo.city.equals("Near you", ignoreCase = true)) snap.city else geo.city,
            neighborhood = neighborhood,
            country = geo.country ?: snap.country,
        )
    }

    fun stabilizeLabels(incoming: GeoLocation, saved: SavedLocation?): GeoLocation {
        if (saved == null) return enrichGeo(incoming)
        val movedKm = LocationRepository.distanceKm(
            incoming.latitude,
            incoming.longitude,
            saved.latitude,
            saved.longitude,
        )
        if (movedKm <= STABLE_LABEL_RADIUS_KM) {
            return GeoLocation(
                city = saved.city,
                neighborhood = saved.neighborhood ?: incoming.neighborhood,
                subArea = saved.subArea ?: incoming.subArea,
                country = saved.country ?: incoming.country,
                latitude = incoming.latitude,
                longitude = incoming.longitude,
            )
        }
        return enrichGeo(incoming)
    }
}
