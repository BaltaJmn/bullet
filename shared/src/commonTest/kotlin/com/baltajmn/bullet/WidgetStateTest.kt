package com.baltajmn.bullet

import com.baltajmn.bullet.data.WidgetJson
import com.baltajmn.bullet.data.WidgetState
import com.baltajmn.bullet.data.widgetState
import com.baltajmn.bullet.data.widgetView
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// 12. Estado de los widgets (docs/tecnico.md 10, #40).
class WidgetStateTest {
    private val today = LocalDate.parse("2026-09-23")
    private val september = YearMonth(2026, 9)

    private fun e(id: String, place: Place, bullet: Bullet = Bullet.NOTE, status: TaskStatus = TaskStatus.OPEN) =
        Entry(id = id, bullet = bullet, text = "secreto $id", status = status, place = place, createdAt = 0L, updatedAt = 0L)

    private fun day(d: Int) = Place.Daily(LocalDate(2026, 9, d))

    /** Notes on the days of the sample's mask, an open task on the 21st, and today with the calendar line. */
    private val journal = Journal(
        entries = listOf(1, 2, 4, 5, 8, 9, 10, 12, 13, 14, 15, 17, 18, 19).map { e("n-$it", day(it)) } + listOf(
            e("late", day(21), Bullet.TASK),
            e("t-1", day(23), Bullet.TASK),
            e("t-2", day(23), Bullet.TASK),
            // The calendar line of today counts as today (docs/tecnico.md 10, test 12).
            e("t-3", Place.Monthly(september, 23), Bullet.TASK),
            e("d-1", day(23), Bullet.TASK, TaskStatus.DONE),
            e("d-2", day(23), Bullet.TASK, TaskStatus.DONE),
            e("m-1", day(23), Bullet.TASK, TaskStatus.MIGRATED),
            e("ev", day(23), Bullet.EVENT),
        ),
        collections = listOf(BulletCollection(id = "c-1", title = "Diario secreto", createdAt = 0L)),
        settings = Settings(cover = "rose"),
    )

    @Test
    fun aFixedDiaryIsExactlyTheSample() {
        assertEquals(WIDGET_SAMPLE, WidgetJson.encodeToString(WidgetState.serializer(), widgetState(journal, isPro = false, today)))
    }

    @Test
    fun noTextOfTheDiaryEverReachesTheFile() {
        val json = WidgetJson.encodeToString(WidgetState.serializer(), widgetState(journal, isPro = true, today))
        assertFalse("secreto" in json)
    }

    @Test
    fun withoutProTheCoverIsSageWhateverTheSetting() {
        assertEquals("sage", widgetState(journal, isPro = false, today).cover)
        assertEquals("rose", widgetState(journal, isPro = true, today).cover)
    }

    @Test
    fun yesterdaysFileReadsAsZerosAndAReview() {
        val st = widgetState(journal, isPro = false, today)
        val next = widgetView(st, today.plusDays(1))
        assertEquals(0, next.open)
        assertEquals(0, next.done)
        assertEquals(0, next.events)
        assertTrue(next.reviewPending)
        assertEquals(st.monthMask, next.monthMask)
        assertEquals(st, widgetView(st, today))
    }

    @Test
    fun aNewMonthStartsWithAnEmptyMaskOfItsOwnLength() {
        val st = widgetState(journal, isPro = false, today)
        val october = widgetView(st, LocalDate(2026, 10, 1))
        assertEquals("2026-10", october.month)
        assertEquals("0".repeat(31), october.monthMask)
    }

    private fun LocalDate.plusDays(n: Int) = LocalDate.fromEpochDays(toEpochDays() + n)
}
