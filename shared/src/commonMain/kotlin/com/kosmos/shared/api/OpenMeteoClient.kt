package com.kosmos.shared.api

import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.models.DailyData
import com.kosmos.shared.models.HourlyData
import com.kosmos.shared.models.WeatherSnapshot
import com.kosmos.shared.travel.DestinationWeather
import com.kosmos.shared.util.TimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenMeteoClient(private val httpClient: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        forecastDays: Int = 2,
    ): WeatherSnapshot {
        val weatherResponse: HttpResponse = httpClient.get(WEATHER_URL) {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter(
                "current",
                "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,uv_index,wind_speed_10m,wind_gusts_10m,surface_pressure",
            )
            parameter(
                "hourly",
                "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation_probability,uv_index,weather_code,is_day,wind_speed_10m,wind_gusts_10m,surface_pressure",
            )
            parameter("daily", "temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max")
            parameter("timezone", "auto")
            parameter("forecast_days", forecastDays)
        }

        val weather = json.decodeFromString<OpenMeteoWeatherResponse>(weatherResponse.body())

        val aqi = runCatching {
            val aqiResponse: HttpResponse = httpClient.get(AQI_URL) {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("current", "us_aqi")
            }
            json.decodeFromString<OpenMeteoAqiResponse>(aqiResponse.body()).current?.usAqi
        }.getOrNull()

        return weather.toSnapshot(latitude, longitude, cityName, aqi)
    }

    suspend fun fetchSoil(latitude: Double, longitude: Double, currentHour: Int = 0): com.kosmos.shared.farmer.SoilData {
        val response: HttpResponse = httpClient.get(WEATHER_URL) {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("hourly", "soil_moisture_0_to_1cm,soil_temperature_0cm")
            parameter("timezone", "auto")
            parameter("forecast_days", 1)
        }
        val soil = json.decodeFromString<SoilResponse>(response.body())
        val hourIdx = currentHour.coerceIn(0, soil.hourly.soilMoisture.lastIndex.coerceAtLeast(0))
        val moisture = soil.hourly.soilMoisture.getOrNull(hourIdx)
        val temperature = soil.hourly.soilTemperature.getOrNull(hourIdx)
        return com.kosmos.shared.farmer.SoilData(moisture = moisture, temperature = temperature)
    }

    @Serializable
    private data class SoilResponse(val hourly: SoilHourly) {
        @Serializable
        data class SoilHourly(
            @SerialName("soil_moisture_0_to_1cm") val soilMoisture: List<Double?>,
            @SerialName("soil_temperature_0cm") val soilTemperature: List<Double?>,
        )
    }

    suspend fun fetchDailyOutlook(
        coords: List<Pair<Double, Double>>,
        days: Int = 5,
    ): List<DestinationWeather> {
        if (coords.isEmpty()) return emptyList()
        if (coords.size == 1) {
            val (lat, lon) = coords.first()
            return listOf(fetchSingleOutlook(lat, lon, days))
        }

        val weatherResponse: HttpResponse = httpClient.get(WEATHER_URL) {
            parameter("latitude", coords.joinToString(",") { it.first.toString() })
            parameter("longitude", coords.joinToString(",") { it.second.toString() })
            parameter("current", "temperature_2m,weather_code")
            parameter("daily", "temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max")
            parameter("timezone", "auto")
            parameter("forecast_days", days)
        }

        val responses = json.decodeFromString<List<OutlookResponse>>(weatherResponse.body())
        return responses.map { it.toDestinationWeather() }
    }

    private suspend fun fetchSingleOutlook(lat: Double, lon: Double, days: Int): DestinationWeather {
        val weatherResponse: HttpResponse = httpClient.get(WEATHER_URL) {
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("current", "temperature_2m,weather_code")
            parameter("daily", "temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max")
            parameter("timezone", "auto")
            parameter("forecast_days", days)
        }
        val weather = json.decodeFromString<OutlookResponse>(weatherResponse.body())
        return weather.toDestinationWeather()
    }

    @Serializable
    private data class OutlookResponse(
        val latitude: Double,
        val longitude: Double,
        val current: OutlookCurrent,
        val daily: OpenMeteoWeatherResponse.DailyBlock,
    ) {
        @Serializable
        data class OutlookCurrent(
            @SerialName("temperature_2m") val temperature: Double,
            @SerialName("weather_code") val weatherCode: Int,
        )

        fun toDestinationWeather(): DestinationWeather {
            val dailyList = daily.time.mapIndexed { index, date ->
                DailyData(
                    dateLabel = TimeUtils.dateLabelFromIso(date),
                    high = daily.high[index],
                    low = daily.low[index],
                    precipProbabilityMax = daily.precipMax[index] ?: 0,
                    sunriseHour = TimeUtils.hourFromIso(daily.sunrise[index]),
                    sunriseMinute = TimeUtils.minuteFromIso(daily.sunrise[index]),
                    sunsetHour = TimeUtils.hourFromIso(daily.sunset[index]),
                    sunsetMinute = TimeUtils.minuteFromIso(daily.sunset[index]),
                )
            }
            return DestinationWeather(
                latitude = latitude,
                longitude = longitude,
                temp = current.temperature,
                weatherCode = current.weatherCode,
                daily = dailyList,
            )
        }
    }

    @Serializable
    private data class OpenMeteoWeatherResponse(
        val latitude: Double,
        val longitude: Double,
        val timezone: String,
        val current: CurrentData,
        val hourly: HourlyBlock,
        val daily: DailyBlock,
    ) {
        @Serializable
        data class CurrentData(
            val time: String,
            @SerialName("temperature_2m") val temperature: Double,
            @SerialName("relative_humidity_2m") val humidity: Int,
            @SerialName("apparent_temperature") val feelsLike: Double,
            @SerialName("is_day") val isDay: Int,
            @SerialName("weather_code") val weatherCode: Int,
            @SerialName("uv_index") val uvIndex: Double,
            @SerialName("wind_speed_10m") val windSpeed: Double? = null,
            @SerialName("wind_gusts_10m") val windGusts: Double? = null,
            @SerialName("surface_pressure") val surfacePressure: Double? = null,
        )

        @Serializable
        data class HourlyBlock(
            val time: List<String>,
            @SerialName("temperature_2m") val temperature: List<Double>,
            @SerialName("apparent_temperature") val feelsLike: List<Double>,
            @SerialName("relative_humidity_2m") val humidity: List<Int>,
            @SerialName("precipitation_probability") val precipProbability: List<Int?>,
            @SerialName("uv_index") val uvIndex: List<Double?>,
            @SerialName("weather_code") val weatherCode: List<Int>,
            @SerialName("is_day") val isDay: List<Int>,
            @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList(),
            @SerialName("wind_gusts_10m") val windGusts: List<Double?> = emptyList(),
            @SerialName("surface_pressure") val surfacePressure: List<Double?> = emptyList(),
        )

        @Serializable
        data class DailyBlock(
            val time: List<String>,
            @SerialName("temperature_2m_max") val high: List<Double>,
            @SerialName("temperature_2m_min") val low: List<Double>,
            val sunrise: List<String>,
            val sunset: List<String>,
            @SerialName("precipitation_probability_max") val precipMax: List<Int?>,
        )

        fun toSnapshot(lat: Double, lon: Double, city: String, aqi: Int?): WeatherSnapshot {
            val currentHour = TimeUtils.hourFromIso(current.time)
            val todayIndex = 0
            val nowIndex = hourly.time.indexOfFirst { it == current.time }.let { idx ->
                if (idx >= 0) idx else hourly.time.indexOfFirst { TimeUtils.hourFromIso(it) == currentHour }
            }.coerceAtLeast(0)

            val hourly = hourly.time.mapIndexed { index, time ->
                val hour = TimeUtils.hourFromIso(time)
                HourlyData(
                    index = index,
                    hour = hour,
                    label = PlainLanguage.formatHourLabel(hour),
                    temp = hourly.temperature[index],
                    feelsLike = hourly.feelsLike[index],
                    precipProbability = hourly.precipProbability[index] ?: 0,
                    uvIndex = hourly.uvIndex[index] ?: 0.0,
                    humidity = hourly.humidity[index],
                    weatherCode = hourly.weatherCode[index],
                    isDay = hourly.isDay[index] == 1,
                    isNow = index == nowIndex,
                    windSpeedKmh = hourly.windSpeed.getOrNull(index) ?: 0.0,
                    windGustKmh = hourly.windGusts.getOrNull(index) ?: hourly.windSpeed.getOrNull(index) ?: 0.0,
                    surfacePressureHpa = hourly.surfacePressure.getOrNull(index),
                )
            }

            val dailyList = daily.time.mapIndexed { index, date ->
                DailyData(
                    dateLabel = TimeUtils.dateLabelFromIso(date),
                    high = daily.high[index],
                    low = daily.low[index],
                    precipProbabilityMax = daily.precipMax[index] ?: 0,
                    sunriseHour = TimeUtils.hourFromIso(daily.sunrise[index]),
                    sunriseMinute = TimeUtils.minuteFromIso(daily.sunrise[index]),
                    sunsetHour = TimeUtils.hourFromIso(daily.sunset[index]),
                    sunsetMinute = TimeUtils.minuteFromIso(daily.sunset[index]),
                )
            }

            val today = dailyList.getOrNull(todayIndex)

            return WeatherSnapshot(
                latitude = lat,
                longitude = lon,
                cityName = city,
                dateLabel = today?.dateLabel ?: TimeUtils.dateLabelFromIso(daily.time.firstOrNull() ?: ""),
                temp = current.temperature,
                feelsLike = current.feelsLike,
                high = today?.high ?: current.temperature,
                low = today?.low ?: current.temperature,
                humidity = current.humidity,
                isDay = current.isDay == 1,
                weatherCode = current.weatherCode,
                uvIndex = current.uvIndex,
                aqi = aqi,
                currentHour = currentHour,
                nowIndex = nowIndex,
                hourly = hourly,
                daily = dailyList,
                sunriseHour = today?.sunriseHour ?: 6,
                sunriseMinute = today?.sunriseMinute ?: 0,
                sunsetHour = today?.sunsetHour ?: 18,
                sunsetMinute = today?.sunsetMinute ?: 0,
                windSpeedKmh = current.windSpeed ?: 0.0,
                windGustKmh = current.windGusts ?: current.windSpeed ?: 0.0,
                surfacePressureHpa = current.surfacePressure,
            )
        }
    }

    @Serializable
    private data class OpenMeteoAqiResponse(
        val current: AqiCurrent?,
    ) {
        @Serializable
        data class AqiCurrent(
            @SerialName("us_aqi") val usAqi: Int,
        )
    }

    companion object {
        private const val WEATHER_URL = "https://api.open-meteo.com/v1/forecast"
        private const val AQI_URL = "https://air-quality-api.open-meteo.com/v1/air-quality"
    }
}
