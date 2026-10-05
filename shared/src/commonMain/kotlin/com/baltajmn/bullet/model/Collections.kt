package com.baltajmn.bullet.model

import com.baltajmn.bullet.data.fold
import kotlinx.datetime.LocalDate
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

    /** One of [INDEX_ICONS], or null for the page's own kind of icon (12.9). */
    val icon: String?

    /** A free label of one line the Index can filter and group by (12.9). */
    val theme: String?

    data class Month(
        val month: YearMonth,
        override val title: String,
        override val createdAt: Long,
        override val icon: String? = null,
        override val theme: String? = null,
    ) : IndexItem

    data class Collection(
        val id: String,
        override val title: String,
        override val createdAt: Long,
        val kind: CollectionKind = CollectionKind.NOTES,
        val archived: Boolean = false,
        /** It continues an earlier list of its block, so the Index paints it under that one (12.1). */
        val threaded: Boolean = false,
        override val icon: String? = null,
        override val theme: String? = null,
    ) : IndexItem
}

/** The method's own index marks (docs/tecnico.md 12.9), drawn like the glyphs. The order is [com.baltajmn.bullet.i18n.S.iconName]'s. */
val INDEX_ICONS = listOf("star", "heart", "book", "home", "work", "travel", "money", "health", "food", "music", "idea", "people")

const val THEME_MAX = 24

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
        .map { (month, times) ->
            val mark = this.months[month.toString()]
            IndexItem.Month(month, monthTitle(month), times.min(), mark?.icon, mark?.theme)
        }

    // A tracker is one row however many months it has: its newest page, in the place its first one started.
    val tails = trackerTails().mapTo(mutableSetOf()) { it.id }
    val byId = collections.associateBy { it.id }
    // A list that continues another of the same block goes where that thread started, right under the
    // list before it (12.1). Archived and active are two blocks, so a thread is only joined inside one.
    val position = mutableMapOf<String, Pair<Long, Int>>()
    val collections = this.collections
        .filter { it.kind != CollectionKind.TRACKER || it.id in tails }
        .map {
            if (it.kind == CollectionKind.TRACKER) {
                IndexItem.Collection(it.id, it.title, trackerThread(it.id).first().createdAt, it.kind, it.archived, icon = it.icon, theme = it.theme)
            } else {
                var root = it
                var steps = 0
                while (true) {
                    val previous = root.threadFrom?.let(byId::get) ?: break
                    if (previous.kind != CollectionKind.NOTES || previous.archived != it.archived || steps > byId.size) break
                    root = previous
                    steps++
                }
                position[it.id] = root.createdAt to steps
                IndexItem.Collection(it.id, it.title, it.createdAt, it.kind, it.archived, threaded = steps > 0, icon = it.icon, theme = it.theme)
            }
        }

    val order = compareBy<IndexItem>({ (it as? IndexItem.Collection)?.let { c -> position[c.id]?.first } ?: it.createdAt }, { (it as? IndexItem.Collection)?.let { c -> position[c.id]?.second } ?: 0 })
    val (archived, active) = (months + collections).partition { it is IndexItem.Collection && it.archived }
    return active.sortedWith(order) + archived.sortedWith(order)
}

/**
 * A new list continuing [id] (docs/tecnico.md 12.1): its own title, `threadFrom = id`, and nothing else.
 * No entry is created or moved, so a thread is only a link between two pages. Only a list continues
 * this way: a tracker already continues itself one month at a time (6.18). Null when [id] is not a list,
 * already has a continuation, or the title is empty.
 */
fun Journal.continueCollection(id: String, title: String, now: Long, newId: String): Journal? {
    val from = collections.find { it.id == id && it.kind == CollectionKind.NOTES } ?: return null
    if (threadNext(from) != null) return null
    val made = createCollection(title, now, newId) ?: return null
    return made.copy(collections = made.collections.map { if (it.id == newId) it.copy(threadFrom = id) else it })
}

/** The list [c] continues, archived or not (docs/tecnico.md 12.1). */
fun Journal.threadPrev(c: BulletCollection): BulletCollection? =
    c.threadFrom?.let { from -> collections.find { it.id == from && it.kind == CollectionKind.NOTES } }

/** The list that continues [c], archived or not (docs/tecnico.md 12.1). */
fun Journal.threadNext(c: BulletCollection): BulletCollection? =
    if (c.kind != CollectionKind.NOTES) null else collections.find { it.threadFrom == c.id && it.kind == CollectionKind.NOTES }

/** A theme as it is kept: one line, trimmed, at most [THEME_MAX] code points, and null when empty (12.9). */
fun cleanTheme(theme: String?): String? = theme?.oneLine()?.trim()?.clampCodePoints(THEME_MAX)?.ifEmpty { null }

/**
 * The icon and the theme of a collection (docs/tecnico.md 12.9). A tracker's go on every page of its
 * thread, so next month's page, which is a copy of the newest, keeps them. Null when nothing changes.
 */
fun Journal.markCollection(id: String, icon: String?, theme: String?, now: Long): Journal? {
    val target = collections.find { it.id == id } ?: return null
    val i = icon?.takeIf { it in INDEX_ICONS }
    val t = cleanTheme(theme)
    val ids = if (target.kind == CollectionKind.TRACKER) trackerThread(id).mapTo(mutableSetOf()) { it.id } else setOf(id)
    if (collections.none { it.id in ids && (it.icon != i || it.theme != t) }) return null
    return copy(collections = collections.map { if (it.id in ids) it.copy(icon = i, theme = t, updatedAt = now) else it })
}

/** The same for a month of the Index, kept in `months["yyyy-MM"]` and dropped when both are empty. */
fun Journal.markMonth(month: YearMonth, icon: String?, theme: String?): Journal? {
    val mark = IndexMark(icon?.takeIf { it in INDEX_ICONS }, cleanTheme(theme))
    val key = month.toString()
    if ((months[key] ?: IndexMark()) == mark) return null
    return copy(months = if (mark == IndexMark()) months - key else months + (key to mark))
}

/**
 * The Index by theme (docs/tecnico.md 12.9): one group per theme in the order the themes first appear,
 * the pages without one last, and the creation order of [items] kept inside each group.
 */
fun groupByTheme(items: List<IndexItem>): List<Pair<String?, List<IndexItem>>> {
    val (themed, plain) = items.partition { it.theme != null }
    val groups = themed.groupBy { it.theme }.toList()
    return if (plain.isEmpty()) groups else groups + (null to plain)
}

/**
 * A past month whose tasks were all decided and at least one of them migrated (docs/tecnico.md 12.9):
 * the Index underlines it, as a page crossed through on paper.
 */
fun Journal.monthMigrated(m: YearMonth, today: LocalDate): Boolean =
    m < monthOf(today) && openTasksOfMonth(m).isEmpty() && entries.any { e ->
        !e.gone && e.status == TaskStatus.MIGRATED && when (val p = e.place) {
            is Place.Daily -> monthOf(p.date) == m
            is Place.Monthly -> p.month == m
            else -> false
        }
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
