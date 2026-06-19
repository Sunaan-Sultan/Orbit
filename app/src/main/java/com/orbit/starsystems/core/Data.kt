package com.orbit.starsystems.core

import androidx.compose.ui.graphics.Color

/** Identifies which animated scene a fact renders. */
enum class SceneId {
    SIZES, SUN, DISTANCE, MOON, LIGHT, STAR,
    // Worlds Up Close (and a few extras in existing categories)
    RINGS, VOLCANO, STORM, DIAMOND, VENUSDAY, URANUS, MERCURYTEMP, SATURNFLOAT, ASTEROIDBELT, PLUTOYEAR,
}

/** A planet: diameter in Earth-diameters, mean distance in AU, and a 3-stop sphere palette. */
data class Planet(
    val name: String,
    val d: Float,
    val au: Float,
    val c: List<Color>,
)

private fun hex(v: Long) = Color(v or 0xFF000000)

val PLANETS: List<Planet> = listOf(
    Planet("MERCURY", 0.383f, 0.39f, listOf(hex(0xd8ccba), hex(0x9c9078), hex(0x4f4738))),
    Planet("VENUS", 0.949f, 0.72f, listOf(hex(0xf6e6c2), hex(0xdcb37e), hex(0x9a7440))),
    Planet("EARTH", 1.000f, 1.00f, listOf(hex(0x9cc4ec), hex(0x3d72b8), hex(0x1a3360))),
    Planet("MARS", 0.532f, 1.52f, listOf(hex(0xe8915c), hex(0xb5462a), hex(0x5f2012))),
    Planet("JUPITER", 10.97f, 5.20f, listOf(hex(0xecd8b4), hex(0xc8a072), hex(0x8f6a40))),
    Planet("SATURN", 9.140f, 9.54f, listOf(hex(0xf0e0b2), hex(0xd8bd82), hex(0xa07f4e))),
    Planet("URANUS", 3.980f, 19.19f, listOf(hex(0xd2eff0), hex(0x9ac8cb), hex(0x5a8d91))),
    Planet("NEPTUNE", 3.860f, 30.07f, listOf(hex(0x8fb0e6), hex(0x3a5fb0), hex(0x1f386e))),
)

/** A neighbouring star system (the "Beyond Sol" library, locked for now). */
data class StarSystem(
    val name: String,
    val dist: String,
    val desc: String,
    val color: List<Color>,
)

val SYSTEMS: List<StarSystem> = listOf(
    StarSystem("Alpha Centauri", "4.37 light-years", "Our nearest neighbour — three suns",
        listOf(hex(0xffe7b0), hex(0xffb74d), hex(0x9c5a18))),
    StarSystem("TRAPPIST-1", "39 light-years", "Seven Earth-size worlds, one red dwarf",
        listOf(hex(0xffcaa8), hex(0xe0744a), hex(0x7a2c14))),
    StarSystem("Sirius", "8.6 light-years", "The brightest star in our sky",
        listOf(hex(0xeaf2ff), hex(0xbcd2f0), hex(0x7a93b8))),
    StarSystem("Kepler-90", "2,840 light-years", "Eight known planets — a rival to Sol",
        listOf(hex(0xdfe8ff), hex(0x9ab0e0), hex(0x46598f))),
)

/** A single celestial fact: its scene, copy, and detail-sheet stats. */
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
)

