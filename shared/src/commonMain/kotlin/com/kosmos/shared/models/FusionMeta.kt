package com.kosmos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class FusionMeta(
    val sources: List<String>,
    val modelsAgreeOnRain: Boolean?,
    val rainConfidenceCopy: String?,
)

@Serializable
data class SecondaryHourly(
    val hour: Int,
    val precipProbability: Int,
    val temperature: Double,
)
