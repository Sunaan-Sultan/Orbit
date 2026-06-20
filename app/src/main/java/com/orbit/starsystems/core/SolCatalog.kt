package com.orbit.starsystems.core

/** The eight planets, used by the Sol scenes. */
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

private fun sol(
    id: String, cat: String, accent: Long, scene: SceneId, dur: Float, hero: Float,
    title: String, sub: String, blurb: String, stats: List<Pair<String, String>>,
) = Fact(id, cat, hex(accent), scene, dur, hero, title, sub, blurb, stats, sys = "sol")

/** The 16 facts of our Solar System. */
val SOL_FACTS: List<Fact> = listOf(
    sol(
        "sizes", "Scale", 0xc8a072, SceneId.SIZES, 9f, 6.2f,
        "Eight Worlds", "The planets, at true relative size",
        "Lined up at one scale, the planets are wildly mismatched. Jupiter and Saturn dwarf the four small rocky worlds — Mercury barely registers as a dot.",
        listOf("Largest" to "Jupiter · 142,984 km", "Smallest" to "Mercury · 4,879 km", "Earths inside Jupiter" to "≈ 1,300"),
    ),
    sol(
        "sun", "Scale", 0xff9e34, SceneId.SUN, 9.4f, 6.4f,
        "The Sun", "1.3 million Earths fit inside",
        "The Sun holds 99.86% of all the mass in the solar system. Every planet, moon and asteroid combined is barely a rounding error beside it.",
        listOf("Diameter" to "1,391,000 km", "Earths across" to "109", "Earths inside" to "≈ 1,300,000"),
    ),
    sol(
        "distance", "Distances", 0x9ac8cb, SceneId.DISTANCE, 9.9f, 7f,
        "Mostly Empty", "Space is mostly… space",
        "Drawn to scale, the planets are specks adrift in oceans of nothing. Neptune orbits thirty times farther from the Sun than Earth does.",
        listOf("Earth → Sun" to "1 AU (150M km)", "Neptune → Sun" to "30 AU (4.5B km)", "Light out to Neptune" to "≈ 4 hours"),
    ),
    sol(
        "moon", "Distances", 0x3d72b8, SceneId.MOON, 9.9f, 7.2f,
        "The Big Gap", "Every planet fits Earth ↔ Moon",
        "The Moon sits about 384,400 km away — roughly 30 Earths in a row. Remarkably, all seven other planets would fit, end to end, inside that gap.",
        listOf("Earth → Moon" to "384,400 km", "Earths apart" to "≈ 30", "Fits all 8 planets" to "yes, just barely"),
    ),
    sol(
        "light", "Light & Time", 0xdfe6ff, SceneId.LIGHT, 9f, 7f,
        "Light Takes Time", "8 min 20 s from the Sun",
        "Nothing outruns light — yet even light needs 8m 20s to cross from the Sun to your skin. You always see the Sun as it was, never as it is now.",
        listOf("Speed of light" to "299,792 km/s", "Sun → Earth" to "8 min 20 s", "Moon → Earth" to "1.3 s"),
    ),
    sol(
        "star", "Stars", 0xe0744a, SceneId.STAR, 9f, 6f,
        "The Nearest Star", "4.24 light-years away",
        "Beyond the Sun, the closest star is Proxima Centauri — 4.24 light-years, about 40 trillion km. Our fastest probe would take tens of thousands of years to reach it.",
        listOf("Distance" to "4.24 light-years", "In kilometres" to "≈ 40 trillion", "By fastest probe" to "≈ 73,000 years"),
    ),
    sol(
        "rings", "Worlds Up Close", 0xe8d6a6, SceneId.RINGS, 9f, 5.4f,
        "Saturn's Rings", "282,000 km wide, ~10 m thick",
        "Saturn's rings stretch about 282,000 km edge to edge — most of the Earth–Moon distance — yet they average only around ten metres thick. They're built almost entirely of ice, from dust grains to house-sized boulders.",
        listOf("Width" to "≈ 282,000 km", "Average thickness" to "≈ 10 m", "Made of" to "Ice & rock"),
    ),
    sol(
        "olympus", "Worlds Up Close", 0xb5462a, SceneId.VOLCANO, 9f, 4.4f,
        "Olympus Mons", "22 km tall — 2.5× Everest",
        "Olympus Mons on Mars is the tallest known volcano in the solar system: about 22 km high, two and a half times the height of Everest, on a base roughly the size of Arizona. Low gravity and a still crust let it grow for millions of years.",
        listOf("Height" to "≈ 22 km", "vs Everest" to "≈ 2.5×", "Base width" to "≈ 600 km"),
    ),
    sol(
        "redspot", "Worlds Up Close", 0xc8643a, SceneId.STORM, 9f, 5f,
        "The Great Red Spot", "A storm wider than Earth",
        "Jupiter's Great Red Spot is a high-pressure storm observed for more than 350 years. It is wide enough to swallow the entire Earth, with winds tearing around its edge at hundreds of km/h.",
        listOf("Age observed" to "350+ years", "Width" to "> 1 Earth", "Edge winds" to "≈ 430 km/h"),
    ),
    sol(
        "diamondrain", "Worlds Up Close", 0x7fa8e6, SceneId.DIAMOND, 9f, 5f,
        "Diamond Rain", "Carbon falls as diamond",
        "Deep inside Neptune (and Uranus), immense heat and pressure break methane apart and squeeze its carbon into diamond. Experiments suggest these diamonds drift downward like a slow, glittering rain.",
        listOf("Where" to "Deep interior", "From" to "Methane", "Falls as" to "Solid diamond"),
    ),
    sol(
        "venusday", "Light & Time", 0xdcb37e, SceneId.VENUSDAY, 9f, 4.6f,
        "Longest Day", "A day longer than a year",
        "Venus rotates so slowly — and backwards — that a single day (243 Earth days) lasts longer than its entire year (225 Earth days). The Sun there would rise in the west and set in the east.",
        listOf("One day" to "243 Earth days", "One year" to "225 Earth days", "Spin" to "Retrograde"),
    ),
    sol(
        "uranus", "Worlds Up Close", 0x9ac8cb, SceneId.URANUS, 9.4f, 5f,
        "The Tipped Planet", "Uranus orbits on its side",
        "Uranus is tilted a staggering 98°, so it effectively rolls around the Sun on its side — likely knocked over by a giant impact. Each pole spends about 42 years in continuous sunlight, then 42 years in darkness.",
        listOf("Axial tilt" to "98°", "Polar day/night" to "≈ 42 years each", "Likely cause" to "Giant impact"),
    ),
    sol(
        "mercurytemp", "Worlds Up Close", 0xff9e34, SceneId.MERCURYTEMP, 9.4f, 5.4f,
        "Fire & Ice", "430°C day, −180°C night",
        "Mercury has barely any atmosphere to hold heat, so its sunlit side roasts at about 430°C while the night side plunges to around −180°C — a swing of more than 600°C, the most extreme of any planet.",
        listOf("Day side" to "≈ +430°C", "Night side" to "≈ −180°C", "Temperature swing" to "> 600°C"),
    ),
    sol(
        "saturnfloat", "Worlds Up Close", 0xd8bd82, SceneId.SATURNFLOAT, 9.4f, 4.4f,
        "It Would Float", "Less dense than water",
        "Saturn is a giant ball of mostly hydrogen and helium with an average density of just 0.69 g/cm³ — lower than water. Given an ocean large enough, the whole planet would bob on the surface.",
        listOf("Saturn density" to "0.69 g/cm³", "Water density" to "1.00 g/cm³", "Mostly" to "Hydrogen & helium"),
    ),
    sol(
        "asteroidbelt", "Distances", 0xb6a479, SceneId.ASTEROIDBELT, 9.4f, 4.6f,
        "The Empty Belt", "Millions of rocks, vast gaps",
        "The asteroid belt between Mars and Jupiter holds over a million catalogued rocks, yet they are spread so thinly — often a million km apart — that every spacecraft sent through has passed without coming close to one.",
        listOf("Known asteroids" to "1,000,000+", "Typical gap" to "≈ 1,000,000 km", "Location" to "Mars ↔ Jupiter"),
    ),
    sol(
        "plutoyear", "Light & Time", 0xc9b8d8, SceneId.PLUTOYEAR, 9.4f, 5f,
        "Pluto's Long Year", "248 Earth years per orbit",
        "Pluto orbits so far from the Sun that one lap takes about 248 Earth years. Since its discovery in 1930, it still hasn't completed a single trip around the Sun.",
        listOf("One orbit" to "248 Earth years", "Discovered" to "1930", "Orbits since" to "Still not one"),
    ),
)
