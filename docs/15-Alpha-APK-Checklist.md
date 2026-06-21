# Kosmos Alpha — APK checklist (run before every tester send)

Run on a **real phone** (360dp width or smaller if possible). Do not ship until every row passes.

## Cold start

| # | Test | Pass |
|---|------|------|
| 1 | Kill app, reopen with location permission **already granted** | Home weather visible within **2 seconds** — no full-screen white flash |
| 2 | Fresh install (or clear app data), grant location when asked | Weather appears within **3 seconds**; area name (neighborhood) updates within **10 seconds** **without** tapping "Use current location" |
| 3 | Deny location permission | App still opens with IP/saved city weather — no crash |

## Header and navigation

| # | Test | Pass |
|---|------|------|
| 4 | Long area name (e.g. Gachibowli, Hyderabad) | **Notifications**, **Search**, and **Settings** icons all visible and tappable |
| 5 | Tap each header icon | Opens notifications inbox, city search, settings |
| 6 | 2-tab bottom nav | Today / Plan switch correctly — no Modes tab in Alpha |
| 6a | Home header shows area + city · country | No "GPS" on home screen |
| 6b | Fresh install onboarding | No mode picker — straight to weather after 3 slides |

## Personalization (Settings, optional)

| # | Test | Pass |
|---|------|------|
| 6c | Settings → Add your work place | Saves office; commute verdicts appear on Today |
| 6d | Settings → I have kids | School/kids insights appear when relevant |
| 6e | Settings → I am a woman | SPF + evening safety insights when relevant |

## Location behaviour

| # | Test | Pass |
|---|------|------|
| 7 | Pull to refresh on Today | Content **stays on screen**; spinner only — no white full-screen |
| 8 | Settings → Location section | Shows current area + source (GPS / Saved / IP approx) |
| 9 | Move 500m+ (or pick new city) | Location label updates; weather matches new area |
| 9a | Home search: type Thirumala | Results appear; tap loads weather for that place |
| 9b | Plan: pick Thirumala from search | Trip results reflect that place |
| 9c | Plan: type "hills" while searching | Hills chip NOT auto-selected |

## Modes and widgets

| # | Test | Pass |
|---|------|------|
| 10 | Settings → optional toggles only | No mode picker or mode grid in Alpha |
| 11 | Remind me on a time-window verdict | Notification fires ~10 min before window |
| 12 | Add Today's Call widget | Updates within ~30 min (or after opening app) |

## Offline and errors

| # | Test | Pass |
|---|------|------|
| 13 | Airplane mode → open app | Clear error or stale message — not infinite spinner |
| 14 | Turn network back on → Try again | Weather loads |

## Build

| # | Test | Pass |
|---|------|------|
| 15 | `./gradlew :androidApp:assembleDebug` | BUILD SUCCESSFUL |
| 16 | `./gradlew :androidApp:testDebugUnitTest` | All tests pass |
| 17 | APK label | **Kosmos Alpha** on launcher |

---

**Sign-off:** Name · Date · Device model · Android version

**If any row fails:** fix before sending APK to testers. One bad APK costs trust with the whole group.

See also: [13-Alpha-Designer-Guide.md](13-Alpha-Designer-Guide.md), [14-Diaspora-Alpha-Guide.md](14-Diaspora-Alpha-Guide.md)
