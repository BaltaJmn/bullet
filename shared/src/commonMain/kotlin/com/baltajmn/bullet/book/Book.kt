package com.baltajmn.bullet.book

import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.trackerThread
import com.baltajmn.bullet.model.yearSummary
import com.baltajmn.bullet.model.yearsWritten
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth

/**
 * The notebook as a book (docs/tecnico.md 12.6, docs/pantallas.md 23.6): A5 on a 17.5 pt grid, laid out
 * here once and drawn by [BookRenderer] page by page. Everything is a [Mark] in points, so both
 * platforms only fill, stroke and write; the measuring of text is theirs, with the same Literata.
 */
const val BOOK_W = 420f
const val BOOK_H = 595f
const val BOOK_UNIT = 17.5f
private const val MARGIN = 2 * BOOK_UNIT
private const val RIGHT = BOOK_W - MARGIN
private const val FIRST_ROW = MARGIN + BOOK_UNIT
private const val LAST_ROW = BOOK_H - MARGIN
private const val INK_SIZE = 12.4f
private const val TITLE_SIZE = 17.5f
private const val LABEL_SIZE = 8.6f
private const val SIGNIFIER_X = MARGIN
private const val GLYPH_X = MARGIN + BOOK_UNIT
private const val TEXT_X = MARGIN + 2 * BOOK_UNIT + 3f

const val BOOK_INK = 0xFF39352EL
const val BOOK_MUTED = 0xFF736D63L
const val BOOK_PAPER = 0xFFFBF8F3L
private const val BOOK_LINE = 0xFFE3DCD1L

/** What a page is made of, in points from its top left. Colors are ARGB. */
sealed interface Mark {
    data class Fill(val color: Long) : Mark
    data class Text(val x: Float, val baseline: Float, val text: String, val size: Float, val color: Long = BOOK_INK, val strike: Boolean = false) : Mark
    data class Line(val points: List<Pair<Float, Float>>, val width: Float, val color: Long = BOOK_INK) : Mark
    data class Dot(val x: Float, val y: Float, val radius: Float, val color: Long = BOOK_INK, val stroke: Float = 0f) : Mark
}

/** The book: its pages, and the paper every page but the cover is drawn on (one list, shared). */
class Book(val pages: List<List<Mark>>, val paper: List<Mark>) {
    val size get() = pages.size
}

/**
 * Lays out the whole diary: the cover in [cover]'s color, the key, the Index with the book's page numbers,
 * each month (its calendar, its tasks and each Daily Log) and each collection in the Index's order, and
 * then a summary of each year as an appendix. [measure] is the width of a text in Literata at a size.
 */
fun layoutBook(j: Journal, cover: Long, paper: String, tz: TimeZone, measure: (String, Float) -> Float): Book {
    val items = j.indexItems(S::monthTitle)
    val months = items.filterIsInstance<IndexItem.Month>().map { it.month }

    // The sections first, without the Index, to know where each one starts.
    val sections = Pager(measure)
    val starts = mutableMapOf<IndexItem, Int>()
    for (item in items) {
        sections.newPage()
        starts[item] = sections.pageCount - 1
        when (item) {
            is IndexItem.Month -> sections.month(j, item.month)
            is IndexItem.Collection -> sections.collection(j, item)
        }
    }
    val years = j.yearsWritten(tz)
    for (year in years) {
        val summary = j.yearSummary(year, tz)
        if (summary.isEmpty) continue
        sections.newPage()
        sections.title(S.yearSummaryTitle(year))
        listOf(S.summaryInspirations to summary.inspirations, S.summaryPriorities to summary.prioritiesDone).forEach { (label, list) ->
            if (list.isNotEmpty()) {
                sections.label(label)
                list.forEach { sections.entry(it) }
            }
        }
        if (summary.mostMigrated.isNotEmpty()) {
            sections.label(S.summaryMostMigrated)
            summary.mostMigrated.forEach { (e, n) -> sections.entry(e, after = S.timesMoved(n)) }
        }
    }

    val key = Pager(measure)
    key.newPage()
    key.key()
    // How many pages the Index takes does not depend on its numbers: one row per page of the diary.
    fun index(offset: Int) = Pager(measure).apply {
        newPage()
        title(S.tabIndex)
        for (item in items) indexLine(item.title, starts.getValue(item) + offset + 1)
    }
    // Cover, key and Index come before the first section; the cover is page 1.
    val offset = 1 + key.pageCount + index(0).pageCount
    val front = key.pages + index(offset).pages

    val coverPage = buildList {
        add(Mark.Fill(cover))
        val title = S.appName
        add(Mark.Text((BOOK_W - measure(title, 35f)) / 2, BOOK_H / 3, title, 35f))
        if (months.isNotEmpty()) {
            val range = S.bookRange(months.min(), months.max())
            add(Mark.Text((BOOK_W - measure(range, 12f)) / 2, BOOK_H / 3 + 2 * BOOK_UNIT, range, 12f, color = 0xB339352EL))
        }
    }
    val pages = listOf(coverPage) + front + sections.pages
    return Book(pages.mapIndexed { i, marks -> if (i == 0) marks else marks + footer(i + 1, measure) }, paperMarks(paper))
}

