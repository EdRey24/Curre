package edu.bu.cs411.group10.curre.ui.screens // Interaction with screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import edu.bu.cs411.group10.curre.ui.components.BottomNavTab
import edu.bu.cs411.group10.curre.ui.components.CurreBottomBar
import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreLimeSoft
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

enum class SafetyMode {
    MODE_A,
    MODE_B
} // END OF ENUM SafetyMode

@Composable
fun SafetyScreen(
    contacts: List<EmergencyContact>,
    isLoading: Boolean = false,
    selectedMode: SafetyMode,
    onModeChange: (SafetyMode) -> Unit,
    onAddContact: (String, String) -> Unit,
    onUpdateContact: (Long, String, String) -> Unit,   // changed from Int to Long
    onDeleteContact: (Long) -> Unit,                   // changed from Int to Long
    onHomeClick: () -> Unit,
    onStartRunClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
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
        } // END OF bottomBar
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

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CurreLime)
                    } // END OF Box
                    Spacer(modifier = Modifier.height(24.dp))
                } // END OF IF-BLOCK

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
                    subtitle = "Requires check-in every X minutes",
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

                if (contacts.isEmpty() && !isLoading) {
                    EmptyContactsCard()
                } else if (!isLoading) {
                    contacts.forEach { contact ->
                        ContactCard(
                            contact = contact,
                            onEditClick = {
                                contactBeingEdited = contact
                            },
                            onDeleteClick = {
                                contactPendingDelete = contact
                            }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    } // END OF forEach
                } // END OF IF-ELSE

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
            } // END OF Column
        } // END OF Surface
    } // END OF Scaffold

    if (showAddDialog) {
        ContactEditorDialog(
            title = "Add New Contact",
            initialName = "",
            initialEmail = "",
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email ->
                onAddContact(name, email)
                showAddDialog = false
            }
        )
    } // END OF IF-BLOCK

    contactBeingEdited?.let { contact ->
        ContactEditorDialog(
            title = "Edit Contact",
            initialName = contact.name,
            initialEmail = contact.email,
            confirmText = "Save",
            onDismiss = { contactBeingEdited = null },
            onConfirm = { name, email ->
                onUpdateContact(contact.id, name, email)
                contactBeingEdited = null
            }
        )
    } // END OF let

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
                        onDeleteContact(contact.id)
                        contactPendingDelete = null
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
    } // END OF let
} // END OF FUNCTION SafetyScreen

private fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.contains("@") &&
            trimmed.substringAfter("@").contains(".") &&
            !trimmed.startsWith("@") &&
            !trimmed.endsWith("@")
} // END OF FUNCTION isValidEmail

@Composable
private fun ContactEditorDialog(
    title: String,
    initialName: String,
    initialEmail: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = email.isNotBlank() && !emailValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors()
                )

                if (email.isNotBlank() && !emailValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter a valid email address",
                        color = CurreOrange,
                        fontSize = 13.sp
                    )
                } // END OF IF-BLOCK
            } // END OF Column
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CurreNavy)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name.trim(), email.trim())
                },
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
} // END OF FUNCTION ContactEditorDialog

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
                } // END OF IF-BLOCK
            } // END OF Box

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
            } // END OF Column
        } // END OF Row
    } // END OF Card
} // END OF FUNCTION SafetyModeCard

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
            } // END OF Box

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
            } // END OF Column

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
                } // END OF IconButton
            } // END OF Box

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
            } // END OF OutlinedButton
        } // END OF Row
    } // END OF Card
} // END OF FUNCTION ContactCard

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
        } // END OF Column
    } // END OF Card
} // END OF FUNCTION EmptyContactsCard