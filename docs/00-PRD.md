# 00 — Product Requirements Document (Android)

> Kosmos for Android — the canonical PRD for v1.0 and the version roadmap beyond it.
> Owner: Satish Kumar. Last updated: 2026-06-19.
> Companion docs: [03-Verdicts.md](./03-Verdicts.md) (engine spec, source of truth), [11-Android.md](./11-Android.md) (architecture), [12-Widgets.md](./12-Widgets.md), [06-Design-System.md](./06-Design-System.md), [10-Master-Brief.md](./10-Master-Brief.md).

---

## 1. Summary

Kosmos is a weather + wellness app that **tells the user what to do, not just what the weather is.** Same Open-Meteo data as any weather app, but the output is interpretation: plain-English actions with specific time windows. Core promise — **never show a number without telling the user what to do with it.**

This PRD covers the **Android** product, built for a **global** audience from day one. Open-Meteo provides worldwide coverage, so the app must support any location, both metric and imperial units, and English as the base language. v1.0 ships **Default mode only**; modes (Farmer, Family, Elder, Travel, Photographer) are deferred to later versions. The app is free forever, no account, no ads.

## 2. Goals & non-goals

**Goals (v1.0)**
- Deliver a widget-first weather app where the home-screen widget is genuinely useful on its own.
- Turn live weather into accurate, deterministic, plain-English insights via the shared VerdictEngine.
- Ship a focused Default experience: hero weather → next 12 hours → insights.
- Work anywhere in the world: any location, metric or imperial units, English base.
- Feel modern and alive: fluid in-app micro-interactions and tasteful widget interactions within Glance limits.

**Non-goals (v1.0)**
- No modes / mode picker (Default only).
- No AI dependency in v1.0 (Ask Kosmos lands in v1.1).
- No second weather source / confidence layer (Open-Meteo only until v3.0).
- No macOS/iOS work — this folder is the Android app only.

## 3. Target users (personas)

The product is global; the personas below are illustrative launch archetypes, not a geographic limit.

- **Ravi** — urban commuter — "will it rain on my bike/commute?" (primary v1 persona)
- **Sneha** — health-conscious parent — "is it safe for my kids outside?"
- **Krishnamurthy** — farmer — served later by Farmer mode
- **Aisha** — digital nomad — served later by Travel overlay
- **Ramesh** — older user wanting simple + big — served later by Elder mode

v1.0 builds for the urban-adult Default audience (Ravi and Sneha) anywhere in the world.

## 4. v1.0 scope — the Default experience

### 4.1 HomeScreen (top to bottom)

1. **Hero block** — large temperature, condition name, feels-like, today's high/low, AQI badge (colored dot, no number), animated character; top bar shows city + date with settings and search icons.
2. **Next 12 hours** — horizontal scroll for the current location: per-hour temp + condition icon.
3. **Insights** — the verdict list in plain English. Example day:
   - "Vitamin D window 9:30–9:45 — 15 min is enough"
   - "Hot today — drink 3L, keep water close"
   - "Scattered rain this evening — carry an umbrella or raincoat"

### 4.2 Insights / VerdictEngine rules

- Source of truth: **[03-Verdicts.md](./03-Verdicts.md)** (the catalog matching the shipped macOS implementation).
- `VerdictEngine.evaluate(snapshot)` returns `List<Verdict>` — never a single verdict.
- Priority tiers: **severe → action → normal.** Show all severe+action; show `normal` only when nothing else fires; `easy` is the no-op default.
- Settings use **base-ID matching**: toggling `vitaminD` hides both `.morning` and `.afternoon`.
- No raw numbers shown to users (AQI = colored badge; UV/feels-like translated to words) unless a future "Show numbers" toggle is enabled.

### 4.3 Widgets (v1.0)

- **Medium (2×1) — PRIORITY, the product for Android users:** temp + condition + 2 top verdicts (title only); single verdict goes full width; AQI as badge only.

### 4.4 Supporting v1.0 features

- **Location:** FusedLocationProvider (last known, instant) → IP fallback (ipwho.is → ipapi.co) → generic last-resort fallback city if all else fails. No region hard-coding.
- **Units & formats:** user-selectable °C/°F, km/h vs mph, 12/24h time; default inferred from device locale. All verdict copy and the hero block respect this.
- **Background refresh:** WorkManager every 30 min (network-constrained); updates DataStore + Glance widget state.
- **Morning notification:** 7 AM (device-local) brief with top 3 verdicts.
- **Settings:** per-verdict toggles (base-ID matching) + units/format prefs.
- **City search:** Open-Meteo geocoding (worldwide).

## 5. Data & accuracy

- **Weather: Open-Meteo** (`api.open-meteo.com/v1/forecast`) — ensemble of ECMWF + GFS + ICON; free, no key. Fields: hourly temp, apparent temp, precip probability, UV, humidity, wind, weather codes; daily high/low; sunrise/sunset.
- **Air quality: Open-Meteo Air-Quality API** — PM2.5/PM10 → AQI; free, no key.
- **Accuracy principles:** verdicts are 100% deterministic from real numbers with documented thresholds — AI never invents a verdict. Location precision comes from real lat/lon (IP fallback is city-level only). Data refreshed every 30 min.
- **Known limitation:** short-term rain timing is the hardest signal anywhere (especially convective/monsoon climates). Addressed later by the Tomorrow.io confidence layer (v3.0), not v1.

