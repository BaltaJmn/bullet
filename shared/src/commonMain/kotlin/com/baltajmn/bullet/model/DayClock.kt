package com.baltajmn.bullet.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth

/** 0..6; the app's own default before a [Settings.dayStartHour] is read (docs/tecnico.md 6.1). */
const val DAY_START_DEFAULT = 4

/** The local wall-clock hour is compared, never an instant minus [dayStartHour]: a DST change never moves the cutoff. */
fun logicalDate(local: LocalDateTime, dayStartHour: Int = DAY_START_DEFAULT): LocalDate =
    if (local.hour < dayStartHour) local.date.minus(1, DateTimeUnit.DAY) else local.date

@OptIn(ExperimentalTime::class)
fun logicalDate(now: Instant, tz: TimeZone, dayStartHour: Int = DAY_START_DEFAULT): LocalDate =
    logicalDate(now.toLocalDateTime(tz), dayStartHour)

/** The first instant after [date] at which [logicalDate] stops being [date]: [dayStartHour]:00 the next day. */
@OptIn(ExperimentalTime::class)
fun nextDayStart(date: LocalDate, dayStartHour: Int, tz: TimeZone): Instant =
    LocalDateTime(date.plus(1, DateTimeUnit.DAY), LocalTime(dayStartHour, 0)).toInstant(tz)

fun monthOf(d: LocalDate): YearMonth = d.yearMonth

/** 28, 29, 30 or 31. */
fun monthDays(m: YearMonth): Int = m.numberOfDays

/** [Settings.firstDayOfWeek] is an ISO day (1 Monday .. 7 Sunday); null defers to the system. */
fun firstDayOfWeek(s: Settings, system: DayOfWeek): DayOfWeek = s.firstDayOfWeek?.let { DayOfWeek(it) } ?: system

/** The days of [m] that start a week under [first] (docs/pantallas.md: Month's week separator). */
fun weekStarts(m: YearMonth, first: DayOfWeek): Set<Int> =
    (1..monthDays(m)).filterTo(mutableSetOf()) { LocalDate(m.year, m.month, it).dayOfWeek == first }
