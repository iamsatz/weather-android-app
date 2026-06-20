# 00 - Kosmos Android Roadmap (Master Index)

> The single place to see where we are, what is next, and what every version delivers.
> Open this first. Every version links to its own detailed doc with sessions.
> Last updated: 2026-06-19

---

## How to read this roadmap

Everything nests in four levels:

```
Platform Phase 3 (Android)
  └─ Version            a shippable milestone (v1.0, v1.1, ...)
       └─ Build Phase   a themed group inside a version (Foundation, Home Polish, ...)
            └─ Session   one focused build chunk we finish together
                 └─ Outcome   what you can see / test after that session
```

- **Version** = what users get in a release.
- **Build Phase** = a chapter of work inside a version.
- **Session** = one sitting of work. Has a goal, the files it touches, and a testable outcome. This is the unit you "start" each time.
- **Outcome** = the visible result, so you always know what changed.

---

## The complete version list

| Version | Theme | What users get | Doc |
|---|---|---|---|
| v1.0 | MVP - Default mode | Working weather app: home, insights, medium widget, refresh | [01-MVP-v1.0.md](01-MVP-v1.0.md) |
| v1.1 | Widgets + alerts | All widget sizes, rain alert, morning brief, chat limits | [02-v1.1.md](02-v1.1.md) |
| v1.2 | Localization | Hindi, Telugu, Tamil; festival context | [03-v1.2.md](03-v1.2.md) |
| v1.3 | Accessibility / Elder | Big text, high contrast, voice-first (first real mode) | [04-v1.3.md](04-v1.3.md) |
| v2.0 | Mode system | Employee, Family, Photographer modes + commute + Kosmos+ | [05-v2.0.md](05-v2.0.md) |
| v2.1 | Travel + social | Where-to-go screen, travel overlay, sharing | [06-v2.1.md](06-v2.1.md) |
| v3.0 | Depth + confidence | Farmer mode, rain confidence, XL widget, 10-day | [07-v3.0.md](07-v3.0.md) |
| v4.0 | Multi-source brain | Fused weather, wind, IMD stub, health Pro, share card | [08-v4.0-plus.md](08-v4.0-plus.md) |
| v5.0 | Weather platform | Voice in/out, wallpaper, radar, UV polish | [08-v4.0-plus.md](08-v4.0-plus.md) |
| v6.0 | Weather depth | Weekly digest, personal diary | [08-v4.0-plus.md](08-v4.0-plus.md) |

Supporting docs:
- [09-Final-App-Vision.md](09-Final-App-Vision.md) - the end-state product (north star).
- [10-Component-Catalog.md](10-Component-Catalog.md) - every UI component + the component-first build order.

---

## Status dashboard (2026-06-19)

Current build: **v6.0.0** — Multi-source fusion through v4; voice, radar, wallpaper (v5); weekly digest + diary (v6).

| Version | Status | Progress |
|---|---|---|
| v1.0 MVP | Complete | 100% |
| v1.1 | Complete | 100% |
| v1.2 | Complete | 100% |
| v1.3 | Complete | 100% |
| v2.0 | Complete | 100% |
| v2.1 | Complete | 100% |
| v3.0 | Complete | 100% |
| v4.0 | **Complete** | 100% |
| v5.0 | **Complete** | 100% |
| v6.0 | **Complete** | 100% |

### What already works (done)

- KMP shared: `MultiSourceClient`, `TomorrowClient`, `ConfidenceEngine` (multi-model), wind fields, `ImdAlertCatalog`, `HealthVerdictEngine`, `WeeklyInsightsEngine`.
- **v4.0:** Fused Open-Meteo + optional Tomorrow.io; rain/heat confidence copy; wind-aware farmer/walk/gust verdicts; hyper-local v2; IMD curated alerts; migraine/asthma Pro; verdict image share.
- **v5.0:** Voice input in chat; TTS brief; weather wallpaper (Pro); RainViewer radar screen; hourly UV on timeline.
- **v6.0:** Weekly insights worker (Sunday digest); weather diary in Settings.

### Removed from roadmap (by design)

- Google Calendar, Google Fit, WhatsApp/Telegram bots, smart home, B2B API, streaks/referrals.

**Kept:** Copy / WhatsApp share intent for weather verdict text (not a bot).

---

## Total scope at a glance
