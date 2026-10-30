package com.baltajmn.bullet.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * One day of the week (docs/tecnico.md 12.13): everything that carries its date, each in the place it
 * lives. The week is a way of looking, not a place: nothing is copied and there is no weekly page.
 */
data class WeekDay(val date: LocalDate, val daily: List<Entry>, val calendar: List<Entry>, val future: List<Entry>) {
    val isEmpty get() = daily.isEmpty() && calendar.isEmpty() && future.isEmpty()
}

/** The day the week of [day] starts on, under [first]. */
fun weekStart(day: LocalDate, first: DayOfWeek): LocalDate =
    day.minus((day.dayOfWeek.isoDayNumber - first.isoDayNumber + 7) % 7, DateTimeUnit.DAY)

/** The seven days of [day]'s week. Each one reads its own month, so a week across two never mixes calendars. */
fun Journal.weekOf(day: LocalDate, first: DayOfWeek): List<WeekDay> {
    val start = weekStart(day, first)
    return (0 until 7).map { i ->
        val d = start.plus(i, DateTimeUnit.DAY)
        val m = monthOf(d)
        WeekDay(d, entriesAt(Place.Daily(d)), entriesAt(Place.Monthly(m, d.day)), entriesAt(Place.Future(m, d.day)))
    }
}

/**
 * Where writing on [day] lands (docs/tecnico.md 12.13): its Daily Log up to tomorrow, the calendar line
 * of this month after that, and its day of the Future Log in a month to come. The same rule Hoy, Mes and
 * Futuro already follow each on their own.
 */
fun writeTarget(day: LocalDate, today: LocalDate): Place = when {
    day <= today.plus(1, DateTimeUnit.DAY) -> Place.Daily(day)
    monthOf(day) == monthOf(today) -> Place.Monthly(monthOf(day), day.day)
    else -> Place.Future(monthOf(day), day.day)
}

/** The furthest week the view goes to: the one holding the last day of next month. */
fun lastWeekAllowed(today: LocalDate, first: DayOfWeek): LocalDate {
    val next = monthOf(today).plus(1, DateTimeUnit.MONTH)
    return weekStart(LocalDate(next.year, next.month, monthDays(next)), first)
}
