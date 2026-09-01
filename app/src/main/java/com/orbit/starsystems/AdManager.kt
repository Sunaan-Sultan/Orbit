package com.orbit.starsystems

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private var interstitialAd: InterstitialAd? = null

    // Shared frequency cap so every trigger point competes for the same slot and
    // ads never stack. Tune these two knobs to trade revenue vs. user experience.
    private const val ACTIONS_PER_AD = 2       // show on every Nth eligible action
    private const val MIN_INTERVAL_MS = 40_000L // ...but never more often than this
    private var actionsSinceAd = 0
    private var lastShownAt = 0L

    // Toggle this for production
    private const val USE_TEST_ADS = false

    private const val REAL_INTERSTITIAL_ID = "ca-app-pub-9720007236604856/3193381376"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    private const val REAL_BANNER_ID = "ca-app-pub-9720007236604856/2781830136"
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    private val interstitialId: String
        get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_ID else REAL_INTERSTITIAL_ID

    val bannerId: String
        get() = if (USE_TEST_ADS) TEST_BANNER_ID else REAL_BANNER_ID

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
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
        if (interstitialAd != null && now - lastShownAt >= minIntervalMs) {
            actionsSinceAd = 0
            lastShownAt = now
            showInterstitial(activity, onProceed)
        } else {
            onProceed()
        }
    }

    /**
     * Entry point for all in-app triggers. Applies the shared frequency cap and
     * shows an interstitial only when both the action count and time interval
     * allow it. Always invokes [onProceed] so navigation is never blocked.
     */
    fun maybeShowInterstitial(activity: android.app.Activity, onProceed: () -> Unit) {
        actionsSinceAd += 1
        val now = System.currentTimeMillis()
        val enoughActions = actionsSinceAd >= ACTIONS_PER_AD
        val enoughTime = now - lastShownAt >= MIN_INTERVAL_MS
        if (interstitialAd != null && enoughActions && enoughTime) {
            actionsSinceAd = 0
            lastShownAt = now
            showInterstitial(activity, onProceed)
        } else {
            onProceed()
        }
    }

    fun showInterstitial(context: android.app.Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(context)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(context)
        } else {
            onAdDismissed()
        }
    }
}
