package com.baltajmn.bullet

import com.baltajmn.bullet.model.Journal
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 9. Sin acciones masivas (docs/tecnico.md 10, #13, #27). "La migración es una decisión por tarea": no
 * existe `migrateAll` ni ninguna función de `model/Migration.kt` que acepte una lista de entradas. Un
 * test normal comprueba lo que hace el código que hay; este comprueba lo que nadie puede escribir
 * después sin que salte, que es la unica forma de que la regla siga viva dentro de un año.
 *
 * `CollectionsKt` entra aquí con #30, que es quien crea el fichero.
 */
class NoBulkActionsTest {
    private val bulkTypes = setOf(
        Collection::class.java,
        java.util.Collection::class.java,
        Iterable::class.java,
        java.lang.Iterable::class.java,
        Sequence::class.java,
        List::class.java,
        java.util.List::class.java,
        Set::class.java,
        Map::class.java,
    )

    @Test
    fun noPublicMigrationFunctionTakesSeveralEntriesAtOnce() {
        val offenders = publicFunctions().filter { method ->
            method.parameterTypes.drop(1).any { it.isArray || it in bulkTypes }
        }
        assertTrue(
            offenders.isEmpty(),
            "Migration.kt no puede recibir varias entradas de golpe: ${offenders.map { it.name }}",
        )
    }

    @Test
    fun noPublicMigrationFunctionIsNamedAll() {
        val offenders = publicFunctions().filter { it.name.endsWith("All") }
        assertTrue(offenders.isEmpty(), "Ninguna funcion de Migration.kt se llama *All: ${offenders.map { it.name }}")
    }

    /** The receiver is a [Journal], never a collection of them: the first parameter is checked too. */
    @Test
    fun theJournalReceiverIsNotACollection() {
        val receivers = publicFunctions().mapNotNull { it.parameterTypes.firstOrNull() }
        assertTrue(receivers.isNotEmpty(), "La reflexion no ha encontrado ninguna funcion de Migration.kt")
        assertTrue(
            receivers.all { it == Journal::class.java || !(it.isArray || it in bulkTypes) },
            "El receptor de una funcion de Migration.kt es una coleccion: ${receivers.filter { it != Journal::class.java }}",
        )
    }

    private fun publicFunctions() = Class.forName("com.baltajmn.bullet.model.MigrationKt").declaredMethods
        .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic }
}
