package com.kosmos.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.mode.CommuteMode
import com.kosmos.shared.mode.UserMode
import com.kosmos.android.notification.NotificationEntry
import com.kosmos.android.notification.ScheduledReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kosmos_prefs")

class PreferencesRepository(private val context: Context) {

    val useCelsius: Flow<Boolean> = context.dataStore.data.map { it[KEY_CELSIUS] ?: true }
    val use24Hour: Flow<Boolean> = context.dataStore.data.map { it[KEY_24HOUR] ?: false }
    val useDarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: false }
    val showNumbers: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_NUMBERS] ?: false }
    val disabledVerdictIds: Flow<Set<String>> = context.dataStore.data.map { it[KEY_DISABLED_VERDICTS] ?: emptySet() }
    val appLocale: Flow<String> = context.dataStore.data.map { it[KEY_LOCALE] ?: AppLocale.EN.code }
    val userMode: Flow<String> = context.dataStore.data.map { it[KEY_USER_MODE] ?: UserMode.DEFAULT.id }
    val activeModes: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        resolveActiveModeOrder(prefs).toSet()
    }
    val activeModeOrder: Flow<List<String>> = context.dataStore.data.map { prefs ->
        resolveActiveModeOrder(prefs)
    }
    val commuteModes: Flow<Set<String>> = context.dataStore.data.map {
        it[KEY_COMMUTE] ?: setOf("bike")
    }
    val isKosmosPlus: Flow<Boolean> = context.dataStore.data.map { it[KEY_KOSMOS_PLUS] ?: false }
    val addedModes: Flow<Set<String>> = context.dataStore.data.map { it[KEY_ADDED_MODES] ?: emptySet() }
    val reminderIds: Flow<Set<String>> = context.dataStore.data.map { it[KEY_REMINDER_IDS] ?: emptySet() }
    val savedCity: Flow<String?> = context.dataStore.data.map { it[KEY_CITY] }
    val savedLat: Flow<Double?> = context.dataStore.data.map { it[KEY_LAT] }
    val savedLon: Flow<Double?> = context.dataStore.data.map { it[KEY_LON] }

    suspend fun setUseCelsius(value: Boolean) {
        context.dataStore.edit { it[KEY_CELSIUS] = value }
    }

    suspend fun setUse24Hour(value: Boolean) {
        context.dataStore.edit { it[KEY_24HOUR] = value }
    }

    suspend fun setUseDarkMode(value: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = value }
    }

    suspend fun setShowNumbers(value: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_NUMBERS] = value }
    }

    suspend fun setAppLocale(code: String) {
        context.dataStore.edit { it[KEY_LOCALE] = code }
    }

    suspend fun getAppLocale(): AppLocale =
        AppLocale.fromCode(context.dataStore.data.first()[KEY_LOCALE] ?: AppLocale.EN.code)

    suspend fun setUserMode(modeId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_MODE] = modeId
            prefs[KEY_ACTIVE_MODES_ORDER] = modeId
            prefs[KEY_ACTIVE_MODES] = setOf(modeId)
        }
    }

    suspend fun getUserMode(): UserMode =
        getActiveModeIds().firstOrNull()?.let { UserMode.fromId(it) }
            ?: UserMode.fromId(context.dataStore.data.first()[KEY_USER_MODE] ?: UserMode.DEFAULT.id)

    suspend fun getActiveModeIds(): List<String> {
        ensureActiveModesMigrated()
        return resolveActiveModeOrder(context.dataStore.data.first())
    }

    suspend fun getActiveModes(): List<UserMode> =
        getActiveModeIds().map { UserMode.fromId(it) }

    suspend fun toggleActiveMode(modeId: String): Boolean {
        if (!UserMode.isActivatable(modeId)) return false
        val mode = UserMode.fromId(modeId)
        ensureActiveModesMigrated()
        context.dataStore.edit { prefs ->
            val order = resolveActiveModeOrder(prefs).toMutableList()
            when {
                mode.isTakeoverMode -> {
                    order.clear()
                    order.add(modeId)
                }
                modeId in order -> {
                    order.remove(modeId)
                    if (order.isEmpty()) order.add(UserMode.DEFAULT.id)
                }
                modeId == UserMode.DEFAULT.id -> {
                    order.clear()
                    order.add(UserMode.DEFAULT.id)
                }
                else -> {
                    order.removeAll { UserMode.fromId(it).isTakeoverMode }
                    order.remove(UserMode.DEFAULT.id)
                    order.add(modeId)
                    trimLensCap(order)
                }
            }
            persistActiveModes(prefs, order)
        }
        return true
    }

    suspend fun activateTakeoverMode(modeId: String) {
        if (!UserMode.isActivatable(modeId)) return
        ensureActiveModesMigrated()
        context.dataStore.edit { prefs ->
            persistActiveModes(prefs, listOf(modeId))
        }
    }

    private suspend fun ensureActiveModesMigrated() {
        context.dataStore.edit { prefs ->
            if (prefs[KEY_ACTIVE_MODES_ORDER] != null) return@edit
            val legacy = prefs[KEY_USER_MODE] ?: UserMode.DEFAULT.id
            persistActiveModes(prefs, listOf(legacy))
        }
    }

    private fun resolveActiveModeOrder(prefs: Preferences): List<String> {
        val order = prefs[KEY_ACTIVE_MODES_ORDER]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() && UserMode.isActivatable(it) }
            ?.distinct()
        if (!order.isNullOrEmpty()) return order
        val legacy = prefs[KEY_USER_MODE] ?: UserMode.DEFAULT.id
        return listOf(legacy)
    }

    private fun trimLensCap(order: MutableList<String>) {
        val lenses = order.filter { id ->
            val mode = UserMode.fromId(id)
            mode.isStackableLens && mode != UserMode.DEFAULT
        }
        if (lenses.size <= MAX_ACTIVE_LENSES) return
        val dropCount = lenses.size - MAX_ACTIVE_LENSES
        lenses.take(dropCount).forEach { order.remove(it) }
    }

    private fun persistActiveModes(prefs: androidx.datastore.preferences.core.MutablePreferences, order: List<String>) {
        val cleaned = order.filter { UserMode.isActivatable(it) }.distinct()
        val finalOrder = cleaned.ifEmpty { listOf(UserMode.DEFAULT.id) }
        prefs[KEY_ACTIVE_MODES_ORDER] = finalOrder.joinToString(",")
        prefs[KEY_ACTIVE_MODES] = finalOrder.toSet()
        prefs[KEY_USER_MODE] = finalOrder.first()
    }

    suspend fun toggleCommute(commuteId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_COMMUTE]?.toMutableSet() ?: mutableSetOf()
            if (commuteId in current) current.remove(commuteId) else current.add(commuteId)
            prefs[KEY_COMMUTE] = current
        }
    }

    suspend fun getCommuteModes(): Set<CommuteMode> =
        CommuteMode.fromIds(context.dataStore.data.first()[KEY_COMMUTE] ?: setOf("bike"))

    suspend fun setKosmosPlus(active: Boolean) {
        context.dataStore.edit { it[KEY_KOSMOS_PLUS] = active }
    }

    suspend fun isKosmosPlusActive(): Boolean =
        PreferencesRepository.ALPHA_ALL_FREE ||
            (context.dataStore.data.first()[KEY_KOSMOS_PLUS] ?: false)

    suspend fun getAddedModes(): Set<String> =
        context.dataStore.data.first()[KEY_ADDED_MODES] ?: emptySet()

    /** Adds a mode to the library. Returns false when the 5-mode cap is hit. */
    suspend fun addMode(modeId: String): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_ADDED_MODES]?.toMutableSet() ?: mutableSetOf()
            when {
                modeId in current -> ok = true
                current.size >= MAX_ADDED_MODES -> ok = false
                else -> {
                    current.add(modeId)
                    prefs[KEY_ADDED_MODES] = current
                    ok = true
                }
            }
        }
        return ok
    }

    suspend fun removeMode(modeId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_ADDED_MODES]?.toMutableSet() ?: mutableSetOf()
            current.remove(modeId)
            prefs[KEY_ADDED_MODES] = current
            val order = resolveActiveModeOrder(prefs).toMutableList()
            if (modeId in order) {
                order.remove(modeId)
                if (order.isEmpty()) order.add(UserMode.DEFAULT.id)
                persistActiveModes(prefs, order)
            } else if (prefs[KEY_USER_MODE] == modeId) {
                prefs[KEY_USER_MODE] = UserMode.DEFAULT.id
            }
        }
    }

    suspend fun getReminderIds(): Set<String> =
        context.dataStore.data.first()[KEY_REMINDER_IDS] ?: emptySet()

    /** Records a reminder. Returns false when today's reminder cap is reached. */
    suspend fun addReminder(verdictId: String): Boolean {
        val today = java.time.LocalDate.now().toString()
        var allowed = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_REMINDER_IDS]?.toMutableSet() ?: mutableSetOf()
            if (verdictId in current) {
                allowed = true
                return@edit
            }
            val count = if (prefs[KEY_REMINDER_DATE] == today) prefs[KEY_REMINDER_COUNT] ?: 0 else 0
            if (count >= FREE_REMINDERS_DAILY) {
                allowed = false
                return@edit
            }
            current.add(verdictId)
            prefs[KEY_REMINDER_IDS] = current
            prefs[KEY_REMINDER_DATE] = today
            prefs[KEY_REMINDER_COUNT] = count + 1
            allowed = true
        }
        return allowed
    }

    suspend fun removeReminder(verdictId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_REMINDER_IDS]?.toMutableSet() ?: mutableSetOf()
            current.remove(verdictId)
            prefs[KEY_REMINDER_IDS] = current
        }
        removeScheduledReminder(verdictId)
    }

    suspend fun getScheduledReminders(): List<ScheduledReminder> {
        val raw = context.dataStore.data.first()[KEY_SCHEDULED_REMINDERS] ?: return emptyList()
        return runCatching {
            reminderJson.decodeFromString<List<ScheduledReminder>>(raw)
        }.getOrDefault(emptyList()).filter { it.fireTimeMs > System.currentTimeMillis() }
    }

    suspend fun upsertScheduledReminder(reminder: ScheduledReminder) {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                reminderJson.decodeFromString<List<ScheduledReminder>>(
                    prefs[KEY_SCHEDULED_REMINDERS] ?: "[]",
                )
            }.getOrDefault(emptyList()).filter { it.verdictId != reminder.verdictId }
            val updated = (current + reminder).sortedBy { it.fireTimeMs }.takeLast(10)
            prefs[KEY_SCHEDULED_REMINDERS] = reminderJson.encodeToString(updated)
        }
    }

    suspend fun removeScheduledReminder(verdictId: String) {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                reminderJson.decodeFromString<List<ScheduledReminder>>(
                    prefs[KEY_SCHEDULED_REMINDERS] ?: "[]",
                )
            }.getOrDefault(emptyList()).filter { it.verdictId != verdictId }
            prefs[KEY_SCHEDULED_REMINDERS] = reminderJson.encodeToString(current)
        }
    }

    suspend fun isSensitivityAsthmaEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SENSITIVITY_ASTHMA] ?: false

    suspend fun isSensitivityKidsEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SENSITIVITY_KIDS] ?: false

    suspend fun isSensitivityPregnancyEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SENSITIVITY_PREGNANCY] ?: false

    suspend fun isSensitivityNightSafetyEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SENSITIVITY_NIGHT_SAFETY] ?: false

    suspend fun isSensitivityWomanEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SENSITIVITY_WOMAN] ?: false

    suspend fun setSensitivityWoman(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENSITIVITY_WOMAN] = enabled }
    }

    suspend fun setSensitivityAsthma(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENSITIVITY_ASTHMA] = enabled }
    }

    suspend fun setSensitivityKids(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENSITIVITY_KIDS] = enabled }
    }

    suspend fun setSensitivityPregnancy(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENSITIVITY_PREGNANCY] = enabled }
    }

    suspend fun setSensitivityNightSafety(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENSITIVITY_NIGHT_SAFETY] = enabled }
    }

    suspend fun getTripWizardDraft(): TripWizardDraft? {
        val raw = context.dataStore.data.first()[KEY_TRIP_WIZARD_DRAFT] ?: return null
        return runCatching { reminderJson.decodeFromString<TripWizardDraft>(raw) }.getOrNull()
    }

    suspend fun saveTripWizardDraft(draft: TripWizardDraft) {
        context.dataStore.edit {
            it[KEY_TRIP_WIZARD_DRAFT] = reminderJson.encodeToString(draft)
        }
    }

    suspend fun clearTripWizardDraft() {
        context.dataStore.edit { it.remove(KEY_TRIP_WIZARD_DRAFT) }
    }

    suspend fun toggleVerdict(baseId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DISABLED_VERDICTS]?.toMutableSet() ?: mutableSetOf()
            if (baseId in current) current.remove(baseId) else current.add(baseId)
            prefs[KEY_DISABLED_VERDICTS] = current
        }
    }

    suspend fun toggleModeVerdict(modeId: String, verdictId: String) {
        val key = modeHiddenKey(modeId)
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.toMutableSet() ?: mutableSetOf()
            if (verdictId in current) current.remove(verdictId) else current.add(verdictId)
            prefs[key] = current
        }
    }

    suspend fun getHiddenVerdictIdsForMode(modeId: String): Set<String> {
        val key = modeHiddenKey(modeId)
        return context.dataStore.data.first()[key] ?: emptySet()
    }

    suspend fun isVerdictEnabled(baseId: String): Boolean {
        val disabled = context.dataStore.data.first()[KEY_DISABLED_VERDICTS] ?: emptySet()
        return baseId !in disabled
    }

    suspend fun getDisabledVerdictIds(): Set<String> =
        context.dataStore.data.first()[KEY_DISABLED_VERDICTS] ?: emptySet()

    suspend fun saveFarmerProfile(profile: com.kosmos.shared.farmer.FarmerProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FARMER_CROPS] = profile.crops
            profile.sowingDateIso?.let { prefs[KEY_FARMER_SOWING] = it }
            profile.plotLat?.let { prefs[KEY_FARMER_PLOT_LAT] = it }
            profile.plotLon?.let { prefs[KEY_FARMER_PLOT_LON] = it }
            profile.plotCity?.let { prefs[KEY_FARMER_PLOT_CITY] = it }
            prefs[KEY_FARMER_LANG] = profile.languageCode
        }
    }

    suspend fun getFarmerProfile(): com.kosmos.shared.farmer.FarmerProfile {
        val prefs = context.dataStore.data.first()
        return com.kosmos.shared.farmer.FarmerProfile(
            crops = prefs[KEY_FARMER_CROPS] ?: emptySet(),
            sowingDateIso = prefs[KEY_FARMER_SOWING],
            plotLat = prefs[KEY_FARMER_PLOT_LAT],
            plotLon = prefs[KEY_FARMER_PLOT_LON],
            plotCity = prefs[KEY_FARMER_PLOT_CITY],
            languageCode = prefs[KEY_FARMER_LANG] ?: "te",
        )
    }

    suspend fun saveEmployeeProfile(profile: com.kosmos.shared.mode.EmployeeProfile) {
        context.dataStore.edit { prefs ->
            profile.homeLabel?.let { prefs[KEY_EMP_HOME_LABEL] = it }
            profile.homeLat?.let { prefs[KEY_EMP_HOME_LAT] = it }
            profile.homeLon?.let { prefs[KEY_EMP_HOME_LON] = it }
            profile.workLabel?.let { prefs[KEY_EMP_WORK_LABEL] = it }
            profile.workLat?.let { prefs[KEY_EMP_WORK_LAT] = it }
            profile.workLon?.let { prefs[KEY_EMP_WORK_LON] = it }
        }
    }

    suspend fun getEmployeeProfile(): com.kosmos.shared.mode.EmployeeProfile {
        val prefs = context.dataStore.data.first()
        return com.kosmos.shared.mode.EmployeeProfile(
            homeLabel = prefs[KEY_EMP_HOME_LABEL],
            homeLat = prefs[KEY_EMP_HOME_LAT],
            homeLon = prefs[KEY_EMP_HOME_LON],
            workLabel = prefs[KEY_EMP_WORK_LABEL],
            workLat = prefs[KEY_EMP_WORK_LAT],
            workLon = prefs[KEY_EMP_WORK_LON],
        )
    }

    suspend fun appendNotification(entry: NotificationEntry) {
        val safeTitle = entry.title.replace("|", " ").replace("\n", " ")
        val safeBody = entry.body.replace("|", " ").replace("\n", " ")
        val line = "${entry.timestampMs}|${entry.type}|$safeTitle|$safeBody"
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_NOTIFICATIONS]?.split("\n")?.toMutableList() ?: mutableListOf()
            current.add(0, line)
            prefs[KEY_NOTIFICATIONS] = current.take(40).joinToString("\n")
        }
    }

    suspend fun getNotifications(): List<NotificationEntry> {
        val raw = context.dataStore.data.first()[KEY_NOTIFICATIONS] ?: return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("|", limit = 4)
            if (parts.size < 4) return@mapNotNull null
            NotificationEntry(
                id = "${parts[1]}_${parts[0]}",
                timestampMs = parts[0].toLongOrNull() ?: return@mapNotNull null,
                type = parts[1],
                title = parts[2],
                body = parts[3],
            )
        }
    }

    suspend fun getNotificationsUnreadCount(): Int {
        val lastSeen = context.dataStore.data.first()[KEY_NOTIFICATIONS_SEEN_AT] ?: 0L
        return getNotifications().count { it.timestampMs > lastSeen }
    }

    suspend fun markNotificationsSeen() {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_SEEN_AT] = System.currentTimeMillis()
        }
    }

    suspend fun setWeeklyDigestEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WEEKLY_DIGEST] = enabled }
    }

    suspend fun isWeeklyDigestEnabled(): Boolean =
        context.dataStore.data.first()[KEY_WEEKLY_DIGEST] ?: true

    suspend fun addDiaryEntry(entry: String) {
        val today = java.time.LocalDate.now().toString()
        val line = "$today|$entry"
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIARY_ENTRIES]?.split("\n")?.toMutableList() ?: mutableListOf()
            current.add(0, line)
            prefs[KEY_DIARY_ENTRIES] = current.take(30).joinToString("\n")
        }
    }

    suspend fun getDiaryEntries(): List<Pair<String, String>> {
        val raw = context.dataStore.data.first()[KEY_DIARY_ENTRIES] ?: return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    suspend fun saveCachedWeather(payload: CachedWeatherPayload) {
        runCatching {
            context.dataStore.edit { prefs ->
                prefs[KEY_WEATHER_CACHE] = weatherCacheJson.encodeToString(payload)
            }
        }
    }

    suspend fun getCachedWeather(): CachedWeatherPayload? {
        val raw = context.dataStore.data.first()[KEY_WEATHER_CACHE] ?: return null
        return runCatching { weatherCacheJson.decodeFromString<CachedWeatherPayload>(raw) }.getOrNull()
    }

    suspend fun saveLocation(geo: GeoLocation) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CITY] = geo.city
            prefs[KEY_LAT] = geo.latitude
            prefs[KEY_LON] = geo.longitude
            if (geo.neighborhood.isNullOrBlank()) {
                prefs.remove(KEY_NEIGHBORHOOD)
            } else {
                prefs[KEY_NEIGHBORHOOD] = geo.neighborhood
            }
            if (geo.subArea.isNullOrBlank()) {
                prefs.remove(KEY_SUB_AREA)
            } else {
                prefs[KEY_SUB_AREA] = geo.subArea
            }
            if (geo.country.isNullOrBlank()) {
                prefs.remove(KEY_COUNTRY)
            } else {
                prefs[KEY_COUNTRY] = geo.country
            }
        }
    }

    suspend fun isUsingLiveGps(): Boolean =
        context.dataStore.data.first()[KEY_USE_LIVE_GPS] ?: true

    suspend fun setUsingLiveGps(useLive: Boolean) {
        context.dataStore.edit { it[KEY_USE_LIVE_GPS] = useLive }
    }

    suspend fun isOnboardingDone(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING_DONE] ?: false

    suspend fun setOnboardingDone(done: Boolean = true) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun isMorningBriefEnabled(): Boolean =
        context.dataStore.data.first()[KEY_MORNING_BRIEF] ?: true

    suspend fun setMorningBriefEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MORNING_BRIEF] = enabled }
    }

    suspend fun getSavedPlaces(): List<SavedPlace> {
        val raw = context.dataStore.data.first()[KEY_SAVED_PLACES] ?: return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 4) {
                SavedPlace(
                    id = parts[0],
                    label = parts[1],
                    latitude = parts[2].toDoubleOrNull() ?: return@mapNotNull null,
                    longitude = parts[3].toDoubleOrNull() ?: return@mapNotNull null,
                    city = parts.getOrElse(4) { parts[1] },
                )
            } else null
        }
    }

    suspend fun addSavedPlace(place: SavedPlace) {
        val current = getSavedPlaces().toMutableList()
        if (current.any { it.id == place.id }) return
        current += place
        persistSavedPlaces(current)
    }

    suspend fun removeSavedPlace(id: String) {
        persistSavedPlaces(getSavedPlaces().filter { it.id != id })
    }

    private suspend fun persistSavedPlaces(places: List<SavedPlace>) {
        val encoded = places.joinToString("\n") {
            "${it.id}|${it.label}|${it.latitude}|${it.longitude}|${it.city}"
        }
        context.dataStore.edit { it[KEY_SAVED_PLACES] = encoded }
    }

    fun verdictCategories(mode: UserMode): List<VerdictCategory> {
        val toggles = modeVerdictToggles(mode).associate { it.first to it.second }
        fun pick(vararg ids: String) = ids.mapNotNull { id -> toggles[id]?.let { id to it } }
        return listOf(
            VerdictCategory("Rain & sky", pick("raincoat", "umbrella", "wind")),
            VerdictCategory("Heat & sun", pick("heat", "sunProtection", "avoidHours", "hydration")),
            VerdictCategory("Air & health", pick("air", "cold", "mosquito")),
            VerdictCategory("Activities", pick("bestWalk", "canJog", "vitaminD", "pleasant")),
            VerdictCategory("Home", pick("openWindows", "closeWindows", "laundry")),
            VerdictCategory("Timing", pick("goldenHour", "coolerTomorrow", "easy")),
        ).filter { it.toggles.isNotEmpty() }
    }

    suspend fun getSavedLocation(): SavedLocation? {
        val prefs = context.dataStore.data.first()
        val city = prefs[KEY_CITY] ?: return null
        val lat = prefs[KEY_LAT] ?: return null
        val lon = prefs[KEY_LON] ?: return null
        if (LocationRepository.isDefaultCoords(lat, lon)) return null
        return SavedLocation(
            city = city,
            neighborhood = prefs[KEY_NEIGHBORHOOD],
            subArea = prefs[KEY_SUB_AREA],
            country = prefs[KEY_COUNTRY],
            latitude = lat,
            longitude = lon,
        )
    }

    suspend fun saveHomeLocation(home: HomeLocation) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_CITY] = home.city
            prefs[KEY_HOME_LAT] = home.latitude
            prefs[KEY_HOME_LON] = home.longitude
            if (home.neighborhood.isNullOrBlank()) prefs.remove(KEY_HOME_NEIGHBORHOOD)
            else prefs[KEY_HOME_NEIGHBORHOOD] = home.neighborhood
            if (home.country.isNullOrBlank()) prefs.remove(KEY_HOME_COUNTRY)
            else prefs[KEY_HOME_COUNTRY] = home.country
        }
    }

    suspend fun getHomeLocation(): HomeLocation? {
        val prefs = context.dataStore.data.first()
        val city = prefs[KEY_HOME_CITY] ?: return null
        val lat = prefs[KEY_HOME_LAT] ?: return null
        val lon = prefs[KEY_HOME_LON] ?: return null
        return HomeLocation(
            city = city,
            neighborhood = prefs[KEY_HOME_NEIGHBORHOOD],
            country = prefs[KEY_HOME_COUNTRY],
            latitude = lat,
            longitude = lon,
        )
    }

    suspend fun getChatMessagesRemaining(): Int {
        if (isKosmosPlusActive()) return Int.MAX_VALUE
        val prefs = context.dataStore.data.first()
        val today = java.time.LocalDate.now().toString()
        val count = if (prefs[KEY_CHAT_DATE] == today) prefs[KEY_CHAT_COUNT] ?: 0 else 0
        return (FREE_CHAT_DAILY - count).coerceAtLeast(0)
    }

    suspend fun consumeChatMessage(): Boolean {
        if (isKosmosPlusActive()) return true
        val today = java.time.LocalDate.now().toString()
        var allowed = false
        context.dataStore.edit { prefs ->
            val storedDate = prefs[KEY_CHAT_DATE]
            val count = if (storedDate == today) prefs[KEY_CHAT_COUNT] ?: 0 else 0
            if (count >= FREE_CHAT_DAILY) {
                allowed = false
                return@edit
            }
            prefs[KEY_CHAT_DATE] = today
            prefs[KEY_CHAT_COUNT] = count + 1
            allowed = true
        }
        return allowed
    }

    fun modeVerdictToggles(mode: UserMode): List<Pair<String, String>> = when (mode) {
        UserMode.DEFAULT -> allVerdictToggles
        UserMode.ELDER -> listOf(
            "raincoat" to "Rain alerts",
            "wind" to "Wind and gusts",
            "heat" to "Heat warnings",
            "bestWalk" to "Best walk times",
        )
        UserMode.EMPLOYEE -> employeeVerdictToggles
        UserMode.FAMILY -> familyVerdictToggles
        UserMode.PHOTOGRAPHER -> photographerVerdictToggles
        UserMode.FARMER -> listOf("spray" to "Spray window (v3.0)")
        UserMode.HOMEMAKER -> homemakerVerdictToggles
    }

    val refreshIntervalMinutes: Flow<Int> = context.dataStore.data.map { it[KEY_REFRESH_INTERVAL] ?: 30 }

    suspend fun getRefreshIntervalMinutes(): Int =
        context.dataStore.data.first()[KEY_REFRESH_INTERVAL] ?: 30

    suspend fun setRefreshIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_REFRESH_INTERVAL] = minutes }
    }

    companion object {
        const val ALPHA_ALL_FREE = true
        const val MAX_ADDED_MODES = 5
        const val MAX_ACTIVE_LENSES = 2
        const val FREE_REMINDERS_DAILY = 2
        private val KEY_CELSIUS = booleanPreferencesKey("use_celsius")
        private val KEY_24HOUR = booleanPreferencesKey("use_24hour")
        private val KEY_DARK_MODE = booleanPreferencesKey("use_dark_mode")
        private val KEY_SHOW_NUMBERS = booleanPreferencesKey("show_numbers")
        private val KEY_LOCALE = stringPreferencesKey("app_locale")
        private val KEY_USER_MODE = stringPreferencesKey("user_mode")
        private val KEY_ACTIVE_MODES = stringSetPreferencesKey("active_modes")
        private val KEY_ACTIVE_MODES_ORDER = stringPreferencesKey("active_modes_order")
        private val KEY_COMMUTE = stringSetPreferencesKey("commute_modes")
        private val KEY_KOSMOS_PLUS = booleanPreferencesKey("kosmos_plus")
        private val KEY_ADDED_MODES = stringSetPreferencesKey("added_modes")
        private val KEY_REMINDER_IDS = stringSetPreferencesKey("reminder_ids")
        private val KEY_REMINDER_DATE = stringPreferencesKey("reminder_date")
        private val KEY_REMINDER_COUNT = intPreferencesKey("reminder_count")
        private val KEY_REFRESH_INTERVAL = intPreferencesKey("refresh_interval_minutes")

        val verdictToggleDescriptions = mapOf(
            "raincoat" to "Heavy rain in the next 12 hours",
            "umbrella" to "Light rain or drizzle ahead",
            "heat" to "Dangerous or extreme heat",
            "sunProtection" to "High UV — SPF and shade",
            "cold" to "Cold snap or wind chill",
            "air" to "Air quality rough or worse",
            "vitaminD" to "Morning + afternoon sun windows",
            "bestWalk" to "Best times to walk outside",
            "avoidHours" to "Hours to stay indoors",
            "canJog" to "Good window for a jog",
            "hydration" to "Daily water goal",
            "mosquito" to "Peak mosquito hours",
            "openWindows" to "Fresh air window",
            "closeWindows" to "Keep pollution out",
            "laundry" to "Good drying weather",
            "goldenHour" to "Sunrise and sunset light",
            "coolerTomorrow" to "Tomorrow cooler than today",
        )

        val allVerdictToggles = listOf(
            "raincoat" to "Rain alerts",
            "umbrella" to "Umbrella reminders",
            "heat" to "Heat warnings",
            "sunProtection" to "Sun protection",
            "cold" to "Cold weather",
            "air" to "Air quality",
            "vitaminD" to "Vitamin D windows",
            "bestWalk" to "Best walk times",
            "avoidHours" to "Avoid outdoors",
            "canJog" to "Jog conditions",
            "hydration" to "Hydration goals",
            "mosquito" to "Mosquito hour",
            "openWindows" to "Open windows",
            "closeWindows" to "Keep windows shut",
            "laundry" to "Good drying weather",
            "pleasant" to "Pleasant day",
            "goldenHour" to "Golden hour",
            "coolerTomorrow" to "Cooler tomorrow",
        )

        val employeeVerdictToggles = listOf(
            "workAir" to "Office air quality",
            "workRainEvening" to "Rain at work",
            "commuteOut" to "Morning commute",
            "deskSun" to "Desk sun window",
            "lunchWalk" to "Lunch walk",
            "screenBreak" to "Screen break",
            "commuteBack" to "Evening commute",
            "gymWindow" to "Gym window",
            "laundryWeekend" to "Weekend laundry",
        )

        val familyVerdictToggles = listOf(
            "schoolUniform" to "School uniform",
            "sunscreen" to "Sunscreen",
            "peClass" to "PE class",
            "kidsHydration" to "Kids hydration",
            "pickup" to "School pickup",
            "playtime" to "Evening play",
            "kidsAir" to "Kids air quality",
            "tiffin" to "Tiffin tips",
        )

        val photographerVerdictToggles = listOf(
            "goldenHour.sunrise" to "Sunrise golden hour",
            "goldenHour.sunset" to "Sunset golden hour",
            "blueHour" to "Blue hour",
            "cumulus" to "Cumulus clouds",
            "fog" to "Fog shots",
            "rainbow" to "Rainbow watch",
            "stars" to "Stargazing",
        )

        val homemakerVerdictToggles = listOf(
            "homePreserve" to "Pickle / drying spell",
            "homeStormPrep" to "Pre-storm prep",
            "homeDrying" to "Clothes drying",
            "homeSpoilage" to "Food spoilage",
            "homeVegShopping" to "Veg before rain",
            "homeMosquito" to "Mosquito hour",
        )

        private val KEY_DISABLED_VERDICTS = stringSetPreferencesKey("disabled_verdicts")
        private val KEY_CITY = stringPreferencesKey("city")
        private val KEY_NEIGHBORHOOD = stringPreferencesKey("neighborhood")
        private val KEY_SUB_AREA = stringPreferencesKey("sub_area")
        private val KEY_COUNTRY = stringPreferencesKey("country")
        private val KEY_HOME_CITY = stringPreferencesKey("home_city")
        private val KEY_HOME_NEIGHBORHOOD = stringPreferencesKey("home_neighborhood")
        private val KEY_HOME_COUNTRY = stringPreferencesKey("home_country")
        private val KEY_HOME_LAT = doublePreferencesKey("home_lat")
        private val KEY_HOME_LON = doublePreferencesKey("home_lon")
        private val KEY_LAT = doublePreferencesKey("lat")
        private val KEY_LON = doublePreferencesKey("lon")
        private val KEY_USE_LIVE_GPS = booleanPreferencesKey("use_live_gps")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_MORNING_BRIEF = booleanPreferencesKey("morning_brief_enabled")
        private val KEY_SAVED_PLACES = stringPreferencesKey("saved_places")
        private val KEY_CHAT_DATE = stringPreferencesKey("chat_date")
        private val KEY_CHAT_COUNT = intPreferencesKey("chat_count")
        private val KEY_FARMER_CROPS = stringSetPreferencesKey("farmer_crops")
        private val KEY_FARMER_SOWING = stringPreferencesKey("farmer_sowing")
        private val KEY_FARMER_PLOT_LAT = doublePreferencesKey("farmer_plot_lat")
        private val KEY_FARMER_PLOT_LON = doublePreferencesKey("farmer_plot_lon")
        private val KEY_FARMER_PLOT_CITY = stringPreferencesKey("farmer_plot_city")
        private val KEY_FARMER_LANG = stringPreferencesKey("farmer_lang")
        private val KEY_EMP_HOME_LABEL = stringPreferencesKey("emp_home_label")
        private val KEY_EMP_HOME_LAT = doublePreferencesKey("emp_home_lat")
        private val KEY_EMP_HOME_LON = doublePreferencesKey("emp_home_lon")
        private val KEY_EMP_WORK_LABEL = stringPreferencesKey("emp_work_label")
        private val KEY_EMP_WORK_LAT = doublePreferencesKey("emp_work_lat")
        private val KEY_EMP_WORK_LON = doublePreferencesKey("emp_work_lon")
        private val KEY_NOTIFICATIONS = stringPreferencesKey("notification_log")
        private val KEY_NOTIFICATIONS_SEEN_AT = longPreferencesKey("notifications_seen_at")
        private val KEY_WEEKLY_DIGEST = booleanPreferencesKey("weekly_digest")
        private val KEY_DIARY_ENTRIES = stringPreferencesKey("weather_diary_entries")
        private val KEY_WEATHER_CACHE = stringPreferencesKey("weather_cache_json")
        private val KEY_SCHEDULED_REMINDERS = stringPreferencesKey("scheduled_reminders")
        private val KEY_SENSITIVITY_ASTHMA = booleanPreferencesKey("sensitivity_asthma")
        private val KEY_SENSITIVITY_KIDS = booleanPreferencesKey("sensitivity_kids")
        private val KEY_SENSITIVITY_PREGNANCY = booleanPreferencesKey("sensitivity_pregnancy")
        private val KEY_SENSITIVITY_NIGHT_SAFETY = booleanPreferencesKey("sensitivity_night_safety")
        private val KEY_SENSITIVITY_WOMAN = booleanPreferencesKey("sensitivity_woman")
        private val KEY_TRIP_WIZARD_DRAFT = stringPreferencesKey("trip_wizard_draft")

        private val weatherCacheJson = Json { ignoreUnknownKeys = true }
        private val reminderJson = Json { ignoreUnknownKeys = true }

        const val FREE_CHAT_DAILY = 5

        private fun modeHiddenKey(modeId: String) =
            stringSetPreferencesKey("hidden_verdicts_$modeId")
    }
}

data class SavedLocation(
    val city: String,
    val neighborhood: String?,
    val subArea: String? = null,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
)

data class SavedPlace(
    val id: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
)

data class VerdictCategory(
    val title: String,
    val toggles: List<Pair<String, String>>,
)
