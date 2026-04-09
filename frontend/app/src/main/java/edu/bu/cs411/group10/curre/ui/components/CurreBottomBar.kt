package edu.bu.cs411.group10.curre.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

enum class BottomNavTab {
    HOME,
    SAFETY,
    RUNS,
    PROFILE
}

@Composable
fun CurreBottomBar(
    selectedTab: BottomNavTab,
    onHomeClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onStartRunClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box {

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    clip = false
                )
        ) {

            NavigationBarItem(
                selected = selectedTab == BottomNavTab.HOME,
                onClick = onHomeClick,
                colors = navItemColors(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("Home") }
            )

            NavigationBarItem(
                selected = selectedTab == BottomNavTab.SAFETY,
                onClick = onSafetyClick,
                colors = navItemColors(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Safety"
                    )
                },
                label = { Text("Safety") }
            )

            // Spacer for FAB
            NavigationBarItem(
                selected = false,
                onClick = { },
                colors = navItemColors(),
                icon = { Spacer(modifier = Modifier.size(24.dp)) },
                label = { Text("") }
            )

            NavigationBarItem(
                selected = selectedTab == BottomNavTab.RUNS,
                onClick = onRunsClick,
                colors = navItemColors(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.List,
                        contentDescription = "Runs"
                    )
                },
                label = { Text("Runs") }
            )

            NavigationBarItem(
                selected = selectedTab == BottomNavTab.PROFILE,
                onClick = onProfileClick,
                colors = navItemColors(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("Profile") }
            )
        }

        // Run Action Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
                .size(96.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(Color.White, CircleShape), // white border
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onStartRunClick,
                containerColor = CurreLime,
                contentColor = CurreNavy,
                shape = CircleShape,
                modifier = Modifier.size(78.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Start Run",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CurreLime,
    selectedTextColor = CurreLime,
    indicatorColor = Color.Transparent,
    unselectedIconColor = CurreTextMuted,
    unselectedTextColor = CurreTextMuted
)