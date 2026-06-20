# 10 — Kosmos Master Brief

> Single portable file. Everything decided so far. Take this to any new session and rebuild context instantly.
> Last updated: 2026-06-19

---

## 1. What is Kosmos?

A weather + wellness companion that **tells you what to do**, not just what the weather is.

**One-liner:**
> *"Apple Weather tells you it's 38°. Kosmos tells you to take an umbrella for shade, drink 3 L of water, step out for Vitamin D between 9:30 and 9:45, avoid the outdoors 11 AM–4 PM, and watch for mosquitoes after 6 PM."*

Same data. Different output. The difference is **interpretation**.

**Core promise:** Never show a number without telling the user what to do with it. "UV 6" means nothing. "Sun is brutal — 15 min max before noon, then SPF" means everything.

**Business model:** Free forever (no account, no ads). Pro tier at ₹99/mo for power features.

**Name:** Kosmos (Greek for "world / universe / order"). App is about bringing order to the chaos of weather.

---

## 2. Platforms — build order

```
Phase 1 (Now–Month 4)    macOS menu bar app → Mac App Store
Phase 2 (Month 5–7)      iOS app + widgets → App Store
Phase 3 (Month 8–10)     Android + Pro tier → Play Store
Phase 4 (Month 11+)      Decide: India deep-dive / Global / Health pivot
```

**macOS reference implementation: DONE** (18 slices shipped, archived in `../Kosmos-code-archive/`)

---

## 3. Five personas — who we build for

| Name | Who | Key need |
|---|---|---|
| **Ravi** | Urban commuter, 28, Hyderabad | "Will it rain on my bike?" |
| **Sneha** | Health-conscious mom, 34, Bengaluru | "Is it safe for my kids outside?" |
| **Krishnamurthy** | Farmer, 52, Kurnool | "When to spray? When to harvest?" |
| **Aisha** | Digital nomad, 29, travels India | "Best weather city right now?" |
| **Ramesh** | Retired teacher, 67, Visakhapatnam | "Simple, big, read to me in Telugu" |

---

## 4. The Verdict Engine — the core product

17 rules that fire based on weather data. Each verdict = plain-English action + time window.

### All 17 verdicts

| ID | Verdict | When it fires |
|---|---|---|
| `rain` | ☔ Heavy rain coming | Precip prob ≥ 70% |
| `umbrella` | ☂ Might rain | Precip prob 40–69% |
| `umbrella.light` | 🌂 Light chance of rain | Precip prob 30–39% |
| `heat` | 🔥 Dangerous heat | Feels-like ≥ 40°C |
| `heat.caution` | ☀ Hot today | Feels-like 35–39°C |
| `cold` | 🧥 Cold today | Feels-like ≤ 10°C |
| `vitaminD.morning` | 🌤 Vitamin D window | UV 2–5 before 10 AM |
| `vitaminD.afternoon` | 🌤 Afternoon Vit D | UV 2–5 between 2–4 PM |
| `bestWalk.morning` | 🚶 Best walk: morning | Daytime 5–10 AM, temp 18–32°C, AQI < 100 |
| `bestWalk.evening` | 🚶 Best walk: evening | Daytime 4–8 PM, temp 18–32°C, AQI < 100 |
| `avoidHours` | 🚫 Avoid outdoors | Feels-like ≥ 38°C during a window |
| `canJog` | 🏃 Good jog conditions | Temp ≤ 28°C, AQI < 80, no rain |
| `hydration` | 💧 Drink extra water | Temp > 35°C |
| `mosquito` | 🦟 Mosquito hour | After sunset, humidity > 70% |
| `openWindows` | 🪟 Open windows | AQI < 50, breeze 10–20 km/h |
| `laundry` | 👕 Good laundry day | No rain, sunny, breeze |
| `goldenHour.sunrise` | 🌅 Golden hour (sunrise) | 20 min around actual sunrise |
| `goldenHour.sunset` | 🌇 Golden hour (sunset) | 20 min around actual sunset |

**Priority tiers:**
- `severe` — shown even when other verdicts fire (e.g. heavy rain, AQI hazardous)
- `action` — shown when no severe verdicts
- `normal` — shown only when no severe/action firing

**Verdict settings:** base-ID matching. Toggle "vitaminD" off → hides both `.morning` and `.afternoon`.

---

## 5. The 6 Modes — biggest new decision (2026-06-19)

