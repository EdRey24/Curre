package edu.bu.cs411.group10.curre.ui.components

import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import edu.bu.cs411.group10.curre.ui.theme.CurreLime
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String,
    helperText: String = "Enter your password",
    isError: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            if (label.isNotBlank()) {
                Text(label)
            }
        },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = "Toggle password visibility",
                    tint = CurreTextMuted
                )
            }
        },
        supportingText = {
            Text(
                text = helperText,
                color = if (isError) Color(0xFFD9534F) else CurreTextMuted
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CurreLime,
            unfocusedBorderColor = Color(0xFFD4D8DE),
            focusedLabelColor = CurreNavy,
            unfocusedLabelColor = CurreNavy,
            focusedContainerColor = CurreSurface,
            unfocusedContainerColor = CurreSurface,
            errorBorderColor = Color(0xFFD9534F),
            errorSupportingTextColor = Color(0xFFD9534F)
        )
    )
}