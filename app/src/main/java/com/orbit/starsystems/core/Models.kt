package com.orbit.starsystems.core

import androidx.compose.ui.graphics.Color

/** Identifies which animated scene a fact renders. */
enum class SceneId {
    // Sol — scale & distance
    SIZES, SUN, DISTANCE, MOON, LIGHT, STAR,
    // Sol — Worlds Up Close (+ a few extras in existing categories)
    RINGS, VOLCANO, STORM, DIAMOND, VENUSDAY, URANUS, MERCURYTEMP, SATURNFLOAT, ASTEROIDBELT, PLUTOYEAR,
    // Alpha Centauri
    AC_TRIPLE, AC_WALTZ, AC_TWIN, AC_PROXIMA, AC_PROXIMAB, AC_TRAVEL,
    // TRAPPIST-1
    TP_INTRO, TP_STAR, TP_MERCURY, TP_ORBITS, TP_HABITABLE, TP_TIDAL, TP_SKY, TP_DISTANCE,
    // Sirius
    SIR_BRIGHTEST, SIR_BINARY, SIR_HOTTER, SIR_WHITEDWARF, SIR_DOGSTAR, SIR_DISTANCE,
    // Kepler-90
    KEP_EIGHT, KEP_SUNLIKE, KEP_CROWDED, KEP_AI, KEP_DISTANCE,
    // Kepler-186
    KP_DWARF, KP_TRANSIT, KP_HZ, KP_SUNSET, KP_CYGNUS,
    // K2-18
    K2_INTRO, K2_DWARF, K2_SIZE, K2_HZ, K2_TRANSIT, K2_SPECTRUM, K2_WATER, K2_HYCEAN, K2_DISTANCE,
    // 55 Cancri
    CN_INTRO, CN_FAMILY, CN_GIANT, CN_BINARY, CN_NAKED, CN_LAVA, CN_YEAR, CN_TIDAL,
    CN_DIAMOND, CN_ATMOS, CN_DISTANCE,
    // Barnard's Star
    BR_INTRO, BR_DWARF, BR_DIM, BR_ANCIENT, BR_FLARE, BR_RUNAWAY, BR_MOON,
    BR_APPROACH, BR_PHANTOM, BR_FOUND, BR_FOUR, BR_ROAST, BR_DAEDALUS, BR_DISTANCE,
    // Wolf 359
    WF_INTRO, WF_TINY, WF_EMBER, WF_INVISIBLE, WF_YOUNG, WF_FLARE, WF_WOLF,
    WF_MAYBE, WF_HUGGING, WF_OURSUN, WF_BORG, WF_DISTANCE,
    // Spotlight — standalone facts
    SP_VOYAGER, SP_ISS, SP_STARSHIP, SP_SUN, SP_OLYMPUS, SP_BLACKHOLE, SP_WORMHOLE,
    SP_PULSAR, SP_NEUTRON, SP_SUPERNOVA, SP_MILKYWAY, SP_COMET, SP_ECLIPSE,
    SP_KUIPER, SP_HEXAGON, SP_ROGUE, SP_AURORA, SP_EUROPA, SP_QUASAR,
}

/** A planet: diameter in Earth-diameters, mean distance in AU, and a 3-stop sphere palette. */
data class Planet(
    val name: String,
    val d: Float,
    val au: Float,
    val c: List<Color>,
)

/** A neighbouring star system shown (locked) in the home library. */
data class StarSystem(
    val name: String,
    val dist: String,
    val desc: String,
    val color: List<Color>,
)

/** A single celestial fact: its scene, copy, detail-sheet stats, and owning system. */
data class Fact(
    val id: String,
    val cat: String,
    val accent: Color,
    val scene: SceneId,
    val dur: Float,
    val hero: Float,
    val title: String,
    val sub: String,
    val blurb: String,
    val stats: List<Pair<String, String>>,
    val sys: String,
    val musicResId: Int? = null,
)

/** Header copy for a featured/explorable system. */
data class SysMeta(
    val label: String,
    val eyebrow: String,
    val eyebrowColor: Color,
    val title: String,
    val blurb: String,
)

/** A large card on the home screen that opens an explorable [sysId]. */
data class FeaturedSystem(
    val sysId: String,
    val scene: SceneId,
    val dur: Float,
    val hero: Float,
    val pill: String,
    val pillColor: Color,
    val title: String,
    val tagline: String,
)

/** The kind of object on the scale-comparison fly-through (drives how it is drawn). */
enum class CompKind { PLANET, STAR, HOLE, NEBULA, CLUSTER, GALAXY, WEB, UNIVERSE }

/** A single object on the scale-comparison fly-through, sorted by true diameter. */
data class CompObj(
    val name: String,
    val sub: String,                   // classification shown beneath the name
    val sizeText: String,
    val diaKm: Double,                 // true diameter, drives the scaling
    val kind: CompKind,
    val colors: List<Color>,
    val glow: Color = Color.Transparent,
    val ring: Color? = null,
)

/** Opaque colour from a 0xRRGGBB literal. */
internal fun hex(v: Long) = Color(v or 0xFF000000)