Modes let the **same brain** (verdict engine + data) serve very different users. A mode changes:
1. Which verdicts fire
2. What thresholds trigger them
3. The tone of the copy
4. The UI density / layout

### Architecture rule

**One primary mode + Travel overlay:**
- User picks ONE primary (Default / Farmer / Family / Elder / Photographer)
- Travel is the only mode that overlays — auto-activates when GPS moves > 100 km
- Returns to primary mode automatically when user returns home

### The 6 modes at a glance

| Mode | Primary user | What changes |
|---|---|---|
| 🌤 **Default** | Ravi / Sneha (urban adult) | Baseline 17 verdicts, standard UI |
| 🌾 **Farmer** | Krishnamurthy | Replaces verdict catalog with crop/spray/harvest/sowing verdicts |
| ✈️ **Travel** | Aisha (overlay only) | Adds: destination + home split, pack list, trip-duration verdicts |
| 👨‍👩‍👧 **Family** | Sneha (parent) | Adds child profiles, stricter AQI/UV thresholds, school briefs |
| 👴 **Elder** | Ramesh (senior) | Bigger text, voice-first, custom thresholds, 2-color high contrast |
| 📸 **Photographer** | Creative user | Adds: blue hour, fog window, stargazing, rainbow watch, lightning chase |

---

### Mode 1: Default

Standard experience. Baseline 17 verdicts. Urban-first. No mode indicator shown.

---

### Mode 2: Farmer mode (deep spec)

**Replaces** the entire verdict catalog for this mode.

**Farmer verdicts:**

| Verdict | When it fires |
|---|---|
| Spray window | Wind < 10 km/h AND no rain forecast 24h AND leaf wetness low |
| Harvest readiness | Soil moisture dropping AND 3+ dry days ahead |
| Sowing window | Monsoon onset in 7–14 days OR soil temp 18–25°C |
| Pest pressure | Humidity > 80% AND temp 25–35°C for 3+ days (bollworm, blight conditions) |
| Stubble warning | Wind direction toward populated area AND dry conditions |
| Monsoon onset | Within 14 days of IMD monsoon onset date |

