package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.discard
import com.baltajmn.bullet.model.migrate
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.ofDay
import com.baltajmn.bullet.model.openTasksBefore
import com.baltajmn.bullet.model.openTasksOfDay
import com.baltajmn.bullet.model.openTasksOfMonth
import com.baltajmn.bullet.model.schedule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private fun task(
    id: String,
    place: Place,
    status: TaskStatus = TaskStatus.OPEN,
    from: String? = null,
    text: String = "tarea",
    bullet: Bullet = Bullet.TASK,
) = Entry(id = id, bullet = bullet, text = text, status = status, place = place, createdAt = 0L, updatedAt = 0L, from = from)

class MigrationTest {

    // 6. Migrate, schedule, discard.
    @Test
    fun migrateLeavesTheOriginalMigratedAndLandsAnOpenCopy() {
        val today = LocalDate.parse("2026-09-22")
        val j = Journal(entries = listOf(task("e-1", Place.Daily(today), text = "llamar")))
        val to = Place.Monthly(YearMonth.parse("2026-10"))

        val result = j.migrate("e-1", to, today, now = 100L, newId = "e-2")!!
        val originalAfter = result.entries.single { it.id == "e-1" }
        val landed = result.entries.single { it.id == "e-2" }

        assertEquals(TaskStatus.MIGRATED, originalAfter.status)
        assertEquals("llamar", originalAfter.text)
        assertEquals(TaskStatus.OPEN, landed.status)
        assertEquals(to, landed.place)
        assertEquals("e-1", landed.from)
        assertEquals("llamar", landed.text)
    }

    @Test
    fun scheduleFromMarchToDecemberCreatesAFutureEntryAndLeavesTheOriginalScheduled() {
        val today = LocalDate.parse("2026-03-05")
        val j = Journal(entries = listOf(task("e-1", Place.Daily(today))))

        val result = j.schedule("e-1", YearMonth.parse("2026-12"), day = null, today = today, now = 1L, newId = "e-2")!!
        assertEquals(TaskStatus.SCHEDULED, result.entries.single { it.id == "e-1" }.status)
        val landed = result.entries.single { it.id == "e-2" }
        assertEquals(Place.Future(YearMonth.parse("2026-12")), landed.place)
        assertEquals(TaskStatus.OPEN, landed.status)
        assertEquals("e-1", landed.from)
    }

    @Test
    fun schedulingTheThirtyFirstOfAThirtyDayMonthChangesNothing() {
        val today = LocalDate.parse("2026-03-05")
        val j = Journal(entries = listOf(task("e-1", Place.Daily(today))))
        assertNull(j.schedule("e-1", YearMonth.parse("2026-04"), day = 31, today = today, now = 1L, newId = "e-2"))
    }

    @Test
    fun discardMarksIrrelevantWithoutACopy() {
        val j = Journal(entries = listOf(task("e-1", Place.Daily(LocalDate.parse("2026-09-22")))))
        val result = j.discard("e-1", now = 1L)!!
        assertEquals(1, result.entries.size)
        assertEquals(TaskStatus.IRRELEVANT, result.entries.single().status)
    }

    @Test
    fun migratingAClosedTaskOrTheSamePlaceIsRejected() {
        val today = LocalDate.parse("2026-09-22")
        val place = Place.Daily(today)
        val closed = Journal(entries = listOf(task("e-1", place, status = TaskStatus.DONE)))
        assertNull(closed.migrate("e-1", Place.Monthly(YearMonth.parse("2026-10")), today, now = 1L, newId = "e-2"))

        val open = Journal(entries = listOf(task("e-1", place)))
        assertNull(open.migrate("e-1", place, today, now = 1L, newId = "e-2"))
    }

    @Test
    fun migratingAnEventOrANoteMovesItInPlaceWithoutACopy() {
        val today = LocalDate.parse("2027-02-14")
        val original = task("e-1", Place.Future(YearMonth.parse("2027-02"), day = 14), bullet = Bullet.EVENT)
        val j = Journal(entries = listOf(original))

        val result = j.migrate("e-1", Place.Daily(today), today, now = 5L, newId = "unused")!!
        val moved = result.entries.single()
        assertEquals("e-1", moved.id)
        assertEquals(Place.Daily(today), moved.place)
        assertEquals(TaskStatus.OPEN, moved.status)
        assertNull(moved.from)
    }

