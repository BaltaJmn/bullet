package com.baltajmn.bullet

import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.archiveCollection
import com.baltajmn.bullet.model.continueCollection
import com.baltajmn.bullet.model.discard
import com.baltajmn.bullet.model.inOldNotebook
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.notebookCollections
import com.baltajmn.bullet.model.notebookTasks
import com.baltajmn.bullet.model.startNotebook
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// 28. Nuevo cuaderno (docs/tecnico.md 10, 12.2, #64).
class NotebookTest {
    private val today = LocalDate(2027, 1, 1)

    private val journal = Journal(
        collections = (1..10).map {
            BulletCollection("c-$it", "Colección $it", createdAt = it.toLong(), kind = if (it == 10) CollectionKind.TRACKER else CollectionKind.NOTES, month = if (it == 10) YearMonth(2026, 12) else null)
        },
        entries = (1..20).map {
            val place = when (it % 4) {
                0 -> Place.Daily(LocalDate(2026, 12, it))
                1 -> Place.Monthly(YearMonth(2026, 12))
                2 -> Place.InCollection("c-${it % 9 + 1}")
                else -> Place.Future(YearMonth(2026, 12), null)
            }
            Entry(id = "e-$it", text = "Tarea $it", place = place, createdAt = 100L + it, updatedAt = 100L + it)
        } + Entry(id = "e-later", text = "En marzo", place = Place.Future(YearMonth(2027, 3)), createdAt = 1, updatedAt = 1),
    )

    @Test
    fun eachStepIsOneElementAndTheFutureStaysOut() {
        assertEquals(10, journal.notebookCollections().size)
        val tasks = journal.notebookTasks(today)
        assertEquals(20, tasks.size)
        assertTrue(tasks.none { it.id == "e-later" })

        // Deciding one changes that one and nothing else.
        val after = journal.discard(tasks.first().id, now = 1)!!
        assertEquals(19, after.notebookTasks(today).size)
        assertEquals(TaskStatus.OPEN, after.entries.first { it.id == tasks[1].id }.status)

        val archived = journal.archiveCollection("c-1", true, now = 1)
        assertEquals(9, archived.notebookCollections().size)
    }

    @Test
    fun theEndWritesTheDateOnceAndTheIndexFoldsWithoutDeleting() {
        val continued = journal.continueCollection("c-2", "Colección 2 bis", now = 1_000, newId = "c-new")!!.archiveCollection("c-2", true, now = 1_000)
        val done = continued.startNotebook(today)!!
        assertEquals(listOf(today), done.settings.notebooks)
        assertNull(done.startNotebook(today))
        assertEquals(continued.entries, done.entries)

        val start = 500L
        val items = done.indexItems { it.toString() }
        val old = items.filter { inOldNotebook(it, start, today) }
        assertTrue(old.any { it is IndexItem.Month && it.month == YearMonth(2026, 12) })
        assertTrue(old.any { it is IndexItem.Collection && it.id == "c-2" })
        assertFalse(old.any { it is IndexItem.Collection && it.id == "c-new" })
        assertFalse(old.any { it is IndexItem.Collection && it.id == "c-3" })
    }
}
