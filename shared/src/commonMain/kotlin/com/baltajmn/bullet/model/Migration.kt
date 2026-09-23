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
 * nothing, when [to] is the entry's own place, an earlier day or month than [today], an archived
 * collection, or the entry is not an open task (docs/tecnico.md 6.4).
 */
fun Journal.migrate(id: String, to: Place, today: LocalDate, now: Long, newId: String): Journal? {
    val original = entries.find { it.id == id && !it.gone } ?: return null
    if (original.place == to) return null
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
    if (day != null && day !in 1..monthDays(month)) return null
    val to = Place.Future(month, day)
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
fun Journal.ofDay(d: LocalDate): List<Entry> {
    val daily = entries.filter { !it.gone && it.place == Place.Daily(d) }.sortedWith(ENTRY_ORDER)
    val calendarLine = entries.filter { !it.gone && it.place == Place.Monthly(monthOf(d), d.day) }.sortedWith(ENTRY_ORDER)
    return daily + calendarLine
}

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
