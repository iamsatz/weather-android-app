# 06 — Design System

> Colors, typography, motion, and voice. Locked principles plus practical specs.

---

## 🎭 Voice & tone

### Voice principles

1. **Decision-first.** Every sentence pushes toward an action.
2. **Specific over generic.** *"57% rain between 4–7 PM"* not *"chance of rain."*
3. **Warm, not formal.** Never *"Dear user."* Say *"you."*
4. **Indian-English idioms when natural** — *"Don't fry yourself in this heat"* over *"Avoid prolonged sun exposure."*
5. **Honest about uncertainty.** *"Models split on evening rain — pack the umbrella anyway."*
6. **Emoji in moderation.** Maximum 2 per reply.

### Good vs bad

| Cold (avoid) | Warm (use) |
|---|---|
| "Light precipitation expected." | "Soft drizzle 3–5 PM ☂️" |
| "AQI 142 — unhealthy for sensitive groups." | "Air is rough today. Skip the jog." |
| "UV reaches a maximum of 9." | "Sun is brutal at noon. 15 min max, then SPF." |
| "Weather code 95: thunderstorm." | "Thunder rolling in by sunset ⛈" |

### Tone shifts by context

| Where | Tone |
|---|---|
| Morning summary | Energetic, brief, action-first |
| Severe alert | Sober, factual, calm — no exclamation overload |
| AI chat answers | Conversational, friendly, specific |
| Vit D nudge | Gentle, coach-like |
| Settings copy | Direct, helpful |

### AI system prompt (full text)

```
You are Kosmos, a warm, plain-spoken weather and wellness companion.
Your voice is the Telangana Weatherman — direct, friendly, decision-first.

Speak in short, decision-first sentences. 2-3 sentences usually enough.

Use plain English. Never say "UV index 6" — say "sun is strong" or give a
time window like "10:30 to 10:45". Be specific with times:
"rain around 4 PM" not "rain in the afternoon."

Indian English idioms when natural ("don't fry yourself in this heat",
"easy day", "carry the foldable"). Maximum 2 emojis per reply.

Be honest about uncertainty: "models split on evening rain" rather than
faking confidence. Never refuse to answer — if unsure, give your best
guess with caveats.

For health questions: never give medical diagnoses. Suggest checking
with a doctor, but offer practical lifestyle guidance based on the
weather.

If the user writes in Hindi, Telugu, Tamil, or any other language —
mirror their language naturally.

Do not start replies with "Hello" or "Sure!" — get straight to the answer.
```

---

## 🌐 Localization

| Language | Status | Priority |
|---|---|---|
| English | v1.0 | 🟢 |
| Hindi (हिंदी) | v1.0 | 🟢 |
| Telugu (తెలుగు) | v1.0 | 🟢 |
| Tamil (தமிழ்) | v1.0 | 🟢 |
| Bengali, Kannada, Marathi | v1.1 | 🟡 |
| Spanish, Indonesian | v1.2 | 🟡 |

Each language gets a localized *personality*, not just translation. e.g., Telugu uses *"ఎండ మండిపోతుంది"* (the sun is blazing) instead of literal *"high heat."*

---

## 🎨 Color palette

### Per-condition pastel backgrounds

| Condition | Hex top | Hex bottom | Mood |
|---|---|---|---|
| Clear morning | `#FFE0B2` | `#F8B98A` | Warm peach |
| Clear midday | `#7BBAF4` | `#3D87D4` | Bright sky |
| Clear evening | `#FFB07A` | `#C46B82` | Sunset glow |
| Clear night | `#1A1B3A` | `#050618` | Indigo night |
| Partly cloudy day | `#A8C7E5` | `#5E89B8` | Hazy blue |
| Partly cloudy night | `#2B3158` | `#0D1029` | Twilight |
| Overcast day | `#888E9C` | `#535869` | Slate grey |
| Overcast night | `#3D4255` | `#0F1219` | Charcoal |
| Foggy | `#D5D5D5` | `#9A9A9A` | Soft grey |
| Drizzle | `#7A8B98` | `#454D5A` | Slate-blue |
| Light rain | `#5A6B7A` | `#2A3340` | Rain grey |
| Heavy rain | `#3A4252` | `#13161F` | Storm dark |
| Thunderstorm | `#2D2E45` | `#0A0B1A` | Purple-black |
| Light snow | `#D8DEE8` | `#969FB2` | Snow blue-white |
| Heavy snow | `#A5B0C0` | `#525C70` | Blizzard |
| Sleet | `#6E7889` | `#363D4B` | Sleet grey |
| Hazy bad AQI | `#C9AC7E` | `#947A5E` | Smoke beige |
| Golden hour | `#FFB67A` | `#9B6E8A` | Magical |

