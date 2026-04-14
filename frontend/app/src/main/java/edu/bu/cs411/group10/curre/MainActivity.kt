package edu.bu.cs411.group10.curre

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding   // <-- ADDED to fix 'padding' reference
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.data.ContactRepository
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
import kotlinx.coroutines.launch
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
                    modifier = Modifier.fillMaxSize(),
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
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.SignUp) }

    var registeredUsers by remember {
        mutableStateOf(
            listOf(
                UserAccount(username = "demo", password = "Password1")
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Backend initialisation state
    var backendReady by remember { mutableStateOf(false) }
    var initialisingBackend by remember { mutableStateOf(true) }

    // Manual IP dialog state
    var showManualDialog by remember { mutableStateOf(false) }
    var manualIp by remember { mutableStateOf("") }

    // Run tracking state
    var runSegmentStartTimeMillis by remember { mutableLongStateOf(0L) }
    var accumulatedElapsedMillis by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var pausedByEndDialog by remember { mutableStateOf(false) }
    var pastRuns by remember { mutableStateOf<List<PastRun>>(emptyList()) }
    var safetyMode by remember { mutableStateOf(SafetyMode.MODE_A) }

    // Emergency contacts state
    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }

    // Initialise RetrofitClient (tries saved URL or default)
    LaunchedEffect(Unit) {
        val success = RetrofitClient.initialize(context)
        backendReady = success
        initialisingBackend = false
        if (!success) {
            Toast.makeText(context, "Cannot connect to backend. Please enter your backend IP manually.", Toast.LENGTH_LONG).show()
            println("DEBUG: Backend initialisation failed")
        } else {
            println("DEBUG: Backend initialised successfully")
        }
    }

    // Show loading screen while initialising
    if (initialisingBackend) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // If backend not ready, show manual IP entry dialog
    if (!backendReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Unable to reach the Curre! backend.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter the IP address of the computer running the backend (e.g., 10.0.0.126).",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showManualDialog = true }) {
                    Text("Enter Backend IP")
                }
            }
        }

        if (showManualDialog) {
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                title = { Text("Backend IP Address") },
                text = {
                    Column {
                        Text("Example: 10.0.0.126 (from ipconfig)")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualIp,
                            onValueChange = { manualIp = it },
                            label = { Text("IP Address") },
                            singleLine = true,
                            placeholder = { Text("e.g., 10.0.0.126") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showManualDialog = false
                        if (manualIp.isNotBlank()) {
                            val customUrl = if (manualIp.contains("://")) manualIp else "http://$manualIp:8080/"
                            initialisingBackend = true
                            coroutineScope.launch {
                                val success = RetrofitClient.initializeWithUrl(customUrl)
                                backendReady = success
                                initialisingBackend = false
                                if (!success) {
                                    Toast.makeText(context, "Cannot reach $customUrl", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }) {
                        Text("Save and Connect")
                    }
                },
                dismissButton = {
                    Button(onClick = { showManualDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        return
    }

    // --- The rest of the navigation logic (unchanged from your original) ---
    // Fetch runs when Home screen is shown
    LaunchedEffect(currentScreen) {
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
                            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(dto.startedAt))
                        )
                    }.reversed()
                    println("DEBUG: Loaded ${pastRuns.size} runs from backend")
                } else {
                    println("DEBUG: Failed to load runs, code ${response.code()}")
                }
            } catch (e: Exception) {
                println("DEBUG: Network error fetching runs: ${e.message}")
            }
        }
    }

    // Fetch contacts when Safety screen is shown
    LaunchedEffect(currentScreen) {
        if (currentScreen is AppScreen.Safety) {
            isLoadingContacts = true
            val result = ContactRepository.loadContacts()
            if (result.isSuccess) {
                emergencyContacts = result.getOrNull() ?: emptyList()
                println("DEBUG: Loaded ${emergencyContacts.size} contacts from backend")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Toast.makeText(context, "Failed to load contacts: $error", Toast.LENGTH_SHORT).show()
                println("DEBUG: Load error: $error")
            }
            isLoadingContacts = false
        }
    }

    // API call wrappers using ContactRepository
    val addContactApi: (String, String) -> Unit = { name, email ->
        coroutineScope.launch {
            val result = ContactRepository.addContact(name, email)
            if (result.isSuccess) {
                result.getOrNull()?.let { newContact ->
                    emergencyContacts = emergencyContacts + newContact
                    println("DEBUG: Contact added, new ID ${newContact.id}")
                }
                Toast.makeText(context, "Contact added", Toast.LENGTH_SHORT).show()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Network error"
                Toast.makeText(context, "Error adding contact: $error", Toast.LENGTH_SHORT).show()
                println("DEBUG: Add failed: $error")
            }
        }
    }

    val updateContactApi: (Long, String, String) -> Unit = { id, name, email ->
        coroutineScope.launch {
            val result = ContactRepository.updateContact(id, name, email)
            if (result.isSuccess) {
                emergencyContacts = emergencyContacts.map { if (it.id == id) it.copy(name = name, email = email) else it }
                Toast.makeText(context, "Contact updated", Toast.LENGTH_SHORT).show()
                println("DEBUG: Contact $id updated")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Network error"
                Toast.makeText(context, "Error updating contact: $error", Toast.LENGTH_SHORT).show()
                println("DEBUG: Update failed: $error")
            }
        }
    }

    val deleteContactApi: (Long) -> Unit = { id ->
        coroutineScope.launch {
            val result = ContactRepository.deleteContact(id)
            if (result.isSuccess) {
                emergencyContacts = emergencyContacts.filterNot { it.id == id }
                Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                println("DEBUG: Contact $id deleted")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Network error"
                Toast.makeText(context, "Error deleting contact: $error", Toast.LENGTH_SHORT).show()
                println("DEBUG: Delete failed: $error")
            }
        }
    }

    // Screen navigation logic
    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            LoginScreen(
                onLogin = { username, password ->
                    registeredUsers.any {
                        it.username.equals(username, ignoreCase = true) && it.password == password
                    }
                },
                onGoToSignUp = { currentScreen = AppScreen.SignUp },
                onLoginSuccess = { currentScreen = AppScreen.Home }
            )
        }
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
        }
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
        }
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
                        println("DEBUG: Start run blocked – no emergency contacts")
                        return@SafetyScreen
                    }
                    accumulatedElapsedMillis = 0L
                    runSegmentStartTimeMillis = System.currentTimeMillis()
                    isPaused = false
                    currentScreen = AppScreen.ActiveRun
                },
                onRunsClick = { /* TODO */ },
                onProfileClick = { /* TODO */ }
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
                    value = if (isPaused) accumulatedElapsedMillis
                    else accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
                    kotlinx.coroutines.delay(1000)
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
                        accumulatedElapsedMillis += (System.currentTimeMillis() - runSegmentStartTimeMillis)
                        isPaused = true
                    }
                },
                onStopClick = {
                    val finalElapsedMillis = if (isPaused) accumulatedElapsedMillis
                    else accumulatedElapsedMillis + (System.currentTimeMillis() - runSegmentStartTimeMillis)
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
                            if (response.isSuccessful) println("DEBUG: SUCCESS! Saved run to backend with ID: ${response.body()?.id}")
                            else println("DEBUG: SERVER ERROR: ${response.errorBody()?.string()}")
                        } catch (e: Exception) {
                            println("DEBUG: NETWORK ERROR: ${e.message}")
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
                        accumulatedElapsedMillis += (System.currentTimeMillis() - runSegmentStartTimeMillis)
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
                onDoneClick = { currentScreen = AppScreen.Home }
            )
        }
    }
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