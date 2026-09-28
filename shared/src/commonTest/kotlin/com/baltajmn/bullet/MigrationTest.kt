package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.capture
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.model.delete
import com.baltajmn.bullet.model.discard
import com.baltajmn.bullet.model.editText
import com.baltajmn.bullet.model.futureBlock
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.migrate
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.ofDay
import com.baltajmn.bullet.model.openTasksBefore
import com.baltajmn.bullet.model.openTasksOfDay
import com.baltajmn.bullet.model.openTasksOfMonth
import com.baltajmn.bullet.model.reopen
import com.baltajmn.bullet.model.reorder
import com.baltajmn.bullet.model.restoreDeleted
import com.baltajmn.bullet.model.schedule
import com.baltajmn.bullet.model.toggleDone
import com.baltajmn.bullet.model.toggleSignifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
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

    // Estados y signifiers (docs/tecnico.md 6.3), #22.
    @Test
    fun toggleDoneFlipsOpenAndDoneOnlyForATask() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val open = Journal(entries = listOf(task("e-1", place)))
        assertEquals(TaskStatus.DONE, open.toggleDone("e-1", now = 1L)!!.entries.single().status)

        val done = Journal(entries = listOf(task("e-1", place, status = TaskStatus.DONE)))
        assertEquals(TaskStatus.OPEN, done.toggleDone("e-1", now = 1L)!!.entries.single().status)

        val migrated = Journal(entries = listOf(task("e-1", place, status = TaskStatus.MIGRATED)))
        assertNull(migrated.toggleDone("e-1", now = 1L))

        val note = Journal(entries = listOf(task("e-1", place, bullet = Bullet.EVENT)))
        assertNull(note.toggleDone("e-1", now = 1L))
    }

    @Test
    fun reopenOnlyAcceptsDoneOrIrrelevant() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val done = Journal(entries = listOf(task("e-1", place, status = TaskStatus.DONE)))
        assertEquals(TaskStatus.OPEN, done.reopen("e-1", now = 1L)!!.entries.single().status)

        val irrelevant = Journal(entries = listOf(task("e-1", place, status = TaskStatus.IRRELEVANT)))
        assertEquals(TaskStatus.OPEN, irrelevant.reopen("e-1", now = 1L)!!.entries.single().status)

        val migrated = Journal(entries = listOf(task("e-1", place, status = TaskStatus.MIGRATED)))
        assertNull(migrated.reopen("e-1", now = 1L))

        val open = Journal(entries = listOf(task("e-1", place)))
        assertNull(open.reopen("e-1", now = 1L))
    }

    @Test
    fun toggleSignifierPutsAndRemovesOnAnyEntry() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val j = Journal(entries = listOf(task("e-1", place, bullet = Bullet.NOTE)))
        val withPriority = j.toggleSignifier("e-1", Signifier.PRIORITY, now = 1L)!!
        assertEquals(setOf(Signifier.PRIORITY), withPriority.entries.single().signifiers)
        val withoutPriority = withPriority.toggleSignifier("e-1", Signifier.PRIORITY, now = 2L)!!
        assertEquals(emptySet(), withoutPriority.entries.single().signifiers)
    }

    @Test
    fun editTextSavesOneLinedAndClampedTextButNotWhenEmptyAfterTrim() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val j = Journal(entries = listOf(task("e-1", place, text = "vieja")))

        val edited = j.editText("e-1", "linea uno\nlinea dos", now = 1L)!!
        assertEquals("linea uno linea dos", edited.entries.single().text)

        assertNull(j.editText("e-1", "   ", now = 1L))
    }

    @Test
    fun deleteDropsAnEntryNothingPointsAt() {
        val j = Journal(entries = listOf(task("e-1", Place.Daily(LocalDate.parse("2026-09-22")))))
        assertEquals(0, j.delete("e-1", now = 1L)!!.entries.size)
    }

    @Test
    fun deletingAMigratedOriginalLeavesASkeletonAndKeepsTheCopyAndItsChain() {
        val today = LocalDate.parse("2026-09-22")
        val original = task("e-1", Place.Daily(today), status = TaskStatus.MIGRATED)
        val landed = task("e-2", Place.Monthly(YearMonth.parse("2026-10")), from = "e-1")
        val j = Journal(entries = listOf(original, landed))

        val result = j.delete("e-1", now = 5L)!!
        val skeleton = result.entries.single { it.id == "e-1" }
        assertTrue(skeleton.gone)
        assertEquals("", skeleton.text)
        assertEquals(emptySet(), skeleton.signifiers)
        assertEquals(1, result.migrationCount("e-2"))
        assertEquals(landed, copyOf(result, "e-1"))
    }

    @Test
    fun deletingTheLastThingPointingAtASkeletonRemovesItToo() {
        val e1 = task("e-1", Place.Daily(LocalDate.parse("2026-01-01"))).copy(gone = true, text = "")
        val e2 = task("e-2", Place.Daily(LocalDate.parse("2026-01-02")), from = "e-1")
        val j = Journal(entries = listOf(e1, e2))

        val result = j.delete("e-2", now = 1L)!!
        assertEquals(emptyList(), result.entries)
    }

    @Test
    fun deletingACascadesThroughAChainOfSkeletons() {
        val e1 = task("e-1", Place.Daily(LocalDate.parse("2026-01-01"))).copy(gone = true, text = "")
        val e2 = task("e-2", Place.Daily(LocalDate.parse("2026-01-02")), from = "e-1").copy(gone = true, text = "")
        val e3 = task("e-3", Place.Daily(LocalDate.parse("2026-01-03")), from = "e-2")
        val j = Journal(entries = listOf(e1, e2, e3))

        val result = j.delete("e-3", now = 1L)!!
        assertEquals(emptyList(), result.entries)
    }

    @Test
    fun copyOfFindsTheNonSkeletonEntryWhoseFromIsId() {
        val landed = task("e-2", Place.Daily(LocalDate.parse("2026-09-23")), from = "e-1")
        val j = Journal(entries = listOf(task("e-1", Place.Daily(LocalDate.parse("2026-09-22")), status = TaskStatus.MIGRATED), landed))
        assertEquals(landed, copyOf(j, "e-1"))
        assertNull(copyOf(j, "e-missing"))
    }

    // Reordenar y deshacer (docs/tecnico.md 6.3, 6.4), #23.
    @Test
    fun reorderRewritesOrderZeroToNMinusOneAtThatPlaceOnly() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val elsewhere = Place.Daily(LocalDate.parse("2026-09-23"))
        val a = task("e-1", place, text = "a").copy(order = 0)
        val b = task("e-2", place, text = "b").copy(order = 1)
        val c = task("e-3", place, text = "c").copy(order = 2)
        val other = task("e-4", elsewhere, text = "other").copy(order = 0)
        val j = Journal(entries = listOf(a, b, c, other))

        val result = j.reorder(place, listOf("e-3", "e-1", "e-2"), now = 9L)
        val byId = result.entries.associateBy { it.id }
        assertEquals(1, byId.getValue("e-1").order)
        assertEquals(2, byId.getValue("e-2").order)
        assertEquals(0, byId.getValue("e-3").order)
        assertEquals(0, byId.getValue("e-4").order)
    }

    @Test
    fun reorderOnlyTouchesUpdatedAtOnEntriesWhoseOrderActuallyChanges() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val a = task("e-1", place).copy(order = 0, updatedAt = 1L)
        val b = task("e-2", place).copy(order = 1, updatedAt = 1L)
        val j = Journal(entries = listOf(a, b))

        // e-1 stays first: its order does not change, so updatedAt must not move either.
        val result = j.reorder(place, listOf("e-1", "e-2"), now = 99L)
        val byId = result.entries.associateBy { it.id }
        assertEquals(1L, byId.getValue("e-1").updatedAt)
        assertEquals(1L, byId.getValue("e-2").updatedAt)
    }

    @Test
    fun restoreDeletedReinsertsAFullyRemovedEntry() {
        val original = task("e-1", Place.Daily(LocalDate.parse("2026-09-22")), text = "vuelve")
        val j = Journal(entries = listOf(task("e-2", Place.Daily(LocalDate.parse("2026-09-22")))))

        val result = j.restoreDeleted(original)
        assertEquals(setOf("e-1", "e-2"), result.entries.map { it.id }.toSet())
        assertEquals("vuelve", result.entries.single { it.id == "e-1" }.text)
    }

    @Test
    fun restoreDeletedReplacesASkeletonLeftInItsPlace() {
        val original = task("e-1", Place.Daily(LocalDate.parse("2026-09-22")), text = "vuelve", status = TaskStatus.DONE)
        val skeleton = original.copy(gone = true, text = "")
        val j = Journal(entries = listOf(skeleton, task("e-2", Place.Daily(LocalDate.parse("2026-09-23")))))

        val result = j.restoreDeleted(original)
        val restored = result.entries.single { it.id == "e-1" }
        assertEquals("vuelve", restored.text)
        assertEquals(TaskStatus.DONE, restored.status)
        assertFalse(restored.gone)
    }

    // A day typed by hand in a numeric field reaches migrate, capture and schedule alike
    // (docs/pantallas.md 5.7, 8): none of them may create a 31 of a 30 day month.
    @Test
    fun noWriterCreatesADayItsMonthDoesNotHave() {
        val today = LocalDate.parse("2026-09-22")
        val nov = YearMonth.parse("2026-11")
        val j = Journal(entries = listOf(task("e-1", Place.Daily(today))))

        assertNull(j.migrate("e-1", Place.Monthly(nov, 31), today = today, now = 1L, newId = "e-2"))
        assertNull(j.capture("pasear", Place.Future(nov, 31), picked = null, now = 1L, newId = "e-2"))
        assertNull(j.schedule("e-1", nov, day = 31, today = today, now = 1L, newId = "e-2"))

        // The 30th of the same month is a real day, so the same three calls go through.
        assertNotNull(j.migrate("e-1", Place.Monthly(nov, 30), today = today, now = 1L, newId = "e-2"))
        assertNotNull(j.capture("pasear", Place.Future(nov, 30), picked = null, now = 1L, newId = "e-2"))
        assertNotNull(j.schedule("e-1", nov, day = 30, today = today, now = 1L, newId = "e-2"))
    }

    // docs/tecnico.md 6.5, the schedule picker of ui/EntrySheet.kt (#22) and the blocks of Futuro (#25).
    @Test
    fun futureMonthsStartsTheMonthAfterTodayAndCountsN() {
        val today = LocalDate.parse("2026-03-05")
        assertEquals(
            listOf("2026-04", "2026-05", "2026-06", "2026-07", "2026-08", "2026-09"),
            futureMonths(today, 6).map { it.toString() },
        )
        assertEquals(24, futureMonths(today, 24).size)
    }

    // A Future Log block paints the days in order and then what has no day (#25).
    @Test
    fun aFutureBlockGoesByDayAndLeavesTheOnesWithoutADayLast() {
        val nov = YearMonth.parse("2026-11")
        val j = Journal(
            entries = listOf(
                task("e-1", Place.Future(nov)),
                task("e-2", Place.Future(nov, 14)),
                task("e-3", Place.Future(nov, 2)),
                task("e-4", Place.Future(nov)),
                task("e-5", Place.Future(YearMonth.parse("2026-12"), 1)),
                task("e-6", Place.Future(nov, 3)).copy(gone = true, text = ""),
            ),
        )
        assertEquals(listOf("e-3", "e-2", "e-1", "e-4"), j.futureBlock(nov).map { it.id })
    }
}