## 6. AI / Ask Kosmos

- **Not core.** The app is fully functional without AI.
- **Ask Kosmos chat** (free-form questions like "should I run at 6 PM?") ships in **v1.1** on **Pollinations.ai** (`text.pollinations.ai/openai`) — free, no key, no backend.
- The chat client lives in the shared KMP module behind a **swappable provider interface**, so moving to Gemini Flash free tier or DeepSeek later is a one-file change. Note: any keyed provider requires a key-protecting proxy backend, which is why Pollinations is the v1.1 choice.
- AI is never used to generate verdicts, safety copy, or the morning brief.

## 7. Interaction & motion

The app should feel modern and alive; the widget should feel responsive within its hard technical limits. These are two very different surfaces.

### 7.1 In-app (Jetpack Compose) — rich micro-interactions

- Spring-based transitions (Compose `animate*AsState`, `AnimatedContent`), not linear tweens.
- Verdict cards expand/collapse in place with a smooth height + content crossfade on tap.
- Animated weather character (Compose Canvas): breathing loop + condition accessory.
- Hourly strip: snappy, momentum scroll; subtle parallax/scale on the focused hour.
- Pull-to-refresh with a weather-themed indicator; haptic feedback on refresh complete and on verdict tap.
- Hero block: gradient background animates between conditions; numbers count-up on first load.
- **Reduce Motion:** when the system setting is on, kill particle/looping animation, keep color transitions, show a static character.

### 7.2 Widget (Glance) — interactions within real limits

Android widgets render via RemoteViews; Glance **cannot** do continuous animation, springs, or gestures. What is realistic and what we will ship:

- **Tap targets:** whole widget → open HomeScreen; individual verdict → deep-link to that verdict in the app.
- **Refresh action:** optional tap-to-refresh button that re-runs the worker and re-renders.
- **State re-renders:** content updates smoothly on each 30-min refresh; Android 12+ state selectors for pressed/disabled visuals.
- **Not possible (set expectations):** live looping animation, gesture scrubbing, iOS-style continuous motion. We design the widget to look crisp and update cleanly rather than fake animation.

## 8. Architecture (shared vs Android-only)

- **Shared (KMP `commonMain`):** models (`WeatherSnapshot`, `Verdict`, `HourlyData`, `DailyData`, `AQIData`), `VerdictEngine`, `PlainLanguage`, `OpenMeteoClient` (+ later `TomorrowClient`, `PollinationsClient`), sun/time math.
- **Android-only:** all Jetpack Compose UI, Glance widgets, FusedLocation + geocoding, DataStore/Room, WorkManager workers, notifications.
- **Stack:** Kotlin, Compose, Glance, ktor (shared HTTP), DataStore, WorkManager, FusedLocationProvider. Min SDK 26, Target SDK 35.

## 9. Version roadmap

- **v1.0 — MVP (Default only, global):** KMP shared engine, medium widget (2×1), HomeScreen (hero → 12-hour → insights), location flow, units/format settings, 30-min refresh, 7 AM notification, settings toggles, worldwide city search, in-app micro-interactions.
- **v1.1 — Reach + engagement:** large (4×2) + small (1×1) widgets, rain-alert notification (60 min before), Ask Kosmos chat (Pollinations).
- **v1.2 — Localization:** i18n framework + first languages (e.g. Hindi, Telugu, Spanish — prioritized by install base); optional regional verdict set (`autoRickshaw`, `bike.commute`).
- **v1.3 — Accessibility:** Elder-friendly presentation (big text, high contrast, voice) — first real "mode".
- **v2.0 — Mode system:** mode config layer (enabled verdicts + threshold overrides + tone + UI density), quick mode switcher, Family mode, Travel overlay (auto >100 km).
- **v3.0 — Phase 4 depth:** Farmer mode (soil + monsoon-onset data, replacement catalog, monsoon-countdown widget), Photographer mode, Extra Large (4×4) widget, Tomorrow.io confidence layer.

## 10. Success metrics

- Day-30 retention > 25%.
- A meaningful share of active users have the medium widget placed.
- Insight trust: users act on a verdict (umbrella/walk/hydration) — measured qualitatively at launch.

## 11. Risks & open questions

- **Rain-timing accuracy** with a single free source — mitigated by honest copy now, confidence layer later.
- **Background refresh throttling** on aggressive battery-saver OEMs — accept last-known-location refresh; document for users.
- **Widget interaction expectations** — Glance can't do continuous animation; communicate this in design reviews so the widget isn't over-spec'd.
- **Pollinations reliability** — swappable interface is the hedge.
- **Open:** confirm v1.0 HomeScreen excludes chat/timeline (currently in v1.1) or pull either into v1.0.

---

*Source of truth for verdict logic is [03-Verdicts.md](./03-Verdicts.md). This PRD governs scope and sequencing.*