    // 7. Chain.
    @Test
    fun threeMigrationsInARowGiveACountOfThree() {
        val e1 = task("e-1", Place.Daily(LocalDate.parse("2026-01-01")), status = TaskStatus.MIGRATED)
        val e2 = task("e-2", Place.Daily(LocalDate.parse("2026-01-02")), status = TaskStatus.MIGRATED, from = "e-1")
        val e3 = task("e-3", Place.Daily(LocalDate.parse("2026-01-03")), status = TaskStatus.MIGRATED, from = "e-2")
        val e4 = task("e-4", Place.Daily(LocalDate.parse("2026-01-04")), from = "e-3")
        val j = Journal(entries = listOf(e1, e2, e3, e4))
        assertEquals(3, j.migrationCount("e-4"))
    }

    @Test
    fun aFromPointingAtAMissingIdStillCountsOneJump() {
        val e1 = task("e-1", Place.Daily(LocalDate.parse("2026-01-01")), from = "e-missing")
        assertEquals(1, Journal(entries = listOf(e1)).migrationCount("e-1"))
    }

    @Test
    fun aCycleStopsInsteadOfHanging() {
        val e1 = task("e-1", Place.Daily(LocalDate.parse("2026-01-01")), from = "e-2")
        val e2 = task("e-2", Place.Daily(LocalDate.parse("2026-01-02")), from = "e-1")
        assertEquals(2, Journal(entries = listOf(e1, e2)).migrationCount("e-1"))
    }

    // Review queries fed by 6.4.
    @Test
    fun openTasksBeforeOnlyListsEarlierDaysOfTheSameMonth() {
        val d = LocalDate.parse("2026-09-22")
        val before = task("e-1", Place.Daily(LocalDate.parse("2026-09-10")))
        val sameDay = task("e-2", Place.Daily(d))
        val otherMonth = task("e-3", Place.Daily(LocalDate.parse("2026-08-10")))
        val calendarLine = task("e-4", Place.Monthly(YearMonth.parse("2026-09"), day = 5))
        val monthTask = task("e-5", Place.Monthly(YearMonth.parse("2026-09")))
        val j = Journal(entries = listOf(before, sameDay, otherMonth, calendarLine, monthTask))

        assertEquals(setOf("e-1", "e-4"), j.openTasksBefore(d).map { it.id }.toSet())
    }

    @Test
    fun openTasksOfMonthListsDailyAndMonthlyEntriesOfThatMonth() {
        val m = YearMonth.parse("2026-09")
        val daily = task("e-1", Place.Daily(LocalDate.parse("2026-09-10")))
        val calendarLine = task("e-2", Place.Monthly(m, day = 5))
        val monthTask = task("e-3", Place.Monthly(m))
        val otherMonth = task("e-4", Place.Daily(LocalDate.parse("2026-08-10")))
        val done = task("e-5", Place.Monthly(m), status = TaskStatus.DONE)
        val j = Journal(entries = listOf(daily, calendarLine, monthTask, otherMonth, done))

        assertEquals(setOf("e-1", "e-2", "e-3"), j.openTasksOfMonth(m).map { it.id }.toSet())
    }

    // docs/tecnico.md 6.5, used by ui/TodayScreen.kt (#21).
    @Test
    fun ofDayListsTheDailyLogFirstThenTheCalendarLineOfThatDay() {
        val d = LocalDate.parse("2026-09-22")
        val calendarLine = task("e-1", Place.Monthly(YearMonth.parse("2026-09"), day = 22))
        val daily = task("e-2", Place.Daily(d))
        val otherDay = task("e-3", Place.Daily(LocalDate.parse("2026-09-21")))
        val skeleton = task("e-4", Place.Daily(d)).copy(gone = true)
        val j = Journal(entries = listOf(calendarLine, daily, otherDay, skeleton))

        assertEquals(listOf("e-2", "e-1"), j.ofDay(d).map { it.id })
    }

    @Test
    fun openTasksOfDayOnlyKeepsOpenTasksOfOfDay() {
        val d = LocalDate.parse("2026-09-22")
        val open = task("e-1", Place.Daily(d))
        val done = task("e-2", Place.Daily(d), status = TaskStatus.DONE)
        val note = task("e-3", Place.Daily(d), bullet = Bullet.NOTE)
        val j = Journal(entries = listOf(open, done, note))

        assertEquals(listOf("e-1"), j.openTasksOfDay(d).map { it.id })
    }
}
