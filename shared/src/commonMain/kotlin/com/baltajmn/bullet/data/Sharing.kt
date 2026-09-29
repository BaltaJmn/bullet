package com.baltajmn.bullet.data

import androidx.compose.ui.graphics.ImageBitmap
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.ofDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** PNG bytes of a page, exactly the bitmap the preview showed. */
expect fun ImageBitmap.encodeToPng(): ByteArray

/** The system share sheet with what was already drawn. Nothing goes to the gallery on its own (docs/tecnico.md 7). */
expect object Sharing {
    fun sharePngs(pngs: List<ByteArray>)
    fun shareText(text: String)
}

/** A row of a shared page: an entry (with its day, on a month's calendar) or the month's task heading. */
sealed interface ShareRow {
    data class Line(val entry: Entry, val day: Int? = null) : ShareRow
    data class Heading(val text: String) : ShareRow
}

/** What the user chose to share (docs/tecnico.md 6.10). The app never suggests it. */
data class ShareContent(val title: String, val rows: List<ShareRow>)

fun dayShare(j: Journal, date: LocalDate) = ShareContent(S.longDateWithYear(date), j.ofDay(date).map { ShareRow.Line(it) })

/** A month is its calendar and its tasks; a day of the calendar with nothing on it is not painted. */
fun monthShare(j: Journal, m: YearMonth): ShareContent {
    val calendar = (1..monthDays(m)).flatMap { day ->
        j.entriesAt(Place.Monthly(m, day)).mapIndexed { i, e -> ShareRow.Line(e, day.takeIf { i == 0 }) }
    }
    val tasks = j.entriesAt(Place.Monthly(m))
    val tasksRows = if (tasks.isEmpty()) emptyList() else listOf(ShareRow.Heading(S.monthTasks)) + tasks.map { ShareRow.Line(it) }
    return ShareContent(S.monthTitle(m), calendar + tasksRows)
}

fun collectionShare(j: Journal, id: String) = ShareContent(
    j.collections.find { it.id == id }?.title.orEmpty(),
    j.entriesAt(Place.InCollection(id)).map { ShareRow.Line(it) },
)

/**
 * An entry in the ASCII of docs/tecnico.md 4.4, without the `- ` of a list item: signifiers, the
 * bullet's symbol and the text. An irrelevant task goes between `~~`.
 */
fun asciiEntry(e: Entry): String {
    val marks = e.signifiers.sortedBy { it.ordinal }.map {
        when (it) {
            Signifier.PRIORITY -> "*"
            Signifier.INSPIRATION -> "!"
            Signifier.EXPLORE -> "?"
        }
    }
    val symbol = when (e.bullet) {
        Bullet.EVENT -> "o"
        Bullet.NOTE -> "-"
        Bullet.TASK -> when (e.status) {
            TaskStatus.OPEN, TaskStatus.IRRELEVANT -> "."
            TaskStatus.DONE -> "x"
            TaskStatus.MIGRATED -> ">"
            TaskStatus.SCHEDULED -> "<"
        }
    }
    val body = if (e.status == TaskStatus.IRRELEVANT) "~~$symbol ${e.text}~~" else "$symbol ${e.text}"
    return (marks + body).joinToString(" ")
}

/** docs/pantallas.md 17.3: the title, a line per row, and the name at the end after a blank line. */
fun shareText(content: ShareContent): String = buildString {
    appendLine(content.title)
    content.rows.forEach { row ->
        when (row) {
            is ShareRow.Heading -> appendLine(row.text)
            is ShareRow.Line -> appendLine((row.day?.let { "($it) " } ?: "") + asciiEntry(row.entry))
        }
    }
    appendLine()
    append(S.appName)
}

/**
 * The rows on each page (docs/tecnico.md 6.10). A row never splits across two: if it does not fit
 * whole in what is left, the next page starts with it. One that does not fit even an empty page goes
 * alone and is cut at the bottom.
 */
fun paginate(heights: List<Int>, pageHeight: Int): List<IntRange> {
    val pages = mutableListOf<IntRange>()
    var start = 0
    var used = 0
    heights.forEachIndexed { i, h ->
        if (i > start && used + h > pageHeight) {
            pages += start until i
            start = i
            used = 0
        }
        used += h
    }
    pages += start until heights.size
    return pages
}
