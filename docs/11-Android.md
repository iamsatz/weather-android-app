# 11 — Android Architecture

> Everything needed to start the Kosmos Android app from scratch.
> Kotlin + Jetpack Compose + Glance widgets + KMP shared logic.

---

## 1. The Android philosophy for Kosmos

Android users behave differently from iOS users:

- They **live in widgets** — the home screen is their dashboard
- They expect **more customization** — widget resize, multiple sizes
- They use **notifications more** — less likely to open the app every day
- They're on **more diverse hardware** — small Redmi phones to large Pixels

**Design decision:** For Android, the widget IS the product for most users. The app is the settings panel + detail view. Build widget-first.

---

## 2. Tech stack

| Layer | Technology | Why |
|---|---|---|
| Language | Kotlin | Native, modern, similar to Swift |
| UI | Jetpack Compose | Declarative, same mental model as SwiftUI |
| Widgets | Glance (Jetpack) | Compose-based widget framework |
| Shared logic | Kotlin Multiplatform (KMP) | Verdict engine + models shared with iOS |
| Local storage | DataStore (Preferences) | UserDefaults equivalent; async |
| Cache | Room (SQLite) | Optional; use if forecast caching grows complex |
| Background refresh | WorkManager | Periodic weather updates every 30 min |
| Location | FusedLocationProviderClient | Battery-efficient; Google's recommended API |
| Geocoding | Geocoder (Android) or Google Geocoding API | City name ↔ lat/lon |
| Notifications | NotificationManager + WorkManager | Rain alerts, morning brief |
| AI chat | Pollinations.ai (same endpoint) | Identical to iOS — no changes needed |
| Build | Gradle (Kotlin DSL) | Standard Android build |
| Min SDK | 26 (Android 8.0 Oreo, 2017) | Covers 94% of active Android devices |
| Target SDK | 35 (Android 15) | Latest stable |

---

## 3. KMP — what gets shared between iOS and Android

```
┌─────────────────────────────────────────────────────┐
│                  shared/ (KMP module)                │
│                                                       │
│  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │  Models          │  │  Verdict Engine           │  │
│  │  WeatherSnapshot │  │  evaluate(_ snapshot)     │  │
│  │  Verdict         │  │  → [Verdict]              │  │
│  │  HourlyData      │  │                           │  │
│  │  DailyData       │  │  All 17 rules             │  │
│  │  AQIData         │  │  Same thresholds          │  │
│  └──────────────────┘  └──────────────────────────┘  │
│                                                       │
│  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │  Open-Meteo API  │  │  Plain Language           │  │
│  │  client          │  │  UV 6 → "Sun is brutal"   │  │
│  │  (ktor HTTP)     │  │  (same copy on both)      │  │
│  └──────────────────┘  └──────────────────────────┘  │
└─────────────────────────────────────────────────────┘
           ↙                              ↘
    iOS (Swift)                    Android (Kotlin)
    SwiftUI views                  Compose views
    WidgetKit                      Glance widgets
    CoreLocation                   FusedLocationProvider
    CLGeocoder                     Android Geocoder
```

**What does NOT get shared:**
- UI code (SwiftUI vs Compose are completely different)
- Platform APIs (location, notifications, storage)
- Widget code (WidgetKit vs Glance are completely different)

