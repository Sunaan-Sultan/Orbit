package com.orbit.starsystems

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.ui.UpdateCheckSplash
import com.orbit.starsystems.ui.UpdateRequiredScreen

class MainActivity : ComponentActivity() {

    /** What the UI is allowed to show. */
    private enum class Gate {
        /** Play hasn't answered yet — show nothing, so the app is never briefly usable. */
        CHECKING,

        /** No forced update outstanding. */
        ALLOWED,

        /** A required update is outstanding; [UpdateRequiredScreen] replaces the app. */
        BLOCKED,
    }

    private lateinit var appUpdateManager: AppUpdateManager

    private var gate by mutableStateOf(Gate.CHECKING)

    /** Kept so the gate's button can relaunch Play's flow without querying again. */
    private var pendingUpdate: AppUpdateInfo? = null

    /** True while Play's UI is up, so the two callers below can't launch it twice. */
    private var updateFlowLaunched = false

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * A hung update check must not brick the app, so the gate opens if Play hasn't
     * answered in [CHECK_TIMEOUT_MS]. The check runs again on the next launch.
     */
    private val checkTimeout = Runnable {
        if (gate == Gate.CHECKING) {
            Log.w(TAG, "Update check timed out after ${CHECK_TIMEOUT_MS}ms; letting the app run.")
            gate = Gate.ALLOWED
        }
    }

    // Play's full-screen immediate-update UI closed. A non-OK result means the user backed
    // out of (or the flow failed) a *forced* update, so the gate stays down and the screen
    // behind it offers the only way on.
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result: ActivityResult ->
        updateFlowLaunched = false
        if (result.resultCode != RESULT_OK) {
            Log.w(TAG, "Immediate update flow did not complete (code=${result.resultCode}).")
            gate = Gate.BLOCKED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OrbitData.init(this)
        OrbitPrefs.init(this)
        // Count this launch towards the daily streak before the UI reads it.
        OrbitPrefs.recordOpen()
        MobileAds.initialize(this) {}
        AdManager.loadInterstitial(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForImmediateUpdate()

        enableEdgeToEdge()
        setContent {
            when (gate) {
                Gate.CHECKING -> UpdateCheckSplash()
                Gate.BLOCKED -> UpdateRequiredScreen(
                    onUpdate = {
                        // Every path that blocks also records the info, but a dead button
                        // on a hard gate would trap the user — so fall back to the listing.
                        pendingUpdate?.let { startImmediateUpdate(it) }
                            ?: AppActions.openPlayListing(this)
                    },
                )
                Gate.ALLOWED -> OrbitApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::appUpdateManager.isInitialized) return
        // If an immediate update was already running (e.g. the app was killed mid-update),
        // Play reports it as in progress — resume the flow so the user can't slip past it.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                block(info)
            }
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(checkTimeout)
        super.onDestroy()
    }

    private fun checkForImmediateUpdate() {
        mainHandler.postDelayed(checkTimeout, CHECK_TIMEOUT_MS)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                mainHandler.removeCallbacks(checkTimeout)
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    block(info)
                } else {
                    gate = Gate.ALLOWED
                }
            }
            .addOnFailureListener { e ->
                mainHandler.removeCallbacks(checkTimeout)
                // No Play Store / offline / sideloaded build — nothing to enforce.
                Log.w(TAG, "App update check failed: ${e.message}")
                gate = Gate.ALLOWED
            }
    }

    /** Closes the gate *before* launching the flow, so a launch that fails still blocks. */
    private fun block(info: AppUpdateInfo) {
        pendingUpdate = info
        gate = Gate.BLOCKED
        startImmediateUpdate(info)
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        if (updateFlowLaunched) return
        updateFlowLaunched = true
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
        }.onFailure { e ->
            updateFlowLaunched = false
            Log.w(TAG, "Could not start immediate update: ${e.message}")
            // The gate is already down; its button is the retry.
        }
    }

    private companion object {
        const val TAG = "OrbitUpdate"
        const val CHECK_TIMEOUT_MS = 2_500L
    }
}
