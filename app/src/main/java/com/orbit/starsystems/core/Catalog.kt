package com.orbit.starsystems.core

// The design plays every scene on a uniform 15-second loop.
private fun List<Fact>.at15() = map { if (it.dur == 15f) it else it.copy(dur = 15f) }

/** Every fact across all explorable systems (uniform 15s loop). */
val ALL_FACTS: List<Fact> = (SOL_FACTS + AC_FACTS + TRAPPIST_FACTS + SIRIUS_FACTS + KEPLER_FACTS + SPOTLIGHT_FACTS).at15()

fun factById(id: String?): Fact? = ALL_FACTS.find { it.id == id }

/** Facts belonging to a system id ("sol" / "acen" / "tr" / "sir"). */
fun factsForSys(sys: String): List<Fact> = when (sys) {
    "acen" -> AC_FACTS
    "tr" -> TRAPPIST_FACTS
    "sir" -> SIRIUS_FACTS
    "kep" -> KEPLER_FACTS
    "spot" -> SPOTLIGHT_FACTS
    else -> SOL_FACTS
}.at15()

/** Distinct categories of a system, in first-seen order (used by Explore). */
fun categoriesForSys(sys: String): List<String> = factsForSys(sys).map { it.cat }.distinct()

/** Header copy for each explorable system. */
val SYS_META: Map<String, SysMeta> = mapOf(
    "sol" to SysMeta(
        label = "Sol", eyebrow = "Our system", eyebrowColor = hex(0xff9e34),
        title = "The Solar System",
        blurb = "The Sun and its eight worlds — tap any fact to watch it unfold.",
    ),
    "acen" to SysMeta(
        label = "Alpha Centauri", eyebrow = "Nearest neighbour", eyebrowColor = hex(0xffcf8a),
        title = "Alpha Centauri",
        blurb = "A triple-star system just over four light-years away — the closest stars to our Sun.",
    ),
    "tr" to SysMeta(
        label = "TRAPPIST-1", eyebrow = "Seven worlds", eyebrowColor = hex(0xe0744a),
        title = "TRAPPIST-1",
        blurb = "A single ultracool dwarf circled by seven Earth-size planets, 39 light-years away.",
    ),
    "sir" to SysMeta(
        label = "Sirius", eyebrow = "The brightest star", eyebrowColor = hex(0x8fc0ff),
        title = "Sirius",
        blurb = "The most brilliant star in our night sky — a hot blue-white sun with a dense white-dwarf companion, 8.6 light-years away.",
    ),
    "kep" to SysMeta(
        label = "Kepler-90", eyebrow = "Eight worlds", eyebrowColor = hex(0xa9c2ff),
        title = "Kepler-90",
        blurb = "A Sun-like star with eight known planets — the first system found to rival our own, 2,840 light-years away in Draco.",
    ),
)

/** Still-locked systems shown under "More systems" on the home screen. */
val SYSTEMS: List<StarSystem> = listOf(
    StarSystem("Kepler-186", "580 light-years", "First Earth-size world in a habitable zone",
        listOf(hex(0xffd9c2), hex(0xe0987a), hex(0x8f4a3a))),
)
