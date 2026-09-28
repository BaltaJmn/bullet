package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.migrationCount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** docs/tecnico.md 6.4, docs/pantallas.md 5.8: delete offers Deshacer, #23. */
class BobbinRepositoryTest {

    private fun freshJournalWith(vararg entries: Entry) {
        BobbinRepository.load(MemoryFiles())
        entries.forEach { e -> BobbinRepository.edit { j -> j.copy(entries = j.entries + e) } }
    }

    private fun entry(id: String, place: Place = Place.Daily(LocalDate.parse("2026-09-22")), status: TaskStatus = TaskStatus.OPEN) = Entry(
        id = id,
        bullet = Bullet.TASK,
        text = "algo",
        status = status,
        place = place,
        order = 0,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun deleteOffersAPendingUndoAndUndoRestoresTheExactSnapshotWhenNothingElseChanged() {
        freshJournalWith(entry("e-1"))

        BobbinRepository.delete("e-1")
        assertTrue(BobbinRepository.journal.entries.isEmpty())
        assertNotNull(BobbinRepository.pendingUndo)

        BobbinRepository.undo()
        assertEquals(listOf("e-1"), BobbinRepository.journal.entries.map { it.id })
        assertNull(BobbinRepository.pendingUndo)
    }

    @Test
    fun undoWithinTheWindowRestoresTheExactOrderAmongTheOtherEntries() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        freshJournalWith(entry("e-1", place).copy(order = 0), entry("e-2", place).copy(order = 1), entry("e-3", place).copy(order = 2))

        BobbinRepository.delete("e-2")
        BobbinRepository.undo()

        assertEquals(
            listOf("e-1" to 0, "e-2" to 1, "e-3" to 2),
            BobbinRepository.journal.entries.sortedBy { it.order }.map { it.id to it.order },
        )
    }

    @Test
    fun undoAfterAnotherChangeReinsertsOnlyTheDeletedEntryAtItsOldStatus() {
        freshJournalWith(entry("e-1", status = TaskStatus.DONE), entry("e-2"))

        BobbinRepository.delete("e-1")
        BobbinRepository.toggleSignifier("e-2", Signifier.PRIORITY)

        BobbinRepository.undo()
        val byId = BobbinRepository.journal.entries.associateBy { it.id }
        assertEquals(TaskStatus.DONE, byId.getValue("e-1").status)
        // The interleaving change survives: undo only puts e-1 back, it does not revert e-2.
        assertEquals(setOf(Signifier.PRIORITY), byId.getValue("e-2").signifiers)
    }

    @Test
    fun aSecondDeleteReplacesThePendingUndoAndTheFirstStaysFirm() {
        freshJournalWith(entry("e-1"), entry("e-2"))

        BobbinRepository.delete("e-1")
        BobbinRepository.delete("e-2")
        assertEquals("e-2", BobbinRepository.pendingUndo?.deleted?.id)

        BobbinRepository.undo()
        // e-1's own delete is firm: only e-2 comes back.
        assertEquals(listOf("e-2"), BobbinRepository.journal.entries.map { it.id })
    }

    @Test
    fun deletingTheOriginalOfAMigrationKeepsTheCopyAndItsChainIntactAfterUndoIsSkipped() {
        val original = entry("e-1", status = TaskStatus.MIGRATED)
        val landed = entry("e-2", place = Place.Monthly(YearMonth.parse("2026-10"))).copy(from = "e-1")
        freshJournalWith(original, landed)

        BobbinRepository.delete("e-1")
        val skeleton = BobbinRepository.journal.entries.single { it.id == "e-1" }
        assertTrue(skeleton.gone)
        assertEquals(1, BobbinRepository.journal.migrationCount("e-2"))
    }
}
