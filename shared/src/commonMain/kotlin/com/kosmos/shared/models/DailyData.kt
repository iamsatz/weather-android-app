package com.kosmos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyData(
    val dateLabel: String,
    val high: Double,
    val low: Double,
    val precipProbabilityMax: Int,
    val sunriseHour: Int,
    val sunriseMinute: Int,
    val sunsetHour: Int,
    val sunsetMinute: Int,
)
