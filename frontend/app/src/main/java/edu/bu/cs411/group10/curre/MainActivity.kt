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
import edu.bu.cs411.group10.curre.ui.screens.RunsScreen
import edu.bu.cs411.group10.curre.ui.screens.RunDetailsScreen
import edu.bu.cs411.group10.curre.ui.screens.ProfileScreen
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.launch
import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.model.RunDto
import edu.bu.cs411.group10.curre.auth.AuthPrefs
import edu.bu.cs411.group10.curre.network.AuthRequest
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.collections.emptyList

const val SAFETY_CHECK_IN_INTERVAL = 30

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RetrofitClient.init(this)

        setContent {
            CurreTheme {
                CurreApp()
            }
        }
    }
}

private enum class RunDetailsSource {
    HOME,
    RUNS
}

// Top-level screen state for the current prototype flow.
private sealed class AppScreen {
    data object Login : AppScreen()
    data object SignUp : AppScreen()
    data object Home : AppScreen()
    data object Safety : AppScreen()
    data object Runs : AppScreen()
    data object Profile : AppScreen()
    data object ActiveRun : AppScreen()
    data class RunDetails(
        val runId: Long,
        val source: RunDetailsSource
    ) : AppScreen()
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
    var fetchedRuns by remember { mutableStateOf<List<RunDto>>(emptyList()) }

    var pausedByEndDialog by remember { mutableStateOf(false) }

    var safetyMode by remember { mutableStateOf(SafetyMode.MODE_A) }

    var checkInIntervalSeconds by remember { mutableIntStateOf(SAFETY_CHECK_IN_INTERVAL) }
    var checkInAccumulatedMillis by remember { mutableLongStateOf(0L) }
    var checkInSegmentStartTime by remember { mutableLongStateOf(0L) }
    var isCheckInOverdue by remember { mutableStateOf(false) }
    var alertsTriggered by remember { mutableIntStateOf(0) }

