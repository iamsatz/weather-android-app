package com.kosmos.android.data

import com.kosmos.shared.api.OpenMeteoClient
import com.kosmos.shared.api.createHttpClient
import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.travel.DestinationAnalyzer
import com.kosmos.shared.travel.DestinationCatalog
import com.kosmos.shared.travel.DestinationResult
import com.kosmos.shared.travel.TravelFilters
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DestinationRepository {

    private val client = OpenMeteoClient(createHttpClient())
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadDestinations(
        fromLat: Double,
        fromLon: Double,
        homeTemp: Double,
        filters: TravelFilters,
    ): List<DestinationResult> {
        val matched = DestinationCatalog.filter(
            fromLat = fromLat,
            fromLon = fromLon,
            filters = filters,
            distanceFn = TravelDetector::distanceKm,
        )

        if (matched.isEmpty()) return emptyList()

        val coords = matched.map { (dest, _) -> dest.latitude to dest.longitude }
        val weatherList = client.fetchDailyOutlook(coords, days = 14)

        val baseResults = matched.mapIndexedNotNull { index, (dest, km) ->
            val weather = weatherList.getOrNull(index) ?: return@mapIndexedNotNull null
            val outlook = DestinationAnalyzer.analyze(
                homeTemp = homeTemp,
                destination = dest,
                weather = weather,
                targetDayOffset = filters.tripDayOffset,
            )
            DestinationResult(
                destination = dest,
                distanceKm = km,
                weather = weather,
                outlook = outlook,
            )
        }.sortedWith(
            compareByDescending<DestinationResult> { it.outlook.score }
                .thenBy { it.distanceKm },
        ).take(MAX_RESULTS)

        return enrichWithAiLines(homeTemp, baseResults)
    }

    fun contextLine(homeTemp: Double, filters: TravelFilters): String =
        DestinationAnalyzer.contextLine(homeTemp, filters)

    private suspend fun enrichWithAiLines(
        homeTemp: Double,
        results: List<DestinationResult>,
    ): List<DestinationResult> = coroutineScope {
        results.map { result ->
            async {
                val whyGo = runCatching { fetchWhyGoLine(homeTemp, result) }.getOrNull()
                result.copy(whyGo = whyGo)
            }
        }.awaitAll()
    }

    private suspend fun fetchWhyGoLine(homeTemp: Double, result: DestinationResult): String {
        val dest = result.destination
        val condition = PlainLanguage.weatherConditionLabel(result.weather.weatherCode)
        val dailySummary = result.weather.daily.joinToString("; ") {
            "${it.dateLabel}: ${it.high.toInt()}°/${it.low.toInt()}°, rain ${it.precipProbabilityMax}%"
        }

        val response = createHttpClient().post("https://text.pollinations.ai/openai") {
            contentType(ContentType.Application.Json)
            setBody(
                WhyGoRequest(
                    model = "openai",
                    messages = listOf(
                        WhyGoMessage(
                            role = "system",
                            content = """
                                You are Kosmos travel weather advisor for India.
                                One sentence only. Decision-first. Weather-only. Max 1 emoji.
                                Never mention hotels, rooms, or booking.
                                Never start with Hello or Sure.
                            """.trimIndent(),
                        ),
                        WhyGoMessage(
                            role = "user",
                            content = """
                                Home is ${homeTemp.toInt()}°. ${dest.name} is ${result.distanceKm} km away.
                                Now: ${result.weather.temp.toInt()}°, $condition.
                                5-day: $dailySummary.
                                Outlook: ${result.outlook.summary}.
                                Why go there for weather this week?
                            """.trimIndent(),
                        ),
                    ),
                ),
            )
        }.body<String>()

        return json.decodeFromString<WhyGoResponse>(response)
            .choices.firstOrNull()?.message?.content
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: result.outlook.summary
    }

    @Serializable
    private data class WhyGoRequest(
        val model: String,
        val messages: List<WhyGoMessage>,
    )

    @Serializable
    private data class WhyGoMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class WhyGoResponse(
        val choices: List<Choice>,
    ) {
        @Serializable
        data class Choice(val message: WhyGoMessage)
    }

    companion object {
        private const val MAX_RESULTS = 20
    }
}
