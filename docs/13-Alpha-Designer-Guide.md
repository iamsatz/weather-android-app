# Kosmos Alpha — designer guide (memory, cache, speed, offline)

> For Satish. Plain English. No Gradle required.

---

## The one picture

```
INTERNET (Open-Meteo, AQI, optional Tomorrow.io)
        ↓ download (needs Wi‑Fi / mobile data)
   PHONE RAM  ←  “what you see right now” (temporary)
        ↓
   PHONE DISK ←  settings, saved city, diary (permanent)
```

---

## 1. Temporary memory (RAM)

**What it is:** While Kosmos Alpha is open, the app keeps the current weather + verdicts in the phone’s working memory — like notes on a desk.

**What happens when you close the app:** The desk is cleared. Next open, the app fetches weather again from the internet (using your saved city if GPS is off).

**Design implication:** There is no “last screen I saw yesterday” unless we add disk cache (planned for beta).

---

## 2. Cache (what stays on the phone)

| Stored where | What | Survives app close? |
|---|---|---|
| **DataStore (disk)** | Dark mode, language, mode, saved city/lat/lon, verdict toggles, weather diary, farmer plot | Yes |
| **RAM** | Current forecast, hourly strip, verdict list | No — only while app runs |
| **Cache folder** | Share-card PNG when you tap “Share card” | Temporary — OS may delete |

**Important alpha truth:** Weather data is **not** written to a weather database yet. Docs mention Room as optional; it is **not built** in alpha.

---

## 3. Speed — how fast does it feel?

| Action | Typical feel | Why |
|---|---|---|
| Cold open (good network) | 1–3 seconds | Downloads weather + air quality (+ optional second source) |
| Pull to refresh | ~1–2 seconds | Same network fetch |
| Toggle a setting / mode | Instant | Re-computes verdicts from data already in RAM |
| Verdict engine | Milliseconds | Runs on the phone; no server |
| AI chat reply | 2–10+ seconds | Calls Pollinations over internet |
| Hyper-local search | 2–5+ seconds | Weather fetch + AI sentence |

Background: WorkManager refreshes about every **30 minutes** when network is available (updates widgets too).

---

## 4. Offline — does it work?

**Short answer:** Partially. Alpha is **online-first**.

| Feature | Offline? |
|---|---|
| Change settings, mode, language | Yes |
| Read weather diary entries | Yes |
| See current weather / verdicts | **No** on cold start (no saved forecast) |
| Widgets with fresh data | **No** — needs network to refresh |
| AI chat | **No** |
| Hyper-local agent | **No** |
| Rain radar | **No** |
| Morning brief / rain alerts | Need network when worker runs |

**Location fallback (works without GPS):** saved city → IP guess → Hyderabad default.

**Beta candidate:** Save last good forecast to disk so home screen works offline with a “stale · updated X ago” banner.

---

## 5. Alpha vs beta vs final (your plan)

| Phase | What you’re testing |
|---|---|
| **Alpha (now)** | Does the product idea work? Verdicts trusted? Modes useful? |
| **Beta** | Designed UI + revised copy + offline polish + pro testing |
| **Final** | Store-ready, performance, accessibility, cut scope |

---

## 6. Build artifacts (for sharing)

- APK file: `androidApp/build/outputs/apk/debug/KosmosAlpha.apk`
- Home screen name: **Kosmos Alpha**

---

*Last updated: 2026-06-19 — alpha v6.0.0*
