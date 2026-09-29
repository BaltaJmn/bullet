package com.baltajmn.bullet

import kotlinx.datetime.YearMonth
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.data.shouldAskReview
import com.baltajmn.bullet.data.Link
import com.baltajmn.bullet.data.nextReminder
import com.baltajmn.bullet.data.parseLink
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.data.paginate
import com.baltajmn.bullet.data.shareText
import com.baltajmn.bullet.data.dayShare
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** docs/tecnico.md 10: tests 10 to 20 that are not in a file of their own. */
class DataTest {

    /** Test 13 (#36, #38). */
    @Test
    fun reminderIsTheNextNineStrictlyAfterNow() {
        assertEquals(LocalDateTime(2026, 9, 29, 21, 0), nextReminder(LocalDateTime(2026, 9, 29, 20, 59), 21, 0))
        assertEquals(LocalDateTime(2026, 9, 30, 21, 0), nextReminder(LocalDateTime(2026, 9, 29, 21, 0), 21, 0))
        assertEquals(LocalDateTime(2027, 1, 1, 21, 0), nextReminder(LocalDateTime(2026, 12, 31, 22, 0), 21, 0))
    }

    @Test
    fun reminderStaysAtNineOnTheWallAcrossAChangeOfClock() {
        val madrid = TimeZone.of("Europe/Madrid")
        // Spring forward on 2026-03-29 and back on 2026-10-25: 21:00 local is 20:00 UTC in winter, 19:00 in summer.
        val spring = nextReminder(LocalDateTime(2026, 3, 28, 21, 30), 21, 0)
        assertEquals(LocalDateTime(2026, 3, 29, 21, 0), spring)
        assertEquals(LocalDateTime(2026, 3, 29, 19, 0).toInstant(TimeZone.UTC), spring.toInstant(madrid))
        val autumn = nextReminder(LocalDateTime(2026, 10, 24, 21, 30), 21, 0)
        assertEquals(LocalDateTime(2026, 10, 25, 20, 0).toInstant(TimeZone.UTC), autumn.toInstant(madrid))
    }

    /** The texts are values: nothing of the diary can reach them, because they never see it. */
    @Test
    fun reminderTextIsFixed() {
        assertTrue(S.reminderTitle.isNotBlank())
        assertTrue(S.reminderBody.isNotBlank())
    }

    /** Test 41 (#39): the part of it that is arithmetic. The thumbnail is checked by hand. */
    @Test
    fun aMinuteAwayLocksAgain() {
        assertFalse(relocks(lockOn = true, away = 59.seconds))
        assertTrue(relocks(lockOn = true, away = 60.seconds))
        assertTrue(relocks(lockOn = true, away = 61.seconds))
        assertFalse(relocks(lockOn = false, away = 61.seconds))
        assertFalse(relocks(lockOn = true, away = null))
    }

    /** Test 17 (#43): forty entries of different heights, over several pages, none split and none lost. */
    @Test
    fun aLongCollectionIsSharedOverPagesWithoutSplittingAnEntry() {
        val heights = List(40) { i -> 108 + (i % 4) * 54 }
        val pages = paginate(heights, 918)
        assertTrue(pages.size > 1)
        assertEquals((0 until 40).toList(), pages.flatMap { it.toList() })
        pages.forEach { page -> assertTrue(page.sumOf { heights[it] } <= 918) }
        // One that does not fit even an empty page goes alone and is cut, instead of being dropped.
        assertEquals(listOf(0..0, 1..1, 2..2), paginate(listOf(100, 2000, 100), 918))
        assertEquals(listOf(0 until 0), paginate(emptyList(), 918))
    }

    @Test
    fun sharedTextUsesTheAsciiOfTheExportAndNoSkeletons() {
        val day = LocalDate(2026, 9, 22)
        fun e(id: String, bullet: Bullet, status: TaskStatus = TaskStatus.OPEN, text: String = id, gone: Boolean = false, signifiers: Set<Signifier> = emptySet()) =
            Entry(id = id, bullet = bullet, text = text, status = status, place = Place.Daily(day), signifiers = signifiers, createdAt = 0L, updatedAt = 0L, gone = gone)
        val j = Journal(
            entries = listOf(
                e("a", Bullet.TASK, signifiers = setOf(Signifier.PRIORITY), text = "Renovar"),
                e("b", Bullet.TASK, TaskStatus.DONE, text = "Banco"),
                e("c", Bullet.TASK, TaskStatus.IRRELEVANT, text = "Tinta"),
                e("d", Bullet.EVENT, text = "Concierto"),
                e("e", Bullet.NOTE, text = "Jueves"),
                e("f", Bullet.TASK, text = "borrada", gone = true),
            ),
        )
        val text = shareText(dayShare(j, day))
        val lines = text.lines()
        assertEquals(S.longDateWithYear(day), lines.first())
        assertEquals(listOf("* . Renovar", "x Banco", "~~. Tinta~~", "o Concierto", "- Jueves"), lines.subList(1, 6))
        assertFalse("borrada" in text)
        assertEquals(S.appName, lines.last())
    }

    /** docs/tecnico.md 7: three links and nothing else. */
    @Test
    fun onlyTheThreeLinksAreFollowed() {
        assertEquals(Link("today"), parseLink("bobbin://today"))
        assertEquals(Link("today", focus = true), parseLink("bobbin://today?focus"))
        assertEquals(Link("review"), parseLink("bobbin://review"))
        assertEquals(Link("pro"), parseLink("bobbin://pro"))
        assertNull(parseLink("bobbin://settings"))
        assertNull(parseLink("https://today"))
        assertNull(parseLink("line://today"))
        assertNull(parseLink(null))
    }
}

/** Test 20 (docs/tecnico.md 10, #56): the rating is asked for once, after the first closed month. */
class ReviewPromptTest {
    private val sep = ReviewScope.Month(YearMonth(2026, 9))
    private val day = ReviewScope.Day(LocalDate(2026, 9, 23))

    @Test
    fun onlyTheFirstClosedMonthAsks() {
        assertTrue(shouldAskReview(asked = false, sep, decided = 4, left = 0))
        // Once asked, a second closed month does not ask again.
        assertFalse(shouldAskReview(asked = true, sep, decided = 4, left = 0))
        // Half a review, a review that opened empty and a Day review never ask.
        assertFalse(shouldAskReview(asked = false, sep, decided = 3, left = 1))
        assertFalse(shouldAskReview(asked = false, sep, decided = 0, left = 0))
        assertFalse(shouldAskReview(asked = false, day, decided = 4, left = 0))
    }
}
