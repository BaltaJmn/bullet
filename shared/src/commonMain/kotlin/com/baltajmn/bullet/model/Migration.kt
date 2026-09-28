package com.baltajmn.bullet.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.monthsUntil

/** From the second migration on, the review says "migrated N times" (SPEC 2.10). */
const val MIGRATION_SHOWN_FROM = 2

/** Future blocks shown at once, and how far "ver más" can reach (docs/pantallas.md). */
const val FUTURE_MONTHS = 6
const val FUTURE_MONTHS_MAX = 24

/**
 * Migrates a single TASK: the original becomes MIGRATED and a copy with [newId] opens at [to],
 * linked back by [from]. An EVENT or a NOTE has no "migrated" status to leave behind, so it is
 * moved in place instead: only the Future Log review uses that branch (6.5). Returns null, changing
 * nothing, when [to] is the entry's own place, a day its month does not have ([dayExists]), an
 * earlier day or month than [today], an archived collection, or the entry is not an open task
 * (docs/tecnico.md 6.4).
 */
fun Journal.migrate(id: String, to: Place, today: LocalDate, now: Long, newId: String): Journal? {
    val original = entries.find { it.id == id && !it.gone } ?: return null
    if (original.place == to) return null
    if (!to.dayExists) return null
    if (to is Place.Daily && to.date < today) return null
    if (to is Place.Monthly && to.month < monthOf(today)) return null
    if (to is Place.InCollection && collections.find { it.id == to.id }?.archived == true) return null

    if (original.bullet != Bullet.TASK) {
        val moved = original.copy(place = to, order = nextOrder(to), updatedAt = now)
        return copy(entries = entries.map { if (it.id == id) moved else it })
    }
    if (original.status != TaskStatus.OPEN) return null

    val migrated = original.copy(status = TaskStatus.MIGRATED, updatedAt = now)
    val landed = original.copy(
        id = newId,
        status = TaskStatus.OPEN,
        place = to,
        order = nextOrder(to),
        createdAt = now,
        updatedAt = now,
        from = id,
        gone = false,
    )
    return copy(entries = entries.map { if (it.id == id) migrated else it } + landed)
}

/**
 * Like [migrate] towards `Place.Future(month, day)`, with the original left `SCHEDULED`. Returns
 * null when [month] is not after [today]'s month, is more than [FUTURE_MONTHS_MAX] away, or [day]
 * does not fit that month.
 */
fun Journal.schedule(id: String, month: YearMonth, day: Int?, today: LocalDate, now: Long, newId: String): Journal? {
    val original = entries.find { it.id == id && !it.gone } ?: return null
    if (original.bullet != Bullet.TASK || original.status != TaskStatus.OPEN) return null
    val currentMonth = monthOf(today)
    if (month <= currentMonth || currentMonth.monthsUntil(month) > FUTURE_MONTHS_MAX) return null
    val to = Place.Future(month, day)
    if (!to.dayExists) return null
    if (original.place == to) return null

    val scheduled = original.copy(status = TaskStatus.SCHEDULED, updatedAt = now)
    val landed = original.copy(
        id = newId,
        status = TaskStatus.OPEN,
        place = to,
        order = nextOrder(to),
        createdAt = now,
        updatedAt = now,
        from = id,
        gone = false,
    )
    return copy(entries = entries.map { if (it.id == id) scheduled else it } + landed)
}

/** Marks an open task IRRELEVANT. No copy: an event or a note is discarded by deleting it (#26). */
fun Journal.discard(id: String, now: Long): Journal? {
    val original = entries.find { it.id == id && !it.gone } ?: return null
    if (original.bullet != Bullet.TASK || original.status != TaskStatus.OPEN) return null
    return copy(entries = entries.map { if (it.id == id) it.copy(status = TaskStatus.IRRELEVANT, updatedAt = now) else it })
}

/**
 * Walks [Entry.from] back from [id] and counts the jumps. A `from` pointing at a missing id still
 * counts once before stopping, and a cycle (a hand-edited file) stops instead of looping forever.
 */
fun Journal.migrationCount(id: String): Int {
    val byId = entries.associateBy { it.id }
    var count = 0
    var currentId = id
    val seen = mutableSetOf(id)
    while (true) {
        val from = byId[currentId]?.from ?: return count
        count++
        if (from in seen) return count
        seen += from
        currentId = from
    }
}

/**
 * Everything that belongs to [d] (docs/tecnico.md 6.5): its Daily Log first, then the calendar
 * line of that day in its Monthly Log. Hoy (#21), the widget (6.13) and the day review (6.6) all
 * read a day through this one function instead of querying [Place] shapes on their own.
 */
fun Journal.ofDay(d: LocalDate): List<Entry> = entriesAt(Place.Daily(d)) + entriesAt(Place.Monthly(monthOf(d), d.day))

/** OPEN tasks of [ofDay]. */
fun Journal.openTasksOfDay(d: LocalDate): List<Entry> = ofDay(d).filter { it.bullet == Bullet.TASK && it.status == TaskStatus.OPEN }

/** Open tasks on days of [monthOf] `d` that come before `d`: Daily(d') and the calendar line Monthly(m, day'). */
fun Journal.openTasksBefore(d: LocalDate): List<Entry> {
    val m = monthOf(d)
    return entries.filter { e ->
        !e.gone && e.bullet == Bullet.TASK && e.status == TaskStatus.OPEN &&
            when (val p = e.place) {
                is Place.Daily -> monthOf(p.date) == m && p.date < d
                is Place.Monthly -> p.month == m && p.day != null && LocalDate(m.year, m.month, p.day) < d
                else -> false
            }
    }
}

