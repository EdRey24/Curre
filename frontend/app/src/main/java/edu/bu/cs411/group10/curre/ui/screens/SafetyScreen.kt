package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.components.BottomNavTab
import edu.bu.cs411.group10.curre.ui.components.CurreBottomBar
import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import edu.bu.cs411.group10.curre.ui.model.EmergencyContactDto
import edu.bu.cs411.group10.curre.ui.theme.*
import kotlinx.coroutines.launch

enum class SafetyMode {
    MODE_A,
    MODE_B
}

@Composable
fun SafetyScreen(
    contacts: List<EmergencyContact>,
    selectedMode: SafetyMode,
    onModeChange: (SafetyMode) -> Unit,
    onHomeClick: () -> Unit,
    onStartRunClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onContactsUpdated: (List<EmergencyContact>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var contactBeingEdited by remember { mutableStateOf<EmergencyContact?>(null) }
    var contactPendingDelete by remember { mutableStateOf<EmergencyContact?>(null) }

    Scaffold(
        containerColor = CurreBackground,
        bottomBar = {
            CurreBottomBar(
                selectedTab = BottomNavTab.SAFETY,
                onHomeClick = onHomeClick,
                onSafetyClick = { },
                onStartRunClick = onStartRunClick,
                onRunsClick = onRunsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CurreBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 28.dp)
                    .navigationBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Safety",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "SAFETY MODE",
                    color = CurreTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                SafetyModeCard(
                    title = "Mode A: Start/End Notify",
                    subtitle = "Notifies contacts when run starts and ends",
                    selected = selectedMode == SafetyMode.MODE_A,
                    onClick = { onModeChange(SafetyMode.MODE_A) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                SafetyModeCard(
                    title = "Mode B: Timed Check-In",
                    subtitle = "Requires check-in every 15 minutes",
                    selected = selectedMode == SafetyMode.MODE_B,
                    onClick = { onModeChange(SafetyMode.MODE_B) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Messages include last known location and time.",
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(34.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAddAlt1,
                        contentDescription = "Emergency contacts",
                        tint = CurreLime,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Your Emergency Contacts (${contacts.size})",
                        color = CurreNavy,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (contacts.isEmpty()) {
                    EmptyContactsCard()
                } else {
                    contacts.forEach { contact ->
                        ContactCard(
                            contact = contact,
                            onEditClick = { contactBeingEdited = contact },
                            onDeleteClick = { contactPendingDelete = contact }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(2.dp, CurreOrange),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CurreOrange,
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add contact",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Add Contact",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Test notification button (still disabled for now)
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDDE1E7),
                        contentColor = Color(0xFF8B93A1),
                        disabledContainerColor = Color(0xFFDDE1E7),
                        disabledContentColor = Color(0xFF8B93A1)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Send test notification",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Send Test Notification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    // Add contact dialog
    if (showAddDialog) {
        ContactEditorDialog(
            title = "Add New Contact",
            initialName = "",
            initialEmail = "",
            initialPhone = "",
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, phone ->
                scope.launch {
                    try {
                        val dto = EmergencyContactDto(name = name, email = email, phone = phone)
                        val response = RetrofitClient.contactApi.addContact(dto)
                        if (response.isSuccessful) {
                            val newContactDto = response.body()
                            if (newContactDto != null) {
                                val newContact = EmergencyContact(
                                    id = newContactDto.id ?: 0L,
                                    name = newContactDto.name,
                                    email = newContactDto.email,
                                    phone = newContactDto.phone
                                )
                                onContactsUpdated(contacts + newContact)
                            }
                            showAddDialog = false
                        }
                    } catch (e: Exception) {
                        // Handle error silently for now
                    }
                }
            }
        )
    }

    // Edit contact dialog
    contactBeingEdited?.let { contact ->
        ContactEditorDialog(
            title = "Edit Contact",
            initialName = contact.name,
            initialEmail = contact.email,
            initialPhone = contact.phone ?: "",
            confirmText = "Save",
            onDismiss = { contactBeingEdited = null },
            onConfirm = { name, email, phone ->
                scope.launch {
                    try {
                        val dto = EmergencyContactDto(
                            id = contact.id,
                            name = name,
                            email = email,
                            phone = phone
                        )
                        val response = RetrofitClient.contactApi.updateContact(contact.id, dto)
                        if (response.isSuccessful) {
                            val updatedDto = response.body()
                            if (updatedDto != null) {
                                val updatedContact = EmergencyContact(
                                    id = updatedDto.id ?: contact.id,
                                    name = updatedDto.name,
                                    email = updatedDto.email,
                                    phone = updatedDto.phone
                                )
                                onContactsUpdated(
                                    contacts.map { if (it.id == contact.id) updatedContact else it }
                                )
                            }
                            contactBeingEdited = null
                        }
                    } catch (e: Exception) {
                        // Handle error silently
                    }
                }
            }
        )
    }

    // Delete confirmation
    contactPendingDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactPendingDelete = null },
            containerColor = CurreSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Contact?",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${contact.name} from your emergency contacts?",
                    color = CurreTextMuted,
                    fontSize = 15.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { contactPendingDelete = null }) {
                    Text("Cancel", color = CurreNavy)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.contactApi.deleteContact(contact.id)
                                if (response.isSuccessful) {
                                    onContactsUpdated(contacts.filterNot { it.id == contact.id })
                                    contactPendingDelete = null
                                }
                            } catch (e: Exception) {
                                // Handle error silently
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CurreOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ContactEditorDialog(
    title: String,
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }
    val emailValid = isValidEmail(email)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CurreSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                color = CurreNavy,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = email.isNotBlank() && !emailValid,
                    modifier = Modifier.fillMaxWidth()
                )

                if (email.isNotBlank() && !emailValid) {
                    Text(
                        text = "Enter a valid email address",
                        color = CurreOrange,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CurreNavy)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), email.trim(), phone.trim()) },
                enabled = name.isNotBlank() && emailValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CurreLime,
                    contentColor = CurreNavy,
                    disabledContainerColor = Color(0xFFDDE1E7),
                    disabledContentColor = Color(0xFF8B93A1)
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.contains("@") &&
            trimmed.substringAfter("@").contains(".") &&
            !trimmed.startsWith("@") &&
            !trimmed.endsWith("@")
}

@Composable
private fun SafetyModeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) Color(0xFF84D600) else Color(0xFFFCFCFB)
    val borderColor = if (selected) Color(0xFF84D600) else Color(0xFFD8DDE4)
    val titleColor = if (selected) Color.White else Color(0xFF39465A)
    val subtitleColor = if (selected) Color.White.copy(alpha = 0.92f) else CurreTextMuted
    val radioBorder = if (selected) Color.White else Color(0xFF98A0AE)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(2.dp, radioBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = CurreSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CurreLimeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = "Contact avatar",
                    tint = CurreTextMuted,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.email,
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEDD8)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit contact",
                        tint = CurreOrange
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.5.dp, CurreNavy),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = CurreNavy
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete contact"
                )
            }
        }
    }
}

@Composable
private fun EmptyContactsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = CurreSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = "No contacts",
                tint = CurreTextMuted,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No emergency contacts yet",
                color = CurreTextMuted,
                fontSize = 15.sp
            )
        }
    }
}