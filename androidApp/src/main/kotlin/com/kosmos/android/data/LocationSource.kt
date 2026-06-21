package com.kosmos.android.data

enum class LocationSource {
    GPS,
    IP_APPROXIMATE,
    SAVED_CITY,
    DEFAULT,
    ;

    fun displayLabel(): String = when (this) {
        GPS -> "GPS"
        IP_APPROXIMATE -> "IP approx"
        SAVED_CITY -> "Saved"
        DEFAULT -> "Default area"
    }
}