**Farmer settings (user sets once during setup):**
- Crops grown (checkboxes): Rice · Cotton · Wheat · Chili · Sugarcane · Maize · Turmeric · Onion
- Sowing date (calendar picker)
- Plot location (separate from home GPS — farmer's field may be 10 km away)
- Language default: Telugu / Hindi / Marathi / Kannada

**UI changes in Farmer mode:**
- 7-day forecast prominent (not today-only)
- Voice-first — tap to hear in Telugu
- Bigger text, simplified icons
- No AI chat (not relevant)
- Monsoon countdown widget

**Data sources for farmer:**
- Open-Meteo soil moisture variable (`soil_moisture_0_1cm`)
- Open-Meteo soil temperature
- IMD monsoon onset dates (hard-coded by state, updated yearly)
- Pest pressure: calculated from temp + humidity thresholds per crop type

**Phase:** Phase 4 — India deep-dive. Not in v1.0 iOS/Android.

---

### Mode 3: Travel mode (overlay)

**Auto-activates** when CoreLocation reports > 100 km displacement from pinned home.

**Adds to the existing primary mode:**

**Split view:**
```
┌────────────────────┐
│ 📍 Manali · 14°    │  ← current location
│ 🏠 Hyderabad · 31° │  ← saved home
└────────────────────┘
```

**New verdicts in Travel mode:**
- Pack list (based on trip duration + destination forecast)
- Return-day weather at home
- Local activity windows (mountain pass open/closed, beach weather)
- Altitude advisory (above 2000m → UV multiplier applied)

**Pro tier:** Travel mode auto-detection. Free users can manually activate.

---

### Mode 4: Family mode

**Settings — add child profiles:**
- Name, age, health conditions (asthma, eczema, sun sensitivity)
- School location (for pickup timing)

**Threshold changes:**
- AQI threshold: 80 (vs 100 default) for outdoor activity
- UV threshold: 5 (vs 6 default) for sun protection verdict

**New verdicts:**
- School uniform advice (temp at 7 AM)
- Tiffin guidance (heat advisory for packed food)
- PE class advisory (AQI + UV check at school location at 10 AM)
- Pickup weather (15-min forecast for pickup time)
- Evening playtime window

**Phase:** Phase 3 or later.

---

### Mode 5: Elder mode

**UI changes:**
- 1.5× text size baseline
- 2-color palette (high contrast)
- Voice mode button prominent
- Larger touch targets (48pt minimum)
- Simplified AI chat (canned suggestions only)
- No Day Timeline (too complex) — replaced with 3-verdict summary

**Threshold changes:**
- Custom: user defines "warn me if temp > X" or "warn me if AQI > Y"
- Default thresholds tighter (heat warning at 33°C not 35°C)

**Phase:** Phase 3 or later. Potential Pro tier ("Kosmos for Caregivers").

---

### Mode 6: Photographer mode

**New verdicts:**
- 🌅 Sunrise golden hour (20 min before + after)
- 🌇 Sunset golden hour (20 min before + after)
- 🌄 Blue hour (30 min after sunset — twilight)
- 🌫 Fog window (visibility < 200m in morning)
- ⛈ Lightning chase (storm within 50 km + clearing local sky)
- 🌈 Rainbow watch (sun angle + rain shower combo)
- 🌌 Stargazing tonight (clear sky + moon phase + no light pollution proximity)
- ☁ Cumulus development (cloud-building conditions for sky photos)

**Settings:**
- Style: Landscape · Portrait · Street · Wildlife · Astro
- Notification: Fog alerts, golden hour, clear nights

**Phase:** Phase 3 or later.

---

## 6. Onboarding flow

```
Screen 1: Welcome + value prop
          "Weather that tells you what to do."

Screen 2: Location permission
          "Kosmos knows where you are. Tap allow."
          (or: Search your city instead)

Screen 3: Who are you?
          ○ 🌤 Just a regular person
          ○ 🌾 I farm
          ○ 👨‍👩‍👧 I'm a parent
          ○ 📸 I take photos
          ○ 👴 I'd like simple + big text
          (Travel turns on automatically when you travel)

Screen 4: Add widget (iOS/Android only)
          30-second tutorial

→ First weather view
```

Mode can be changed anytime: Settings → Mode.

---

## 7. Android — what we're building

### Android-specific design decisions

**Widget-first philosophy:** Android users live in widgets. The widget IS the product for many users.

**Four widget sizes:**

| Size | Content |
|---|---|
| 1×1 small | Temp + 1 verdict icon + condition icon |
| 2×1 medium | Temp + condition + 2 top verdicts |
| 4×2 large | Full Insights list (4 verdicts) + hourly strip |
| 4×4 extra large | Full day view — verdicts + timeline + character |

**Dynamic Island (Android equivalent: Notification Island):** Not applicable to Android. Skip.

**Android notification strategy:**
- Morning brief: 7 AM push with top 3 verdicts
- Rain alert: 60 min before rain
- Severe weather: immediate
- Vit D window: push 10 min before window opens (optional, default off)

### Android tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Widgets | Glance (Jetpack) |
| Shared logic | Kotlin Multiplatform (KMP) — verdict engine + models |
| Storage | DataStore (preferences) + Room (cache if needed) |
| Location | FusedLocationProviderClient |
| AI chat | Same Pollinations.ai endpoint |
| Notifications | FCM (push) — needs a lightweight backend |
| Distribution | Play Store |

**KMP strategy:** Share verdict engine + data models + API clients between iOS and Android. UI is native on each platform (SwiftUI on iOS, Compose on Android). This is the recommended approach — reduces drift over time.

### Android data sources (same as iOS)

- Open-Meteo: primary weather
- Tomorrow.io: secondary + confidence (slice 19)
- ipwho.is / ipapi.co: IP geolocation fallback
- Google Geocoding API (alternative to Apple CLGeocoder on Android)

### Android file structure (planned)

```
KosmosAndroid/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/kosmos/
│   │   │   ├── ui/
│   │   │   │   ├── home/HomeScreen.kt        ← main screen (Compose)
│   │   │   │   ├── timeline/TimelineScreen.kt
│   │   │   │   ├── settings/SettingsScreen.kt
│   │   │   │   ├── chat/ChatScreen.kt
│   │   │   │   └── components/               ← shared Compose components
│   │   │   ├── widget/
│   │   │   │   ├── SmallWidget.kt            ← Glance 1×1
│   │   │   │   ├── MediumWidget.kt           ← Glance 2×1
│   │   │   │   ├── LargeWidget.kt            ← Glance 4×2
│   │   │   │   └── WidgetDataProvider.kt     ← refreshes widget data
│   │   │   ├── service/
│   │   │   │   └── WeatherUpdateService.kt   ← WorkManager periodic refresh
│   │   │   └── notification/
│   │   │       └── RainAlertWorker.kt        ← FCM + local notification
├── shared/                                    ← KMP shared module
│   ├── commonMain/kotlin/com/kosmos/shared/
│   │   ├── models/Weather.kt                 ← WeatherSnapshot, Verdict, etc.
│   │   ├── engine/VerdictEngine.kt           ← THE BRAIN (shared)
│   │   ├── api/OpenMeteoClient.kt            ← shared API client
│   │   └── brain/PlainLanguage.kt            ← number → sentence
```

---

## 8. Feature catalog — full list with phase assignments

### ✅ In v1.0 (macOS + iOS Phase 1–2)

| Feature | Description |
|---|---|
| Live weather | Open-Meteo ensemble (ECMWF + GFS + ICON) |
| 17 verdict rules | Plain-English actions with time windows |
| Day timeline | Vertical 5 AM–11 PM with verdicts placed in slots |
| Hourly forecast | Horizontal scroll, next 12h |
| AI chat | Ask Kosmos anything weather-related (Pollinations.ai) |
| Morning notification | 7 AM brief with top 3 verdicts |
| City picker | Search any city worldwide |
| Neighborhood location | "Dhulapally, Hyderabad" (subLocality level) |
| Settings panel | Per-verdict toggles + module toggles |
| Show numbers mode | Expandable panel for nerds (UV 6, AQI 89, etc.) |
| Menu bar app (macOS) | LSUIElement — no dock icon |
| Weather patterns | 18 visual conditions with character + animation |

### 🔜 Phase 2–3 (iOS + Pro tier)

| Feature | Description | Tier |
|---|---|---|
| Home screen widget | Small + medium (WidgetKit) | Free |
| Lock screen widget | iOS 16+ (WidgetKit) | Free |
| Live Activity | Severe weather banner (ActivityKit) | Free |
| Tomorrow.io integration | Confidence indicators, pollen, lightning | Free |
| Hindi + Telugu localization | Full app translation | Free |
| Multiple saved cities | Home + favorites | Pro |
| Custom alert rules | "Warn me if AQI > 80" | Pro |
| Outfit recommendation | AI-generated based on weather | Pro |
| Travel mode auto-detect | GPS triggers travel overlay | Pro |
| 10-day forecast | Extended outlook | Pro |
| Hourly graph | Visual 24h chart | Pro |
| Family mode | Child profiles + stricter thresholds | Pro |

### 📅 Phase 4 (long-term)

| Feature | Description |
|---|---|
| Farmer mode | Full crop-specific advice surface |
| Elder mode | Bigger text, voice, caregiver alerts |
| Photographer mode | Golden hour, fog, stargazing, rainbow |
| WhatsApp bot | "Send Kosmos a city → get verdict" |
| IMD partnership | Official alerts integrated |
| Apple Watch complication | Advanced glance |
| HealthKit integration | Walk session tracking |
| Wallpaper generator | Weather-matched lock screen art |
| B2B API | Embed Kosmos verdicts in other apps |
| Smart home | Trigger AC when heat warning fires |

### ❌ Not building (and why)

| Feature | Why not |
|---|---|
| Generic gardening reminders | Farmer mode replaces this better |
| Construction work safety | Not our target user; liability risk |
| Medical appointment reminders | Calendar apps do this; not weather-related |
| Tire / EV battery tips | Too niche; not India-relevant |
| Flight delay prediction | Flightradar24 does it; out of scope |
| Pollen index (v1) | Not in Open-Meteo; needs Tomorrow.io; India has low pollen awareness |
| Astrology | Not what this product is |
| Ride-hailing integration | Partnership complexity; marginal value |

---

## 9. Data sources — how weather gets into the app

```
                    ┌─────────────────────────┐
                    │     Open-Meteo (free)    │
                    │  ECMWF + GFS + ICON      │
                    │  ensemble average        │
                    └────────────┬────────────┘
                                 │
                         Primary source
                         All verdicts run here
                                 │
                    ┌────────────▼────────────┐
                    │     Tomorrow.io (free    │
                    │     tier, needs key)     │
                    │     500 calls/day        │
                    └────────────┬────────────┘
                                 │
                         Secondary source
                         Confidence check:
                         "3/3 models agree" or
                         "models split — pack
                          the umbrella anyway"
                                 │
                    ┌────────────▼────────────┐
                    │     VerdictEngine        │
                    │     (Swift / Kotlin)     │
                    │     17 rules             │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     PlainLanguage.swift  │
                    │     Number → Sentence    │
                    │     UV 6 → "Sun is       │
                    │     brutal"              │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     UI                   │
                    │     Verdicts shown       │
                    │     in plain English     │
                    └─────────────────────────┘
```

**Key endpoints:**

| Source | URL | What it gives | Cost |
|---|---|---|---|
| Open-Meteo forecast | `api.open-meteo.com/v1/forecast` | Weather + hourly + daily | Free, no key |
| Open-Meteo AQI | `air-quality-api.open-meteo.com/v1/air-quality` | PM2.5, PM10, AQI | Free, no key |
| Open-Meteo geocoding | `geocoding-api.open-meteo.com/v1/search` | City name → lat/lon | Free, no key |
| Tomorrow.io | `api.tomorrow.io/v4/timelines` | Cross-check + pollen + lightning | Free 500/day, needs key |
| IP geolocation #1 | `ipwho.is/` | City from IP if GPS denied | Free |
| IP geolocation #2 | `ipapi.co/json/` | Fallback | Free 1000/day |
| AI chat | `text.pollinations.ai/openai` | Natural language answers | Free, no key |

---

## 10. AI chat — how it works

```
User types: "Should I go for a run at 6 PM?"
                    │
    Hidden system prompt injected:
    "You are Kosmos. Warm, decision-first.
     Never say UV 6 — say 'sun is strong'..."
                    │
    Hidden context block injected:
    "Current: Hyderabad, 34°C, AQI 72,
     Rain prob 18%, Feels like 38°C.
     Active verdicts: heat caution, avoid 11–4 PM,
     best walk 6:30–7:30 PM."
                    │
    POST → Pollinations.ai (free, no key)
                    │
    Response:
    "6 PM is actually your best window today.
     Temp drops to 31° and AQI is decent at 72.
     Just drink water before you head out — it's
     been a hot day."
```

**Kosmos's voice (from system prompt):**
- Decision-first (answer before explanation)
- Specific with times ("around 4 PM" not "in the afternoon")
- Indian-English idioms when natural
- Max 2 emojis per reply
- Honest about uncertainty
- Mirrors user's language (Telugu, Hindi, English)
- Never starts with "Hello!" or "Sure!" — gets straight to it

---

## 11. Design system — key specs

### Color logic

Each of the 18 weather patterns has its own gradient:
- Clear morning: `#FFE0B2 → #F8B98A` (warm peach)
- Clear midday: `#7BBAF4 → #3D87D4` (bright sky)
- Thunderstorm: `#2D2E45 → #0A0B1A` (purple-black)
- etc. (see [06-Design-System.md](./06-Design-System.md) for all 18)

### Typography rule

| Element | Size | Weight |
|---|---|---|
| Temperature (hero) | 64–88pt | Ultralight |
| Verdict title | 13pt | Semibold |
| Verdict detail | 11pt | Regular |
| All other text | System defaults | — |

### Motion rule

- Sun rays: 60s rotation, linear, looping
- Clouds: 60–180s drift, linear, looping
- Rain: 1–2s fall, continuous
- Reduce Motion: keep color transitions, kill particles

### Voice rule

| Bad | Good |
|---|---|
| "Light precipitation expected." | "Soft drizzle 3–5 PM ☂️" |
| "AQI 142 — unhealthy for sensitive groups." | "Air is rough today. Skip the jog." |
| "UV reaches a maximum of 9." | "Sun is brutal at noon. 15 min max, then SPF." |

---

## 12. macOS reference implementation — what was built

18 development slices completed. Archived at `../Kosmos-code-archive/`.

### Key files

| File | What it does |
|---|---|
| `KosmosApp.swift` | @main entry; MenuBarExtra + Settings scenes |
| `WeatherStore.swift` | @Observable singleton; all live data |
| `VerdictEngine.swift` | 17 rules; returns `[Verdict]` array |
| `PlainLanguage.swift` | Numbers → plain sentences |
| `OpenMeteoClient.swift` | Primary weather API |
| `LocationService.swift` | GPS + IP fallback + timeout (5s) |
| `MenuBarView.swift` | Popover: character + verdicts + hourly |
| `DayTimeline.swift` | Vertical 5 AM–midnight timeline |
| `AskKosmosPanel.swift` | AI chat UI |
| `PollinationsClient.swift` | AI chat HTTP client |

### Key patterns to carry forward

```swift
// Verdict engine returns arrays (multi-window support)
static func evaluate(_ snapshot: WeatherSnapshot) -> [Verdict]

// Dotted IDs for sub-verdicts
vitaminD.morning, vitaminD.afternoon   // same base = same toggle
bestWalk.morning, bestWalk.evening

// Base-ID matching in settings
func isVerdictEnabled(_ id: String) -> Bool {
    let baseId = id.components(separatedBy: ".").first ?? id
    return !disabledVerdictIds.contains(baseId)
}

// visibleVerdicts — no cap, filter by priority
var visibleVerdicts: [Verdict] {
    let hasNonNormal = verdicts.contains { $0.priority != .normal }
    return hasNonNormal
        ? verdicts.filter { $0.priority != .normal }
        : verdicts
}
```

### Known fixes to not re-introduce

- `authorizedWhenInUse` is iOS-only → use `.authorized` on macOS
- `ScrollView` collapses in menu bar popover → use plain `VStack`
- `@State private var store = WeatherStore.shared` not `let` (SwiftUI observation)
- IP geolocation: ipapi.co rate-limits at 1000/day → use ipwho.is as primary
- Walk verdict: must constrain to daytime hours `guard hour >= 5 && hour <= 21`

---

## 13. What to build next — recommended sequence

### For a new Claude Code session starting iOS:

1. **Set up iOS target** — new Xcode target sharing the `shared/` Swift package
2. **Port VerdictEngine** — identical logic, just a new platform
3. **Build HomeView** — SwiftUI scroll view with character + verdicts + hourly
4. **WidgetKit** — small (1×1) first, then medium
5. **Settings** — reuse same Preferences.swift
6. **Tomorrow.io** — add slice 19 (confidence layer)
7. **Localization** — Telugu first (biggest underserved market)

### For a new Claude Code session starting Android:

1. **Set up KMP module** — `shared/commonMain/` with VerdictEngine + models
2. **Android project** — Gradle, Compose, min SDK 26 (Android 8)
3. **Medium widget** — Glance 2×1 with temp + 2 verdicts (most-used size)
4. **HomeScreen** — Compose equivalent of MenuBarView
5. **WorkManager** — periodic weather refresh (every 30 min)
6. **DayTimeline** — vertical scroll Compose equivalent

---

## 14. Top open questions (decide before building)

1. **Tomorrow.io API key** — 5-min signup at tomorrow.io; free tier = 500/day
2. **App icon** — need a real icon before TestFlight submission
3. **18 weather pattern illustrations** — Figma design work; start with 6 most common
4. **Trademark** — verify "Kosmos" is clear in India, US, EU before launch
5. **Domain** — register kosmos.app before someone else does
6. **Mode decision** — which modes ship in v1? Recommendation: Default only in v1, add Farmer + Family in Phase 4
7. **Hindi or Telugu first** — recommendation: Telugu (Satish's market, more differentiating)

---

## 15. The north-star moments

We've succeeded when:

- 🌟 A user says: *"Kosmos told me to take the umbrella. Apple Weather didn't. Guess who got soaked? My friend."*
- 🌟 An aunty in Kakinada uses it daily in Telugu
- 🌟 A journalist writes: *"Kosmos is what Apple Weather should have been."*
- 🌟 Telangana Weatherman X account endorses it
- 🌟 Featured by Apple in "Apps We Love"
- 🌟 Day-30 retention > 25%

---

## 16. Quick-start for a new Claude Code session

Paste this at the start of any new session:

```
I'm building Kosmos — a weather + wellness app that tells users what to do,
not just what the weather is. Think "Apple Weather tells you 38°, Kosmos
tells you to drink 3L water, step out for Vitamin D 9:30–9:45, avoid
outdoors 11AM–4PM."

Tech: Swift/SwiftUI (macOS + iOS), Kotlin/Compose (Android).
Weather data: Open-Meteo (free, no key). AI: Pollinations.ai (free, no key).

The verdict engine has 17 rules that return plain-English actions.
No numbers shown to users unless they toggle "Show numbers."

macOS reference implementation is DONE (archived).
Now building: [iOS / Android — say which].

Full docs are in: /Users/apple/Documents/Claude Apps/Weather App/mac OS/docs/
Key files: 10-Master-Brief.md (this file), 03-Verdicts.md, 07-Architecture.md
```

---

*Owner: Satish Kumar — designer, product lead, user #1.*
*Sessions documented: macOS build (18 slices) + mode architecture + Android planning.*
*Next: iOS build or Android build — pick one and go.*
