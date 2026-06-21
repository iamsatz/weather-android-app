package com.kosmos.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IndiaPlacesCatalogTest {

    @Test
    fun snapToNearest_findsGachibowliWithin2km() {
        val snap = IndiaPlacesCatalog.snapToNearest(17.4420, 78.3500)
        assertNotNull(snap)
        assertEquals("Gachibowli", snap!!.neighborhood)
        assertEquals("Hyderabad", snap.city)
    }

    @Test
    fun snapToNearest_returnsNullWhenFar() {
        assertNull(IndiaPlacesCatalog.snapToNearest(51.5074, -0.1278))
    }

    @Test
    fun stabilizeLabels_keepsSavedWhenWithin500m() {
        val saved = SavedLocation(
            city = "Hyderabad",
            neighborhood = "Biramguda",
            subArea = null,
            country = "India",
            latitude = 17.3120,
            longitude = 78.5340,
        )
        val incoming = GeoLocation(
            city = "Near you",
            neighborhood = null,
            country = "India",
            latitude = 17.3125,
            longitude = 78.5345,
        )
        val stable = IndiaPlacesCatalog.stabilizeLabels(incoming, saved)
        assertEquals("Hyderabad", stable.city)
        assertEquals("Biramguda", stable.neighborhood)
        assertEquals(17.3125, stable.latitude, 0.0001)
    }

    @Test
    fun enrichGeo_snapsVagueNearYouToCatalog() {
        val geo = GeoLocation(
            city = "Near you",
            neighborhood = null,
            country = null,
            latitude = 17.4410,
            longitude = 78.3490,
        )
        val enriched = IndiaPlacesCatalog.enrichGeo(geo)
        assertEquals("Hyderabad", enriched.city)
        assertEquals("Gachibowli", enriched.neighborhood)
    }
}
