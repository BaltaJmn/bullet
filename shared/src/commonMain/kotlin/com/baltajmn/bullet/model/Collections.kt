package com.baltajmn.bullet.model

import com.baltajmn.bullet.data.fold
import kotlinx.datetime.YearMonth

/** The Index, the collections and the trackers (docs/tecnico.md 6.7, 6.18). */

const val COLLECTION_TITLE_MAX = 60

/** Trackers without Pro (docs/tecnico.md 6.18): counted by thread, so a new month is never a new tracker. */
const val FREE_TRACKER_LIMIT = 1

/**
 * A row of the Index: a month with content, or a collection (docs/tecnico.md 6.7). A Daily Log is never
 * one of these, and the Future Log does not make a month: the Index is the list of pages that exist,
 * and neither of those is a page someone named.
 */
sealed interface IndexItem {
    val title: String

    /** What the Index sorts by, ascending and never alphabetically: the order the pages were started in. */
    val createdAt: Long

    data class Month(val month: YearMonth, override val title: String, override val createdAt: Long) : IndexItem

    data class Collection(
        val id: String,
        override val title: String,
        override val createdAt: Long,
        val kind: CollectionKind = CollectionKind.NOTES,
        val archived: Boolean = false,
    ) : IndexItem
}

/**
 * The Index in order (docs/tecnico.md 6.7): months with content and collections by their own moment of
 * creation, and the archived collections last, which is where the screen folds them away.
 *
 * A month's moment is the oldest `createdAt` of its entries, so a month sits in the list where it was
 * actually started, not where its name falls in the year. [monthTitle] comes from the caller because
 * the device language is a platform thing and `model/` does not read one.
 */
fun Journal.indexItems(monthTitle: (YearMonth) -> String): List<IndexItem> {
    val months = entries.filter { !it.gone }
        .mapNotNull { e ->
            when (val p = e.place) {
                is Place.Daily -> monthOf(p.date) to e.createdAt
                is Place.Monthly -> p.month to e.createdAt
                else -> null
            }
        }
        .groupBy({ it.first }, { it.second })
        .map { (month, times) -> IndexItem.Month(month, monthTitle(month), times.min()) }

    // A tracker is one row however many months it has: its newest page, in the place its first one started.
    val tails = trackerTails().mapTo(mutableSetOf()) { it.id }
    val collections = this.collections
        .filter { it.kind != CollectionKind.TRACKER || it.id in tails }
        .map {
            val started = if (it.kind == CollectionKind.TRACKER) trackerThread(it.id).first().createdAt else it.createdAt
            IndexItem.Collection(it.id, it.title, started, it.kind, it.archived)
        }

    val (archived, active) = (months + collections).partition { it is IndexItem.Collection && it.archived }
    return active.sortedBy { it.createdAt } + archived.sortedBy { it.createdAt }
}

/** The Index filter (docs/tecnico.md 6.7): by title, without telling case or accents apart. An empty query filters nothing. */
fun filterIndex(items: List<IndexItem>, query: String): List<IndexItem> {
    val q = fold(query.trim())
    return if (q.isEmpty()) items else items.filter { fold(it.title).contains(q) }
}

/**
 * Renames a collection (docs/tecnico.md 6.7): one line, capped at [COLLECTION_TITLE_MAX], and an empty
 * title changes nothing. It never touches `createdAt`, so the Index does not reshuffle: a page keeps
 * the place it was started in however often it is renamed.
 */
fun Journal.renameCollection(id: String, title: String, now: Long): Journal? {
    val clean = title.oneLine().trim().clampCodePoints(COLLECTION_TITLE_MAX)
    if (clean.isEmpty()) return null
    val target = collections.find { it.id == id } ?: return null
    if (target.title == clean) return null
    return copy(collections = collections.map { if (it.id == id) it.copy(title = clean, updatedAt = now) else it })
}

/**
 * Archives a collection or takes it out of the archive (docs/tecnico.md 6.7). It touches no entry at
 * all: archiving moves the page to the folded block of the Index and nothing else. An archived
 * collection is still read and written, but it is not a destination for a migration (6.4).
 */
