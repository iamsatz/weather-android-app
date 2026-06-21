# Weather Insight Feature — Full Content Master

> **Scope:** This documents ONE feature — the *insight layer* that converts raw weather into "what should I do today." Not the whole app. This is the complete content picture: every insight once, deduplicated, mapped to the personas that surface it.

> **Philosophy:** People don't want weather. They want a decision. Weather → Insight → Action.

---

## How to read this doc

- **Section A — Canonical Insight Library:** every unique insight, ONE row each, no repeats. The single source of truth.
- **Section B — Persona → Insight Map:** which insights each persona pins (insights are reused, not rewritten).
- **Section C — The 4 Logic Engines:** the reusable computation spine.
- **Section D — Opportunity / Inverse insights:** weather as *gain*, not threat (new).
- **Section E — Local / Cultural insights:** Telangana / Hyderabad-specific (new).
- **Section F — Generic default layer.**
- **Section G — Open content problems.**

**Importance scale:** Extreme (money/safety/season loss) · High (real avoidable cost) · Medium (comfort/efficiency) · Low (nice-to-have)

---

## A. Canonical Insight Library

Each insight appears **once**. The *Personas* column shows reuse. Grouped by theme.

### A1. Drying & Preservation
| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| D1 | Clothes drying (బట్టలు) | Sun + low humidity + no rain 6h + wind | Re-wash wastes a day; musty smell | High | Homemaker, Family, Parents, Apartment, Students |
| D2 | Uniform drying | Same as D1, timed to next school day | Kid needs dry uniform AM | High | Homemaker, Parents, Students |
| D3 | Vadiyalu drying (వడియాలు) | 3–4 hot dry days in a row | Half-dried = fungus, batch ruined | Extreme | Homemaker |
| D4 | Appadalu / papad (అప్పడాలు) | Strong sun, low humidity | Won't crisp; rubbery | High | Homemaker |
| D5 | Pickle / avakaya (ఆవకాయ) | Peak summer dry heat | Moisture = spoilage, oil split | Extreme | Homemaker |
| D6 | Red chillies (ఎండుమిర్చి) | 4–5 day dry spell | Mold; year's stock lost | High | Homemaker |
| D7 | Sandige / fryums (సందిగెలు) | Hot dry midday | Stick & spoil if damp | Medium | Homemaker |
| D8 | Turmeric/tamarind (పసుపు,చింతపండు) | Sustained dry heat | Storage rot | Medium | Homemaker |
| D9 | Grain drying before storage (ధాన్యం) | Dry, breezy day / 3–4 sunny days | Weevils, unsafe storage moisture | Extreme(farmer)/Med(home) | Homemaker, Farmer |
| D10 | Hay / fodder drying | 3–5 day dry breezy spell | Moldy fodder, livestock loss | High | Farmer |
| D11 | Bedding/mattress airing (పరుపులు) | Winter sun | Dust mites, damp smell | Low | Homemaker, Family |
| D12 | Floor / mop drying time | Humidity + low wind | Slippery floors, damp | Low | Homemaker |
| D13 | Hair drying time | Humidity band | Plan bath/outing timing | Low | Homemaker, Women |
| D14 | Paint / whitewash dry window | Dry, low-humidity spell | Paint won't cure in damp | Medium | Painter (sub-persona) |

### A2. Sun / UV / Skin
| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| S1 | Sunscreen SPF 50 (సన్‌స్క్రీన్) | UV ≥8 | Tan, pigmentation, damage | High | Women, Homemaker, Parents, Family, Athletes |
| S2 | Scooter full-cover scarf/gloves (స్కార్ఫ్) | UV ≥6 + dust | Sun + pollution on commute | High | Women, Commuters |
| S3 | Sunglasses / eye protection | UV ≥8 | Cataract, eye strain | Medium | Women, Commuters, Drivers |
| S4 | Hair frizz / sweat forecast | Humidity >75% | Frizz, meltdown | Low | Women |
| S5 | Moisturizer / lip balm | Humidity <30%, winter dry wind | Cracked skin/lips | Medium | Women, Homemaker |
| S6 | Cotton clothing suggestion | Heat + humidity | Comfort, sweat | Low | Women, Generic |
| S7 | Morning sun for Vitamin D | Clear winter AM | Common deficiency | Medium | Women, Homemaker, Elderly |
| S8 | Foot/heel crack care | Dry season | Dry-heat cracking | Low | Women, Homemaker |

