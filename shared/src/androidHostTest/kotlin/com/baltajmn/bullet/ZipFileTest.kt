package com.baltajmn.bullet

import com.baltajmn.bullet.data.exportZip
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

/** Test 14, the half that needs a real file: the system's own `unzip` takes the backup as good. */
class ZipFileTest {
    @Test
    fun theSystemUnzipAcceptsTheBackup() {
        val unzip = listOf("/usr/bin/unzip", "/bin/unzip").map(::File).firstOrNull { it.canExecute() } ?: return
        val j = Journal(
            entries = listOf(
                Entry(id = "a", text = "Café", place = Place.Daily(LocalDate(2026, 9, 22)), createdAt = 0L, updatedAt = 0L),
                Entry(id = "b", text = "Dune", place = Place.InCollection("c-1"), createdAt = 0L, updatedAt = 0L),
            ),
            collections = listOf(BulletCollection(id = "c-1", title = "Canciones de mañana", createdAt = 0L)),
        )
        val file = File.createTempFile("bobbin", ".zip")
        try {
            file.outputStream().use { out -> exportZip(j) { out.write(it) } }
            val run = ProcessBuilder(unzip.path, "-t", file.path).redirectErrorStream(true).start()
            val output = run.inputStream.bufferedReader().readText()
            assertEquals(0, run.waitFor(), output)
        } finally {
            file.delete()
        }
    }
}