/** The book's file name, `bobbin-book-AAAA-MM-DD.pdf` (docs/tecnico.md 12.6). */
fun bookName(today: LocalDate) = "bobbin-book-$today.pdf"

private fun footer(n: Int, measure: (String, Float) -> Float): Mark {
    val text = "$n"
    return Mark.Text((BOOK_W - measure(text, 8f)) / 2, BOOK_H - BOOK_UNIT, text, 8f, BOOK_MUTED)
}

/** The active paper on the 17.5 grid: 1.1 pt dots, a rule per row, or rows and columns. */
private fun paperMarks(paper: String): List<Mark> = buildList {
    val rows = (1 until (BOOK_H / BOOK_UNIT).toInt()).map { it * BOOK_UNIT }
    val columns = (1 until (BOOK_W / BOOK_UNIT).toInt()).map { it * BOOK_UNIT }
    when (paper) {
        "dotted" -> rows.forEach { y -> columns.forEach { x -> add(Mark.Dot(x, y, 0.55f, BOOK_LINE)) } }
        "lined" -> rows.forEach { y -> add(Mark.Line(listOf(0f to y, BOOK_W to y), 0.5f, BOOK_LINE)) }
        "grid" -> {
            rows.forEach { y -> add(Mark.Line(listOf(0f to y, BOOK_W to y), 0.5f, BOOK_LINE)) }
            columns.forEach { x -> add(Mark.Line(listOf(x to 0f, x to BOOK_H), 0.5f, BOOK_LINE)) }
        }
    }
}

/** Fills pages row by row on the grid, starting a new one when the next block does not fit. */
private class Pager(private val measure: (String, Float) -> Float) {
    val pages = mutableListOf<MutableList<Mark>>()
    private var y = FIRST_ROW
    val pageCount get() = pages.size

    fun newPage() {
        pages += mutableListOf<Mark>()
        y = FIRST_ROW
    }

    private fun room(rows: Int) {
        if (pages.isEmpty() || y + (rows - 1) * BOOK_UNIT > LAST_ROW) newPage()
    }

    private val page get() = pages.last()

    fun title(text: String) {
        room(3)
        y += BOOK_UNIT
        wrap(text, TITLE_SIZE, RIGHT - MARGIN).forEach { line ->
            page += Mark.Text(MARGIN, y, line, TITLE_SIZE)
            y += 2 * BOOK_UNIT
        }
    }

    fun label(text: String) {
        room(2)
        page += Mark.Text(MARGIN, y, text.uppercase(), LABEL_SIZE, BOOK_MUTED)
        y += BOOK_UNIT
    }

    fun note(text: String) {
        wrap(text, INK_SIZE * 0.8f, RIGHT - MARGIN).forEach { line ->
            room(1)
            page += Mark.Text(MARGIN, y, line, INK_SIZE * 0.8f, BOOK_MUTED)
            y += BOOK_UNIT
        }
    }

    /** One entry: its signifier and glyph in their columns, its text wrapped under itself, as in the app. */
    fun entry(e: Entry, after: String? = null) {
        val closed = e.bullet == Bullet.TASK && e.status != TaskStatus.OPEN
        val lines = wrap(e.text, INK_SIZE, RIGHT - TEXT_X)
        room(lines.size)
        e.signifiers.sortedBy { it.ordinal }.firstOrNull()?.let { page += signifierMarks(it, SIGNIFIER_X, y) }
        page += bulletMarks(e.bullet, e.status, GLYPH_X, y)
        lines.forEachIndexed { i, line ->
            if (i > 0) room(1)
            page += Mark.Text(TEXT_X, y, line, INK_SIZE, if (closed) BOOK_MUTED else BOOK_INK, strike = e.status == TaskStatus.IRRELEVANT)
            y += BOOK_UNIT
        }
        if (after != null) {
            room(1)
            page += Mark.Text(TEXT_X, y - 5f, after, LABEL_SIZE, BOOK_MUTED)
            y += BOOK_UNIT / 2
        }
    }

