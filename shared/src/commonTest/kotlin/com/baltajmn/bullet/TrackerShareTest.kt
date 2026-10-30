package com.baltajmn.bullet

import com.baltajmn.bullet.data.ShareFormat
import com.baltajmn.bullet.data.TrackerShare
import com.baltajmn.bullet.data.trackerPages
import com.baltajmn.bullet.data.trackerShare
import com.baltajmn.bullet.data.trackerShareText
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.TrackerRow
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.YearMonth

/** Test 53 (#77): a tracker goes out as its grid, and a story fits more rows than a post. */
class TrackerShareTest {

    private val october = YearMonth(2026, 10)
    private val language = S.lang

    @AfterTest
    fun restoreLanguage() {
        S.lang = language
    }

    @Test
    fun rowsAreNeverSplitAndAStoryHoldsMore() {
        assertEquals(listOf(0..15, 16..29), trackerPages(30, ShareFormat.POST))
        assertEquals(listOf(0..25, 26..29), trackerPages(30, ShareFormat.STORY))
        assertEquals(listOf(0..2), trackerPages(3, ShareFormat.POST))
        assertEquals(emptyList(), trackerPages(0, ShareFormat.POST))
    }

    @Test
    fun theMarksAreTheDaysOfEachRow() {
        val page = BulletCollection(
            id = "c-water", title = "Agua", createdAt = 0L, kind = CollectionKind.TRACKER, month = october,
            rows = listOf(TrackerRow("r-1", "Vasos", setOf(5, 1, 2)), TrackerRow("r-2", "Paseo")),
        )
        assertEquals(TrackerShare("Agua", october, listOf("Vasos" to setOf(1, 2, 5), "Paseo" to emptySet())), trackerShare(page, october))
    }

    @Test
    fun asTextItIsALinePerRowWithItsDays() {
        S.lang = "es"
        val t = TrackerShare("Agua", october, listOf("Vasos" to setOf(9, 1, 2, 5), "Paseo" to emptySet()))
        assertEquals("Agua, octubre de 2026\nVasos: 1, 2, 5, 9\nPaseo\n\nBobbin", trackerShareText(t))
    }
}
