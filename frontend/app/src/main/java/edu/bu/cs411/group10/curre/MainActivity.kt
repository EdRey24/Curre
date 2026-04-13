package edu.bu.cs411.group10.curre

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import edu.bu.cs411.group10.curre.ui.model.RoutePointDto
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
import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.model.RunDto
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.concurrent.timer

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
    // Tracks which screen is currently visible.
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.SignUp) }

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
    var pastRuns by remember { mutableStateOf<List<PastRun>>(emptyList()) }

    var pausedByEndDialog by remember { mutableStateOf(false) }

    var safetyMode by remember { mutableStateOf(SafetyMode.MODE_A) }

    var emergencyContacts by remember {
        mutableStateOf(
            listOf(
                EmergencyContact(
                    id = 1,
                    name = "Jane Doe",
                    email = "jane@example.com"
                )
            )
        )
    }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)
        if (fineLocationGranted){
            println("GPS Permission Granted!")
        } else {
            println("GPS Permission Denied :(")
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var routePoints by remember { mutableStateOf<List<RoutePointDto>>(emptyList()) }
    var dynamicDistanceMiles by remember { mutableStateOf(0.0)}

    LaunchedEffect(currentScreen) {
        if (currentScreen is AppScreen.Home){
            try {
                val response = RetrofitClient.api.getRuns()
                if (response.isSuccessful){
                    val fetchedDtos = response.body() ?: emptyList()
                    pastRuns = fetchedDtos.map { dto ->
                        PastRun(
                            dto.id?.toInt() ?: 0,
                            dto.avgPaceSecsPerMile / 60,
                            dto.durationSeconds / 60,
                            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(dto.startedAt))
                        )
                    }.reversed()
                }
            } catch (e: Exception){
                println("NETWORK ERROR FETCHING RUNS: ${e.message}")
            }
        }
    }

    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            LoginScreen(
                onLogin = { username, password ->
                    registeredUsers.any {
                        it.username.equals(username, ignoreCase = true) &&
                                it.password == password
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
                isUsernameTaken = { username ->
                    registeredUsers.any { it.username.equals(username, ignoreCase = true) }
                },
                onCreateAccount = { username, password ->
                    registeredUsers = registeredUsers + UserAccount(
                        username = username,
                        password = password
                    )
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
                onStartRun = {
                    // Starting a brand-new run resets timing state.
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                    routePoints = emptyList()
                    dynamicDistanceMiles = 0.0
                },
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
                    currentScreen = AppScreen.SignUp
                }
            )
        }

        is AppScreen.Safety -> {
            SafetyScreen(
                contacts = emergencyContacts,
                selectedMode = safetyMode,
                onModeChange = { safetyMode = it },
                onAddContact = { name, email ->
                    val nextId = (emergencyContacts.maxOfOrNull { it.id } ?: 0) + 1
                    emergencyContacts = emergencyContacts + EmergencyContact(
                        id = nextId,
                        name = name,
                        email = email
                    )
                },
                onUpdateContact = { contactId, name, email ->
                    emergencyContacts = emergencyContacts.map { contact ->
                        if (contact.id == contactId) {
                            contact.copy(name = name, email = email)
                        } else {
                            contact
                        }
                    }
                },
                onDeleteContact = { contactId ->
                    emergencyContacts = emergencyContacts.filterNot { it.id == contactId }
                },
                onHomeClick = {
                    currentScreen = AppScreen.Home
                },
                onStartRunClick = {
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                },
                onRunsClick = {
                    // TODO
                },
                onProfileClick = {
                    // TODO
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

            DisposableEffect(Unit) {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()
                val locationCallback = object : LocationCallback(){
                    override fun onLocationResult(locationResult: LocationResult){
                        for (location in locationResult.locations){
                            val newPoint = RoutePointDto(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestampMillis = System.currentTimeMillis()
                            )
                            val updatedPoints = routePoints + newPoint
                            routePoints = updatedPoints

                            if (updatedPoints.size >= 2){
                                val lastPoint = updatedPoints[updatedPoints.size - 2]
                                val results = FloatArray(1)
                                Location.distanceBetween(
                                    lastPoint.latitude, lastPoint.longitude,
                                    newPoint.latitude, newPoint.longitude,
                                    results
                                )
                                // Convert meters to miles and update state
                                dynamicDistanceMiles += (results[0] / 1609.34)
                            }
                        }
                    }
                }
                @SuppressLint("MissingPermission")
                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                } catch (e: SecurityException){
                    println("GPS Permission denied!")
                }

                onDispose {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }

            val liveDurationSecs = (elapsedMillis / 1000).toInt()
            val liveCalories = (dynamicDistanceMiles * 100).toInt()

            val livePace = if (dynamicDistanceMiles > 0){
                (liveDurationSecs / 60.0) / dynamicDistanceMiles
            } else {
                0.0
            }

            ActiveRunScreen(
                elapsedTime = formatElapsedTime(elapsedMillis),
                distanceMiles = dynamicDistanceMiles,
                calories = liveCalories,
                avgPace = livePace,
                isPaused = isPaused,
                onPauseResumeClick = {
                    if (isPaused) {
                        // Resume:
                        // Start a new running segment now, while preserving previous elapsed time.
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false
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

                    val distance = dynamicDistanceMiles
                    val durationSecs = (finalElapsedMillis / 1000).toInt()
                    val paceSecsPerMile = if (distance > 0) durationSecs / distance else 0.0
                    val paceMinutes = (paceSecsPerMile / 60).toInt()
                    val paceSeconds = (paceSecsPerMile % 60).toInt()
                    val formattedPace = String.format("%d:%02d", paceMinutes, paceSeconds)
                    val estimatedCalories = (distance * 100).toInt()
                    val runtoSave = RunDto(
                        startedAt = System.currentTimeMillis() - finalElapsedMillis,
                        endedAt = System.currentTimeMillis(),
                        distanceMiles = distance,
                        durationSeconds = durationSecs,
                        avgPaceSecsPerMile = if (distance > 0) durationSecs / distance else 0.0,
                        calories = estimatedCalories,
                        routePoints = routePoints
                    )
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.api.saveRun(runtoSave)
                            if (response.isSuccessful) println("SUCCESS! Saved run to backend with ID: ${response.body()?.id}")
                            else println("SERVER ERROR: ${response.errorBody()?.string()}")
                        } catch (e: Exception){
                            println("NETWORK ERROR: ${e.message}")
                        }
                    }

                    pausedByEndDialog = false

                    currentScreen = AppScreen.EndRun(
                        RunSummary(
                            miles = distance,
                            durationText = formatSummaryDuration(finalElapsedMillis),
                            avgPaceText = formattedPace,
                            calories = estimatedCalories,
                            routePoints = routePoints
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