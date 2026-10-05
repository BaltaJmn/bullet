package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.yearSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

// 33. Resumen del año (docs/tecnico.md 10, 12.8, #70).
class YearSummaryTest {
    private val y2026 = 1_780_000_000_000L // 2026-05-28
    private val y2025 = 1_750_000_000_000L // 2025-06-15
    private val place = Place.Daily(LocalDate(2026, 5, 28))

    private fun e(id: String, at: Long, bullet: Bullet = Bullet.TASK, status: TaskStatus = TaskStatus.OPEN, signifiers: Set<Signifier> = emptySet(), from: String? = null) =
        Entry(id = id, bullet = bullet, text = id, status = status, signifiers = signifiers, place = place, createdAt = at, updatedAt = at, from = from)

    @Test
    fun gathersInspirationsPrioritiesDoneAndTheMostMigrated() {
        val j = Journal(
            entries = listOf(
                e("idea", y2026, Bullet.NOTE, signifiers = setOf(Signifier.INSPIRATION)),
                e("old-idea", y2025, Bullet.NOTE, signifiers = setOf(Signifier.INSPIRATION)),
                e("prio-done", y2026, status = TaskStatus.DONE, signifiers = setOf(Signifier.PRIORITY)),
                e("prio-open", y2026, signifiers = setOf(Signifier.PRIORITY)),
                // A chain of three jumps, the last link created in 2026.
                e("a1", y2025, status = TaskStatus.MIGRATED),
                e("a2", y2026, status = TaskStatus.MIGRATED, from = "a1"),
                e("a3", y2026 + 1, status = TaskStatus.MIGRATED, from = "a2"),
                e("a4", y2026 + 2, from = "a3"),
                // Only one jump: not shown.
                e("b1", y2026, status = TaskStatus.MIGRATED),
                e("b2", y2026 + 1, from = "b1"),
            ),
        )
        val s = j.yearSummary(2026, TimeZone.UTC)
        assertEquals(listOf("idea"), s.inspirations.map { it.id })
        assertEquals(listOf("prio-done"), s.prioritiesDone.map { it.id })
        assertEquals(listOf("a4" to 3), s.mostMigrated.map { it.first.id to it.second })
    }

    @Test
    fun aYearWithoutSignifiersIsTheEmptyStateNotAZero() {
        val j = Journal(entries = listOf(e("plain", y2026), e("done", y2026, status = TaskStatus.DONE)))
        assertTrue(j.yearSummary(2026, TimeZone.UTC).isEmpty)
    }
}