**KMP transport note:** Use `ktor` HTTP client in shared module (it's multiplatform). Both iOS and Android call the same `OpenMeteoClient.kt` in shared.

---

## 4. Project structure (full)

```
Kosmos/
├── shared/                              ← KMP module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/com/kosmos/shared/
│       │       ├── models/
│       │       │   ├── WeatherSnapshot.kt   ← all weather data in one struct
│       │       │   ├── Verdict.kt           ← id, title, detail, priority, window
│       │       │   ├── HourlyData.kt
│       │       │   ├── DailyData.kt
│       │       │   └── AQIData.kt
│       │       ├── engine/
│       │       │   ├── VerdictEngine.kt     ← THE BRAIN — 17 rules
│       │       │   └── PlainLanguage.kt     ← numbers → sentences
│       │       ├── api/
│       │       │   ├── OpenMeteoClient.kt   ← primary weather
│       │       │   └── TomorrowClient.kt    ← confidence layer (slice 19)
│       │       └── util/
│       │           └── TimeUtils.kt         ← sunrise/sunset math (shared)
│       ├── androidMain/
│       │   └── kotlin/com/kosmos/shared/
│       │       └── PlatformActual.android.kt   ← Android-specific implementations
│       └── iosMain/
│           └── kotlin/com/kosmos/shared/
│               └── PlatformActual.ios.kt       ← iOS-specific implementations
│
└── androidApp/                          ← Android-only module
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        └── kotlin/com/kosmos/android/
            │
            ├── KosmosApplication.kt         ← Application class; init DI
            ├── MainActivity.kt              ← single activity; Compose NavHost
            │
            ├── ui/
            │   ├── theme/
            │   │   ├── Color.kt             ← all 18 condition palettes
            │   │   ├── Type.kt              ← Inter font + size scale
            │   │   └── Theme.kt             ← KosmosTheme (light + dark)
            │   │
            │   ├── home/
            │   │   ├── HomeScreen.kt        ← main screen
            │   │   ├── HomeViewModel.kt     ← observes WeatherRepository
            │   │   ├── VerdictCard.kt       ← single verdict row
            │   │   ├── HourlyStrip.kt       ← horizontal scroll of next 12h
            │   │   └── CharacterView.kt     ← animated character (Canvas API)
            │   │
            │   ├── timeline/
            │   │   ├── TimelineScreen.kt    ← vertical day view
            │   │   └── TimelineViewModel.kt
            │   │
            │   ├── chat/
            │   │   ├── ChatScreen.kt        ← AI chat
            │   │   └── ChatViewModel.kt     ← Pollinations.ai calls
            │   │
            │   ├── settings/
            │   │   ├── SettingsScreen.kt    ← verdict toggles + prefs
            │   │   └── SettingsViewModel.kt
            │   │
            │   └── components/
            │       ├── WeatherBackground.kt  ← gradient canvas per condition
            │       ├── AqiBadge.kt           ← colored pill
            │       └── CitySearchSheet.kt    ← bottom sheet city picker
            │
            ├── widget/
            │   ├── SmallWidget.kt           ← 1×1: temp + condition icon
            │   ├── MediumWidget.kt          ← 2×1: temp + 2 verdicts (PRIORITY)
            │   ├── LargeWidget.kt           ← 4×2: 4 verdicts + hourly
            │   ├── ExtraLargeWidget.kt      ← 4×4: full timeline
            │   └── WidgetReceiver.kt        ← GlanceAppWidgetReceiver; all sizes
            │
            ├── data/
            │   ├── WeatherRepository.kt     ← orchestrates API + cache + verdicts
            │   ├── LocationRepository.kt    ← FusedLocation + geocoding
            │   ├── PreferencesRepository.kt ← DataStore wrapper
            │   └── WeatherCache.kt          ← last-good snapshot persistence
            │
            ├── service/
            │   ├── WeatherRefreshWorker.kt  ← WorkManager; runs every 30 min
            │   └── RainAlertWorker.kt       ← checks for upcoming rain hourly
            │
            └── notification/
                ├── MorningBriefWorker.kt    ← 7 AM daily notification
                ├── RainNotification.kt      ← "Rain in 60 min" push
                └── NotificationChannels.kt  ← creates all notification channels
```

---

## 5. The 4 widget sizes — content rules

### Size 1: Small (1×1) — 112×112 dp

```
┌──────────────┐
│  🌤          │
│              │
│  34°         │
│  ☔ Rain 4PM  │
└──────────────┘
```

**Content:**
- Weather condition icon (top left, 24dp)
- Temperature (hero, 32sp ultralight)
- Top 1 verdict (icon + 2-word label)
- Taps to → open app HomeScreen

**Rules:** No verdict detail text. No city name (too small). Condition icon only, no animation.

---

### Size 2: Medium (2×1) — 238×112 dp — PRIORITY, build first

```
┌─────────────────────────────────────┐
│ Hyderabad · 34°C     🌤 Partly cloudy│
│                                      │
│ ☔ Rain at 4 PM       🚶 Walk: 6–7 PM │
└─────────────────────────────────────┘
```

**Content:**
- City + temperature (header row)
- Condition name (header row, right)
- 2 top verdicts side by side (icon + short title)
- Taps to → open app HomeScreen

**Rules:** Title only, no detail. If only 1 verdict firing, show it full width. Never show AQI number — show badge color only.

---

### Size 3: Large (4×2) — 238×238 dp

```
┌─────────────────────────────────────┐
│ Hyderabad                      34°C │
│ 🌤 Partly cloudy · AQI ●Good         │
│                                      │
│ ☔ Rain at 4 PM, 73% chance           │
│ 🚶 Best walk: 6–7 PM                 │
│ 🦟 Mosquito hour after 7 PM          │
│ 💧 Drink extra water today           │
│                                      │
│ 3PM  4PM  5PM  6PM  7PM  8PM  9PM   │
│  34°  32°  31°  29°  28°  27°  26°  │
│  🌤    ☔   ☔   🌤   🌤   🌤   🌙   │
└─────────────────────────────────────┘
```

**Content:**
- City + temp + condition + AQI badge (header)
- Top 4 verdicts (icon + full title, no detail)
- Hourly strip: next 7 hours (temp + condition icon)

**Rules:** Verdict detail text only if 2 or fewer verdicts. Scroll-within-widget NOT allowed (Android limitation) — show top 4 only.

---

### Size 4: Extra Large (4×4) — 238×412 dp

```
┌─────────────────────────────────────┐
│ Hyderabad · Thursday               │
│ 34°C · 🌤 Partly cloudy · AQI ●Good │
│                                      │
│ ──── INSIGHTS ────                   │
│ ☔ Rain at 4 PM, 73%                 │
│ 🚶 Walk window: 6–7 PM              │
│ 🦟 Mosquito hour after 7 PM         │
│ 💧 Drink extra water                 │
│ 🌅 Golden hour: 6:42 PM             │
│                                      │
│ ──── TODAY ────                      │
│ 5AM  ···  9AM 🌤 Vit D window        │
│ 11AM ···  4PM 🔥 Avoid outdoors      │
│ 4PM  ···  5PM ☔ Rain                 │
│ 6PM  ···  7PM 🚶 Best walk           │
│ 6:42PM       🌅 Golden hour          │
└─────────────────────────────────────┘
```

**Content:**
- Full Insights section (all firing verdicts)
- Mini Day Timeline (verdict blocks on a vertical line)

**Rules:** This widget is ambitious — build last. The timeline must be static (no animation). Verdict count capped at 6 in widget (show rest in app).

---

## 6. Background refresh — how weather stays current

### WorkManager setup

```kotlin
// Enqueue on app start and after widget added
val refreshRequest = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(
    30, TimeUnit.MINUTES,
    5, TimeUnit.MINUTES          // flex window
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "weather_refresh",
    ExistingPeriodicWorkPolicy.KEEP,
    refreshRequest
)
```

### WeatherRefreshWorker does:

1. Get current location (FusedLocationProvider, last known — no fresh GPS)
2. Call OpenMeteoClient (from shared KMP module)
3. Run VerdictEngine (from shared KMP module)
4. Write to DataStore + update Glance widget state
5. If rain detected in next 60 min → trigger RainAlertWorker
6. Done

**Battery note:** WorkManager respects Doze mode. On Android 12+, battery-restricted apps get throttled. Default 30 min is safe; users on strict battery saver get 1–2h intervals.

---

## 7. Notifications setup

### Notification channels (create once on app start)

| Channel ID | Name | Importance | For |
|---|---|---|---|
| `morning_brief` | Morning Brief | HIGH | 7 AM daily summary |
| `rain_alert` | Rain Alert | HIGH | "Rain in 60 min" |
| `severe_weather` | Severe Weather | URGENT | IMD warnings |
| `reminders` | Vit D & Walk Reminders | DEFAULT | Wellness nudges |

### Morning brief (7 AM)

```kotlin
// Scheduled via WorkManager at 7 AM daily (or user-set time)
class MorningBriefWorker : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val snapshot = weatherRepo.getLatestSnapshot()
        val verdicts = VerdictEngine.evaluate(snapshot)  // shared KMP
        val top3 = verdicts.take(3)

        val notification = NotificationCompat.Builder(context, "morning_brief")
            .setSmallIcon(R.drawable.ic_kosmos)
            .setContentTitle("Good morning, Hyderabad · ${snapshot.temp}°C")
            .setContentText(top3.joinToString(" · ") { it.title })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(top3.joinToString("\n") { "${it.emoji} ${it.title}: ${it.detail}" }))
            .build()

        NotificationManagerCompat.from(context).notify(MORNING_ID, notification)
        return Result.success()
    }
}
```

**Result looks like:**
```
Kosmos — Good morning, Hyderabad · 28°C
☔ Rain at 4 PM: 73% chance — carry the umbrella
🚶 Best walk: 6–7 PM · AQI 68
💧 Drink extra water — stays hot till 7 PM
```

---

## 8. Location handling on Android

### Flow

```
App opens
     │
Try FusedLocationProvider (last known, instant)
     │
     ├── Has cached location? → Use it. Refresh in background.
     │
     └── No cached? → Request current location (5s timeout)
                │
                ├── Got GPS? → Reverse geocode → show
                │
                └── Denied or timeout? → Try IP geolocation
                              │
                              ├── ipwho.is → use
                              └── ipapi.co → use as fallback
                                          │
                                          └── Hard default: Hyderabad
```

### Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- For widget background refresh — needed for Android 10+ -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<!-- Only request background if user enables "always on" widget -->
```

**Note:** Background location on Android 12+ requires explicit user action (Settings → Permissions → Allow all the time). Don't require it for v1 — widget refresh uses last known location.

---

## 9. HomeScreen — what the main screen looks like

```
┌─────────────────────────────────────────┐
│                                         │
│  Hyderabad · Thu Jun 19           ⚙ 🔍  │
│                                         │
│       [Character figure]                │
│       34°C    Partly cloudy             │
│       Feels like 38° · AQI 72 ●Good    │
│                                         │
│  ─── INSIGHTS ──────────────────────── │
│  ☔ Rain at 4 PM   73% — carry umbrella  │
│  🚶 Walk: 6–7 PM   AQI good, 29°C       │
│  🦟 Mosquito hour  after sunset          │
│  💧 Drink more     stays hot till 7 PM  │
│                                         │
│  ─── NEXT 12 HOURS ──────────────────── │
│  2PM 3PM 4PM 5PM 6PM 7PM 8PM 9PM       │
│   34° 33° 31° 30° 29° 28° 27° 26°      │
│    🌤  🌤  ☔  ☔  🌤  🌤  🌙  🌙      │
│                                         │
│  ─── TODAY ──────────────────────────── │
│  [Tap to see Day Timeline]              │
│                                         │
│  [Ask Kosmos] ←── floating button       │
└─────────────────────────────────────────┘
```

**Navigation:**
- Tap ⚙ → Settings
- Tap 🔍 → City search (bottom sheet)
- Tap "TODAY" → Timeline screen (full screen)
- Tap "Ask Kosmos" → Chat screen (bottom sheet)
- Tap any verdict row → expanded detail (in-place)

---

## 10. Day timeline on Android

Same logic as macOS DayTimeline.swift — translated to Compose.

```kotlin
@Composable
fun DayTimeline(verdicts: List<Verdict>) {
    val hours = (5..23)  // 5 AM to 11 PM

    LazyColumn {
        items(hours.toList()) { hour ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Hour label (left column, 48dp wide)
                Text(
                    text = formatHour(hour),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(48.dp)
                )
                // Verdict chips that overlap this hour
                Column {
                    verdicts
                        .filter { it.window?.contains(hour) == true }
                        .forEach { verdict ->
                            VerdictChip(verdict = verdict)
                        }
                }
            }
        }
    }
}
```

---

## 11. Theming — Compose equivalent of the pastel system

```kotlin
// Color.kt — 18 condition backgrounds (match macOS exactly)
object KosmosColors {
    val clearMorningTop = Color(0xFFFFE0B2)
    val clearMorningBottom = Color(0xFFF8B98A)

