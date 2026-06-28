package com.orbit.starsystems

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orbit.starsystems.core.OrbitData

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    // Launches Play's full-screen immediate-update UI. A non-OK result means the user
    // backed out of (or the flow failed) a *forced* update, so we refuse to continue.
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            Log.w(TAG, "Immediate update flow did not complete (code=${result.resultCode}).")
            Toast.makeText(this, "An update is required to continue.", Toast.LENGTH_LONG).show()
            // Re-prompt; for a hard gate this also closes the app so the old version
            // can't be used until the update is taken.
            checkForImmediateUpdate()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OrbitData.init(this)
        MobileAds.initialize(this) {}
        AdManager.loadInterstitial(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForImmediateUpdate()

        enableEdgeToEdge()
        setContent {
            OrbitApp()
        }
    }

    override fun onResume() {
        super.onResume()
        // If an immediate update was already running (e.g. the app was killed mid-update),
        // Play reports it as in progress — resume the flow so the user can't slip past it.
        if (!::appUpdateManager.isInitialized) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(info)
            }
        }
    }

    private fun checkForImmediateUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    startImmediateUpdate(info)
                }
            }
            .addOnFailureListener { e ->
                // No Play Store / offline / sideloaded build — let the app run normally.
                Log.w(TAG, "App update check failed: ${e.message}")
            }
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
        }.onFailure { e ->
            Log.w(TAG, "Could not start immediate update: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "OrbitUpdate"
    }
}
