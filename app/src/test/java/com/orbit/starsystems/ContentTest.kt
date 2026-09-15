package com.orbit.starsystems

import com.orbit.starsystems.core.CompKind
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.core.SourceManager
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the bundled catalog in `assets/`, which `core/OrbitData.kt` parses at startup with
 * no error handling: `SceneId.valueOf` and `CompKind.valueOf` throw on an unknown name, and
 * every `getString`/`getDouble` throws on a missing key. Any of those is a crash before the
 * first frame draws, so a typo in a fact file is otherwise shippable.
 *
 * Runs on the JVM against the files directly — no Context, no emulator. `org.json` is a real
 * test dependency here, because the one on the Android unit-test classpath is a stub whose
 * every method throws.
 */
class ContentTest {

    private val assets = File("src/main/assets")
    private val raw = File("src/main/res/raw")

    private val systems: JSONObject by lazy {
        JSONObject(File(assets, "systems.json").readText())
    }

    /** Explorable system ids paired with their fact files, in `systems.json` order. */
    private val factFiles: List<Pair<String, File>> by lazy {
        val explorable = systems.getJSONArray("explorable")
        (0 until explorable.length()).map {
            val o = explorable.getJSONObject(it)
            o.getString("id") to File(assets, o.getString("factsFile"))
        } + ("spot" to File(assets, "facts/spotlight.json"))
    }

    private fun facts(file: File): List<JSONObject> {
        val arr = JSONArray(file.readText())
        return (0 until arr.length()).map { arr.getJSONObject(it) }
    }

    private fun eachFact(check: (sys: String, num: Int, fact: JSONObject, where: String) -> Unit) {
        factFiles.forEach { (sys, file) ->
            facts(file).forEachIndexed { i, fact ->
                val id = fact.optString("id", "#${i + 1}")
                check(sys, i + 1, fact, "${file.name} [$id]")
            }
        }
    }

    @Test
    fun `every fact file referenced by systems json exists`() {
        factFiles.forEach { (sys, file) ->
            assertTrue("$sys points at a missing fact file: $file", file.isFile)
        }
        assertTrue("no explorable systems found", factFiles.size > 1)
    }

    @Test
    fun `every scene name resolves to a SceneId`() {
        val known = SceneId.entries.map { it.name }.toSet()
        eachFact { _, _, fact, where ->
            val scene = fact.getString("scene")
            assertTrue("$where: unknown scene \"$scene\"", scene in known)
        }
    }

    @Test
    fun `every music key resolves to a bundled track`() {
        val tracks = raw.listFiles().orEmpty().map { it.nameWithoutExtension }.toSet()
        assertTrue("no audio found in res/raw", tracks.isNotEmpty())
        eachFact { _, _, fact, where ->
            val music = fact.optString("music")
            if (music.isNotEmpty()) {
                assertTrue("$where: unknown music \"$music\"", music in tracks)
            }
        }
    }

    @Test
    fun `every fact carries the fields OrbitData reads`() {
        eachFact { _, _, fact, where ->
            listOf("id", "cat", "accent", "scene", "title", "sub", "blurb").forEach { key ->
                assertTrue("$where: missing \"$key\"", fact.has(key))
            }
            assertTrue("$where: missing \"hero\"", fact.has("hero"))
            assertTrue("$where: bad accent \"${fact.getString("accent")}\"", isColor(fact.getString("accent")))

            val stats = fact.getJSONArray("stats")
            (0 until stats.length()).forEach { i ->
                val pair = stats.getJSONArray(i)
                assertEquals("$where: stat $i is not a [label, value] pair", 2, pair.length())
            }
        }
    }

    @Test
    fun `fact ids are unique across the whole catalog`() {
        val seen = mutableMapOf<String, String>()
        eachFact { _, _, fact, where ->
            val id = fact.getString("id")
            val first = seen.put(id, where)
            assertTrue("duplicate fact id \"$id\" in $where and $first", first == null)
        }
    }

    /**
     * Source links are keyed `<systemId>.<position in file>` (`OrbitData.toFact`, `num = it + 1`),
     * so inserting a fact mid-file silently re-points every later fact at the wrong page. Only a
     * whole-set comparison catches that; spot-checking individual keys cannot.
     */
    @Test
    fun `source link keys line up with fact positions`() {
        val expected = factFiles.flatMap { (sys, file) ->
            facts(file).indices.map { SourceManager.keyFor(sys, it + 1) }
        }.toSet()
        val actual = SourceManager.keys

        assertEquals("source keys with no fact at that position", emptySet<String>(), actual - expected)
        assertEquals("facts with no source key", emptySet<String>(), expected - actual)
    }

    /**
     * Per-fact source blocks were replaced by the map in `SourceManager`; any left behind are
     * dead data that can silently contradict the live link.
     */
    @Test
    fun `no fact carries a stale inline source`() {
        val stale = mutableListOf<String>()
        eachFact { _, _, fact, where ->
            if (fact.has("sourceName") || fact.has("sourceUrl") || fact.has("source")) stale += where
        }
        assertEquals("facts still carrying inline source keys", emptyList<String>(), stale)
    }

    @Test
    fun `comparison objects parse`() {
        val arr = JSONArray(File(assets, "comparison.json").readText())
        val kinds = CompKind.entries.map { it.name }.toSet()
        (0 until arr.length()).forEach { i ->
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "#$i")
            assertTrue("comparison [$name]: unknown kind \"${o.getString("kind")}\"", o.getString("kind") in kinds)
            assertTrue("comparison [$name]: diameter must be positive", o.getDouble("diaKm") > 0.0)
            listOf("sub", "sizeText").forEach { key ->
                assertTrue("comparison [$name]: missing \"$key\"", o.has(key))
            }
            o.getJSONArray("colors")
        }
    }

    @Test
    fun `planets parse`() {
        val arr = JSONArray(File(assets, "planets.json").readText())
        (0 until arr.length()).forEach { i ->
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "#$i")
            assertTrue("planet [$name]: diameter must be positive", o.getDouble("d") > 0.0)
            assertTrue("planet [$name]: distance must be positive", o.getDouble("au") > 0.0)
            o.getJSONArray("colors")
        }
    }

    @Test
    fun `every featured system has header copy and a valid card scene`() {
        val known = SceneId.entries.map { it.name }.toSet()
        val explorable = systems.getJSONArray("explorable")
        (0 until explorable.length()).forEach { i ->
            val o = explorable.getJSONObject(i)
            val id = o.getString("id")
            val meta = o.getJSONObject("meta")
            listOf("label", "eyebrow", "eyebrowColor", "title", "blurb").forEach { key ->
                assertTrue("$id.meta: missing \"$key\"", meta.has(key))
            }
            assertTrue("$id.meta: bad eyebrowColor", isColor(meta.getString("eyebrowColor")))

            val featured = o.getJSONObject("featured")
            val scene = featured.getString("scene")
            assertTrue("$id.featured: unknown scene \"$scene\"", scene in known)
            assertTrue("$id.featured: bad pillColor", isColor(featured.getString("pillColor")))
            listOf("pill", "title", "tagline").forEach { key ->
                assertTrue("$id.featured: missing \"$key\"", featured.has(key))
            }
        }
    }

    /** Mirrors `OrbitData.color`: a 6-hex string is opaque RGB, an 8-hex string is ARGB. */
    private fun isColor(s: String): Boolean {
        val v = s.removePrefix("0x").removePrefix("#")
        return v.length in 1..8 && v.toLongOrNull(16) != null
    }
}
