package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.ENTRY_ORDER
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Search over the loaded [Journal], in memory: no network, no background index and no second store
 * (docs/tecnico.md 6.8). FTS5 is not in the system SQLite of most Androids, and a second store would
 * break "un solo fichero es la fuente de verdad".
 */

/**
 * Lowercase without diacritics, so "cafe" finds "Café" and "strasse" finds "Straße". Copied from
 * `line/.../model/Text.kt`: no `java.text.Normalizer` in common code, and a table of the letters the
 * five languages actually use is smaller than carrying an ICU.
 */
fun fold(s: String): String {
    val out = StringBuilder(s.length)
    for (c in s.lowercase()) {
        // Decomposed accents arrive from pastes and some keyboards: the letter stays, the mark goes.
        if (c.code in 0x300..0x36F) continue
        when (c) {
            'á', 'à', 'â', 'ã', 'ä', 'å' -> out.append('a')
            'é', 'è', 'ê', 'ë' -> out.append('e')
            'í', 'ì', 'î', 'ï' -> out.append('i')
            'ó', 'ò', 'ô', 'õ', 'ö' -> out.append('o')
            'ú', 'ù', 'û', 'ü' -> out.append('u')
            'ý', 'ÿ' -> out.append('y')
            'ç' -> out.append('c')
            'ñ' -> out.append('n')
            'ß' -> out.append("ss")
            'œ' -> out.append("oe")
            'æ' -> out.append("ae")
            else -> out.append(c)
        }
    }
    return out.toString()
}

/**
 * A tag is `#` and then letters, digits, `_` or `-`, up to the first character that is not one
 * (docs/tecnico.md 6.8). Folded and without the `#`, so `#Viaje` and `#viaje` are the same tag. There
 * is no tag manager and no screen of their own: a tag is a word someone typed.
 */
fun tags(text: String): Set<String> {
    val found = mutableSetOf<String>()
    var i = 0
    while (i < text.length) {
        if (text[i] != '#') {
            i++
            continue
        }
        var end = i + 1
        while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_' || text[end] == '-')) end++
        if (end > i + 1) found += fold(text.substring(i + 1, end))
        i = end
    }
    return found
}

/** The one touch filters of docs/pantallas.md 12, combined with AND. */
enum class SearchFilter { OPEN, PRIORITY, INSPIRATION, EXPLORE }

/** Where a group of results came from (docs/tecnico.md 6.8). A Daily and its month's calendar line are different pages. */
sealed interface GroupKey {
    data class Day(val date: LocalDate) : GroupKey
    data class Month(val m: YearMonth) : GroupKey
    data class FutureMonth(val m: YearMonth) : GroupKey
    data class InCollection(val id: String) : GroupKey
}

data class SearchGroup(val place: GroupKey, val entries: List<Entry>)

/**
 * Searches the loaded diary (docs/tecnico.md 6.8): no network, no background index and no second
 * store, because "un solo fichero es la fuente de verdad" and an index would be a second one.
 *
 * An empty query with filters is everything those filters allow; an empty query with no filters is
 * nothing, because the screen has its own thing to say then.
 */
fun search(j: Journal, query: String, filters: Set<SearchFilter>): List<SearchGroup> {
    val q = fold(query.trim())
    if (q.isEmpty() && filters.isEmpty()) return emptyList()

    val matching = j.entries.filter { e ->
        !e.gone && matchesQuery(e, q) && filters.all { it.allows(e) }
    }

    val groups = matching.groupBy { groupOf(it) }
    val dated = groups.keys.filterNot { it is GroupKey.InCollection }
    val collections = groups.keys.filterIsInstance<GroupKey.InCollection>()

    // Newest page first; the collections after every dated page, by when each was started.
    val byDate = dated.sortedByDescending { dateOf(it) }
    val byCreation = collections.sortedByDescending { key -> j.collections.find { it.id == key.id }?.createdAt ?: 0L }

    return (byDate + byCreation).map { key -> SearchGroup(key, groups.getValue(key).sortedWith(ENTRY_ORDER)) }
}

/** `#algo` looks in [tags]; anything else in the folded text (docs/tecnico.md 6.8). */
private fun matchesQuery(e: Entry, foldedQuery: String): Boolean = when {
    foldedQuery.isEmpty() -> true
    foldedQuery.startsWith("#") -> foldedQuery.drop(1) in tags(e.text)
    else -> fold(e.text).contains(foldedQuery)
}

private fun SearchFilter.allows(e: Entry): Boolean = when (this) {
    // Open is an open task: done, migrated, scheduled and discarded are all out (docs/pantallas.md 12).
    SearchFilter.OPEN -> e.bullet == Bullet.TASK && e.status == TaskStatus.OPEN
    SearchFilter.PRIORITY -> Signifier.PRIORITY in e.signifiers
    SearchFilter.INSPIRATION -> Signifier.INSPIRATION in e.signifiers
    SearchFilter.EXPLORE -> Signifier.EXPLORE in e.signifiers
}

private fun groupOf(e: Entry): GroupKey = when (val p = e.place) {
    is Place.Daily -> GroupKey.Day(p.date)
    is Place.Monthly -> GroupKey.Month(p.month)
    is Place.Future -> GroupKey.FutureMonth(p.month)
    is Place.InCollection -> GroupKey.InCollection(p.id)
}

/** The day a dated group sorts by: a month is its first day, so a day of it comes out above its month. */
private fun dateOf(key: GroupKey): LocalDate = when (key) {
    is GroupKey.Day -> key.date
    is GroupKey.Month -> LocalDate(key.m.year, key.m.month, 1)
    is GroupKey.FutureMonth -> LocalDate(key.m.year, key.m.month, 1)
    is GroupKey.InCollection -> LocalDate(1, 1, 1)
}
