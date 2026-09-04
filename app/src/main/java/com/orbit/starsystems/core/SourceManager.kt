package com.orbit.starsystems.core

import java.net.URI

object SourceManager {

    const val FALLBACK_NAME = "NASA / ESA"

    private val LINKS: Map<String, String> = mapOf(
        "sol.1" to "https://science.nasa.gov/solar-system/planets/planet-sizes-and-locations-in-our-solar-system/",
        "sol.2" to "https://en.wikipedia.org/wiki/Sun",
        "sol.3" to "https://science.nasa.gov/neptune/facts/",
        "sol.4" to "https://science.nasa.gov/moon/facts/",
        "sol.5" to "https://science.nasa.gov/mission/voyager/voyager-1/voyager-1-what-is-a-light-day/",
        "sol.6" to "https://en.wikipedia.org/wiki/Proxima_Centauri",
        "sol.7" to "https://science.nasa.gov/saturn/facts/",
        "sol.8" to "https://en.wikipedia.org/wiki/Olympus_Mons",
        "sol.9" to "https://en.wikipedia.org/wiki/Great_Red_Spot",
        "sol.10" to "https://www.nasa.gov/podcasts/gravity-assist/gravity-assist-its-raining-diamonds-on-these-planets/",
        "sol.11" to "https://science.nasa.gov/venus/venus-facts/",
        "sol.12" to "https://en.wikipedia.org/wiki/Uranus",
        "sol.13" to "https://science.nasa.gov/mercury/facts/",
        "sol.14" to "https://en.wikipedia.org/wiki/Saturn",
        "sol.15" to "https://en.wikipedia.org/wiki/Asteroid_belt",
        "sol.16" to "https://en.wikipedia.org/wiki/Pluto",

        "acen.1" to "https://www.nasa.gov/image-article/alpha-centauri-triple-star-system-about-4-light-years-from-earth/",
        "acen.2" to "https://en.wikipedia.org/wiki/Alpha_Centauri",
        "acen.3" to "https://en.wikipedia.org/wiki/Alpha_Centauri",
        "acen.4" to "https://en.wikipedia.org/wiki/Proxima_Centauri",
        "acen.5" to "https://en.wikipedia.org/wiki/Proxima_Centauri_b",
        "acen.6" to "https://en.wikipedia.org/wiki/Interstellar_travel",

        "tr.1" to "https://science.nasa.gov/exoplanets/trappist1/",
        "tr.2" to "https://en.wikipedia.org/wiki/TRAPPIST-1",
        "tr.3" to "https://en.wikipedia.org/wiki/TRAPPIST-1",
        "tr.4" to "https://en.wikipedia.org/wiki/TRAPPIST-1",
        "tr.5" to "https://science.nasa.gov/exoplanets/trappist1/",
        "tr.6" to "https://en.wikipedia.org/wiki/TRAPPIST-1",
        "tr.7" to "https://en.wikipedia.org/wiki/TRAPPIST-1",
        "tr.8" to "https://science.nasa.gov/exoplanets/trappist1/",

        "sir.1" to "https://en.wikipedia.org/wiki/Sirius",
        "sir.2" to "https://en.wikipedia.org/wiki/Sirius",
        "sir.3" to "https://en.wikipedia.org/wiki/Sirius",
        "sir.4" to "https://en.wikipedia.org/wiki/Sirius",
        "sir.5" to "https://en.wikipedia.org/wiki/Sirius",
        "sir.6" to "https://en.wikipedia.org/wiki/Sirius",

        "kep.1" to "https://en.wikipedia.org/wiki/Kepler-90",
        "kep.2" to "https://en.wikipedia.org/wiki/Kepler-90",
        "kep.3" to "https://en.wikipedia.org/wiki/Kepler-90",
        "kep.4" to "https://science.nasa.gov/universe/exoplanets/discovery-of-eight-planets-makes-alien-system-the-first-to-tie-with-our-solar-system/",
        "kep.5" to "https://en.wikipedia.org/wiki/Kepler-90",

        "k186.1" to "https://en.wikipedia.org/wiki/Kepler-186f",
        "k186.2" to "https://en.wikipedia.org/wiki/Kepler-186f",
        "k186.3" to "https://en.wikipedia.org/wiki/Kepler-186f",
        "k186.4" to "https://science.nasa.gov/resource/kepler-186f-the-first-earth-size-planet-in-the-habitable-zone-artists-concept/",
        "k186.5" to "https://en.wikipedia.org/wiki/Kepler-186f",

        "k218.1" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.2" to "https://en.wikipedia.org/wiki/K2-18",
        "k218.3" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.4" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.5" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.6" to "https://science.nasa.gov/mission/webb/science-overview/science-explainers/how-will-webb-study-exoplanets/",
        "k218.7" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.8" to "https://en.wikipedia.org/wiki/K2-18b",
        "k218.9" to "https://en.wikipedia.org/wiki/K2-18",

        "55c.1" to "https://en.wikipedia.org/wiki/55_Cancri",
        "55c.2" to "https://en.wikipedia.org/wiki/55_Cancri_f",
        "55c.3" to "https://en.wikipedia.org/wiki/55_Cancri_d",
        "55c.4" to "https://en.wikipedia.org/wiki/55_Cancri",
        "55c.5" to "https://en.wikipedia.org/wiki/55_Cancri",
        "55c.6" to "https://en.wikipedia.org/wiki/55_Cancri_e",
        "55c.7" to "https://en.wikipedia.org/wiki/55_Cancri_e",
        "55c.8" to "https://en.wikipedia.org/wiki/55_Cancri_e",
        "55c.9" to "https://arxiv.org/abs/1309.6032",
        "55c.10" to "https://en.wikipedia.org/wiki/55_Cancri_e",
        "55c.11" to "https://en.wikipedia.org/wiki/55_Cancri",

        "bar.1" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.2" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.3" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.4" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.5" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.6" to "https://en.wikipedia.org/wiki/Proper_motion",
        "bar.7" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.8" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.9" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.10" to "https://en.wikipedia.org/wiki/Barnard%27s_Star_b",
        "bar.11" to "https://en.wikipedia.org/wiki/Barnard%27s_Star",
        "bar.12" to "https://en.wikipedia.org/wiki/Barnard%27s_Star_b",
        "bar.13" to "https://en.wikipedia.org/wiki/Project_Daedalus",
        "bar.14" to "https://en.wikipedia.org/wiki/List_of_nearest_stars",

        "wolf.1" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.2" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.3" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.4" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.5" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.6" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.7" to "https://en.wikipedia.org/wiki/Wolf_359_b",
        "wolf.8" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.9" to "https://en.wikipedia.org/wiki/Wolf_359",
        "wolf.10" to "",
        "wolf.11" to "https://en.wikipedia.org/wiki/List_of_nearest_stars",
        "wolf.12" to "https://en.wikipedia.org/wiki/The_Best_of_Both_Worlds_(Star_Trek:_The_Next_Generation)",

        "spot.1" to "https://science.nasa.gov/mission/voyager/voyager-1/",
        "spot.2" to "https://en.wikipedia.org/wiki/Sun",
        "spot.3" to "https://www.nasa.gov/international-space-station/space-station-facts-and-figures/",
        "spot.4" to "https://en.wikipedia.org/wiki/Interstellar_travel",
        "spot.5" to "https://en.wikipedia.org/wiki/Olympus_Mons",
        "spot.6" to "https://science.nasa.gov/universe/black-holes/",
        "spot.7" to "https://en.wikipedia.org/wiki/Wormhole",
        "spot.8" to "https://en.wikipedia.org/wiki/Pulsar",
        "spot.9" to "https://science.nasa.gov/universe/stories/quick-reads/neutron-stars-are-weird/",
        "spot.10" to "https://en.wikipedia.org/wiki/Supernova",
        "spot.11" to "https://science.nasa.gov/universe/exoplanets/our-milky-way-galaxy-how-big-is-space/",
        "spot.12" to "https://science.nasa.gov/solar-system/comets/facts/",
        "spot.13" to "https://science.nasa.gov/eclipses/geometry/",
        "spot.14" to "https://science.nasa.gov/solar-system/kuiper-belt/facts/",
        "spot.15" to "https://science.nasa.gov/mission/cassini/science/saturn/hexagon-in-motion/",
        "spot.16" to "https://science.nasa.gov/mission/roman-space-telescope/rogue-planets/",
        "spot.17" to "https://science.nasa.gov/sun/auroras/",
        "spot.18" to "https://science.nasa.gov/mission/europa-clipper/why-europa-evidence-for-an-ocean/",
        "spot.19" to "https://en.wikipedia.org/wiki/Quasar",
    )

