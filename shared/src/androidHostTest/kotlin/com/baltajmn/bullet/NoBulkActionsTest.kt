package com.baltajmn.bullet

import com.baltajmn.bullet.model.Journal
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 9. Sin acciones masivas (docs/tecnico.md 10, #13, #27, #30). "La migración es una decisión por
 * tarea": no existe `migrateAll` ni ninguna función de `model/Migration.kt` o `model/Collections.kt`
 * que decida sobre varias entradas de una vez. Un test normal comprueba lo que hace el código que hay;
 * este comprueba lo que nadie puede escribir después sin que salte, que es la unica forma de que la
 * regla siga viva dentro de un año.
 *
 * Lo que cuenta como acción es una función que **escribe** en el diario, es decir que devuelve un
 * `Journal`. Una consulta que recibe una lista para leerla, como `filterIndex(items, query)`, no decide
 * nada sobre ninguna entrada, y prohibirla no protegería la regla: la confundiría con su forma.
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
    fun noFunctionThatWritesToTheDiaryTakesSeveralEntriesAtOnce() {
        val offenders = writers().filter { method ->
            method.parameterTypes.drop(1).any { it.isArray || it in bulkTypes }
        }
        assertTrue(
            offenders.isEmpty(),
            "Una funcion que escribe en el diario recibe varias entradas de golpe: ${offenders.map { it.name }}",
        )
    }

    @Test
    fun noPublicFunctionIsNamedAll() {
        val offenders = publicFunctions().filter { it.name.endsWith("All") }
        assertTrue(offenders.isEmpty(), "Ninguna funcion se puede llamar *All: ${offenders.map { it.name }}")
    }

    /** The receiver is a [Journal], never a collection of them: the first parameter is checked too. */
    @Test
    fun theReceiverOfAWriterIsNotACollection() {
        val receivers = writers().mapNotNull { it.parameterTypes.firstOrNull() }
        assertTrue(receivers.isNotEmpty(), "La reflexion no ha encontrado ninguna funcion que escriba en el diario")
        assertTrue(
            receivers.none { it.isArray || it in bulkTypes },
            "El receptor de una funcion que escribe es una coleccion: ${receivers.filter { it.isArray || it in bulkTypes }}",
        )
    }

    private val files = listOf("com.baltajmn.bullet.model.MigrationKt", "com.baltajmn.bullet.model.CollectionsKt")

    private fun publicFunctions() = files.flatMap { Class.forName(it).declaredMethods.toList() }
        .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic }

    /** The ones that change the diary: they hand back a [Journal]. Everything else only reads. */
    private fun writers() = publicFunctions().filter { it.returnType == Journal::class.java }
}
