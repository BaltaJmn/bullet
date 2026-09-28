package com.baltajmn.bullet.ui

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

private fun entry(bullet: Bullet, status: TaskStatus = TaskStatus.OPEN) = Entry(
    id = "e-1",
    bullet = bullet,
    text = "algo",
    status = status,
    place = Place.Daily(LocalDate.parse("2026-09-22")),
    createdAt = 0L,
    updatedAt = 0L,
)

/** docs/pantallas.md 5.6's table, #22's own stated test: "una nota no ofrece estados de tarea en la hoja". */
class EntrySheetTest {

    @Test
    fun aNoteOrAnEventOfferNoStatusActions() {
        assertEquals(emptyList(), statusActionsFor(entry(Bullet.NOTE), copyExists = false))
        assertEquals(emptyList(), statusActionsFor(entry(Bullet.EVENT), copyExists = false))
    }

    @Test
    fun anOpenTaskOffersMigrateScheduleAndDiscard() {
        assertEquals(
            listOf(StatusAction.MIGRATE, StatusAction.SCHEDULE, StatusAction.DISCARD),
            statusActionsFor(entry(Bullet.TASK, TaskStatus.OPEN), copyExists = false),
        )
    }

    @Test
    fun aDoneOrIrrelevantTaskOffersOnlyReopen() {
        assertEquals(listOf(StatusAction.REOPEN), statusActionsFor(entry(Bullet.TASK, TaskStatus.DONE), copyExists = false))
        assertEquals(listOf(StatusAction.REOPEN), statusActionsFor(entry(Bullet.TASK, TaskStatus.IRRELEVANT), copyExists = false))
    }

    @Test
    fun aMigratedOrScheduledTaskOffersGoToCopyOnlyWhileTheCopyExists() {
        assertEquals(listOf(StatusAction.GO_TO_COPY), statusActionsFor(entry(Bullet.TASK, TaskStatus.MIGRATED), copyExists = true))
        assertEquals(emptyList(), statusActionsFor(entry(Bullet.TASK, TaskStatus.MIGRATED), copyExists = false))
        assertEquals(listOf(StatusAction.GO_TO_COPY), statusActionsFor(entry(Bullet.TASK, TaskStatus.SCHEDULED), copyExists = true))
    }
}
