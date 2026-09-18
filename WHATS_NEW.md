# What's New — Space Facts

Release notes for **Space Facts** (`com.orbit.starsystems`).

Current version in source: **3.2** (versionCode 32) — see `app/build.gradle.kts`.

> **How this file is maintained**
> - The top section is always the **next** release: work that is merged but not yet live on Google Play.
> - When a version is shipped to the Play Store, its heading is changed from `Unreleased` to `Shipped — YYYY-MM-DD` and a fresh `Unreleased` section is opened above it.
> - New work after that goes into the new `Unreleased` section as it lands.
> - The **Play Store copy** block below is the ready-to-paste text for the Play Console "What's new" field (500 character limit).
> - In-app release notes live in `app/src/main/java/com/orbit/starsystems/ui/WhatsNewSheet.kt` (the `Release` object). Keep the pending section here and that object in step.

---

> **Note:** 3.0 and 3.1 were cut (2026-09-12) without sections here, and the 2.9 and 2.7
> sections below were left marked `Unreleased` after their work had already gone out. They
> are marked "merged" rather than "shipped" because the repo records when each version was
> cut, not when it reached the Play Store — set those dates when you confirm them in the
> Play Console.

---

## 3.2 — Unreleased

**Headline:** A quiz a day, a streak to keep, and a way to find any fact

- **Fact of the day.** One of the 131 facts now leads the Systems tab, chosen fresh each morning and the same for the whole day. It walks the catalog as a shuffled cycle, so nothing repeats until everything else has had its turn.
- **A daily reminder.** Switch it on in the You tab and pick a time — morning, midday, evening or night — and the day's fact arrives as a notification. Tapping it opens that fact directly. Off by default; the permission is only ever asked for on the tap that turns it on.
- **Cosmic Quiz.** Ten multiple-choice questions a round, drawn from a bank of 255 written against the facts already in the app — at least one for every one of the 131. Wrong answers show the right one straight away, and the round ends with every fact you missed, ready to open and read again.
- **Today's quiz, and a streak to keep.** One fixed round a day, worked out from the date alone, so everyone gets the same ten questions. Finishing it keeps your streak — as does watching a fact, so there is still only one streak and only one number to care about. The Systems tab, the quiz and the You tab all show it, with the last seven days as dots and your longest run beside it.
- **Restore a broken streak.** Miss a single day on a run of three or more and you can get it back, that day only, by watching a short video. Capped at once a fortnight, because a streak that can be bought back whenever you like is not a record of anything. If no video will load, the streak is simply given back.
- **A 50/50 on today's quiz.** Once a round, rule out two wrong answers. Optional, and never on practice rounds.
- **The reminder now knows what is at stake.** The same one send a day, at the time you picked, but when a live streak has not been kept yet it points at the quiz and says so, rather than always offering the day's fact.
- Anyone who has bought **Remove ads** gets both the streak repair and the 50/50 outright, with no video.
- **Search.** The library outgrew browsing. Search any fact by name, by what it is, by its category or system, or by a number buried in its blurb or stats — "diamond", "habitable", "1918".
- **Your progress now shows.** Facts you have seen are ticked in every list, each system card carries a progress bar and tells you how many of its facts you have explored, and each category shows how far through it you are.
- **Share a fact as a picture.** The Learn more sheet has a share button that renders the fact's own scene as an image card and hands it to the share sheet — with a link that opens straight back to that fact.
- **A privacy choice for ads.** Where the law gives you one, ads now ask before they personalise, and the choice can be changed at any time from the You tab.
- **The quiz carries an ad, at the end of a round.** A free quiz has to pay for itself somewhere, so the ad goes where the round is already over — after the score, as you tap Done or start another round. Never between questions, never before the score, and never on the back arrow. It obeys the same limits as the rest of the app: nothing in the first minute and a half of a session, nothing within two minutes of any other ad, and never one straight after a video you chose to watch for the 50/50. Anyone who has bought **Remove ads** sees none of it.
- The daily streak now counts days you actually watched a fact or finished the quiz, rather than days you merely opened the app. It also remembers your longest run, which older versions never recorded — an existing streak is carried over intact, with its week of dots filled in.
- Groundwork for measurement: the app can now report anonymous usage to Firebase Analytics once a `google-services.json` is added. Without one, nothing leaves the device and nothing changes.

