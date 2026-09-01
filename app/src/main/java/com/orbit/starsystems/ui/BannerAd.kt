package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.orbit.starsystems.AdManager

/**
 * Anchored adaptive banner shown on the list screens. Adaptive sizing yields the
 * best fill/eCPM for the device width. [applyNavInset] adds the system
 * navigation-bar padding so the banner clears the gesture area when the app's own
 * bottom bar is hidden (it scrolls away), otherwise the bar supplies that inset.
 */
@Composable
fun BannerAd(applyNavInset: Boolean, modifier: Modifier = Modifier) {
    val widthDp = LocalConfiguration.current.screenWidthDp
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF08080A).copy(alpha = 0.92f))
            .then(if (applyNavInset) Modifier.navigationBarsPadding() else Modifier),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthDp))
                adUnitId = AdManager.bannerId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
