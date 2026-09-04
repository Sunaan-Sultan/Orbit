package com.orbit.starsystems.core

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.orbit.starsystems.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads every piece of catalog content (systems, facts, planets, the scale-comparison
 * line-up) from JSON in `assets/` instead of hard-coding it in Kotlin.
 *
 * Call [init] once at app start (see MainActivity) before any screen reads the data.
 * The public catalog API in Catalog.kt simply forwards to the fields populated here.
 */
object OrbitData {

    // The "spot" pseudo-system (Spotlight tab) is always loaded but never shown as a card.
    private const val SPOTLIGHT = "spot"

    var planets: List<Planet> = emptyList(); private set
    var sysMeta: Map<String, SysMeta> = emptyMap(); private set
    var featured: List<FeaturedSystem> = emptyList(); private set
    var lockedSystems: List<StarSystem> = emptyList(); private set
    var compObjects: List<CompObj> = emptyList(); private set

    /** Facts keyed by system id (incl. "spot"), in authoring order. */
    private var factsBySys: Map<String, List<Fact>> = emptyMap()

    /** Every fact across explorable systems then Spotlight (matches the old ALL_FACTS order). */
    var allFacts: List<Fact> = emptyList(); private set

    @Volatile private var loaded = false

    fun factsForSys(sys: String): List<Fact> =
        factsBySys[sys] ?: factsBySys["sol"] ?: emptyList()

    /** Idempotent — safe to call from every Activity.onCreate. */
    fun init(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val assets = context.applicationContext.assets

            fun read(path: String) = assets.open(path).bufferedReader().use { it.readText() }

            planets = parseArray(read("planets.json")) { it.toPlanet() }
            compObjects = parseArray(read("comparison.json")) { it.toCompObj() }

            val root = JSONObject(read("systems.json"))

            val explorable = root.getJSONArray("explorable")
            val meta = LinkedHashMap<String, SysMeta>()
            val cards = ArrayList<FeaturedSystem>()
            val facts = LinkedHashMap<String, List<Fact>>()
            for (i in 0 until explorable.length()) {
                val o = explorable.getJSONObject(i)
                val id = o.getString("id")
                meta[id] = o.getJSONObject("meta").toSysMeta()
                cards += o.getJSONObject("featured").toFeatured(id)
                facts[id] = parseFacts(read(o.getString("factsFile")), id)
            }
            // Spotlight is loaded as its own system but kept out of the explorable lists.
            facts[SPOTLIGHT] = parseFacts(read("facts/spotlight.json"), SPOTLIGHT)

            sysMeta = meta
            featured = cards
            factsBySys = facts
            lockedSystems = parseArray(root.getJSONArray("locked")) { it.toStarSystem() }

            allFacts = facts.entries
                .filter { it.key != SPOTLIGHT }
                .flatMap { it.value } + (facts[SPOTLIGHT] ?: emptyList())

            loaded = true
        }
    }

    // ───────────────────────── parsing helpers ─────────────────────────

    private inline fun <T> parseArray(json: String, map: (JSONObject) -> T): List<T> =
        parseArray(JSONArray(json), map)

    private inline fun <T> parseArray(arr: JSONArray, map: (JSONObject) -> T): List<T> =
        (0 until arr.length()).map { map(arr.getJSONObject(it)) }

    private fun parseFacts(json: String, sys: String): List<Fact> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { arr.getJSONObject(it).toFact(sys, it + 1) }
    }

    /** A 6-hex string is opaque RGB; an 8-hex string is taken as full ARGB (used for glows). */
    private fun color(s: String): Color {
        val v = s.removePrefix("0x").removePrefix("#")
        val n = v.toLong(16)
        return if (v.length <= 6) Color(n or 0xFF000000) else Color(n)
    }

    private fun JSONArray.toColors(): List<Color> =
        (0 until length()).map { color(getString(it)) }

    private fun JSONObject.colorList(key: String) = getJSONArray(key).toColors()

    private val MUSIC: Map<String, Int> = mapOf(
        "music1" to R.raw.music1, "music2" to R.raw.music2, "music3" to R.raw.music3,
        "music4" to R.raw.music4, "music5" to R.raw.music5, "music6" to R.raw.music6,
        "music7" to R.raw.music7,
    )

    private fun JSONObject.toPlanet() = Planet(
        name = getString("name"),
        d = getDouble("d").toFloat(),
        au = getDouble("au").toFloat(),
        c = colorList("colors"),
    )

    private fun JSONObject.toSysMeta() = SysMeta(
        label = getString("label"),
        eyebrow = getString("eyebrow"),
        eyebrowColor = color(getString("eyebrowColor")),
        title = getString("title"),
        blurb = getString("blurb"),
    )

    private fun JSONObject.toFeatured(id: String) = FeaturedSystem(
        sysId = id,
        scene = SceneId.valueOf(getString("scene")),
        dur = getDouble("dur").toFloat(),
        hero = getDouble("hero").toFloat(),
        pill = getString("pill"),
        pillColor = color(getString("pillColor")),
        title = getString("title"),
        tagline = getString("tagline"),
    )

    private fun JSONObject.toStarSystem() = StarSystem(
        name = getString("name"),
        dist = getString("dist"),
        desc = getString("desc"),
        color = colorList("colors"),
    )

    private fun JSONObject.toFact(sys: String, num: Int): Fact {
        val stats = getJSONArray("stats")
        return Fact(
            id = getString("id"),
            cat = getString("cat"),
            accent = color(getString("accent")),
            scene = SceneId.valueOf(getString("scene")),
            dur = 15f,                       // every scene plays on a uniform 15 s loop
            hero = getDouble("hero").toFloat(),
            title = getString("title"),
            sub = getString("sub"),
            blurb = getString("blurb"),
            stats = (0 until stats.length()).map {
                val p = stats.getJSONArray(it)
                p.getString(0) to p.getString(1)
            },
            sys = sys,
            musicResId = optString("music").takeIf { it.isNotEmpty() }?.let { MUSIC[it] },
            source = SourceManager.sourceFor(sys, num),
        )
    }

    private fun JSONObject.toCompObj() = CompObj(
        name = getString("name"),
        sub = getString("sub"),
        sizeText = getString("sizeText"),
        diaKm = getDouble("diaKm"),
        kind = CompKind.valueOf(getString("kind")),
        colors = colorList("colors"),
        glow = optString("glow").takeIf { it.isNotEmpty() }?.let { color(it) } ?: Color.Transparent,
        ring = optString("ring").takeIf { it.isNotEmpty() }?.let { color(it) },
    )
}
