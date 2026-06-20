package com.kosmos.android.data

import com.kosmos.shared.models.WeatherSnapshot
import kotlinx.serialization.Serializable

@Serializable
data class CachedWeatherPayload(
    val snapshot: WeatherSnapshot,
    val neighborhood: String? = null,
    val country: String? = null,
    val hasLiveLocation: Boolean = false,
    val locationSource: String = LocationSource.SAVED_CITY.name,
    val savedAtMillis: Long,
)
