package com.baltajmn.bullet.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val DOT_DIAMETER = 1.5.dp
private val COLUMN_STEP = 24.dp
private val COLUMN_START = 12.dp
private val SPINE = 48.dp

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

/**
 * docs/pantallas.md 21: on a wide window the content is one page of at most [MAX_CONTENT_WIDTH], or a
 * [spread] of two and a [SPINE], centred with the left edge on a dot column, so every row keeps its
 * place on the paper that goes on filling the window at both sides. On a phone it does nothing. Goes
 * after [paper], so the dots cover the whole width and only the content narrows.
 */
@Composable
fun Modifier.page(spread: Boolean = false): Modifier {
    if (!isWideScreen()) return this
    return layout { measurable, constraints ->
        if (!constraints.hasBoundedWidth) {
            val p = measurable.measure(constraints)
            return@layout layout(p.width, p.height) { p.place(0, 0) }
        }
        val step = COLUMN_STEP.toPx()
        val columns = (constraints.maxWidth / step).toInt()
        val pageColumns = (MAX_CONTENT_WIDTH / COLUMN_STEP).toInt()
        val spineColumns = (SPINE / COLUMN_STEP).toInt()
        val used = if (spread) 2 * minOf(pageColumns, (columns - spineColumns) / 2) + spineColumns else minOf(pageColumns, columns)
        val width = (used * step).roundToInt()
        val p = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(constraints.maxWidth, p.height) { p.place(((columns - used) / 2 * step).roundToInt(), 0) }
    }
}

/**
 * The two pages of a `page(spread = true)`, like an open notebook: [left] and [right] each as wide as
 * the other, and the spine's 1dp line in the middle of the 48dp between them (docs/pantallas.md 21).
 */
@Composable
fun Spread(left: @Composable ColumnScope.() -> Unit, right: @Composable ColumnScope.() -> Unit) {
    val spine = MaterialTheme.colorScheme.outlineVariant
    Row(
        Modifier.fillMaxWidth().drawBehind {
            val x = size.width / 2
            drawLine(spine, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        },
    ) {
        Column(Modifier.weight(1f), content = left)
        Spacer(Modifier.width(SPINE))
        Column(Modifier.weight(1f), content = right)
    }
}
