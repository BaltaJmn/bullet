package com.baltajmn.bullet

import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.Storage
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// 23. Almacen en disco (#14, #33).
class StorageDiskTest {
    private lateinit var dir: File

    private val good = """{"schemaVersion":1,"entries":[{"id":"e-1","text":"kept","place":{"daily":"2027-01-17"},"createdAt":0,"updatedAt":0}]}"""

    @BeforeTest
    fun setUp() {
        dir = createTempDirectory("bullet").toFile()
        Storage.rootOverride = dir
    }

    @AfterTest
    fun tearDown() {
        Storage.rootOverride = null
        dir.deleteRecursively()
    }

    @Test
    fun halfWrittenTempAndUnreadableMainFallBackToTheBackup() {
        File(dir, "journal.tmp.json").writeText("{\"schemaVersion\":1,\"entr")
        File(dir, "journal.json").writeText("not json")
        File(dir, "journal.bak.json").writeText(good)

        BobbinRepository.load()

        assertEquals("kept", BobbinRepository.journal.entries.single().text)
        assertFalse(BobbinRepository.corrupt)
        assertEquals(good, File(dir, "journal.json").readText())
        assertEquals(good, File(dir, "journal.bak.json").readText())
    }

    @Test
    fun twoUnreadableFilesAreQuarantinedUntouched() {
        File(dir, "journal.json").writeText("garbage one")
        File(dir, "journal.bak.json").writeText("garbage two")

        BobbinRepository.load()

        assertTrue(BobbinRepository.corrupt)
        assertTrue(BobbinRepository.journal.entries.isEmpty())
        assertFalse(File(dir, "journal.json").exists())
        assertFalse(File(dir, "journal.bak.json").exists())
        val moved = File(dir, "corrupt").listFiles().orEmpty()
        assertEquals(setOf("garbage one", "garbage two"), moved.map { it.readText() }.toSet())
        assertTrue(moved.any { it.name.endsWith(".bak.json") })
    }

    @Test
    fun noFilesIsAFreshDiary() {
        BobbinRepository.load()
        assertFalse(BobbinRepository.corrupt)
        assertTrue(BobbinRepository.journal.entries.isEmpty())
        assertFalse(File(dir, "journal.json").exists())
    }

    @Test
    fun writeRotatesTheBackupAndLeavesNoTemp() {
        Storage.write("first")
        Storage.write("second")
        assertEquals("second", Storage.read())
        assertEquals("first", Storage.readPrevious())
        assertFalse(File(dir, "journal.tmp.json").exists())
    }

    @Test
    fun keepCopySavesANamedSnapshotThatWriteNeverTouches() {
        Storage.keepCopy("pre-migration", good)
        Storage.write("current")

        assertEquals(good, Storage.readCopy("pre-migration"))
        assertEquals("current", Storage.read())
        assertFalse(File(dir, "journal.pre-migration.tmp.json").exists())
    }

    @Test
    fun wipeLeavesNothingInTheFolder() {
        Storage.write("first")
        Storage.write("second")
        Storage.keepCopy("pre-migration", good)
        File(dir, "journal.json").writeText("garbage")
        Storage.quarantine()

        Storage.wipe()

        assertTrue(dir.listFiles().orEmpty().isEmpty())
    }
}
