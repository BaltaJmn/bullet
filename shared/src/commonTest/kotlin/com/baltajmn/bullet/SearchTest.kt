package com.baltajmn.bullet

import com.baltajmn.bullet.data.GroupKey
import com.baltajmn.bullet.data.SearchFilter
import com.baltajmn.bullet.data.search
import com.baltajmn.bullet.data.tags
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private fun e(
    id: String,
    text: String,
    place: Place = Place.Daily(LocalDate.parse("2026-09-22")),
    status: TaskStatus = TaskStatus.OPEN,
    bullet: Bullet = Bullet.TASK,
    signifiers: Set<Signifier> = emptySet(),
    gone: Boolean = false,
    createdAt: Long = 0L,
) = Entry(
    id = id, bullet = bullet, text = text, status = status, signifiers = signifiers,
    place = place, createdAt = createdAt, updatedAt = createdAt, gone = gone,
)

// 11. Busqueda (docs/tecnico.md 10, #35).
class SearchTest {
    private val day = LocalDate.parse("2026-09-22")

    @Test
    fun theSameWordIsFoundWithAccentsWithoutThemAndDecomposed() {
        val j = Journal(
            entries = listOf(
                e("e-1", "Café con Marta"),
                e("e-2", "cafe solo"),
                e("e-3", "Café descompuesto"),
                e("e-4", "nada que ver"),
            ),
        )
        val found = search(j, "cafe", emptySet()).flatMap { it.entries }.map { it.id }
        assertEquals(setOf("e-1", "e-2", "e-3"), found.toSet())
        // Y al contrario: buscar con acento encuentra lo escrito sin el.
        assertEquals(3, search(j, "Café", emptySet()).flatMap { it.entries }.size)
    }

    @Test
    fun theOpenFilterLeavesOutDoneMigratedScheduledAndDiscarded() {
        val j = Journal(
            entries = listOf(
                e("abierta", "cafe", status = TaskStatus.OPEN),
                e("hecha", "cafe", status = TaskStatus.DONE),
                e("migrada", "cafe", status = TaskStatus.MIGRATED),
                e("programada", "cafe", status = TaskStatus.SCHEDULED),
                e("descartada", "cafe", status = TaskStatus.IRRELEVANT),
                e("nota", "cafe", bullet = Bullet.NOTE),
            ),
        )
        assertEquals(6, search(j, "cafe", emptySet()).flatMap { it.entries }.size)
        // Abiertas es tarea abierta: una nota tampoco es una tarea abierta.
        assertEquals(listOf("abierta"), search(j, "cafe", setOf(SearchFilter.OPEN)).flatMap { it.entries }.map { it.id })
    }

    @Test
    fun aTagIsFoundByTypingItAndDoesNotMatchAWordThatMerelyStartsTheSame() {
        val j = Journal(
            entries = listOf(
                e("e-1", "Billetes #viaje"),
                e("e-2", "un viajero cualquiera"),
                e("e-3", "#viajes en plural"),
            ),
        )
        assertEquals(listOf("e-1"), search(j, "#viaje", emptySet()).flatMap { it.entries }.map { it.id })
        // Sin la almohadilla es una busqueda de texto normal, y ahi si entra "viajero".
        assertEquals(setOf("e-1", "e-2", "e-3"), search(j, "viaje", emptySet()).flatMap { it.entries }.map { it.id }.toSet())
    }

    @Test
    fun aTagIsCutAtTheFirstCharacterThatIsNotOneOfItsOwn() {
        assertEquals(setOf("viaje"), tags("Billetes #viaje, manana"))
        assertEquals(setOf("viaje-2026", "casa_nueva"), tags("#viaje-2026 y #casa_nueva."))
        assertEquals(setOf("cafe"), tags("#Café"))
        assertTrue(tags("nada de # solo").isEmpty())
    }

    @Test
    fun filtersCombineWithAnd() {
        val j = Journal(
            entries = listOf(
                e("ambas", "cafe", signifiers = setOf(Signifier.PRIORITY)),
                e("solo-prioridad", "cafe", status = TaskStatus.DONE, signifiers = setOf(Signifier.PRIORITY)),
                e("solo-abierta", "cafe"),
            ),
        )
        assertEquals(
            listOf("ambas"),
            search(j, "cafe", setOf(SearchFilter.OPEN, SearchFilter.PRIORITY)).flatMap { it.entries }.map { it.id },
        )
    }

    @Test
    fun groupsComeOutNewestPageFirstAndCollectionsAfterEveryDatedPage() {
        val j = Journal(
            entries = listOf(
                e("viejo", "cafe", place = Place.Daily(LocalDate.parse("2026-08-01"))),
                e("nuevo", "cafe", place = Place.Daily(LocalDate.parse("2026-09-30"))),
                e("mes", "cafe", place = Place.Monthly(YearMonth.parse("2026-09"))),
                e("coleccion", "cafe", place = Place.InCollection("c-1")),
            ),
            collections = listOf(BulletCollection(id = "c-1", title = "Lecturas", createdAt = 100L)),
        )
        val keys = search(j, "cafe", emptySet()).map { it.place }
        assertEquals(
            listOf(
                GroupKey.Day(LocalDate.parse("2026-09-30")),
                GroupKey.Month(YearMonth.parse("2026-09")),
                GroupKey.Day(LocalDate.parse("2026-08-01")),
                GroupKey.InCollection("c-1"),
            ),
            keys,
        )
    }

    @Test
    fun skeletonsNeverComeOutAndAnEmptyQueryWithNoFilterFindsNothing() {
        val j = Journal(entries = listOf(e("fantasma", "", gone = true), e("real", "cafe")))
        assertEquals(listOf("real"), search(j, "cafe", emptySet()).flatMap { it.entries }.map { it.id })
        assertTrue(search(j, "   ", emptySet()).isEmpty())
        // Solo filtros: todo lo que los cumple.
        assertEquals(listOf("real"), search(j, "", setOf(SearchFilter.OPEN)).flatMap { it.entries }.map { it.id })
    }
}
