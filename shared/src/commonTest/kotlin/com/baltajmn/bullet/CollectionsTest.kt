package com.baltajmn.bullet

import com.baltajmn.bullet.data.fold
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.COLLECTION_TITLE_MAX
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.archiveCollection
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.model.createCollection
import com.baltajmn.bullet.model.deleteCollection
import com.baltajmn.bullet.model.filterIndex
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.migrate
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.renameCollection
import com.baltajmn.bullet.model.restoreCollection
import com.baltajmn.bullet.model.restoreDeleted
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

    // 7. Cadena y borrado (#30): crear una coleccion, migrar a ella una tarea de hoy, y la cadena de
    // `from` sigue completa al volver a Hoy.
    @Test
    fun migratingTodaysTaskIntoANewCollectionKeepsTheFromChainWhole() {
        val today = LocalDate.parse("2026-09-22")
        val start = Journal(entries = listOf(Entry(id = "e-1", text = "leer el junco", place = Place.Daily(today), createdAt = 100L, updatedAt = 100L)))

        val withCollection = start.createCollection("Lecturas 2026", now = 200L, newId = "c-1")!!
        val migrated = withCollection.migrate("e-1", Place.InCollection("c-1"), today = today, now = 300L, newId = "e-2")!!

        val original = migrated.entries.single { it.id == "e-1" }
        val landed = migrated.entries.single { it.id == "e-2" }
        assertEquals(TaskStatus.MIGRATED, original.status)
        assertEquals(Place.Daily(today), original.place)
        assertEquals("leer el junco", original.text)
        assertEquals(TaskStatus.OPEN, landed.status)
        assertEquals(Place.InCollection("c-1"), landed.place)
        // Es la copia la que enlaza de vuelta, y copyOf la encuentra desde Hoy.
        assertEquals("e-1", landed.from)
        assertEquals("e-2", copyOf(migrated, "e-1")?.id)
        assertEquals(1, migrated.migrationCount("e-2"))
    }

    @Test
    fun creatingACollectionAsksForTheTitleAndNothingElse() {
        val j = Journal().createCollection("  Lecturas 2026  ", now = 100L, newId = "c-1")!!
        val made = j.collections.single()
        assertEquals("Lecturas 2026", made.title)
        assertEquals(CollectionKind.NOTES, made.kind)
        assertEquals(100L, made.createdAt)
        assertEquals(100L, made.updatedAt)
        assertFalse(made.archived)
        assertTrue(made.rows.isEmpty())

        assertNull(Journal().createCollection("   ", now = 100L, newId = "c-1"))
        assertEquals(COLLECTION_TITLE_MAX, Journal().createCollection("x".repeat(200), now = 100L, newId = "c-1")!!.collections.single().title.length)
    }

    @Test
    fun deletingACollectionTakesItsEntriesAndLeavesASkeletonForTheOnesAMigrationCameFrom() {
        val today = LocalDate.parse("2026-09-22")
        val j = Journal(
            entries = listOf(
                // La primera es origen de una migracion que aterrizo fuera: su copia no se puede quedar colgada.
                Entry(id = "e-1", text = "origen", status = TaskStatus.MIGRATED, place = Place.InCollection("c-1"), createdAt = 100L, updatedAt = 100L),
                Entry(id = "e-2", text = "la copia", place = Place.Daily(today), createdAt = 200L, updatedAt = 200L, from = "e-1"),
                Entry(id = "e-3", text = "solo mia", place = Place.InCollection("c-1"), createdAt = 300L, updatedAt = 300L),
                Entry(id = "e-4", text = "de otra", place = Place.InCollection("c-2"), createdAt = 400L, updatedAt = 400L),
            ),
            collections = listOf(collection("c-1", "Lecturas", 100L), collection("c-2", "Otra", 200L)),
        )
        val after = j.deleteCollection("c-1", now = 500L)

        assertTrue(after.collections.none { it.id == "c-1" })
        assertTrue(after.entries.none { it.id == "e-3" })
        // El esqueleto se queda sin texto, y la copia sigue enlazada.
        val skeleton = after.entries.single { it.id == "e-1" }
        assertTrue(skeleton.gone)
        assertEquals("", skeleton.text)
        assertEquals("e-1", after.entries.single { it.id == "e-2" }.from)
        // La otra coleccion no se toca.
        assertEquals("de otra", after.entries.single { it.id == "e-4" }.text)
    }

    // El hilo se engancha por encima del hueco (docs/tecnico.md 6.7).
    @Test
    fun deletingACollectionInTheMiddleOfAThreadJoinsTheNextOneToThePrevious() {
        val j = Journal(
            collections = listOf(
                collection("c-1", "Agua, enero", 100L),
                collection("c-2", "Agua, febrero", 200L).copy(threadFrom = "c-1"),
                collection("c-3", "Agua, marzo", 300L).copy(threadFrom = "c-2"),
            ),
        )
        val after = j.deleteCollection("c-2", now = 400L)
        assertEquals("c-1", after.collections.single { it.id == "c-3" }.threadFrom)
        assertEquals(2, after.collections.size)
    }

    // Deshacer cuando el diario se movio entre medias: vuelven la coleccion y todo lo que tenia.
    @Test
    fun restoringADeletedCollectionBringsBackItsEntriesToo() {
        val gone = collection("c-1", "Lecturas", 100L)
        val its = listOf(
            Entry(id = "e-1", text = "uno", place = Place.InCollection("c-1"), createdAt = 100L, updatedAt = 100L),
            Entry(id = "e-2", text = "dos", place = Place.InCollection("c-1"), createdAt = 200L, updatedAt = 200L),
        )
        val j = Journal(entries = its, collections = listOf(gone))
        val after = j.deleteCollection("c-1", now = 300L)
        assertTrue(after.entries.isEmpty() && after.collections.isEmpty())

        // Algo mas escribio despues del borrado, asi que la reparacion es la estrecha, no la instantanea:
        // la coleccion vuelve entera y sus entradas de una en una, como las repone el repositorio.
        val moved = after.copy(entries = listOf(Entry(id = "e-9", text = "nueva", place = Place.Daily(LocalDate.parse("2026-09-22")), createdAt = 400L, updatedAt = 400L)))
        val restored = its.fold(moved.restoreCollection(gone)) { j, e -> j.restoreDeleted(e) }
        assertEquals(gone, restored.collections.single())
        assertEquals(listOf("e-9", "e-1", "e-2"), restored.entries.map { it.id })
        assertEquals("uno", restored.entries.single { it.id == "e-1" }.text)
    }
}
