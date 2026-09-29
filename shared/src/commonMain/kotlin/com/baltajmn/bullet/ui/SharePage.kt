package com.baltajmn.bullet.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.ShareRow
import com.baltajmn.bullet.data.paginate
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.ui.theme.Paper

const val SHARE_W = 1080
const val SHARE_H = 1350

// Always in light, like the sisters' cards: a page is read in someone else's feed, not in this app.
private val Cream = Color(0xFFFBF8F3)
private val Ink = Color(0xFF39352E)
private val Muted = Color(0xFF736D63)
private val Dots = Color(0xFFEFE9DF)

// The app's grid at 2.25 (docs/pantallas.md 17.2): a 54 px unit, 20 columns by 25 rows.
private const val U = 54f
private const val SCALE = 2.25f
private const val TEXT_X = 216f
private const val RIGHT = 1026f
private const val ENTRIES_TOP = 270f
private const val ENTRIES_BOTTOM = 1188f

/**
 * The shared page as images (docs/pantallas.md 17.2). Every size is a pixel of the image, so [measurer]
 * has to measure at density 1: one from `rememberTextMeasurer()` brings the screen's and paints at two
 * or three times the size.
 */
fun renderSharePages(content: ShareContent, journal: Journal, paper: Paper, literata: FontFamily, measurer: TextMeasurer): List<ImageBitmap> {
    val entryStyle = TextStyle(fontFamily = literata, fontSize = 38.sp, lineHeight = 54.sp, color = Ink)
    val titleStyle = TextStyle(fontFamily = literata, fontSize = 54.sp, lineHeight = 108.sp, color = Ink)
    val headingStyle = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp, color = Muted)
    val baseline = measurer.measure("I", entryStyle).firstBaseline

    val title = measurer.measure(content.title, titleStyle, constraints = Constraints(maxWidth = (RIGHT - 162f).toInt()))
    // A title that wraps pushes the entries down a whole row per line, the same on every page.
    val top = maxOf(ENTRIES_TOP, 108f + title.size.height + U)
    val laid = content.rows.map { row ->
        when (row) {
            is ShareRow.Heading -> measurer.measure(row.text.uppercase(), headingStyle)
            is ShareRow.Line -> measurer.measure(entryText(row.entry, journal), entryStyle, constraints = Constraints(maxWidth = (RIGHT - TEXT_X).toInt()))
        }
    }
    // Each entry, its lines and one blank one; a heading, one row.
    val heights = content.rows.mapIndexed { i, row -> if (row is ShareRow.Heading) U.toInt() else laid[i].size.height + U.toInt() }
    val pages = paginate(heights, (ENTRIES_BOTTOM - top).toInt())

    return pages.mapIndexed { index, range ->
        page { scope ->
            scope.paper(paper, baseline)
            scope.drawText(title, topLeft = Offset(162f, 108f))
            scope.clipRect(top = top, bottom = ENTRIES_BOTTOM) {
                var y = top
                for (i in range) {
                    when (val row = content.rows[i]) {
                        is ShareRow.Heading -> drawText(laid[i], topLeft = Offset(162f, y + baseline - laid[i].firstBaseline))
                        is ShareRow.Line -> {
                            row.day?.let { day ->
                                drawText(measurer.measure("$day", entryStyle.copy(color = Muted)), topLeft = Offset(U, y))
                            }
                            // A glyph box is one line tall, so its centre is the middle of the first line.
                            row.entry.signifiers.sortedByDescending { it.ordinal }.forEachIndexed { k, s ->
                                signifier(s, 162f - U * (k + 1), y)
                            }
                            bullet(row.entry, 162f, y)
                            drawText(laid[i], topLeft = Offset(TEXT_X, y))
                        }
                    }
                    y += heights[i]
                }
            }
            scope.footer(measurer, literata, index + 1, pages.size)
        }
    }
}

/** docs/pantallas.md 5.1: closed tasks in the muted ink, a discarded one struck, and where a copy went. */
private fun entryText(e: Entry, journal: Journal) = buildAnnotatedString {
    val dimmed = e.bullet == Bullet.TASK && e.status in setOf(TaskStatus.DONE, TaskStatus.MIGRATED, TaskStatus.SCHEDULED)
    val struck = e.status == TaskStatus.IRRELEVANT
    withStyle(SpanStyle(color = if (dimmed) Muted else Ink, textDecoration = if (struck) TextDecoration.LineThrough else null)) {
        append(e.text)
    }
    val went = if (e.status == TaskStatus.MIGRATED || e.status == TaskStatus.SCHEDULED) wentToText(copyOf(journal, e.id)?.place, journal) else null
    if (went != null) {
        append("  ")
        withStyle(SpanStyle(color = Muted)) { append(went) }
    }
}

/** Density 1: a size in sp is a size in px, which is what the fixed layout of the page is written in. */
private fun page(draw: (DrawScope) -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(SHARE_W, SHARE_H)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), Size(SHARE_W.toFloat(), SHARE_H.toFloat())) {
        drawRect(Cream)
        draw(this)
    }
    return bitmap
}

