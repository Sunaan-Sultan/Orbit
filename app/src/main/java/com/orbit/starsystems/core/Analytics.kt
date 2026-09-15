package com.orbit.starsystems.core

import android.util.Log
import com.orbit.starsystems.BuildConfig

/**
 * The app's event taxonomy, and the one place any of it is named.
 *
 * Space Facts ships ads and a paid ad-free upgrade while measuring nothing: there is no way
 * to tell which facts are watched, where a session ends, or whether a change to the ad cap
 * helped or hurt. Every call site is wired here so that question becomes answerable.
 *
 * **No events leave the device yet.** Attaching a provider is deliberately the last step,
 * because it is the only part that needs an account: add the Firebase Analytics dependency
 * and `app/google-services.json`, then set [sink] from `MainActivity.onCreate` —
 *
 * ```
 * Analytics.sink = Analytics.Sink { event, params ->
 *     Firebase.analytics.logEvent(event, bundleOf(*params.toList().toTypedArray()))
 * }
 * ```
 *
 * Until then a debug build logs to logcat, so the call sites can be verified without one.
 */
object Analytics {

    private const val TAG = "OrbitAnalytics"

    fun interface Sink {
        fun log(event: String, params: Map<String, String>)
    }

    /** Null until a provider is attached; events are dropped rather than queued. */
    var sink: Sink? = null

    /** Where a fact was opened from, so drop-off can be attributed to a surface. */
    object Source {
        const val TODAY = "today"
        const val SYSTEM = "system"
        const val SPOTLIGHT = "spotlight"
        const val SAVED = "saved"
        const val SEARCH = "search"
        const val DEEP_LINK = "deep_link"
    }

    fun factView(fact: Fact, source: String) =
        log("fact_view", "fact_id" to fact.id, "system" to fact.sys, "source" to source)

    fun systemOpen(sysId: String) = log("system_open", "system" to sysId)

    fun factSave(fact: Fact, saved: Boolean) =
        log("fact_save", "fact_id" to fact.id, "saved" to saved.toString())

    fun factShare(fact: Fact) = log("share_fact", "fact_id" to fact.id, "system" to fact.sys)

    fun search(query: String, resultCount: Int) =
        log("search", "length" to query.length.toString(), "results" to resultCount.toString())

    fun reminderToggled(enabled: Boolean, hour: Int) =
        log("reminder_toggled", "enabled" to enabled.toString(), "hour" to hour.toString())

    fun purchase(outcome: String) = log("purchase", "outcome" to outcome)

    private fun log(event: String, vararg params: Pair<String, String>) {
        val map = params.toMap()
        sink?.log(event, map)
        if (BuildConfig.DEBUG) Log.d(TAG, "$event $map")
    }
}
