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
    onLogin: (String, String) -> Boolean,
    onGoToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val usernameInteractionSource = remember { MutableInteractionSource() }
    val usernameFocused by usernameInteractionSource.collectIsFocusedAsState()

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

            // card container for nputs
            AuthCard {
                Text(
                    text = "Username",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    interactionSource = usernameInteractionSource,
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
                        errorMessage = ""
                    },
                    placeholder = "Password",
                    helperText = "Enter your password"
                )

                // error message
                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD9534F)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // login button
                Button(
                    onClick = {
                        val success = onLogin(username.trim(), password)
                        if (success) {
                            onLoginSuccess()  // go to homescreen
                        } else {
                            errorMessage = "Incorrect username or password"
                        }
                    },
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
                        text = "Log In",
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