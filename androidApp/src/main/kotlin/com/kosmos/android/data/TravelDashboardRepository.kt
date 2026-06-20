package com.kosmos.android.data

import com.kosmos.shared.api.OpenMeteoClient
import com.kosmos.shared.api.createHttpClient
import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.models.WeatherSnapshot as SharedSnapshot
import com.kosmos.shared.travel.PackItem
import com.kosmos.shared.travel.PackListEngine
import com.kosmos.shared.travel.TravelVerdictEngine
import com.kosmos.shared.models.Verdict as SharedVerdict

data class HomeWeatherSummary(
    val city: String,
    val locationLine: String,
    val temp: Int,
    val tempCelsius: Double,
    val conditionLabel: String,
    val shared: SharedSnapshot,
)

data class TravelDashboardData(
    val home: HomeWeatherSummary,
    val packItems: List<PackItem>,
    val travelVerdicts: List<SharedVerdict>,
)

class TravelDashboardRepository {

    private val client = OpenMeteoClient(createHttpClient())

    suspend fun load(
        currentShared: SharedSnapshot,
        travelState: TravelState,
        useCelsius: Boolean,
    ): TravelDashboardData? {
        val home = travelState.home ?: return null
        val homeShared = client.fetchWeather(home.latitude, home.longitude, home.city)
        val homeSummary = HomeWeatherSummary(
            city = home.city,
            locationLine = home.label,
            temp = PlainLanguage.toDisplayTemp(homeShared.temp, useCelsius),
            tempCelsius = homeShared.temp,
            conditionLabel = PlainLanguage.weatherConditionLabel(homeShared.weatherCode),
            shared = homeShared,
        )
        val packItems = PackListEngine.generate(
            homeTemp = homeShared.temp,
            destTemp = currentShared.temp,
            destDaily = currentShared.daily,
            destUv = currentShared.uvIndex,
            distanceKm = travelState.distanceKm,
        )
        val travelVerdicts = TravelVerdictEngine.travelVerdicts(
            current = currentShared,
            home = homeShared,
            distanceKm = travelState.distanceKm,
        )
        return TravelDashboardData(
            home = homeSummary,
            packItems = packItems,
            travelVerdicts = travelVerdicts,
        )
    }
}
