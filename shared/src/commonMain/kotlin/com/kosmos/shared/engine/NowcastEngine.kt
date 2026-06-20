package com.kosmos.shared.engine

import com.kosmos.shared.models.HourlyData

object NowcastEngine {

    data class Result(
        val message: String,
        val isWet: Boolean,
    )

    fun generate(hourly: List<HourlyData>, nowIndex: Int, areaName: String): Result {
        val area = areaName.trim().ifBlank { "your area" }
        val upcoming = hourly.drop(nowIndex.coerceIn(0, hourly.size)).take(4)

        if (upcoming.isEmpty()) {
            return steady()
        }

        val rainHour = upcoming.firstOrNull { it.precipProbability >= 60 }
        if (rainHour != null) {
            val hoursUntil = (rainHour.index - nowIndex).coerceAtLeast(1)
            val timing = when {
                hoursUntil == 1 -> "in about an hour"
                hoursUntil == 2 -> "in about two hours"
                else -> "around ${PlainLanguage.formatHourLabel(rainHour.hour)}"
            }
            val intensity = when {
                rainHour.precipProbability >= 80 -> "Heavy showers"
                rainHour.precipProbability >= 65 -> "Rain likely"
                else -> "Rain possible"
            }
            val advice = when {
                rainHour.precipProbability >= 75 -> "raincoat, not umbrella, and watch for waterlogging"
                else -> "carry a raincoat if you head out"
            }
            return Result(
                message = "$intensity over your area ($area) $timing — $advice.",
                isWet = true,
            )
        }

        val drizzleHour = upcoming.firstOrNull { it.precipProbability in 40..59 }
        if (drizzleHour != null) {
            val minutes = (drizzleHour.index - nowIndex).coerceAtLeast(1) * 30
            return Result(
                message = "Light drizzle reaching your area ($area) in ~$minutes min — a foldable umbrella is enough.",
                isWet = true,
            )
        }

        val current = upcoming.first()
        if (current.humidity >= 75 && current.precipProbability < 30) {
            return Result(
                message = "No rain, but your area ($area) stays muggy till evening — carry water if you are out.",
                isWet = false,
            )
        }

        if (upcoming.all { it.precipProbability < 20 && it.weatherCode in CLEAR_CODES }) {
            return Result(
                message = "Clear over your area ($area) for the next few hours — a good window to be outside.",
                isWet = false,
            )
        }

        return steady()
    }

    private fun steady() = Result(
        message = "Conditions are steady around you for the next hour.",
        isWet = false,
    )

    private val CLEAR_CODES = setOf(0, 1, 2)
}
