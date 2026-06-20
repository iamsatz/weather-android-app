package com.kosmos.android.data

import android.content.Context

class TravelRepository(context: Context) {

    private val prefs = PreferencesRepository(context)

    suspend fun getHome(): HomeLocation? = prefs.getHomeLocation()

    suspend fun ensureHomeFromGeo(geo: GeoLocation) {
        if (prefs.getHomeLocation() != null) return
        prefs.saveHomeLocation(
            HomeLocation(
                city = geo.city,
                neighborhood = geo.neighborhood,
                country = geo.country,
                latitude = geo.latitude,
                longitude = geo.longitude,
            ),
        )
    }

    suspend fun evaluateFromGeo(geo: GeoLocation, hasGps: Boolean): TravelState {
        if (hasGps) ensureHomeFromGeo(geo)
        val home = prefs.getHomeLocation()
        return TravelDetector.evaluate(geo.latitude, geo.longitude, home, hasGps)
    }
}
