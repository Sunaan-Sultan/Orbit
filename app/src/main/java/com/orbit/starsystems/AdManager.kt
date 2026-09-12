package com.orbit.starsystems

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.orbit.starsystems.billing.BillingManager

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var loadInFlight = false

    // Shared frequency cap so every trigger point competes for the same slot and
    // ads never stack. Tune these knobs to trade revenue vs. user experience.
    private const val ACTIONS_PER_AD = 4         // show on every Nth eligible action
    private const val MIN_INTERVAL_MS = 100_000L // ...but never more often than this
    private const val LAUNCH_GRACE_MS = 90_000L  // ...and never this soon after a launch

    private var actionsSinceAd = 0
    private var lastShownAt = 0L
    private var sessionStartedAt = System.currentTimeMillis()

    // Debug builds must never touch the live unit: a developer clicking real ads is
    // what gets an AdMob account suspended.
    private val useTestAds: Boolean get() = BuildConfig.DEBUG

    private const val REAL_INTERSTITIAL_ID = "ca-app-pub-9720007236604856/3193381376"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    private const val REAL_BANNER_ID = "ca-app-pub-9720007236604856/2781830136"
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    private val interstitialId: String
        get() = if (useTestAds) TEST_INTERSTITIAL_ID else REAL_INTERSTITIAL_ID

    val bannerId: String
        get() = if (useTestAds) TEST_BANNER_ID else REAL_BANNER_ID

    val adsEnabled: Boolean get() = !BillingManager.isAdFree

    var isAdShowing by mutableStateOf(false)
        private set

    /** Restarts the launch grace period. Called once from MainActivity.onCreate. */
    fun startSession() {
        sessionStartedAt = System.currentTimeMillis()
        actionsSinceAd = 0
        lastShownAt = 0L
        isAdShowing = false
    }

    fun loadInterstitial(context: Context) {
        if (!adsEnabled || loadInFlight || interstitialAd != null) return
        loadInFlight = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadInFlight = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadInFlight = false
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Time-only trigger: shows an interstitial if at least [minIntervalMs] has
     * elapsed since the last ad, ignoring the action count. Used for fact
     * scrolling, where we want an ad roughly every [minIntervalMs] regardless of
     * how many facts were swiped. Shares [lastShownAt] with the other triggers so
     * ads never bunch up. Always invokes [onProceed].
     */
    fun maybeShowInterstitialAfter(
        activity: android.app.Activity,
        minIntervalMs: Long,
        onProceed: () -> Unit,
    ) {
        val now = System.currentTimeMillis()
        if (interstitialAd != null && quotaAllows(now, minIntervalMs)) {
            record(now)
            showInterstitial(activity, onProceed)
        } else {
            loadInterstitial(activity)
            onProceed()
        }
    }

    /**
     * Entry point for all in-app triggers. Applies the shared frequency cap and
     * shows an interstitial only when the action count, time interval and launch
     * grace period all allow it. Always invokes [onProceed] so navigation is never
     * blocked.
     *
     * Actions taken while no ad is loaded are not counted: otherwise a fill outage
     * banks up credits and the first ad to load fires immediately.
     */
    fun maybeShowInterstitial(activity: android.app.Activity, onProceed: () -> Unit) {
        if (interstitialAd == null) {
            loadInterstitial(activity)
            onProceed()
            return
        }
        actionsSinceAd += 1
        val now = System.currentTimeMillis()
        if (actionsSinceAd >= ACTIONS_PER_AD && quotaAllows(now, MIN_INTERVAL_MS)) {
            record(now)
            showInterstitial(activity, onProceed)
        } else {
            onProceed()
        }
    }

    private fun quotaAllows(now: Long, minIntervalMs: Long): Boolean =
        adsEnabled &&
            now - sessionStartedAt >= LAUNCH_GRACE_MS &&
            now - lastShownAt >= minIntervalMs

    private fun record(now: Long) {
        actionsSinceAd = 0
        lastShownAt = now
    }

    fun showInterstitial(context: android.app.Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            onAdDismissed()
            return
        }
        isAdShowing = true
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                interstitialAd = null
                loadInterstitial(context)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isAdShowing = false
                interstitialAd = null
                onAdDismissed()
            }
        }
        ad.show(context)
    }

    /** Drops any cached ad the moment the user buys the entitlement. */
    fun discard() {
        interstitialAd = null
        loadInFlight = false
    }
}
