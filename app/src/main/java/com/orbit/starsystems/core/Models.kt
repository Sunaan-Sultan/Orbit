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
    // Spotlight — standalone facts
    SP_VOYAGER, SP_ISS, SP_STARSHIP, SP_SUN, SP_OLYMPUS, SP_BLACKHOLE, SP_WORMHOLE,
    SP_PULSAR, SP_SUPERNOVA, SP_COMET, SP_ECLIPSE,
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

/** Opaque colour from a 0xRRGGBB literal. */
internal fun hex(v: Long) = Color(v or 0xFF000000)
