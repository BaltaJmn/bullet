package com.baltajmn.bullet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The interface's own icons (docs/pantallas.md 2): drawn on a 24 unit square so the same drawing works
 * at any size, 1.7 unit strokes with rounded caps and joins. These are not the method's marks
 * (BulletGlyph.kt), which never stand for an action.
 */
enum class Glyph {
    BACK, FORWARD, CLOSE, SHARE, SETTINGS, SEARCH, MORE, CHECK,
    TODAY, MONTH, FUTURE, INDEX, EDIT, TRASH, PLUS, LIST, GRID, BOOK, LATER,
}

/** A 48dp tap target with no background: the glyph is the whole control. */
@Composable
fun GlyphButton(glyph: Glyph, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { GlyphIcon(glyph, size = 22.dp) }
}

@Composable
fun GlyphIcon(glyph: Glyph, size: Dp = 20.dp, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Canvas(Modifier.size(size)) {
        val u = this.size.width / 24f
        val stroke = Stroke(width = 1.7f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun at(x: Float, y: Float) = Offset(x * u, y * u)
        fun line(vararg points: Pair<Float, Float>) = drawPath(
            Path().apply {
                points.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
            },
            tint,
            style = stroke,
        )
        fun dot(x: Float, y: Float, r: Float) = drawCircle(tint, radius = r * u, center = at(x, y))
        fun ring(x: Float, y: Float, r: Float) = drawCircle(tint, radius = r * u, center = at(x, y), style = stroke)
        fun box(x: Float, y: Float, w: Float, h: Float, r: Float) =
            drawRoundRect(tint, topLeft = at(x, y), size = Size(w * u, h * u), cornerRadius = CornerRadius(r * u), style = stroke)

        when (glyph) {
            Glyph.BACK -> line(14.5f to 5.5f, 8f to 12f, 14.5f to 18.5f)
            Glyph.FORWARD -> line(9.5f to 5.5f, 16f to 12f, 9.5f to 18.5f)
            Glyph.CLOSE -> {
                line(6.5f to 6.5f, 17.5f to 17.5f)
                line(17.5f to 6.5f, 6.5f to 17.5f)
            }
            Glyph.SHARE -> {
                line(12f to 20f, 12f to 4.5f)
                line(7f to 9.5f, 12f to 4.5f, 17f to 9.5f)
            }
            // Eight teeth around a hole: the sign for settings on both systems.
            Glyph.SETTINGS -> {
                val teeth = 8
                val gear = Path()
                for (k in 0 until teeth * 2) {
                    val a0 = 2 * PI * k / (teeth * 2) - PI / 2
                    val r = if (k % 2 == 0) 8.6f else 6.6f
                    for (s in listOf(-1, 1)) {
                        val a = a0 + s * PI / (teeth * 2) * 0.55
                        val x = (12 + r * cos(a)).toFloat() * u
                        val y = (12 + r * sin(a)).toFloat() * u
                        if (k == 0 && s == -1) gear.moveTo(x, y) else gear.lineTo(x, y)
                    }
                }
                gear.close()
                drawPath(gear, tint, style = stroke)
                ring(12f, 12f, 2.8f)
            }
            Glyph.SEARCH -> {
                ring(11f, 11f, 6f)
                line(15.5f to 15.5f, 20f to 20f)
            }
            Glyph.MORE -> listOf(5.5f, 12f, 18.5f).forEach { x -> dot(x, 12f, 1.7f) }
            Glyph.CHECK -> line(5.5f to 12.5f, 10f to 17f, 18.5f to 7.5f)
            Glyph.TODAY -> {
                box(5f, 3.5f, 14f, 17f, 2.5f)
                dot(12f, 12f, 2f)
            }
            Glyph.MONTH -> {
                box(3.5f, 5f, 17f, 15f, 2.5f)
                line(3.5f to 10f, 20.5f to 10f)
                line(8f to 3f, 8f to 7f)
                line(16f to 3f, 16f to 7f)
            }
            Glyph.FUTURE -> {
                box(3.5f, 5f, 17f, 15f, 2.5f)
                line(3.5f to 10f, 20.5f to 10f)
                line(8f to 3f, 8f to 7f)
                line(16f to 3f, 16f to 7f)
                line(10.5f to 13f, 13.5f to 15.5f, 10.5f to 18f)
            }
            Glyph.INDEX, Glyph.LIST -> listOf(7f, 12f, 17f).forEach { y ->
                dot(5.2f, y, 1.2f)
                line(9.5f to y, 19.5f to y)
            }
            Glyph.EDIT -> line(5f to 19f, 6f to 15f, 16f to 5f, 19f to 8f, 9f to 18f, 5f to 19f)
            Glyph.TRASH -> {
                line(5f to 7f, 19f to 7f)
                line(10f to 7f, 10f to 4.5f, 14f to 4.5f, 14f to 7f)
                line(7f to 7f, 8f to 19.5f, 16f to 19.5f, 17f to 7f)
            }
            Glyph.PLUS -> {
                line(12f to 5.5f, 12f to 18.5f)
                line(5.5f to 12f, 18.5f to 12f)
            }
            Glyph.GRID -> listOf(4f to 4f, 13.5f to 4f, 4f to 13.5f, 13.5f to 13.5f).forEach { (x, y) -> box(x, y, 6.5f, 6.5f, 1.5f) }
            Glyph.BOOK -> {
                line(12f to 7.5f, 12f to 19f)
                line(12f to 7.5f, 10f to 5.5f, 5f to 5.5f, 5f to 17f, 10f to 17f, 12f to 19f)
                line(12f to 7.5f, 14f to 5.5f, 19f to 5.5f, 19f to 17f, 14f to 17f, 12f to 19f)
            }
            Glyph.LATER -> {
                ring(12f, 12f, 7.5f)
                line(12f to 8f, 12f to 12.5f, 15f to 14.3f)
            }
        }
    }
}
