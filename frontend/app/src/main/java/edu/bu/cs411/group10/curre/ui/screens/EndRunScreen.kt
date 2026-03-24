package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.model.RunSummary
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreBorder
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreSafetyText
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

@Composable
fun EndRunScreen(
    summary: RunSummary,
    onDoneClick: () -> Unit
) {
    // Full-screen page for the run completion summary.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CurreBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Main success heading.
            Text(
                text = "Great Job!",
                color = CurreNavy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Summary card in the middle of the screen.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CurreSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CurreBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Run Summary",
                        color = CurreNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // First row of metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryMetric(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "Miles",
                                    tint = CurreOrange
                                )
                            },
                            value = String.format("%.2f", summary.miles),
                            label = "Miles"
                        )

                        SummaryMetric(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = "Duration",
                                    tint = CurreSafetyText
                                )
                            },
                            value = summary.durationText,
                            label = "Duration"
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Second row of metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryMetric(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Bolt,
                                    contentDescription = "Average pace",
                                    tint = CurreOrange
                                )
                            },
                            value = summary.avgPaceText,
                            label = "Avg Pace (min/mi)"
                        )

                        SummaryMetric(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.LocalFireDepartment,
                                    contentDescription = "Calories",
                                    tint = CurreSafetyText
                                )
                            },
                            value = summary.calories.toString(),
                            label = "Calories"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom confirmation button that returns the user to home.
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CurreLime,
                    contentColor = CurreNavy
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    // Reusable metric block used for the 4 summary items.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = value,
            color = CurreNavy,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = CurreTextMuted,
            fontSize = 14.sp
        )
    }
}