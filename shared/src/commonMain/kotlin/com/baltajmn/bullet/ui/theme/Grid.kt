package com.baltajmn.bullet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The page's vertical unit (docs/pantallas.md 1.1): every row height, margin and target is a
 * multiple of it, so the rejilla grows with the system font scale and nothing overlaps, but never
 * shrinks below 24dp. Horizontal columns stay a fixed 24dp (Paper.kt) so a line of text keeps its
 * width regardless of scale.
 */
val gridUnit: Dp
    @Composable
    @ReadOnlyComposable
    get() = 24.dp * maxOf(1f, LocalDensity.current.fontScale)