### Verdict accent colors (left bar in Insights rows)

| Verdict | Hex | Logic |
|---|---|---|
| Raincoat | `#1A5CB3` | Deep ocean blue (severe) |
| Umbrella | `#3380C7` | Medium blue (action) |
| Umbrella light | `#5294D6` | Lighter blue (watch) |
| Heat | `#D96B33` | Burnt orange |
| Sun protection | `#EB9933` | Amber |
| Cold | `#66A8D1` | Icy blue |
| Air good | `#4DC85A` | Green |
| Air moderate | `#F2C633` | Yellow |
| Air unhealthy | `#EB4747` | Red |
| Air hazardous | `#9E2A2A` | Deep red |
| Vitamin D | `#D98C33` | Warm amber |
| Best walk | `#3FA67F` | Walking green |
| Avoid hours | `#C74D33` | Warning red-orange |
| Can I jog | `#4DB371` | Jog green |
| Hydration | `#3F8CD9` | Water blue |
| Mosquito | `#8C4D80` | Mosquito purple |
| Open windows | `#80B3D9` | Sky blue |
| Close windows | `#8C7366` | Warm grey |
| Laundry | `#BFB259` | Sunny yellow-green |
| Cooler tomorrow | `#7399BF` | Cool blue |
| Golden hour | `#F28C4D` | Warm gold |
| Easy day | `#3F8C66` | Forest green |

### AQI badge colors (US EPA scale)

| AQI range | Hex | Label |
|---|---|---|
| 0–49 | `#4DC85A` | Good |
| 50–99 | `#F7BF33` | Moderate |
| 100–149 | `#FA8C33` | Unhealthy SG |
| 150–199 | `#EB4D4D` | Unhealthy |
| 200–299 | `#A653CC` | Very Unhealthy |
| 300+ | `#8C1F33` | Hazardous |

---

## ✏️ Typography

| Use | Font | Weight | Size |
|---|---|---|---|
| Temperature (hero) | SF Pro / Inter | Ultralight | 64–88 pt |
| Condition word | SF Pro / Inter | Medium | 16 pt |
| City pill | SF Pro / Inter | Semibold | 13 pt |
| Section label | SF Pro / Inter | Semibold tracking 0.8 | 10 pt (UPPERCASE) |
| Verdict title | SF Pro / Inter | Semibold | 13 pt |
| Verdict detail | SF Pro / Inter | Regular | 11 pt |
| Hourly cell time | SF Pro / Inter | Medium (Bold for "Now") | 10 pt |
| Footer / diagnostic | SF Mono / JetBrains Mono | Regular | 10 pt |

iOS / macOS use SF Pro. Android uses Inter (Google Fonts).

---

## 🎬 Motion vocabulary

### Animation timing reference

| Element | Duration | Easing | Loop |
|---|---|---|---|
| Sun rays rotation | 60 s/rev | linear | yes |
| Cloud drift | 60–180 s | linear | yes |
| Rain drop fall | 1–2 s | linear | continuous |
| Snowflake drift | 3–5 s | ease | continuous |
| Star twinkle | 1.5–3 s | sine | yes (per-star phase) |
| Moon pulse | 8–12 s | ease in/out | yes |
| Lightning flash | 80 ms on, 250 ms fade | ease out | 6–10 s gap |
| Heat shimmer | 4 s cycle | sine | yes |
| Character breathing | 2 s, ±0.5% scale | sine | yes |
| Refresh icon spin | 0.9 s/rev | linear | while loading |
| Cell expand/collapse | 0.25 s | ease in/out | once per tap |
| Color/condition crossfade | 0.8 s | ease in/out | once per change |

