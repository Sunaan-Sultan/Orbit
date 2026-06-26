package com.orbit.starsystems.core

import com.orbit.starsystems.R

private fun acen(
    id: String, cat: String, accent: Long, scene: SceneId, dur: Float, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
    music: Int? = null
) = Fact(id, cat, hex(accent), scene, dur, hero, title, sub, blurb, stats, sys = "acen", musicResId = music)

/** The six facts of the Alpha Centauri system. */
val AC_FACTS: List<Fact> = listOf(
    acen(
        "ac_triple", "The System", 0xffd34a, SceneId.AC_TRIPLE, 9.4f, 4.8f,
        "Three Suns", "A bound triple-star system",
        "Alpha Centauri is really three stars held together by gravity: a Sun-like pair, A and B, orbiting close together, and faint red Proxima Centauri drifting thousands of AU farther out.",
        listOf("Stars in system" to "3", "Inner pair" to "A + B", "Proxima distance" to "≈ 13,000 AU"),
        R.raw.music1
    ),
    acen(
        "ac_waltz", "The System", 0xff9e4a, SceneId.AC_WALTZ, 9.4f, 5f,
        "An 80-Year Waltz", "A and B orbit each other",
        "The two bright stars, A and B, circle their shared centre of mass roughly every 79 years — sweeping from about 11 AU apart (Saturn-like) out to 36 AU (beyond Neptune) and back again.",
        listOf("Orbital period" to "≈ 79 years", "Closest" to "11 AU", "Farthest" to "36 AU"),
        R.raw.music2
    ),
    acen(
        "ac_twin", "The Stars", 0xffd34a, SceneId.AC_TWIN, 9.4f, 5.8f,
        "A Solar Twin", "Alpha Cen A ≈ our Sun",
        "Alpha Centauri A is almost a carbon copy of our Sun — a yellow G-type star of similar size, colour and age. Its partner B is a slightly smaller, cooler orange star.",
        listOf("Alpha Cen A" to "G2 · Sun-like", "Radius" to "1.08× Sun", "Companion B" to "K1 · orange"),
        R.raw.music3
    ),
    acen(
        "ac_proxima", "The Stars", 0xe0744a, SceneId.AC_PROXIMA, 9.4f, 5f,
        "Proxima Centauri", "A small, flaring red dwarf",
        "Proxima Centauri is a red dwarf only about a seventh the Sun's width and far cooler — yet it unleashes powerful flares that can briefly brighten it many times over.",
        listOf("Type" to "Red dwarf (M5.5)", "Width" to "≈ 1/7 of Sun", "Surface" to "≈ 3,000°C"),
        R.raw.music4
    ),
    acen(
        "ac_proximab", "Worlds", 0x7fb0e6, SceneId.AC_PROXIMAB, 9.4f, 5.4f,
        "A Planet Next Door", "Earth-mass, in the habitable zone",
        "Proxima b is a roughly Earth-mass planet that laps its star every 11 days. Because Proxima is so dim, that tight orbit still sits in the habitable zone where liquid water is possible.",
        listOf("Mass" to "≈ 1.1× Earth", "Orbit" to "11.2 days", "Zone" to "Habitable"),
        R.raw.music7
    ),
    acen(
        "ac_travel", "Getting There", 0xcdd6e0, SceneId.AC_TRAVEL, 9.4f, 5.8f,
        "So Near, So Far", "4.24 light-years away",
        "Even as our closest stellar neighbours, they're staggeringly far: our fastest probes would need around 73,000 years to arrive. A laser-pushed nanocraft might one day cut that to about 20.",
        listOf("Distance" to "4.24 light-years", "Fastest probe" to "≈ 73,000 yrs", "Laser sail concept" to "≈ 20 yrs"),
        R.raw.music6
    ),
)