    val clearMiddayTop = Color(0xFF7BBAF4)
    val clearMiddayBottom = Color(0xFF3D87D4)

    val thunderstormTop = Color(0xFF2D2E45)
    val thunderstormBottom = Color(0xFF0A0B1A)
    // ... all 18 conditions
}

// WeatherBackground.kt — draws the gradient
@Composable
fun WeatherBackground(condition: WeatherCondition, content: @Composable () -> Unit) {
    val (topColor, bottomColor) = conditionToColors(condition)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
    ) {
        content()
    }
}
```

---

## 12. Character animation on Android

macOS used SwiftUI Canvas. Android uses Compose Canvas.

```kotlin
@Composable
fun CharacterFigure(condition: WeatherCondition, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    // Breathing animation (same as macOS: 2s, sine curve)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.scale(scale)) {
        // Draw character body
        // Condition-specific accessory (umbrella, sunglasses, coat...)
        // Expression changes per condition
        drawCharacter(condition = condition)
    }
}
```

**Reduce Motion:** Check `LocalContext.current.isReduceMotionEnabled` → if true, skip animation, show static character.

---

## 13. AI chat on Android

Same Pollinations.ai endpoint. Compose UI equivalent of `AskKosmosPanel.swift`.

```kotlin
// ChatViewModel.kt
class ChatViewModel(
    private val weatherRepo: WeatherRepository
) : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    var isLoading by mutableStateOf(false)

    fun send(userText: String) {
        messages.add(ChatMessage(role = "user", content = userText))
        isLoading = true

        viewModelScope.launch {
            val snapshot = weatherRepo.getLatestSnapshot()
            val systemPrompt = buildKosmosSystemPrompt()
            val contextBlock = buildContextBlock(snapshot)

            val response = PollinationsClient.chat(
                systemPrompt = systemPrompt,
                contextBlock = contextBlock,
                messages = messages.toList()
            )

            messages.add(ChatMessage(role = "assistant", content = response))
            isLoading = false
        }
    }
}
```

The `PollinationsClient` lives in the **shared KMP module** — same code on both iOS and Android.

---

## 14. Build and run

### First-time setup

```bash
# Install Android Studio (latest stable)
# Open androidApp/ as the project root

