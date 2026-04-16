package edu.bu.cs411.group10.curre

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import edu.bu.cs411.group10.curre.auth.AuthPrefs
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import edu.bu.cs411.group10.curre.ui.model.UserAccount
import edu.bu.cs411.group10.curre.ui.screens.LoginScreen
import edu.bu.cs411.group10.curre.ui.screens.SignUpScreen
import edu.bu.cs411.group10.curre.ui.model.PastRun
import edu.bu.cs411.group10.curre.ui.model.RunSummary
import edu.bu.cs411.group10.curre.ui.screens.ActiveRunScreen
import edu.bu.cs411.group10.curre.ui.screens.EndRunScreen
import edu.bu.cs411.group10.curre.ui.screens.HomeScreen
import edu.bu.cs411.group10.curre.ui.theme.CurreTheme
import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import edu.bu.cs411.group10.curre.ui.screens.SafetyMode
import edu.bu.cs411.group10.curre.ui.screens.SafetyScreen
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import edu.bu.cs411.group10.curre.network.AuthRequest
import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.model.RunDto
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import edu.bu.cs411.group10.curre.network.StartSafetyRequest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*


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
    data object Login : AppScreen()
    data object SignUp : AppScreen()
    data object Home : AppScreen()
    data object Safety : AppScreen()
    data object ActiveRun : AppScreen()
    data class EndRun(val summary: RunSummary) : AppScreen()
}