    private val PUBLISHERS: Map<String, String> = mapOf(
        "science.nasa.gov" to "NASA Science",
        "exoplanets.nasa.gov" to "NASA Exoplanets",
        "exoplanetarchive.ipac.caltech.edu" to "NASA Exoplanet Archive",
        "jpl.nasa.gov" to "NASA JPL",
        "svs.gsfc.nasa.gov" to "NASA Goddard",
        "nasa.gov" to "NASA",
        "esa.int" to "ESA",
        "eso.org" to "ESO",
        "esahubble.org" to "ESA/Hubble",
        "esawebb.org" to "ESA/Webb",
        "hubblesite.org" to "HubbleSite",
        "webbtelescope.org" to "Webb Telescope",
        "noirlab.edu" to "NOIRLab",
        "nrao.edu" to "NRAO",
        "cfa.harvard.edu" to "Harvard CfA",
        "iau.org" to "IAU",
        "wikipedia.org" to "Wikipedia",
        "arxiv.org" to "arXiv",
        "aanda.org" to "Astronomy & Astrophysics",
        "iopscience.iop.org" to "IOP Science",
        "nature.com" to "Nature",
        "science.org" to "Science",
    )

    fun keyFor(sys: String, num: Int): String = "$sys.$num"

    fun urlFor(sys: String, num: Int): String? =
        LINKS[keyFor(sys, num)]?.trim()?.takeIf { it.startsWith("https://") }

    fun sourceFor(sys: String, num: Int): FactSource? =
        urlFor(sys, num)?.let { FactSource(name = publisherOf(it), url = it) }

    fun publisherOf(url: String): String {
        val host = runCatching { URI(url).host }.getOrNull()
            .orEmpty()
            .lowercase()
            .removePrefix("www.")
        if (host.isEmpty()) return FALLBACK_NAME
        PUBLISHERS[host]?.let { return it }
        return PUBLISHERS.entries.firstOrNull { host.endsWith(".${it.key}") }?.value ?: host
    }
}
