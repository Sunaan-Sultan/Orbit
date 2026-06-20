package com.orbit.starsystems.core

import com.orbit.starsystems.R

private fun tp(
    id: String, cat: String, accent: Long, scene: SceneId, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
    music: Int? = null
) = Fact(id, cat, hex(accent), scene, dur = 15f, hero = hero, title = title, sub = sub, blurb = blurb, stats = stats, sys = "tr", musicResId = music)

/** The eight facts of the TRAPPIST-1 system. */
val TRAPPIST_FACTS: List<Fact> = listOf(
    tp(
        "tp_seven", "The System", 0xe0744a, SceneId.TP_INTRO, 5.5f,
        "Seven Earths", "Seven rocky worlds, one tiny star",
        "TRAPPIST-1 hosts seven roughly Earth-sized planets orbiting a single ultracool red dwarf — the largest known family of rocky worlds around one star.",
        listOf("Planets" to "7", "All sized like" to "Earth", "Star" to "Ultracool red dwarf"),
        R.raw.music1
    ),
    tp(
        "tp_star", "The Star", 0xff7338, SceneId.TP_STAR, 5.5f,
        "A Star Like Jupiter", "Barely bigger than a gas giant",
        "TRAPPIST-1 is just 12% the Sun's width and about 9% its mass — only a little larger than Jupiter. It is so cool it shines mostly in infrared light.",
        listOf("Width" to "12% of Sun", "Mass" to "9% of Sun", "Glows in" to "Infrared"),
        R.raw.music2
    ),
    tp(
        "tp_compact", "The System", 0xe0915c, SceneId.TP_MERCURY, 5.5f,
        "Smaller Than One Orbit", "Fits inside Mercury's orbit",
        "Every TRAPPIST-1 planet orbits closer to its star than Mercury does to the Sun. The whole seven-world system would fit comfortably inside Mercury's orbit.",
        listOf("Outer planet" to "0.062 AU", "Mercury" to "0.39 AU", "Fits inside" to "Mercury's orbit"),
        R.raw.music3
    ),
    tp(
        "tp_years", "Time", 0xe0744a, SceneId.TP_ORBITS, 5f,
        "A Year in Days", "Orbits of 1.5 to 19 days",
        "Because the planets hug their tiny star, their years are astonishingly short — from 1.5 days on the innermost world to about 19 days on the outermost.",
        listOf("Innermost year" to "1.5 days", "Outermost year" to "19 days", "Earth" to "365 days"),
        R.raw.music4
    ),
    tp(
        "tp_hz", "Habitability", 0x5fae7a, SceneId.TP_HABITABLE, 5.5f,
        "The Water Zone", "Three planets could hold water",
        "Planets e, f and g orbit within the habitable zone, where temperatures could let liquid water pool on a rocky surface.",
        listOf("In the zone" to "e · f · g", "Could hold" to "Liquid water", "Best candidate" to "TRAPPIST-1e"),
        R.raw.music5
    ),
    tp(
        "tp_locked", "Worlds Up Close", 0xffb070, SceneId.TP_TIDAL, 5f,
        "Permanent Day & Night", "Tidally locked to the star",
        "The planets are likely tidally locked, keeping one face in eternal daylight and the other in endless night — with a ring of perpetual twilight in between.",
        listOf("Day side" to "Always lit", "Night side" to "Always dark", "Between" to "Perpetual twilight"),
        R.raw.music6
    ),
    tp(
        "tp_sky", "Worlds Up Close", 0x9cc4ec, SceneId.TP_SKY, 5f,
        "Worlds That Fill the Sky", "Neighbours loom large overhead",
        "The planets are packed so closely that, from the surface of one, its neighbours can appear several times larger than our Moon looks from Earth.",
        listOf("Neighbour size" to "Several × Moon", "Spacing" to "Very tight", "View" to "Spectacular"),
        R.raw.music1
    ),
    tp(
        "tp_distance", "Getting There", 0xe0744a, SceneId.TP_DISTANCE, 6f,
        "39 Light-Years Away", "Close enough to study",
        "TRAPPIST-1 lies about 39 light-years away in Aquarius — near enough that telescopes can already probe its planets' atmospheres for signs of life.",
        listOf("Distance" to "39 light-years", "In km" to "≈ 370 trillion", "Constellation" to "Aquarius"),
        R.raw.music2
    ),
)