@Composable
fun CurreApp() {
    val context = LocalContext.current
    val initialScreen = if (AuthPrefs.isLoggedIn(context)) AppScreen.Home else AppScreen.SignUp
    var currentScreen by remember { mutableStateOf<AppScreen>(initialScreen) }

    var registeredUsers by remember {
        mutableStateOf(
            listOf(
                UserAccount(
                    username = "demo",
                    password = "Password1"
                )
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()

    var runSegmentStartTimeMillis by remember { mutableLongStateOf(0L) }

    var accumulatedElapsedMillis by remember { mutableLongStateOf(0L) }

    var isPaused by remember { mutableStateOf(false) }

    var pastRuns by remember { mutableStateOf<List<PastRun>>(emptyList()) }

    var pausedByEndDialog by remember { mutableStateOf(false) }

    var safetyMode by remember { mutableStateOf(SafetyMode.MODE_A) }

    var currentRunId by remember { mutableStateOf<Long?>(null) }

    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }
    var showNoContactsError by remember { mutableStateOf(false) }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            is AppScreen.Home, is AppScreen.Safety, is AppScreen.ActiveRun -> {
                if (emergencyContacts.isEmpty() && !isLoadingContacts) {
                    isLoadingContacts = true
                    try {
                        val response = RetrofitClient.contactApi.getContacts()
                        if (response.isSuccessful) {
                            val dtos = response.body() ?: emptyList()
                            emergencyContacts = dtos.map { dto ->
                                EmergencyContact(
                                    id = dto.id ?: 0L,
                                    name = dto.name,
                                    email = dto.email,
                                    phone = dto.phone
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Handle error silently; validation will still block if empty
                    } finally {
                        isLoadingContacts = false
                    }
                }
            }

            else -> { /* not authenticated yet */
            }
        }
        if (currentScreen is AppScreen.Home) {
            try {
                val response = RetrofitClient.api.getRuns()
                if (response.isSuccessful) {
                    val fetchedDtos = response.body() ?: emptyList()
                    pastRuns = fetchedDtos.map { dto ->
                        PastRun(
                            dto.id?.toInt() ?: 0,
                            dto.avgPaceSecsPerMile / 60,
                            dto.durationSeconds / 60,
                            SimpleDateFormat(
                                "M/d/yy",
                                Locale.getDefault()
                            ).format(Date(dto.startedAt))
                        )
                    }.reversed()
                }
            } catch (e: Exception) {
                println("NETWORK ERROR FETCHING RUNS: ${e.message}")
            }
        }
    }

    fun attemptStartRun() {
        if (emergencyContacts.isEmpty()) {
            showNoContactsError = true
        } else {
            accumulatedElapsedMillis = 0L
            runSegmentStartTimeMillis = System.currentTimeMillis()
            isPaused = false
            currentRunId = null
            currentScreen = AppScreen.ActiveRun
        }
    }

    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            LoginScreen(
                onLogin = { email, password, onResult ->
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.authApi.login(
                                AuthRequest(email, password)
                            )
                            if (response.isSuccessful && response.body()?.userId != null) {
                                val body = response.body()!!
                                AuthPrefs.saveLogin(context, body.userId!!, body.email ?: email)
                                onResult(true, body.message)
                            } else {
                                val errorMsg = response.body()?.message ?: "Login failed"
                                onResult(false, errorMsg)
                            }
                        } catch (e: Exception) {
                            onResult(false, "Network error: ${e.message}")
                        }
                    }
                },
                onGoToSignUp = {
                    currentScreen = AppScreen.SignUp
                },
                onLoginSuccess = {
                    currentScreen = AppScreen.Home
                }
            )
        }

        is AppScreen.SignUp -> {
            SignUpScreen(
                onCreateAccount = { email, password, confirmPassword, onResult ->
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.authApi.register(
                                AuthRequest(email, password, confirmPassword)
                            )
                            if (response.isSuccessful && response.body()?.userId != null) {
                                val body = response.body()!!
                                AuthPrefs.saveLogin(context, body.userId!!, body.email ?: email)
                                onResult(true, body.message)
                            } else {
                                val errorMsg = response.body()?.message ?: "Registration failed"
                                onResult(false, errorMsg)
                            }
                        } catch (e: Exception) {
                            onResult(false, "Network error: ${e.message}")
                        }
                    }
                },
                onGoToLogin = {
                    currentScreen = AppScreen.Login
                },
                onSignUpSuccess = {
                    currentScreen = AppScreen.Home
                }
            )
        }

        is AppScreen.Home -> {
            HomeScreen(
                emergencyContactsCount = emergencyContacts.size,
                weeklyMiles = 12.5,
                streakDays = 20,
                pastRuns = pastRuns,
                onStartRun = { attemptStartRun() },
                onSafetyClick = {
                    currentScreen = AppScreen.Safety
                },
                onRunsClick = {
                    // TODO: Add runs screen navigation later.
                },
                onProfileClick = {
                    // TODO: Add profile screen navigation later.
                },
                onRecentRunClick = {
                    // TODO: Add run detail screen later.
                },
                onSignOut = {
                    AuthPrefs.logout(context)
                    currentScreen = AppScreen.SignUp
                }
            )
        }

        is AppScreen.Safety -> {
            SafetyScreen(
                selectedMode = safetyMode,
                onModeChange = { safetyMode = it },
                onHomeClick = { currentScreen = AppScreen.Home },
                onStartRunClick = { attemptStartRun() },
                onRunsClick = { /* TODO */ },
                onProfileClick = { /* TODO */ },
                onContactsUpdated = { updatedContacts ->
                    emergencyContacts = updatedContacts
                },
                contacts = emergencyContacts
            )
        }


        is AppScreen.ActiveRun -> {
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
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false
                    } else {
                        accumulatedElapsedMillis +=
                            (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                    }
                },
                onStopClick = {
                    val finalElapsedMillis = if (isPaused) {
                        accumulatedElapsedMillis
                    } else {
                        accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
                    }

                    val distance = 0.15
                    val durationSecs = (finalElapsedMillis / 1000).toInt()
                    val paceSecsPerMile = if (distance > 0) durationSecs / distance else 0.0
                    val paceMinutes = (paceSecsPerMile / 60).toInt()
                    val paceSeconds = (paceSecsPerMile % 60).toInt()
                    val formattedPace = String.format("%d:%02d", paceMinutes, paceSeconds)
                    val estimatedCalories = (distance * 100).toInt()
                    val runToSave = RunDto(
                        startedAt = System.currentTimeMillis() - finalElapsedMillis,
                        endedAt = System.currentTimeMillis(),
                        distanceMiles = distance,
                        durationSeconds = durationSecs,
                        avgPaceSecsPerMile = if (distance > 0) durationSecs / distance else 0.0,
                        calories = estimatedCalories
                    )

                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.api.saveRun(runToSave)
                            if (response.isSuccessful) {
                                val savedRun = response.body()
                                savedRun?.id?.let { runId ->
                                    currentRunId = runId
                                    if (safetyMode == SafetyMode.MODE_A || safetyMode == SafetyMode.MODE_B) {
                                        try {
                                            val interval = if (safetyMode == SafetyMode.MODE_B) 900 else 0
                                            RetrofitClient.safetyApi.startSafety(
                                                StartSafetyRequest(runId, interval)
                                            )
                                        } catch (e: Exception) {
                                            println("Failed to start safety monitoring: ${e.message}")
                                        }
                                    }
                                }
                                println("SUCCESS! Saved run to backend with ID: ${response.body()?.id}")
                            } else {
                                println("SERVER ERROR: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            println("NETWORK ERROR: ${e.message}")
                        }
                    }

                    pausedByEndDialog = false

                    currentScreen = AppScreen.EndRun(
                        RunSummary(
                            miles = distance,
                            durationText = formatSummaryDuration(finalElapsedMillis),
                            avgPaceText = formattedPace,
                            calories = estimatedCalories
                        )
                    )
                },
                onPauseForEndDialog = {
                    if (!isPaused) {
                        accumulatedElapsedMillis +=
                            (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                        pausedByEndDialog = true
                    } else {
                        pausedByEndDialog = false
                    }
                },
                onResumeAfterEndDialogDismiss = {
                    if (pausedByEndDialog) {
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false
                    }
                }
            )
        }

        is AppScreen.EndRun -> {
            EndRunScreen(
                summary = screen.summary,
                onDoneClick = {
                    currentRunId?.let { runId ->
                        coroutineScope.launch {
                            try {
                                RetrofitClient.safetyApi.stopSafety(runId)
                            } catch (e: Exception) {
                                println("Failed to stop safety monitoring: ${e.message}")
                            }
                        }
                    }
                    currentScreen = AppScreen.Home
                }
            )
        }
    }

    // No contacts alert dialog
    if (showNoContactsError) {
        AlertDialog(
            onDismissRequest = { showNoContactsError = false },
            containerColor = CurreSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Cannot Start Run",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "You have safety features enabled, but no emergency contacts are added. Please add at least one contact before starting a run.",
                    color = CurreTextMuted,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoContactsError = false
                        currentScreen = AppScreen.Safety   // Take user to Safety screen to add contacts
                    }
                ) {
                    Text("Go to Safety", color = CurreLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContactsError = false }) {
                    Text("Cancel", color = CurreNavy)
                }
            }
        )
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