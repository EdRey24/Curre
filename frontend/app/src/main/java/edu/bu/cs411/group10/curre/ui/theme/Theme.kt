package edu.bu.cs411.group10.curre.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CurreColorScheme = lightColorScheme(
    primary = CurreLime,
    secondary = CurreOrange,
    background = CurreBackground,
    surface = CurreSurface,
    onPrimary = CurreNavy,
    onSecondary = CurreSurface,
    onBackground = CurreNavy,
    onSurface = CurreNavy
)

@Composable
fun CurreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CurreColorScheme,
        typography = Typography,
        content = content
    )
}