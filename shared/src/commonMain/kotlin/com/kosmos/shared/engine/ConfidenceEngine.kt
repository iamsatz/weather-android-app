package com.kosmos.shared.engine

import com.kosmos.shared.models.HourlyData
import kotlin.math.abs

object ConfidenceEngine {

    enum class Level { HIGH, MEDIUM, LOW }

    data class RainConfidence(
        val level: Level,
        val copy: String,
        val modelsAgree: Boolean? = null,
    )

    data class TempConfidence(
        val level: Level,
        val copy: String,
        val modelsAgree: Boolean? = null,
    )

    fun rainConfidence(next12: List<HourlyData>, hasSecondarySource: Boolean = false): RainConfidence? {
        if (next12.isEmpty()) return null
        val probs = next12.map { it.precipProbability }
        val max = probs.maxOrNull() ?: 0
        if (max < 30) return null

        val rainHours = probs.count { it >= 40 }
        val spread = (probs.maxOrNull() ?: 0) - (probs.minOrNull() ?: 0)
        val multiModel = multiModelRainAgreement(next12, hasSecondarySource)

        val base = when {
            rainHours >= 4 && max >= 60 -> RainConfidence(
                Level.HIGH,
                "High confidence — rain likely across $rainHours hours",
            )
            rainHours >= 2 && spread < 35 -> RainConfidence(
                Level.MEDIUM,
                "Moderate confidence — models mostly agree on rain",
            )
            spread >= 40 -> RainConfidence(
                Level.LOW,
                "Low confidence — forecast split, keep an eye on updates",
            )
            else -> RainConfidence(
                Level.MEDIUM,
                "Moderate confidence — rain possible but timing uncertain",
            )
        }

        return when {
            multiModel == true -> base.copy(
                copy = "2 models agree — ${base.copy.lowercase()}",
                modelsAgree = true,
            )
            multiModel == false -> base.copy(
                copy = "Models split on rain — ${base.copy.lowercase()}",
                modelsAgree = false,
            )
            else -> base
        }
    }

    fun tempConfidence(next12: List<HourlyData>, hasSecondarySource: Boolean): TempConfidence? {
        if (!hasSecondarySource || next12.isEmpty()) return null
        val pairs = next12.mapNotNull { h ->
            h.secondaryTemperature?.let { h.temp to it }
        }
        if (pairs.size < 4) return null

        val primaryHigh = next12.maxOf { it.temp }
        val secondaryHigh = pairs.maxOf { it.second }
        val primaryLow = next12.minOf { it.temp }
        val agree = kotlin.math.abs(primaryHigh - secondaryHigh) < 2.5
        return if (agree) {
            TempConfidence(Level.HIGH, "2 models agree on today's peak ~${primaryHigh.toInt()}°", true)
        } else {
            TempConfidence(
                Level.MEDIUM,
                "Models split on peak heat — plan for ${primaryLow.toInt()}–${primaryHigh.toInt()}°",
                false,
            )
        }
    }

    private fun multiModelRainAgreement(next12: List<HourlyData>, hasSecondary: Boolean): Boolean? {
        if (!hasSecondary) return null
        val pairs = next12.mapNotNull { h ->
            h.secondaryPrecipProbability?.let { h.precipProbability to it }
        }
        if (pairs.size < 3) return null

        val agreeCount = pairs.count { (primary, secondary) ->
            abs(primary - secondary) <= 20 || (primary >= 50 && secondary >= 50) || (primary < 30 && secondary < 30)
        }
        return agreeCount >= pairs.size * 2 / 3
    }
}