/** The paper chosen, on the baseline of every row, like the screen (docs/pantallas.md 17.2). */
private fun DrawScope.paper(paper: Paper, baseline: Float) {
    var y = baseline
    while (y < SHARE_H) {
        when (paper) {
            Paper.Dotted -> {
                var x = U / 2
                while (x < SHARE_W) {
                    drawCircle(Dots, radius = 1.7f, center = Offset(x, y))
                    x += U
                }
            }
            Paper.Lined, Paper.Grid -> drawLine(Dots, Offset(0f, y), Offset(SHARE_W.toFloat(), y), strokeWidth = 2f)
            Paper.Blank -> Unit
        }
        y += U
    }
    if (paper == Paper.Grid) {
        var x = U / 2
        while (x < SHARE_W) {
            drawLine(Dots, Offset(x, 0f), Offset(x, SHARE_H.toFloat()), strokeWidth = 2f)
            x += U
        }
    }
}

private fun DrawScope.footer(measurer: TextMeasurer, literata: FontFamily, index: Int, count: Int) {
    val brand = measurer.measure(S.appName, TextStyle(fontFamily = literata, fontSize = 30.sp, color = Muted, textAlign = TextAlign.End))
    drawText(brand, topLeft = Offset(RIGHT - brand.size.width, 1296f - brand.firstBaseline))
    if (count > 1) {
        val page = measurer.measure(S.pageOf(index, count), TextStyle(fontSize = 30.sp, color = Muted))
        drawText(page, topLeft = Offset(162f, 1296f - page.firstBaseline))
    }
}

// The glyphs of docs/pantallas.md 1.5 at 2.25, in a 54 px box whose top left is (x, y).
private fun DrawScope.at(x: Float, y: Float, px: Float, py: Float) = Offset(x + px * SCALE, y + py * SCALE)
private val glyphStroke = Stroke(width = 1.5f * SCALE, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.polyline(x: Float, y: Float, vararg points: Pair<Float, Float>) = drawPath(
    Path().apply {
        points.forEachIndexed { i, (px, py) ->
            val p = at(x, y, px, py)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
    },
    Ink,
    style = glyphStroke,
)

private fun DrawScope.bullet(e: Entry, x: Float, y: Float) {
    val dot = { drawCircle(Ink, radius = 2.5f * SCALE, center = at(x, y, 12f, 12f)) }
    when (e.bullet) {
        Bullet.EVENT -> drawCircle(Ink, radius = 4f * SCALE, center = at(x, y, 12f, 12f), style = glyphStroke)
        Bullet.NOTE -> polyline(x, y, 8f to 12f, 16f to 12f)
        Bullet.TASK -> when (e.status) {
            TaskStatus.OPEN, TaskStatus.IRRELEVANT -> dot()
            TaskStatus.DONE -> {
                dot()
                polyline(x, y, 8f to 8f, 16f to 16f)
                polyline(x, y, 16f to 8f, 8f to 16f)
            }
            TaskStatus.MIGRATED -> polyline(x, y, 10f to 8f, 14.5f to 12f, 10f to 16f)
            TaskStatus.SCHEDULED -> polyline(x, y, 14f to 8f, 9.5f to 12f, 14f to 16f)
        }
    }
}

private fun DrawScope.signifier(s: Signifier, x: Float, y: Float) {
    when (s) {
        Signifier.PRIORITY -> {
            polyline(x, y, 12f to 8f, 12f to 16f)
            polyline(x, y, 8.5f to 10f, 15.5f to 14f)
            polyline(x, y, 15.5f to 10f, 8.5f to 14f)
        }
        Signifier.INSPIRATION -> {
            polyline(x, y, 12f to 7.5f, 12f to 13.5f)
            drawCircle(Ink, radius = 1f * SCALE, center = at(x, y, 12f, 16.5f))
        }
        Signifier.EXPLORE -> {
            val start = at(x, y, 6.5f, 12f)
            val end = at(x, y, 17.5f, 12f)
            val up = at(x, y, 12f, 7.5f)
            val down = at(x, y, 12f, 16.5f)
            drawPath(
                Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(up.x, up.y, end.x, end.y)
                    quadraticTo(down.x, down.y, start.x, start.y)
                },
                Ink,
                style = glyphStroke,
            )
            drawCircle(Ink, radius = 1.5f * SCALE, center = at(x, y, 12f, 12f))
        }
    }
}

/** Where a migrated or scheduled task's copy landed, or null once it no longer exists. */
private fun wentToText(place: Place?, journal: Journal): String? = when (place) {
    null -> null
    is Place.Daily -> S.wentToDay(place.date)
    is Place.Monthly -> S.wentToMonth(place.month)
    is Place.Future -> S.wentToFuture(place.month, place.day)
    is Place.InCollection -> journal.collections.find { it.id == place.id }?.title
}
