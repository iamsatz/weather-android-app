package com.kosmos.android.data

import com.kosmos.shared.engine.PlainLanguage

data class LocationDisplay(
    val headline: String,
    val locality: String?,
    val subArea: String?,
    val city: String,
    val country: String?,
    val source: LocationSource,
    val isLiveGps: Boolean,
) {
    val appBarTitle: String
        get() {
            val primary = locality?.takeIf { it.isNotBlank() }
                ?: headline.takeIf { it.isNotBlank() }
                ?: city
            val secondary = subArea?.takeIf {
                it.isNotBlank() && !it.equals(primary, ignoreCase = true)
            } ?: city.takeIf {
                it.isNotBlank() &&
                    !it.equals(primary, ignoreCase = true) &&
                    locality != null
            }
            return when {
                secondary != null -> "$primary ($secondary)"
                primary.isNotBlank() -> primary
                else -> city.ifBlank { "Near you" }
            }
        }

    val detailLine: String
        get() {
            val parts = buildList {
                subArea?.trim()?.takeIf { it.isNotEmpty() && !it.equals(headline, ignoreCase = true) }?.let { add(it) }
                city.trim().takeIf { it.isNotEmpty() && !it.equals(headline, ignoreCase = true) }?.let { add(it) }
            }.distinctBy { it.lowercase() }
            return parts.joinToString(" · ")
        }

    val combinedLine: String
        get() = appBarTitle

    val legacyLine: String
        get() = PlainLanguage.formatLocationLine(locality ?: headline, city, country)

    companion object {
        fun from(
            neighborhood: String?,
            city: String,
            country: String?,
            source: LocationSource,
            isLiveGps: Boolean,
            subArea: String? = null,
        ): LocationDisplay {
            val live = isLiveGps && source == LocationSource.GPS
            val locality = neighborhood?.trim()?.takeIf { it.isNotEmpty() }?.let { titleCase(it) }
            val cityTitle = titleCase(city)
            val countryTitle = country?.trim()?.takeIf { it.isNotEmpty() }?.let { titleCase(it) }
            val area = subArea?.trim()?.takeIf { it.isNotEmpty() }?.let { titleCase(it) }

            val headline = when {
                live && locality != null -> locality
                live -> cityTitle
                locality != null -> locality
                else -> cityTitle
            }

            return LocationDisplay(
                headline = headline,
                locality = locality,
                subArea = area ?: cityTitle.takeIf { locality != null && !it.equals(locality, ignoreCase = true) },
                city = cityTitle,
                country = countryTitle,
                source = source,
                isLiveGps = live,
            )
        }

        private fun titleCase(text: String): String {
            if (text.isBlank()) return text
            return text.split(Regex("\\s+")).joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase() else c.toString()
                }
            }
        }
    }
}
