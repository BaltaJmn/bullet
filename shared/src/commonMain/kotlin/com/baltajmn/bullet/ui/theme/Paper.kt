package com.baltajmn.bullet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

private val DOT_DIAMETER = 1.5.dp
private val COLUMN_STEP = 24.dp
private val COLUMN_START = 12.dp

/**
 * Paints the dotted page (docs/pantallas.md 1.1): a 1.5dp dot every 24dp horizontally, on every
 * vertical grid line, aligned so a dot sits on the baseline of an `Ink` row at any font scale.
 * Lined, grid and blank paper arrive with #49; dotted is always the free default. The page itself
 * scrolls with the content, so this only paints, it never reserves space.
 */
@Composable
fun Modifier.paper(): Modifier {
    val unit = gridUnit
    val dotColor = MaterialTheme.colorScheme.outlineVariant
    val inkStyle = Type.Ink
    val textMeasurer = rememberTextMeasurer()
    val baseline = remember(inkStyle, textMeasurer) { inkBaseline(textMeasurer, inkStyle) }
    return drawBehind {
        val step = COLUMN_STEP.toPx()
        val startX = COLUMN_START.toPx()
        val radius = (DOT_DIAMETER / 2).toPx()
        val unitPx = unit.toPx()
        var y = baseline
        while (y <= size.height) {
            var x = startX
            while (x <= size.width) {
                drawCircle(dotColor, radius = radius, center = Offset(x, y))
                x += step
            }
            y += unitPx
        }
    }
}

private fun inkBaseline(textMeasurer: TextMeasurer, style: TextStyle): Float =
    textMeasurer.measure("I", style).firstBaseline
