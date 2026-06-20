# 03 — Verdicts

> Kosmos's moat. Each verdict answers a concrete daily question with a plain-English
> action, based on documented public-health or meteorological standards.

## How the engine works

```
WeatherSnapshot ─→ 17 rules ─→ array of Verdicts ─→ sorted by priority ─→ UI
```

- Rules return arrays (0, 1, or many verdicts each) — supports multi-window outputs
- Verdict IDs use dotted convention for sub-windows: `vitaminD.morning`, `bestWalk.evening`
- Three priority tiers: **severe** (red/urgent), **action** (orange/decision), **normal** (status info)
- Normal-priority verdicts hide when anything severe/action is firing
- The Insights list shows all enabled+firing verdicts (no cap; popover scrolls)
- The Day Timeline places verdicts with `timeWindow` at their start hour

---

## Verdict catalog (17 types)

Each verdict has:
- **ID** — internal identifier (uses dotted form for sub-windows)
- **Title / detail** — plain-English copy shown to user
- **Trigger** — exact conditions for firing
- **Source** — standard/authority the threshold is based on
- **Priority** — severe / action / normal
- **TimeWindow** — yes if the verdict has a specific time slot

---

### 1. Raincoat — `raincoat`

- **Title:** Take a raincoat
- **Detail:** "78% rain · 4–6 PM" (always shows max precip %)
- **Trigger:** max precip ≥ 70% in next 12h OR heavy weather codes (65, 67, 75, 82, 95, 96, 99) present
- **Priority:** severe
- **TimeWindow:** yes (span of contiguous hours ≥ 70%)
- **Source:** WMO precipitation probability standards

### 2. Umbrella — `umbrella`

- **Title:** Take an umbrella
- **Detail:** "52% rain · 4–6 PM"
- **Trigger:** max precip 40–69% in next 12h OR drizzle codes (51, 53, 55, 61, 63, 80, 81) present
- **Priority:** action
- **TimeWindow:** yes

### 3. Light chance of rain — `umbrella.light`

- **Title:** Light chance of rain
- **Detail:** "32% around 5 PM. Carry one if you'll be out long."
- **Trigger:** max precip 30–39% in next 12h
- **Priority:** action
- **TimeWindow:** yes (1-hour window centered on peak)

### 4. Heat (severe) — `heat`

- **Title:** Brutal heat — protect yourself
- **Detail:** Combines actions like "Umbrella for shade + sunscreen SPF 30+ · Drink 3L water · Avoid 11am–4pm sun"
- **Trigger:** apparent temperature ≥ 38°C
- **Priority:** severe
- **TimeWindow:** no (all-day)
- **Source:** US NWS heat advisory thresholds

### 5. Heat (action) — `heat`

- **Title:** Hot day — stay hydrated
- **Detail:** "Feels 36°. Carry water, light clothes. Sunscreen + hat if going out."
- **Trigger:** apparent temperature 35–37°C
- **Priority:** action

### 6. Sun protection — `sunProtection`

- **Title:** Sun is strong — protect your skin
- **Detail:** "Sunscreen SPF 30+, sun hat or umbrella for shade. UV is 9."
- **Trigger:** UV ≥ 8 AND apparent temp < 35 AND is_day = true
- **Priority:** action
- **Source:** WHO UV exposure guidance

### 7. Cold — `cold`

- **Title:** Bundle up — chilly day
- **Detail:** "Feels like 7°."
- **Trigger:** current temperature < 10°C
- **Priority:** action
- **Source:** UK Met Office cold guidance

### 8. Air quality — `air`

Six tiers based on US-EPA AQI scale:

| AQI range | Title | Priority |
|---|---|---|
| 0–49 | Air is excellent | normal |
| 50–99 | Air is okay | normal |
| 100–149 | Air is rough — skip the jog | action |
| 150–199 | Air is bad — wear a mask outside | severe |
| 200+ | Air is hazardous — stay indoors today | severe |

- **Source:** US-EPA AQI breakpoints

### 9. Cooler tomorrow — `coolerTomorrow`

- **Title:** Tomorrow's much cooler
- **Detail:** "Dropping ~8°. Layer up — sniffles weather."
- **Trigger:** tomorrow's max temp ≥ 6°C below today's max
- **Priority:** action
- **TimeWindow:** no

### 10. Vitamin D — `vitaminD.morning` and `vitaminD.afternoon`

Two distinct windows:

**Morning window** (`vitaminD.morning`):
- Scan 8 AM – 11 AM, find hour with UV ∈ [3, 6], not raining
- Pick the hour with UV closest to 5
- Detail: "9:30–9:45 · UV 5 · 15 min is enough"

**Afternoon window** (`vitaminD.afternoon`):
- Scan 3 PM – 5 PM, same criteria
- Detail: "3:30–3:45 · UV 4 · 15 min is enough"

- **Priority:** action
- **TimeWindow:** yes
- **Source:** WHO + ICMR Vit D synthesis guidance (UV 3-6 = safe synthesis range)

### 11. Best walk — `bestWalk.morning` and `bestWalk.evening`

