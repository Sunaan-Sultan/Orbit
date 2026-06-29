package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.ALL_FACTS

@Composable
fun SavedScreen(saved: Set<String>, onOpen: (String) -> Unit) {
    val items = ALL_FACTS.filter { saved.contains(it.id) }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            Modifier.statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 16.dp),
        ) {
            Text("YOUR COLLECTION", style = ts(12f, FontWeight.Bold, Dim, 0.24f))
            Text("Saved", style = ts(32f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 6.dp))
        }
        if (items.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Ico("saved", size = 28.dp, color = Color(0xFF6A6A6A))
                }
                Spacer4()
                Text("Nothing saved yet", style = ts(17f, FontWeight.SemiBold, Color(0xFFCFCFCF)))
                Text("Tap the bookmark on any fact to keep it here.", style = ts(14f, color = Dim, lineHeight = 21f), modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 102.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(items.size) { i -> FactCard(items[i], onClick = { onOpen(items[i].id) }) }
            }
        }
    }
}

@Composable
private fun Spacer4() = Box(Modifier.height(18.dp))
