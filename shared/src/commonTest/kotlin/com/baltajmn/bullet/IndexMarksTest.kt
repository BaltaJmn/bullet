package com.baltajmn.bullet

import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.groupByTheme
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.markCollection
import com.baltajmn.bullet.model.markMonth
import com.baltajmn.bullet.model.monthMigrated
import com.baltajmn.bullet.model.trackerThread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// 34. Índice por tema (docs/tecnico.md 10, 12.9, #71).
class IndexMarksTest {
    private val j = Journal(
        collections = listOf(
            BulletCollection("c-1", "Lecturas", createdAt = 100),
            BulletCollection("c-2", "Viajes", createdAt = 200),
            BulletCollection("c-3", "Recetas", createdAt = 300),
            BulletCollection("c-4", "Japón", createdAt = 400),
        ),
    )

    @Test
    fun withoutMarksTheIndexIsTheSameAsBefore() {
        val items = j.indexItems { "" }
        assertEquals(listOf("c-1", "c-2", "c-3", "c-4"), items.map { (it as IndexItem.Collection).id })
        assertTrue(items.all { it.icon == null && it.theme == null })
    }

    @Test
    fun groupsByThemeWithThePlainOnesLastInCreationOrder() {
        val marked = j.markCollection("c-4", null, " Viajes ", now = 1)!!
            .markCollection("c-2", "travel", "Viajes", now = 1)!!
            .markCollection("c-3", null, "Casa", now = 1)!!
        val groups = groupByTheme(marked.indexItems { "" })
        assertEquals(listOf("Viajes", "Casa", null), groups.map { it.first })
        assertEquals(listOf("c-2", "c-4"), groups[0].second.map { (it as IndexItem.Collection).id })
        assertEquals(listOf("c-1"), groups[2].second.map { (it as IndexItem.Collection).id })
        assertEquals("travel", marked.collections.first { it.id == "c-2" }.icon)
    }

    @Test
    fun aThemeIsOneLineOfTwentyFourAndAnUnknownIconIsNone() {
        val marked = j.markCollection("c-1", "dragon", "x".repeat(40), now = 1)!!
        val c = marked.collections.first { it.id == "c-1" }
        assertNull(c.icon)
        assertEquals(24, c.theme!!.length)
        assertNull(marked.markCollection("c-1", null, "x".repeat(40), now = 2))
    }

    @Test
    fun aTrackersMarkGoesOnEveryPage() {
        val t = Journal(
            collections = listOf(
                BulletCollection("t-1", "Agua", createdAt = 1, kind = CollectionKind.TRACKER, month = YearMonth(2026, 9)),
                BulletCollection("t-2", "Agua", createdAt = 2, kind = CollectionKind.TRACKER, month = YearMonth(2026, 10), threadFrom = "t-1"),
            ),
        ).markCollection("t-2", "health", null, now = 3)!!
        assertTrue(t.trackerThread("t-1").all { it.icon == "health" })
    }

    @Test
    fun aMonthsMarkIsKeptByKeyAndDroppedWhenEmpty() {
        val sep = YearMonth(2026, 9)
        val marked = j.markMonth(sep, "star", null)!!
        assertEquals("star", marked.months["2026-09"]?.icon)
        assertTrue("2026-09" !in marked.markMonth(sep, null, "")!!.months)
    }

    @Test
    fun aPastMonthMigratedThroughIsUnderlined() {
        val aug = YearMonth(2026, 8)
        fun task(id: String, status: TaskStatus) =
            Entry(id = id, text = "t", status = status, place = Place.Daily(LocalDate(2026, 8, 3)), createdAt = 1, updatedAt = 1)
        val today = LocalDate(2026, 9, 10)
        val migrated = Journal(entries = listOf(task("e-1", TaskStatus.MIGRATED), task("e-2", TaskStatus.DONE)))
        assertTrue(migrated.monthMigrated(aug, today))
        assertFalse(migrated.monthMigrated(aug, LocalDate(2026, 8, 20)))
        val stillOpen = Journal(entries = listOf(task("e-1", TaskStatus.MIGRATED), task("e-2", TaskStatus.OPEN)))
        assertFalse(stillOpen.monthMigrated(aug, today))
        val nothingMoved = Journal(entries = listOf(task("e-2", TaskStatus.DONE)))
        assertFalse(nothingMoved.monthMigrated(aug, today))
    }
}
