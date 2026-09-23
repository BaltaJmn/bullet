package com.baltajmn.bullet.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test 25 (docs/tecnico.md 10, docs/pantallas.md 1.2): every text-on-background pair the app
 * paints, in light and in dark, at WCAG AA (4.5:1) or better. `surfaceVariant` is excluded on
 * purpose: no text is ever painted on it (docs/pantallas.md 1.2).
 */
class ThemeTest {
    private fun linearize(channel: Float): Double {
        val c = channel.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun relativeLuminance(color: Color): Double =
        0.2126 * linearize(color.red) + 0.7152 * linearize(color.green) + 0.0722 * linearize(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val lighter = max(relativeLuminance(a), relativeLuminance(b))
        val darker = min(relativeLuminance(a), relativeLuminance(b))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertAA(text: Color, background: Color, label: String) {
        val ratio = contrast(text, background)
        assertTrue(ratio >= 4.5, "$label: $ratio is below 4.5:1")
    }

    @Test
    fun everyTextBackgroundPairPassesAAInLight() {
        assertAA(Light.onBackground, Light.background, "onBackground/background light")
        assertAA(Light.onBackground, Light.surface, "onBackground/surface light")
        assertAA(Light.onSurfaceVariant, Light.background, "onSurfaceVariant/background light")
        assertAA(Light.onSurfaceVariant, Light.surface, "onSurfaceVariant/surface light")
        assertAA(Light.primary, Light.background, "primary/background light")
        assertAA(Light.primary, Light.surface, "primary/surface light")
    }

    @Test
    fun everyTextBackgroundPairPassesAAInDark() {
        assertAA(Dark.onBackground, Dark.background, "onBackground/background dark")
        assertAA(Dark.onBackground, Dark.surface, "onBackground/surface dark")
        assertAA(Dark.onSurfaceVariant, Dark.background, "onSurfaceVariant/background dark")
        assertAA(Dark.onSurfaceVariant, Dark.surface, "onSurfaceVariant/surface dark")
        assertAA(Dark.primary, Dark.background, "primary/background dark")
        assertAA(Dark.primary, Dark.surface, "primary/surface dark")
    }
}
