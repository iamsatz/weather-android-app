package com.kosmos.android.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kosmos.shared.models.HourlyData
import com.kosmos.shared.models.WeatherSnapshot
import kotlinx.coroutines.flow.first
import java.util.Calendar

private val Context.alertDataStore by preferencesDataStore("kosmos_alerts")

object AlertLogic {

    private val KEY_LAST_RAIN = stringPreferencesKey("last_rain_alert")
    private val KEY_LAST_SEVERE = stringPreferencesKey("last_severe_alert")
    private val KEY_FEED_SENT = stringSetPreferencesKey("feed_alerts_sent")

    fun isQuietHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 7
    }

    fun rainAlertMessage(hourly: List<HourlyData>, nowIndex: Int): String? {
        if (isQuietHours()) return null
        val rainHour = hourly.getOrNull(nowIndex + 1)?.takeIf { it.precipProbability >= 60 } ?: return null

        return when {
            rainHour.precipProbability >= 80 ->
                "Heavy rain likely in about an hour — grab a raincoat."
            else ->
                "Rain likely in about an hour — carry cover if you head out."
        }
    }

    fun severeAlertMessage(snapshot: WeatherSnapshot): String? {
        if (isQuietHours()) return null
        if (snapshot.feelsLike >= 40) {
            return "Dangerous heat — stay indoors and drink water."
        }
        val aqi = snapshot.aqi
        if (aqi != null && aqi > 200) {
            return "Air quality is very bad — keep windows shut and limit time outside."
        }
        return null
    }

    suspend fun shouldSendRainAlert(context: Context, key: String): Boolean {
        val last = context.alertDataStore.data.first()[KEY_LAST_RAIN]
        if (last == key) return false
        context.alertDataStore.edit { it[KEY_LAST_RAIN] = key }
        return true
    }

    suspend fun shouldSendFeedAlert(context: Context, key: String): Boolean {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val fullKey = "feed_${day}_$key"
        val sent = context.alertDataStore.data.first()[KEY_FEED_SENT] ?: emptySet()
        if (fullKey in sent) return false
        context.alertDataStore.edit { prefs ->
            prefs[KEY_FEED_SENT] = sent + fullKey
        }
        return true
    }

    suspend fun shouldSendSevereAlert(context: Context, key: String): Boolean {
        val last = context.alertDataStore.data.first()[KEY_LAST_SEVERE]
        if (last == key) return false
        context.alertDataStore.edit { it[KEY_LAST_SEVERE] = key }
        return true
    }

    fun rainAlertKey(nowIndex: Int, rainHourIndex: Int): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return "rain_${day}_${nowIndex}_$rainHourIndex"
    }

    fun severeAlertKey(type: String): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return "severe_${type}_$day"
    }
}
