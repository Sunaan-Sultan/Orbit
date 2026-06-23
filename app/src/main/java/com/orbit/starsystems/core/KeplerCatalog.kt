package com.orbit.starsystems.core

import com.orbit.starsystems.R

private fun kep(
    id: String, cat: String, accent: Long, scene: SceneId, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
    music: Int? = null,
) = Fact(id, cat, hex(accent), scene, dur = 15f, hero = hero, title = title, sub = sub, blurb = blurb, stats = stats, sys = "kep", musicResId = music)

/** The five facts of the Kepler-90 system. */
val KEPLER_FACTS: List<Fact> = listOf(
    kep(
        "kep_eight", "The System", 0xa9c2ff, SceneId.KEP_EIGHT, 5.5f,
        "Eight Worlds, One Star", "A rival to our own Solar System",
        "Kepler-90 is circled by eight known planets — the same number as our own Sun, and the first star found to match it. Together they make a true miniature solar system far across the galaxy.",
        listOf("Known planets" to "8", "Ties with" to "The Solar System", "Star type" to "Sun-like (G)"),
        R.raw.music1,
    ),
    kep(
        "kep_sunlike", "The Star", 0xffd35e, SceneId.KEP_SUNLIKE, 5.5f,
        "A Sun Much Like Ours", "Slightly bigger, slightly hotter",
        "Unlike the cool red dwarfs that host many known planets, Kepler-90 is a G-type star much like the Sun — about 20% larger and a little hotter, with a familiar yellow-white glow.",
        listOf("Type" to "G-type (like Sun)", "Radius" to "1.2× Sun", "Temperature" to "≈ 6,000 °C"),
        R.raw.music2,
    ),
    kep(
        "kep_crowded", "The System", 0xa9c2ff, SceneId.KEP_CROWDED, 5.5f,
        "Eight Worlds, Packed Tight", "All squeezed inside Earth's orbit",
        "Every one of Kepler-90's eight planets orbits closer to its star than Earth does to the Sun. The entire system would fit within our own orbit, with the outermost world about where Earth sits.",
        listOf("Outermost planet" to "≈ 1.0 AU", "Inner six" to "Inside Mercury", "Earth" to "1.0 AU"),
        R.raw.music3,
    ),
    kep(
        "kep_ai", "Discovery", 0x7fd6c0, SceneId.KEP_AI, 5.5f,
        "Found by Artificial Intelligence",
        "A neural network spotted the eighth planet",
        "Kepler-90i, the eighth planet, was too faint for people to notice. In 2017 a Google neural network trained to recognise the tiny dips of a transit found it hidden in the data — the first world discovered this way.",
        listOf("Planet" to "Kepler-90i", "Found" to "2017, by AI", "Method" to "Neural network"),
        R.raw.music4,
    ),
    kep(
        "kep_distance", "Getting There", 0xa9c2ff, SceneId.KEP_DISTANCE, 6f,
        "2,840 Light-Years Away", "Deep in the constellation Draco",
        "Kepler-90 lies about 2,840 light-years away in Draco — hundreds of times farther than our nearest stellar neighbours. The light we study from it tonight left long before recorded history.",
        listOf("Distance" to "2,840 light-years", "Constellation" to "Draco", "In km" to "≈ 27 quadrillion"),
        R.raw.music5,
    ),
)
