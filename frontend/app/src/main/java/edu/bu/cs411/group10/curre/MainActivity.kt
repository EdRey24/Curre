package edu.bu.cs411.group10.curre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import edu.bu.cs411.group10.curre.model.PastRun
import edu.bu.cs411.group10.curre.model.RunSummary
import edu.bu.cs411.group10.curre.ui.screens.ActiveRunScreen
import edu.bu.cs411.group10.curre.ui.screens.EndRunScreen
import edu.bu.cs411.group10.curre.ui.screens.HomeScreen
import edu.bu.cs411.group10.curre.ui.theme.CurreTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CurreTheme {
                CurreApp()
            }
        }
    }
}

// Top-level screen state for the current prototype flow.
private sealed class AppScreen {
    data object Home : AppScreen()
    data object ActiveRun : AppScreen()
    data class EndRun(val summary: RunSummary) : AppScreen()
}

@Composable
fun CurreApp() {
    // Tracks which screen is currently visible.
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    // Stores the time when the current "running segment" began.
    // This resets when the user resumes after pausing.
    var runSegmentStartTimeMillis by remember { mutableLongStateOf(0L) }

    // Stores total elapsed time accumulated before the current running segment.
    // Example:
    // - User runs 10 sec -> pause
    // - accumulatedElapsedMillis becomes 10000
    // - on resume, timer continues from that value
    var accumulatedElapsedMillis by remember { mutableLongStateOf(0L) }

    // Whether the run is currently paused.
    var isPaused by remember { mutableStateOf(false) }

    // Placeholder recent run data for the home screen.
    val sampleRuns = listOf(
        PastRun(1, 5.2, 20, "2/10/26"),
        PastRun(2, 3.8, 10, "2/9/26"),
        PastRun(3, 7.1, 30, "2/8/26")
    )

    when (val screen = currentScreen) {
        is AppScreen.Home -> {
            HomeScreen(
                emergencyContactsCount = 1,
                weeklyMiles = 12.5,
                streakDays = 20,
                pastRuns = sampleRuns,
                onStartRun = {
                    // Starting a brand-new run resets timing state.
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                },
                onSafetyClick = {
                    // TODO: Add safety screen navigation later.
                },
                onRunsClick = {
                    // TODO: Add runs screen navigation later.
                },
                onProfileClick = {
                    // TODO: Add profile screen navigation later.
                },
                onRecentRunClick = {
                    // TODO: Add run detail screen later.
                }
            )
        }

        is AppScreen.ActiveRun -> {
            // Updates once per second while the run screen is open.
            // If paused, the displayed elapsed time stays frozen.
            val elapsedMillis by produceState(
                initialValue = accumulatedElapsedMillis,
                key1 = runSegmentStartTimeMillis,
                key2 = accumulatedElapsedMillis,
                key3 = isPaused
            ) {
                while (true) {
                    value = if (isPaused) {
                        accumulatedElapsedMillis
                    } else {
                        accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
                    }
                    delay(1000)
                }
            }

            ActiveRunScreen(
                elapsedTime = formatElapsedTime(elapsedMillis),
                distanceMiles = 1.0,
                calories = 30,
                avgPace = 11.9,
                isPaused = isPaused,
                onPauseResumeClick = {
                    if (isPaused) {
                        // Resume:
                        // Start a new running segment now, while preserving previous elapsed time.
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        isPaused = false
                    } else {
                        // Pause:
                        // Save elapsed time up to this moment, then freeze.
                        accumulatedElapsedMillis +=
                            (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                    }
                },
                onStopClick = {
                    // Calculate the final elapsed time correctly depending on pause state.
                    val finalElapsedMillis = if (isPaused) {
                        accumulatedElapsedMillis
                    } else {
                        accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
                    }

                    currentScreen = AppScreen.EndRun(
                        RunSummary(
                            miles = 0.05,
                            durationText = formatSummaryDuration(finalElapsedMillis),
                            avgPaceText = "11.90",
                            calories = 3
                        )
                    )
                }
            )
        }

        is AppScreen.EndRun -> {
            EndRunScreen(
                summary = screen.summary,
                onDoneClick = {
                    // Return home after acknowledging the summary.
                    currentScreen = AppScreen.Home
                }
            )
        }
    }
}

/**
 * Converts milliseconds into a timer string like 00:12:02.
 */
private fun formatElapsedTime(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Converts milliseconds into a shorter summary string like 12m 02s.
 */
private fun formatSummaryDuration(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format("%dm %02ds", minutes, seconds)
}