package com.orbit.starsystems.core

import android.content.Context
import android.content.SharedPreferences
import java.util.TimeZone

/**
 * The small amount of state that has to outlive the process: the saved collection,
 * which facts have been seen, and the daily-open streak.
 *
 * [init] runs once from MainActivity.onCreate, before any screen reads these. Every
 * accessor falls back to a sane default when that hasn't happened (Compose previews,
 * unit tests), so nothing here throws on an uninitialised object.
 */
object OrbitPrefs {

    private const val FILE = "orbit_prefs"
    private const val KEY_SAVED = "saved_ids"
    private const val KEY_VIEWED = "viewed_ids"
    private const val KEY_STREAK = "streak_days"
    private const val KEY_LAST_OPEN = "last_open_day"
    private const val KEY_FIRST_OPEN = "first_open_day"
    private const val KEY_LAST_VERSION = "last_seen_version"
    private const val KEY_AD_FREE = "ad_free"

    private const val MILLIS_PER_DAY = 86_400_000L

    /** The one fact a brand-new install starts with, seeded only on a true first run. */
    private val FIRST_RUN_SAVED = setOf("moon")

    private var prefs: SharedPreferences? = null

    /** Whether [init] found no existing data, i.e. this is the app's very first launch. */
    private var freshInstall = false

    /** Idempotent — safe to call from every Activity.onCreate. */
    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        prefs = p
        freshInstall = !p.contains(KEY_FIRST_OPEN)
        // Seed the first-run values only when the keys are absent, so a user who clears
        // their collection doesn't get "moon" handed back on the next launch.
        p.edit().apply {
            if (!p.contains(KEY_SAVED)) putStringSet(KEY_SAVED, FIRST_RUN_SAVED)
            if (!p.contains(KEY_FIRST_OPEN)) putLong(KEY_FIRST_OPEN, today())
        }.apply()
    }

    // getStringSet hands back an instance owned by SharedPreferences that must not be
    // mutated, so both accessors copy across the boundary.
    var saved: Set<String>
        get() = prefs?.getStringSet(KEY_SAVED, null)?.toSet() ?: FIRST_RUN_SAVED
        set(value) { prefs?.edit()?.putStringSet(KEY_SAVED, value.toSet())?.apply() }

    var viewed: Set<String>
        get() = prefs?.getStringSet(KEY_VIEWED, null)?.toSet() ?: emptySet()
        set(value) { prefs?.edit()?.putStringSet(KEY_VIEWED, value.toSet())?.apply() }

    /**
     * Cached copy of the Play "remove ads" entitlement, so the ad code can answer
     * synchronously on a cold start. Play owns the real answer and overwrites this on
     * every successful ownership query.
     */
    var adFree: Boolean
        get() = prefs?.getBoolean(KEY_AD_FREE, false) ?: false
        set(value) { prefs?.edit()?.putBoolean(KEY_AD_FREE, value)?.apply() }

    /** Consecutive days the app has been opened, today included. */
    val streak: Int get() = prefs?.getInt(KEY_STREAK, 1)?.coerceAtLeast(1) ?: 1

    /** Whole days since the first launch — 0 on the first day. */
    val daysSinceFirstOpen: Int
        get() {
            val p = prefs ?: return 0
            val first = p.getLong(KEY_FIRST_OPEN, today())
            return (today() - first).coerceAtLeast(0L).toInt()
        }

    /**
     * Advances the streak for today's launch: the same day changes nothing, yesterday
     * extends the run, and any longer gap (or a first launch) starts a new one at 1.
     * Idempotent within a day, so calling it on every launch is safe.
     */
    fun recordOpen() {
        val p = prefs ?: return
        val today = today()
        val last = p.getLong(KEY_LAST_OPEN, Long.MIN_VALUE)
        if (last == today) return
        val next = if (last == today - 1) p.getInt(KEY_STREAK, 0) + 1 else 1
        p.edit().putInt(KEY_STREAK, next).putLong(KEY_LAST_OPEN, today).apply()
    }

    /**
     * True exactly once after the app is updated: an existing install whose recorded
     * version is behind [current]. Records [current] either way, so a second call in the
     * same install returns false and the notice can't reappear.
     *
     * A build from before version tracking existed has no recorded version, so it reads
     * as an upgrade — which is what makes the notice show on the first update after this
     * release. A genuinely fresh install is excluded: nothing is "new" to a new user.
     */
    fun consumeWhatsNew(current: Long): Boolean {
        val p = prefs ?: return false
        val recorded = p.getLong(KEY_LAST_VERSION, Long.MIN_VALUE)
        if (recorded != current) p.edit().putLong(KEY_LAST_VERSION, current).apply()
        return !freshInstall && recorded < current
    }

    /**
     * Days since the epoch in the device's own time zone, so the streak rolls over at
     * local midnight rather than UTC. java.time isn't available at minSdk 24 without
     * core library desugaring, hence the offset arithmetic.
     */
    private fun today(): Long {
        val now = System.currentTimeMillis()
        return Math.floorDiv(now + TimeZone.getDefault().getOffset(now), MILLIS_PER_DAY)
    }
}
