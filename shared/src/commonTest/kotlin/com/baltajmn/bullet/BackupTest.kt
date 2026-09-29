package com.baltajmn.bullet

import com.baltajmn.bullet.data.ImportProblem
import com.baltajmn.bullet.data.JOURNAL_NAME
import com.baltajmn.bullet.data.ReadBackup
import com.baltajmn.bullet.data.ZipDamaged
import com.baltajmn.bullet.data.ZipReader
import com.baltajmn.bullet.data.ZipWriter
import com.baltajmn.bullet.data.collectionFileNames
import com.baltajmn.bullet.data.crc32
import com.baltajmn.bullet.data.exportZip
import com.baltajmn.bullet.data.merge
import com.baltajmn.bullet.data.monthMarkdown
import com.baltajmn.bullet.data.readBackup
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth

private val september = YearMonth(2026, 9)

private fun e(
    id: String,
    place: Place,
    bullet: Bullet = Bullet.TASK,
    status: TaskStatus = TaskStatus.OPEN,
    text: String = id,
    signifiers: Set<Signifier> = emptySet(),
    updatedAt: Long = 0L,
    gone: Boolean = false,
    order: Int = 0,
) = Entry(
    id = id, bullet = bullet, text = text, status = status, signifiers = signifiers, place = place,
    order = order, createdAt = 0L, updatedAt = updatedAt, gone = gone,
)

private fun zipOf(vararg files: Pair<String, ByteArray>): ByteArray {
    val out = mutableListOf<ByteArray>()
    val zip = ZipWriter({ out += it }, LocalDateTime(2026, 9, 23, 10, 0))
    files.forEach { (name, bytes) -> zip.add(name, bytes) }
    zip.finish()
    return out.fold(ByteArray(0)) { acc, b -> acc + b }
}

/** A pull source over [bytes], in chunks of whatever size is asked. */
private fun sourceOf(bytes: ByteArray): (Int) -> ByteArray? {
    var at = 0
    return { n ->
        if (at >= bytes.size) {
            null
        } else {
            val end = minOf(bytes.size, at + n)
            bytes.copyOfRange(at, end).also { at = end }
        }
    }
}

private fun unzip(bytes: ByteArray): Map<String, String> {
    val files = linkedMapOf<String, String>()
    ZipReader(sourceOf(bytes)).forEach { name, content -> files[name] = content.decodeToString() }
    return files
}

class BackupTest {

    // 14. Zip (#44).
    @Test
    fun zipGoesThereAndBackWithAccentsInTheNames() {
        val bytes = zipOf(JOURNAL_NAME to "{}".encodeToByteArray(), "months/2026-09.md" to "# ñ".encodeToByteArray(), "collections/Canción.md" to "é".encodeToByteArray())
        assertEquals(listOf(JOURNAL_NAME, "months/2026-09.md", "collections/Canción.md"), unzip(bytes).keys.toList())
        assertEquals("é", unzip(bytes)["collections/Canción.md"])
    }

    @Test
    fun crcOfTheStandardCheckString() {
        assertEquals(0xCBF43926.toInt(), crc32("123456789".encodeToByteArray()))
    }

    @Test
    fun aZipCutInHalfIsDamaged() {
        val bytes = zipOf(JOURNAL_NAME to "{\"schemaVersion\":1}".repeat(50).encodeToByteArray())
        assertFailsWith<ZipDamaged> { unzip(bytes.copyOf(bytes.size / 2)) }
    }

