package com.kosmos.android.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RainViewerClient(private val httpClient: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    data class RadarFrame(
        val timeSeconds: Long,
        val path: String,
        val tileUrl: String,
    )

    data class RadarResult(
        val lat: Double,
        val lon: Double,
        val zoom: Int,
        val frames: List<RadarFrame>,
    ) {
        val latestFrame: RadarFrame? get() = frames.lastOrNull()
        val tileTemplate: String?
            get() = latestFrame?.path?.let { path ->
                "https://tilecache.rainviewer.com$path/256/{z}/{x}/{y}/2/1_1.png"
            }
    }

    suspend fun loadRadar(lat: Double, lon: Double, zoom: Int = 9): RadarResult? {
        val response: String = httpClient.get("https://api.rainviewer.com/public/weather-maps.json").body()
        val data = json.decodeFromString<RainViewerResponse>(response)
        val past = data.radar?.past?.takeLast(8) ?: return null
        if (past.isEmpty()) return null
        val frames = past.map { frame ->
            RadarFrame(
                timeSeconds = frame.time,
                path = frame.path,
                tileUrl = buildTileUrl(frame.path, lat, lon, zoom),
            )
        }
        return RadarResult(lat = lat, lon = lon, zoom = zoom, frames = frames)
    }

    fun buildTileUrl(path: String, lat: Double, lon: Double, zoom: Int): String {
        val (x, y) = latLonToTile(lat, lon, zoom)
        return "https://tilecache.rainviewer.com$path/256/$zoom/$x/$y/2/1_1.png"
    }

    companion object {
        fun latLonToTile(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
            val n = 1 shl zoom
            val x = ((lon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
            val latRad = Math.toRadians(lat)
            val y = ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt().coerceIn(0, n - 1)
            return x to y
        }
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
