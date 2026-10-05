package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

// 32. Sincronización (docs/tecnico.md 10, 12.7, #69).
class SyncTest {
    private val day = Place.Daily(LocalDate(2026, 10, 6))
    private fun e(id: String, updatedAt: Long = 0) = Entry(id = id, text = id, place = day, createdAt = 0, updatedAt = updatedAt)
    private val base = Journal(entries = listOf(e("a")))
    private val here = base.copy(entries = base.entries + e("here"))
    private val there = base.copy(entries = base.entries + e("there"))

    @Test
    fun onlyOneSideChangedAndThatSideWins() {
        assertEquals(SyncStep.Push, syncStep(here, remote = base, base = base))
        assertEquals(SyncStep.Pull(there), syncStep(base, remote = there, base = base))
        assertEquals(SyncStep.Nothing, syncStep(base, remote = base, base = base))
        assertEquals(SyncStep.Push, syncStep(here, remote = null, base = base))
    }

    @Test
    fun bothChangedAsksAndNeverMergesOnItsOwn() {
        assertEquals(SyncStep.Conflict(there), syncStep(here, remote = there, base = base))
        // A first sync on a device that already has a diary asks too; an empty one just takes the cloud's.
        assertEquals(SyncStep.Conflict(there), syncStep(here, remote = there, base = null))
        assertEquals(SyncStep.Pull(there), syncStep(Journal(), remote = there, base = null))
    }

    @Test
    fun theLockAndTheReminderAreEachPhonesOwn() {
        val locked = base.copy(settings = Settings(lockOn = true, reminderOn = true, reminderHour = 7))
        assertEquals(SyncStep.Nothing, syncStep(locked, remote = base, base = base))
        assertEquals(SyncStep.Nothing, syncStep(base, remote = locked, base = locked))
    }

    @Test
    fun theCloudsVersionKeepsThisDiaryAsPreSyncFirst() = runBlocking {
        val files = MemoryFiles()
        BobbinRepository.load(files)
        BobbinRepository.edit { here.copy(settings = Settings(lockOn = true)) }
        val local = BobbinRepository.journal
        assertTrue(BobbinRepository.replaceFromSync(local, there))
        assertEquals(local, JournalJson.decodeFromString(Journal.serializer(), files.copies.getValue("pre-sync")))
        assertEquals(there.entries, BobbinRepository.journal.entries)
        assertTrue(BobbinRepository.journal.settings.lockOn)

        // Anything written while the cloud's was on its way stops it: that edit is not lost.
        assertEquals(false, BobbinRepository.replaceFromSync(local, base))
        assertEquals(there.entries, BobbinRepository.journal.entries)
    }

    @Test
    fun mergingBothDuplicatesNothing() {
        val merged = merge(here, there).journal
        assertEquals(listOf("a", "here", "there"), merged.entries.map { it.id }.sorted())
        assertEquals(SyncStep.Push, syncStep(merged, remote = there, base = there))
    }
}
