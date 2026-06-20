package com.kosmos.android.data

enum class LocationSource {
    GPS,
    IP_APPROXIMATE,
    SAVED_CITY,
    DEFAULT,
    ;

    fun displayLabel(): String = when (this) {
        GPS -> "GPS · accurate"
        IP_APPROXIMATE -> "Approximate · from network"
        SAVED_CITY -> "Saved city"
        DEFAULT -> "Default area"
    }
}
