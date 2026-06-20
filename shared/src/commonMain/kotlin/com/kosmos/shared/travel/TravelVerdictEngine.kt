package com.kosmos.shared.travel

import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot

object TravelVerdictEngine {

    fun travelVerdicts(
        current: WeatherSnapshot,
        home: WeatherSnapshot?,
        distanceKm: Int,
    ): List<Verdict> {
        if (home == null) return emptyList()
        val verdicts = mutableListOf<Verdict>()

        val packItems = PackListEngine.generate(
            homeTemp = home.temp,
            destTemp = current.temp,
            destDaily = current.daily,
            destUv = current.uvIndex,
            distanceKm = distanceKm,
        )
        val packDetail = packItems.joinToString(" · ") { "${it.emoji} ${it.item}" }
        verdicts += Verdict(
            id = "travel.pack",
            emoji = "🎒",
            title = "Pack for this trip",
            detail = packDetail,
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF5A8C4D,
        )

        val homeDelta = current.temp - home.temp
        val homeCondition = PlainLanguage.weatherConditionLabel(home.weatherCode).lowercase()
        verdicts += Verdict(
            id = "travel.homeReturn",
            emoji = "🏠",
            title = "Home is ${home.temp.toInt()}° when you return",
            detail = when {
                homeDelta >= 8 -> "${home.cityName} is ${homeDelta.toInt()}° warmer — you'll feel the heat stepping off the train"
                homeDelta <= -8 -> "${home.cityName} is ${(-homeDelta).toInt()}° cooler — pack something warm for arrival"
                homeCondition.contains("rain") -> "Rain waiting at ${home.cityName} — plan dry clothes for home"
                else -> "${home.cityName} looks similar — no big shock when you're back"
            },
            priority = VerdictPriority.ACTION,
            accentColor = 0xFF3F8CD9,
        )

        if (current.uvIndex >= 8 && current.temp > home.temp) {
            verdicts += Verdict(
                id = "travel.altitude",
                emoji = "⛰",
                title = "Stronger sun here",
                detail = "UV is ${PlainLanguage.uvDescription(current.uvIndex).lowercase()} — altitude or clearer air amps the burn",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD98C33,
            )
        }

        return verdicts
    }
}
