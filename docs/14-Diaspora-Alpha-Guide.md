# Kosmos Alpha — Diaspora tester guide

Kosmos is **India-first**, but **Default mode works wherever you are**. This guide is for testers in the UK, US, Australia, and anywhere outside India.

---

## What to use abroad

| Feature | Abroad behavior |
|---------|-----------------|
| **Default mode** | Use this. Rain, heat, UV, walk windows — tuned for your local weather. |
| **Homemaker / Farmer** | Optional. Weather logic works; copy is India-home focused (pickles, monsoon, etc.). Skip unless you want to peek at home-style verdicts. |
| **Forecast data** | Open-Meteo for your GPS coordinates — same engine as in India. |
| **Language** | English, Telugu, Hindi, Tamil — switch in Settings. |
| **Widgets** | Today's Call, Next Rain, Mode-aware, Best Windows — all work locally. |
| **Remind me** | Works on time-window verdicts in your timezone. |

**One-liner for testers:** *"Default mode is for wherever you are. Homemaker and Farmer are India-home modes — optional abroad."*

---

## What we need from you

Please test for **3–5 days** in your normal routine and report:

### 1. Location label

- Does the area name at the top match where you actually are?
- Does it **stay stable** between refreshes (not jump between neighborhood names)?
- Open **Settings** → check **Source** line: should say **GPS**, **Saved**, or **IP approx**.

### 2. Weather trust

- Does rain / heat advice match what you feel outside?
- After midday, do hourly forecasts still feel correct (not stuck on morning hours)?

### 3. Copy clarity

- Any confusing **India-only** phrasing in **Default mode**? (There should be none.)
- If you try Homemaker/Farmer abroad, note anything that feels wrong for your city.

### 4. Widgets & notifications

- Add **Today's Call** or **Next Rain** widget — does it update within ~30 minutes?
- Try **Remind me** on a walk or vitamin-D window — does the notification fire ~10 min before?

---

## Feedback template

Copy-paste into WhatsApp / email / GitHub issue:

```
Kosmos Alpha — diaspora feedback
City: 
Country: 
Mode tested: Default / Homemaker / Farmer
Location label correct? Y/N — what it showed: 
Label stable on refresh? Y/N
Rain/heat felt accurate? Y/N — notes: 
Confusing India copy in Default? Y/N — example: 
Widget tested? Y/N — which: 
Remind me tested? Y/N
Other:
```

---

## Known Alpha limits (not bugs)

- **Weather lives in RAM** while the app is open — force-close clears the live snapshot.
- **Settings and diary persist** on disk; full forecast cache for offline cold-start is **backlog** (Beta).
- **First open needs internet** (~1–3 s). Toggles and mode switches are instant (re-runs verdict engine on cached data).
- **IMD official cyclone alerts** — India only; not wired for UK Met Office / US NWS yet (backlog).

---

## APK

- Label: **Kosmos Alpha**
- File: `KosmosAlpha.apk` from the build Satish shares.

For how memory vs cache works in plain English, see [13-Alpha-Designer-Guide.md](13-Alpha-Designer-Guide.md).
