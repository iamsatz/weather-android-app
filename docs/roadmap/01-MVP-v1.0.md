# 01 - MVP (v1.0) - Default Mode

> The first complete, shippable Kosmos Android app. Default mode only, works worldwide.
> Source: [docs/00-PRD.md](../00-PRD.md) section 4 + [docs/14-Prototype-Spec.md](../14-Prototype-Spec.md).

---

## Summary

A widget-first weather app that tells you what to do. Live Open-Meteo data runs through the shared VerdictEngine to produce plain-English insights. Home screen, a single insights dropdown, hourly strip, day timeline, settings, city search, a medium home-screen widget, 30-minute background refresh, and a 7 AM morning brief.

## In scope

- Default mode only (no mode picker).
- Worldwide location, metric/imperial units, 12/24h time.
- Home, Timeline, Settings, City search, Ask Kosmos chat (basic).
- Medium (2x1) widget.
- Background refresh + morning notification.
- Polished UI matching the prototype + dark mode.

## Out of scope (later versions)

- Other modes, commute, travel, Kosmos+ (v2.0+).
- Large/small/XL widgets, rain alert (v1.1).
- Other languages (v1.2).

---

## Build phases and sessions

### Phase 1 - Foundation (DONE)

- Session 1.0.1 - KMP models + VerdictEngine + PlainLanguage. Outcome: 17 rules produce verdicts from a snapshot. (done)
- Session 1.0.2 - OpenMeteoClient (weather + AQI) + ktor. Outcome: real forecast for any lat/lon. (done)
- Session 1.0.3 - Android data layer: Weather/Location/Preferences repositories + DataStore. Outcome: app fetches live data with GPS->IP->default fallback. (done)
- Session 1.0.4 - Navigation + screens wired to real data; prototype mock removed. Outcome: app runs on a phone with live weather. (done)

### Phase 2 - UI polish (DONE)

- Session A - Design system. (done)
- Session B - Home fidelity: UPDATE card, neighborhood, units, pull-to-refresh. (done)
- Session C - Verify + package: clean debug APK, smoke-test flows, shareable build. (done)

### Phase 3 - Notifications + dark mode (DONE)

- Session D - Dark mode. (done)
- Session E - Morning brief. `MorningBriefWorker` + 7 AM schedule + test preview in Settings. (done)
- Session F - Reduce Motion + a11y pass. System motion setting, TalkBack labels on key controls. (done)

### Phase 4 - Settings completeness (DONE)

- Session G - Show numbers toggle wired to hero (UV, AQI, humidity). (done)
- Session H - Verdict toggles polish + base-ID matching + enabled count. (done)

---

## Definition of Done (v1.0)

- Live weather worldwide; verdicts accurate and deterministic.
- Home, Timeline, Settings, City search, basic chat all functional and styled.
- Medium widget renders and refreshes.
- 30-min refresh + 7 AM brief working.
- Dark mode + units + 12/24h applied.
- Builds to a signed-debug APK; no crashes in smoke test.

## What you can test after v1.0

- Install on any phone, see your city's weather translated into actions.
- Add the medium widget to your home screen.
- Toggle dark mode and Celsius/Fahrenheit.
- Get a 7 AM brief.

## Outcome for users

A genuinely useful, good-looking weather app that tells them what to do - ready for a wider test group and a Play Store internal track.
