package com.kosmos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class HourlyData(
    val index: Int = 0,
    val hour: Int,
    val label: String,
    val temp: Double,
    val feelsLike: Double,
    val precipProbability: Int,
    val uvIndex: Double,
    val humidity: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val isNow: Boolean = false,
    val windSpeedKmh: Double = 0.0,
    val windGustKmh: Double = 0.0,
    val surfacePressureHpa: Double? = null,
    val secondaryPrecipProbability: Int? = null,
    val secondaryTemperature: Double? = null,
)