# Or build from CLI
./gradlew :androidApp:assembleDebug

# Install on connected device
./gradlew :androidApp:installDebug

# Run shared module tests
./gradlew :shared:test
```

### Gradle dependencies (key ones)

```kotlin
// shared/build.gradle.kts
kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets.commonMain.dependencies {
        implementation("io.ktor:ktor-client-core:2.3.7")
        implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    }
    sourceSets.androidMain.dependencies {
        implementation("io.ktor:ktor-client-android:2.3.7")
    }
    sourceSets.iosMain.dependencies {
        implementation("io.ktor:ktor-client-darwin:2.3.7")
    }
}

// androidApp/build.gradle.kts
dependencies {
    implementation(project(":shared"))

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")

    // Widgets
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}
```

---

## 15. Phase 3 — what ships at Android launch

### Must-have (day 1)

- [ ] HomeScreen with character + verdicts + hourly
- [ ] Medium widget (2×1) — most important
- [ ] Day timeline
- [ ] Settings (verdict toggles + mode picker)
- [ ] Morning notification (7 AM brief)
- [ ] Rain alert notification
- [ ] City search
- [ ] KMP shared module (verdict engine + models + API client)

### Should-have (at launch)

- [ ] Large widget (4×2)
- [ ] Small widget (1×1)
- [ ] AI chat
- [ ] Hindi localization (100M+ Android users read Hindi)
- [ ] Telugu localization

### Out (Phase 4)

- [ ] Extra large widget (4×4)
- [ ] Tomorrow.io confidence layer
- [ ] Farmer mode
- [ ] Family mode

---

## 16. Android-specific verdicts to add

Two verdicts that matter more on Android (more outdoor/commuter Android users in India):

| Verdict ID | Trigger | Copy |
|---|---|---|
| `autoRickshaw` | Rain prob > 60% in next 2h | "Book an auto now — rain coming in 2h" |
| `bike.commute` | Rain prob < 20% AND AQI < 80 AND temp < 32°C | "Good biking day — skip the auto" |

These are India-specific, commuter-specific. Skip on macOS/iOS v1. Add for Android India launch.

---

## 17. Quick-start prompt for a new Android session

Paste this at the start:

```
I'm building the Android version of Kosmos — a weather + wellness app.

The verdict engine + models + API client are shared via KMP (Kotlin Multiplatform).
The iOS app is already planned (same logic). I need to build:

1. KMP shared module: VerdictEngine, WeatherSnapshot, OpenMeteoClient
2. Android app: Jetpack Compose HomeScreen + Glance medium widget (2×1) first

Weather data: Open-Meteo (free, no key). AI: Pollinations.ai (free, no key).
Min SDK: 26. Target SDK: 35.

Full docs: /Users/apple/Documents/Claude Apps/Weather App/mac OS/docs/
Key files: 10-Master-Brief.md, 11-Android.md (this file), 03-Verdicts.md
```

---

*Start with the KMP shared module, then the medium widget, then the HomeScreen.*
*In that order — widget is the product for Android users.*