**UV→SPF rule (WHO):** 0–2 none · 3–5 SPF30 · 6–7 SPF30–50+cap · 8–10 **SPF50**, avoid 11–3 · 11+ SPF50+ stay in. *(Reapply 2h; clouds don't block UV.)*

### A3. Kids / School
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| K1 | School pickup / bus-stop weather | Rain % + heat-index at start/drop time | Mother + kid exposed in sun/rain | Extreme | Parents, Homemaker, Family, Students |
| K2 | Uniform & layering guide | AM cold + hot PM | Avoid cold/fever | High | Parents, Family |
| K3 | Best kids play window | Heat-index + UV + AQI + rain | Safe 1–2 hr outdoor slot | High | Parents, Family, Students |
| K4 | School holiday / disruption odds | Heavy-rain red alert overnight | Plan the day early | Medium | Parents, Family, Students |
| K5 | Indoor game suggestion | Rain all day / extreme heat | Plan B for energy | Low | Parents, Family |
| K6 | Lightning safety for kids | Lightning forecast | Outdoor danger | High | Parents, Family |
| K7 | Exam-day comfort | Heat/AQI on exam date | Focus, hydration | Low | Students |

### A4. Rain / Storm / Commute
| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| R1 | Umbrella check | Rain % at travel hours | Stay dry — core need | High | Generic, all |
| R2 | Leave early — traffic/flood | Heavy rain at commute hours | Waterlogging, jams, delays | High | Employees, Commuters, Drivers, Healthcare |
| R3 | 2-wheeler safety index | Rain + wind + visibility | Skid/accident risk | Extreme | Commuters, Riders, Motorcyclists |
| R4 | First-rain skid warning | First rain after dry spell | Road oil = slick surface | Extreme | Commuters, Riders, Drivers |
| R5 | Waterlogging / pothole danger | After heavy localized rain | Skidding, hidden potholes | Extreme | Riders, Commuters, Drivers |
| R6 | Highway fog / visibility | Dense fog, <50m | Multi-car pileups | Extreme | Drivers, Travelers, Truckers |
| R7 | Aquaplaning risk | Torrential rain on route | Standing-water skids | High | Drivers, Travelers |
| R8 | Best departure time | Clear window vs rain/fog | Safe travel timing | Medium | Drivers, Travelers, Family |
| R9 | Dust / visor-down alert | High wind + dust | Dust in eyes, low visibility | High | Commuters, Riders, Allergy |
| R10 | Severe weather / storm alert | Storm, hail, lightning, gale | Safety override — all users | Extreme | Generic, all |

### A5. Outage & Prep-Ahead *(secret weapon — lead time)*
| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| O1 | Charge phones/power bank (ఫోన్ చార్జ్) | Storm/heavy rain forecast | Stay reachable in outage | Extreme | Homemaker, Family, Employees, Remote |
| O2 | Charge inverter/light (ఇన్వర్టర్) | Pre-storm | Hours of darkness likely | Extreme | Homemaker, Family |
| O3 | Finish online work / download offline | Net-risk forecast | Net drops in pedha gaali | High | Employees, Remote, Students |
| O4 | Candles/torch ready (కొవ్వొత్తి) | Storm alert | Backup if inverter dies | High | Homemaker, Family |
| O5 | Cook ahead / boil water early | Pre-outage | No power = no mixer/induction | High | Homemaker |
| O6 | Fill water (motor needs power) | Outage forecast | Pump won't run | High | Homemaker, Family |
| O7 | Unplug TV/fridge (ప్లగ్ తీయండి) | Lightning | Surge damage | High | Homemaker, Family |
| O8 | Secure terrace pots/items (కుండీలు) | Strong winds | Flying objects, breakage | High | Homemaker, Gardener, Family |
| O9 | Park away from trees | Pedha gaali | Branch/tree fall | Medium | Family, Drivers |
| O10 | Cash on hand (UPI may fail) | Net-down forecast | No power/net = no UPI | Medium | Family, Employees, Vendors |
| O11 | WFH recommendation | Severe weather + hybrid option | Skip miserable commute | High | Employees, Remote |
| O12 | Room/desk comfort window | Heat/humidity indoors | Productivity slot | Low | Remote, Employees |

### A6. Health-Prep *(PROMPT, never prescribe)*
> ⚠️ Never name medicines/doses. Prompt to *prepare doctor-advised kit* + "see a doctor if symptoms appear." Awareness + timing only. Needs medical/legal review before ship.

| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| H1 | Mosquito risk after rain (దోమలు) | 24–72h post heavy rain, standing water | Dengue/malaria; empty buckets | Extreme | Homemaker, Family, Parents, Generic |
| H2 | Heatstroke awareness (వడదెబ్బ) | Temp >40°C, extreme heat-index | Hydration readiness | High | Homemaker, Family, Elderly, Outdoor workers |
| H3 | Cold/cough season (జలుబు) | Winter onset, sharp temp drop | Kids sick first | High | Homemaker, Parents, Family |
| H4 | Viral fever alert (జ్వరం) | Seasonal change, monsoon | Fevers spike | High | Homemaker, Family |
| H5 | Diarrhea/stomach-bug (విరేచనాలు) | Heat + humidity, monsoon | Food/water spoils | High | Homemaker, Family |
| H6 | Drink boiled/safe water | Monsoon/post-flood | Typhoid, cholera | High | Homemaker, Family, Generic |
| H7 | Eye flu / conjunctivitis | Monsoon humidity | Spreads fast | Medium | Family, Students |
| H8 | Skin/fungal infection | High humidity spell | Rashes, ringworm | Medium | Homemaker, Family |
| H9 | Respiratory / asthma warning | Cold+humid AM, poor AQI, dust | Attacks spike | Extreme | Elderly, Allergy, Asthma |
| H10 | Joint pain / barometric drop | Sharp pressure drop, cold damp | Arthritis flare | High | Elderly |
| H11 | Pollen / thunderstorm-asthma | High wind+pollen; pre-storm gusts | Allergen surge | High | Allergy, Asthma, Parents |
| H12 | Indoor damp & mold risk | High humidity + low wind | Asthma trigger, hygiene | High | Homemaker, Allergy |
| H13 | Vulnerable-member heat caution | Heat-index extreme | Elders, kids, pregnancy risk | Extreme | Family, Elderly, Pregnancy |
| H14 | Food spoilage (milk/curd/leftovers) | Heat + humidity | Health + waste | High | Homemaker, Family, Caterers |

### A7. Comfort / Lifestyle
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| C1 | Feels-like reality | Temp vs humidity gap | True comfort, not raw °C | High | Generic, all |
| C2 | Cooler vs AC decision | Humidity + temp | Cooler useless in humidity | Medium | Homemaker, Family |
| C3 | Office AC layering guide | Hot outside, cold AC inside | Desk comfort | Low | Employees |
| C4 | Kitchen heat comfort | Temp + cooking heat | Plan cooking timing | Low | Homemaker |
| C5 | Terrace usage / sleeping window | Clear, cool, low-mosquito night | Old-Hyd summer habit | Low | Homemaker, Family |
| C6 | Veg shopping before rain | Rain forecast | Price spike, quality drop | Medium | Homemaker, Family |
| C7 | Car wash regret | Rain/muddy showers tomorrow | Wasted effort/money | Medium | Family |
| C8 | Muggu/rangoli skip (ముగ్గు) | Rain washes it | Save effort | Low | Homemaker |
| C9 | Best outdoor errand window | Avoid peak heat/rain | Comfort, safety | Medium | Family, Homemaker, Elderly |

### A8. Farming
| # | Insight (Telugu) | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| F1 | Sowing window (విత్తనం) | Light rain then dry, soil moist | Best germination, no seed rot | Extreme | Farmer |
| F2 | Don't sow before wet spell | Rain in 2–3 days | Seeds rot, re-sow cost | Extreme | Farmer |
| F3 | Safe spray window | Calm, dry, mild AM, no rain 6–8h | Max absorption, no drift | Extreme | Farmer |
| F4 | Don't spray (rain/wind) | Rain <8h OR wind >15km/h | Washes off / drifts, waste | Extreme | Farmer |
| F5 | Irrigation need today | Past rain + ET + heat | Save water/power, avoid stress | Extreme | Farmer |
| F6 | Skip irrigation | Rain forecast | Over-watering, waste | High | Farmer |
| F7 | Fertilizer timing | Heavy rain after application | Nutrient leaching, cost loss | High | Farmer |
| F8 | Harvest before rain (కోత) | Rain in 2–3 days | Sprouting, rot | Extreme | Farmer |
| F9 | Cover harvested heaps | Sudden rain alert | Spoilage of cut crop | Extreme | Farmer |
| F10 | Hailstorm crop cover (వడగళ్లు) | Hail alert | Flowers/fruit destroyed | Extreme | Farmer |
| F11 | Frost protection (మంచు) | Winter night <8°C | Tender crop/veg damage | High | Farmer |
| F12 | Disease/pest surge prep | Humid+warm 3–5 days | Blast/blight outbreaks | High | Farmer |
| F13 | Pollination risk | Heavy rain at flowering | Poor fruit set | High | Farmer |
| F14 | Livestock heat stress (పశువులు) | Heat-index extreme | Milk drop, deaths | High | Farmer, Dairy |
| F15 | Cattle/poultry shelter | Storm/heat/cold extreme | Animal safety, mortality | High | Farmer, Poultry |

### A9. Fishing / Sea
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| FS1 | Gale / cyclone warning | Wind 45km/h+, rough sea | Boats overturn — life risk | Extreme | Fishermen |
| FS2 | Wave height / sea state | Swell forecast | Go/no-go for venturing out | Extreme | Fishermen |
| FS3 | Catch timing | Pressure drop, water temp | Optimal feeding window | Medium | Fishermen |
| FS4 | Visibility at sea | Fog/haze | Navigation safety | High | Fishermen |

### A10. Work / Outdoor Labor
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| W1 | Work-stop rain alert | Rain during 8–5 workday | No work = no income | Extreme | Daily-wage, Construction |
| W2 | Work-hour prediction | Dry windows in the day | Plan shifts around weather | High | Daily-wage, Construction |
| W3 | Concrete curing forecast | Temp >38°C + humidity | Cracks from fast drying | High | Construction |
| W4 | Heat-break planning | Heat-index extreme | Shift labor pre-11/post-4 | Extreme | Construction, Daily-wage, Riders |
| W5 | Lightning work-stop | Lightning forecast | Open-site danger | High | Construction, Daily-wage |
| W6 | Rider heat-fatigue | Extreme UV/heat | Dehydration, dizziness | High | Riders, Delivery |
| W7 | Raincoat / gear nudge | Drizzle during shift | Illness, ruined deliveries | High | Riders, Delivery, Healthcare |

### A11. Photography / Light *(genre = hard mode switch)*
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| P1 | Golden hour quality score | Sun angle + cloud type at sunrise/sunset | #1 light data | Extreme | Photographers, Creators |
| P2 | Blue hour window | Pre-sunrise/post-sunset | Moody cityscapes | High | Photographers, Creators |
| P3 | Cloud cover / soft-light day | Even cloud, no rain | Portraits, even skin | High | Photographers, Creators |
| P4 | Dramatic sky odds | Partial cloud at sunset | Shot of the week | High | Photographers |
| P5 | Harsh midday warning | Clear sun 11–3 | Flat, blown highlights | Medium | Photographers |
| P6 | Fog/mist atmosphere | Dawn humidity | Ethereal shots | Medium | Photographers |
| P7 | Star/Milky Way visibility | Clear + new moon + low humidity | Astro window | High | Photographers |
| P8 | Rain+wind gear risk | Intense rain/wind | Tripod/drone/gear damage | High | Photographers, Creators |
| P9 | Drone flyability | Wind <20km/h, no rain | Safe, stable footage | High | Photographers, Creators |
| P10 | Outdoor shoot comfort index | Heat-index+UV+rain | Bride/groom/guest comfort | Extreme | Wedding/portrait, Creators |
| P11 | Makeup/hair risk | Humidity + wind band | Frizz/meltdown ruins shoot | High | Wedding/portrait |
| P12 | Backup-plan trigger (fixed date) | Rain probability threshold | Shift to indoor/covered | Extreme | Wedding, Event, Creators |
| P13 | Wildlife activity window | Cool calm dawn/dusk, post-rain | Animals active/visible | Extreme | Wildlife photographers |
| P14 | Wind noise (audio) | Wind speed | Ruins video audio | Medium | Creators, Influencers |

### A12. Events / Festivals / Gardening / Energy / Pets
| # | Insight | Trigger | Why | Importance | Personas |
|---|---|---|---|---|---|
| E1 | Tent/setup wind check | Gusts 35km/h+ | Canopies, backdrops blow away | Extreme | Events, Caterers, Wedding planners |
| E2 | Outdoor setup viability (fixed date) | Rain/wind/heat on event day | Go/Plan-B decision | Extreme | Events, Wedding planners, Religious |
| E3 | Procession / crowd comfort | Humidity/heat at march time | Water stations, safety | High | Religious, Events |
| E4 | Decoration / flower protection (పూలు) | Rain forecast | Wilting, washout | Medium | Religious, Events, Family |
| E5 | Diya / rangoli conditions | Wind/rain | Won't stay lit / washes out | Low | Religious, Family |
| E6 | Catering food storage | High ambient heat | Buffet spoilage | High | Caterers, Events |
| G1 | Sun-scorch / shade-net | Extreme UV midday | Kills potted plants | Medium | Gardeners, Family |
| G2 | Smart watering nudge | Rain tonight → skip | Drown/save water | Medium | Gardeners, Family |
| G3 | Frost/wind plant protection | Frost night / strong wind | Plant loss | Medium | Gardeners |
| EN1 | Solar generation forecast | Cloud cover | 40% generation drop | Medium | Solar/EV |
| EN2 | EV range / charging window | Heat (AC drain), cloud | ~15% range drop, plan charge | High | EV owners |
| PT1 | Asphalt paw-burn alert | Ground >50°C | Burns dog paws | Extreme | Pet owners |
| PT2 | Pet walk timing | Heat/rain/lightning | Comfort, anxiety | Medium | Pet owners |

---

## B. Persona → Insight Map

Personas don't get new content — they **pin a subset** of Section A. (IDs reference the library above.)

| Persona | Pinned insights |
|---|---|
| **Generic (default)** | R1, R10, C1, S1(band), H1, H6, + day overview |
| **Homemaker** | D1–D9,D11–D13, S1,S5,S7, K1, O1–O8, H1,H3–H6,H8,H12,H14, C2,C4–C9 |
| **Family** | D1,D11, K1–K6, S1, O1,O2,O4,O6–O10, H1–H6,H13,H14, C2,C5–C7,C9, R8 |
| **Parents** | K1–K7, S1, D2, H1,H3,H11, R1 |
| **Students** | K1,K3,K4,K7, D1,D2, R1, H7 |
| **Women / Beauty** | S1–S8 |
| **Employees** | R1,R2, O3,O11,O12, C1,C3, H2 |
| **Remote workers** | O3,O11,O12,O1, R10 |
| **Commuters (2W)** | R1–R5,R9, S2,S3 |
| **Delivery/Gig riders** | R3–R5,R9, W4,W6,W7, S2 |
| **Daily-wage/Construction** | W1–W5, R10, H2 |
| **Street vendors** | W1(footfall), H14, O10, R1, S6 |
| **Drivers/Travelers** | R6–R8, S3, R10 |
| **Farmers** | F1–F15, D9,D10 |
| **Fishermen** | FS1–FS4, R10 |
| **Photographers** | P1–P9,P13, R10 |
| **Wedding/Event photographers** | P1–P3,P10–P12, E2 |
| **Content creators** | P1–P3,P8,P9,P14, P10 |
| **Athletes/Runners** | K3-style window, H2, S1, H9(AQI) |
| **Elderly/Chronic** | H2,H9,H10,H13, S7, C1 |
| **Allergy/Asthma** | H9,H11,H12, R9, S-none |
| **Events/Caterers** | E1–E6, R10 |
| **Religious/Festival** | E2–E5,E3, R10 |
| **Gardeners** | G1–G3, O8 |
| **Solar/EV** | EN1,EN2 |
| **Pet owners** | PT1,PT2, R10 |

---

## C. The 4 Logic Engines

Every insight is computed by ONE of four reusable engines. Personas + thresholds change; engine code doesn't.

| Engine | Question | Logic | Example insight IDs |
|---|---|---|---|
| **Window Finder** | "When's the *good* time?" | Scan forecast for ideal window | D1–D10, F1,F3,F5, P1,P7,P13, K3 |
| **Risk / Contingency** | "Plan is fixed — what ruins it?" | Flag threats to a locked date | P10,P12, E1,E2, F8 |
| **Go / No-Go** | "Can I work/travel today?" | Binary daily verdict | W1, R3, FS1,FS2, EN2 |
| **Prep-Ahead** | "Act *before* it hits" | Alert hours before impact | O1–O11, H1–H6, R10, S1 |

---

## D. Opportunity / Inverse Insights *(new — weather as GAIN, not threat)*
> Every other doc frames weather as a problem. For some, bad weather = good business.

| Insight | Trigger | Who benefits | Importance |
|---|---|---|---|
| Sales-spike alert: umbrellas/raincoats | Rain forecast | Sellers, vendors | Medium |
| Sales-spike: tea/coffee/snacks | Rain or cold day | Tea stalls, cafes | Medium |
| Sales-spike: ice-cream/cold drinks | Heatwave | Vendors, shops | Medium |
| Sales-spike: cooler/AC/fan | Heat onset | Appliance sellers | Medium |
| Dhobi/laundry peak day | Strong sun | Laundry services | Medium |
| Footfall-up market window | Pleasant evening | All vendors | Medium |

---

## E. Local / Cultural Insights *(new — Telangana / Hyderabad)*
| Insight | Trigger | Why | Importance |
|---|---|---|---|
| Water-tanker booking nudge | Heatwave / supply-cut season | Book before the rush, not during | High |
| Terrace-sleeping window | Clear cool low-mosquito night | Old-Hyd summer habit | Low |
| Bonalu / Bathukamma (outdoor, water/flowers) | Rain/heat on festival day | Procession + decor planning | Medium |
| Ganesh Nimajjanam crowd comfort | Heat/humidity/rain on immersion day | Massive outdoor crowds | Medium |
| Diwali cracker-AQI spike | Festival + low wind | Asthma/kids/elderly warning | High |
| Ramzan fasting + heat | Sehri/iftar timing + heat-index | Hydration window, fasting safety | High |

---

## F. Generic Default Layer
Always-on, persona-agnostic floor shown to everyone:
Rain next 0–3h band · Feels-like (C1) · UV band (S1) · AQI band · Wind/storm risk (R10) · Sunrise/sunset · 3-day outlook · Tomorrow heads-up · Outdoor comfort score · Clothing suggestion.

---

## G. Open Content Problems (still unsolved)

1. **No numbers.** Every "Extreme/High" is a label, not a threshold. Need a values table: UV≥8, rain>60%, wind>25km/h, AQI>150, heat-index>40°C, humidity>75%, etc. **Until this exists, nothing is buildable.**
2. **Prioritization model.** With reuse mapped, you still need the Top-1/2 ranker per persona per day (impact × frequency × today's severity).
3. **Notification budget.** Hard cap (max 1–2/day) or users mute and abandon.
4. **Forecast accuracy honesty.** Hyderabad pre-monsoon *gaali* is hard to predict — frame storm/outage as "likely", never "will".
5. **Health liability.** H-series needs medical + legal review before ship.
6. **Inverse-insight data.** Opportunity cards (Section D) need a sales-correlation source, not just weather.

---

*Status: content scope complete and deduplicated. This is the full picture of the insight feature. The remaining work is numeric (thresholds + scoring), not more personas.*
