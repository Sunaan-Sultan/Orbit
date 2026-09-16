package com.orbit.starsystems

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.orbit.starsystems.core.Analytics
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.Streak

/**
 * Process-wide setup, so that the things every entry point needs are ready wherever the process
 * was started from.
 *
 * It exists chiefly for analytics. The reminder runs in a cold process with no Activity, so a
 * sink attached in `MainActivity.onCreate` would be null exactly when the notification is being
 * posted — silently losing the one event that says whether notifications are being delivered at
 * all. Attaching it here covers both paths.
 */
class OrbitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        OrbitPrefs.init(this)
        OrbitData.init(this)
        attachAnalytics()
    }

    /**
     * Wires Firebase to [Analytics], if it is configured.
     *
     * `initializeApp` returns null when no `google-services.json` has been supplied, which is the
     * normal state of this repo. In that case the sink stays null and events are dropped exactly
     * as before — the app builds, runs and ships without a Firebase account, and starts reporting
     * the moment one is added.
     */
    private fun attachAnalytics() {
        if (Analytics.sink != null) return
        val app = runCatching { FirebaseApp.initializeApp(this) }.getOrNull()
        if (app == null) {
            Log.i(TAG, "No Firebase config — analytics events stay on the device.")
            return
        }
        val firebase = FirebaseAnalytics.getInstance(this)
        Analytics.sink = Analytics.Sink { event, params ->
            firebase.logEvent(event, Bundle().apply { params.forEach { (k, v) -> putString(k, v) } })
        }
        // Cohorts worth splitting every other number by. Set once per process, from values that
        // are already on disk, so none of this costs a read at an awkward moment.
        firebase.setUserProperty("ad_free", OrbitPrefs.adFree.toString())
        firebase.setUserProperty("notify_on", OrbitPrefs.notifyEnabled.toString())
        firebase.setUserProperty("streak_bucket", streakBucket(OrbitPrefs.streakState))
    }

    /** Buckets rather than the raw number: a user property with hundreds of values is useless. */
    private fun streakBucket(streak: Streak): String = when (streak.current) {
        0 -> "0"
        in 1..2 -> "1-2"
        in 3..6 -> "3-6"
        in 7..29 -> "7-29"
        else -> "30+"
    }

    private companion object {
        const val TAG = "OrbitApp"
    }
}
