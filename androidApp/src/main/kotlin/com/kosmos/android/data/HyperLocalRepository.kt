package com.kosmos.android.data

import com.kosmos.android.BuildConfig
import com.kosmos.shared.api.MultiSourceClient
import com.kosmos.shared.models.upcomingHours
import com.kosmos.shared.api.OpenMeteoClient
import com.kosmos.shared.api.TomorrowClient
import com.kosmos.shared.api.createHttpClient
import com.kosmos.shared.engine.PlainLanguage
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class HyperLocalCard(
    val title: String,
    val body: String,
)

data class HyperLocalResult(
    val areaName: String,
    val temp: Int,
    val condition: String,
    val officialSummary: String,
    val agentInsight: String,
    val fusionSources: List<String> = emptyList(),
    val cards: List<HyperLocalCard> = emptyList(),
)

class HyperLocalRepository(
    private val locationRepo: LocationRepository,
) {
    private val openMeteo = OpenMeteoClient(createHttpClient())
    private val multiSource = MultiSourceClient(
        openMeteo,
        BuildConfig.TOMORROW_API_KEY.takeIf { it.isNotBlank() }?.let {
            TomorrowClient(createHttpClient(), it)
        },
    )
    private val http = createHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyze(areaQuery: String, useCelsius: Boolean): HyperLocalResult? {
        val geo = locationRepo.geocodeCity(areaQuery.trim()) ?: return null
        return analyzeGeo(geo, useCelsius)
    }

    suspend fun analyzeGeo(geo: GeoLocation, useCelsius: Boolean): HyperLocalResult {
        val areaName = PlainLanguage.formatLocationLine(geo.neighborhood, geo.city, geo.country)
        val weather = multiSource.fetchWeather(geo.latitude, geo.longitude, geo.city)
        val condition = PlainLanguage.weatherConditionLabel(weather.weatherCode)
        val nextHours = weather.upcomingHours(6)
        val hourlyRain = nextHours.joinToString(", ") {
            val secondary = it.secondaryPrecipProbability?.let { s -> " / alt $s%" } ?: ""
            "${it.label}: ${it.precipProbability}% rain$secondary"
        }
        val windLine = "Wind ${weather.windSpeedKmh.toInt()} km/h, gusts ${weather.windGustKmh.toInt()} km/h"
        val pressureLine = weather.surfacePressureHpa?.let { "Pressure ${it.toInt()} hPa" } ?: ""
        val sources = weather.fusionMeta?.sources?.joinToString(" + ") ?: "Open-Meteo"

        val officialSummary = buildString {
            append("${weather.temp.toInt()}°, $condition. ")
            append("Feels ${PlainLanguage.feelsLikeDescription(weather.feelsLike).lowercase()}. ")
            append("$windLine. ")
            if (pressureLine.isNotEmpty()) append("$pressureLine. ")
            if (hourlyRain.isNotEmpty()) append("Next hours: $hourlyRain. ")
            append("Sources: $sources.")
        }

        val agentInsight = runCatching {
            fetchAgentInsight(areaName, officialSummary, weather.humidity, weather.uvIndex, sources)
        }.getOrElse { officialSummary }

        val nextFewHours = nextHours.joinToString("\n") {
            "${it.label}: ${it.temp.toInt()}°, ${PlainLanguage.weatherConditionLabel(it.weatherCode).lowercase()}, rain ${it.precipProbability}%"
        }

        val cards = listOf(
            HyperLocalCard(
                title = "Right now",
                body = "${weather.temp.toInt()}°, $condition. Feels ${PlainLanguage.feelsLikeDescription(weather.feelsLike).lowercase()}. Humidity ${weather.humidity}%.",
            ),
            HyperLocalCard(
                title = "Next few hours",
                body = nextFewHours.ifBlank { "Steady through the next hours." },
            ),
            HyperLocalCard(
                title = "Wind & air",
                body = "$windLine. ${pressureLine.ifBlank { "Pressure steady." }} UV ${PlainLanguage.uvDescription(weather.uvIndex)}.",
            ),
            HyperLocalCard(
                title = "Local read",
                body = agentInsight,
            ),
        )

        return HyperLocalResult(
            areaName = areaName,
            temp = PlainLanguage.toDisplayTemp(weather.temp, useCelsius),
            condition = condition,
            officialSummary = officialSummary,
            agentInsight = agentInsight,
            fusionSources = weather.fusionMeta?.sources ?: listOf("Open-Meteo"),
            cards = cards,
        )
    }

    private suspend fun fetchAgentInsight(
        areaName: String,
        officialSummary: String,
        humidity: Int,
        uv: Double,
        sources: String,
    ): String {
        val response = http.post("https://text.pollinations.ai/openai") {
            contentType(ContentType.Application.Json)
            setBody(
                AgentRequest(
                    model = "openai",
                    messages = listOf(
                        AgentMessage(
                            role = "system",
                            content = """
                                You are Kosmos hyper-local weather agent for India.
                                Cross-check fused forecast data with what locals might report (traffic, sudden drizzle, heat pockets).
                                Weather-only. 2-3 sentences. Decision-first. Max 1 emoji.
                                Ground everything in the official fused numbers provided — do not invent numbers.
                                Never mention hotels or non-weather topics.
                            """.trimIndent(),
                        ),
                        AgentMessage(
                            role = "user",
                            content = """
                                Area: $areaName
                                Fused forecast ($sources): $officialSummary
                                Humidity: $humidity%, UV: ${uv.toInt()}
                                What should someone know about hyper-local weather here right now?
                            """.trimIndent(),
                        ),
                    ),
                ),
            )
        }.body<String>()

        return json.decodeFromString<AgentResponse>(response)
            .choices.firstOrNull()?.message?.content?.trim()
            ?: officialSummary
    }

    @Serializable
    private data class AgentRequest(val model: String, val messages: List<AgentMessage>)

    @Serializable
    private data class AgentMessage(val role: String, val content: String)

    @Serializable
    private data class AgentResponse(val choices: List<Choice>) {
        @Serializable
        data class Choice(val message: AgentMessage)
    }
}
