package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreDarkBanner
import edu.bu.cs411.group10.curre.ui.theme.CurreGpsDot
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreLimeSoft
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreSafetyText
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

@Composable
fun ActiveRunScreen(
    elapsedTime: String,
    distanceMiles: Double,
    calories: Int,
    avgPace: Double,
    isPaused: Boolean,
    onPauseResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onPauseForEndDialog: () -> Unit,
    onResumeAfterEndDialogDismiss: () -> Unit
) {
    // Controls whether the "End Run" confirmation dialog is visible.
    var showEndRunDialog by remember { mutableStateOf(false) }

    // Full-screen surface for the active run page.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CurreBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Screen title
            Text(
                text = "Active Run",
                color = CurreNavy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main run timer
            Text(
                text = elapsedTime,
                color = CurreOrange,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // GPS + safety status row
            RunStatusBanner(isPaused = isPaused)

            Spacer(modifier = Modifier.height(30.dp))

            // Main run stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActiveStat(
                    value = String.format("%.1f", distanceMiles),
                    unit = "MI",
                    label = "Distance"
                )
                ActiveStat(
                    value = calories.toString(),
                    unit = "CAL",
                    label = "Calories"
                )
                ActiveStat(
                    value = String.format("%.1f", avgPace),
                    unit = "/MI",
                    label = "Avg Pace"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Dark banner that confirms safety notifications were sent.
            ContactsBanner()

            Spacer(modifier = Modifier.weight(1f))

            // Bottom action row: pause/resume and stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPauseResumeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    shape = RoundedCornerShape(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = CurreNavy
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                IconButton(
                    onClick = {
                        onPauseForEndDialog()
                        showEndRunDialog = true },
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(CurreLime)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = "Stop run",
                        tint = CurreNavy,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // End run confirmation dialog
    if (showEndRunDialog) {
        AlertDialog(
            onDismissRequest = {
                showEndRunDialog = false
                onResumeAfterEndDialogDismiss()
            },
            containerColor = CurreSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "End Run?",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end this run and view your summary?",
                    color = CurreTextMuted,
                    fontSize = 15.sp
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showEndRunDialog = false
                        onResumeAfterEndDialogDismiss()
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = CurreNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndRunDialog = false
                        onStopClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CurreLime,
                        contentColor = CurreNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "End Run",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun RunStatusBanner(
    isPaused: Boolean
) {
    // Status row below the timer showing GPS and safety mode state.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side status changes depending on whether the run is paused.
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) CurreTextMuted else CurreGpsDot)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isPaused) "Run paused" else "GPS updating 3s ago",
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            // Right side: active safety mode badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CurreLimeSoft)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Safety mode",
                        tint = CurreSafetyText,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Safety Mode A: ON",
                        color = CurreSafetyText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveStat(
    value: String,
    unit: String,
    label: String
) {
    // Reusable stat column for distance, calories, and pace.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = CurreNavy,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = unit,
            color = CurreTextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = CurreTextMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ContactsBanner() {
    // Banner confirming that emergency contacts were notified when the run started.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CurreDarkBanner)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Contacts notified: Run started",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}