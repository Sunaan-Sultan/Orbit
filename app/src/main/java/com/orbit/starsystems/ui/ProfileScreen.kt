package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(savedCount: Int, viewed: Int) {
    Column(
        Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(bottom = 80.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Sphere(sizeUnits = 84f, colors = listOf(Color(0xFF9CC4EC), Color(0xFF3D72B8), Color(0xFF1A3360)), glow = Color(0xFF508CD2).copy(alpha = 0.4f))
            Text("Stargazer", style = ts(22f, FontWeight.Bold, Color.White), modifier = Modifier.padding(top = 16.dp))
            Text("Exploring since today", style = ts(13.5f, color = Dim, spacingEm = 0.02f), modifier = Modifier.padding(top = 4.dp))
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
            ProfileStat("2", "Systems")
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
                Text("3-day streak", style = ts(19f, FontWeight.Bold, Color.White))
                Text("Come back tomorrow for a new fact.", style = ts(13.5f, color = Color(0xFFC9A98A)), modifier = Modifier.padding(top = 1.dp))
            }
        }
        Text(
            "A daily window onto the cosmos.",
            style = ts(13f, color = Color(0xFF5A5A5A)),
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
        )
    }
}

@Composable
private fun RowScope.ProfileStat(n: String, label: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n, style = ts(30f, FontWeight.Bold, Color.White))
        Text(label.uppercase(), style = ts(11f, FontWeight.SemiBold, Dim, 0.1f), modifier = Modifier.padding(top = 4.dp))
    }
}
