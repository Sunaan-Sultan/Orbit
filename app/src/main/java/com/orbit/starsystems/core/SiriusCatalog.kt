package com.orbit.starsystems.core

import com.orbit.starsystems.R

private fun sir(
    id: String, cat: String, accent: Long, scene: SceneId, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
    music: Int? = null,
) = Fact(id, cat, hex(accent), scene, dur = 15f, hero = hero, title = title, sub = sub, blurb = blurb, stats = stats, sys = "sir", musicResId = music)

/** The six facts of the Sirius system. */
val SIRIUS_FACTS: List<Fact> = listOf(
    sir(
        "sir_bright", "In Our Sky", 0x8fc0ff, SceneId.SIR_BRIGHTEST, 5.5f,
        "The Brightest Star", "Brighter than any other in the night sky",
        "Sirius is the brightest star in Earth's night sky, shining at apparent magnitude −1.46 — almost twice as bright as the next-brightest star, Canopus. Its brilliance owes as much to its closeness as to its true power.",
        listOf("Apparent magnitude" to "−1.46", "Next brightest" to "Canopus (−0.74)", "Constellation" to "Canis Major"),
        R.raw.music1,
    ),
    sir(
        "sir_binary", "The System", 0x8fc0ff, SceneId.SIR_BINARY, 5f,
        "Not One Star, But Two", "A bright star with a white-dwarf partner",
        "Sirius is a binary: brilliant Sirius A and the faint white dwarf Sirius B orbit each other every 50 years. The companion was predicted from a wobble in 1844 and first seen in 1862.",
        listOf("Stars" to "2 (A + B)", "Orbital period" to "≈ 50 years", "B discovered" to "1862"),
        R.raw.music2,
    ),
    sir(
        "sir_hotter", "The Star", 0x8fc0ff, SceneId.SIR_HOTTER, 5.5f,
        "Bigger, Hotter, Brighter", "Sirius A outshines the Sun",
        "Sirius A is about twice the Sun's mass and 1.7 times its radius, with a blue-white surface near 9,900°C. All told it radiates roughly 25 times more light than the Sun.",
        listOf("Mass" to "2.0× Sun", "Radius" to "1.7× Sun", "Luminosity" to "25× Sun"),
        R.raw.music3,
    ),
    sir(
        "sir_dwarf", "The Star", 0xcfe0ff, SceneId.SIR_WHITEDWARF, 5.5f,
        "A Sun the Size of Earth", "Sirius B is a dense white dwarf",
        "Sirius B packs about a full solar mass into a sphere the size of Earth. The result is staggering density — a teaspoon of its matter would weigh as much as a small car.",
        listOf("Mass" to "≈ 1 solar mass", "Size" to "≈ Earth", "Density" to "≈ 5 t / teaspoon"),
        R.raw.music4,
    ),
    sir(
        "sir_dog", "Sky Lore", 0x8fc0ff, SceneId.SIR_DOGSTAR, 5.5f,
        "The Dog Star", "Keeper of the calendar",
        "As the brightest star of Canis Major, the Great Dog, Sirius gives us the term \"dog days.\" Its dawn rising once heralded the annual flooding of the Nile in ancient Egypt.",
        listOf("Nickname" to "The Dog Star", "Constellation" to "Canis Major", "Marked" to "The Nile flood"),
        R.raw.music5,
    ),
    sir(
        "sir_distance", "Getting There", 0x8fc0ff, SceneId.SIR_DISTANCE, 6f,
        "8.6 Light-Years Away", "A close cosmic neighbour",
        "At 8.6 light-years, Sirius is one of the Sun's nearest neighbours. The light reaching your eyes tonight set out from Sirius more than eight years ago.",
        listOf("Distance" to "8.6 light-years", "In km" to "≈ 81 trillion", "Light travel" to "8.6 years"),
        R.raw.music6,
    ),
)
