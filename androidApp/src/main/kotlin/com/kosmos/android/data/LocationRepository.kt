package com.kosmos.android.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class GeoLocation(
    val city: String,
    val neighborhood: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
)

data class ResolvedLocation(
    val geo: GeoLocation,
    val isLiveGps: Boolean,
    val source: LocationSource,
)

class LocationRepository(
    private val context: Context,
    private val httpClient: HttpClient,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val json = Json { ignoreUnknownKeys = true }
    private val resolveMutex = Mutex()

    fun isLocationServicesEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Single entry point for weather location. Coordinates always win over city names.
     * Never returns the dev default when the user granted location permission.
     */
    suspend fun resolveForWeather(hasPermission: Boolean, forceLiveGps: Boolean = false): ResolvedLocation? =
        resolveMutex.withLock {
            val prefs = PreferencesRepository(context)
            val wantsLiveGps = forceLiveGps || prefs.isUsingLiveGps()

            if (hasPermission && wantsLiveGps && isLocationServicesEnabled()) {
                obtainDeviceFix()?.let { fix ->
                    return@withLock ResolvedLocation(
                        buildGeoFromFix(fix),
                        isLiveGps = true,
                        source = LocationSource.GPS,
                    )
                }
                if (forceLiveGps) return@withLock null
            }

            fetchIpLocation()?.let {
                return@withLock ResolvedLocation(it, isLiveGps = false, source = LocationSource.IP_APPROXIMATE)
            }

            prefs.getSavedLocation()?.let { saved ->
                if (!isDefaultCoords(saved.latitude, saved.longitude)) {
                    return@withLock ResolvedLocation(
                        savedToGeo(saved),
                        isLiveGps = false,
                        source = LocationSource.SAVED_CITY,
                    )
                }
            }

            if (!hasPermission) {
                return@withLock ResolvedLocation(
                    DEFAULT_LOCATION,
                    isLiveGps = false,
                    source = LocationSource.DEFAULT,
                )
            }

            null
        }

    @SuppressLint("MissingPermission")
    suspend fun resolveFreshGpsLocation(): GeoLocation? =
        resolveMutex.withLock {
            if (!isLocationServicesEnabled()) return@withLock null
            obtainDeviceFix()?.let { buildGeoFromFix(it) }
        }

    private fun savedToGeo(saved: SavedLocation): GeoLocation = GeoLocation(
        city = saved.city,
        neighborhood = saved.neighborhood,
        country = saved.country,
        latitude = saved.latitude,
        longitude = saved.longitude,
    )

    @SuppressLint("MissingPermission")
    private suspend fun obtainDeviceFix(): Location? {
        val cached = readLastKnownLocation()
        if (cached != null && cached.accuracy <= 200f) return cached

        awaitBestLocationFix()?.let { return it }

        requestSingleFix(Priority.PRIORITY_HIGH_ACCURACY, 15_000)?.let { return it }
        requestSingleFix(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000)?.let { return it }

        return cached?.takeIf { it.accuracy <= 200f }
            ?: readLastKnownLocation()?.takeIf { it.accuracy <= 200f }
    }

    @SuppressLint("MissingPermission")
    private suspend fun readLastKnownLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(priority: Int, timeoutMs: Long): Location? =
        withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                val token = CancellationTokenSource()
                cont.invokeOnCancellation { token.cancel() }
                fusedClient.getCurrentLocation(priority, token.token)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitBestLocationFix(timeoutMs: Long = 20_000): Location? =
        withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                var best: Location? = null
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300L)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(200L)
                    .setMaxUpdateDelayMillis(1_000L)
                    .build()
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        for (loc in result.locations) {
                            if (best == null || loc.accuracy < best!!.accuracy) {
                                best = loc
                            }
                            if (loc.accuracy <= 150f) {
                                fusedClient.removeLocationUpdates(this)
                                if (cont.isActive) cont.resume(loc)
                            }
                        }
                    }
                }
                cont.invokeOnCancellation {
                    fusedClient.removeLocationUpdates(callback)
                    if (cont.isActive) cont.resume(best)
                }
                fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                    .addOnFailureListener {
                        fusedClient.removeLocationUpdates(callback)
                        if (cont.isActive) cont.resume(best)
                    }
            }
        }

    /** Weather uses lat/lon — city label is optional. Never drop a GPS fix because geocoding failed. */
    private suspend fun buildGeoFromFix(fix: Location): GeoLocation {
        reverseGeocode(fix.latitude, fix.longitude, freshGps = true)?.let { return it }
        return GeoLocation(
            city = "Near you",
            neighborhood = null,
            country = null,
            latitude = fix.latitude,
            longitude = fix.longitude,
        )
    }

    suspend fun reverseGeocode(lat: Double, lon: Double, freshGps: Boolean = false): GeoLocation? {
        fetchOpenMeteoReverse(lat, lon, freshGps)?.let { return it }
        return geocodeWithAndroid(lat, lon, freshGps)
    }

    private suspend fun geocodeWithAndroid(lat: Double, lon: Double, freshGps: Boolean): GeoLocation? =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull() ?: return@withContext null
                addressToGeo(address, lat, lon, freshGps)
            } catch (_: Exception) {
                null
            }
        }

    private fun addressToGeo(address: Address, lat: Double, lon: Double, freshGps: Boolean): GeoLocation {
        val city = sequenceOf(
            address.subLocality,
            address.locality,
            address.subAdminArea,
            address.adminArea,
        ).firstOrNull { !it.isNullOrBlank() && it.length >= 3 } ?: "Near you"

        val subLocality = address.subLocality?.takeIf {
            !it.isNullOrBlank() && !it.equals(city, ignoreCase = true)
        }
        val neighborhood = when {
            subLocality != null && looksLikeNeighborhood(subLocality, city) -> subLocality
            freshGps -> address.thoroughfare?.takeIf { !it.equals(city, ignoreCase = true) }
            else -> sequenceOf(
                address.featureName?.takeIf { !it.equals(city, ignoreCase = true) },
                address.thoroughfare,
            ).firstOrNull { !it.isNullOrBlank() && !it.equals(city, ignoreCase = true) }
        }
        val country = address.countryName
        return GeoLocation(city, neighborhood, country, lat, lon)
    }

    private fun looksLikeNeighborhood(name: String, city: String): Boolean {
        if (name.equals(city, ignoreCase = true)) return false
        if (name.length < 3) return false
        return true
    }

    private suspend fun fetchOpenMeteoReverse(lat: Double, lon: Double, freshGps: Boolean): GeoLocation? {
        return runCatching {
            val response = httpClient.get(
                "https://geocoding-api.open-meteo.com/v1/reverse?latitude=$lat&longitude=$lon&language=en&count=1",
            ).body<String>()
            val data = json.decodeFromString<OpenMeteoReverseResponse>(response)
            val result = data.results?.firstOrNull() ?: return null
            val city = result.name ?: return null
            GeoLocation(
                city = city,
                neighborhood = null,
                country = result.country,
                latitude = lat,
                longitude = lon,
            )
        }.getOrNull()
    }

    private suspend fun fetchIpLocation(): GeoLocation? =
        fetchIpWhoLocation() ?: fetchIpApiLocation()

    private suspend fun fetchIpWhoLocation(): GeoLocation? {
        return runCatching {
            val response = httpClient.get("https://ipwho.is/").body<String>()
            val data = json.decodeFromString<IpWhoResponse>(response)
            if (data.success != true) return null
            val city = data.city ?: return null
            val lat = data.latitude ?: return null
            val lon = data.longitude ?: return null
            GeoLocation(city, null, data.country, lat, lon)
        }.getOrNull()
    }

    private suspend fun fetchIpApiLocation(): GeoLocation? {
        return runCatching {
            val response = httpClient.get("https://ipapi.co/json/").body<String>()
            val data = json.decodeFromString<IpApiResponse>(response)
            val city = data.city ?: return null
            val lat = data.latitude ?: return null
            val lon = data.longitude ?: return null
            GeoLocation(city, data.region, data.country_name, lat, lon)
        }.getOrNull()
    }

    suspend fun geocodeCity(cityQuery: String): GeoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(cityQuery, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null
                addressToGeo(address, address.latitude, address.longitude, freshGps = false)
            } catch (_: Exception) {
                null
            }
        }
    }

    @Serializable
    private data class IpApiResponse(
        val city: String? = null,
        val region: String? = null,
        val country_name: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    @Serializable
    private data class IpWhoResponse(
        val success: Boolean? = null,
        val city: String? = null,
        val country: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    @Serializable
    private data class OpenMeteoReverseResponse(
        val results: List<OpenMeteoPlace>? = null,
    ) {
        @Serializable
        data class OpenMeteoPlace(
            val name: String? = null,
            val admin1: String? = null,
            val country: String? = null,
        )
    }

    companion object {
        val DEFAULT_LOCATION = GeoLocation("Hyderabad", null, "India", 17.3850, 78.4867)

        fun isDefaultCoords(lat: Double, lon: Double): Boolean {
            val d = DEFAULT_LOCATION
            return abs(lat - d.latitude) < 0.05 && abs(lon - d.longitude) < 0.05
        }

        fun isDefaultLocation(geo: GeoLocation): Boolean =
            isDefaultCoords(geo.latitude, geo.longitude)

        fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
            val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
            return earthRadiusKm * c
        }

        val presetCities = listOf(
            "Hyderabad, India" to GeoLocation("Hyderabad", "Gachibowli", "India", 17.3850, 78.4867),
            "Bengaluru, India" to GeoLocation("Bengaluru", "Indiranagar", "India", 12.9716, 77.5946),
            "Mumbai, India" to GeoLocation("Mumbai", "Bandra", "India", 19.0760, 72.8777),
            "Delhi, India" to GeoLocation("Delhi", "Connaught Place", "India", 28.6139, 77.2090),
            "Chennai, India" to GeoLocation("Chennai", "T Nagar", "India", 13.0827, 80.2707),
            "Visakhapatnam, India" to GeoLocation("Visakhapatnam", "RK Beach", "India", 17.6868, 83.2185),
            "Bolangir, India" to GeoLocation("Bolangir", null, "India", 20.7075, 83.4848),
            "Balangir, India" to GeoLocation("Balangir", null, "India", 20.7075, 83.4848),
            "London, UK" to GeoLocation("London", "Westminster", "United Kingdom", 51.5074, -0.1278),
            "New York, USA" to GeoLocation("New York", "Manhattan", "United States", 40.7128, -74.0060),
            "Tokyo, Japan" to GeoLocation("Tokyo", "Shibuya", "Japan", 35.6762, 139.6503),
        )
    }
}
