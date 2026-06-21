package com.kosmos.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationDisplayTest {

    @Test
    fun gpsWithLocality_showsLocalityInAppBar() {
        val display = LocationDisplay.from(
            neighborhood = "Gachibowli",
            city = "Hyderabad",
            country = "India",
            source = LocationSource.GPS,
            isLiveGps = true,
            subArea = "BHEL",
        )
        assertEquals("Gachibowli", display.headline)
        assertEquals("Gachibowli (Bhel)", display.appBarTitle)
        assertTrue(display.isLiveGps)
    }

    @Test
    fun gpsWithoutLocality_showsCityHeadline() {
        val display = LocationDisplay.from(
            neighborhood = null,
            city = "Hyderabad",
            country = "India",
            source = LocationSource.GPS,
            isLiveGps = true,
        )
        assertEquals("Hyderabad", display.headline)
        assertEquals("Hyderabad", display.appBarTitle)
        assertTrue(display.isLiveGps)
    }

    @Test
    fun ipFallback_showsCityNotCurrentLocation() {
        val display = LocationDisplay.from(
            neighborhood = null,
            city = "Hyderabad",
            country = "India",
            source = LocationSource.IP_APPROXIMATE,
            isLiveGps = false,
        )
        assertEquals("Hyderabad", display.headline)
        assertEquals("Hyderabad", display.appBarTitle)
        assertFalse(display.isLiveGps)
    }

    @Test
    fun savedCityWithNeighborhood_showsLocalityInAppBar() {
        val display = LocationDisplay.from(
            neighborhood = "Banjara Hills",
            city = "Hyderabad",
            country = "India",
            source = LocationSource.SAVED_CITY,
            isLiveGps = false,
        )
        assertEquals("Banjara Hills", display.headline)
        assertEquals("Banjara Hills (Hyderabad)", display.appBarTitle)
    }
}
