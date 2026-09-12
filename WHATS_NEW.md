# What's New — Space Facts

Release notes for **Space Facts** (`com.orbit.starsystems`).

Current version in source: **2.9** (versionCode 29) — see `app/build.gradle.kts`.

> **How this file is maintained**
> - The top section is always the **next** release: work that is merged but not yet live on Google Play.
> - When a version is shipped to the Play Store, its heading is changed from `Unreleased` to `Shipped — YYYY-MM-DD` and a fresh `Unreleased` section is opened above it.
> - New work after that goes into the new `Unreleased` section as it lands.
> - The **Play Store copy** block below is the ready-to-paste text for the Play Console "What's new" field (500 character limit).
> - In-app release notes live in `app/src/main/java/com/orbit/starsystems/ui/WhatsNewSheet.kt` (the `Release` object). Keep the pending section here and that object in step.

---

## 2.9 — Unreleased

**Headline:** Fewer ads, or none at all

- **Ads are far less frequent.** The minimum gap between full-screen ads went from 40 seconds to 2 minutes, they now need four actions instead of two, and there are no ads at all in the first 90 seconds of a session.
- **No more ad when you close a fact.** Opening and closing a fact used to be able to trigger two ads; now only opening one can.
- **No ad on the Compare fly-through.** The full-screen ad that appeared when the comparison animation finished a loop is gone — it interrupted without you having tapped anything.
- **Ads while scrolling facts are rarer**, every two minutes instead of every one.
- **Remove ads permanently** — a one-time purchase in the You tab turns off every ad in the app, banner and full-screen alike. Includes a **Restore purchase** button for a new device or reinstall.

### Play Store copy

```
Fewer interruptions.

Full-screen ads now appear at most once every two minutes instead of every forty seconds, and never in your first minute and a half. The ad when you closed a fact is gone, and so is the one that interrupted the Compare fly-through.

Prefer none at all? Remove ads is a one-time purchase in the You tab — it turns off every ad in the app, forever.
```

---

## 2.7 — Unreleased

**Headline:** Check the source yourself

- **Source links on every fact** — the Learn More sheet now has a **Read the source** button that opens the exact page a fact came from, in a reader inside the app. Tap the corner icon to hand it off to your browser.
- The source credit under each fact now names its real source instead of a fixed "NASA / ESA" label.
- **All eleven systems are sourced** — 110 of Space Facts' 111 facts carry a verified link, checked against the page's own wording. The one exception is Wolf 359's *Our Sun, Seen From There*, where no published page states the figures; that fact keeps the old credit line and shows no button.

### Play Store copy

```
Don't just take our word for it.

Every fact now has a Read the source button in Learn More — it opens the exact NASA or Wikipedia page the fact came from, right inside the app.

• All eleven systems sourced — 110 facts linked to a page we checked
• Read without leaving Space Facts, or hand off to your browser
• Each fact now credits its real source
```

---

## 2.6 — Shipped 2026-09-04

**Headline:** One new system

- **Wolf 359** — one of the smallest stars known, and the fifth-closest system to the Sun. Twelve facts across four chapters, each with its own animated scene: its hiding place in Leo, a Sun/Jupiter size comparison, its blackbody spectrum against the Sun's, a magnitude ladder, its lifespan on a log timeline, its flares, Max Wolf's photographic plates, the two candidate planets, the week-wide habitable zone, our own Sun seen from its sky, the Star Trek battle, and the distance ladder.
- Wolf 359 removed from the locked **More systems** list now that it is explorable.

## 2.5 — Shipped 2026-09-03

- **55 Cancri** — a super-Earth made of lava, with its own animated scenes and fact set.
- **Barnard's Star** — the fastest star in our sky, with its own animated scenes and fact set.
- New **settings page on the You tab** — reviews, support and app info.
- Your **streak, saved facts and progress now persist** between launches.
- New **What's New sheet** that shows once after an update.
- Update gate for prompting users onto the latest build.

## 2.4 — Shipped 2026-09-02

- Added the **55 Cancri** system (scenes, facts, catalog entry).
- Added the **Barnard's Star** system (scenes, facts, catalog entry).
- Scene toolkit extended to support the new system visuals.

## 2.3 — Shipped 2026-09-01

- Added the **K2-18** system — a possible ocean world.
- New **Spotlight** scenes and spotlight fact set.
- **Banner ads** added, with reworked ad loading and lifecycle handling.
- Comparison screen refinements.
- Bottom navigation now hides on scroll down.

## 2.0 — Shipped 2026-06-28

- Added the **Kepler-186** system — the first Earth-size world found in a habitable zone.
- **Force update** support so users can be moved onto a required build.
- Fact and system JSON restructured for faster loading and easier authoring.

## 1.8 — Shipped 2026-06-26

- **Compare tab** reworked.

## 1.7 — Shipped 2026-06-25

- Added the **Kepler-90** system — a rival to our Solar System.
- Added the **neutron star** scene.
- Black hole animation improved.

## 1.6 — Shipped 2026-06-23

- Added the **Sirius** system — the brightest star in our sky.
- New system integration pipeline.
- Progress bar added.
- Music added to TRAPPIST-1.

## 1.5 — Shipped 2026-06-20

- Maintenance release.

## 1.4 — Shipped 2026-06-20

- Background **music** added.
- **Swipe up** gesture implemented.
- Music keeps playing when the app is minimised.

## 1.3 — Shipped 2026-06-20

- **Navigation grid** updated.

## 1.2 — Shipped 2026-06-20

- Codebase refactor and architecture cleanup.
- **Ads integrated.**

## 1.1 — Shipped 2026-06-20

- App icon added.
- The **Sun** added.
- More facts added.
- Back and save button positions fixed; general UI adjustments.

## 1.0 — Shipped 2026-06-19

- First release.
