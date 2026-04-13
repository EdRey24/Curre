package edu.bu.cs411.group10.curre

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import edu.bu.cs411.group10.curre.ui.model.PastRun
import edu.bu.cs411.group10.curre.ui.model.RunDto
import edu.bu.cs411.group10.curre.ui.model.RunSummary
import edu.bu.cs411.group10.curre.ui.model.UserAccount
import edu.bu.cs411.group10.curre.ui.screens.ActiveRunScreen
import edu.bu.cs411.group10.curre.ui.screens.EndRunScreen
import edu.bu.cs411.group10.curre.ui.screens.HomeScreen
import edu.bu.cs411.group10.curre.ui.screens.LoginScreen
import edu.bu.cs411.group10.curre.ui.screens.SafetyMode
import edu.bu.cs411.group10.curre.ui.screens.SafetyScreen
import edu.bu.cs411.group10.curre.ui.screens.SignUpScreen
import edu.bu.cs411.group10.curre.ui.theme.CurreTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurreTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CurreApp()
                } // END OF Surface
            } // END OF CurreTheme
        } // END OF setContent
    } // END OF FUNCTION onCreate
} // END OF CLASS MainActivity

// Top-level screen state for the current prototype flow.
private sealed class AppScreen {
    data object Login : AppScreen()
    data object SignUp : AppScreen()
    data object Home : AppScreen()
    data object Safety : AppScreen()
    data object ActiveRun : AppScreen()
    data class EndRun(val summary: RunSummary) : AppScreen()
} // END OF SEALED CLASS AppScreen

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
    } // END OF INITIALIZATION registeredUsers

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Stores the time when the current "running segment" began.
    var runSegmentStartTimeMillis by remember { mutableLongStateOf(0L) }
    var accumulatedElapsedMillis by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var pausedByEndDialog by remember { mutableStateOf(false) }
    var pastRuns by remember { mutableStateOf<List<PastRun>>(emptyList()) }
    var safetyMode by remember { mutableStateOf(SafetyMode.MODE_A) }

    // Emergency contacts state and API integration
    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }
    val userId = 1L  // Hardcoded for demo; backend expects X-User-Id header

    // Fetch contacts when Safety screen is shown
    LaunchedEffect(currentScreen) {
        if (currentScreen is AppScreen.Safety) {
            isLoadingContacts = true
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.contactApi.getContacts(userId)
                } // END OF withContext
                if (response.isSuccessful) {
                    emergencyContacts = response.body() ?: emptyList()
                    println("DEBUG: Loaded ${emergencyContacts.size} contacts from backend") // DEBUG
                } else {
                    val errorMsg = "Failed to load contacts: ${response.code()}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    println("DEBUG: $errorMsg") // DEBUG
                } // END OF IF-ELSE BLOCK
            } catch (e: Exception) {
                val errorMsg = "Network error loading contacts: ${e.message}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                println("DEBUG: $errorMsg") // DEBUG
            } finally {
                isLoadingContacts = false
            } // END OF TRY-CATCH BLOCK
        } // END OF IF-BLOCK
    } // END OF LaunchedEffect

    // Fetch runs when Home screen is shown
    LaunchedEffect(currentScreen) {
        if (currentScreen is AppScreen.Home) {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.runApi.getRuns()
                } // END OF withContext
                if (response.isSuccessful) {
                    val fetchedDtos = response.body() ?: emptyList()
                    pastRuns = fetchedDtos.map { dto ->
                        PastRun(
                            dto.id?.toInt() ?: 0,
                            dto.avgPaceSecsPerMile / 60,
                            dto.durationSeconds / 60,
                            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(dto.startedAt))
                        )
                    }.reversed()
                    println("DEBUG: Loaded ${pastRuns.size} runs from backend") // DEBUG
                } else {
                    println("DEBUG: Failed to load runs, code ${response.code()}") // DEBUG
                } // END OF IF-ELSE BLOCK
            } catch (e: Exception) {
                println("DEBUG: Network error fetching runs: ${e.message}") // DEBUG
            } // END OF TRY-CATCH BLOCK
        } // END OF IF-BLOCK
    } // END OF LaunchedEffect

    // API call wrappers for emergency contacts
    val addContactApi: (String, String) -> Unit = { name, email ->
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.contactApi.addContact(userId, EmergencyContact(id = 0, name = name, email = email))
                } // END OF withContext
                if (response.isSuccessful) {
                    response.body()?.let { newContact ->
                        emergencyContacts = emergencyContacts + newContact
                        println("DEBUG: Contact added, new ID ${newContact.id}") // DEBUG
                    } // END OF let
                    Toast.makeText(context, "Contact added", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = "Error adding contact: ${response.code()}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    println("DEBUG: $errorMsg") // DEBUG
                } // END OF IF-ELSE BLOCK
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                println("DEBUG: $errorMsg") // DEBUG
            } // END OF TRY-CATCH BLOCK
        } // END OF launch
    } // END OF addContactApi

    val updateContactApi: (Long, String, String) -> Unit = { id, name, email ->
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.contactApi.updateContact(userId, id, EmergencyContact(id = id, name = name, email = email))
                } // END OF withContext
                if (response.isSuccessful) {
                    emergencyContacts = emergencyContacts.map { if (it.id == id) it.copy(name = name, email = email) else it }
                    Toast.makeText(context, "Contact updated", Toast.LENGTH_SHORT).show()
                    println("DEBUG: Contact $id updated") // DEBUG
                } else {
                    Toast.makeText(context, "Error updating contact", Toast.LENGTH_SHORT).show()
                    println("DEBUG: Update failed, code ${response.code()}") // DEBUG
                } // END OF IF-ELSE BLOCK
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                println("DEBUG: Update network error ${e.message}") // DEBUG
            } // END OF TRY-CATCH BLOCK
        } // END OF launch
    } // END OF updateContactApi

    val deleteContactApi: (Long) -> Unit = { id ->
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.contactApi.deleteContact(userId, id)
                } // END OF withContext
                if (response.isSuccessful) {
                    emergencyContacts = emergencyContacts.filterNot { it.id == id }
                    Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                    println("DEBUG: Contact $id deleted") // DEBUG
                } else {
                    Toast.makeText(context, "Error deleting contact", Toast.LENGTH_SHORT).show()
                    println("DEBUG: Delete failed, code ${response.code()}") // DEBUG
                } // END OF IF-ELSE BLOCK
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                println("DEBUG: Delete network error ${e.message}") // DEBUG
            } // END OF TRY-CATCH BLOCK
        } // END OF launch
    } // END OF deleteContactApi

    // Screen navigation logic
    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            LoginScreen(
                onLogin = { username, password ->
                    registeredUsers.any {
                        it.username.equals(username, ignoreCase = true) &&
                                it.password == password
                    }
                },
                onGoToSignUp = { currentScreen = AppScreen.SignUp },
                onLoginSuccess = { currentScreen = AppScreen.Home }
            )
        } // END OF AppScreen.Login

        is AppScreen.SignUp -> {
            SignUpScreen(
                isUsernameTaken = { username ->
                    registeredUsers.any { it.username.equals(username, ignoreCase = true) }
                },
                onCreateAccount = { username, password ->
                    registeredUsers = registeredUsers + UserAccount(username, password)
                },
                onGoToLogin = { currentScreen = AppScreen.Login },
                onSignUpSuccess = { currentScreen = AppScreen.Home }
            )
        } // END OF AppScreen.SignUp

        is AppScreen.Home -> {
            HomeScreen(
                emergencyContactsCount = emergencyContacts.size,
                weeklyMiles = 12.5,
                streakDays = 20,
                pastRuns = pastRuns,
                onStartRun = {
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                },
                onSafetyClick = { currentScreen = AppScreen.Safety },
                onRunsClick = { /* TODO */ },
                onProfileClick = { /* TODO */ },
                onRecentRunClick = { /* TODO */ },
                onSignOut = { currentScreen = AppScreen.SignUp }
            )
        } // END OF AppScreen.Home

        is AppScreen.Safety -> {
            SafetyScreen(
                contacts = emergencyContacts,
                isLoading = isLoadingContacts,
                selectedMode = safetyMode,
                onModeChange = { safetyMode = it },
                onAddContact = addContactApi,
                onUpdateContact = updateContactApi,
                onDeleteContact = deleteContactApi,
                onHomeClick = { currentScreen = AppScreen.Home },
                onStartRunClick = {
                    if (emergencyContacts.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Cannot start run with safety features: No emergency contacts added. Please add at least one contact in Safety settings.",
                            Toast.LENGTH_LONG
                        ).show()
                        println("DEBUG: Start run blocked – no emergency contacts") // DEBUG
                        return@SafetyScreen
                    } // END OF IF-BLOCK
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                },
                onRunsClick = { /* TODO */ },
                onProfileClick = { /* TODO */ }
            )
        } // END OF AppScreen.Safety

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
                    } // END OF IF-ELSE
                    delay(1000)
                } // END OF WHILE-LOOP
            } // END OF produceState

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
                        accumulatedElapsedMillis += (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                    } // END OF IF-ELSE
                },
                onStopClick = {
                    val finalElapsedMillis = if (isPaused) {
                        accumulatedElapsedMillis
                    } else {
                        accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
                    } // END OF IF-ELSE
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
                            val response = RetrofitClient.runApi.saveRun(runToSave)
                            if (response.isSuccessful) {
                                println("DEBUG: SUCCESS! Saved run to backend with ID: ${response.body()?.id}")
                            } else {
                                println("DEBUG: SERVER ERROR: ${response.errorBody()?.string()}")
                            } // END OF IF-ELSE
                        } catch (e: Exception) {
                            println("DEBUG: NETWORK ERROR: ${e.message}")
                        } // END OF TRY-CATCH
                    } // END OF launch
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
                        accumulatedElapsedMillis += (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                        pausedByEndDialog = true
                    } else {
                        pausedByEndDialog = false
                    } // END OF IF-ELSE
                },
                onResumeAfterEndDialogDismiss = {
                    if (pausedByEndDialog) {
                        runSegmentStartTimeMillis = System.currentTimeMillis()
                        isPaused = false
                        pausedByEndDialog = false
                    } // END OF IF-BLOCK
                }
            )
        } // END OF AppScreen.ActiveRun

        is AppScreen.EndRun -> {
            EndRunScreen(
                summary = screen.summary,
                onDoneClick = { currentScreen = AppScreen.Home }
            )
        } // END OF AppScreen.EndRun
    } // END OF WHEN-BLOCK
} // END OF FUNCTION CurreApp

private fun formatElapsedTime(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
} // END OF FUNCTION formatElapsedTime

private fun formatSummaryDuration(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%dm %02ds", minutes, seconds)
} // END OF FUNCTION formatSummaryDuration