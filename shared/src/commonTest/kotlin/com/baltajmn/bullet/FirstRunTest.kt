package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.capture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

/**
 * El primer arranque (#31, SPEC 6): "cero configuracion antes de escribir". No hay pantalla previa que
 * pasar, ni ajuste que elegir, ni nada que marcar como visto.
 */
class FirstRunTest {
    @Test
    fun aCleanInstallWritesItsFirstBulletWithNoSetupAtAll() {
        val today = LocalDate.parse("2026-09-22")
        val fresh = Journal()

        // Nada que decidir antes: todos los ajustes tienen su valor por defecto.
        assertEquals(Settings(), fresh.settings)
        assertTrue(fresh.entries.isEmpty() && fresh.collections.isEmpty())

        val after = fresh.capture("llamar al fontanero", Place.Daily(today), picked = null, now = 1L, newId = "e-1")!!
        val first = after.entries.single()
        assertEquals("llamar al fontanero", first.text)
        assertEquals(Bullet.TASK, first.bullet)
        assertEquals(TaskStatus.OPEN, first.status)
        assertEquals(Place.Daily(today), first.place)
        // Escribir el primer bullet no ha tocado ningun ajuste, asi que no deja nada pendiente.
        assertEquals(Settings(), after.settings)
    }

    /**
     * "Once destinos y no mas" (docs/pantallas.md 3). La guia es el unico que se abre solo, una vez,
     * y no pide nada: ni alta, ni permisos, ni ajustes (13.1). Si alguien anade otro paso de entrada,
     * aqui salta. El que falta, PRO, es el `Paywall`, un dialogo y no una pantalla (docs/tecnico.md 6.16).
     */
    @Test
    fun theGuideIsTheOnlyFirstStartDestination() {
        assertEquals(
            listOf("TODAY", "MONTH", "FUTURE", "INDEX", "COLLECTION", "REVIEW", "SEARCH", "KEY", "SETTINGS", "GUIDE"),
            Screen.entries.map { it.name },
        )
    }
}
