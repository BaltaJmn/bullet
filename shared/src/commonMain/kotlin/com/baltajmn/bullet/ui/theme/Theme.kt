package com.baltajmn.bullet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Ink on paper, not a generic task manager (docs/pantallas.md 1.2, SPEC 5). Adapted from the
 * family's palette (line/docs/pantallas.md 1.1) with two changes in light, for AA contrast on the
 * paper: `onSurfaceVariant` (was 8B8479, 3.5:1) and `primary` (was 6FAE9B, 2.4:1). Dark already
 * passed and is unchanged. `error` is never shown; it reads as `onSurfaceVariant` in case a
 * Material component reaches for it on its own, so nothing is ever painted red.
 */
internal val Light = lightColorScheme(
    primary = Color(0xFF3F7A69),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8F3),
    onBackground = Color(0xFF39352E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF39352E),
    surfaceVariant = Color(0xFFF0EBE2),
    onSurfaceVariant = Color(0xFF736D63),
    outline = Color(0xFFE3DCD1),
    outlineVariant = Color(0xFFEFE9DF),
    error = Color(0xFF736D63),
)

internal val Dark = darkColorScheme(
    primary = Color(0xFF8FC9B6),
    onPrimary = Color(0xFF12271F),
    background = Color(0xFF17150F),
    onBackground = Color(0xFFECE5D9),
    surface = Color(0xFF201D16),
    onSurface = Color(0xFFECE5D9),
    surfaceVariant = Color(0xFF2C2820),
    onSurfaceVariant = Color(0xFF9C9486),
    outline = Color(0xFF3A352B),
    outlineVariant = Color(0xFF2C2820),
    error = Color(0xFF9C9486),
)

@Composable
fun BobbinTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}
