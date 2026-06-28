package com.orbit.starsystems.core

// All catalog content now lives in JSON under assets/ and is loaded once by
// OrbitData.init() at app start. These accessors simply forward to it, so the rest
// of the app keeps using the same names it always has.

/** The eight planets used by the Sol scenes. */
val PLANETS: List<Planet> get() = OrbitData.planets

/** Every fact across all systems plus Spotlight (uniform 15 s loop). */
val ALL_FACTS: List<Fact> get() = OrbitData.allFacts

fun factById(id: String?): Fact? = ALL_FACTS.find { it.id == id }

/** Facts belonging to a system id ("sol" / "acen" / "tr" / "sir" / "kep" / "spot"). */
fun factsForSys(sys: String): List<Fact> = OrbitData.factsForSys(sys)

/** Distinct categories of a system, in first-seen order (used by Explore). */
fun categoriesForSys(sys: String): List<String> = factsForSys(sys).map { it.cat }.distinct()

/** Facts of each explorable system, by id (kept for any per-system callers). */
val SOL_FACTS: List<Fact> get() = factsForSys("sol")
val AC_FACTS: List<Fact> get() = factsForSys("acen")
val TRAPPIST_FACTS: List<Fact> get() = factsForSys("tr")
val SIRIUS_FACTS: List<Fact> get() = factsForSys("sir")
val KEPLER_FACTS: List<Fact> get() = factsForSys("kep")
val SPOTLIGHT_FACTS: List<Fact> get() = factsForSys("spot")

/** The large "Explore" cards on the home screen, in display order. */
val FEATURED_SYSTEMS: List<FeaturedSystem> get() = OrbitData.featured

/** Header copy for each explorable system. */
val SYS_META: Map<String, SysMeta> get() = OrbitData.sysMeta

/** Still-locked systems shown under "More systems" on the home screen. */
val SYSTEMS: List<StarSystem> get() = OrbitData.lockedSystems
