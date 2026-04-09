package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.model.PastRun
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreIconMuted
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreOrangeSoft
import edu.bu.cs411.group10.curre.ui.theme.CurreStreakCard
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreSurfaceSoft
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
    onRecentRunClick: (PastRun) -> Unit
) {
    // Scaffold gives us a main app layout with a fixed bottom bar.
    Scaffold(
        containerColor = CurreBackground,
        bottomBar = {
            HomeBottomBar(
                onStartRun = onStartRun,
                onSafetyClick = onSafetyClick,
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
            contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App title
            item {
                Text(
                    text = "Curre!",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp
                )
            }

            // Emergency status card
            item {
                EmergencyBanner(
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
private fun EmergencyBanner(
    contactsCount: Int
) {
    // Soft orange card showing emergency safety readiness.
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurreOrangeSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = "Emergency alert",
                tint = CurreOrange
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Emergency Alert Ready",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay safe on every adventure!",
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            // Small circular badge showing number of emergency contacts.
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CurreOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contactsCount.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    contentColor: Color
) {
    // Reusable card for the "THIS WEEK" and "STREAK" boxes.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = value,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                color = contentColor.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
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

                Spacer(modifier = Modifier.width(8.dp))

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

@Composable
private fun RecentRunItem(
    run: PastRun,
    onClick: () -> Unit
) {
    // Each individual run row inside the recent runs card.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = CurreSurfaceSoft,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Orange circular pace badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CurreOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.1f", run.pace),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Duration and date
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${run.durationMinutes} Minutes",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = run.date,
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            // Arrow icon on the right
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open run",
                tint = CurreIconMuted
            )
        }
    }
}

@Composable
private fun HomeBottomBar(
    onStartRun: () -> Unit,
    onSafetyClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Bottom nav bar with a center floating run button.
    Box {
        NavigationBar(
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = true,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CurreLime,
                    selectedTextColor = CurreLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = CurreTextMuted,
                    unselectedTextColor = CurreTextMuted
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("Home") }
            )

            NavigationBarItem(
                selected = false,
                onClick = onSafetyClick,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CurreLime,
                    selectedTextColor = CurreLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = CurreTextMuted,
                    unselectedTextColor = CurreTextMuted
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Safety"
                    )
                },
                label = { Text("Safety") }
            )

            // Empty center slot so the floating button can sit above it.
            NavigationBarItem(
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CurreLime,
                    selectedTextColor = CurreLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = CurreTextMuted,
                    unselectedTextColor = CurreTextMuted
                ),
                icon = { Spacer(modifier = Modifier.size(24.dp)) },
                label = { Text("") }
            )

            NavigationBarItem(
                selected = false,
                onClick = onRunsClick,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CurreLime,
                    selectedTextColor = CurreLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = CurreTextMuted,
                    unselectedTextColor = CurreTextMuted
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.List,
                        contentDescription = "Runs"
                    )
                },
                label = { Text("Runs") }
            )

            NavigationBarItem(
                selected = false,
                onClick = onProfileClick,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CurreLime,
                    selectedTextColor = CurreLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = CurreTextMuted,
                    unselectedTextColor = CurreTextMuted
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("Profile") }
            )
        }

        // Main action button for starting a run.
        FloatingActionButton(
            onClick = onStartRun,
            containerColor = CurreLime,
            contentColor = CurreNavy,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(74.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = "Start Run"
            )
        }
    }
}