val FACTS: List<Fact> = listOf(
    Fact(
        id = "sizes", cat = "Scale", accent = hex(0xc8a072), scene = SceneId.SIZES,
        dur = 9f, hero = 6.2f, title = "Eight Worlds",
        sub = "The planets, at true relative size",
        blurb = "Lined up at one scale, the planets are wildly mismatched. Jupiter and Saturn dwarf the four small rocky worlds — Mercury barely registers as a dot.",
        stats = listOf(
            "Largest" to "Jupiter · 142,984 km",
            "Smallest" to "Mercury · 4,879 km",
            "Earths inside Jupiter" to "≈ 1,300",
        ),
    ),
    Fact(
        id = "sun", cat = "Scale", accent = hex(0xff9e34), scene = SceneId.SUN,
        dur = 9.4f, hero = 6.4f, title = "The Sun",
        sub = "1.3 million Earths fit inside",
        blurb = "The Sun holds 99.86% of all the mass in the solar system. Every planet, moon and asteroid combined is barely a rounding error beside it.",
        stats = listOf(
            "Diameter" to "1,391,000 km",
            "Earths across" to "109",
            "Earths inside" to "≈ 1,300,000",
        ),
    ),
    Fact(
        id = "distance", cat = "Distances", accent = hex(0x9ac8cb), scene = SceneId.DISTANCE,
        dur = 9.9f, hero = 7f, title = "Mostly Empty",
        sub = "Space is mostly… space",
        blurb = "Drawn to scale, the planets are specks adrift in oceans of nothing. Neptune orbits thirty times farther from the Sun than Earth does.",
        stats = listOf(
            "Earth → Sun" to "1 AU (150M km)",
            "Neptune → Sun" to "30 AU (4.5B km)",
            "Light out to Neptune" to "≈ 4 hours",
        ),
    ),
    Fact(
        id = "moon", cat = "Distances", accent = hex(0x3d72b8), scene = SceneId.MOON,
        dur = 9.9f, hero = 7.2f, title = "The Big Gap",
        sub = "Every planet fits Earth ↔ Moon",
        blurb = "The Moon sits about 384,400 km away — roughly 30 Earths in a row. Remarkably, all seven other planets would fit, end to end, inside that gap.",
        stats = listOf(
            "Earth → Moon" to "384,400 km",
            "Earths apart" to "≈ 30",
            "Fits all 8 planets" to "yes, just barely",
        ),
    ),
    Fact(
        id = "light", cat = "Light & Time", accent = hex(0xdfe6ff), scene = SceneId.LIGHT,
        dur = 9f, hero = 7f, title = "Light Takes Time",
        sub = "8 min 20 s from the Sun",
        blurb = "Nothing outruns light — yet even light needs 8m 20s to cross from the Sun to your skin. You always see the Sun as it was, never as it is now.",
        stats = listOf(
            "Speed of light" to "299,792 km/s",
            "Sun → Earth" to "8 min 20 s",
            "Moon → Earth" to "1.3 s",
        ),
    ),
    Fact(
        id = "star", cat = "Stars", accent = hex(0xe0744a), scene = SceneId.STAR,
        dur = 9f, hero = 6f, title = "The Nearest Star",
        sub = "4.24 light-years away",
        blurb = "Beyond the Sun, the closest star is Proxima Centauri — 4.24 light-years, about 40 trillion km. Our fastest probe would take tens of thousands of years to reach it.",
        stats = listOf(
            "Distance" to "4.24 light-years",
            "In kilometres" to "≈ 40 trillion",
            "By fastest probe" to "≈ 73,000 years",
        ),
    ),
    Fact(
        id = "rings", cat = "Worlds Up Close", accent = hex(0xe8d6a6), scene = SceneId.RINGS,
        dur = 9f, hero = 5.4f, title = "Saturn's Rings",
        sub = "282,000 km wide, ~10 m thick",
        blurb = "Saturn's rings stretch about 282,000 km edge to edge — most of the Earth–Moon distance — yet they average only around ten metres thick. They're built almost entirely of ice, from dust grains to house-sized boulders.",
        stats = listOf("Width" to "≈ 282,000 km", "Average thickness" to "≈ 10 m", "Made of" to "Ice & rock"),
    ),
    Fact(
        id = "olympus", cat = "Worlds Up Close", accent = hex(0xb5462a), scene = SceneId.VOLCANO,
        dur = 9f, hero = 4.4f, title = "Olympus Mons",
        sub = "22 km tall — 2.5× Everest",
        blurb = "Olympus Mons on Mars is the tallest known volcano in the solar system: about 22 km high, two and a half times the height of Everest, on a base roughly the size of Arizona. Low gravity and a still crust let it grow for millions of years.",
        stats = listOf("Height" to "≈ 22 km", "vs Everest" to "≈ 2.5×", "Base width" to "≈ 600 km"),
    ),
    Fact(
        id = "redspot", cat = "Worlds Up Close", accent = hex(0xc8643a), scene = SceneId.STORM,
        dur = 9f, hero = 5f, title = "The Great Red Spot",
        sub = "A storm wider than Earth",
        blurb = "Jupiter's Great Red Spot is a high-pressure storm observed for more than 350 years. It is wide enough to swallow the entire Earth, with winds tearing around its edge at hundreds of km/h.",
        stats = listOf("Age observed" to "350+ years", "Width" to "> 1 Earth", "Edge winds" to "≈ 430 km/h"),
    ),
    Fact(
        id = "diamondrain", cat = "Worlds Up Close", accent = hex(0x7fa8e6), scene = SceneId.DIAMOND,
        dur = 9f, hero = 5f, title = "Diamond Rain",
        sub = "Carbon falls as diamond",
        blurb = "Deep inside Neptune (and Uranus), immense heat and pressure break methane apart and squeeze its carbon into diamond. Experiments suggest these diamonds drift downward like a slow, glittering rain.",
        stats = listOf("Where" to "Deep interior", "From" to "Methane", "Falls as" to "Solid diamond"),
    ),
    Fact(
        id = "venusday", cat = "Light & Time", accent = hex(0xdcb37e), scene = SceneId.VENUSDAY,
        dur = 9f, hero = 4.6f, title = "Longest Day",
        sub = "A day longer than a year",
        blurb = "Venus rotates so slowly — and backwards — that a single day (243 Earth days) lasts longer than its entire year (225 Earth days). The Sun there would rise in the west and set in the east.",
        stats = listOf("One day" to "243 Earth days", "One year" to "225 Earth days", "Spin" to "Retrograde"),
    ),
    Fact(
        id = "uranus", cat = "Worlds Up Close", accent = hex(0x9ac8cb), scene = SceneId.URANUS,
        dur = 9.4f, hero = 5f, title = "The Tipped Planet",
        sub = "Uranus orbits on its side",
        blurb = "Uranus is tilted a staggering 98°, so it effectively rolls around the Sun on its side — likely knocked over by a giant impact. Each pole spends about 42 years in continuous sunlight, then 42 years in darkness.",
        stats = listOf("Axial tilt" to "98°", "Polar day/night" to "≈ 42 years each", "Likely cause" to "Giant impact"),
    ),
    Fact(
        id = "mercurytemp", cat = "Worlds Up Close", accent = hex(0xff9e34), scene = SceneId.MERCURYTEMP,
        dur = 9.4f, hero = 5.4f, title = "Fire & Ice",
        sub = "430°C day, −180°C night",
        blurb = "Mercury has barely any atmosphere to hold heat, so its sunlit side roasts at about 430°C while the night side plunges to around −180°C — a swing of more than 600°C, the most extreme of any planet.",
        stats = listOf("Day side" to "≈ +430°C", "Night side" to "≈ −180°C", "Temperature swing" to "> 600°C"),
    ),
    Fact(
        id = "saturnfloat", cat = "Worlds Up Close", accent = hex(0xd8bd82), scene = SceneId.SATURNFLOAT,
        dur = 9.4f, hero = 4.4f, title = "It Would Float",
        sub = "Less dense than water",
        blurb = "Saturn is a giant ball of mostly hydrogen and helium with an average density of just 0.69 g/cm³ — lower than water. Given an ocean large enough, the whole planet would bob on the surface.",
        stats = listOf("Saturn density" to "0.69 g/cm³", "Water density" to "1.00 g/cm³", "Mostly" to "Hydrogen & helium"),
    ),
    Fact(
        id = "asteroidbelt", cat = "Distances", accent = hex(0xb6a479), scene = SceneId.ASTEROIDBELT,
        dur = 9.4f, hero = 4.6f, title = "The Empty Belt",
        sub = "Millions of rocks, vast gaps",
        blurb = "The asteroid belt between Mars and Jupiter holds over a million catalogued rocks, yet they are spread so thinly — often a million km apart — that every spacecraft sent through has passed without coming close to one.",
        stats = listOf("Known asteroids" to "1,000,000+", "Typical gap" to "≈ 1,000,000 km", "Location" to "Mars ↔ Jupiter"),
    ),
    Fact(
        id = "plutoyear", cat = "Light & Time", accent = hex(0xc9b8d8), scene = SceneId.PLUTOYEAR,
        dur = 9.4f, hero = 5f, title = "Pluto's Long Year",
        sub = "248 Earth years per orbit",
        blurb = "Pluto orbits so far from the Sun that one lap takes about 248 Earth years. Since its discovery in 1930, it still hasn't completed a single trip around the Sun.",
        stats = listOf("One orbit" to "248 Earth years", "Discovered" to "1930", "Orbits since" to "Still not one"),
    ),
)

fun factById(id: String?): Fact = FACTS.find { it.id == id } ?: FACTS.first()

/** Distinct fact categories, in first-seen order (used by Explore). */
val FACT_CATEGORIES: List<String> = FACTS.map { it.cat }.distinct()
