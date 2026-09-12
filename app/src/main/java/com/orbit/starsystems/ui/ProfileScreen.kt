package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.AdManager
import com.orbit.starsystems.AppActions
import com.orbit.starsystems.billing.BillingManager
import com.orbit.starsystems.core.FEATURED_SYSTEMS
import com.orbit.starsystems.core.OrbitPrefs

private val Danger = Color(0xFFE0654A)

@Composable
fun ProfileScreen(savedCount: Int, viewed: Int, onClearSaved: () -> Unit) {
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }
    // Result of the last manual update check, shown under the row instead of as a toast.
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    // Result of the last purchase or restore attempt, shown under the row it came from.
    var purchaseStatus by remember { mutableStateOf<String?>(null) }
    var billingBusy by remember { mutableStateOf(false) }
    // Both come from disk and only change between launches, so a plain read is enough.
    val streak = OrbitPrefs.streak
    val daysExploring = OrbitPrefs.daysSinceFirstOpen

    Column(
        Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(bottom = 80.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Sphere(sizeUnits = 84f, colors = listOf(Color(0xFF9CC4EC), Color(0xFF3D72B8), Color(0xFF1A3360)), glow = Color(0xFF508CD2).copy(alpha = 0.4f))
            Text("Stargazer", style = ts(22f, FontWeight.Bold, Color.White), modifier = Modifier.padding(top = 16.dp))
            Text(
                when (daysExploring) {
                    0 -> "Exploring since today"
                    1 -> "Exploring since yesterday"
                    else -> "Exploring for $daysExploring days"
                },
                style = ts(13.5f, color = Dim, spacingEm = 0.02f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row(
            Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                .padding(vertical = 18.dp),
        ) {
            ProfileStat(viewed.toString(), "Facts seen")
            ProfileStat(FEATURED_SYSTEMS.size.toString(), "Systems")
            ProfileStat(savedCount.toString(), "Saved")
        }
        Row(
            Modifier
                .padding(horizontal = 18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFE0744A).copy(alpha = 0.18f), Color(0xFFFF9E34).copy(alpha = 0.06f))))
                .border(1.dp, Color(0xFFFF9E34).copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico("bolt", size = 30.dp, color = Color(0xFFFF9E34), filled = true)
            Spacer(Modifier.width(14.dp))
            Column {
                Text("$streak-day streak", style = ts(19f, FontWeight.Bold, Color.White))
                Text(
                    if (streak == 1) "Come back tomorrow to start a run." else "Come back tomorrow to keep it going.",
                    style = ts(13.5f, color = Color(0xFFC9A98A)),
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }

        SectionLabel("ADS")
        SettingsCard {
            if (BillingManager.isAdFree) {
                SettingsRow(
                    icon = "bolt",
                    iconTint = Color(0xFF9CC4EC),
                    title = "Ad-free unlocked",
                    subtitle = "Thanks for supporting Orbit.",
                    showChevron = false,
                )
            } else {
                SettingsRow(
                    icon = "bolt",
                    iconTint = Color(0xFF9CC4EC),
                    title = "Remove ads",
                    subtitle = when {
                        billingBusy -> "Opening Google Play…"
                        purchaseStatus != null -> purchaseStatus!!
                        // Play's own localised price, never a hardcoded one — it differs
                        // by country, tax and currency.
                        BillingManager.price != null -> "One-time purchase · ${BillingManager.price}"
                        else -> "One-time purchase, yours forever"
                    },
                    subtitleTint = if (purchaseStatus != null && !billingBusy) Color(0xFF9CC4EC) else Dim,
                    enabled = !billingBusy,
                    onClick = {
                        val activity = context as? android.app.Activity ?: return@SettingsRow
                        billingBusy = true
                        purchaseStatus = null
                        BillingManager.launchPurchase(activity) { outcome ->
                            billingBusy = false
                            purchaseStatus = when (outcome) {
                                BillingManager.Outcome.PURCHASED -> {
                                    AdManager.discard()
                                    "Ads removed — thank you!"
                                }
                                BillingManager.Outcome.CANCELLED -> null
                                BillingManager.Outcome.UNAVAILABLE ->
                                    "Google Play isn't available right now."
                                BillingManager.Outcome.ERROR ->
                                    "Purchase didn't go through. Please try again."
                            }
                        }
                    },
                )
                RowDivider()
                SettingsRow(
                    icon = "refresh",
                    title = "Restore purchase",
                    subtitle = "Already bought it? Bring it back here",
                    enabled = !billingBusy,
                    onClick = {
                        billingBusy = true
                        purchaseStatus = null
                        BillingManager.refreshPurchases { owned ->
                            billingBusy = false
                            purchaseStatus = if (owned) {
                                AdManager.discard()
                                "Purchase restored — ads are off."
                            } else {
                                "No previous purchase found on this account."
                            }
                        }
                    },
                )
            }
        }

        SectionLabel("SUPPORT ORBIT")
        SettingsCard {
            SettingsRow(
                icon = "star",
                iconTint = Color(0xFFFFC24D),
                title = "Leave a review",
                subtitle = "Rate Space Facts on Google Play",
                onClick = { AppActions.openPlayListing(context) },
            )
            RowDivider()
            SettingsRow(
                icon = "share",
                title = "Share the app",
                subtitle = "Send a friend the Play Store link",
                onClick = { AppActions.shareApp(context) },
            )
        }

        SectionLabel("HELP")
        SettingsCard {
            SettingsRow(
                icon = "mail",
                title = "Contact support",
                subtitle = AppActions.SUPPORT_EMAIL,
                onClick = { AppActions.emailSupport(context) },
            )
            RowDivider()
            SettingsRow(
                icon = "refresh",
                title = "Check for updates",
                subtitle = when {
                    checkingUpdate -> "Checking…"
                    else -> updateStatus ?: "Ask Google Play for a newer build"
                },
                subtitleTint = if (updateStatus != null && !checkingUpdate) Color(0xFF9CC4EC) else Dim,
                enabled = !checkingUpdate,
                onClick = {
                    checkingUpdate = true
                    updateStatus = null
                    AppActions.checkForUpdates(context) { result ->
                        checkingUpdate = false
                        updateStatus = result
                    }
                },
            )
        }

        SectionLabel("DATA")
        SettingsCard {
            SettingsRow(
                icon = "trash",
                iconTint = if (savedCount > 0) Danger else Color(0xFF5A5A5A),
                title = "Clear saved facts",
                titleTint = if (savedCount > 0) Danger else Color(0xFF5A5A5A),
                subtitle = if (savedCount > 0) {
                    "Remove all $savedCount from your collection"
                } else {
                    "Nothing saved yet"
                },
                showChevron = savedCount > 0,
                enabled = savedCount > 0,
                onClick = { confirmClear = true },
            )
        }

        SectionLabel("ABOUT")
        SettingsCard {
            SettingsRow(
                icon = "info",
                title = "App version",
                // Read off the installed package, so it always matches the running build.
                trailing = AppActions.versionLabel(context),
                showChevron = false,
            )
        }

        Text(
            "A daily window onto the cosmos.",
            style = ts(13f, color = Color(0xFF5A5A5A)),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 30.dp),
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Color(0xFF141418),
            titleContentColor = Color.White,
            textContentColor = Mute,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Clear saved facts?", style = ts(19f, FontWeight.Bold, Color.White)) },
            text = {
                Text(
                    "All $savedCount saved fact${if (savedCount == 1) "" else "s"} will be removed from your collection. This can't be undone.",
                    style = ts(14.5f, color = Mute, lineHeight = 21f),
                )
            },
            confirmButton = {
                TextButton(onClick = { onClearSaved(); confirmClear = false }) {
                    Text("Clear", style = ts(15f, FontWeight.SemiBold, Danger))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel", style = ts(15f, FontWeight.SemiBold, Mute))
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) =
    Text(
        text,
        style = ts(11.5f, FontWeight.Bold, Dim, 0.22f),
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 10.dp),
    )

/** The rounded translucent container the settings rows sit in, matching the stats card. */
@Composable
private fun SettingsCard(content: @Composable () -> Unit) =
    Column(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp)),
    ) { content() }

@Composable
private fun RowDivider() =
    Box(Modifier.padding(start = 62.dp).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    iconTint: Color = Color(0xFFCFCFCF),
    titleTint: Color = Color.White,
    subtitleTint: Color = Dim,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            Ico(icon, size = 19.dp, color = iconTint)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = ts(15.5f, FontWeight.SemiBold, titleTint))
            subtitle?.let {
                Text(it, style = ts(12.5f, color = subtitleTint), modifier = Modifier.padding(top = 2.dp))
            }
        }
        trailing?.let {
            Text(it, style = ts(14f, FontWeight.Medium, Mute), modifier = Modifier.padding(start = 8.dp))
        }
        if (showChevron) {
            Spacer(Modifier.width(6.dp))
            Ico("chevR", size = 17.dp, color = Color(0xFF5A5A5A))
        }
    }
}

@Composable
private fun RowScope.ProfileStat(n: String, label: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n, style = ts(30f, FontWeight.Bold, Color.White))
        Text(label.uppercase(), style = ts(11f, FontWeight.SemiBold, Dim, 0.1f), modifier = Modifier.padding(top = 4.dp))
    }
}
