# 14 — HTML Prototype Spec (what to build in Android)

> Everything the clickable prototype demonstrates, how to share it with friends, and how each piece maps to the real Kosmos Android app.
>
> **Prototype file:** `prototype/index.html` (single file — HTML + CSS + JS)
> **Related docs:** [10-Master-Brief.md](10-Master-Brief.md) · [11-Android.md](11-Android.md) · [03-Verdicts.md](03-Verdicts.md) · [12-Widgets.md](12-Widgets.md) · [06-Design-System.md](06-Design-System.md)

---

## 1. How to share the prototype with other people

### Option A — Same WiFi (fastest for friends nearby)

1. On your Mac, open Terminal and run:
   ```bash
   cd "/Users/apple/Documents/Claude Apps/Android Apps/Kosmos/prototype"
   ./serve.sh
   ```
   Or double-click `prototype/start.command`.

2. The script prints two URLs:
   - `http://localhost:8080` — for you on this Mac
   - `http://YOUR-LAN-IP:8080` — for phones/laptops on the **same WiFi**

3. Send the LAN link on WhatsApp. They open it in Chrome/Safari. Works on Android and iPhone.

**Note:** Your Mac must stay awake and the server running. Firewall may block — allow incoming connections for Python if prompted.

### Option B — Send the folder (works offline)

1. Zip the `prototype/` folder (only needs `index.html`, `serve.sh`, `start.command`).
2. Send via WhatsApp / Drive / email.
3. Recipient opens `index.html` in a browser **or** runs `./serve.sh` themselves.

Opening `index.html` directly (`file://`) works for most UI, but some browsers restrict local storage — prefer the small server.

### Option C — Free public link (friends anywhere in the world)

Upload the single file to any static host:

