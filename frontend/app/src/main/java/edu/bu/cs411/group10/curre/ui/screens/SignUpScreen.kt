package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
fun SignUpScreen(
    onCreateAccount: (String, String, String, (Boolean, String) -> Unit) -> Unit,
    onGoToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val emailInteractionSource = remember { MutableInteractionSource() }

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
            Spacer(modifier = Modifier.height(50.dp))

            AuthHeader(
                title = "Create Account",
                subtitle = "Join the Curre! community"
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
                        emailError = ""
                        statusMessage = ""
                    },
                    placeholder = { Text("Enter your email") },
                    singleLine = true,
                    isError = emailError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    interactionSource = emailInteractionSource,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CurreLime,
                        unfocusedBorderColor = Color(0xFFD4D8DE),
                        focusedContainerColor = CurreSurface,
                        unfocusedContainerColor = CurreSurface,
                        errorBorderColor = Color(0xFFD9534F)
                    )
                )

                if (emailError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = emailError,
                        color = Color(0xFFD9534F)
                    )
                }

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
                        passwordError = ""
                    },
                    placeholder = "Password",
                    helperText = "Enter your password",
                    isError = passwordError.isNotBlank()
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    placeholder = "Confirm password",
                    helperText = "Re-enter your password",
                    isError = confirmPasswordError.isNotBlank()
                )

                if (passwordError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = passwordError,
                        color = Color(0xFFD9534F)
                    )
                }

                if (confirmPasswordError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = confirmPasswordError,
                        color = Color(0xFFD9534F)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val passwordValidation = validatePassword(password)

                        emailError = when {
                            trimmedEmail.isBlank() -> "Email is required"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Invalid email format"
                            else -> ""
                        }

                        passwordError = passwordValidation

                        confirmPasswordError = when {
                            confirmPassword.isBlank() -> "Please confirm your password"
                            confirmPassword != password -> "Passwords do not match"
                            else -> ""
                        }

                        if (
                            emailError.isBlank() &&
                            passwordError.isBlank() &&
                            confirmPasswordError.isBlank()
                        ) {
                            isLoading = true
                            statusMessage = ""
                            onCreateAccount(trimmedEmail, password, confirmPassword) { success, message ->
                                isLoading = false
                                statusMessage = message
                                if (success) {
                                    onSignUpSuccess()
                                }
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
                        text = if (isLoading) "Signing Up..." else "Sign Up",
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusMessage,
                        color = if (statusMessage.contains("successful", ignoreCase = true)) 
                            Color(0xFF4CAF50) else Color(0xFFD9534F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            TextButton(onClick = onGoToLogin) {
                Text(
                    text = "Already have an account? Log In",
                    color = CurreTextMuted
                )
            }
        }
    }
}

private fun validatePassword(password: String): String {
    return when {
        password.length < 8 -> "Password must be at least 8 characters"
        password.none { it.isUpperCase() } -> "Include at least one uppercase letter"
        password.none { it.isLowerCase() } -> "Include at least one lowercase letter"
        password.none { it.isDigit() } -> "Include at least one number"
        else -> ""
    }
}