# Fact Source Links — Orbit

Per-fact "Read the source" links in the Learn More sheet, rolled out **one system at a time**.
Read this file first when picking up the rollout in a new session.

Status: **110 of 111 keys filled** — all eleven systems done. The single exception is `wolf.10`,
which has no citable source and is meant to stay `""` ([why](#wolf10-has-no-source--leave-it-empty)).
All links live in `core/SourceManager.kt`; see [The link map](#the-link-map).

---

## What the feature is

Every fact can carry the URL of a page that states its specific claim. When it does, its Learn
More sheet shows a **"Read the source ↗"** button that opens that page in an in-app WebView,
and the credit line under it names the real source instead of a hardcoded one.

Before this, `ui/DetailSheet.kt` ended every fact with the same dead label — `Source · NASA / ESA` —
identical for all 111 facts, not tappable, and wrong for many of them.

### Decisions

| | |
|---|---|
| Browser surface | In-app WebView (`ui/SourceWebScreen.kt`), top-bar ⧉ hands off to Custom Tabs → `ACTION_VIEW` → toast |
| Sources | Best page per fact (see rules below) |
| Sheet UI | Bordered secondary button under "Save fact", tinted with `fact.accent` |
| Storage | One flat `Map<String, String>` in `core/SourceManager.kt`, keyed `<systemId>.<factNumber>` |
| Publisher label | Derived from the URL host, not stored |

### Code style

Per `~/.claude/CLAUDE.md`: **no comments in source** — no `//`, no KDoc, no banners. Anything
that needs explaining belongs in this file.

---

## The link map

Every source URL lives in **one** Kotlin file: `core/SourceManager.kt`. Nothing about sources is
in the fact JSON any more — the old per-fact `"source": { name, url }` block is gone from
`facts/sol.json` and must not come back.

`SourceManager.LINKS` is a flat `Map<String, String>` keyed **`<systemId>.<factNumber>`**:

```kotlin
private val LINKS: Map<String, String> = mapOf(
    "sol.1" to "https://science.nasa.gov/solar-system/planets/planet-sizes-and-locations-in-our-solar-system/",
    "sol.2" to "https://en.wikipedia.org/wiki/Sun",
    ...
    "wolf.12" to "",
)
```

- `systemId` is the id from `assets/systems.json` — `sol`, `acen`, `tr`, `sir`, `kep`, `k186`,
  `k218`, `55c`, `bar`, `wolf`, plus `spot` for Spotlight.
- `factNumber` is the fact's **1-based position in its own JSON file**, so `sol.1` is the first
  object in `facts/sol.json`. The full key → fact mapping is tabulated in
  [Key reference](#key-reference) at the bottom of this file.
- **All 111 keys are already present.** Filling one in means replacing its `""` with a URL —
  never adding or renaming a key.

### To add a source

Paste the URL into the existing entry:

```kotlin
"acen.4" to "https://en.wikipedia.org/wiki/Proxima_Centauri",
```

That is the whole change. No JSON edit, no code change, no rebuild of anything else.

### Why an empty string is safe

`urlFor()` returns null unless the value starts with `https://`:

```kotlin
fun urlFor(sys: String, num: Int): String? =
    LINKS[keyFor(sys, num)]?.trim()?.takeIf { it.startsWith("https://") }
```

So `""`, whitespace, a stray `http://`, and a missing key all behave identically: no button, and
the credit line falls back to `Source · NASA / ESA` (`SourceManager.FALLBACK_NAME`). That is what
makes filling the map in one fact at a time safe — a half-filled map cannot crash or half-render.

### Reordering a fact file renumbers its keys

The key is a **position**, not the fact's `id`. Insert, delete or reorder objects inside a
`facts/*.json` and every key below the change now points at the wrong fact. If you touch fact
order, re-run the audit below and re-check the affected keys against the
[Key reference](#key-reference).

### The source name is derived, not stored

There is no `name` field to maintain. `publisherOf(url)` maps the URL's host to a label through
`SourceManager.PUBLISHERS` — `science.nasa.gov` → `NASA Science`, `en.wikipedia.org` → `Wikipedia`,
and so on, matching an exact host first, then any parent domain in the table. An unknown host
falls back to the bare host (`example.org`), which is honest but ugly: when a new publisher shows
up, add it to `PUBLISHERS` rather than leaving the raw host on screen.

### Audit script

Replaces the old JSON check. Reports how many keys are filled per system, and flags anything
malformed:

```powershell
python -c "
import re,collections
s=open('app/src/main/java/com/orbit/starsystems/core/SourceManager.kt',encoding='utf-8').read()
body=s.split('LINKS: Map<String, String> = mapOf(')[1].split('
    )')[0]
pairs=re.findall(r'"([^"]+)" to "([^"]*)"',body)
tot=collections.Counter();done=collections.Counter()
for k,v in pairs:
    sysid=k.rsplit('.',1)[0];tot[sysid]+=1
    if v.startswith('https://'):done[sysid]+=1
for sysid in tot: print('%-6s %3d/%-3d' % (sysid,done[sysid],tot[sysid]))
print('total %d/%d' % (sum(done.values()),len(pairs)))
bad=[k for k,v in pairs if v and not v.startswith('https://')]
urls=[v for k,v in pairs if v]
dupes=[u for u,c in collections.Counter(urls).items() if c>1]
print('malformed',bad);print('dupes',dupes)
print('dup keys',[k for k,c in collections.Counter(k for k,v in pairs).items() if c>1])"
```

Expect `malformed []` and `dup keys []`. A repeated URL is not automatically wrong — one page can
legitimately state two facts — but check each one is deliberate rather than a copy-paste slip.

### Publisher labels

Nothing to keep consistent by hand — the label comes from the host via `PUBLISHERS`. Across all
111 keys only four labels ever appear: `NASA Science`, `NASA`, `Wikipedia` and `arXiv` (once, for
`55c.9`). **No new entry had to be added to `PUBLISHERS` for the full rollout** — every host used
was already in the table.

---

## Source-selection rules

In priority order:

1. `science.nasa.gov`
2. other `nasa.gov`
3. `esa.int`
4. `en.wikipedia.org` — only where no agency page states the claim

**The standing rule, which outranks the priority order:** the link must point at a page that
states *this fact's specific claim*, not merely the same planet. Linking `/wiki/Saturn` for a fact
about ring thickness is a bug, not a shortcut.

That rule is why Sol came out 10 NASA / 6 Wikipedia. NASA's own pages contradict or omit the app's
own numbers in six cases — NASA says the Sun is 99.8% of system mass where the fact says 99.86%,
gives Olympus Mons as 16 mi / 25 km / 27 km depending which page you read, frames Uranus as a
"21-year winter" rather than 42 years each way, and asserts Saturn would float without ever
printing the 0.69 g/cm³ density that makes the point. **Prefer NASA, but when NASA's page
contradicts or omits the number the fact states, link the page that actually states it.**

Also require of every URL: `https`, a stable public page, **not** a PDF, not a press release, not
a search-results URL, and it must render acceptably in a mobile WebView.

> ### ⚠ `nssdc.gsfc.nasa.gov` is dead — do not link it
> Every planetary fact-sheet URL (`/planetary/factsheet/jupiterfact.html`, `saturnfact.html`, the
> index) now 307-redirects to `https://www.nasa.gov/nssdc/`. This was the natural second tier for
> numeric claims and its loss is why several Sol facts fell through to Wikipedia. Do not spend
> time retrying it.

---

## Rollout status

| # | System | File | Facts | Sources |
|---|---|---|---|---|
| 1 | Sol / The Solar System | `facts/sol.json` | 16 | ✅ |
| 2 | Alpha Centauri | `facts/acen.json` | 6 | ✅ |
| 3 | TRAPPIST-1 | `facts/trappist.json` | 8 | ✅ |
| 4 | Sirius | `facts/sirius.json` | 6 | ✅ |
| 5 | Kepler-90 | `facts/kepler.json` | 5 | ✅ |
| 6 | Kepler-186 | `facts/kepler186.json` | 5 | ✅ |
| 7 | K2-18 | `facts/k218.json` | 9 | ✅ |
| 8 | 55 Cancri | `facts/cancri.json` | 11 | ✅ |
| 9 | Barnard's Star | `facts/barnard.json` | 14 | ✅ |
| 10 | Wolf 359 | `facts/wolf359.json` | 12 | ✅ 11/12 |
| 11 | Spotlight | `facts/spotlight.json` | 19 | ✅ |

111 facts total, 110 linked. The prediction above held: the exoplanet systems do lean far harder on
Wikipedia than Sol did. Final split, counting every key:

| Publisher | Keys |
|---|---|
| Wikipedia | 82 |
| NASA Science | 24 |
| NASA (`www.nasa.gov`) | 3 |
| arXiv | 1 |
| *(none — `wolf.10`)* | 1 |

NASA holds up where it writes reference pages and collapses where it does not. Sol is 8/16 NASA and
Spotlight 12/19 — solar-system bodies, missions and phenomena all have NASA pages with numbers on
them. The eight exoplanet and nearby-star systems together manage **6 NASA keys out of 70** —
Spotlight is in the table below for contrast, not part of that count:

| System | NASA | Wikipedia | other |
|---|---|---|---|
| TRAPPIST-1 | 3 | 5 | |
| Sirius | 0 | 6 | |
| Kepler-90 | 1 | 4 | |
| Kepler-186 | 1 | 4 | |
| K2-18 | 1 | 8 | |
| 55 Cancri | 0 | 10 | 1 arXiv |
| Barnard's Star | 0 | 14 | |
| Wolf 359 | 0 | 11 | 1 none |
| Spotlight | 12 | 7 | |

Three reasons, all worth knowing before hunting for a NASA page next time:

1. **No reference page exists.** Sirius, Barnard's Star, Wolf 359 and 55 Cancri have NASA imagery
   and news items, and nothing that states a number.
2. **The exoplanet catalog fights the app's own figures.** `exoplanet-catalog/k2-18-b/` gives the
   radius as 2.37× Earth and calls it a "Super Earth" where the app says 2.6× and sub-Neptune;
   `55-cancri-e/` gives no temperature; `kepler-186-f/` never mentions the habitable zone, which is
   the whole point of that fact. Each catalog page carries five numbers and no prose.
3. **Some NASA pages serve no text to a fetch.** `science.nasa.gov/exoplanets/star-catalog/kepler-90/`
   returns the site shell, so its content cannot be verified even though it renders in a browser.
   Never link a page whose claim you could not read.

### Repeated URLs

The audit reports eighteen. All are deliberate: a system's own reference article is usually the only
page carrying every number its facts claim, so it serves several keys.

| Uses | URL |
|---|---|
| 9 | `en.wikipedia.org/wiki/Barnard%27s_Star` |
| 8 | `en.wikipedia.org/wiki/Wolf_359` |
| 6 | `en.wikipedia.org/wiki/Sirius`, `en.wikipedia.org/wiki/K2-18b` |
| 5 | `en.wikipedia.org/wiki/TRAPPIST-1` |
| 4 | `en.wikipedia.org/wiki/Kepler-90`, `.../Kepler-186f`, `.../55_Cancri`, `.../55_Cancri_e` |
| 3 | `science.nasa.gov/exoplanets/trappist1/` |
| 2 | `.../Sun`, `.../Proxima_Centauri`, `.../Olympus_Mons`, `.../Alpha_Centauri`, `.../Interstellar_travel`, `.../K2-18`, `.../Barnard%27s_Star_b`, `.../List_of_nearest_stars` |

Where a distinct page states a claim just as well, it is used instead of piling another key onto the
system article — `bar.6` on `Proper_motion`, `bar.14` and `wolf.11` on `List_of_nearest_stars`,
`spot.5` reusing `Olympus_Mons` from `sol.8`.

---

## Procedure — kept for the next fact or system added

1. Read the system's fact file. For each fact, note the **exact claim and numbers** in `blurb`
   and `stats` — those are what the source has to support.
2. Find one page per fact under the rules above.
3. **Verify each one by actually fetching it.** Confirm it is not a 404 or a redirect to a
   homepage, and confirm the page text states the claim. Do not link from memory of what a page
   probably says.
4. Paste each URL into its existing key in `SourceManager.LINKS` — look the keys up in
   [Key reference](#key-reference).
5. Run the audit script above.
6. Build, then spot-check every fact in the app: sheet shows the button, credit line names the
   right source, page loads.
7. Record any fact where the best available page is only a **partial** match, and what it does not
   state, in "Known-weak links" below.
8. Record any fact whose **copy is not supportable** in "Open: fact copy that no source supports".
   Do not quietly link a page that contradicts the app.
9. Tick the row in the status table, and add a line to `WHATS_NEW.md`.

### Regression check that matters most

Open **Wolf 359 → *Our Sun, Seen From There* → Learn more** — the one remaining empty key. The
sheet must render with **no** source button and the `Source · NASA / ESA` fallback.

Then open Sol → *Eight Worlds* → Learn more: button present, credit line reads
`Source · NASA Science`. Those two together prove the map is being read and that an empty value is
inert. `wolf.10` is now the only fact that can prove the second half, which is one more reason not
to fill it with something unciteable.

Worth one extra tap each, because they are the only two unusual publisher labels in the app:
55 Cancri → *The Diamond Planet* must read `Source · arXiv`, and Spotlight → *The Space Station*
must read `Source · NASA` (not `NASA Science` — it is the only `www.nasa.gov` link outside Sol).

---

## Verified sources — Sol (16/16)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.
Re-verify if a link starts 404ing; NASA reorganises `science.nasa.gov` fairly often.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `sol.1` | `sizes` | Eight Worlds | NASA Science | https://science.nasa.gov/solar-system/planets/planet-sizes-and-locations-in-our-solar-system/ |
| `sol.2` | `sun` | The Sun | Wikipedia | https://en.wikipedia.org/wiki/Sun |
| `sol.3` | `distance` | Mostly Empty | NASA Science | https://science.nasa.gov/neptune/facts/ |
| `sol.4` | `moon` | The Big Gap | NASA Science | https://science.nasa.gov/moon/facts/ |
| `sol.5` | `light` | Light Takes Time | NASA Science | https://science.nasa.gov/mission/voyager/voyager-1/voyager-1-what-is-a-light-day/ |
| `sol.6` | `star` | The Nearest Star | Wikipedia | https://en.wikipedia.org/wiki/Proxima_Centauri |
| `sol.7` | `rings` | Saturn's Rings | NASA Science | https://science.nasa.gov/saturn/facts/ |
| `sol.8` | `olympus` | Olympus Mons | Wikipedia | https://en.wikipedia.org/wiki/Olympus_Mons |
| `sol.9` | `redspot` | The Great Red Spot | Wikipedia | https://en.wikipedia.org/wiki/Great_Red_Spot |
| `sol.10` | `diamondrain` | Diamond Rain | NASA | https://www.nasa.gov/podcasts/gravity-assist/gravity-assist-its-raining-diamonds-on-these-planets/ |
| `sol.11` | `venusday` | Longest Day | NASA Science | https://science.nasa.gov/venus/venus-facts/ |
| `sol.12` | `uranus` | The Tipped Planet | Wikipedia | https://en.wikipedia.org/wiki/Uranus |
| `sol.13` | `mercurytemp` | Fire & Ice | NASA Science | https://science.nasa.gov/mercury/facts/ |
| `sol.14` | `saturnfloat` | It Would Float | Wikipedia | https://en.wikipedia.org/wiki/Saturn |
| `sol.15` | `asteroidbelt` | The Empty Belt | Wikipedia | https://en.wikipedia.org/wiki/Asteroid_belt |
| `sol.16` | `plutoyear` | Pluto's Long Year | Wikipedia | https://en.wikipedia.org/wiki/Pluto |

Exact matches, where the page states every number the fact claims: `sun`, `olympus`, `venusday`,
`mercurytemp`, `saturnfloat`, `asteroidbelt`, `plutoyear`, and `diamondrain` (mechanism only — it
has no numbers to check).

### Known-weak links

Honest but imperfect. Improve if a better page turns up; do not silently treat them as exact.

| key | id | What the linked page does not state |
|---|---|---|
| `sol.5` | `light` | Gives light speed as "about 186,000 miles every second", not the app's `299,792 km/s`. It does state 8 min 20 s and 1.3 s. No single page carries all three. |
| `sol.3` | `distance` | Supports 30 AU, 4.5 B km and the 4-hour light time, but not "1 AU = 150 M km" — that figure is on `science.nasa.gov/earth/facts/`. |
| `sol.10` | `diamondrain` | The only agency source is a **podcast transcript**. Stable and it does explain the mechanism, but conversational rather than a fact page. No dedicated Wikipedia article states the claim as cleanly. |
| `sol.1` | `sizes` | States both diameters (142,984 km / 4,880 km) but **not** "≈ 1,300 Earths inside Jupiter" — see below. |

---

## Verified sources — Alpha Centauri (6/6)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `acen.1` | `ac_triple` | Three Suns | NASA | https://www.nasa.gov/image-article/alpha-centauri-triple-star-system-about-4-light-years-from-earth/ |
| `acen.2` | `ac_waltz` | An 80-Year Waltz | Wikipedia | https://en.wikipedia.org/wiki/Alpha_Centauri |
| `acen.3` | `ac_twin` | A Solar Twin | Wikipedia | https://en.wikipedia.org/wiki/Alpha_Centauri |
| `acen.4` | `ac_proxima` | Proxima Centauri | Wikipedia | https://en.wikipedia.org/wiki/Proxima_Centauri |
| `acen.5` | `ac_proximab` | A Planet Next Door | Wikipedia | https://en.wikipedia.org/wiki/Proxima_Centauri_b |
| `acen.6` | `ac_travel` | So Near, So Far | Wikipedia | https://en.wikipedia.org/wiki/Interstellar_travel |

Exact matches, where the page states every number the fact claims: `ac_waltz` (79.762 yr, periastron
11.2 AU, apastron 35.6 AU — the app's 79 / 11 / 36) and `ac_proximab` (≥1.055 M⊕, 11.18465 d, and
the habitable-zone claim in the lead).

### Why this system came out 1 NASA / 5 Wikipedia

As predicted in the rollout table. NASA has no reference page for Alpha Centauri that carries
numbers — what exists is an image article, Hubble/Webb news items, and an SVS visualisation.
`science.nasa.gov/exoplanet-catalog/proxima-centauri-b/` **was** checked for `acen.5` and does
state 1.055 Earth masses and 11.2 days, but never mentions the habitable zone, which is the whole
point of that fact's blurb; the Wikipedia article states all three claims. The 2016 release
`science.nasa.gov/universe/exoplanets/eso-discovers-earth-size-planet-in-habitable-zone-of-nearest-star/`
does state the zone, but it is a press release (excluded by the rules) and gives Proxima b as
"at least 1.3 times the mass of Earth", contradicting the app's `≈ 1.1× Earth`.

### Its two repeated URLs are deliberate

Both of Alpha Centauri's dupes are intended — see [Repeated URLs](#repeated-urls) for the full
list once every system was done:

- `en.wikipedia.org/wiki/Alpha_Centauri` serves `acen.2` and `acen.3` — one article states the
  orbit (79.762 yr, 11.2–35.6 AU) and both spectral types (G2-V, K1-V orange).
- `en.wikipedia.org/wiki/Proxima_Centauri` serves `acen.4` and, from Sol, `sol.6`. It is the only
  page carrying Proxima's one-seventh diameter, M5.5 class and flare behaviour together.

### Known-weak links — Alpha Centauri

| key | id | What the linked page does not state |
|---|---|---|
| `acen.1` | `ac_triple` | Gives Proxima's separation as "more than 10 thousand times farther from the AB pair than the Earth-Sun distance", not the app's `≈ 13,000 AU`. Consistent, but the number is not printed. Wikipedia's Alpha Centauri article does say "about 13,000 AU" — kept on NASA per the priority order, since nothing here is contradicted. |
| `acen.4` | `ac_proxima` | States `≈ 3,000 K`, where the fact's stat reads `≈ 3,000°C` — see the copy table below. Diameter (one-seventh), M5.5 and the flares are all stated. |
| `acen.6` | `ac_travel` | States 4.243 ly and 75,000 years for Voyager 1, but not the app's `≈ 73,000 yrs`, and not the `≈ 20 yrs` laser-sail figure. That 20 is on `en.wikipedia.org/wiki/Breakthrough_Starshot` ("between 20 and 30 years", 15–20% of light speed), which in turn states neither the distance nor the probe time. No one page carries all three. |

---

## Verified sources — TRAPPIST-1 (8/8)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `tr.1` | `tp_seven` | Seven Earths | NASA Science | https://science.nasa.gov/exoplanets/trappist1/ |
| `tr.2` | `tp_star` | A Star Like Jupiter | Wikipedia | https://en.wikipedia.org/wiki/TRAPPIST-1 |
| `tr.3` | `tp_compact` | Smaller Than One Orbit | Wikipedia | https://en.wikipedia.org/wiki/TRAPPIST-1 |
| `tr.4` | `tp_years` | A Year in Days | Wikipedia | https://en.wikipedia.org/wiki/TRAPPIST-1 |
| `tr.5` | `tp_hz` | The Water Zone | NASA Science | https://science.nasa.gov/exoplanets/trappist1/ |
| `tr.6` | `tp_locked` | Permanent Day & Night | Wikipedia | https://en.wikipedia.org/wiki/TRAPPIST-1 |
| `tr.7` | `tp_sky` | Worlds That Fill the Sky | Wikipedia | https://en.wikipedia.org/wiki/TRAPPIST-1 |
| `tr.8` | `tp_distance` | 39 Light-Years Away | NASA Science | https://science.nasa.gov/exoplanets/trappist1/ |

Exact matches: `tp_star` (radius "12% of that of the Sun", "only slightly larger than the planet
Jupiter", mass "approximately 9%", luminosity "emitted mostly as infrared radiation"), `tp_years`
(1.510826 d and 18.772866 d), `tp_hz` (NASA names e, f and g), `tp_locked` ("permanent day on one
side and permanent night on the other").

### Known-weak links — TRAPPIST-1

| key | id | What the linked page does not state |
|---|---|---|
| `tr.3` | `tp_compact` | Has "All the planets are much closer to their star than Mercury is to the Sun" and the outermost at 0.06189 AU, but never prints Mercury's own 0.39 AU. NASA's page has the framing ("could easily fit inside the orbit of Mercury") and no numbers; Wikipedia has the numbers and not the framing. |
| `tr.7` | `tp_sky` | Says neighbours "would, in many cases, appear larger than Earth's Moon" — not the app's "several × Moon". NASA's wording is weaker still: "as visible as our Moon is from Earth". |
| `tr.8` | `tp_distance` | NASA says "about 40 light-years", not 39 — see the copy table below. Aquarius is confirmed. |

---

## Verified sources — Sirius (6/6)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `sir.1` | `sir_bright` | The Brightest Star | Wikipedia | https://en.wikipedia.org/wiki/Sirius |
| `sir.2` | `sir_binary` | Not One Star, But Two | Wikipedia | https://en.wikipedia.org/wiki/Sirius |
| `sir.3` | `sir_hotter` | Bigger, Hotter, Brighter | Wikipedia | https://en.wikipedia.org/wiki/Sirius |
| `sir.4` | `sir_dwarf` | A Sun the Size of Earth | Wikipedia | https://en.wikipedia.org/wiki/Sirius |
| `sir.5` | `sir_dog` | The Dog Star | Wikipedia | https://en.wikipedia.org/wiki/Sirius |
| `sir.6` | `sir_distance` | 8.6 Light-Years Away | Wikipedia | https://en.wikipedia.org/wiki/Sirius |

All six on one article, deliberately: **NASA has no reference page for Sirius** — what exists is
imagery and Hubble news items. The article carries every number the six facts claim, bar the two
noted below.

Exact matches: `sir_bright` (−1.46, "almost twice as bright as Canopus", Canis Major), `sir_binary`
(A0/A1 plus a DA2 white dwarf, "orbit every 50 years", Bessel's 1844 deduction, Clark's observation
on 31 January 1862), `sir_hotter` (2.063 M☉, 1.7144 R☉, 9,845 K, "25 times as luminous as the
Sun"), `sir_dog` ("The heliacal rising of Sirius marked the flooding of the Nile in Ancient Egypt
and the 'dog days' of summer"), `sir_distance` (8.6 ly).

`sir.6` stays on this article rather than `List_of_nearest_stars`, which gives the Sirius system as
8.7094 ly and would contradict the fact's 8.6.

### Known-weak links — Sirius

| key | id | What the linked page does not state |
|---|---|---|
| `sir.1` | `sir_bright` | Gives Canopus only as "the next brightest star", never its own magnitude, so the stat `Canopus (−0.74)` is uncited. |
| `sir.4` | `sir_dwarf` | States 1.018 M☉ "packed into a volume roughly equal to the Earth's" and a diameter of 12,000 km, which covers the blurb — but prints no density, so `≈ 5 t / teaspoon` rests on arithmetic. |

---

## Verified sources — Kepler-90 (5/5)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `kep.1` | `kep_eight` | Eight Worlds, One Star | Wikipedia | https://en.wikipedia.org/wiki/Kepler-90 |
| `kep.2` | `kep_sunlike` | A Sun Much Like Ours | Wikipedia | https://en.wikipedia.org/wiki/Kepler-90 |
| `kep.3` | `kep_crowded` | Eight Worlds, Packed Tight | Wikipedia | https://en.wikipedia.org/wiki/Kepler-90 |
| `kep.4` | `kep_ai` | Found by Artificial Intelligence | NASA Science | https://science.nasa.gov/universe/exoplanets/discovery-of-eight-planets-makes-alien-system-the-first-to-tie-with-our-solar-system/ |
| `kep.5` | `kep_distance` | 2,840 Light-Years Away | Wikipedia | https://en.wikipedia.org/wiki/Kepler-90 |

Exact matches: `kep_eight` ("the only confirmed planetary system with the same number of observed
planets as the Solar System"), `kep_crowded` ("All of the eight known planet candidates orbit
within about 1 AU", outermost 1.01±0.11 AU), `kep_ai` (NASA: 2017, "machine learning from Google",
and that "the weakest signals often are missed" by the older methods).

`kep.4` is the one place in the rollout where a NASA **news article** is used. Nothing in NASA's
reference catalog mentions the neural network at all — `exoplanet-catalog/kepler-90-i/` lists only
"2017" and "Transit" — and `exoplanets/star-catalog/kepler-90/` serves no article text to a fetch
at all (it returns the site shell), so it cannot be verified even though it renders in a browser.

### Known-weak links — Kepler-90

| key | id | What the linked page does not state |
|---|---|---|
| `kep.2` | `kep_sunlike` | Gives radius 1.2 R☉ and 6,080 K, but types the star **F**, not G — see the copy table. NASA calls it "a Sun-like star" and prints no class or radius. |
| `kep.5` | `kep_distance` | Gives 2,790 light-years, not the app's 2,840. NASA's article says 2,545. See the copy table. Draco is confirmed on both. |

---

## Verified sources — Kepler-186 (5/5)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `k186.1` | `kp_dwarf` | A Smaller, Redder Sun | Wikipedia | https://en.wikipedia.org/wiki/Kepler-186f |
| `k186.2` | `kp_transit` | Caught in Transit | Wikipedia | https://en.wikipedia.org/wiki/Kepler-186f |
| `k186.3` | `kp_hz` | The First Earth in the Zone | Wikipedia | https://en.wikipedia.org/wiki/Kepler-186f |
| `k186.4` | `kp_sunset` | A Sky of Endless Sunset | NASA Science | https://science.nasa.gov/resource/kepler-186f-the-first-earth-size-planet-in-the-habitable-zone-artists-concept/ |
| `k186.5` | `kp_cygnus` | Hidden in the Swan | Wikipedia | https://en.wikipedia.org/wiki/Kepler-186f |

Exact matches: `kp_hz` ("the first planet with a radius similar to Earth's to be discovered in the
habitable zone of another star", "receives about 32% ... near the outer edge"), `kp_cygnus` ("about
580 light-years (180 parsecs) from Earth in the constellation of Cygnus"), and `kp_sunset`, whose
central claim NASA quotes verbatim: "If you could stand on the surface of Kepler-186f, the
brightness of its star at high noon would appear as bright as our sun is about an hour before
sunset on Earth."

### Known-weak links — Kepler-186

| key | id | What the linked page does not state |
|---|---|---|
| `k186.1` | `kp_dwarf` | Has the star as M-type with 0.54 M☉ and 0.52 R☉, but says nothing about M dwarfs being the commonest stars in the galaxy or burning for tens of billions of years — both blurb claims. |
| `k186.2` | `kp_transit` | Confirms the transit method and the Kepler telescope, but prints no transit depth, so `Brightness dip < 0.1%` is uncited. |
| `k186.3` | `kp_hz` | Gives the radius as 1.17× (and 1.21±0.07 R🜨) where the app says "only about 10% larger" — see the copy table. |
| `k186.4` | `kp_sunset` | A one-paragraph image caption. It supports the noon-brightness claim exactly and nothing else: not the deep red light, not the near-black plants. |

---

## Verified sources — K2-18 (9/9)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `k218.1` | `k2_intro` | An Ocean-World Candidate | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.2` | `k2_dwarf` | A Cool Red Dwarf | Wikipedia | https://en.wikipedia.org/wiki/K2-18 |
| `k218.3` | `k2_size` | Between Two Worlds | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.4` | `k2_hz` | In the Habitable Zone | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.5` | `k2_transit` | Caught in Transit | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.6` | `k2_spectrum` | Reading the Starlight | NASA Science | https://science.nasa.gov/mission/webb/science-overview/science-explainers/how-will-webb-study-exoplanets/ |
| `k218.7` | `k2_water` | Water in the Air | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.8` | `k2_hycean` | A Hycean World? | Wikipedia | https://en.wikipedia.org/wiki/K2-18b |
| `k218.9` | `k2_distance` | 124 Light-Years Away | Wikipedia | https://en.wikipedia.org/wiki/K2-18 |

Exact matches: `k2_size` ("a sub-Neptune about 2.6 times the radius of Earth", mass 8.63±1.35 M🜨 —
the app's 2.6× and ≈8.6×), `k2_hz` ("33-day orbit within the star's habitable zone", 0.15910 AU),
`k2_hycean` (JWST methane and CO₂, "ammonia concentrations appear to be unmeasurably low", "the
prototype for hycean planets", and on the dimethyl sulfide claim "there is disagreement about
whether the data ... can in fact be interpreted as proof"), `k2_distance` (124 light-years, Leo).

NASA's catalog page for the planet exists but is the wrong source here: it gives the radius as
2.37× Earth and calls the world a "Super Earth", both of which fight the app's copy, and it prints
no distance.

### Known-weak links — K2-18

| key | id | What the linked page does not state |
|---|---|---|
| `k218.2` | `k2_dwarf` | Gives 0.469 R☉ and 3,645 K where the app says 0.41× and ≈3,500 °C — see the copy table. Nothing about trillion-year lifespans either. |
| `k218.5` | `k2_transit` | Confirms 2015, the Kepler telescope and the transit method; the "K2 mission" name and the 33-day repeat sit elsewhere in the article rather than on the discovery line. |
| `k218.6` | `k2_spectrum` | States the mechanism exactly — starlight "filters through the planet's atmosphere", absorption "will appear as dips in the spectrum", molecules identified by matching those dips against references — but it is a Webb explainer and never mentions K2-18 b. |
| `k218.7` | `k2_water` | Calls the 2019 detection "the first discovery of water vapour on an exoplanet that is not a hot Jupiter", not the app's "first ... around a planet in the habitable zone". |

---

## Verified sources — 55 Cancri (11/11)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `55c.1` | `cn_intro` | The Copernicus System | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri |
| `55c.2` | `cn_family` | The First Family of Five | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_f |
| `55c.3` | `cn_giant` | A Jupiter of Its Own | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_d |
| `55c.4` | `cn_binary` | A Sun and a Distant Ember | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri |
| `55c.5` | `cn_naked` | You Can See It Yourself | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri |
| `55c.6` | `cn_lava` | A World of Molten Rock | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_e |
| `55c.7` | `cn_year` | A Year in Eighteen Hours | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_e |
| `55c.8` | `cn_tidal` | One Face, Forever | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_e |
| `55c.9` | `cn_diamond` | The Diamond Planet | arXiv | https://arxiv.org/abs/1309.6032 |
| `55c.10` | `cn_atmos` | Air Above the Magma | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri_e |
| `55c.11` | `cn_distance` | 41 Light-Years Away | Wikipedia | https://en.wikipedia.org/wiki/55_Cancri |

Exact matches: `cn_intro` ("a binary star system located 41 light-years away ... in Cancer" and "In
December 2015 the IAU announced the winning names were Copernicus for 55 Cancri A and Galileo,
Brahe, Lipperhey, Janssen and Harriot for its planets"), `cn_family` ("its discovery made 55 Cancri
A the first star other than the Sun known to have at least five planets", published 2007),
`cn_giant` (5.58 AU, 5,084±40 d = 13.92 yr, 3.23 M_J, discovered 13 June 2002), `cn_naked`
("apparent magnitude (V) 5.95, making it just visible to the naked eye under very dark skies"),
`cn_lava` (Spitzer's "average day-side temperature of 2,700 K (2,430 °C; 4,400 °F)" and radius
1.856 R🜨 — the app's ≈2,400 °C and 1.9×), `cn_year` (0.73654625 d, i.e. 17.68 h, at 0.01596151 AU),
`cn_atmos` (2024 JWST "evidence for a substantial atmosphere rich in carbon dioxide or carbon
monoxide", ruling out the bare vaporised-rock alternative).

### `55c.9` is the only link in the app outside NASA and Wikipedia

This fact's punchline is the **undoing** of the diamond-planet story, and no encyclopaedia page
carries it. Wikipedia's `55_Cancri_e` states the 2012 proposal ("roughly a third of the planet's
mass would be carbon, much of which may be in the form of diamond") and stops there;
`Carbon_planet` still lists the world as a live candidate on the strength of a C/O ratio of 0.78.
The paper that moved that number is the source: Teske, Cunha, Schuler, Griffith & Smith (2013),
*Carbon and Oxygen Abundances in Cool Metal-rich Exoplanet Hosts: A Case Study of the C/O Ratio of
55 Cancri*, whose abstract gives C/O = 0.78±0.08 against the earlier 1.12 and places the system "at
the sensitive boundary between protoplanetary disk compositions giving rise to planets with high
(>0.8) versus low (<0.8) C/O ratios". An `arxiv.org/abs/` page is HTML, stable and renders on
mobile — it is **not** a PDF link — and `PUBLISHERS` already labels the host `arXiv`.

If a NASA or Wikipedia page ever states the revision plainly, prefer it: a paper abstract is a poor
reading experience for a casual tap.

### Known-weak links — 55 Cancri

| key | id | What the linked page does not state |
|---|---|---|
| `55c.2` | `cn_family` | Carries the five-planet milestone and 2007, but not the 1996 first discovery (that is on `55_Cancri_b`) and not the 0.015–5.5 AU spread (only in the `55_Cancri` planet table). No one page has all three. |
| `55c.3` | `cn_giant` | Does not claim d was **the first** exoplanet on a Jupiter-sized orbit; it says only that it "would be at a similar distance from its star as Jupiter is from the Sun". |
| `55c.4` | `cn_binary` | Has the red-dwarf companion and "85″, an estimated separation of 1,065 AU", but no orbital period for the pair, so "millennia to complete a single lap" is uncited — and it types the primary K0IV–V, not G8. See the copy table. |
| `55c.5` | `cn_naked` | Never prints the naked-eye limit, so `Eye limit ≈ Magnitude 6.5` is uncited. |
| `55c.6` | `cn_lava` | The 2,430 °C the app matches is the Spitzer number; the same page also carries a JWST-era dayside figure of 3,771 K (3,498 °C). The app's number is citeable, but it is the low end of a wide spread. |
| `55c.8` | `cn_tidal` | Tidal locking is "extremely likely" and the night side is given as "<1,649 K (1,376 °C)" — warm, as the fact says — but the page offers no heat-ferrying mechanism, only that tidal and centrifugal forces "can partially confine a hydrogen-rich atmosphere on the nightside". |

---

## Verified sources — Barnard's Star (14/14)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `bar.1` | `br_intro` | Our Nearest Neighbour Alone | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.2` | `br_dwarf` | A Sixth of a Sun | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.3` | `br_dim` | Too Faint to See | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.4` | `br_ancient` | Older Than the Sun | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.5` | `br_flare` | The Old Star Still Erupts | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.6` | `br_runaway` | The Fastest Star in Our Sky | Wikipedia | https://en.wikipedia.org/wiki/Proper_motion |
| `bar.7` | `br_moon` | A Moon-Width in a Lifetime | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.8` | `br_approach` | It Is Coming Closer | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.9` | `br_phantom` | The Planets That Were Not | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.10` | `br_found` | A Real World at Last | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star_b |
| `bar.11` | `br_four` | Four Worlds Smaller Than Earth | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star |
| `bar.12` | `br_roast` | All Four Sit Too Close | Wikipedia | https://en.wikipedia.org/wiki/Barnard%27s_Star_b |
| `bar.13` | `br_daedalus` | The Starship Aimed Here | Wikipedia | https://en.wikipedia.org/wiki/Project_Daedalus |
| `bar.14` | `br_distance` | Six Light-Years Away | Wikipedia | https://en.wikipedia.org/wiki/List_of_nearest_stars |

Exact matches: `br_intro` ("At a distance of 5.96 light-years ... it is the fourth-nearest-known
individual star to the Sun", Ophiuchus, four planets), `br_dwarf` ("about 16% of the Sun's" mass in
"19% of the Sun's diameter", 0.00340 L☉ — the app's 1/300 — and 3,195 K), `br_dim` ("a dim apparent
visual magnitude of +9.5 and is invisible to the unaided eye", Barnard in 1916), `br_runaway` ("the
largest proper motion of all stars, moving at 10.3″ yr−1"), `br_approach` ("The radial velocity of
Barnard's Star is −110 km/s" and closest approach "around 11,800 CE, when it will approach to
within about 3.75 light-years"), `br_phantom` (van de Kamp's 1963 claim of a 1.6 M_J planet,
Gatewood and Eichhorn's 1973 failure to verify, and the signal "attributed to an artifact of
maintenance and upgrade work"), `br_found` (announced 1 October 2024, "radial velocity data from the
ESPRESSO spectrograph on the Very Large Telescope", "minimum mass of 0.3 times the mass of Earth",
"completing an orbit every 3.15 days"), `br_four` (0.193–0.335 M🜨 and 2.34–6.74 d — the app's
0.19–0.34 and 2.3–6.7), `br_daedalus` ("a study conducted between 1973 and 1978 by the British
Interplanetary Society", "to reach Barnard's Star", "about 12% of light speed (0.12 c)", "The trip
was estimated to take 50 years"), `br_distance` (5.9629 ly, third-nearest system after the Sun and
Alpha Centauri).

`bar.6` points at `Proper_motion` rather than the star's own article on purpose: it is the page that
states the superlative and the number in one sentence, and it keeps one of Barnard's keys off an
article that already serves eight.

### Known-weak links — Barnard's Star

| key | id | What the linked page does not state |
|---|---|---|
| `bar.4` | `br_ancient` | "7–12 billion years" brackets the app's ≈10 billion, and "typical of the old, red dwarf population II stars" fits — but the page then says the opposite of the app's stat: "Barnard's Star's metallicity is higher than that of a halo star". See the copy table. |
| `bar.5` | `br_flare` | Confirms a flare in 1998, but as a **temperature** event: "temperature was 8,000 K, more than twice the normal temperature of the star". Nothing about brightness doubling, and no duration. See the copy table. |
| `bar.6` | `br_runaway` | Gives 10.3″ per year, where the app's stat reads 10.4″. |
| `bar.7` | `br_moon` | Gives the rate as "a quarter of a degree in a human lifetime, roughly half the angular diameter of the full Moon". The app's ≈180 years to cross the whole Moon, and "about two-thirds across since 1916", are arithmetic on that rate — correct, but not printed. |
| `bar.10` | `br_found` | Gives the radial-velocity amplitude as "0.440±0.036 m/s" — 44 cm/s, not the app's "barely thirty centimetres a second". That 30 cm/s is ESPRESSO's quoted sensitivity, from the 2024 ESO release. |
| `bar.12` | `br_roast` | Has "orbits closer to the star than the habitable zone and so is too hot to be potentially habitable" and a semi-major axis of 0.0229 AU. The app's outermost orbit ≈0.04 AU follows from the 6.74-day period but is printed nowhere, and the page's equilibrium temperature is 438 K (165 °C), not ≈125 °C. See the copy table. |
| `bar.14` | `br_distance` | Gives the distance and the rank but no constellation, so the `Ophiuchus` stat is carried only by `bar.1`'s page. |

---

## Verified sources — Wolf 359 (11/12)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.
**`wolf.10` is deliberately left empty** — see below.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `wolf.1` | `wf_intro` | The Faintest Star Next Door | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.2` | `wf_tiny` | Barely a Star at All | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.3` | `wf_ember` | A 2,800-Degree Ember | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.4` | `wf_invisible` | You Will Need a Telescope | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.5` | `wf_young` | Young, and Nearly Immortal | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.6` | `wf_flare` | CN Leonis Erupts | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.7` | `wf_maybe` | Two Worlds, Maybe | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359_b |
| `wolf.8` | `wf_hugging` | A Habitable Zone Days Wide | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.9` | `wf_wolf` | Number 359 | Wikipedia | https://en.wikipedia.org/wiki/Wolf_359 |
| `wolf.10` | `wf_oursun` | Our Sun, Seen From There | — | *(none — left empty)* |
| `wolf.11` | `wf_distance` | 7.86 Light-Years | Wikipedia | https://en.wikipedia.org/wiki/List_of_nearest_stars |
| `wolf.12` | `wf_borg` | The Battle That Never Was | Wikipedia | https://en.wikipedia.org/wiki/The_Best_of_Both_Worlds_(Star_Trek:_The_Next_Generation) |

Exact matches: `wf_ember` ("temperature of ~2,800 K", "emitting about 0.1% of the Sun's power", and
a bolometric luminosity of 0.00106 L☉ — the app's 1/1,000), `wf_young` ("relatively young star with
an estimated age of less than a billion years"), `wf_distance` (7.856 ly, fifth-nearest system, with
Luhman 16 and WISE 0855−0714 named as the intervening brown dwarfs — every claim in that fact,
including the rank).

Wolf 359 is the worst-aged system in the app: **five of its twelve facts carry numbers the current
reference pages no longer support**, and one has no source at all. All of it is in the copy table;
nothing was linked to a page that contradicts it without being recorded here.

### `wolf.10` has no source — leave it empty

`wf_oursun` claims the Sun would shine at magnitude 1.8 from Wolf 359, in Aquarius, about as bright
as Dubhe. Both figures are easy to derive — the Sun's absolute magnitude of 4.83 seen from 2.41 pc
gives 1.74, and Wolf 359's antipode on the sky lands in Aquarius — but **no page on any allowed
publisher prints either.** Wikipedia's `Wolf_359` says nothing about the view from there, and a
targeted search turned up only sites outside `PUBLISHERS`, which would render as a raw host in the
credit line.

Per [Why an empty string is safe](#why-an-empty-string-is-safe) this costs nothing: the fact shows
no button and falls back to `Source · NASA / ESA`. Do not fill it from arithmetic — that is exactly
the failure mode the rule exists to prevent. It is also what keeps the regression check below
possible.

### Known-weak links — Wolf 359

| key | id | What the linked page does not state |
|---|---|---|
| `wolf.2` | `wf_tiny` | The page gives 11% of the Sun's mass and a radius of 14.4%, against the app's 9% and 16%, and "a mere 40% wider than the planet" Jupiter against the app's 1.5×. See the copy table. |
| `wolf.3` | `wf_ember` | Has the total output (0.1%) but no visible-light figure, so `≈ 1/100,000 of the Sun` is uncited. |
| `wolf.4` | `wf_invisible` | Confirms magnitude 13.54 and that it "can only be seen with a large telescope", but neither the "six hundred times too faint" comparison nor the ≈20 cm aperture. |
| `wolf.6` | `wf_flare` | Has the CN Leonis designation and flare-star behaviour, and "strong bursts of X-ray and gamma ray radiation" where the app's stat says optical · UV · X-ray. Nothing on "several times in a single day". |
| `wolf.8` | `wf_hugging` | Gives the habitable zone as 0.024–0.052 AU where the app says 0.02–0.04, and never prints Mercury's 0.39 AU for scale. |
| `wolf.9` | `wf_wolf` | Confirms the photographic plates, "He listed this star as entry number 359" and 4.696″/yr — but dates the proper-motion measurement to 1917 and the catalogue to 1919, not 1918. See the copy table. |
| `wolf.12` | `wf_borg` | Confirms the 18 June 1990 air date and that "The *Enterprise* arrives at Wolf 359 to find the fleet has been destroyed", but gives no ship count: the "39 ships" figure comes from *Deep Space Nine*'s "Emissary", not this episode. Nor does it say the real star was chosen for being real and nearby. |

---

## Verified sources — Spotlight (19/19)

Every URL below was fetched and its text checked against the fact's claim on 2026-09-04.

| key | id | Fact | label | `url` |
|---|---|---|---|---|
| `spot.1` | `sp_voyager` | Voyager | NASA Science | https://science.nasa.gov/mission/voyager/voyager-1/ |
| `spot.2` | `sp_sun` | The Sun | Wikipedia | https://en.wikipedia.org/wiki/Sun |
| `spot.3` | `sp_iss` | The Space Station | NASA | https://www.nasa.gov/international-space-station/space-station-facts-and-figures/ |
| `spot.4` | `sp_starship` | Starships to Come | Wikipedia | https://en.wikipedia.org/wiki/Interstellar_travel |
| `spot.5` | `sp_olympus` | Olympus Mons | Wikipedia | https://en.wikipedia.org/wiki/Olympus_Mons |
| `spot.6` | `sp_blackhole` | Black Holes | NASA Science | https://science.nasa.gov/universe/black-holes/ |
| `spot.7` | `sp_wormhole` | Wormholes | Wikipedia | https://en.wikipedia.org/wiki/Wormhole |
| `spot.8` | `sp_pulsar` | Pulsars | Wikipedia | https://en.wikipedia.org/wiki/Pulsar |
| `spot.9` | `sp_neutron` | Neutron Stars | NASA Science | https://science.nasa.gov/universe/stories/quick-reads/neutron-stars-are-weird/ |
| `spot.10` | `sp_supernova` | Supernova | Wikipedia | https://en.wikipedia.org/wiki/Supernova |
| `spot.11` | `sp_milkyway` | The Milky Way | NASA Science | https://science.nasa.gov/universe/exoplanets/our-milky-way-galaxy-how-big-is-space/ |
| `spot.12` | `sp_comet` | Comets | NASA Science | https://science.nasa.gov/solar-system/comets/facts/ |
| `spot.13` | `sp_eclipse` | Total Eclipse | NASA Science | https://science.nasa.gov/eclipses/geometry/ |
| `spot.14` | `sp_kuiper` | The Kuiper Belt | NASA Science | https://science.nasa.gov/solar-system/kuiper-belt/facts/ |
| `spot.15` | `sp_hexagon` | Saturn's Hexagon | NASA Science | https://science.nasa.gov/mission/cassini/science/saturn/hexagon-in-motion/ |
| `spot.16` | `sp_rogue` | Rogue Planets | NASA Science | https://science.nasa.gov/mission/roman-space-telescope/rogue-planets/ |
| `spot.17` | `sp_aurora` | The Aurora | NASA Science | https://science.nasa.gov/sun/auroras/ |
| `spot.18` | `sp_europa` | Europa | NASA Science | https://science.nasa.gov/mission/europa-clipper/why-europa-evidence-for-an-ocean/ |
| `spot.19` | `sp_quasar` | Quasars | Wikipedia | https://en.wikipedia.org/wiki/Quasar |

Spotlight came out 12 NASA / 7 Wikipedia — the best NASA ratio after Sol, because nearly every fact
here is a solar-system or mission topic NASA keeps reference pages for.

Exact matches: `sp_voyager` ("Launched in 1977", "The most distant human-made object", 164.7 AU
≈ 24.6 billion km, interstellar since August 2012), `sp_sun` (1,391,400 km, "around 109 times that
of Earth", "about 99.86% of the total mass of the Solar System", and "about 600 billion kilograms
of hydrogen into helium" each second — 600 million tonnes), `sp_olympus` (21.287 km, "about 2.5
times the elevation of Mount Everest", "about 600 km wide", and both growth mechanisms the fact
names), `sp_hexagon` ("about 20,000 miles (30,000 kilometers) across", "twice as wide as Earth", the
hurricane-like vortex on the pole, and persistence "for decades and, who knows, maybe centuries"),
`sp_kuiper` ("a large, doughnut-shaped region of icy bodies extending far beyond the orbit of
Neptune", inner edge "at about 30 AU", main region ending "around 50 AU", Pluto, "remnants left over
from the formation of the solar system"), `sp_rogue` ("worlds that don't orbit a star", flung out by
close encounters, "outnumbering star-bound worlds by about six to one ... trillions of worlds
wandering alone across our galaxy"), `sp_europa` ("an ocean more than twice the size of Earth's
oceans combined", a global ocean of salty water, tidal heating maintaining it), `sp_aurora` (the
solar wind into Earth's magnetic shield, "Oxygen excited to different energy levels can produce
green and red", "Excited nitrogen gas ... glows blue"), `sp_supernova` ("The peak optical luminosity
of a supernova can be comparable to that of an entire galaxy before fading over several weeks or
months", a major source of the elements, and a progenitor that "either collapses to a neutron star
or black hole"), `sp_milkyway` ("about 100,000 light-years across", "100 to 400 billion stars"),
`sp_eclipse` ("Even though the Sun is about 400 times bigger than the Moon, it is also about 400
times farther away", the corona "too dim to see when the bright solar disk is not covered"),
`sp_iss` ("356 feet (109 meters) end-to-end, one yard shy of the full length of an American football
field", "orbiting Earth about every 90 minutes", "16 sunrises and sunsets"), `sp_comet` ("leftovers
from the dawn of our solar system", "two tails – a dust tail and an ion (gas) tail", blown "away
from the Sun").

### Why the two neutron-star facts are split across publishers

`sp_pulsar` and `sp_neutron` claim nearly the same things, and no single page supports both stat
blocks. NASA's *Neutron Stars Are Weird!* gives "up to two solar masses into a city-size volume" and
"one sugar cube of neutron star material would weigh about 1 billion tons" — the app's mass and
density stats — so it takes `spot.9`. NASA's own **pulsar** page puts a teaspoon at "10 million
tons", which flatly contradicts the app's billion tonnes, so `spot.8` goes to Wikipedia's `Pulsar`,
which states the lighthouse beam and "716 times a second" (the app's "up to 700×/sec") and simply
omits the density.

### Known-weak links — Spotlight

| key | id | What the linked page does not state |
|---|---|---|
| `spot.3` | `sp_iss` | No altitude, so `≈ 408 km` is uncited (NASA's ISS hub says "250 miles"), and speed appears as "five miles per second" — about 29,000 km/h, not the app's 27,600. The 45-minute sunrise interval is arithmetic on 16 orbits a day. See the copy table. |
| `spot.4` | `sp_starship` | Gives fusion rockets "of the order of 10% of that of light" and beamed-laser concepts at 11–50%, so "≈ 10–20% light" sits inside the range without being printed. Proxima at 4.243 ly and the 75,000-year crossing for today's probes are stated. |
| `spot.6` | `sp_blackhole` | Has the event horizon ("nothing – not even light – can escape") and Sagittarius A* at "4 million times the Sun's mass", but never mentions the 2019 M87* image, so `First imaged 2019 · M87*` is uncited. |
| `spot.7` | `sp_wormhole` | Has the Einstein-Rosen bridge, consistency with general relativity and the exotic-matter requirement, but never says outright that none has been observed — it only treats them as hypothetical throughout. |
| `spot.8` | `sp_pulsar` | Has 716 spins per second and the lighthouse beam, but neither the ≈20 km size nor the teaspoon density. |
| `spot.9` | `sp_neutron` | Says "city-size" rather than ≈20 km, weighs a sugar cube rather than a teaspoon, and gives no surface gravity. Wikipedia's `Neutron_star` has the 10 km radius and >10¹¹ × Earth gravity, but weighs a matchbox at 3 billion tonnes and gives the typical mass as 1.4 M☉. |
| `spot.10` | `sp_supernova` | Credits supernovae with elements "from oxygen to rubidium" — **not gold**, which NASA attributes to neutron-star mergers. See the copy table. |
| `spot.11` | `sp_milkyway` | Has the size and the star count but nothing on the 225-million-year galactic orbit. Wikipedia's `Milky_Way` is unusable here: it now gives the diameter as 87,400 light-years, which would contradict the fact's 100,000. See the copy table. |
| `spot.12` | `sp_comet` | No tail length, so `Tens of millions km` is uncited on this page; "a tail that stretches away from the Sun for millions of miles" is on `science.nasa.gov/solar-system/comets/`, which in turn omits the two tails and the leftover-material origin. |
| `spot.13` | `sp_eclipse` | Says totality "may last only a few minutes, or even less" — it does not support `Totality up to ~7.5 min`, nor print the ≈0.5° apparent width of either body. See the copy table. |
| `spot.19` | `sp_quasar` | Supports the trillion-Suns claim via 3C 273 at "about 4 trillion times that of the Sun", plus the supermassive-black-hole engine and the billions-of-light-years distances, but never gives the temperature of the accreting gas. |

---

## Open: fact copy that no source supports

Found while sourcing Sol. **Not yet fixed** — this is real content work, separate from the link
feature, and it is the most valuable thing the sourcing exercise turned up. Attaching a citation
beside an unsupportable number is worse than the dead label it replaced, because now a reader can
check. Worth clearing before the next system.

| key | id | Problem | Suggested fix |
|---|---|---|---|
| `sol.4` | `moon` | Blurb claims **all eight** other planets fit in the Earth–Moon gap. There are only **seven** other planets, and NASA says "just about fit the other seven". | 8 → 7 in the blurb and in the `Fits all 8 planets` stat |
| `sol.7` | `rings` | Says the rings span 282,000 km "edge to edge". NASA says they extend up to 282,000 km **from the planet** — edge-to-edge would be roughly double. Number right, framing wrong. | Reword to "extend 282,000 km from the planet" |
| `sol.1` | `sizes` | "≈ 1,300 Earths inside Jupiter" is on no page. NASA's Jupiter facts page says **1,000**; Wikipedia gives volume 1,321×. | Adopt NASA's 1,000, or cite Wikipedia's 1,321 |
| `sol.9` | `redspot` | "350+ years" is contested — the 1665–1713 spot may be a different storm, and continuous observation dates only to 1831. NASA says "more than 300 years"; NASA Goddard has said "150 years—maybe even much longer". | Soften to NASA's "300+ years" |
| `sol.6` | `star` | Stat reads "By fastest probe ≈ 73,000 years". That figure is **Voyager 1** specifically, which is not the fastest probe (Parker Solar Probe is). "≈ 40 trillion km" also appears on no verifiable page. | Reword to "Voyager 1 would take ≈ 73,000 years" |

A trap worth recording: Wikipedia's "1.94 trillion km" on the Proxima Centauri article is Proxima's
distance from **Alpha Centauri AB**, not from us. Do not use it for the `star` fact.

### Found while sourcing Alpha Centauri

| key | id | Problem | Suggested fix |
|---|---|---|---|
| `acen.3` | `ac_twin` | Stat reads `Radius 1.08× Sun`. Alpha Cen A's radius is **1.22×** the Sun's (Wikipedia: "a radius about 22% larger"); 1.079 is its **mass** in solar masses. The label and the number do not belong together. | Either `Mass 1.08× Sun` or `Radius 1.22× Sun` |
| `acen.4` | `ac_proxima` | Stat reads `Surface ≈ 3,000°C`. The measured effective temperature is ≈ 3,000 **K**, i.e. ≈ 2,730°C — K read as °C, overstating by ~270 degrees. | `≈ 3,000 K`, or `≈ 2,700°C` |
| `acen.6` | `ac_travel` | `Fastest probe ≈ 73,000 yrs` — the same defect already logged for `sol.6`. Wikipedia computes **75,000** years for Voyager 1, and Voyager 1 is not the fastest probe. | `Voyager 1 ≈ 75,000 yrs` |
| `acen.1` | `ac_triple` | Not wrong, just unciteable as written: `≈ 13,000 AU` appears on Wikipedia but on no agency page. Low priority. | Leave, or soften to `> 10,000 AU` to match NASA |

---

### Found while sourcing the remaining nine systems

Same rule as above: none of these is fixed, and none was quietly linked to a page that contradicts
it — every one is recorded in the system's own "Known-weak links" table too. Sorted worst first.

| key | id | Problem | Suggested fix |
|---|---|---|---|
| `wolf.7` | `wf_maybe` | Two of three stats are stale. Wikipedia now reports Wolf 359 **c** as "a false positive, resulting from the rotation of the star rather than a planetary companion", so the app's "warm one circling every 2.7 days" is a signal that has been withdrawn; and it lists **b**'s period as 2,938±436 days — about eight years, not the app's "about 2.7 years". Only "still only candidates" survives, and the fact's own closing line (a flare star can fake the wobble) is exactly what happened. | Rewrite to one surviving candidate: b, ≈8-year period, unconfirmed as of 2023; note c was retracted as stellar rotation. |
| `wolf.2` | `wf_tiny` | Every number is off. Wikipedia gives 11% of the Sun's mass (app: 9%), a radius of 14.4% (app: 16%), and Jupiter comparison "a mere 40% wider" (app: ≈1.5×). "Ninety Jupiters" follows from the app's 9%; 11% is about 115. | 0.11 × Sun, 0.144 × Sun, ≈1.4 × Jupiter, ≈115 Jupiters. |
| `bar.4` | `br_ancient` | Stat reads `Population: Metal-poor halo star`. The source says the opposite: "Barnard's Star's metallicity is higher than that of a halo star". Old and metal-poor relative to the Sun, yes; halo, no. | `Old disc population, metal-poor`. |
| `bar.5` | `br_flare` | Stat reads `Peak: Brightness roughly doubled`. The 1998 flare doubled the **temperature** — "8,000 K, more than twice the normal temperature of the star". No brightness factor and no duration are given anywhere. | `Peak ≈ 8,000 K — twice the star's own`, and drop the unsourced `Duration: Minutes`. |
| `tr.8` | `tp_distance` | 39 light-years and ≈370 trillion km are the pre-Gaia figures. NASA now says "about 40 light-years"; Wikipedia gives 40.66 ly. The fact's title carries the number, so the title changes too. | 40 light-years, ≈385 trillion km — title *40 Light-Years Away*. |
| `kep.5` | `kep_distance` | 2,840 light-years appears on no page. Wikipedia gives 2,790 ly (Gaia); NASA's 2017 article says 2,545 ly. Again the number is in the title. | Adopt 2,790 ly and ≈26 quadrillion km. |
| `wolf.9` | `wf_wolf` | `Found: 1918 · Max Wolf`. Wikipedia dates the proper-motion measurement to **1917** and the catalogue of high-proper-motion stars to **1919**. `wf_intro` repeats 1918 ("nothing was ever recorded at this spot until 1918"). | 1917 for the discovery, 1919 for the catalogue — fix both facts together. |
| `k218.2` | `k2_dwarf` | `Size 0.41× the Sun` is on no page: Wikipedia's star article gives 0.469 R☉, the planet article "45% of the Sun's". And `Surface ≈ 3,500 °C` is the 3,645 **K** figure read as Celsius — the third instance of this defect after `acen.4` and `sir.3`. | 0.47 × Sun; `≈ 3,600 K`. |
| `sir.3` | `sir_hotter` | Blurb says "a blue-white surface near 9,900°C". The measured value is 9,845 **K**, i.e. ≈9,570 °C. | `≈ 9,900 K`. |
| `kep.2` | `kep_sunlike` | Two problems. Wikipedia types Kepler-90 **F**, not G, so "a G-type star much like the Sun" and the `G-type (like Sun)` stat are wrong — though NASA does call it "a Sun-like star" without printing a class. And `Temperature ≈ 6,000 °C` is 6,080 **K** (≈5,800 °C). | Say "Sun-like" without the letter, or F-type; and `≈ 6,100 K`. |
| `bar.12` | `br_roast` | `Barnard b ≈ 125 °C` is the 2024 ESO release's figure; Wikipedia now gives an equilibrium temperature of 438 K (165 °C). The `≈ 0.04 AU` outermost orbit is correct but printed nowhere. | `≈ 165 °C`, or attribute the 125 °C. |
| `k186.3` | `kp_hz` | "Only about 10% larger than Earth" and `≈ 1.1× Earth`. Both linked pages give 1.17× (NASA's catalog too), and Wikipedia's infobox 1.21. | `≈ 1.17× Earth`, "about 17% larger". |
| `k186.1` | `kp_dwarf` | `Size ≈ 0.54× Sun` is the **mass**; the radius is 0.52×. Same label/number mismatch already logged for `acen.3`. | `Mass ≈ 0.54× Sun`, or `Radius ≈ 0.52× Sun`. |
| `55c.4` | `cn_binary` | `Primary: Yellow dwarf (G8)`. Wikipedia types 55 Cancri A **K0IV–V**; G8V is the older catalogue value, so the app is defensible in the literature but contradicts the page a reader will land on. | `Primary: Orange-yellow (K0)`, or drop the class. |
| `spot.11` | `sp_milkyway` | `One orbit ≈ 225 million yrs`. NASA and Wikipedia both give about 240 million years. | `≈ 240 million yrs`. |
| `spot.10` | `sp_supernova` | `Forges: Gold, iron, oxygen`. Wikipedia credits supernovae with "elements ... from oxygen to rubidium"; NASA attributes gold to neutron-star mergers, not supernovae. | Drop gold — `Iron, oxygen, silicon`. |
| `spot.3` | `sp_iss` | `Speed 27,600 km/h` — NASA gives 17,500 mph, i.e. about 28,200 km/h, and "five miles per second" on the facts page. Altitude 408 km is right but printed on neither page (the hub says 250 miles). | `≈ 28,000 km/h`. |
| `spot.13` | `sp_eclipse` | `Totality up to ~7.5 min` is the theoretical maximum and appears on no linked NASA page; the eclipse-geometry page says totality "may last only a few minutes, or even less". | Soften to "a few minutes", or cite an eclipse-specific page carrying the maximum. |
| `tr.1` | `tp_seven` | "the largest known family of rocky worlds around one star" is stated nowhere. Both sources make the narrower claim: most Earth-sized planets **in the habitable zone** of one star. | Match NASA's wording. |
| `55c.3` | `cn_giant` | "the first exoplanet known on an orbit as roomy as our own Jupiter's" — not claimed by any page checked. Low priority; the rest of the fact is exact. | Soften, or leave and accept the weak-link note. |
| `wolf.12` | `wf_borg` | `Fleet lost: 39 ships, in fiction` is right but comes from *Deep Space Nine*'s "Emissary"; the linked TNG episode gives no count. Not an error — just uncitable from the page a reader opens. | Leave, or point the fact at the DS9 episode. |

---

## Where the code lives

| File | Role |
|---|---|
| `core/SourceManager.kt` | **`LINKS`** — the key → URL map, the only file the rollout edits. Also `PUBLISHERS`, `urlFor()`, `sourceFor()`, `publisherOf()`, `FALLBACK_NAME` |
| `core/Models.kt` | `FactSource`, and `Fact.source` |
| `core/OrbitData.kt` | `parseFacts()` numbers each fact 1-based; `toFact()` resolves `source = SourceManager.sourceFor(sys, num)` |
| `app/src/main/assets/facts/*.json` | Fact copy only — **no** `source` blocks |
| `ui/DetailSheet.kt` | The "Read the source ↗" button and the credit line |
| `ui/SourceWebScreen.kt` | The in-app WebView screen |
| `ui/Icons.kt` | The `"external"` icon |
| `AppActions.kt` | `openExternal()` — Custom Tabs → `ACTION_VIEW` → toast |
| `OrbitApp.kt` | `sourceUrl` state and the overlay mount |
| `AndroidManifest.xml` | `INTERNET` permission, Custom Tabs `<queries>` |
| `gradle/libs.versions.toml` | `androidx.browser` |

### Behaviour worth knowing before you touch it

- **`SourceWebScreen` owns its own `BackHandler`**, so back walks the page history first and only
  closes the screen at the start of it. It works because that handler is composed deeper than the
  one in `OrbitApp.kt`. Do not add `sourceUrl` to `OrbitApp`'s `BackHandler` — that would make
  which handler fires ambiguous.
- **The Learn More sheet stays open behind the WebView**, so closing the WebView returns the user
  to the fact's sheet where they left it.
- **Opening a source never fires an interstitial.** It deliberately does not go through
  `withAd`, unlike `openFact` and `exitPlayer`. Citations should not sit behind an ad.
- **`AndroidView`'s `update` lambda must not call `loadUrl` unconditionally** — progress updates
  cause recompositions, so that would be an infinite reload loop. It is guarded on a hoisted
  last-loaded value.
- **JS and DOM storage are required.** `science.nasa.gov` renders almost nothing without them.
  File and content access are explicitly disabled; only remote https is ever loaded.
- **No ProGuard rules are needed.** R8 full mode is on, but the screen adds no
  `@JavascriptInterface` bridge, which is the only WebView construct R8 strips.
- Rotation loses WebView scroll position, because the app declares no `configChanges` and all
  navigation lives in plain `remember`. That is the app's existing behaviour on every screen.

---

## Key reference

Every key in `SourceManager.LINKS`, in map order, with the fact it points at. `factNumber` is the
fact's 1-based position in its JSON file — regenerate this table if you ever reorder a fact file.

### Sol / The Solar System — `sol.*` (`facts/sol.json`, 16 facts)

| key | fact id | title |
|---|---|---|
| `sol.1` | `sizes` | Eight Worlds |
| `sol.2` | `sun` | The Sun |
| `sol.3` | `distance` | Mostly Empty |
| `sol.4` | `moon` | The Big Gap |
| `sol.5` | `light` | Light Takes Time |
| `sol.6` | `star` | The Nearest Star |
| `sol.7` | `rings` | Saturn's Rings |
| `sol.8` | `olympus` | Olympus Mons |
| `sol.9` | `redspot` | The Great Red Spot |
| `sol.10` | `diamondrain` | Diamond Rain |
| `sol.11` | `venusday` | Longest Day |
| `sol.12` | `uranus` | The Tipped Planet |
| `sol.13` | `mercurytemp` | Fire & Ice |
| `sol.14` | `saturnfloat` | It Would Float |
| `sol.15` | `asteroidbelt` | The Empty Belt |
| `sol.16` | `plutoyear` | Pluto's Long Year |

### Alpha Centauri — `acen.*` (`facts/acen.json`, 6 facts)

| key | fact id | title |
|---|---|---|
| `acen.1` | `ac_triple` | Three Suns |
| `acen.2` | `ac_waltz` | An 80-Year Waltz |
| `acen.3` | `ac_twin` | A Solar Twin |
| `acen.4` | `ac_proxima` | Proxima Centauri |
| `acen.5` | `ac_proximab` | A Planet Next Door |
| `acen.6` | `ac_travel` | So Near, So Far |

### TRAPPIST-1 — `tr.*` (`facts/trappist.json`, 8 facts)

| key | fact id | title |
|---|---|---|
| `tr.1` | `tp_seven` | Seven Earths |
| `tr.2` | `tp_star` | A Star Like Jupiter |
| `tr.3` | `tp_compact` | Smaller Than One Orbit |
| `tr.4` | `tp_years` | A Year in Days |
| `tr.5` | `tp_hz` | The Water Zone |
| `tr.6` | `tp_locked` | Permanent Day & Night |
| `tr.7` | `tp_sky` | Worlds That Fill the Sky |
| `tr.8` | `tp_distance` | 39 Light-Years Away |

### Sirius — `sir.*` (`facts/sirius.json`, 6 facts)

| key | fact id | title |
|---|---|---|
| `sir.1` | `sir_bright` | The Brightest Star |
| `sir.2` | `sir_binary` | Not One Star, But Two |
| `sir.3` | `sir_hotter` | Bigger, Hotter, Brighter |
| `sir.4` | `sir_dwarf` | A Sun the Size of Earth |
| `sir.5` | `sir_dog` | The Dog Star |
| `sir.6` | `sir_distance` | 8.6 Light-Years Away |

### Kepler-90 — `kep.*` (`facts/kepler.json`, 5 facts)

| key | fact id | title |
|---|---|---|
| `kep.1` | `kep_eight` | Eight Worlds, One Star |
| `kep.2` | `kep_sunlike` | A Sun Much Like Ours |
| `kep.3` | `kep_crowded` | Eight Worlds, Packed Tight |
| `kep.4` | `kep_ai` | Found by Artificial Intelligence |
| `kep.5` | `kep_distance` | 2,840 Light-Years Away |

### Kepler-186 — `k186.*` (`facts/kepler186.json`, 5 facts)

| key | fact id | title |
|---|---|---|
| `k186.1` | `kp_dwarf` | A Smaller, Redder Sun |
| `k186.2` | `kp_transit` | Caught in Transit |
| `k186.3` | `kp_hz` | The First Earth in the Zone |
| `k186.4` | `kp_sunset` | A Sky of Endless Sunset |
| `k186.5` | `kp_cygnus` | Hidden in the Swan |

### K2-18 — `k218.*` (`facts/k218.json`, 9 facts)

| key | fact id | title |
|---|---|---|
| `k218.1` | `k2_intro` | An Ocean-World Candidate |
| `k218.2` | `k2_dwarf` | A Cool Red Dwarf |
| `k218.3` | `k2_size` | Between Two Worlds |
| `k218.4` | `k2_hz` | In the Habitable Zone |
| `k218.5` | `k2_transit` | Caught in Transit |
| `k218.6` | `k2_spectrum` | Reading the Starlight |
| `k218.7` | `k2_water` | Water in the Air |
| `k218.8` | `k2_hycean` | A Hycean World? |
| `k218.9` | `k2_distance` | 124 Light-Years Away |

### 55 Cancri — `55c.*` (`facts/cancri.json`, 11 facts)

| key | fact id | title |
|---|---|---|
| `55c.1` | `cn_intro` | The Copernicus System |
| `55c.2` | `cn_family` | The First Family of Five |
| `55c.3` | `cn_giant` | A Jupiter of Its Own |
| `55c.4` | `cn_binary` | A Sun and a Distant Ember |
| `55c.5` | `cn_naked` | You Can See It Yourself |
| `55c.6` | `cn_lava` | A World of Molten Rock |
| `55c.7` | `cn_year` | A Year in Eighteen Hours |
| `55c.8` | `cn_tidal` | One Face, Forever |
| `55c.9` | `cn_diamond` | The Diamond Planet |
| `55c.10` | `cn_atmos` | Air Above the Magma |
| `55c.11` | `cn_distance` | 41 Light-Years Away |

### Barnard's Star — `bar.*` (`facts/barnard.json`, 14 facts)

| key | fact id | title |
|---|---|---|
| `bar.1` | `br_intro` | Our Nearest Neighbour Alone |
| `bar.2` | `br_dwarf` | A Sixth of a Sun |
| `bar.3` | `br_dim` | Too Faint to See |
| `bar.4` | `br_ancient` | Older Than the Sun |
| `bar.5` | `br_flare` | The Old Star Still Erupts |
| `bar.6` | `br_runaway` | The Fastest Star in Our Sky |
| `bar.7` | `br_moon` | A Moon-Width in a Lifetime |
| `bar.8` | `br_approach` | It Is Coming Closer |
| `bar.9` | `br_phantom` | The Planets That Were Not |
| `bar.10` | `br_found` | A Real World at Last |
| `bar.11` | `br_four` | Four Worlds Smaller Than Earth |
| `bar.12` | `br_roast` | All Four Sit Too Close |
| `bar.13` | `br_daedalus` | The Starship Aimed Here |
| `bar.14` | `br_distance` | Six Light-Years Away |

### Wolf 359 — `wolf.*` (`facts/wolf359.json`, 12 facts)

| key | fact id | title |
|---|---|---|
| `wolf.1` | `wf_intro` | The Faintest Star Next Door |
| `wolf.2` | `wf_tiny` | Barely a Star at All |
| `wolf.3` | `wf_ember` | A 2,800-Degree Ember |
| `wolf.4` | `wf_invisible` | You Will Need a Telescope |
| `wolf.5` | `wf_young` | Young, and Nearly Immortal |
| `wolf.6` | `wf_flare` | CN Leonis Erupts |
| `wolf.7` | `wf_maybe` | Two Worlds, Maybe |
| `wolf.8` | `wf_hugging` | A Habitable Zone Days Wide |
| `wolf.9` | `wf_wolf` | Number 359 |
| `wolf.10` | `wf_oursun` | Our Sun, Seen From There |
| `wolf.11` | `wf_distance` | 7.86 Light-Years |
| `wolf.12` | `wf_borg` | The Battle That Never Was |

### Spotlight — `spot.*` (`facts/spotlight.json`, 19 facts)

| key | fact id | title |
|---|---|---|
| `spot.1` | `sp_voyager` | Voyager |
| `spot.2` | `sp_sun` | The Sun |
| `spot.3` | `sp_iss` | The Space Station |
| `spot.4` | `sp_starship` | Starships to Come |
| `spot.5` | `sp_olympus` | Olympus Mons |
| `spot.6` | `sp_blackhole` | Black Holes |
| `spot.7` | `sp_wormhole` | Wormholes |
| `spot.8` | `sp_pulsar` | Pulsars |
| `spot.9` | `sp_neutron` | Neutron Stars |
| `spot.10` | `sp_supernova` | Supernova |
| `spot.11` | `sp_milkyway` | The Milky Way |
| `spot.12` | `sp_comet` | Comets |
| `spot.13` | `sp_eclipse` | Total Eclipse |
| `spot.14` | `sp_kuiper` | The Kuiper Belt |
| `spot.15` | `sp_hexagon` | Saturn's Hexagon |
| `spot.16` | `sp_rogue` | Rogue Planets |
| `spot.17` | `sp_aurora` | The Aurora |
| `spot.18` | `sp_europa` | Europa |
| `spot.19` | `sp_quasar` | Quasars |