| Service | Steps |
|---------|--------|
| **Netlify Drop** | Go to [app.netlify.com/drop](https://app.netlify.com/drop), drag `prototype/index.html` (or whole folder). Get a URL like `https://random-name.netlify.app` |
| **GitHub Pages** | Push repo to GitHub → Settings → Pages → deploy `prototype/` folder |
| **Surge.sh** | `npm i -g surge` then `cd prototype && surge` |

No backend needed — it's one static HTML file.

### Option D — Temporary tunnel (quick demo link)

If you have ngrok or Cloudflare Tunnel:
```bash
cd prototype && python3 -m http.server 8080
# in another terminal:
ngrok http 8080
```
Share the `https://….ngrok.io` link. Stops when you close the terminal.

### What to tell testers

> "Open this link on your phone. Tap around — change city, try modes in Settings, ask a weather question, check Where to go. Tell me what's confusing or useless."

Collect feedback in a simple Google Form or WhatsApp group.

---

## 2. Screens in the prototype

| Screen | ID | Entry point | Android target |
|--------|-----|-------------|----------------|
| Home | `screen-home` | App launch | `HomeScreen.kt` |
| Settings | `screen-settings` | Gear icon | `SettingsScreen.kt` |
| Day timeline | `screen-timeline` | TODAY → timeline link | `TimelineScreen.kt` |
| Widget preview | `screen-widget` | TODAY → widgets link / Settings | Dev-only preview; real widgets = Glance |
| Where to go | `screen-travel` | TRAVEL card on home | New: `TravelScreen.kt` (Phase 2+) |
| City search | `sheet-search` | Search icon | Bottom sheet / `CitySearchScreen` |
| Ask Kosmos | `sheet-chat` | FAB on home | `ChatScreen.kt` |
| Lock-screen alert | `#lockscreen` overlay | Auto once + Settings → Preview | `NotificationManager` + full-screen intent |

---

## 3. Home screen — components

| Component | Behavior | Real app |
|-----------|----------|----------|
| **GPS location** | Green dot + `Biramguda, Hyderabad · date` | FusedLocationProvider → reverse geocode to neighborhood |
| **Character + temp** | Compact hero (~48px temp, small avatar) | Animated character composable; mood from weather |
| **Verdict cards** | Tap to expand detail; filtered by mode + customize toggles | `VerdictCard` from `VerdictEngine.evaluate()` |
| **Commute line** | One merged sentence on rain/heat cards | Append from `CommuteProfile` + selected modes |
| **UPDATE card** | Plain bulletin: "Rain likely in your area (Biramguda)…" | Nowcast from Open-Meteo + neighborhood name |
| **TRAVEL card** | Opens Where to go screen | Curated destinations (mocked in prototype) |
| **Hourly strip** | Next 12 hours scroll | Open-Meteo hourly |
| **Ask Kosmos FAB** | Opens chat sheet | Pollinations.ai chat |
| **Read aloud** | Alert mock (Kosmos+ badge) | TTS — premium |

### Verdict copy rules (from prototype feedback)

Cards use **time ranges**, not single hours:

- Mosquito: `More mosquitoes 5–9 PM`
- Best walk: `Best walks: 6–8 AM and 6–9 PM` (morning + evening)
- Vitamin D: `8:30–11 AM and 3–5 PM — 15 min is enough`
- Golden hour: `6:42–7:00 PM` (explicit range)

Implement in `PlainLanguage.kt` / verdict templates in shared module.

---

## 4. Modes (personas)

v1.0 ships **Default only**. Others are preview in prototype.

| Mode | ID | Cards (prototype) | Customize list |
|------|-----|-------------------|----------------|
| Default | `default` | City verdicts (~7 per city) | 17 toggle **types** (Rain alerts, Umbrella, …) |
| Employee | `employee` | 7 work-day cards | Each card by title |
| Farmer | `farmer` | 8 crop cards | Each card by title |
| Family | `family` | 8 family cards | Each card by title |
| Elder | `elder` | 3 simplified cards (max 3 on home) | Each card by title |
| Photographer | `photographer` | 7 shoot-window cards | Each card by title |

**Mode switching:** single-select. Stored in `localStorage` key `kosmos-mode`.

**Android:** `DataStore` preference `user_mode`. Default mode only in v1.0; gate others behind Kosmos+ or later phase.

---

## 5. Settings — sections

| Section | Prototype state | Android storage |
|---------|-----------------|-----------------|
| **Your mode** | `state.mode` | DataStore `mode` |
| **Commute** | `state.commute` Set — Bike/Bus/Car/Walk, default Bike+Walk | DataStore multi-select |
| **Appearance** | Dark mode toggle | DataStore + `MaterialTheme` |
| **Units** | Celsius / 12h-24h | DataStore |
| **Widget** | Preview only | Glance implementations |
| **Alerts** | Preview rain alert button | Notification channel + WorkManager |
| **Kosmos+** | Read-only pricing preview | Play Billing (later) |
| **Customize cards** | Mode-aware toggles | See section 6 |
| **Feature catalog** | Read-only accordion from `13-Feature-Catalog` | Not in app UI — internal roadmap |

---

## 6. Customize cards (mode-aware)

**Problem solved:** Default has 17 insight *types*; other modes have different card sets (Employee 7, Farmer 8, etc.).

| Mode | Toggle mechanism | Filter logic |
|------|------------------|--------------|
| Default | `state.enabledVerdicts` Set of 17 base IDs | Base-ID match: `vitaminD` hides `vitaminD.morning` |
| Other modes | `state.hiddenCards[modeId]` Set of hidden card IDs | Exact ID match |

- Customize section visible in **all modes**
- List rebuilds when mode or city changes (`renderVerdictToggles()` in `applyMode`)
- Count label: `"N cards"` dynamic

**Android:**
- Default: `PreferencesRepository.enabledVerdictTypes: Set<String>`
- Modes: `hiddenVerdictIdsByMode: Map<Mode, Set<String>>`
- UI: Settings → "Customize cards" shows current mode's list

---

## 7. Commute advice

- Multi-select: Bike, Bus, Car, Walk (min 1 selected)
- On rain (`raincoat`/`umbrella`) and heat (`heat`/`avoidHours`) verdicts, append **one merged sentence**:
  - Example: *"On a bike or on foot, leave by 3:40 PM with rain cover — roads turn slippery…"*
- Stored: `localStorage` key `kosmos-commute`

**Android:** `CommuteRepository` + template strings keyed by `(category, selectedModes)`.

---

## 8. Ask Kosmos (chat)

- Suggestion chips: dry clothes, rain next hour, bike ride, etc.
- Keyword-matched canned answers (no real AI in prototype)
- 5 free/day shown in subtitle; unlimited = Kosmos+

**Android v1.0:** Pollinations.ai POST with weather context injected. Rate limit free tier.

---

## 9. Hyperlocal UPDATE + rain alert

| Feature | Prototype | Real app |
|---------|-----------|----------|
| UPDATE card | Per-city `nowcast` string, "your area (Area)" format | Rule-based nowcast from hourly rain probability |
| Lock-screen mock | Full-screen overlay, tap dismiss, auto-show once | `NotificationCompat` high-priority + optional full-screen |
| Preview button | Settings → Alerts | Dev / settings test notification |

Copy pattern: *"Rain likely in your area (Biramguda) in about an hour — carry a raincoat if you head out."*

---

## 10. Where to go (travel recommender)

Mock destination list with multi-select filters:

| Filter | Options | Logic |
|--------|---------|-------|
| **Vibe** | Cooler/Hills · Beach/Sea · Anything | OR within row; default Cooler |
| **Good for** | Everyone · Family · Kids · Friends · Couples | OR; tag match on destination |
| **Distance** | <10 · <100 · <500 · Any km | OR: distance ≤ any selected max |
| **Region** | All India + states + Goa | OR; region match |

15 destinations in `TRAVEL_DESTINATIONS` with `vibe`, `tags`, `temp`, `why`.

**Android Phase 2+:** Curated JSON + filter UI. Not v1.0. Could later use weather contrast (hot home → suggest cool/beach).

---

## 11. Widgets (preview)

- **Medium 2×1:** city, temp, condition, AQI, top 2 verdicts
- **Large 4×4:** + hourly strip, top 4 verdicts

See [12-Widgets.md](12-Widgets.md) for Glance specs. Prototype is visual reference only.

---

## 12. Kosmos+ (premium preview)

| Tier | Price (prototype) | Features shown |
|------|-------------------|----------------|
| Free | ₹0 | All verdicts, hyperlocal alerts, 5 Ask/day |
| Kosmos+ | ₹199/yr · ₹399 lifetime | Unlimited Ask, voice (Telugu/English), all modes, multi-location |

No paywall in prototype — badges only on Read aloud + Ask subtitle.

---

## 13. Cities (mock data)

8 cities with unique weather + verdicts: Hyderabad, Delhi, Bengaluru, Mumbai, Chennai, London, New York, Tokyo.

Each city has: `area`, `nowcast`, `verdicts[]`, `hourly[]`, chat intro.

**Android:** Open-Meteo live data; saved cities in Room/DataStore for premium.

---

## 14. What's mocked vs real in the prototype

| Feature | Prototype | Android v1.0 |
|---------|-----------|--------------|
| Weather data | Hard-coded JS | Open-Meteo API (shared KMP client) |
| GPS | Fake green dot + area string | FusedLocationProvider |
| Nowcast / UPDATE | Static strings | Hourly rain + rules |
| Push notifications | CSS lock-screen overlay | NotificationManager |
| Ask Kosmos | Keyword map | Pollinations.ai |
| Travel destinations | Static array | Phase 2+ (curated or API) |
| Modes (non-default) | UI preview | Phase 2+ / Kosmos+ |
| Customize persistence | In-memory (except dark/mode/commute in localStorage) | DataStore |
| Widgets | HTML preview | Glance |

---

## 15. Recommended Android build order (from prototype)

Follow [.cursorrules](../.cursorrules) build order, using prototype as visual spec:

1. **KMP shared** — VerdictEngine, models, OpenMeteoClient ([03-Verdicts.md](03-Verdicts.md))
2. **Medium widget** — matches prototype medium preview ([12-Widgets.md](12-Widgets.md))
3. **HomeScreen** — hero, verdict list, UPDATE card, hourly strip (this doc §3)
4. **Settings** — mode (Default only), commute, customize cards, units, dark mode
5. **WorkManager** — 30 min refresh
6. **Notifications** — morning brief + rain alert (lock-screen style from prototype)
7. **Timeline screen**
8. **Ask Kosmos** — chat with canned → then Pollinations
9. **Large widget**
10. **Phase 2** — Employee/Family modes, travel screen, Kosmos+

---

## 16. Prototype state reference (for porting)

```javascript
state = {
  mode: 'default',
  enabledVerdicts: Set,      // Default customize — 17 types
  hiddenCards: {},         // modeId → Set of hidden card ids
  commute: Set(['bike','walk']),
  travel: { vibes, audiences, distances, regions }, // each a Set
  darkMode, useCelsius, use24h, widgetSize,
  chatMessages, expandedVerdict
}
```

localStorage keys: `kosmos-dark`, `kosmos-mode`, `kosmos-commute`, `kosmos-alert-seen`

---

## 17. Giving feedback → changing the app

1. Share prototype link (§1)
2. Friends test on phone
3. Note: which cards they read, which they ignore, which modes they want
4. Update [03-Verdicts.md](03-Verdicts.md) for new rules
5. Update shared `VerdictEngine` — both prototype (mock) and Android use same logic eventually
6. Keep prototype in sync for the next feedback round

---

*Last updated: June 2026 — matches prototype through mode-aware customize cards + multi-select travel filters.*
