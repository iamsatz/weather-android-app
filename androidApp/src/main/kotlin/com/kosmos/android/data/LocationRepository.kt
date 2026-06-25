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
import io.ktor.client.request.header
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val subArea: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
)

data class GeocodeSearchResult(
    val label: String,
    val detail: String,
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
                        buildGeoFromFixFast(fix),
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
        subArea = saved.subArea,
        country = saved.country,
        latitude = saved.latitude,
        longitude = saved.longitude,
    )

    @SuppressLint("MissingPermission")
    private suspend fun obtainDeviceFix(): Location? {
        readLastKnownLocation()?.takeIf { it.isUsableCachedFix() }?.let { return it }

        requestSingleFix(Priority.PRIORITY_BALANCED_POWER_ACCURACY, BALANCED_FIX_TIMEOUT_MS)?.let { return it }

        awaitBestLocationFix(HIGH_ACCURACY_BURST_MS)?.let { return it }

        return readLastKnownLocation()?.takeIf { it.accuracy <= 1_000f }
    }

    private fun Location.isUsableCachedFix(): Boolean {
        val ageMs = System.currentTimeMillis() - time
        return ageMs <= MAX_CACHED_FIX_AGE_MS && accuracy <= MAX_CACHED_FIX_ACCURACY_M
    }

    /** Coords + India catalog snap — no network geocode wait. */
    private fun buildGeoFromFixFast(fix: Location): GeoLocation {
        val base = GeoLocation(
            city = "Near you",
            neighborhood = null,
            country = null,
            latitude = fix.latitude,
            longitude = fix.longitude,
        )
        return IndiaPlacesCatalog.enrichGeo(base)
    }

    /** Full geocode path — used for background label refinement. */
    private suspend fun buildGeoFromFix(fix: Location): GeoLocation {
        val raw = reverseGeocode(fix.latitude, fix.longitude, freshGps = true)
            ?: GeoLocation(
                city = "Near you",
                neighborhood = null,
                country = null,
                latitude = fix.latitude,
                longitude = fix.longitude,
            )
        return IndiaPlacesCatalog.enrichGeo(raw)
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
    private suspend fun awaitBestLocationFix(timeoutMs: Long = HIGH_ACCURACY_BURST_MS): Location? =
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

    /** Keeps display labels stable when GPS jitter is under 500 m. Coords always update. */
    suspend fun stabilizeForDisplay(incoming: GeoLocation, saved: SavedLocation?): GeoLocation {
        val prefs = PreferencesRepository(context)
        val baseline = saved ?: prefs.getSavedLocation()
        return IndiaPlacesCatalog.stabilizeLabels(incoming, baseline)
    }


    suspend fun reverseGeocode(lat: Double, lon: Double, freshGps: Boolean = false): GeoLocation? =
        coroutineScope {
            val android = async(Dispatchers.IO) { geocodeWithAndroid(lat, lon, freshGps) }
            val openMeteo = async(Dispatchers.IO) { fetchOpenMeteoReverse(lat, lon) }
            val bigData = async(Dispatchers.IO) { fetchBigDataCloudReverse(lat, lon) }
            val nominatim = async(Dispatchers.IO) { fetchNominatimReverse(lat, lon) }

            val sources = listOfNotNull(android.await(), openMeteo.await(), bigData.await(), nominatim.await())
            if (sources.isEmpty()) return@coroutineScope null
            mergeGeocodes(sources, lat, lon)
        }

    private fun mergeGeocodes(sources: List<GeoLocation>, lat: Double, lon: Double): GeoLocation {
        val cities = sources.map { it.city.trim() }
            .filter { it.isNotBlank() && !it.equals("Near you", ignoreCase = true) }
        val city = cities.groupingBy { it.lowercase() }
            .eachCount()
            .maxByOrNull { it.value }
            ?.let { top -> cities.first { it.equals(top.key, ignoreCase = true) } }
            ?: cities.firstOrNull()
            ?: "Near you"

        val neighborhood = sources.mapNotNull { it.neighborhood?.trim()?.takeIf { n -> n.isNotBlank() } }
            .firstOrNull { !it.equals(city, ignoreCase = true) }

        val subArea = sources.mapNotNull { it.subArea?.trim()?.takeIf { s -> s.isNotBlank() } }
            .firstOrNull {
                !it.equals(city, ignoreCase = true) &&
                    !it.equals(neighborhood, ignoreCase = true)
            }

        val country = sources.mapNotNull { it.country?.trim()?.takeIf { c -> c.isNotBlank() } }.firstOrNull()

        return GeoLocation(
            city = city,
            neighborhood = neighborhood,
            subArea = subArea,
            country = country,
            latitude = lat,
            longitude = lon,
        )
    }

    private suspend fun geocodeWithAndroid(lat: Double, lon: Double, freshGps: Boolean): GeoLocation? =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull() ?: return@withContext null
                AddressParser.parse(address, freshGps)
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun fetchOpenMeteoReverse(lat: Double, lon: Double): GeoLocation? =
        runCatching {
            val response = httpClient.get(
                "https://geocoding-api.open-meteo.com/v1/reverse" +
                    "?latitude=$lat&longitude=$lon&language=en",
            ).body<String>()
            val data = json.decodeFromString<OpenMeteoReverseResponse>(response)
            val city = sequenceOf(
                data.name,
                data.admin1,
            ).firstOrNull { !it.isNullOrBlank() } ?: return null
            GeoLocation(
                city = city,
                neighborhood = data.name?.takeIf {
                    !it.equals(city, ignoreCase = true) &&
                        data.admin1 != null &&
                        !it.equals(data.admin1, ignoreCase = true)
                },
                country = data.country,
                latitude = lat,
                longitude = lon,
            )
        }.getOrNull()

    private suspend fun fetchBigDataCloudReverse(lat: Double, lon: Double): GeoLocation? =
        runCatching {
            val response = httpClient.get(
                "https://api.bigdatacloud.net/data/reverse-geocode-client" +
                    "?latitude=$lat&longitude=$lon&localityLanguage=en",
            ).body<String>()
            val data = json.decodeFromString<BigDataCloudReverseResponse>(response)
            val city = data.city?.takeIf { it.isNotBlank() } ?: return null
            val neighborhood = sequenceOf(
                data.locality,
                data.localityInfo?.administrative
                    ?.sortedByDescending { it.order ?: 0 }
                    ?.firstOrNull { admin ->
                        admin.name.isNotBlank() &&
                            !admin.name.equals(city, ignoreCase = true) &&
                            (admin.adminLevel ?: 0) >= 6
                    }?.name,
            ).firstOrNull { !it.isNullOrBlank() && !it.equals(city, ignoreCase = true) }

            GeoLocation(
                city = city,
                neighborhood = neighborhood,
                country = data.countryName,
                latitude = lat,
                longitude = lon,
            )
        }.getOrNull()

    private suspend fun fetchNominatimReverse(lat: Double, lon: Double): GeoLocation? =
        runCatching {
            val response = httpClient.get(
                "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=$lat&lon=$lon&format=json&addressdetails=1&zoom=14&accept-language=en",
            ) {
                header("User-Agent", "Komos/1.0 Android weather")
            }.body<String>()
            val data = json.decodeFromString<NominatimReverseResponse>(response)
            val address = data.address ?: return null
            val city = sequenceOf(
                address.city,
                address.town,
                address.municipality,
                address.state_district,
            ).firstOrNull { !it.isNullOrBlank() } ?: return null

            val neighborhood = sequenceOf(
                address.suburb,
                address.village,
                address.neighbourhood,
                address.quarter,
                address.city_district,
            ).firstOrNull {
                !it.isNullOrBlank() && !it.equals(city, ignoreCase = true)
            } ?: address.county
                ?.replace(Regex("(?i)\\s*mandal$"), "")
                ?.trim()
                ?.takeIf { it.isNotBlank() && !it.equals(city, ignoreCase = true) }

            val subArea = sequenceOf(
                address.neighbourhood,
                address.residential,
                address.road,
            ).firstOrNull {
                !it.isNullOrBlank() &&
                    !it.equals(city, ignoreCase = true) &&
                    !it.equals(neighborhood, ignoreCase = true)
            }

            GeoLocation(
                city = city,
                neighborhood = neighborhood,
                subArea = subArea,
                country = address.country,
                latitude = lat,
                longitude = lon,
            )
        }.getOrNull()

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
            GeoLocation(city, neighborhood = null, country = data.country, latitude = lat, longitude = lon)
        }.getOrNull()
    }

    private suspend fun fetchIpApiLocation(): GeoLocation? {
        return runCatching {
            val response = httpClient.get("https://ipapi.co/json/").body<String>()
            val data = json.decodeFromString<IpApiResponse>(response)
            val city = data.city ?: return null
            val lat = data.latitude ?: return null
            val lon = data.longitude ?: return null
            GeoLocation(city, neighborhood = data.region, country = data.country_name, latitude = lat, longitude = lon)
        }.getOrNull()
    }

    suspend fun searchPlaces(query: String, limit: Int = 6): List<GeocodeSearchResult> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        val preset = presetCities
            .filter { (label, _) ->
                label.startsWith(trimmed, ignoreCase = true) ||
                    (trimmed.length >= 4 && label.contains(trimmed, ignoreCase = true))
            }
            .take(limit)
            .map { (label, geo) ->
                GeocodeSearchResult(
                    label = geo.neighborhood ?: geo.city,
                    detail = listOfNotNull(geo.city, geo.country).joinToString(" · "),
                    latitude = geo.latitude,
                    longitude = geo.longitude,
                )
            }

        val remote = runCatching { fetchOpenMeteoSearch(trimmed, limit) }.getOrDefault(emptyList())
        return (remote + preset).distinctBy { "${it.latitude},${it.longitude}" }.take(limit)
    }

    private suspend fun fetchOpenMeteoSearch(query: String, limit: Int): List<GeocodeSearchResult> =
        withContext(Dispatchers.IO) {
            val encoded = java.net.URLEncoder.encode(query, Charsets.UTF_8.name())
            val response = httpClient.get(
                "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=$limit&language=en&format=json",
            ).body<String>()
            val data = json.decodeFromString<OpenMeteoSearchResponse>(response)
            data.results.orEmpty().mapNotNull { place ->
                val lat = place.latitude ?: return@mapNotNull null
                val lon = place.longitude ?: return@mapNotNull null
                val name = place.name ?: return@mapNotNull null
                GeocodeSearchResult(
                    label = name,
                    detail = listOfNotNull(place.admin1, place.country).joinToString(" · "),
                    latitude = lat,
                    longitude = lon,
                )
            }
        }

    suspend fun geocodeCity(cityQuery: String): GeoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(cityQuery, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null
                AddressParser.parse(address, freshGps = false)
            } catch (_: Exception) {
                null
            }
        }
    }

    @Serializable
    private data class OpenMeteoReverseResponse(
        val name: String? = null,
        val admin1: String? = null,
        val country: String? = null,
    )

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
    private data class BigDataCloudReverseResponse(
        val city: String? = null,
        val locality: String? = null,
        val countryName: String? = null,
        val localityInfo: BigDataCloudLocalityInfo? = null,
    )

    @Serializable
    private data class BigDataCloudLocalityInfo(
        val administrative: List<BigDataCloudAdmin>? = null,
    )

    @Serializable
    private data class BigDataCloudAdmin(
        val name: String,
        val adminLevel: Int? = null,
        val order: Int? = null,
    )

    @Serializable
    private data class NominatimReverseResponse(
        val address: NominatimAddress? = null,
    )

    @Serializable
    private data class NominatimAddress(
        val suburb: String? = null,
        val village: String? = null,
        val neighbourhood: String? = null,
        val quarter: String? = null,
        val city_district: String? = null,
        val city: String? = null,
        val town: String? = null,
        val municipality: String? = null,
        val state_district: String? = null,
        val county: String? = null,
        val residential: String? = null,
        val road: String? = null,
        val country: String? = null,
    )

    @Serializable
    private data class OpenMeteoSearchResponse(
        val results: List<OpenMeteoSearchPlace>? = null,
    ) {
        @Serializable
        data class OpenMeteoSearchPlace(
            val name: String? = null,
            val latitude: Double? = null,
            val longitude: Double? = null,
            val admin1: String? = null,
            val country: String? = null,
        )
    }

    companion object {
        private const val MAX_CACHED_FIX_AGE_MS = 24 * 60 * 60_000L
        private const val MAX_CACHED_FIX_ACCURACY_M = 500f
        private const val BALANCED_FIX_TIMEOUT_MS = 5_000L
        private const val HIGH_ACCURACY_BURST_MS = 8_000L

        val DEFAULT_LOCATION = GeoLocation(
            city = "Hyderabad",
            neighborhood = null,
            country = "India",
            latitude = 17.3850,
            longitude = 78.4867,
        )

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
            "Hyderabad, India" to GeoLocation("Hyderabad", "Gachibowli", country = "India", latitude = 17.3850, longitude = 78.4867),
            "Biramguda, Hyderabad" to GeoLocation("Hyderabad", "Biramguda", country = "India", latitude = 17.3120, longitude = 78.5340),
            "Bengaluru, India" to GeoLocation("Bengaluru", "Indiranagar", country = "India", latitude = 12.9716, longitude = 77.5946),
            "Mumbai, India" to GeoLocation("Mumbai", "Bandra", country = "India", latitude = 19.0760, longitude = 72.8777),
            "Delhi, India" to GeoLocation("Delhi", "Connaught Place", country = "India", latitude = 28.6139, longitude = 77.2090),
            "Chennai, India" to GeoLocation("Chennai", "T Nagar", country = "India", latitude = 13.0827, longitude = 80.2707),
            "Pune, India" to GeoLocation("Pune", "Koregaon Park", country = "India", latitude = 18.5204, longitude = 73.8567),
            "Kolkata, India" to GeoLocation("Kolkata", "Salt Lake", country = "India", latitude = 22.5726, longitude = 88.3639),
            "Ahmedabad, India" to GeoLocation("Ahmedabad", "Satellite", country = "India", latitude = 23.0225, longitude = 72.5714),
            "Visakhapatnam, India" to GeoLocation("Visakhapatnam", "RK Beach", country = "India", latitude = 17.6868, longitude = 83.2185),
            "Bolangir, India" to GeoLocation("Bolangir", country = "India", latitude = 20.7075, longitude = 83.4848),
            "Balangir, India" to GeoLocation("Balangir", country = "India", latitude = 20.7075, longitude = 83.4848),
            "London, UK" to GeoLocation("London", "Westminster", country = "United Kingdom", latitude = 51.5074, longitude = -0.1278),
            "New York, USA" to GeoLocation("New York", "Manhattan", country = "United States", latitude = 40.7128, longitude = -74.0060),
            "Sydney, Australia" to GeoLocation("Sydney", "CBD", country = "Australia", latitude = -33.8688, longitude = 151.2093),
            "Tokyo, Japan" to GeoLocation("Tokyo", "Shibuya", country = "Japan", latitude = 35.6762, longitude = 139.6503),
        )
    }
}