### Fixed

- **The quiz now keeps your place.** Backing out of a round mid-question used to throw the whole round away, so coming back started again at question one. The round is now held outside the screen: leave it — for a fact, for the home page, for anything — and reopening the quiz drops you back on the same question with your answers intact. The round now survives the app being closed as well: it is written to disk on every answer, so closing Space Facts mid-question and coming back later reopens the round exactly where it was left, rather than starting over. A finished round still clears itself, so the next visit opens the hub, and an unfinished *daily* round left over from a previous day is discarded rather than resumed as "today's quiz".
- **"Update now" works again.** On the forced-update screen, the button could do nothing at all: Google Play only lets each update handle launch its flow once, and the button was replaying the handle already spent when the screen appeared. It now opens the app's Play Store listing instead, so there is always somewhere for the tap to go.

### Play Store copy

```
A quiz a day, and a streak to keep.

Ten questions every day, the same for everyone, written from Space Facts' own 131 facts. Finish them to keep your streak going — and a reminder will tell you when it is at stake.

• Today's quiz — ten questions, a new set each morning
• A streak, your longest run, and the last seven days at a glance
• A fact of the day leading the Systems tab
• Search every fact by name, subject or number
• Share any fact as a picture of its own scene
```

---

## 2.9 — Merged, superseded by 3.1

**Headline:** A new neighbour, and five new wonders

- **A new system: Teegarden's Star.** Ten facts on the red dwarf 12.5 light-years away that nobody catalogued until 2003 — a star barely wider than Jupiter, around eight billion years old, with three small worlds packed inside Mercury's orbit. One of them, Teegarden b, is the closest match to Earth yet measured. And because the system sits in just the right patch of sky, anyone there watching our Sun would see Earth cross its face — they could find us the way we found them.
- **Five new Spotlight facts** — Titan's methane rain, the Oort Cloud at the true edge of the Solar System, the gravitational waves from two colliding black holes, the interstellar visitor ʻOumuamua, and the oldest light in the universe.
- **Ads are far less frequent.** The minimum gap between full-screen ads went from 40 seconds to 2 minutes, they now need four actions instead of two, and there are no ads at all in the first 90 seconds of a session.
- **No more ad when you close a fact.** Opening and closing a fact used to be able to trigger two ads; now only opening one can.
- **No ad on the Compare fly-through.** The full-screen ad that appeared when the comparison animation finished a loop is gone — it interrupted without you having tapped anything.
- **Ads while scrolling facts are rarer**, every two minutes instead of every one.
- **Remove ads permanently** — a one-time purchase in the You tab turns off every ad in the app, banner and full-screen alike. Includes a **Restore purchase** button for a new device or reinstall.
- **Fixed: “Keep exploring” links now actually open.** Tapping a related fact at the bottom of the Learn more sheet did nothing — the sheet closed but the fact behind it stayed put and went silent. It now jumps to the fact you picked.
- **Fixed: facts no longer skip ahead behind a full-screen ad.** The scene animation, its progress bar and the music kept running underneath an ad, so a fact came back mid-way through — or on a completely different beat — once the ad closed. Everything now freezes the moment an ad appears and picks up exactly where it left off. The same applies to the Compare fly-through and to the music on any screen.

### Play Store copy

```
A new neighbour, and five new wonders.

Teegarden's Star joins the library: a red dwarf 12.5 light-years away, missed by every sky survey until 2003, with three small worlds — one of them the closest match to Earth yet measured.

Plus five new Spotlight facts: Titan, the Oort Cloud, gravitational waves, ʻOumuamua and the oldest light there is.

Fewer interruptions, too — full-screen ads are far rarer, and Remove ads turns them off for good.
```

---

## 2.7 — Merged, superseded by 3.1

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
