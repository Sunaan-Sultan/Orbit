package com.orbit.starsystems

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.ALL_FACTS
import com.orbit.starsystems.core.factById
import com.orbit.starsystems.ui.BottomNav
import com.orbit.starsystems.ui.DetailSheet
import com.orbit.starsystems.ui.FactScreen
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.ProfileScreen
import com.orbit.starsystems.ui.SavedScreen
import com.orbit.starsystems.ui.SystemExplore
import com.orbit.starsystems.ui.SystemsList
import kotlinx.coroutines.delay

@Composable
fun OrbitApp() {
    var tab by remember { mutableStateOf("systems") }
    var openSys by remember { mutableStateOf<String?>(null) }
    var factId by remember { mutableStateOf<String?>(null) }
    var paused by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(setOf("moon")) }
    var sheet by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var viewed by remember { mutableStateOf(setOf<String>()) }

    val context = LocalContext.current
    DisposableEffect(factId == null) {
        if (factId != null) return@DisposableEffect onDispose {}

        val mp = MediaPlayer.create(context, R.raw.menu_music).apply {
            isLooping = true
            start()
        }

        onDispose {
            mp.stop()
            mp.release()
        }
    }

    fun toggleSave(id: String) {
        saved = if (saved.contains(id)) saved - id else saved + id
    }

    fun openFact(id: String) {
        factId = id; paused = false; sheet = false; viewed = viewed + id
    }

    LaunchedEffect(toast) {
        if (toast != null) { delay(1700); toast = null }
    }

    // Unwind the in-app navigation stack on system back before letting the OS exit.
    BackHandler(enabled = sheet || factId != null || openSys != null || tab != "systems") {
        when {
            sheet -> sheet = false
            factId != null -> { sheet = false; factId = null }
            openSys != null -> openSys = null
            tab != "systems" -> tab = "systems"
        }
    }

    val curFact = factById(factId) ?: ALL_FACTS.first()

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (factId != null) {
            FactScreen(
                fact = curFact,
                paused = paused,
                onTogglePause = { paused = !paused },
                onBack = { sheet = false; factId = null },
                onLearn = { paused = true; sheet = true },
                isSaved = saved.contains(curFact.id),
                onToggleSave = {
                    val was = saved.contains(curFact.id)
                    toggleSave(curFact.id)
                    toast = if (was) "Removed from Saved" else "Saved to your collection"
                },
            )
        } else {
            Box(Modifier.fillMaxSize()) {
                when (tab) {
                    "systems" -> {
                        val sys = openSys
                        if (sys != null) {
                            SystemExplore(sys = sys, onOpenFact = { openFact(it) }, onBack = { openSys = null })
                        } else {
                            SystemsList(onOpenSol = { openSys = "sol" }, onOpenAcen = { openSys = "acen" })
                        }
                    }
                    "saved" -> SavedScreen(saved = saved, onOpen = { openFact(it) })
                    "you" -> ProfileScreen(savedCount = saved.size, viewed = viewed.size)
                }
            }
        }

        DetailSheet(
            fact = curFact,
            open = sheet,
            onClose = { sheet = false },
            onJump = { sheet = false; openFact(it) },
            isSaved = saved.contains(curFact.id),
            onToggleSave = { toggleSave(curFact.id) },
        )

        toast?.let { msg ->
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (factId != null) 30.dp else 90.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(msg, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black))
            }
        }

        if (factId == null) {
            BottomNav(
                tab = tab,
                onSelect = { tab = it },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            )
        }
    }
}
