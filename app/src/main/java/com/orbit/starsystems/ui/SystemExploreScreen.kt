package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.SYS_META
import com.orbit.starsystems.core.categoriesForSys
import com.orbit.starsystems.core.factsForSys

@Composable
fun SystemExplore(sys: String, onOpenFact: (String) -> Unit, onBack: () -> Unit) {
    val facts = factsForSys(sys)
    val meta = SYS_META[sys] ?: return
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.78f))
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Ico("back", size = 20.dp, color = Color.White, sw = 2.1f) }
            Spacer(Modifier.width(12.dp))
            Text(meta.label, style = ts(18f, FontWeight.SemiBold, Color.White))
        }
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 6.dp)) {
            Text(meta.eyebrow.uppercase(), style = ts(12f, FontWeight.Bold, meta.eyebrowColor, 0.28f))
            Text(meta.title, style = ts(32f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 6.dp))
            Text(meta.blurb, style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f), modifier = Modifier.padding(top = 12.dp))
        }
        categoriesForSys(sys).forEach { cat ->
            val inCat = facts.filter { it.cat == cat }
            val accent = inCat.firstOrNull()?.accent ?: Color.White
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 40.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(3.5.dp, 22.dp).clip(RoundedCornerShape(100)).background(accent))
                    Spacer(Modifier.width(12.dp))
                    Text(cat, style = ts(26f, FontWeight.Bold, Color.White, -0.01f))
                }
                Text("${inCat.size} ${if (inCat.size == 1) "fact" else "facts"}".uppercase(), style = ts(11f, FontWeight.SemiBold, Color(0xFF6A6A6A), 0.14f))
            }
            Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                inCat.forEach { f ->
                    FactListCard(fact = f, onClick = { onOpenFact(f.id) })
                }
            }
        }
    }
}
