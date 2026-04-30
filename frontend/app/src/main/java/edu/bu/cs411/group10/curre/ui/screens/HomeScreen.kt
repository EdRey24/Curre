package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.components.AlertBanner
import edu.bu.cs411.group10.curre.ui.components.BottomNavTab
import edu.bu.cs411.group10.curre.ui.components.CurreBottomBar
import edu.bu.cs411.group10.curre.ui.components.RecentRunItem
import edu.bu.cs411.group10.curre.ui.components.StatCard
import edu.bu.cs411.group10.curre.ui.model.PastRun
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreStreakCard
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

@Composable
fun HomeScreen(
    emergencyContactsCount: Int,
    weeklyMiles: Double,
    streakDays: Int,
    pastRuns: List<PastRun>,
    onStartRun: () -> Unit,
    onSafetyClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRecentRunClick: (PastRun) -> Unit,
) {
    // Scaffold gives us a main app layout with a fixed bottom bar.
    Scaffold(
        containerColor = CurreBackground,
        bottomBar = {
            CurreBottomBar(
                selectedTab = BottomNavTab.HOME,
                onHomeClick = { },
                onSafetyClick = onSafetyClick,
                onStartRunClick = onStartRun,
                onRunsClick = onRunsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        // LazyColumn makes the home screen vertically scrollable.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CurreBackground)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // App title
            item {
                Text(
                    text = "Curre!",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 45.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Emergency status card
            item {
                AlertBanner(
                    contactsCount = emergencyContactsCount
                )
            }

            // Weekly stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "THIS WEEK",
                        value = weeklyMiles.toString(),
                        subtitle = "Miles",
                        backgroundColor = CurreOrange,
                        contentColor = Color.White
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "STREAK",
                        value = streakDays.toString(),
                        subtitle = "Days",
                        backgroundColor = CurreStreakCard,
                        contentColor = CurreNavy
                    )
                }
            }

            // Recent runs section
            item {
                RecentRunsCard(
                    runs = pastRuns,
                    onRecentRunClick = onRecentRunClick
                )
            }
        }
    }
}

@Composable
private fun RecentRunsCard(
    runs: List<PastRun>,
    onRecentRunClick: (PastRun) -> Unit
) {
    // Outer card that groups the recent run items together.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CurreSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Recent runs",
                    tint = CurreOrange
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Recent Runs",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                runs.take(3).forEach { run ->
                    RecentRunItem(
                        run = run,
                        onClick = { onRecentRunClick(run) }
                    )
                }
            }
        }
    }
}