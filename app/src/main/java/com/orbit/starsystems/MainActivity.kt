package com.orbit.starsystems

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.orbit.starsystems.core.OrbitData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OrbitData.init(this)
        MobileAds.initialize(this) {}
        AdManager.loadInterstitial(this)
        enableEdgeToEdge()
        setContent {
            OrbitApp()
        }
    }
}
