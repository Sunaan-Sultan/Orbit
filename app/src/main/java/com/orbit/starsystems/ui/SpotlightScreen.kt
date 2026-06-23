package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.SPOTLIGHT_FACTS

/** A standalone tab of singular facts — spacecraft, our Sun, and landmarks — outside any one system. */
@Composable
fun SpotlightScreen(onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).padding(bottom = 80.dp)) {
        Column(
            Modifier.statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 8.dp),
        ) {
            Text("SPOTLIGHT", style = ts(12f, FontWeight.Bold, Mute, 0.28f))
            Text("Beyond the systems", style = ts(32f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 6.dp))
            Text(
                "Singular wonders — spacecraft, our own star, and the landmarks of space.",
                style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(SPOTLIGHT_FACTS.size) { i ->
                FactCard(SPOTLIGHT_FACTS[i], onClick = { onOpen(SPOTLIGHT_FACTS[i].id) })
            }
        }
    }
}
