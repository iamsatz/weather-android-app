# 12 — Widget Designs

> Widget specs for iOS (WidgetKit) and Android (Glance).
> Widgets are where most users will spend 90% of their time with Kosmos.

---

## 1. Widget philosophy

A widget has **one job:** give the user enough information to make a decision without opening the app.

Rules:
- **One clear action per widget.** "Carry umbrella." Not a weather dashboard.
- **No numbers without meaning.** Never show "AQI 72" — show "●Good" instead.
- **Top verdict always visible.** Whatever is most urgent is always the headline.
- **Tap → app opens to relevant screen.** Not just the home screen.

---

## 2. iOS widgets (WidgetKit)

### Small (2×2) — 155×155 pt

**Shows:** Temperature + condition + 1 top verdict

```
┌──────────────────┐
│ 🌤              │
│                  │
│ 34°C             │
│                  │
│ ☔ Rain at 4 PM   │
└──────────────────┘
```

**Content rules:**
- Condition icon: top-left, 24pt
- Temperature: 36pt ultralight, centered
- Verdict: icon + 3-word max title, bottom row
- Background: condition gradient (same 18 palettes from the main app)
- No city name (too small)
- Tap: opens app HomeScreen

**What changes by condition:**
| Condition | Background | Verdict shown |
|---|---|---|
| Thunderstorm | Purple-black | ⛈ Stay indoors |
| Heat 38°+ | Burnt orange | 🔥 Dangerous heat |
| AQI > 150 | Smoke beige | 😷 Bad air — stay in |
| Clear + Vit D | Warm peach | 🌤 Vit D: 9:30–9:45 |
| Clear + walk | Sky blue | 🚶 Walk: 6–7 PM |
| No verdicts | Gradient | Condition + temp only |

---

### Medium (4×2) — 329×155 pt — PRIORITY, build first

**Shows:** Temperature + condition + 2 top verdicts + AQI badge

```
┌─────────────────────────────────────────────┐
│ Hyderabad                             34°C  │
│ 🌤 Partly cloudy                  ●AQI Good │
│                                             │
│ ☔ Rain at 4 PM, 73%     🚶 Walk: 6–7 PM   │
└─────────────────────────────────────────────┘
```

**Content rules:**
- Header: city (left) + temp (right), 13pt semibold + 24pt ultralight
- Sub-header: condition name (left) + AQI badge (right)
- Bottom row: 2 verdicts split 50/50 — icon + title only (no detail)
- If only 1 verdict: show full width with detail text
- Tap left verdict: opens app to relevant timeline position
- Tap right verdict: same
- Tap elsewhere: opens HomeScreen

**AQI badge:**
- ●Good (green): 0–49
- ●OK (yellow): 50–99
- ●Rough (orange): 100–149
- ●Bad (red): 150–199
- Never show the number

---

### Large (4×4) — 329×345 pt

**Shows:** Full insights list + hourly strip

```
┌─────────────────────────────────────────────┐
│ Hyderabad · Thu Jun 19                34°C │
│ 🌤 Partly cloudy                  ●AQI OK  │
├─────────────────────────────────────────────┤
│ ☔ Rain at 4 PM — 73% chance                 │
│ 🚶 Best walk: 6–7 PM, AQI 68               │
│ 🦟 Mosquito hour after sunset               │
│ 💧 Drink extra water today                  │
├─────────────────────────────────────────────┤
│ 3PM  4PM  5PM  6PM  7PM  8PM  9PM  10PM    │
│  34°  32°  31°  29°  28°  27°  26°  24°    │
│  🌤   ☔   ☔   🌤   🌤   🌙   🌙   🌙    │
└─────────────────────────────────────────────┘
```

**Content rules:**
- Header: same as medium
- Insights section: top 4 verdicts (title only, no detail — too small)
- Hourly strip: next 8 hours, temp + condition icon
- Dividers between sections
- Background: solid light wash (not full gradient — too dark for text at this size)
- Tap → HomeScreen

---

### Lock Screen (circular) — iOS 16+

**Shows:** Temperature only or verdict icon

```
  ┌───┐
  │34°│
  └───┘
```

or

```
  ┌───┐
  │ ☔ │
  └───┘
```

**Rules:**
- Lock screen: monochrome, system-rendered
- Show temp by default
- If severe verdict (rain / heat / AQI): show verdict icon instead
- Tap → unlocks to Kosmos HomeScreen

---

### Lock Screen (rectangular) — iOS 16+

```
┌─────────────────────┐
│ ☔ Rain at 4 PM · 34°│
└─────────────────────┘
```

**Rules:**
- Icon + top verdict title + temp
- 2 lines max

---

### Inline lock screen (above clock) — iOS 16+

```
☔ Hyderabad · Rain at 4 PM · 34°C
```

One line. Most compact. Most-seen widget surface.

---

## 3. iOS Live Activity (ActivityKit)

**Not a widget — it's a banner** that appears on the lock screen and Dynamic Island during an active weather event.

### When it activates

- Rain starting in < 30 min
- Severe AQI alert
- Temperature crossing dangerous threshold (> 42°C or < 2°C)

### Lock screen view

```
┌─────────────────────────────────────────────┐
│ 🌧 KOSMOS                                   │
│ Rain arriving in 18 minutes                 │
│ Get inside or grab an umbrella now          │
└─────────────────────────────────────────────┘
```

### Dynamic Island (compact)

```
[🌧 18 min]   [☔ →]
```

### Dynamic Island (expanded, long press)

```
┌────────────────────────────────┐
│  🌧 Rain arriving              │
│  In 18 minutes                 │
│                                │
│  Hyderabad · currently 32°C    │
│  73% probability               │
│                                │
│  [Open Kosmos]                 │
└────────────────────────────────┘
```

