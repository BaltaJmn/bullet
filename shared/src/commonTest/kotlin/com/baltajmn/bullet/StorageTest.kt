package com.baltajmn.bullet

import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.MemoryFiles
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

// 21. Almacen: carga, reparacion y esquema (commonMain half: #14).
class StorageTest {

    private val good = JournalJson.encodeToString(
        Journal.serializer(),
        Journal(entries = listOf(Entry(id = "e-1", bullet = Bullet.TASK, text = "kept", place = Place.Daily(LocalDate.parse("2026-09-22")), createdAt = 0L, updatedAt = 0L))),
    )

    @Test
    fun mainUnreadableAndGoodBackupRestoresMainWithoutRotatingIt() {
        val files = MemoryFiles(main = "not json", backup = good)

        BobbinRepository.load(files)

        assertFalse(BobbinRepository.corrupt)
        assertEquals("kept", BobbinRepository.journal.entries.single().text)
        assertEquals(good, files.main)
        assertEquals(good, files.backup)
        assertEquals(0, files.quarantineCalls)
    }

    @Test
    fun bothFilesUnreadableAreQuarantinedWithAnEmptyDiaryAndNothingOverwritten() {
        val files = MemoryFiles(main = "garbage one", backup = "garbage two")

        BobbinRepository.load(files)

        assertTrue(BobbinRepository.corrupt)
        assertTrue(BobbinRepository.journal.entries.isEmpty())
        assertEquals(1, files.quarantineCalls)
        assertEquals("garbage one", files.quarantinedMain)
        assertEquals("garbage two", files.quarantinedBackup)
        assertNull(files.main)
        assertNull(files.backup)
    }

    @Test
    fun noFilesAtAllIsAFreshEmptyDiary() {
        val files = MemoryFiles()

        BobbinRepository.load(files)

        assertFalse(BobbinRepository.corrupt)
        assertTrue(BobbinRepository.journal.entries.isEmpty())
        assertEquals(0, files.quarantineCalls)
        assertNull(files.main)
    }

    @Test
    fun oneHundredConsecutiveEditsLoseNoEntryAndTheLastSaveIsTheLastSnapshot() = runBlocking {
        val files = MemoryFiles()
        BobbinRepository.load(files)

        repeat(100) { i ->
            val date = LocalDate.parse("2026-01-01")
            BobbinRepository.edit { j ->
                j.copy(entries = j.entries + Entry(id = "e-$i", text = "n$i", place = Place.Daily(date), createdAt = i.toLong(), updatedAt = i.toLong()))
            }
        }
        BobbinRepository.flush()

        assertEquals(100, BobbinRepository.journal.entries.size)
        val onDisk = JournalJson.decodeFromString(Journal.serializer(), files.main!!)
        assertEquals(100, onDisk.entries.size)
        assertEquals(BobbinRepository.journal, onDisk)
    }
}
