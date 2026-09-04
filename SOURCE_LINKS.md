# Fact Source Links — Orbit

Per-fact "Read the source" links in the Learn More sheet, rolled out **one system at a time**.
Read this file first when picking up the rollout in a new session.

Status: **16 of 111 keys filled** — Sol complete, every other system still `""`.
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

Nothing to keep consistent by hand — the label comes from the host via `PUBLISHERS`. Sol's links
render as `NASA Science`, `NASA` and `Wikipedia`.

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
| 2 | Alpha Centauri | `facts/acen.json` | 6 | ⬜ |
| 3 | TRAPPIST-1 | `facts/trappist.json` | 8 | ⬜ |
| 4 | Sirius | `facts/sirius.json` | 6 | ⬜ |
| 5 | Kepler-90 | `facts/kepler.json` | 5 | ⬜ |
| 6 | Kepler-186 | `facts/kepler186.json` | 5 | ⬜ |
| 7 | K2-18 | `facts/k218.json` | 9 | ⬜ |
| 8 | 55 Cancri | `facts/cancri.json` | 11 | ⬜ |
| 9 | Barnard's Star | `facts/barnard.json` | 14 | ⬜ |
| 10 | Wolf 359 | `facts/wolf359.json` | 12 | ⬜ |
| 11 | Spotlight | `facts/spotlight.json` | 19 | ⬜ |

111 facts total. Note the exoplanet systems (2–10) will lean much harder on Wikipedia and on
mission pages than Sol did — there is far less NASA consumer-facing copy about K2-18 or Wolf 359
than about Saturn.

---

## Procedure for the next system

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

Open a system whose keys are still `""` (Wolf 359, Spotlight) → any fact → Learn more. The sheet
must render with **no** source button and the `Source · NASA / ESA` fallback.

Then open Sol → *Eight Worlds* → Learn more: button present, credit line reads
`Source · NASA Science`. Those two together prove the map is being read and that an empty value
is inert.

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
