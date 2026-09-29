package com.baltajmn.bullet

import com.baltajmn.bullet.data.SearchFilter
import com.baltajmn.bullet.data.search
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.entriesAt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTimedValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod

/**
 * JVM ceilings for test 26. Generous on purpose: a CI runner is slower and noisier than a phone and
 * the point is to catch an order of magnitude, a quadratic scan or a reparse per screen, not 10 %.
 */
const val PARSE_BUDGET_MS = 1000
const val SEARCH_BUDGET_MS = 200

/** 26. Rendimiento (docs/tecnico.md 6.19, #55): the same spread as tools/perf/generar.py. */
class PerfTest {
    private fun journal(): Journal {
        val rnd = Random(26)
        val start = LocalDate(2023, 9, 1)
        val collections = (0 until 12).map { BulletCollection("c-$it", "Coleccion $it", createdAt = it.toLong()) }
        val words = "comprar tinta llamar a Ana revisar notas cena reunion leer cafe con Luis".split(" ")
        val entries = (0 until 5000).map { i ->
            val day = start.plus(DatePeriod(days = rnd.nextInt(36 * 30)))
            val bullet = listOf(Bullet.TASK, Bullet.TASK, Bullet.TASK, Bullet.EVENT, Bullet.NOTE)[rnd.nextInt(5)]
            val place = when (rnd.nextInt(10)) {
                0 -> Place.Monthly(YearMonth(day.year, day.month), day.day)
                1 -> Place.InCollection(collections[rnd.nextInt(12)].id)
                else -> Place.Daily(day)
            }
            Entry(
                id = "e-$i",
                bullet = bullet,
                text = List(2 + rnd.nextInt(7)) { words[rnd.nextInt(words.size)] }.joinToString(" "),
                status = if (bullet == Bullet.TASK) TaskStatus.entries[rnd.nextInt(TaskStatus.entries.size)] else TaskStatus.OPEN,
                signifiers = if (rnd.nextInt(10) == 0) setOf(Signifier.PRIORITY) else emptySet(),
                place = place,
                order = i,
                createdAt = i.toLong(),
                updatedAt = i.toLong(),
            )
        }
        return Journal(entries = entries, collections = collections)
    }

    @Test
    fun fiveThousandEntriesDecodeAndSearchWithinBudget() {
        val text = JournalJson.encodeToString(Journal.serializer(), journal())
        val (decoded, parse) = measureTimedValue { JournalJson.decodeFromString(Journal.serializer(), text) }
        assertEquals(5000, decoded.entries.size)
        assertTrue(parse.inWholeMilliseconds < PARSE_BUDGET_MS, "decode took $parse")

        val (found, took) = measureTimedValue { search(decoded, "cafe", setOf(SearchFilter.OPEN, SearchFilter.PRIORITY)) }
        assertTrue(found.isNotEmpty())
        assertTrue(took.inWholeMilliseconds < SEARCH_BUDGET_MS, "search took $took")
        println("test 26: decode $parse, search $took")
    }

    /** A month of Mes asks for 31 places: the index answers from memory instead of 31 scans of 5.000. */
    @Test
    fun aMonthOfPlacesIsReadFromTheIndex() {
        val j = journal()
        val (_, took) = measureTimedValue {
            repeat(10) { (1..31).forEach { d -> j.entriesAt(Place.Daily(LocalDate(2025, 1, d))) } }
        }
        assertTrue(took.inWholeMilliseconds < SEARCH_BUDGET_MS, "310 places took $took")
        val day = Place.Daily(LocalDate(2025, 1, 15))
        assertEquals(j.entries.filter { it.place == day && !it.gone }.map { it.id }.sorted(), j.entriesAt(day).map { it.id }.sorted())
    }
}