fun Journal.archiveCollection(id: String, archived: Boolean, now: Long): Journal {
    val target = collections.find { it.id == id } ?: return this
    if (target.archived == archived) return this
    return copy(collections = collections.map { if (it.id == id) it.copy(archived = archived, updatedAt = now) else it })
}

/**
 * Creates a collection (docs/tecnico.md 6.7). It asks for the title and nothing else: no template, no
 * fields, no kind to pick beyond notes or tracker. An empty title creates nothing.
 */
fun Journal.createCollection(
    title: String,
    now: Long,
    newId: String,
    kind: CollectionKind = CollectionKind.NOTES,
    month: YearMonth? = null,
): Journal? {
    val clean = title.oneLine().trim().clampCodePoints(COLLECTION_TITLE_MAX)
    if (clean.isEmpty()) return null
    val made = BulletCollection(id = newId, title = clean, createdAt = now, kind = kind, month = month.takeIf { kind == CollectionKind.TRACKER })
    return copy(collections = collections + made)
}

/**
 * Deletes a collection and its entries (docs/tecnico.md 6.7). Each entry goes through [Journal.delete]
 * one at a time, so an entry that is the original of a migration leaves its skeleton behind and the copy
 * that landed elsewhere keeps its link (6.4).
 *
 * A collection that continued this one in a thread hangs from whatever this one hung from: the thread
 * joins over the hole instead of breaking in two.
 */
fun Journal.deleteCollection(id: String, now: Long): Journal {
    val target = collections.find { it.id == id } ?: return this
    val mine = entries.filter { it.place == Place.InCollection(id) && !it.gone }.map { it.id }
    var next = this
    for (entryId in mine) next = next.delete(entryId, now) ?: next
    return next.copy(
        collections = next.collections
            .filterNot { it.id == id }
            .map { if (it.threadFrom == id) it.copy(threadFrom = target.threadFrom, updatedAt = now) else it },
    )
}

/**
 * Puts the collection itself back after [deleteCollection], for the Deshacer of docs/tecnico.md 6.7.
 * Its entries come back one at a time through [Journal.restoreDeleted]: nothing in `model/` takes a
 * list of entries, so turning one gesture into its several writes is the repository's job, not a
 * signature anyone could call with a list of their own.
 */
fun Journal.restoreCollection(collection: BulletCollection): Journal =
    if (collections.any { it.id == collection.id }) this else copy(collections = collections + collection)

/** The pages of the tracker [id] belongs to, oldest first (docs/tecnico.md 6.18). */
fun Journal.trackerThread(id: String): List<BulletCollection> {
    val trackers = collections.filter { it.kind == CollectionKind.TRACKER }
    val byId = trackers.associateBy { it.id }
    var head = byId[id] ?: return emptyList()
    val seen = mutableSetOf(head.id)
    while (true) {
        val previous = head.threadFrom?.let(byId::get) ?: break
        if (!seen.add(previous.id)) break
        head = previous
    }
    val continuedBy = trackers.filter { it.threadFrom != null }.associateBy { it.threadFrom }
    val thread = mutableListOf(head)
    while (true) {
        val next = continuedBy[thread.last().id] ?: break
        if (next in thread) break
        thread += next
    }
    return thread
}

/** Each tracker's newest page, the one no other page continues: what the Index shows (docs/tecnico.md 6.18). */
fun Journal.trackerTails(): List<BulletCollection> {
    val continued = collections.filter { it.kind == CollectionKind.TRACKER }.mapNotNullTo(mutableSetOf()) { it.threadFrom }
    return collections.filter { it.kind == CollectionKind.TRACKER && it.id !in continued }
}

/** Trackers that exist now, archived ones included: archiving frees no room, deleting does. */
fun trackerCount(j: Journal): Int = j.trackerTails().size

fun canCreateTracker(j: Journal, isPro: Boolean): Boolean = isPro || trackerCount(j) < FREE_TRACKER_LIMIT

