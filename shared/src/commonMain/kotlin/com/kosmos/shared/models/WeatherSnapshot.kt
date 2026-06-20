package com.kosmos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherSnapshot(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val dateLabel: String,
    val temp: Double,
    val feelsLike: Double,
    val high: Double,
    val low: Double,
    val humidity: Int,
    val isDay: Boolean,
    val weatherCode: Int,
    val uvIndex: Double,
    val aqi: Int?,
    val currentHour: Int,
    val nowIndex: Int = 0,
    val hourly: List<HourlyData>,
    val daily: List<DailyData>,
    val sunriseHour: Int,
    val sunriseMinute: Int,
    val sunsetHour: Int,
    val sunsetMinute: Int,
    val windSpeedKmh: Double = 0.0,
    val windGustKmh: Double = 0.0,
    val surfacePressureHpa: Double? = null,
    val pollenIndex: Int? = null,
    val fusionMeta: FusionMeta? = null,
)