### Reduce Motion

When macOS / iOS Reduce Motion is on:
- Disable: rotation, drift, particles (rain, snow), pulses, twinkles
- Keep: color transitions, crossfades (these are functional cues, not decoration)
- Replace particles with static texture at moderate density

---

## 🪟 Components

### Card

- Corner radius 14 pt
- Background: `.white.opacity(0.85)` over pastel bg
- Subtle shadow: y +4, blur 20, opacity 0.12
- Padding: 14 pt horizontal, 12 pt vertical

### Pill

- Full pill shape (corner radius = height / 2)
- Background: `.white.opacity(0.85)` for neutral; tinted for AQI etc.
- Padding: 10 pt horizontal, 5 pt vertical
- Text: 12 pt semibold

### Suggestion chip (AI chat)

- Same as pill but smaller
- 11 pt medium text
- Padding: 10 pt × 5 pt
- White text @ 85% on `.white.opacity(0.14)` background

### Detail row

- 28 pt left padding (emoji column width)
- 13 pt title + 11 pt detail
- 10 pt vertical spacing between rows

### Verdict row (Insights)

- Left accent bar: 3 pt wide, full row height
- Emoji column: 30 pt wide, centered
- Text column: takes remaining width
- 8–10 pt vertical spacing between rows

---

## 📐 Spacing & layout

8 pt grid throughout.

| Container | Horizontal padding | Vertical padding |
|---|---|---|
| Menu bar popover content | 18 pt | 16 pt top, 8 pt bottom |
| Main window | 22 pt | 16 pt top, 24 pt bottom |
| Settings tab | 20 pt | 20 pt |
| Hero block sections | 14 pt | 14 pt |

### Mobile breakpoints (iOS)

| Width | Target |
|---|---|
| 320 pt | iPhone SE / small Android |
| 375 pt | iPhone 13/14/15 |
| 393 pt | Pro Max / Pixel |
| 430 pt | iPhone Pro Max landscape (handle, not optimize) |

---

## 🦮 Accessibility

- **Dynamic Type** — every text uses scaled fonts
- **VoiceOver / TalkBack** — every interactive element labeled
- **Color blindness** — verdict rows use color AND emoji, never color alone
- **Reduce Motion** — all kinetic effects degrade gracefully (see above)
- **High contrast mode** — switches to flatter palette with thicker text

VoiceOver labels currently:
- Temperature: "28 degrees Celsius"
- City pill: "Change city — currently Hyderabad"
- Refresh button: "Refresh weather"
- Verdict row: title + detail combined

---

## 🌒 Dark mode

True dark (not just dimmed) for:
- OLED battery savings
- Astronomy-feel for night weather
- Less eye strain at night

Both light and dark adopt the same gradient principles — just shifted to deeper hues at night.

Kosmos primarily lives in the menu bar (light pastel by default). The main window respects system dark mode.

---

## 🎤 Microcopy guide

| Where | Tone | Example |
|---|---|---|
| First launch onboarding | Welcoming, brief | *"Kosmos knows where you are. Tap allow."* |
| Empty state (no internet) | Calm, helpful | *"No connection. Showing your last forecast from 12 minutes ago."* |
| Permission denied | Patient, non-punishing | *"No location yet. Tap to search your city instead."* |
| Severe alert | Sober, specific | *"IMD: Heavy rain warning for your district till 9 PM."* |
| Error toast | Apologetic, brief | *"Couldn't reach Kosmos's brain. Try again in a moment."* |

---

*Next: [07-Architecture.md](./07-Architecture.md) — tech stack and data sources.*
