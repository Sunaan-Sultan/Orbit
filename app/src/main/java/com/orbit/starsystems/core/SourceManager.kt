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

        "acen.1" to "",
        "acen.2" to "",
        "acen.3" to "",
        "acen.4" to "",
        "acen.5" to "",
        "acen.6" to "",

        "tr.1" to "",
        "tr.2" to "",
        "tr.3" to "",
        "tr.4" to "",
        "tr.5" to "",
        "tr.6" to "",
        "tr.7" to "",
        "tr.8" to "",

        "sir.1" to "",
        "sir.2" to "",
        "sir.3" to "",
        "sir.4" to "",
        "sir.5" to "",
        "sir.6" to "",

        "kep.1" to "",
        "kep.2" to "",
        "kep.3" to "",
        "kep.4" to "",
        "kep.5" to "",

        "k186.1" to "",
        "k186.2" to "",
        "k186.3" to "",
        "k186.4" to "",
        "k186.5" to "",

        "k218.1" to "",
        "k218.2" to "",
        "k218.3" to "",
        "k218.4" to "",
        "k218.5" to "",
        "k218.6" to "",
        "k218.7" to "",
        "k218.8" to "",
        "k218.9" to "",

        "55c.1" to "",
        "55c.2" to "",
        "55c.3" to "",
        "55c.4" to "",
        "55c.5" to "",
        "55c.6" to "",
        "55c.7" to "",
        "55c.8" to "",
        "55c.9" to "",
        "55c.10" to "",
        "55c.11" to "",

        "bar.1" to "",
        "bar.2" to "",
        "bar.3" to "",
        "bar.4" to "",
        "bar.5" to "",
        "bar.6" to "",
        "bar.7" to "",
        "bar.8" to "",
        "bar.9" to "",
        "bar.10" to "",
        "bar.11" to "",
        "bar.12" to "",
        "bar.13" to "",
        "bar.14" to "",

        "wolf.1" to "",
        "wolf.2" to "",
        "wolf.3" to "",
        "wolf.4" to "",
        "wolf.5" to "",
        "wolf.6" to "",
        "wolf.7" to "",
        "wolf.8" to "",
        "wolf.9" to "",
        "wolf.10" to "",
        "wolf.11" to "",
        "wolf.12" to "",

        "spot.1" to "",
        "spot.2" to "",
        "spot.3" to "",
        "spot.4" to "",
        "spot.5" to "",
        "spot.6" to "",
        "spot.7" to "",
        "spot.8" to "",
        "spot.9" to "",
        "spot.10" to "",
        "spot.11" to "",
        "spot.12" to "",
        "spot.13" to "",
        "spot.14" to "",
        "spot.15" to "",
        "spot.16" to "",
        "spot.17" to "",
        "spot.18" to "",
        "spot.19" to "",
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
