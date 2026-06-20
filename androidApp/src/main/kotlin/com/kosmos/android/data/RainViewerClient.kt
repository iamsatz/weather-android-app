package com.kosmos.android.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RainViewerClient(private val httpClient: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun latestRadarUrl(): String? {
        val response: String = httpClient.get("https://api.rainviewer.com/public/weather-maps.json").body()
        val data = json.decodeFromString<RainViewerResponse>(response)
        val path = data.radar?.past?.lastOrNull()?.path ?: return null
        return "https://tilecache.rainviewer.com$path/256/5/23/13/2/1_1.png"
    }

    @Serializable
    private data class RainViewerResponse(
        val radar: Radar?,
    ) {
        @Serializable
        data class Radar(val past: List<Frame>?)

        @Serializable
        data class Frame(val time: Long, val path: String)
    }
}