    // 15. Exportar Markdown (#44, #32).
    @Test
    fun aMonthListsTheFiveStatesWithTheirSymbols() {
        val day = Place.Daily(LocalDate(2026, 9, 22))
        val j = Journal(
            entries = listOf(
                e("a", day, signifiers = setOf(Signifier.PRIORITY), order = 0, text = "Llamar"),
                e("b", day, status = TaskStatus.DONE, order = 1, text = "Banco"),
                e("c", day, status = TaskStatus.MIGRATED, order = 2, text = "Fontanero"),
                e("d", day, status = TaskStatus.SCHEDULED, order = 3, text = "Seguro"),
                e("f", day, status = TaskStatus.IRRELEVANT, order = 4, text = "Tinta"),
                e("g", day, bullet = Bullet.EVENT, signifiers = setOf(Signifier.INSPIRATION), order = 5, text = "Concierto"),
                e("h", day, bullet = Bullet.NOTE, order = 6, text = "Jueves"),
                e("i", day, order = 7, text = "borrada", gone = true),
                e("k", Place.Monthly(september, 14), bullet = Bullet.EVENT, text = "Ana"),
                e("l", Place.Future(september, 14), status = TaskStatus.SCHEDULED, text = "Revisar"),
            ),
        )
        val md = monthMarkdown(j, september)!!
        assertEquals(
            """
            # 2026-09

            ## Calendar

            ### 14
            - o Ana

            ## 2026-09-22
            - * . Llamar
            - x Banco
            - > Fontanero
            - < Seguro
            - ~~. Tinta~~
            - ! o Concierto
            - - Jueves

            ## Future log
            - (14) < Revisar

            """.trimIndent(),
            md,
        )
        // No empty section, no skeleton, and a month with nothing has no file.
        assertFalse("## Tasks" in md)
        assertFalse("borrada" in md)
        assertEquals(null, monthMarkdown(j, YearMonth(2026, 10)))
    }

    @Test
    fun collectionNamesAreCleanedAndRepeatsNumbered() {
        fun c(id: String, title: String, kind: CollectionKind = CollectionKind.NOTES, month: YearMonth? = null) =
            BulletCollection(id = id, title = title, createdAt = 0L, kind = kind, month = month)
        val names = collectionFileNames(
            listOf(c("c-1", "Libros: 2026/27"), c("c-2", "Libros: 2026/27"), c("c-3", "   "), c("c-4", "Agua", CollectionKind.TRACKER, september)),
        )
        assertEquals(listOf("Libros- 2026-27", "Libros- 2026-27 2", "c-3", "Agua 2026-09"), names)
    }

    @Test
    fun theZipJournalReadsBackTheSameWithTheCurrentSettings() {
        val j = Journal(
            entries = listOf(e("a", Place.InCollection("c-1"), text = "Dune")),
            collections = listOf(BulletCollection(id = "c-1", title = "Libros", createdAt = 0L)),
            settings = Settings(dayStartHour = 2, cover = "rose"),
        )
        val out = mutableListOf<ByteArray>()
        exportZip(j) { out += it }
        val files = unzip(out.fold(ByteArray(0)) { acc, b -> acc + b })
        assertEquals(listOf(JOURNAL_NAME, "collections/Libros.md"), files.keys.toList())
        assertEquals(j, JournalJson.decodeFromString(Journal.serializer(), files.getValue(JOURNAL_NAME)))
    }

    // 16. Validacion de importacion (#45).
    private fun problem(bytes: ByteArray) = (readBackup(sourceOf(bytes)) as? ReadBackup.Failed)?.problem
    private fun problem(json: String) = problem(json.encodeToByteArray())

