package com.kosmos.android.data

import kotlinx.serialization.Serializable

@Serializable
data class TripWizardDraft(
    val step: Int = 0,
    val destinationQuery: String = "",
    val destinationLat: Double? = null,
    val destinationLon: Double? = null,
    val destinationLabel: String = "",
    val pickedPlaceConfirmed: Boolean = false,
    val distanceMaxKm: Int = 100,
    val selectedVibe: String = "any",
    val tripDayOffset: Int = 0,
    val tripDays: Int = 2,
    val audience: String = "solo",
    val tripPurposes: Set<String> = setOf("any"),
)
