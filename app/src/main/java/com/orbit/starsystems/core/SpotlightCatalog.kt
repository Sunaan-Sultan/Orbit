package com.orbit.starsystems.core

import com.orbit.starsystems.R

private fun spot(
    id: String, cat: String, accent: Long, scene: SceneId, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
    music: Int? = null,
) = Fact(id, cat, hex(accent), scene, dur = 15f, hero = hero, title = title, sub = sub, blurb = blurb, stats = stats, sys = "spot", musicResId = music)

/**
 * Standalone "Spotlight" facts that don't belong to any one star system —
 * spacecraft, our own Sun, and singular landmarks. Shown in their own tab.
 */
val SPOTLIGHT_FACTS: List<Fact> = listOf(
    spot(
        "sp_voyager", "Explorers", 0xe8d6a6, SceneId.SP_VOYAGER, 4.6f,
        "Voyager", "The farthest we've ever reached",
        "Launched in 1977, the twin Voyager probes are the most distant human-made objects ever — now drifting through interstellar space, more than 24 billion km from home, still faintly calling back to Earth.",
        listOf("Launched" to "1977", "Distance" to "≈ 24 billion km", "Now in" to "Interstellar space"),
        R.raw.music1,
    ),
    spot(
        "sp_sun", "Our Star", 0xff9e34, SceneId.SP_SUN, 3.2f,
        "The Sun", "A furnace fusing 600M tonnes a second",
        "Our star is a ball of glowing plasma 1.39 million km across, holding 99.86% of all the mass in the solar system. Every second it fuses some 600 million tonnes of hydrogen into helium — the light and warmth of every world.",
        listOf("Diameter" to "1,391,000 km", "Earths across" to "109", "Fuses per second" to "≈ 600M tonnes"),
        R.raw.music2,
    ),
    spot(
        "sp_iss", "In Orbit", 0x9cc4ec, SceneId.SP_ISS, 4.6f,
        "The Space Station", "A laboratory circling the Earth",
        "The International Space Station is the largest structure humans have ever put in space — about the size of a football field. It races around Earth at 27,600 km/h, so its crew sees a sunrise or sunset every 45 minutes.",
        listOf("Altitude" to "≈ 408 km", "Speed" to "27,600 km/h", "Orbit" to "Every 90 min"),
        R.raw.music3,
    ),
    spot(
        "sp_starship", "Future Flight", 0x7fd6c0, SceneId.SP_STARSHIP, 4.4f,
        "Starships to Come", "The engines that might reach a star",
        "No craft yet built could cross to another star in a lifetime. Concepts — fusion rockets, ion drives, and vast light-sails pushed by lasers — imagine vehicles that could one day travel a tenth of the speed of light.",
        listOf("Concept top speed" to "≈ 10–20% light", "Powered by" to "Fusion / light-sail", "Nearest star" to "4.24 light-years"),
        R.raw.music4,
    ),
    spot(
        "sp_olympus", "Landmarks", 0xb5462a, SceneId.SP_OLYMPUS, 3.2f,
        "Olympus Mons", "22 km tall — 2.5× Everest",
        "Olympus Mons on Mars is the tallest known volcano in the solar system: about 22 km high, two and a half times the height of Everest, on a base roughly the size of Arizona. Low gravity and a still crust let it grow for millions of years.",
        listOf("Height" to "≈ 22 km", "vs Everest" to "≈ 2.5×", "Base width" to "≈ 600 km"),
        R.raw.music5,
    ),
    spot(
        "sp_blackhole", "Extreme Gravity", 0xffb060, SceneId.SP_BLACKHOLE, 3.2f,
        "Black Holes", "Where not even light escapes",
        "A black hole crams so much mass into so little space that nothing — not even light — can escape once it crosses the event horizon. The supermassive one at the heart of our galaxy, Sagittarius A*, weighs about 4 million Suns.",
        listOf("Escape speed" to "Faster than light", "Sagittarius A*" to "≈ 4 million Suns", "First imaged" to "2019 · M87*"),
        R.raw.music6,
    ),
    spot(
        "sp_wormhole", "Spacetime", 0x8fd6ff, SceneId.SP_WORMHOLE, 3.2f,
        "Wormholes", "A theoretical shortcut through spacetime",
        "A wormhole — an Einstein-Rosen bridge — is a hypothetical tunnel joining two distant points in spacetime, a shortcut permitted by Einstein's equations. None has ever been observed, and holding one open would demand strange \"exotic\" matter.",
        listOf("Predicted by" to "General relativity", "Ever observed" to "Never", "Would need" to "Exotic matter"),
        R.raw.music1,
    ),
    spot(
        "sp_pulsar", "Dead Stars", 0x9fe8ff, SceneId.SP_PULSAR, 3.0f,
        "Pulsars", "A city-sized star spinning hundreds of times a second",
        "When a giant star dies it can leave behind a neutron star — the densest object in the universe, an entire Sun crushed into a ball the size of a city. Some spin hundreds of times a second, sweeping beams of radio light past Earth like a cosmic lighthouse.",
        listOf("Size" to "≈ 20 km across", "Spin" to "Up to 700×/sec", "A teaspoon" to "≈ a billion tonnes"),
        R.raw.music2,
    ),
    spot(
        "sp_supernova", "Stellar Death", 0xffd36a, SceneId.SP_SUPERNOVA, 4.2f,
        "Supernova", "A dying star outshines a whole galaxy",
        "When a massive star runs out of fuel its core collapses and it detonates as a supernova — for a few weeks blazing brighter than its entire galaxy, and forging the heavy elements that go on to build planets and people.",
        listOf("Brightness" to "Billions of Suns", "Forges" to "Gold, iron, oxygen", "Leaves behind" to "Neutron star or black hole"),
        R.raw.music3,
    ),
    spot(
        "sp_comet", "Wanderers", 0xbfe8ff, SceneId.SP_COMET, 4.5f,
        "Comets", "Icy wanderers that grow tails near the Sun",
        "A comet is a chunk of ice and dust left over from the birth of the solar system. As it swings near the Sun it heats up and streams two glowing tails — always pointing away from the Sun — that can stretch tens of millions of kilometres.",
        listOf("Made of" to "Ice & dust", "Tail length" to "Tens of millions km", "Tail points" to "Away from the Sun"),
        R.raw.music4,
    ),
    spot(
        "sp_eclipse", "Alignments", 0xffcaa0, SceneId.SP_ECLIPSE, 5.0f,
        "Total Eclipse", "The Moon perfectly hides the Sun",
        "By sheer cosmic coincidence the Moon and Sun look almost exactly the same size in our sky. When the Moon slips perfectly in front, day fades to dusk and the Sun's ghostly corona blazes into view for a few breathtaking minutes.",
        listOf("Coincidence" to "Both ≈ 0.5° wide", "Totality" to "Up to ~7.5 min", "Reveals" to "The Sun's corona"),
        R.raw.music5,
    ),
)
