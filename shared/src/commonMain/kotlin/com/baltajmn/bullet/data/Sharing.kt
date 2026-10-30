package com.baltajmn.bullet.data

import androidx.compose.ui.graphics.ImageBitmap
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
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

    /** A sent list as a file named [name] (docs/tecnico.md 12.11), for AirDrop, Quick Share or a chat. */
    fun shareFile(name: String, mime: String, bytes: ByteArray)
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

/** The two shapes a shared page comes in (docs/tecnico.md 12.12): a post, 4:5, and a story, 9:16. */
enum class ShareFormat(val height: Int) { POST(1350), STORY(1920) }

/** A tracker's month as it is shared (docs/tecnico.md 12.12): its title, the month and each row's marks. */
data class TrackerShare(val title: String, val month: YearMonth, val rows: List<Pair<String, Set<Int>>>)

fun trackerShare(page: BulletCollection, month: YearMonth): TrackerShare =
    TrackerShare(page.title, page.month ?: month, page.rows.map { it.title to it.days })

/** Where a tracker's rows start on the image: under the title, the month and the line of day numbers. */
const val TRACKER_TOP = 324

/** The rows of a tracker on each image of [format]: one 54 px row each, never split. */
fun trackerPages(rows: Int, format: ShareFormat): List<IntRange> {
    val perPage = (format.height - 162 - TRACKER_TOP) / 54
    return (0 until rows).chunked(perPage).map { it.first()..it.last() }
}

/** `Agua, octubre de 2026` and a line per row with the days it has, then the name (12.12). */
fun trackerShareText(t: TrackerShare): String = buildString {
    appendLine("${t.title}, ${S.monthYear(t.month)}")
    t.rows.forEach { (title, days) ->
        appendLine(if (days.isEmpty()) title else "$title: " + days.sorted().joinToString(", "))
    }
    appendLine()
    append(S.appName)
}

fun collectionShare(j: Journal, id: String) = ShareContent(
    j.collections.find { it.id == id }?.title.orEmpty(),
    j.entriesAt(Place.InCollection(id)).map { ShareRow.Line(it) },
)

/**
 * An entry in the ASCII of docs/tecnico.md 4.4, without the `- ` of a list item: signifiers, the
 * bullet's symbol and the text. An irrelevant task goes between `~~`. [before] goes right before the
 * symbol, as the `(14)` of a Future Log entry with a day.
 */
fun asciiEntry(e: Entry, before: String? = null): String {
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
    return (marks + listOfNotNull(before) + body).joinToString(" ")
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