Two adaptive windows:

**Morning walk** (`bestWalk.morning`):
- Scan 5 AM – 10 AM, find hour with lowest comfort penalty
- Skip rainy hours (precip ≥ 40% or rainy weather codes)
- Detail: "6 AM–7 AM · ~28°, gentlest air."

**Evening walk** (`bestWalk.evening`):
- Scan 4 PM – 9 PM, same logic
- Detail: "6 PM–7 PM · ~31°"

**Comfort score (lower = better):**
```
heatPenalty = max(0, temp - 26) × 1.5
coldPenalty = max(0, 18 - temp) × 1.2
uvPenalty   = max(0, uv - 4) × 1.8
score       = heatPenalty + coldPenalty + uvPenalty
```

- **Priority:** action
- **TimeWindow:** yes

### 12. Avoid these hours — `avoidHours`

- **Title:** Avoid outdoors 11 AM–4 PM
- **Detail:** "38° + climbing. Plan errands before or after."
- **Trigger:** contiguous hours today (future) where temp ≥ 36°C OR UV ≥ 9
- **Priority:** action
- **TimeWindow:** yes (the bad span)

### 13. Good day to jog — `canJog`

- **Title:** Good day to jog
- **Detail:** "Best window: 6 AM at ~24°."
- **Trigger:** apparent temp < 32°C AND AQI < 100 AND not raining
- **Priority:** action
- **TimeWindow:** no (advisory)

### 14. Hydration goal — `hydration`

- **Title:** Drink 3L water today
- **Detail:** "Hot. Keep a bottle close, sip every hour."
- **Trigger:** apparent temp 26–34°C (heat rule covers ≥ 35°C)
- **Priority:** action
- **TimeWindow:** no

### 15. Mosquito hour — `mosquito`

- **Title:** Mosquito hour — cover up
- **Detail:** "Sleeves, repellent, screens. Peak biting till 9 PM."
- **Trigger:** hour 17–21 local AND temp 20–32°C AND humidity ≥ 60% AND today's max precip ≥ 40% (recent rain → breeding)
- **Priority:** action
- **TimeWindow:** 5 PM – 9 PM today

### 16. Open the windows — `openWindows`

- **Title:** Open the windows
- **Detail:** "Outdoor is cool + clean. Let fresh air in for an hour."
- **Trigger:** outdoor temp 18–26°C AND AQI < 80 AND not raining
- **Priority:** action

### 17. Keep windows shut — `closeWindows`

- **Title:** Keep windows shut
- **Detail:** "AQI 162 outside. Run an air purifier if you have one." (OR "Outdoors is 35° + climbing. Draw curtains, run AC.")
- **Trigger:** AQI ≥ 120 OR temp ≥ 32°C
- **Priority:** action

### 18. Laundry day — `laundry`

- **Title:** Great day for laundry
- **Detail:** "Dry air, no rain expected. ~5 hours of sun."
- **Trigger:** humidity < 65% AND max precip in next 8h < 25% AND ≥ 3 hours with UV ≥ 3
- **Priority:** action

### 19. Golden hour — `goldenHour.sunrise` and `goldenHour.sunset`

Two windows:

**Sunrise** (`goldenHour.sunrise`):
- 15 min before sunrise → 60 min after
- Skip if heavy rain expected in that window

**Sunset** (`goldenHour.sunset`):
- 60 min before sunset → 15 min after
- Only fires when within 12 hours

- **Priority:** action
- **TimeWindow:** yes
- **Source:** Photographer's golden-hour convention

### 20. Easy day (default) — `easy`

- **Title:** Easy day — go about your business
- **Detail:** condition summary (e.g., "28° · partly cloudy")
- **Trigger:** no other verdict fires
- **Priority:** normal

---

## Priority filter rules

The Insights list applies this filter:

```
1. If any severe verdict is firing → hide all normal verdicts
2. If any action verdict is firing → hide all normal verdicts
3. If only normal verdicts → show them all
4. "Easy day" only fires when nothing else does
```

The Day Timeline shows all verdicts that have a `timeWindow`, regardless of priority filter — it's a complete view of the day.

---

## Settings — what users can toggle

One toggle per *type* (base ID before the dot):

- `vitaminD` controls both morning + afternoon windows
- `bestWalk` controls both morning + evening windows
- `goldenHour` controls both sunrise + sunset

Implementation in `Preferences.isVerdictEnabled(_:)`:
```swift
let baseId = id.components(separatedBy: ".").first ?? id
return !disabledVerdictIds.contains(baseId)
```

---

## Future verdicts (slice 19+)

Documented in [09-Open-Questions.md](./09-Open-Questions.md). Candidates:

- Pollen / allergy alert (needs Tomorrow.io)
- Migraine pressure warning (barometric pressure drops)
- Lightning proximity warning
- Sleep weather (overnight temp/humidity → bedroom plan)
- Festival weather (Diwali, Holi, Sankranti context)
- Monsoon countdown
- Cricket-match weather

---

*Next: [04-Weather-Patterns.md](./04-Weather-Patterns.md) — visual treatment for 18 weather conditions.*
