package com.orbit.starsystems

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.orbit.starsystems.core.ALL_FACTS
import com.orbit.starsystems.core.Analytics
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.factById
import com.orbit.starsystems.core.factsForSys
import com.orbit.starsystems.ui.BannerAd
import com.orbit.starsystems.ui.BottomNav
import com.orbit.starsystems.ui.ComparisonScreen
import com.orbit.starsystems.ui.DetailSheet
import com.orbit.starsystems.ui.FactScreen
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.ProfileScreen
import com.orbit.starsystems.ui.QuizScreen
import com.orbit.starsystems.ui.SavedScreen
import com.orbit.starsystems.ui.SearchScreen
import com.orbit.starsystems.ui.ShareCardCapture
import com.orbit.starsystems.ui.SourceWebScreen
import com.orbit.starsystems.ui.SpotlightScreen
import com.orbit.starsystems.ui.SystemExplore
import com.orbit.starsystems.ui.SystemsList
import com.orbit.starsystems.ui.WhatsNewSheet
import kotlinx.coroutines.delay

@Composable
fun SpaceFactsApp(
    pendingFactId: String? = null,
    onPendingFactConsumed: () -> Unit = {},
    pendingQuiz: Boolean = false,
    onPendingQuizConsumed: () -> Unit = {},
) {
    var tab by remember { mutableStateOf("systems") }
    var openSys by remember { mutableStateOf<String?>(null) }
    var factId by remember { mutableStateOf<String?>(null) }
    var paused by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(OrbitPrefs.saved) }
    var sheet by remember { mutableStateOf(false) }
    var sourceUrl by remember { mutableStateOf<String?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var viewed by remember { mutableStateOf(OrbitPrefs.viewed) }
    var barVisible by remember { mutableStateOf(true) }
    var whatsNew by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    var quizOpen by remember { mutableStateOf(false) }
    // Non-null only while a share card is being drawn and captured.
    var sharing by remember { mutableStateOf<String?>(null) }

    // Mirror the collection and seen-list back to disk whenever they change, so both
    // survive the process. The first run of each is a no-op write of what was just read.
    LaunchedEffect(saved) { OrbitPrefs.saved = saved }
    LaunchedEffect(viewed) { OrbitPrefs.viewed = viewed }

    // Hide the bottom bar when the content scrolls down, reveal it when scrolling up.
    val barScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -2f) barVisible = false
                else if (available.y > 2f) barVisible = true
                return Offset.Zero
            }
        }
    }
    // Always show the bar again when switching tabs.
    LaunchedEffect(tab, openSys) { barVisible = true }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val adShowing = AdManager.isAdShowing

    // Show the release notes once, on the first launch after an update. consumeWhatsNew
    // records the version as it answers, so re-running this (e.g. after a rotation)
    // returns false rather than showing the sheet again.
    LaunchedEffect(Unit) {
        whatsNew = OrbitPrefs.consumeWhatsNew(AppActions.versionCode(context))
    }
    // Menu music disabled
    // DisposableEffect(factId == null, lifecycleOwner) {
    //     if (factId != null) return@DisposableEffect onDispose {}
    //     val mp = MediaPlayer.create(context, R.raw.menu_music).apply {
    //         isLooping = true
    //         start()
    //     }
    //     val observer = LifecycleEventObserver { _, event ->
    //         when (event) {
    //             Lifecycle.Event.ON_PAUSE -> mp.pause()
    //             Lifecycle.Event.ON_RESUME -> mp.start()
    //             else -> {}
    //         }
    //     }
    //     lifecycleOwner.lifecycle.addObserver(observer)
    //     onDispose {
    //         lifecycleOwner.lifecycle.removeObserver(observer)
    //         mp.stop()
    //         mp.release()
    //     }
    // }

    fun toggleSave(id: String) {
        val nowSaved = !saved.contains(id)
        saved = if (nowSaved) saved + id else saved - id
        factById(id)?.let { Analytics.factSave(it, nowSaved) }
    }

    val activity = context as? android.app.Activity

    // Route a navigation transition through the shared interstitial cap, then run
    // [action]. maybeShowInterstitial() always calls back (immediately if no ad /
    // no Activity / capped), so navigation is never blocked.
    fun withAd(action: () -> Unit) {
        if (activity != null) AdManager.maybeShowInterstitial(activity, action) else action()
    }

    fun openFact(id: String, source: String) {
        // The streak counts days something was actually read, not bare launches, so it is
        // recorded here rather than in MainActivity. Finishing the daily quiz credits the same
        // day through the same transform. Idempotent within a day.
        OrbitPrefs.recordActivity()
        factById(id)?.let { Analytics.factView(it, source) }
        withAd { factId = id; paused = false; sheet = false; sourceUrl = null; viewed = viewed + id }
    }

    /**
     * Opens a fact without consulting the ad cap. Used for deep links, where the user tapped
     * a notification or a shared link rather than navigating inside the app — an interstitial
     * on arrival would be an ad they never asked for.
     */
    fun jumpToFact(id: String) {
        OrbitPrefs.recordActivity()
        factById(id)?.let { Analytics.factView(it, Analytics.Source.DEEP_LINK) }
        factId = id
        paused = false
        sheet = false
        sourceUrl = null
        viewed = viewed + id
    }

    // A deep link can arrive before this composes (cold start behind the update gate) or
    // long after it (singleTop onNewIntent), so it is consumed here rather than passed in once.
    LaunchedEffect(pendingFactId) {
        val target = pendingFactId ?: return@LaunchedEffect
        if (factById(target) != null) {
            tab = "systems"
            openSys = null
            jumpToFact(target)
        }
        onPendingFactConsumed()
    }

    // The reminder points here when a streak is at stake. Not routed through the ad cap, for the
    // same reason jumpToFact isn't: an interstitial on arrival is an ad the user never asked for.
    LaunchedEffect(pendingQuiz) {
        if (!pendingQuiz) return@LaunchedEffect
        tab = "systems"
        openSys = null
        searching = false
        // Load-bearing: the quiz only renders while no fact player is open, so arriving from a
        // notification on top of one would otherwise set the flag with nothing on screen.
        factId = null
        quizOpen = true
        Analytics.notificationOpened("quiz")
        onPendingQuizConsumed()
    }

    fun exitPlayer() {
        sheet = false
        sourceUrl = null
        factId = null
    }

    LaunchedEffect(toast) {
        if (toast != null) { delay(1700); toast = null }
    }

    // Unwind the in-app navigation stack on system back before letting the OS exit.
    BackHandler(enabled = sourceUrl != null || sheet || factId != null || searching || quizOpen || openSys != null || tab != "systems") {
        when {
            sourceUrl != null -> sourceUrl = null
            sheet -> sheet = false
            factId != null -> exitPlayer()
            searching -> searching = false
            quizOpen -> quizOpen = false
            openSys != null -> openSys = null
            tab != "systems" -> tab = "systems"
        }
    }

    val curFact = factById(factId) ?: ALL_FACTS.first()

    // Page through the facts of the opened fact's own system (works for systems,
    // Spotlight, and Saved alike — independent of which screen launched it).
    val pagerSys = factById(factId)?.sys
    val pagerFacts = remember(pagerSys) {
        pagerSys?.let { factsForSys(it) } ?: emptyList()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (factId != null && pagerFacts.isNotEmpty()) {
            val initialPage = remember(pagerFacts) {
                pagerFacts.indexOfFirst { it.id == factId }.coerceAtLeast(0)
            }
            val pagerState = key(pagerSys) { rememberPagerState(initialPage = initialPage) { pagerFacts.size } }

            LaunchedEffect(factId, pagerFacts) {
                val target = pagerFacts.indexOfFirst { it.id == factId }
                if (target >= 0 && target != pagerState.currentPage) pagerState.scrollToPage(target)
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    val newFact = pagerFacts[page]
                    if (factId != newFact.id) {
                        factId = newFact.id
                        viewed = viewed + newFact.id
                        // Scrolling facts: surface an ad on the first swipe once
                        // 120s have elapsed since the last one (time-based, not per-swipe).
                        activity?.let { AdManager.maybeShowInterstitialAfter(it, 120_000L) {} }
                    }
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { pagerFacts[it].id }
            ) { page ->
                val f = pagerFacts[page]
                FactScreen(
                    fact = f,
                    paused = paused || sheet || sourceUrl != null || whatsNew || adShowing,
                    isActive = factId == f.id,
                    onTogglePause = { paused = !paused },
                    onBack = { exitPlayer() },
                    onLearn = { sheet = true },
                    isSaved = saved.contains(f.id),
                    onToggleSave = {
                        val was = saved.contains(f.id)
                        toggleSave(f.id)
                        toast = if (was) "Removed from Saved" else "Saved to your collection"
                    },
                )
            }
        } else {
            Box(Modifier.fillMaxSize().nestedScroll(barScrollConnection)) {
                when (tab) {
                    "systems" -> {
                        val sys = openSys
                        if (sys != null) {
                            SystemExplore(sys = sys, viewed = viewed, onOpenFact = { openFact(it, Analytics.Source.SYSTEM) }, onBack = { openSys = null })
                        } else {
                            SystemsList(
                                viewed = viewed,
                                onOpenSystem = { sys -> Analytics.systemOpen(sys); withAd { openSys = sys } },
                                onOpenFact = { openFact(it, Analytics.Source.TODAY) },
                                onOpenSearch = { searching = true },
                                onOpenQuiz = { quizOpen = true },
                            )
                        }
                    }
                    "spotlight" -> SpotlightScreen(viewed = viewed, onOpen = { openFact(it, Analytics.Source.SPOTLIGHT) })
                    "compare" -> ComparisonScreen(externalPaused = whatsNew || sourceUrl != null || adShowing)
                    "saved" -> SavedScreen(saved = saved, viewed = viewed, onOpen = { openFact(it, Analytics.Source.SAVED) })
                    "you" -> ProfileScreen(
                        savedCount = saved.size,
                        viewed = viewed.size,
                        onClearSaved = { saved = emptySet(); toast = "Saved collection cleared" },
                    )
                }
            }
        }

        if (quizOpen && factId == null) {
            QuizScreen(
                // Leaves the quiz rather than stacking on top of it: the round is finished by
                // the time these are reachable.
                onOpenFact = { quizOpen = false; openFact(it, Analytics.Source.QUIZ) },
                onClose = { quizOpen = false },
                // Done, from the score screen, is the best natural break the app has — the
                // round is over and the user is leaving anyway. The back arrow stays free.
                onFinish = { quizOpen = false; withAd { } },
            )
        }

        if (searching && factId == null) {
            SearchScreen(
                viewed = viewed,
                onOpen = { searching = false; openFact(it, Analytics.Source.SEARCH) },
                onClose = { searching = false },
            )
        }

        WhatsNewSheet(
            open = whatsNew,
            onClose = { whatsNew = false },
            onOpenSystem = { sys ->
                whatsNew = false
                tab = "systems"
                openSys = sys
            },
        )

        DetailSheet(
            fact = curFact,
            open = sheet,
            onClose = { sheet = false },
            onJump = { sheet = false; openFact(it, Analytics.Source.SYSTEM) },
            isSaved = saved.contains(curFact.id),
            onToggleSave = { toggleSave(curFact.id) },
            onOpenSource = { sourceUrl = it },
            onShare = { Analytics.factShare(curFact); sharing = curFact.id },
        )

        sharing?.let { id ->
            factById(id)?.let { f ->
                ShareCardCapture(fact = f, onDone = { sharing = null })
            }
        }

        sourceUrl?.let { url ->
            SourceWebScreen(url = url, accent = curFact.accent, onClose = { sourceUrl = null })
        }

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

        // The what's-new sheet is modal: without this the bar draws over it and stays
        // tappable behind the scrim.
        if (factId == null && !whatsNew && !searching && !quizOpen) {
            androidx.compose.foundation.layout.Column(Modifier.align(Alignment.BottomCenter)) {
                // Banner only on the Compare tab. Take the nav-bar inset ourselves
                // when the app bar is hidden (scrolled away).
                if (tab == "compare" && AdManager.adsEnabled) BannerAd(applyNavInset = !barVisible)
                AnimatedVisibility(
                    visible = barVisible,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    BottomNav(
                        tab = tab,
                        onSelect = { tab = it },
                    )
                }
            }
        }
    }
}