/**
 * [month]'s page of the tracker [tail] ends, or a blank one continuing it: same title and rows (same
 * ids), no marks, and an empty id because it is not in the diary. It is saved by the first mark or the
 * first change of rows, so a month nobody touched leaves no page behind, and last month's marks are
 * never copied (docs/tecnico.md 6.18).
 */
fun trackerPage(j: Journal, tail: BulletCollection, month: YearMonth): BulletCollection =
    j.trackerThread(tail.id).find { it.month == month }
        ?: tail.copy(
            id = "",
            createdAt = 0,
            updatedAt = 0,
            threadFrom = tail.id,
            month = month,
            rows = tail.rows.map { it.copy(days = emptySet()) },
        )

/** Saves [page] under [newId] if it is the blank of [trackerPage]. Returns the diary and the page's id. */
fun Journal.withTrackerPage(page: BulletCollection, newId: String, now: Long): Pair<Journal, String> =
    if (page.id.isNotEmpty()) {
        this to page.id
    } else {
        copy(collections = collections + page.copy(id = newId, createdAt = now, updatedAt = now)) to newId
    }

/** One tap on a cell: the day is marked, or no longer is. Outside the page's month it does nothing. */
fun Journal.toggleTrackerDay(pageId: String, rowId: String, day: Int, now: Long): Journal? = updatePage(pageId, now) { page ->
    val month = page.month ?: return@updatePage null
    if (day !in 1..monthDays(month) || page.rows.none { it.id == rowId }) return@updatePage null
    page.copy(rows = page.rows.map { if (it.id == rowId) it.copy(days = if (day in it.days) it.days - day else it.days + day) else it })
}

/** A new row at the end, from the capture row of docs/pantallas.md 10.2: no prefixes, one line. */
fun Journal.addTrackerRow(pageId: String, title: String, rowId: String, now: Long): Journal? = updatePage(pageId, now) { page ->
    val clean = title.oneLine().trim().clampCodePoints(COLLECTION_TITLE_MAX)
    if (clean.isEmpty()) null else page.copy(rows = page.rows + TrackerRow(rowId, clean))
}

fun Journal.renameTrackerRow(pageId: String, rowId: String, title: String, now: Long): Journal? = updatePage(pageId, now) { page ->
    val clean = title.oneLine().trim().clampCodePoints(COLLECTION_TITLE_MAX)
    if (clean.isEmpty() || page.rows.none { it.id == rowId && it.title != clean }) null
    else page.copy(rows = page.rows.map { if (it.id == rowId) it.copy(title = clean) else it })
}

fun Journal.deleteTrackerRow(pageId: String, rowId: String, now: Long): Journal? = updatePage(pageId, now) { page ->
    if (page.rows.none { it.id == rowId }) null else page.copy(rows = page.rows.filterNot { it.id == rowId })
}

/** Gesture 4 on the rows (docs/pantallas.md 4): the one row that was dragged, to its new place. */
fun Journal.moveTrackerRow(pageId: String, rowId: String, to: Int, now: Long): Journal? = updatePage(pageId, now) { page ->
    val from = page.rows.indexOfFirst { it.id == rowId }
    if (from < 0 || to !in page.rows.indices || to == from) null
    else page.copy(rows = page.rows.toMutableList().apply { add(to, removeAt(from)) })
}

/** Puts [row] back at [index] of its page, for the Deshacer of a deleted row (docs/pantallas.md 5.8). */
fun Journal.restoreTrackerRow(pageId: String, row: TrackerRow, index: Int, now: Long): Journal =
    updatePage(pageId, now) { page ->
        if (page.rows.any { it.id == row.id }) null
        else page.copy(rows = page.rows.toMutableList().apply { add(index.coerceIn(0, size), row) })
    } ?: this

private fun Journal.updatePage(pageId: String, now: Long, change: (BulletCollection) -> BulletCollection?): Journal? {
    val page = collections.find { it.id == pageId && it.kind == CollectionKind.TRACKER } ?: return null
    val changed = change(page) ?: return null
    return copy(collections = collections.map { if (it.id == pageId) changed.copy(updatedAt = now) else it })
}