    fun month(j: Journal, m: YearMonth) {
        title(S.monthTitle(m))
        val calendar = (1..monthDays(m)).map { it to j.entriesAt(Place.Monthly(m, it)) }.filter { it.second.isNotEmpty() }
        if (calendar.isNotEmpty()) {
            label(S.calendarTitle)
            calendar.forEach { (day, entries) ->
                val date = LocalDate(m.year, m.month, day)
                room(2)
                page += Mark.Text(MARGIN, y, "$day ${S.weekdayInitial()[date.dayOfWeek.ordinal]}", LABEL_SIZE, BOOK_MUTED)
                y += BOOK_UNIT
                entries.forEach { entry(it) }
            }
        }
        val tasks = j.entriesAt(Place.Monthly(m))
        if (tasks.isNotEmpty()) {
            label(S.monthTasks)
            tasks.forEach { entry(it) }
        }
        for (day in 1..monthDays(m)) {
            val date = LocalDate(m.year, m.month, day)
            val entries = j.entriesAt(Place.Daily(date))
            if (entries.isEmpty()) continue
            label(S.longDateWithYear(date))
            entries.forEach { entry(it) }
        }
    }

    fun collection(j: Journal, item: IndexItem.Collection) {
        title(item.title)
        if (item.kind == CollectionKind.TRACKER) {
            for (page in j.trackerThread(item.id)) {
                val month = page.month ?: continue
                label(S.trackerSubtitle(month))
                page.rows.forEach { row -> note("${row.title}: ${S.a11yTrackerMarked(row.days.sorted())}") }
            }
        } else {
            j.entriesAt(Place.InCollection(item.id)).forEach { entry(it) }
        }
    }

    /** The key of docs/pantallas.md 13, without the gestures. */
    fun key() {
        title(S.keyTitle)
        fun row(glyph: List<Mark>, name: String, means: String) {
            val lines = wrap(means, LABEL_SIZE + 1, RIGHT - TEXT_X)
            room(1 + lines.size)
            page += glyph.map { it.shiftedTo(y) }
            page += Mark.Text(TEXT_X, y, name, INK_SIZE)
            y += BOOK_UNIT
            lines.forEach {
                page += Mark.Text(TEXT_X, y - 4f, it, LABEL_SIZE + 1, BOOK_MUTED)
                y += BOOK_UNIT * 0.75f
            }
            y = FIRST_ROW + kotlin.math.ceil((y - FIRST_ROW) / BOOK_UNIT) * BOOK_UNIT
        }
        label(S.keyWrite)
        row(bulletMarks(Bullet.TASK, TaskStatus.OPEN, GLYPH_X, 0f), S.bulletTask, S.keyTask)
        row(bulletMarks(Bullet.EVENT, TaskStatus.OPEN, GLYPH_X, 0f), S.bulletEvent, S.keyEvent)
        row(bulletMarks(Bullet.NOTE, TaskStatus.OPEN, GLYPH_X, 0f), S.bulletNote, S.keyNote)
        label(S.keyHappened)
        row(bulletMarks(Bullet.TASK, TaskStatus.DONE, GLYPH_X, 0f), S.stateDone, S.keyDone)
        row(bulletMarks(Bullet.TASK, TaskStatus.MIGRATED, GLYPH_X, 0f), S.movedName, S.keyMoved)
        row(bulletMarks(Bullet.TASK, TaskStatus.SCHEDULED, GLYPH_X, 0f), S.otherMonthName, S.keyOtherMonth)
        row(discardMarks(GLYPH_X, 0f), S.stateDiscarded, S.keyDiscarded)
        label(S.keyMargin)
        Signifier.entries.forEach { s ->
            val (name, means) = when (s) {
                Signifier.PRIORITY -> S.signifierPriority to S.keyPriority
                Signifier.INSPIRATION -> S.signifierInspiration to S.keyInspiration
                Signifier.EXPLORE -> S.signifierExplore to S.keyExplore
            }
            row(signifierMarks(s, GLYPH_X, 0f), name, means)
        }
        note(S.keyPrefixes.replace("^", ""))
    }

    /** A row of the Index: the title, and the page it starts on at the right, where paper has none. */
    fun indexLine(title: String, pageNumber: Int) {
        room(1)
        val number = "$pageNumber"
        val numberX = RIGHT - measure(number, INK_SIZE)
        val fitted = fit(title, INK_SIZE, numberX - MARGIN - BOOK_UNIT)
        page += Mark.Text(MARGIN, y, fitted, INK_SIZE)
        page += Mark.Text(numberX, y, number, INK_SIZE, BOOK_MUTED)
        y += BOOK_UNIT
    }

