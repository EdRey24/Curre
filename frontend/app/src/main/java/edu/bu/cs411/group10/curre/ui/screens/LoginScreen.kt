package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.bu.cs411.group10.curre.ui.components.AuthCard
import edu.bu.cs411.group10.curre.ui.components.AuthHeader
import edu.bu.cs411.group10.curre.ui.components.PasswordField
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

@Composable
fun LoginScreen(
    onLogin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onGoToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val emailInteractionSource = remember { MutableInteractionSource() }
    val emailFocused by emailInteractionSource.collectIsFocusedAsState()

    // Mainscreen container
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CurreBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(CurreBackground)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top spacing
            Spacer(modifier = Modifier.height(50.dp))

            // Title + subtitle
            AuthHeader(
                title = "Welcome Back",
                subtitle = "Log in to continue running with Curre!"
            )

            Spacer(modifier = Modifier.height(34.dp))

            AuthCard {
                Text(
                    text = "Email",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        statusMessage = ""
                    },
                    placeholder = { Text("Enter your email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    interactionSource = emailInteractionSource,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CurreLime,
                        unfocusedBorderColor = Color(0xFFD4D8DE),
                        focusedContainerColor = CurreSurface,
                        unfocusedContainerColor = CurreSurface
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Password",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                PasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        statusMessage = ""
                    },
                    placeholder = "Password",
                    helperText = "Enter your password"
                )

                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage,
                        color = if (isError) Color(0xFFD9534F) else Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isBlank() || password.isBlank()) {
                            statusMessage = "Email and password are required"
                            isError = true
                            return@Button
                        }
                        isLoading = true
                        statusMessage = ""
                        onLogin(trimmedEmail, password) { success, message ->
                            isLoading = false
                            statusMessage = message
                            isError = !success
                            if (success) {
                                onLoginSuccess()
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CurreLime,
                        contentColor = CurreNavy
                    )
                ) {
                    Text(
                        text = if (isLoading) "Logging In..." else "Log In",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // switch to signup
            TextButton(onClick = onGoToSignUp) {
                Text(
                    text = "Don't have an account? Sign Up",
                    color = CurreTextMuted
                )
            }
        }
    }
}