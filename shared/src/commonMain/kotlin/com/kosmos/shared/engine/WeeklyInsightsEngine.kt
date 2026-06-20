package com.kosmos.shared.engine

import com.kosmos.shared.models.DailyData
import com.kosmos.shared.models.WeatherSnapshot

data class WeeklyInsight(
    val rainDays: Int,
    val heatSpikes: Int,
    val summary: String,
)

object WeeklyInsightsEngine {

    fun summarize(snapshot: WeatherSnapshot): WeeklyInsight {
        val days = snapshot.daily.take(7)
        val rainDays = days.count { it.precipProbabilityMax >= 40 }
        val heatSpikes = days.count { it.high >= 38 }
        val summary = buildString {
            append("Your week: ")
            append("$rainDays rain day${if (rainDays == 1) "" else "s"}")
            append(", $heatSpikes heat spike${if (heatSpikes == 1) "" else "s"}")
            if (rainDays == 0 && heatSpikes == 0) append(" — mostly steady")
        }
        return WeeklyInsight(rainDays, heatSpikes, summary)
    }

    fun digestBody(snapshot: WeatherSnapshot): String {
        val insight = summarize(snapshot)
        val hottest = snapshot.daily.take(7).maxByOrNull { it.high }
        val wettest = snapshot.daily.take(7).maxByOrNull { it.precipProbabilityMax }
        return buildString {
            append(insight.summary)
            append(". ")
            hottest?.let { append("Hottest: ${it.dateLabel} ~${it.high.toInt()}°. ") }
            wettest?.takeIf { it.precipProbabilityMax >= 30 }?.let {
                append("Wettest: ${it.dateLabel} (${it.precipProbabilityMax}% rain). ")
            }
            append("Open Kosmos for today's call.")
        }
    }
}
