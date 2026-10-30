package com.baltajmn.bullet

import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.lastWeekAllowed
import com.baltajmn.bullet.model.weekOf
import com.baltajmn.bullet.model.weekStart
import com.baltajmn.bullet.model.writeTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private fun at(id: String, place: Place) = Entry(id = id, text = id, place = place, createdAt = 0L, updatedAt = 0L)

/** Test 52 (#78): the week is a way of looking at places that already exist. */
class WeekTest {

    // Thursday 29 October 2026.
    private val today = LocalDate(2026, 10, 29)

    @Test
    fun theWeekStartsWhereTheSettingSays() {
        assertEquals(LocalDate(2026, 10, 26), weekStart(today, DayOfWeek.MONDAY))
        assertEquals(LocalDate(2026, 10, 25), weekStart(today, DayOfWeek.SUNDAY))
        assertEquals(LocalDate(2026, 10, 26), weekStart(LocalDate(2026, 10, 26), DayOfWeek.MONDAY))
        assertEquals(LocalDate(2026, 10, 26), weekStart(LocalDate(2026, 11, 1), DayOfWeek.MONDAY))
    }

    @Test
    fun aWeekAcrossTwoMonthsReadsEachDayInItsOwnMonth() {
        val october = YearMonth(2026, 10)
        val november = YearMonth(2026, 11)
        val j = Journal(
            entries = listOf(
                at("daily", Place.Daily(LocalDate(2026, 10, 27))),
                at("calendar", Place.Monthly(october, 31)),
                at("future", Place.Future(november, 1)),
                // Same day number, other month: never on this week's 27th.
                at("other", Place.Monthly(YearMonth(2026, 9), 27)),
                at("tasks", Place.Monthly(october)),
            ),
        )
        val week = j.weekOf(today, DayOfWeek.MONDAY)
        assertEquals(7, week.size)
        assertEquals((26..31).map { LocalDate(2026, 10, it) } + LocalDate(2026, 11, 1), week.map { it.date })
        assertEquals(listOf("daily"), week[1].daily.map { it.id })
        assertEquals(listOf("calendar"), week[5].calendar.map { it.id })
        assertEquals(listOf("future"), week[6].future.map { it.id })
        // The tasks of the month have no day, so no day of the week shows them.
        assertTrue(week.none { d -> (d.daily + d.calendar + d.future).any { it.id == "tasks" || it.id == "other" } })
    }

    @Test
    fun writingLandsWhereHoyMesAndFuturoWouldPutIt() {
        assertEquals(Place.Daily(LocalDate(2026, 10, 28)), writeTarget(LocalDate(2026, 10, 28), today))
        assertEquals(Place.Daily(today), writeTarget(today, today))
        assertEquals(Place.Daily(LocalDate(2026, 10, 30)), writeTarget(LocalDate(2026, 10, 30), today))
        assertEquals(Place.Monthly(YearMonth(2026, 10), 31), writeTarget(LocalDate(2026, 10, 31), today))
        assertEquals(Place.Future(YearMonth(2026, 11), 2), writeTarget(LocalDate(2026, 11, 2), today))
        // On the last day of a month, tomorrow is already the next one, and still the Daily Log.
        val lastDay = LocalDate(2026, 10, 31)
        assertEquals(Place.Daily(LocalDate(2026, 11, 1)), writeTarget(LocalDate(2026, 11, 1), lastDay))
    }

    @Test
    fun theFurthestWeekHoldsTheLastDayOfNextMonth() {
        // 30 November 2026 is a Monday.
        assertEquals(LocalDate(2026, 11, 30), lastWeekAllowed(today, DayOfWeek.MONDAY))
        assertEquals(LocalDate(2026, 11, 29), lastWeekAllowed(today, DayOfWeek.SUNDAY))
    }
}