    /** Greedy, by words; a word longer than the line is cut where it no longer fits. */
    private fun wrap(text: String, size: Float, width: Float): List<String> {
        val lines = mutableListOf<String>()
        var current = ""
        for (word in text.split(' ').filter { it.isNotEmpty() }) {
            val joined = if (current.isEmpty()) word else "$current $word"
            if (measure(joined, size) <= width) {
                current = joined
                continue
            }
            if (current.isNotEmpty()) lines += current
            var rest = word
            while (measure(rest, size) > width && rest.length > 1) {
                var cut = rest.length - 1
                while (cut > 1 && measure(rest.substring(0, cut), size) > width) cut--
                if (rest[cut - 1].isHighSurrogate()) cut--
                lines += rest.substring(0, cut)
                rest = rest.substring(cut)
            }
            current = rest
        }
        if (current.isNotEmpty() || lines.isEmpty()) lines += current
        return lines
    }

    private fun fit(text: String, size: Float, width: Float): String {
        if (measure(text, size) <= width) return text
        var end = text.length
        while (end > 1 && measure(text.substring(0, end) + "...", size) > width) end--
        return text.substring(0, end).trimEnd() + "..."
    }
}

// --- The method's glyphs, with BulletGlyph.kt's geometry scaled from 24 to one grid unit ---------------

private const val SCALE = BOOK_UNIT / 24f
private const val STROKE = 1.5f * SCALE

/** A glyph box sits on its row: its center on the middle of the lowercase letters of the line. */
private fun boxTop(baseline: Float) = baseline - INK_SIZE * 0.35f - BOOK_UNIT / 2

private fun at(x: Float, top: Float, gx: Float, gy: Float) = (x + gx * SCALE) to (top + gy * SCALE)

private fun poly(x: Float, top: Float, vararg points: Pair<Float, Float>) = Mark.Line(points.map { (gx, gy) -> at(x, top, gx, gy) }, STROKE)

private fun dot(x: Float, top: Float, gx: Float, gy: Float, diameter: Float, ring: Boolean = false): Mark {
    val (cx, cy) = at(x, top, gx, gy)
    return Mark.Dot(cx, cy, diameter / 2 * SCALE, stroke = if (ring) STROKE else 0f)
}

private fun bulletMarks(bullet: Bullet, status: TaskStatus, x: Float, baseline: Float): List<Mark> {
    val top = boxTop(baseline)
    return when (bullet) {
        Bullet.EVENT -> listOf(dot(x, top, 12f, 12f, 8f, ring = true))
        Bullet.NOTE -> listOf(poly(x, top, 8f to 12f, 16f to 12f))
        Bullet.TASK -> when (status) {
            TaskStatus.OPEN, TaskStatus.IRRELEVANT -> listOf(dot(x, top, 12f, 12f, 5f))
            TaskStatus.DONE -> listOf(dot(x, top, 12f, 12f, 5f), poly(x, top, 8f to 8f, 16f to 16f), poly(x, top, 16f to 8f, 8f to 16f))
            TaskStatus.MIGRATED -> listOf(poly(x, top, 10f to 8f, 14.5f to 12f, 10f to 16f))
            TaskStatus.SCHEDULED -> listOf(poly(x, top, 14f to 8f, 9.5f to 12f, 14f to 16f))
        }
    }
}

private fun discardMarks(x: Float, baseline: Float): List<Mark> {
    val top = boxTop(baseline)
    return listOf(dot(x, top, 12f, 12f, 5f), poly(x, top, 8f to 12f, 16f to 12f))
}

private fun signifierMarks(s: Signifier, x: Float, baseline: Float): List<Mark> {
    val top = boxTop(baseline)
    return when (s) {
        Signifier.PRIORITY -> listOf(poly(x, top, 12f to 8f, 12f to 16f), poly(x, top, 8.5f to 10f, 15.5f to 14f), poly(x, top, 15.5f to 10f, 8.5f to 14f))
        Signifier.INSPIRATION -> listOf(poly(x, top, 12f to 7.5f, 12f to 13.5f), dot(x, top, 12f, 16.5f, 2f))
        Signifier.EXPLORE -> {
            // The eye's two quadratic curves, sampled: a polyline is all both renderers need to draw.
            fun quad(t: Float, cy: Float) = (6.5f + 11f * t) to ((1 - t) * (1 - t) * 12f + 2 * (1 - t) * t * cy + t * t * 12f)
            val upper = (0..8).map { quad(it / 8f, 7.5f) }
            val lower = (0..8).map { quad(it / 8f, 16.5f) }
            listOf(poly(x, top, *(upper + lower.reversed()).toTypedArray()), dot(x, top, 12f, 12f, 3f))
        }
    }
}

/** The key draws its glyphs at baseline 0 and moves them to the row. */
private fun Mark.shiftedTo(baseline: Float): Mark = when (this) {
    is Mark.Line -> copy(points = points.map { (px, py) -> px to py + baseline })
    is Mark.Dot -> copy(y = y + baseline)
    else -> this
}