/** Open tasks anywhere in [m]: every Daily of that month plus every Monthly(m, *), with or without a day. */
fun Journal.openTasksOfMonth(m: YearMonth): List<Entry> = entries.filter { e ->
    !e.gone && e.bullet == Bullet.TASK && e.status == TaskStatus.OPEN &&
        when (val p = e.place) {
            is Place.Daily -> monthOf(p.date) == m
            is Place.Monthly -> p.month == m
            else -> false
        }
}

/** One past the highest [Entry.order] already at [place], so a landed entry goes to the end. Also used by [Journal.capture] (RapidParse.kt). */
internal fun Journal.nextOrder(place: Place): Int = (entries.filter { it.place == place }.maxOfOrNull { it.order } ?: -1) + 1

/**
 * The "meses con contenido" of docs/tecnico.md 6.7: any non skeleton entry in a `Daily` of the month
 * or in `Monthly(m, *)`. The Future Log does not make a month. Mes stops its back arrow at the
 * earliest one (docs/pantallas.md 7.1).
 */
fun Journal.monthsWithContent(): Set<YearMonth> = entries.mapNotNullTo(mutableSetOf()) { e ->
    when (val p = e.place) {
        is Place.Daily -> monthOf(p.date).takeUnless { e.gone }
        is Place.Monthly -> p.month.takeUnless { e.gone }
        else -> null
    }
}

/**
 * The day [Entry.place] points at inside its own month (docs/tecnico.md 4.1): a `Daily`'s date, or the
 * calendar line of a `Monthly` or a `Future`. Null for a list that has no day, and for a collection,
 * which is not a month.
 */
val Entry.placeDay: Int? get() = when (val p = place) {
    is Place.Daily -> p.date.day
    is Place.Monthly -> p.day
    is Place.Future -> p.day
    is Place.InCollection -> null
}

/**
 * How one month's entries sit on the page (docs/tecnico.md 6.5, 6.6): by day, the ones with no day
 * after every dated one, and then [ENTRY_ORDER]. A Future Log block and a review queue are both this.
 */
val PAGE_ORDER: Comparator<Entry> = compareBy<Entry> { it.placeDay ?: Int.MAX_VALUE }.then(ENTRY_ORDER)

/**
 * One Future Log block (docs/pantallas.md 8, docs/tecnico.md 6.5): the `Future(m, dia)` by day
 * ascending and then the `Future(m, null)` by `order`, which is how Futuro paints a month and how
 * the Future Log review (#26) walks it.
 */
fun Journal.futureBlock(m: YearMonth): List<Entry> = entries
    .filter { !it.gone && it.place.let { p -> p is Place.Future && p.month == m } }
    .sortedWith(PAGE_ORDER)

/** The month of a Future Log entry (docs/tecnico.md 4.1). */
val Entry.futureMonth: YearMonth? get() = (place as? Place.Future)?.month

/**
 * What the Future Log still holds for this month or an earlier one (docs/tecnico.md 6.5): OPEN tasks,
 * and events and notes always, by month and then as [futureBlock] orders a block. Mes counts these in
 * its `futureWaiting` line and the review of #26 walks them one by one. Reading this changes nothing:
 * the line is passive, so a month can pass without anyone opening Mes and no entry moves.
 */
fun Journal.futureWaiting(today: LocalDate): List<Entry> {
    val current = monthOf(today)
    return entries.mapNotNullTo(mutableSetOf()) { it.futureMonth }
        .filter { it <= current }
        .sorted()
        .flatMap { m -> futureBlock(m).filter { it.bullet != Bullet.TASK || it.status == TaskStatus.OPEN } }
}

/**
 * The oldest past month that still has open tasks (docs/tecnico.md 6.4): what Hoy and Mes put in their
 * "sin cerrar" line, and the month a `Month` review closes. The current month is never it: a month is
 * not late until it is over.
 */
fun Journal.unclosedMonth(today: LocalDate): YearMonth? = monthsWithContent()
    .filter { it < monthOf(today) }
    .sorted()
    .firstOrNull { openTasksOfMonth(it).isNotEmpty() }

/**
 * What a review walks (docs/tecnico.md 6.6). Every scope lives inside one month, which is why
 * [PAGE_ORDER] is enough to order a queue: there is no second month to break the tie.
 */
sealed interface ReviewScope {
    /** The whole of [month], from the line "Agosto sin cerrar: N abiertas" of Hoy and Mes. */
    data class Month(val month: YearMonth) : ReviewScope

    /** The days of this month before [today], from the line "Quedan N abiertas de dias anteriores" of Hoy. */
    data class Earlier(val today: LocalDate) : ReviewScope

    /** Those and [today]'s own, from the reminder and from `bobbin://review`. */
    data class Day(val today: LocalDate) : ReviewScope
}

/**
 * The queue of [scope], recomputed from the diary every time (docs/tecnico.md 6.6): each of the five
 * actions changes the task's own state, so a review left half way through simply asks this again and
 * the ones already decided are no longer open. Nothing keeps a progress of its own.
 */
fun Journal.reviewQueue(scope: ReviewScope): List<Entry> = when (scope) {
    is ReviewScope.Month -> openTasksOfMonth(scope.month)
    is ReviewScope.Earlier -> openTasksBefore(scope.today)
    is ReviewScope.Day -> openTasksBefore(scope.today) + openTasksOfDay(scope.today)
}.sortedWith(PAGE_ORDER)
