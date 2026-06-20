package com.kosmos.android.data

import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherAlert
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.shared.engine.PlainLanguage

object WeatherAlertEngine {

    fun buildFeed(snapshot: WeatherSnapshot): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()

        rainSoonAlert(snapshot)?.let { alerts += it }
        heatAlert(snapshot)?.let { alerts += it }
        aqiAlert(snapshot)?.let { alerts += it }
        stormAlert(snapshot)?.let { alerts += it }
        windAlert(snapshot)?.let { alerts += it }

        alerts += WeatherAlert(
            id = "nowcast",
            emoji = if (snapshot.nowcastIsWet) "🌧" else "📍",
            title = if (snapshot.nowcastIsWet) "Rain watch" else "Right now",
            detail = snapshot.nowcast,
            severity = if (snapshot.nowcastIsWet) VerdictPriority.ACTION else VerdictPriority.NORMAL,
        )

        return alerts.distinctBy { it.id }
    }

    fun notificationCandidates(snapshot: WeatherSnapshot): List<WeatherAlert> =
        buildFeed(snapshot).filter {
            it.id != "nowcast" && it.severity != VerdictPriority.NORMAL
        }

    private fun rainSoonAlert(snapshot: WeatherSnapshot): WeatherAlert? {
        val next = snapshot.hourly.drop(1).take(3)
        val rainHour = next.firstOrNull { hour ->
            hour.emoji.contains("☔") || hour.emoji.contains("🌧")
        } ?: return null
        return WeatherAlert(
            id = "rain_soon_${rainHour.hour}",
            emoji = "☔",
            title = "Rain likely ${rainHour.label}",
            detail = "Carry cover if you head out around ${rainHour.label.lowercase()}.",
            severity = VerdictPriority.SEVERE,
        )
    }

    private fun heatAlert(snapshot: WeatherSnapshot): WeatherAlert? {
        if (snapshot.feelsLike < 38) return null
        return WeatherAlert(
            id = "heat_${snapshot.currentHour}",
            emoji = "🔥",
            title = "Dangerous heat",
            detail = "Feels like ${snapshot.feelsLikePlain} — hydrate and limit time outside.",
            severity = VerdictPriority.SEVERE,
        )
    }

    private fun aqiAlert(snapshot: WeatherSnapshot): WeatherAlert? {
        val aqi = snapshot.aqiValue ?: return null
        if (aqi < 150) return null
        val label = PlainLanguage.aqiLabel(aqi)
        return WeatherAlert(
            id = "aqi_${aqi / 50}",
            emoji = "😷",
            title = "Air is $label",
            detail = when {
                aqi >= 200 -> "Keep windows shut and limit outdoor time."
                else -> "Sensitive groups should take it easy outside."
            },
            severity = if (aqi >= 200) VerdictPriority.SEVERE else VerdictPriority.ACTION,
        )
    }

    private fun stormAlert(snapshot: WeatherSnapshot): WeatherAlert? {
        if (snapshot.condition != com.kosmos.android.model.WeatherCondition.THUNDERSTORM) return null
        return WeatherAlert(
            id = "storm",
            emoji = "⛈",
            title = "Storm incoming",
            detail = "Thunder possible — stay indoors if you can.",
            severity = VerdictPriority.SEVERE,
        )
    }

    private fun windAlert(snapshot: WeatherSnapshot): WeatherAlert? {
        if (snapshot.windSpeedKmh < 40) return null
        return WeatherAlert(
            id = "wind",
            emoji = "💨",
            title = "Strong wind",
            detail = "Gusty conditions — secure loose items and take care on two-wheelers.",
            severity = VerdictPriority.ACTION,
        )
    }
}
