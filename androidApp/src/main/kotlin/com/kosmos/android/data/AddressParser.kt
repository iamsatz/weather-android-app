package com.kosmos.android.data

import android.location.Address

/**
 * Parses Android [Address] into city / neighborhood / sub-area for India-first display.
 * Matches how AccuWeather / Apple / Google label places: area name + city, not swapped.
 */
object AddressParser {

    fun parse(address: Address, freshGps: Boolean): GeoLocation {
        val lat = address.latitude
        val lon = address.longitude

        val city = sequenceOf(
            address.locality,
            address.subAdminArea,
            address.adminArea,
        ).firstOrNull { isUsablePlaceName(it) && !looksLikeStreet(it) } ?: "Near you"

        val neighborhood = sequenceOf(
            address.subLocality,
            address.featureName?.takeIf { isUsablePlaceName(it) && !looksLikeStreet(it) },
        ).firstOrNull {
            isUsablePlaceName(it) &&
                !it.equals(city, ignoreCase = true) &&
                !looksLikeStreet(it)
        }

        val subArea = sequenceOf(
            address.thoroughfare,
            address.featureName?.takeIf {
                isUsablePlaceName(it) &&
                    !it.equals(neighborhood, ignoreCase = true) &&
                    !it.equals(city, ignoreCase = true)
            },
            address.premises,
            address.subLocality?.takeIf {
                !it.equals(neighborhood, ignoreCase = true) &&
                    !it.equals(city, ignoreCase = true)
            },
        ).firstOrNull {
            isUsablePlaceName(it) &&
                !it.equals(city, ignoreCase = true) &&
                !it.equals(neighborhood, ignoreCase = true)
        }

        return GeoLocation(
            city = city,
            neighborhood = neighborhood,
            subArea = subArea,
            country = address.countryName,
            latitude = lat,
            longitude = lon,
        )
    }

    private fun isUsablePlaceName(value: String?): Boolean =
        !value.isNullOrBlank() && value.length >= 2

    private fun looksLikeStreet(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        val lower = value.lowercase()
        return lower.contains("road") ||
            lower.contains("street") ||
            lower.contains(" lane") ||
            lower.endsWith(" rd") ||
            lower.endsWith(" st")
    }
}
