package com.kosmos.shared.engine

import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot

object HealthVerdictEngine {

    fun proVerdicts(snapshot: WeatherSnapshot, isPlus: Boolean): List<Verdict> {
        if (!isPlus) return emptyList()
        val verdicts = mutableListOf<Verdict>()

        migraineVerdict(snapshot)?.let { verdicts += it }
        asthmaVerdict(snapshot)?.let { verdicts += it }

        return verdicts
    }

    private fun migraineVerdict(snapshot: WeatherSnapshot): Verdict? {
        val pressures = snapshot.hourly.mapNotNull { it.surfacePressureHpa }
        if (pressures.size < 6) return null
        val drop = pressures.first() - pressures.last()
        if (drop < 4) return null
        return Verdict(
            id = "health.migraine",
            emoji = "🧠",
            title = "Pressure dropping — migraine risk",
            detail = "Barometer falling ~${drop.toInt()} hPa today — stay hydrated, limit screen glare",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF7B5EA7,
        )
    }

    private fun asthmaVerdict(snapshot: WeatherSnapshot): Verdict? {
        val aqi = snapshot.aqi ?: return null
        val humid = snapshot.humidity
        val pollen = snapshot.pollenIndex
        val roughAir = aqi >= 100 || humid > 85 || (pollen != null && pollen >= 3)
        if (!roughAir) return null

        val parts = buildList {
            if (aqi >= 100) add("rough air")
            if (humid > 85) add("heavy humidity")
            pollen?.takeIf { it >= 3 }?.let { add("high pollen") }
        }
        return Verdict(
            id = "health.asthma",
            emoji = "🫁",
            title = "Rough air for sensitive lungs",
            detail = parts.joinToString(" + ") + " — limit outdoor exertion, keep inhaler handy",
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF5A7C9E,
        )
    }
}
