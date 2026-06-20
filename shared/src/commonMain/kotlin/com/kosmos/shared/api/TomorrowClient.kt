package com.kosmos.shared.api

import com.kosmos.shared.models.SecondaryHourly
import com.kosmos.shared.util.TimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TomorrowClient(
    private val httpClient: HttpClient,
    private val apiKey: String,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchHourly(lat: Double, lon: Double): List<SecondaryHourly> {
        if (apiKey.isBlank()) return emptyList()
        val response: HttpResponse = httpClient.get(FORECAST_URL) {
            parameter("location", "$lat,$lon")
            parameter("timesteps", "1h")
            parameter("units", "metric")
            parameter("apikey", apiKey)
        }
        val body = json.decodeFromString<TomorrowForecastResponse>(response.body())
        return body.timelines?.hourly?.firstOrNull()?.intervals?.mapNotNull { interval ->
            val hour = TimeUtils.hourFromIso(interval.startTime)
            val precip = interval.values.precipitationProbability ?: return@mapNotNull null
            SecondaryHourly(
                hour = hour,
                precipProbability = precip.toInt().coerceIn(0, 100),
                temperature = interval.values.temperature ?: 0.0,
            )
        } ?: emptyList()
    }

    suspend fun fetchPollenIndex(lat: Double, lon: Double): Int? {
        if (apiKey.isBlank()) return null
        return runCatching {
            val response: HttpResponse = httpClient.get(FORECAST_URL) {
                parameter("location", "$lat,$lon")
                parameter("timesteps", "1d")
                parameter("units", "metric")
                parameter("fields", "pollenIndex")
                parameter("apikey", apiKey)
            }
            val body = json.decodeFromString<TomorrowForecastResponse>(response.body())
            body.timelines?.daily?.firstOrNull()?.intervals?.firstOrNull()?.values?.pollenIndex?.toInt()
        }.getOrNull()
    }

    @Serializable
    private data class TomorrowForecastResponse(
        val timelines: Timelines?,
    ) {
        @Serializable
        data class Timelines(
            val hourly: List<Timeline>? = null,
            val daily: List<Timeline>? = null,
        )

        @Serializable
        data class Timeline(val intervals: List<Interval>)

        @Serializable
        data class Interval(
            @SerialName("startTime") val startTime: String,
            val values: Values,
        )

        @Serializable
        data class Values(
            val temperature: Double? = null,
            @SerialName("precipitationProbability") val precipitationProbability: Double? = null,
            @SerialName("pollenIndex") val pollenIndex: Double? = null,
        )
    }

    companion object {
        private const val FORECAST_URL = "https://api.tomorrow.io/v4/timelines"
    }
}
