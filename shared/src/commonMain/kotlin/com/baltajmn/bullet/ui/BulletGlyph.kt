package com.baltajmn.bullet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus

/**
 * The method's own marks, drawn in `Canvas` rather than typed (docs/pantallas.md 1.2, 1.5): a
 * typed glyph renders as colour emoji on iOS and plain text on Android, and no font ships a
 * migrated-task angle. Every shape lives in a 24dp box with 1.5dp rounded strokes; nothing here
 * scales with the system font (SPEC 5), only the box a caller places it in can move.
 */
val GLYPH_BOX = 24.dp
private val STROKE_WIDTH = 1.5.dp

/** A task, event or note by its bullet and status. Color is always `onBackground`: a closed task
 * dims its text, never its glyph, so the state stays legible without relying on color alone. */
@Composable
fun BulletGlyph(
    bullet: Bullet,
    status: TaskStatus,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(
        modifier
            .size(GLYPH_BOX)
            .semantics { contentDescription = S.glyphName(bullet, status) },
    ) {
        drawBulletGlyph(bullet, status, tint)
    }
}

/** A signifier mark in its own margin column, same box and stroke as [BulletGlyph]. */
@Composable
fun SignifierGlyph(
    signifier: Signifier,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(
        modifier
            .size(GLYPH_BOX)
            .semantics { contentDescription = S.glyphName(signifier) },
    ) {
        drawSignifierGlyph(signifier, tint)
    }
}

private fun DrawScope.at(x: Float, y: Float) = Offset(x.dp.toPx(), y.dp.toPx())

private fun DrawScope.strokeOf() = Stroke(width = STROKE_WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.drawDot(tint: Color, x: Float, y: Float, diameter: Float) =
    drawCircle(tint, radius = (diameter / 2).dp.toPx(), center = at(x, y))

private fun DrawScope.drawRing(tint: Color, x: Float, y: Float, diameter: Float) =
    drawCircle(tint, radius = (diameter / 2).dp.toPx(), center = at(x, y), style = strokeOf())

private fun DrawScope.drawLine(tint: Color, vararg points: Pair<Float, Float>) = drawPath(
    Path().apply {
        points.forEachIndexed { i, (x, y) ->
            val offset = at(x, y)
            if (i == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
        }
    },
    tint,
    style = strokeOf(),
)

/** Coordinates from docs/pantallas.md 1.5, a 24dp box with origin at the top left. */
private fun DrawScope.drawBulletGlyph(bullet: Bullet, status: TaskStatus, tint: Color) {
    when (bullet) {
        Bullet.EVENT -> drawRing(tint, 12f, 12f, 8f)
        Bullet.NOTE -> drawLine(tint, 8f to 12f, 16f to 12f)
        Bullet.TASK -> when (status) {
            // IRRELEVANT keeps the open dot: the strikethrough runs across the entry's text, which
            // is the row composable's job, not a 24dp glyph box.
            TaskStatus.OPEN, TaskStatus.IRRELEVANT -> drawDot(tint, 12f, 12f, 5f)
            TaskStatus.DONE -> {
                drawDot(tint, 12f, 12f, 5f)
                drawLine(tint, 8f to 8f, 16f to 16f)
                drawLine(tint, 16f to 8f, 8f to 16f)
            }
            TaskStatus.MIGRATED -> drawLine(tint, 10f to 8f, 14.5f to 12f, 10f to 16f)
            TaskStatus.SCHEDULED -> drawLine(tint, 14f to 8f, 9.5f to 12f, 14f to 16f)
        }
    }
}

private fun DrawScope.drawSignifierGlyph(signifier: Signifier, tint: Color) {
    when (signifier) {
        // Three 8dp strokes through the center: vertical, and +-60deg from it, six spokes in all.
        Signifier.PRIORITY -> {
            drawLine(tint, 12f to 8f, 12f to 16f)
            drawLine(tint, 8.5f to 10f, 15.5f to 14f)
            drawLine(tint, 15.5f to 10f, 8.5f to 14f)
        }
        Signifier.INSPIRATION -> {
            drawLine(tint, 12f to 7.5f, 12f to 13.5f)
            drawDot(tint, 12f, 16.5f, 2f)
        }
        Signifier.EXPLORE -> {
            val eye = Path().apply {
                val start = at(6.5f, 12f)
                moveTo(start.x, start.y)
                val topEnd = at(17.5f, 12f)
                val topControl = at(12f, 7.5f)
                quadraticTo(topControl.x, topControl.y, topEnd.x, topEnd.y)
                val bottomControl = at(12f, 16.5f)
                quadraticTo(bottomControl.x, bottomControl.y, start.x, start.y)
            }
            drawPath(eye, tint, style = strokeOf())
            drawDot(tint, 12f, 12f, 3f)
        }
    }
}
