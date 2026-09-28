package com.baltajmn.bullet

import com.baltajmn.bullet.data.fold
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.COLLECTION_TITLE_MAX
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.archiveCollection
import com.baltajmn.bullet.model.filterIndex
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.renameCollection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private fun entry(id: String, place: Place, createdAt: Long, gone: Boolean = false) =
    Entry(id = id, text = if (gone) "" else "algo", place = place, createdAt = createdAt, updatedAt = createdAt, gone = gone)

private fun collection(id: String, title: String, createdAt: Long, archived: Boolean = false, kind: CollectionKind = CollectionKind.NOTES) =
    BulletCollection(id = id, title = title, createdAt = createdAt, archived = archived, kind = kind)

// 5. Indice y colecciones (docs/tecnico.md 10, #29).
class CollectionsTest {
    private val sep = YearMonth.parse("2026-09")
    private val oct = YearMonth.parse("2026-10")
    private fun title(m: YearMonth) = "${m.month.name} ${m.year}"

    @Test
    fun theIndexMixesMonthsAndCollectionsByWhenEachWasStarted() {
        val j = Journal(
            entries = listOf(
                entry("e-1", Place.Daily(LocalDate.parse("2026-09-03")), createdAt = 300L),
                entry("e-2", Place.Daily(LocalDate.parse("2026-09-01")), createdAt = 100L),
                entry("e-3", Place.Monthly(oct, 2), createdAt = 500L),
            ),
            collections = listOf(collection("c-1", "Lecturas", createdAt = 200L)),
        )
        // Septiembre nace en 100, la coleccion en 200, octubre en 500: nunca por orden alfabetico.
        assertEquals(listOf("SEPTEMBER 2026", "Lecturas", "OCTOBER 2026"), j.indexItems(::title).map { it.title })
    }

    @Test
    fun aMonthWithNoEntryIsNotInTheIndexAndNeitherIsADailyLogOnItsOwn() {
        val j = Journal(
            entries = listOf(
                entry("e-1", Place.Daily(LocalDate.parse("2026-09-03")), createdAt = 100L),
                // Un esqueleto no es contenido, y el Future Log no hace mes.
                entry("e-2", Place.Monthly(oct), createdAt = 200L, gone = true),
                entry("e-3", Place.Future(YearMonth.parse("2026-12"), 1), createdAt = 300L),
            ),
        )
        val months = j.indexItems(::title).filterIsInstance<IndexItem.Month>()
        assertEquals(listOf(sep), months.map { it.month })
        // El Daily del 3 de septiembre no sale como fila propia: solo su mes.
        assertEquals(1, j.indexItems(::title).size)
    }

    @Test
    fun renamingACollectionLeavesItWhereItWasInTheIndex() {
        val j = Journal(
            entries = listOf(entry("e-1", Place.Daily(LocalDate.parse("2026-09-03")), createdAt = 500L)),
            collections = listOf(collection("c-1", "Lecturas", createdAt = 100L)),
        )
        assertEquals(listOf("Lecturas", "SEPTEMBER 2026"), j.indexItems(::title).map { it.title })

        // "Zzz" iria al final por orden alfabetico, y "Aaa" al principio: ninguno de los dos se mueve.
        val renamed = j.renameCollection("c-1", "Zzz un titulo nuevo", now = 900L)!!
        assertEquals(listOf("Zzz un titulo nuevo", "SEPTEMBER 2026"), renamed.indexItems(::title).map { it.title })
        assertEquals(100L, renamed.collections.single().createdAt)
        assertEquals(900L, renamed.collections.single().updatedAt)

        assertNull(j.renameCollection("c-1", "   ", now = 900L))
        assertNull(j.renameCollection("c-1", "Lecturas", now = 900L))
        assertEquals(COLLECTION_TITLE_MAX, j.renameCollection("c-1", "x".repeat(200), now = 900L)!!.collections.single().title.length)
    }

    @Test
    fun archivedCollectionsGoLastAndNoEntryIsTouched() {
        val j = Journal(
            entries = listOf(entry("e-1", Place.InCollection("c-1"), createdAt = 100L)),
            collections = listOf(collection("c-1", "Vieja", createdAt = 100L), collection("c-2", "Nueva", createdAt = 900L)),
        )
        val archived = j.archiveCollection("c-1", archived = true, now = 1000L)

        assertEquals(listOf("Nueva", "Vieja"), archived.indexItems(::title).map { it.title })
        assertTrue((archived.indexItems(::title).last() as IndexItem.Collection).archived)
        // Archivar no borra ni mueve ninguna entrada.
        assertEquals(j.entries, archived.entries)
        // Archivar lo ya archivado no escribe nada, ni un updatedAt nuevo.
        assertEquals(archived, archived.archiveCollection("c-1", archived = true, now = 2000L))
        assertFalse(archived.archiveCollection("c-1", archived = false, now = 2000L).collections.single { it.id == "c-1" }.archived)
    }

    @Test
    fun theFilterIgnoresCaseAndAccentsAndAnEmptyQueryFiltersNothing() {
        val items = listOf(
            IndexItem.Collection("c-1", "Canción", 100L),
            IndexItem.Collection("c-2", "Lecturas", 200L),
            IndexItem.Collection("c-3", "Straße", 300L),
        )
        assertEquals(listOf("Canción"), filterIndex(items, "cancion").map { it.title })
        assertEquals(listOf("Canción"), filterIndex(items, "CANCIÓN").map { it.title })
        assertEquals(listOf("Lecturas"), filterIndex(items, "LECTURAS").map { it.title })
        assertEquals(listOf("Straße"), filterIndex(items, "strasse").map { it.title })
        assertEquals(3, filterIndex(items, "   ").size)
        assertTrue(filterIndex(items, "zzz").isEmpty())
    }

    // Un acento descompuesto (NFD) llega de pegar texto y de algunos teclados.
    @Test
    fun foldReadsADecomposedAccentAsTheSameLetter() {
        assertEquals("cafe", fold("Café"))
        assertEquals("cafe", fold("Café"))
        assertEquals(fold("Café"), fold("Café"))
        assertEquals("strasse", fold("Straße"))
    }
}