    @Test
    fun everyRowOfTheValidationTable() {
        assertEquals(ImportProblem.NOT_BACKUP, problem("hola"))
        assertEquals(ImportProblem.DAMAGED, problem(zipOf(JOURNAL_NAME to "{}".repeat(40).encodeToByteArray()).let { it.copyOf(it.size / 2) }))
        assertEquals(ImportProblem.NOT_BACKUP, problem(zipOf("otra.txt" to "x".encodeToByteArray())))
        assertEquals(ImportProblem.NOT_BACKUP, problem("""{"entries":[]}"""))
        assertEquals(ImportProblem.NOT_BACKUP, problem("""{"schemaVersion":1}"""))
        assertEquals(ImportProblem.TOO_NEW, problem("""{"schemaVersion":99,"entries":[]}"""))
        assertEquals(ImportProblem.DAMAGED, problem("""{"schemaVersion":1,"entries":[{"id":"a","bullet":"rocket","text":"x","place":{"daily":"2026-09-22"},"createdAt":0,"updatedAt":0}]}"""))
        val entry = """{"id":"a","text":"x","place":{"daily":"2026-09-22"},"createdAt":0,"updatedAt":0}"""
        assertEquals(ImportProblem.DAMAGED, problem("""{"schemaVersion":1,"entries":[$entry,$entry]}"""))
        assertEquals(ImportProblem.DAMAGED, problem("""{"schemaVersion":1,"entries":[{"id":"b","text":"x","place":{"collection":"nope"},"createdAt":0,"updatedAt":0}]}"""))
        assertEquals(ImportProblem.EMPTY, problem("""{"schemaVersion":1,"entries":[]}"""))
        assertEquals(null, problem("""{"schemaVersion":1,"entries":[$entry]}"""))
        assertEquals(null, problem(zipOf(JOURNAL_NAME to """{"schemaVersion":1,"entries":[$entry]}""".encodeToByteArray())))
    }

    @Test
    fun theSistersBackupsAreRecognisedAndRefused() {
        assertEquals(ImportProblem.SIBLING_PURL, problem(zipOf("entries.json" to "{}".encodeToByteArray(), "journal.md" to "#".encodeToByteArray())))
        assertEquals(ImportProblem.SIBLING_PURL, problem("""{"version":1,"entries":{"2026-09-22":{"text":"x"}}}"""))
        assertEquals(ImportProblem.SIBLING_MOOD, problem("""{"app":"mood","store":{"entries":[]}}"""))
        assertEquals(ImportProblem.SIBLING_QUILT, problem("""{"habits":[]}"""))
    }

    // 22. Fusion (#45).
    private val day = Place.Daily(LocalDate(2026, 9, 22))

    @Test
    fun importingTheSameDiaryTwiceDuplicatesNothing() {
        val backup = Journal(entries = listOf(e("a", day), e("b", day)))
        val once = merge(Journal(entries = listOf(e("x", day))), backup)
        assertEquals(2, once.added)
        val twice = merge(once.journal, backup)
        assertEquals(0, twice.added)
        assertEquals(0, twice.updated)
        assertEquals(2, twice.same)
        assertEquals(3, twice.journal.entries.size)
    }

    @Test
    fun aClosedTaskNeverReopensAndTheNewestWins() {
        val device = Journal(entries = listOf(e("a", day, status = TaskStatus.DONE, updatedAt = 10), e("b", day, text = "old", updatedAt = 10)))
        val backup = Journal(entries = listOf(e("a", day, text = "renamed", updatedAt = 20), e("b", day, text = "new", updatedAt = 20), e("c", day)))
        val result = merge(device, backup)
        val a = result.journal.entries.first { it.id == "a" }
        assertEquals(TaskStatus.DONE, a.status)
        assertEquals("renamed", a.text)
        assertEquals("new", result.journal.entries.first { it.id == "b" }.text)
        assertEquals(1, result.added)
        assertEquals(2, result.updated)
        assertEquals(0, result.same)
    }

    @Test
    fun theBackupsSettingsOnlyApplyToAnEmptyDiaryAndNothingOfTheDeviceGoes() {
        val backup = Journal(entries = listOf(e("a", day)), settings = Settings(dayStartHour = 2))
        assertEquals(2, merge(Journal(), backup).journal.settings.dayStartHour)
        val device = Journal(entries = listOf(e("x", day)), settings = Settings(dayStartHour = 5))
        val merged = merge(device, backup).journal
        assertEquals(5, merged.settings.dayStartHour)
        assertTrue(merged.entries.any { it.id == "x" })
    }
}