**Auto-dismisses:** 30 min after the rain event starts.

---

## 4. Android widgets (Glance)

Same 4 sizes as described in [11-Android.md](./11-Android.md) but with Android-specific notes.

### Small (1×1) — 112×112 dp

```
┌──────────────────┐
│  🌤              │
│                  │
│  34°             │
│  ☔ Rain 4PM      │
└──────────────────┘
```

**Android-specific:**
- Rounded corners: 12dp (system default on Android 12+)
- No custom fonts in widgets (Android limitation) — use system default
- Background: use widget background color from Material You (follows wallpaper)
- If Dynamic Color enabled: use user's derived palette

---

### Medium (2×1) — 238×112 dp — PRIORITY

Same content as iOS medium. Android notes:
- Glance uses `@Composable` but with restrictions — no custom Canvas
- Icons: use adaptive icons or Material Icons (not SF Symbols)
- Text: `TextStyle(fontFamily = FontFamily.Default)` — no custom fonts in Glance

---

### Large (4×2) — 238×238 dp

Same as iOS large. Android notes:
- `LazyColumn` not available in Glance — use `Column` with manual items
- Limit to 4 verdicts hard (scroll not allowed in widgets)

---

### Extra Large (4×4) — 238×412 dp

Only available on Android (no iOS equivalent at this size in WidgetKit).

Includes mini Day Timeline — a simplified vertical layout of verdict blocks. Build last.

---

## 5. Widget refresh strategy

### iOS (WidgetKit)

```swift
// Timeline provider — called by system to get entries
struct KosmosWidgetProvider: TimelineProvider {
    func getTimeline(in context: Context,
                     completion: @escaping (Timeline<KosmosEntry>) -> Void) {
        Task {
            let snapshot = await WeatherStore.shared.fetchLatest()
            let verdicts = VerdictEngine.evaluate(snapshot)
            let entry = KosmosEntry(date: .now, snapshot: snapshot, verdicts: verdicts)

            // Refresh in 30 minutes
            let nextRefresh = Calendar.current.date(byAdding: .minute, value: 30, to: .now)!
            let timeline = Timeline(entries: [entry], policy: .after(nextRefresh))
            completion(timeline)
        }
    }
}
```

**Refresh budget:** iOS limits widgets to ~40–70 refreshes per day. At 30 min intervals = 48 refreshes. Within budget.

### Android (Glance + WorkManager)

WorkManager triggers every 30 min → updates GlanceAppWidget state → widget redraws.

```kotlin
class WeatherRefreshWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val snapshot = OpenMeteoClient.fetch(lat, lon)
        val verdicts = VerdictEngine.evaluate(snapshot)

        // Update all widget sizes
        SmallWidget().updateAll(context)
        MediumWidget().updateAll(context)
        LargeWidget().updateAll(context)

        return Result.success()
    }
}
```

---

## 6. Widget empty states

| State | What to show |
|---|---|
| No location permission | "Tap to set your city →" |
| No internet | Last known temp + "●Offline" badge |
| Loading (first launch) | Skeleton: grey bars where content will be |
| Location denied + no cached city | Kosmos logo + "Tap to open" |

**Never show:** blank white widget, error codes, stack traces.

---

## 7. What the widget taps do

| Tap target | Opens |
|---|---|
| Temperature | App HomeScreen |
| Condition icon | App HomeScreen |
| Verdict row (left) | App → timeline at that verdict's time |
| Verdict row (right) | App → timeline at that verdict's time |
| AQI badge | App → verdict for AQI detail |
| City name | App → city search (to change city) |

On Android: `actionStartActivity` in Glance with Intent extras to deep-link.
On iOS: `Link` or `widgetURL` in WidgetKit.

---

## 8. Widget design tokens

### Widget background options

| Mode | Background |
|---|---|
| Condition gradient | Full gradient (same as app — most beautiful) |
| Material You (Android) | System-derived from wallpaper |
| Plain white | Clean, always readable |
| Plain dark | For AMOLED users |

Default: condition gradient. User can change in Settings → Widget style.

### Text on gradient

All text: `Color.white` with `Shadow(color: .black.opacity(0.25), radius: 2, offset: (0, 1))`.

This ensures readability across all 18 gradients — from bright peach to dark thunderstorm.

### Icon set

- iOS: SF Symbols (system, free)
- Android: Material Icons + custom SVGs for weather conditions

Same emoji used in verdict copy appears in widgets — provides visual continuity between widget and app.

---

## 9. Homescreen widget recommendations

### iOS home screen — recommended layout for users

```
[Small] Top-left corner
        "Quick glance — temp + top verdict"

[Medium] Full width below
         "Today's key information"
```

### Android home screen — recommended layout

```
[Medium 2×1] Top of home screen
             "Primary info surface"

Or

[Large 4×2] Full-width feature
             "Full insight set"
```

Kosmos will show a "how to add a widget" tutorial on first launch for both platforms.

---

## 10. Watch complications (future)

Not in v1. Design notes for later:

### Apple Watch

| Complication slot | Content |
|---|---|
| Corner (small) | Temp number |
| Circular small | Condition icon |
| Modular small | Temp + condition |
| Modular large | Top verdict row |

### Wear OS

Same complication types. Jetpack Compose for Wear.

---

*Widget is the product. Build medium widget first — it's the most-used surface.*
*Docs: [10-Master-Brief.md](./10-Master-Brief.md) · [11-Android.md](./11-Android.md) · [07-Architecture.md](./07-Architecture.md)*
