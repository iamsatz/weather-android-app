package com.kosmos.shared.api

import com.kosmos.shared.engine.ConfidenceEngine
import com.kosmos.shared.models.FusionMeta
import com.kosmos.shared.models.SecondaryHourly
import com.kosmos.shared.models.WeatherSnapshot

class MultiSourceClient(
    private val openMeteo: OpenMeteoClient,
    private val tomorrow: TomorrowClient?,
) {

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        forecastDays: Int = 2,
    ): WeatherSnapshot {
        val primary = openMeteo.fetchWeather(latitude, longitude, cityName, forecastDays)
        val secondary = tomorrow?.fetchHourly(latitude, longitude) ?: emptyList()
        val pollen = tomorrow?.fetchPollenIndex(latitude, longitude)

        val mergedHourly = mergeSecondaryPrecip(primary.hourly, secondary)
        val sources = buildList {
            add("Open-Meteo")
            if (secondary.isNotEmpty()) add("Tomorrow.io")
        }

        val next12 = mergedHourly.drop(primary.nowIndex.coerceIn(0, mergedHourly.size)).take(12)
        val rainConf = ConfidenceEngine.rainConfidence(next12, secondary.isNotEmpty())
        val fusion = FusionMeta(
            sources = sources,
            modelsAgreeOnRain = rainConf?.modelsAgree,
            rainConfidenceCopy = rainConf?.copy,
        )

        return primary.copy(
            hourly = mergedHourly,
            pollenIndex = pollen,
            fusionMeta = fusion,
        )
    }

    private fun mergeSecondaryPrecip(
        hourly: List<com.kosmos.shared.models.HourlyData>,
        secondary: List<SecondaryHourly>,
    ): List<com.kosmos.shared.models.HourlyData> {
        if (secondary.isEmpty()) return hourly
        val byHour = secondary.associateBy { it.hour }
        return hourly.map { h ->
            val secondaryHour = byHour[h.hour]
            h.copy(
                secondaryPrecipProbability = secondaryHour?.precipProbability,
                secondaryTemperature = secondaryHour?.temperature,
            )
        }
    }
}
