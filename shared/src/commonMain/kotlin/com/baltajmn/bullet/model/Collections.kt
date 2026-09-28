package com.baltajmn.bullet.model

import com.baltajmn.bullet.data.fold
import kotlinx.datetime.YearMonth

/**
 * The Index and the collections (docs/tecnico.md 6.7). `createCollection` and `deleteCollection` land
 * with #30, and the threading and trackers of 6.18 with #50 and #63.
 */

const val COLLECTION_TITLE_MAX = 60

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

    val collections = this.collections.map {
        IndexItem.Collection(it.id, it.title, it.createdAt, it.kind, it.archived)
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
