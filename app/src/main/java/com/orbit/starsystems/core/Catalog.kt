package com.orbit.starsystems.core

/** Every fact across all explorable systems. */
val ALL_FACTS: List<Fact> = SOL_FACTS + AC_FACTS

fun factById(id: String?): Fact? = ALL_FACTS.find { it.id == id }

/** Facts belonging to a system id ("sol" / "acen"). */
fun factsForSys(sys: String): List<Fact> = if (sys == "acen") AC_FACTS else SOL_FACTS

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
)

/** Still-locked systems shown under "More systems" on the home screen. */
val SYSTEMS: List<StarSystem> = listOf(
    StarSystem("TRAPPIST-1", "39 light-years", "Seven Earth-size worlds, one red dwarf",
        listOf(hex(0xffcaa8), hex(0xe0744a), hex(0x7a2c14))),
    StarSystem("Sirius", "8.6 light-years", "The brightest star in our sky",
        listOf(hex(0xeaf2ff), hex(0xbcd2f0), hex(0x7a93b8))),
    StarSystem("Kepler-90", "2,840 light-years", "Eight known planets — a rival to Sol",
        listOf(hex(0xdfe8ff), hex(0x9ab0e0), hex(0x46598f))),
)