    var currentRunId by remember { mutableStateOf<Long?>(null) }

    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }
    var showNoContactsError by remember { mutableStateOf(false) }

    var lastGpsUpdateTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

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

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var routeSegments by remember { mutableStateOf<List<List<RoutePointDto>>>(listOf(emptyList())) }
    var dynamicDistanceMiles by remember { mutableStateOf(0.0)}
    var ignoreNextDistance by remember { mutableStateOf(false) }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            is AppScreen.Home, is AppScreen.Safety, is AppScreen.ActiveRun, is AppScreen.Runs,
            is AppScreen.Profile -> {
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
        if (currentScreen is AppScreen.Home ||
            currentScreen is AppScreen.Runs ||
            currentScreen is AppScreen.RunDetails) {
            try {
                val response = RetrofitClient.api.getRuns()
                if (response.isSuccessful) {
                    val fetchedDtos = response.body() ?: emptyList()
                    fetchedRuns = fetchedDtos
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
        if (emergencyContacts.isEmpty() && safetyMode != SafetyMode.NONE) {
            showNoContactsError = true
        } else {
            accumulatedElapsedMillis = 0L
            runSegmentStartTimeMillis = System.currentTimeMillis()
            isPaused = false
            pausedByEndDialog = false

            currentRunId = null
            currentScreen = AppScreen.ActiveRun
            routeSegments = listOf(emptyList())
            dynamicDistanceMiles = 0.0
            ignoreNextDistance = false

            checkInIntervalSeconds =
                if (safetyMode == SafetyMode.MODE_B) SAFETY_CHECK_IN_INTERVAL else 0

            alertsTriggered = 0
            lastGpsUpdateTimeMillis = System.currentTimeMillis()
            checkInAccumulatedMillis = 0L
            checkInSegmentStartTime = System.currentTimeMillis()
            isCheckInOverdue = false

            coroutineScope.launch {
                try {
                    val initialRun = RunDto(
                        startedAt = System.currentTimeMillis(),
                        endedAt = 0L,
                        distanceMiles = 0.0,
                        durationSeconds = 0,
                        avgPaceSecsPerMile = 0.0,
                        calories = 0,
                        routePoints = emptyList()
                    )

                    val response = RetrofitClient.api.saveRun(initialRun)

                    if (response.isSuccessful) {
                        response.body()?.id?.let { runId ->
                            currentRunId = runId

                            if (
                                safetyMode == SafetyMode.MODE_A ||
                                safetyMode == SafetyMode.MODE_B
                            ) {
                                val interval =
                                    if (safetyMode == SafetyMode.MODE_B) {
                                        SAFETY_CHECK_IN_INTERVAL
                                    } else {
                                        900
                                    }

                                @SuppressLint("MissingPermission")
                                fusedLocationClient
                                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location: Location? ->
                                        val payload = mapOf(
                                            "runId" to runId,
                                            "intervalSeconds" to interval,
                                            "lat" to location?.latitude,
                                            "lng" to location?.longitude
                                        )

                                        coroutineScope.launch {
                                            try {
                                                RetrofitClient.safetyApi.startSafety(payload)
                                            } catch (e: Exception) {
                                                println("Failed to start safety: ${e.message}")
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        val payload = mapOf(
                                            "runId" to runId,
                                            "intervalSeconds" to interval,
                                            "lat" to null,
                                            "lng" to null
                                        )

                                        coroutineScope.launch {
                                            try {
                                                RetrofitClient.safetyApi.startSafety(payload)
                                            } catch (e: Exception) {
                                                println("Failed to start safety without GPS: ${e.message}")
                                            }
                                        }
                                    }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to initialize run/safety: ${e.message}")
                }
            }

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
                                AuthPrefs.saveLogin(
                                    context,
                                    body.userId!!,
                                    body.email ?: email,
                                    body.firstName,
                                    body.lastName
                                )
                                onResult(true, body.message)
                            } else {
                                val errorMsg = try {
                                    val errorJson = response.errorBody()?.string()
                                    val parsed = com.google.gson.Gson().fromJson(
                                        errorJson,
                                        edu.bu.cs411.group10.curre.network.AuthResponse::class.java
                                    )
                                    parsed?.message ?: "Login failed"
                                } catch (e: Exception) {
                                    "Login failed"
                                }

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
                onCreateAccount = { firstName, lastName, email, password, confirmPassword, onResult ->
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.authApi.register(
                                AuthRequest(
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    firstName = firstName,
                                    lastName = lastName
                                )
                            )

                            if (response.isSuccessful && response.body()?.userId != null) {
                                val body = response.body()!!
                                AuthPrefs.saveLogin(
                                    context,
                                    body.userId!!,
                                    body.email ?: email,
                                    firstName,
                                    lastName
                                )
                                onResult(true, body.message)
                            } else {
                                val errorMsg = try {
                                    val errorJson = response.errorBody()?.string()
                                    val parsed = com.google.gson.Gson().fromJson(
                                        errorJson,
                                        edu.bu.cs411.group10.curre.network.AuthResponse::class.java
                                    )
                                    parsed?.message ?: "Registration failed"
                                } catch (e: Exception) {
                                    "Registration failed"
                                }

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
            val weeklyMilesValue =
                String.format("%.2f", fetchedRuns.sumOf { it.distanceMiles }).toDouble()

            val streakDaysValue = calculateConsecutiveStreakDays(fetchedRuns)

            HomeScreen(
                emergencyContactsCount = emergencyContacts.size,
                weeklyMiles = weeklyMilesValue,
                streakDays = streakDaysValue,
                pastRuns = pastRuns,
                onStartRun = { attemptStartRun() },
                onSafetyClick = {
                    currentScreen = AppScreen.Safety
                },
                onRunsClick = {
                    currentScreen = AppScreen.Runs
                },
                onProfileClick = {
                    currentScreen = AppScreen.Profile
                },
                onRecentRunClick = { run ->
                    currentScreen = AppScreen.RunDetails(
                        runId = run.id.toLong(),
                        source = RunDetailsSource.HOME
                    )
                }
            )
        }

        is AppScreen.Safety -> {
            SafetyScreen(
                selectedMode = safetyMode,
                onModeChange = { safetyMode = it },
                onHomeClick = { currentScreen = AppScreen.Home },
                onStartRunClick = { attemptStartRun() },
                onRunsClick = { currentScreen = AppScreen.Runs },
                onProfileClick = { currentScreen = AppScreen.Profile },
                onContactsUpdated = { updatedContacts ->
                    emergencyContacts = updatedContacts
                },
                contacts = emergencyContacts
            )
        }

        is AppScreen.Runs -> {
            RunsScreen(
                runs = fetchedRuns.sortedByDescending { it.startedAt },
                onHomeClick = { currentScreen = AppScreen.Home },
                onSafetyClick = { currentScreen = AppScreen.Safety },
                onStartRunClick = { attemptStartRun() },
                onRunsClick = { currentScreen = AppScreen.Runs },
                onProfileClick = { currentScreen = AppScreen.Profile },
                onRunClick = { runId ->
                    currentScreen = AppScreen.RunDetails(
                        runId = runId,
                        source = RunDetailsSource.RUNS
                    )
                }
            )
        }

        is AppScreen.Profile -> {
            ProfileScreen(
                firstName = AuthPrefs.getFirstName(context) ?: "Demo",
                lastName = AuthPrefs.getLastName(context) ?: "User",
                email = AuthPrefs.getEmail(context) ?: "demo@curre.com",
                onHomeClick = { currentScreen = AppScreen.Home },
                onSafetyClick = { currentScreen = AppScreen.Safety },
                onStartRunClick = { attemptStartRun() },
                onRunsClick = { currentScreen = AppScreen.Runs },
                onProfileClick = { currentScreen = AppScreen.Profile },
                onSignOutClick = {
                    AuthPrefs.logout(context)
                    pastRuns = emptyList()
                    fetchedRuns = emptyList()
                    emergencyContacts = emptyList()
                    currentScreen = AppScreen.SignUp
                }
            )
        }

        is AppScreen.RunDetails -> {
            val selectedRun = fetchedRuns.firstOrNull { it.id == screen.runId }

            if (selectedRun != null) {
                RunDetailsScreen(
                    run = selectedRun,
                    onBackClick = {
                        currentScreen = when (screen.source) {
                            RunDetailsSource.HOME -> AppScreen.Home
                            RunDetailsSource.RUNS -> AppScreen.Runs
                        }
                    }
                )
            } else {
                currentScreen = when (screen.source) {
                    RunDetailsSource.HOME -> AppScreen.Home
                    RunDetailsSource.RUNS -> AppScreen.Runs
                }
            }
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

            var checkInRemainingSeconds by remember {
                mutableIntStateOf(checkInIntervalSeconds)
            }

            LaunchedEffect(
                checkInSegmentStartTime,
                checkInAccumulatedMillis,
                isPaused,
                isCheckInOverdue,
                alertsTriggered
            ) {
                while (alertsTriggered < 3) {
                    if (isPaused || isCheckInOverdue) {
                        delay(1000)
                        continue
                    }

                    val checkInElapsedMillis =
                        checkInAccumulatedMillis +
                                (System.currentTimeMillis() - checkInSegmentStartTime)

                    val remaining =
                        checkInIntervalSeconds - (checkInElapsedMillis / 1000).toInt()

                    if (remaining <= 0) {
                        alertsTriggered++
                        isCheckInOverdue = true
                        checkInRemainingSeconds = 0
                    } else {
                        checkInRemainingSeconds = remaining
                    }

                    delay(1000)
                }
            }

            val checkInTimerString = String.format(
                "%02d:%02d",
                checkInRemainingSeconds / 60,
                checkInRemainingSeconds % 60
            )

            val gpsSecondsAgo by produceState(
                initialValue = 0L,
                key1 = lastGpsUpdateTimeMillis
            ) {
                while (true) {
                    value = (System.currentTimeMillis() - lastGpsUpdateTimeMillis) / 1000
                    delay(1000)
                }
            }

            val gpsStatusText =
                if (isPaused) "Run paused" else "GPS updated ${gpsSecondsAgo}s ago"

            DisposableEffect(Unit) {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()
                val locationCallback = object : LocationCallback(){
                    override fun onLocationResult(locationResult: LocationResult){
                        if (!isPaused){
                            lastGpsUpdateTimeMillis = System.currentTimeMillis()
                            for (location in locationResult.locations){
                                val newPoint = RoutePointDto(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    timestampMillis = System.currentTimeMillis()
                                )
                                val currentSegments = routeSegments.toMutableList()
                                val activeSegment = currentSegments.last().toMutableList()

                                activeSegment.add(newPoint)
                                currentSegments[currentSegments.lastIndex] = activeSegment
                                routeSegments = currentSegments

                                if (activeSegment.size >= 2){
                                    val lastPoint = activeSegment[activeSegment.size - 2]
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
                safetyMode = when (safetyMode) {
                    SafetyMode.NONE -> "None"
                    SafetyMode.MODE_A -> "Mode A"
                    SafetyMode.MODE_B -> "Mode B"
                },
                activeRunId = currentRunId,
                checkInTimerString = checkInTimerString,
                isCheckInOverdue = isCheckInOverdue,
                maxAlertsReached = alertsTriggered >= 3,
                gpsStatusText = gpsStatusText,
                onCheckInClick = {
                    currentRunId?.let { runId ->
                        checkInAccumulatedMillis = 0L
                        checkInSegmentStartTime = System.currentTimeMillis()
                        isCheckInOverdue = false

                        @SuppressLint("MissingPermission")
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                val payload = mapOf(
                                    "lat" to location?.latitude,
                                    "lng" to location?.longitude
                                )

                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.checkIn(runId, payload)
                                    } catch (e: Exception) {
                                        println("Check-in failed: ${e.message}")
                                    }
                                }
                            }
                            .addOnFailureListener {
                                val payload = mapOf("lat" to null, "lng" to null)

                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.checkIn(runId, payload)
                                    } catch (e: Exception) {
                                        println("Check-in failed: ${e.message}")
                                    }
                                }
                            }
                    }
                },
                elapsedTime = formatElapsedTime(elapsedMillis),
                distanceMiles = dynamicDistanceMiles,
                calories = liveCalories,
                avgPace = livePace,
                isPaused = isPaused,
                onPauseResumeClick = {
                    if (isPaused) {
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        checkInSegmentStartTime = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false
                        routeSegments = routeSegments + listOf(emptyList())

                        currentRunId?.let { runId ->
                            if (safetyMode == SafetyMode.MODE_B && !isCheckInOverdue) {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.resumeSafety(
                                            runId,
                                            mapOf("remainingSeconds" to checkInRemainingSeconds)
                                        )
                                    } catch (e: Exception) {
                                        println("Resume safety failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    } else {
                        val now = System.currentTimeMillis()

                        accumulatedElapsedMillis += now - runSegmentStartTimeMillis

                        if (!isCheckInOverdue) {
                            checkInAccumulatedMillis += now - checkInSegmentStartTime
                        }

                        isPaused = true

                        currentRunId?.let { runId ->
                            if (safetyMode == SafetyMode.MODE_B) {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.pauseSafety(runId)
                                    } catch (e: Exception) {
                                        println("Pause safety failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                },
                onStopClick = {
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
                    val flatPoints = routeSegments.flatMapIndexed { index, segment -> segment.map { point -> point.copy(segmentIndex = index) } }
                    val runToSave = RunDto(
                        id = currentRunId,
                        startedAt = System.currentTimeMillis() - finalElapsedMillis,
                        endedAt = System.currentTimeMillis(),
                        distanceMiles = distance,
                        durationSeconds = durationSecs,
                        avgPaceSecsPerMile = if (distance > 0) durationSecs / distance else 0.0,
                        calories = estimatedCalories,
                        routePoints = flatPoints
                    )

                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.api.saveRun(runToSave)

                            if (response.isSuccessful) {
                                println("SUCCESS! Saved final run to backend")
                            } else {
                                println("SERVER ERROR: ${response.errorBody()?.string()}")
                            }

                            if (
                                safetyMode == SafetyMode.MODE_A ||
                                safetyMode == SafetyMode.MODE_B
                            ) {
                                currentRunId?.let { runId ->
                                    try {
                                        RetrofitClient.safetyApi.stopSafety(runId)
                                    } catch (e: Exception) {
                                        println("Failed to stop safety monitoring: ${e.message}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("NETWORK ERROR: ${e.message}")
                        }
                    }

                    currentScreen = AppScreen.EndRun(
                        RunSummary(
                            miles = distance,
                            durationText = formatSummaryDuration(finalElapsedMillis),
                            avgPaceText = formattedPace,
                            calories = estimatedCalories,
                            routeSegments = routeSegments
                        )
                    )
                },
                onPauseForEndDialog = {
                    if (!isPaused) {
                        val now = System.currentTimeMillis()

                        accumulatedElapsedMillis += now - runSegmentStartTimeMillis

                        if (!isCheckInOverdue) {
                            checkInAccumulatedMillis += now - checkInSegmentStartTime
                        }

                        isPaused = true
                        pausedByEndDialog = true
                        routeSegments = routeSegments + listOf(emptyList())

                        currentRunId?.let { runId ->
                            if (safetyMode == SafetyMode.MODE_B) {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.pauseSafety(runId)
                                    } catch (e: Exception) {
                                        println("Pause safety failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    } else {
                        pausedByEndDialog = false
                    }
                },
                onResumeAfterEndDialogDismiss = {
                    if (pausedByEndDialog) {
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        checkInSegmentStartTime = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false

                        currentRunId?.let { runId ->
                            if (safetyMode == SafetyMode.MODE_B && !isCheckInOverdue) {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.safetyApi.resumeSafety(
                                            runId,
                                            mapOf("remainingSeconds" to checkInRemainingSeconds)
                                        )
                                    } catch (e: Exception) {
                                        println("Resume safety failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        is AppScreen.EndRun -> {
            EndRunScreen(
                summary = screen.summary,
                onDoneClick = {
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

private fun calculateConsecutiveStreakDays(runs: List<RunDto>): Int {
    if (runs.isEmpty()) return 0

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val uniqueRunDates = runs
        .map { dateFormatter.format(Date(it.startedAt)) }
        .toSet()

    val calendar = Calendar.getInstance()
    var streak = 0

    while (true) {
        val currentDate = dateFormatter.format(calendar.time)

        if (uniqueRunDates.contains(currentDate)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            break
        }
    }

    return streak
}