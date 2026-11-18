package com.baltajmn.bullet

import kotlinx.datetime.YearMonth
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.data.shouldAskReview
import com.baltajmn.bullet.data.OfferPrefs
import com.baltajmn.bullet.data.Offers
import com.baltajmn.bullet.data.PREF_BOOK_CARD_DAY
import com.baltajmn.bullet.data.PREF_OFFERS_INIT
import com.baltajmn.bullet.data.PREF_PRO_SEEN_MS
import com.baltajmn.bullet.data.PREF_REVIEW_ASKED
import com.baltajmn.bullet.data.PREF_REVIEW_LAST_MS
import com.baltajmn.bullet.data.WINBACK_MAX
import com.baltajmn.bullet.data.isGoodMoment
import com.baltajmn.bullet.data.isWorkDay
import com.baltajmn.bullet.data.shouldShowBookCard
import com.baltajmn.bullet.data.shouldShowWinBack
import com.baltajmn.bullet.data.shouldShowEarlyBookCard
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
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

/** Test 20 (docs/tecnico.md 10, #56): the rating is asked for once per version, 120 days apart, after a real success. */
class ReviewPromptTest {
    private val sep = ReviewScope.Month(YearMonth(2026, 9))
    private val day = ReviewScope.Day(LocalDate(2026, 9, 23))
    private val earlier = ReviewScope.Earlier(LocalDate(2026, 9, 23))
    private val dayMs = 24L * 60 * 60 * 1000
    private val hourMs = 60L * 60 * 1000
    private val installed = 1_000_000_000L
    private val later = installed + 3 * dayMs

    private fun ask(
        scope: ReviewScope = sep,
        decided: Int = 4,
        left: Int = 0,
        good: Boolean = isGoodMoment(scope, decided, left),
        installedAt: Long? = installed,
        now: Long = later,
        lastAsk: Long? = null,
        lastVersion: String? = null,
        version: String = "1.1.0",
        touch: Long? = null,
        proToday: Boolean = false,
        overlay: Boolean = false,
        error: Boolean = false,
        quiet: Boolean = false,
        tried: Boolean = false,
    ) = shouldAskReview(good, installedAt, now, lastAsk, lastVersion, version, touch, proToday, overlay, error, quiet, tried)

    @Test
    fun onlyAReviewThatClosesEverythingAsks() {
        assertTrue(ask())
        assertFalse(ask(decided = 3, left = 1))
        assertFalse(ask(decided = 0))
        assertFalse(ask(scope = day, decided = 0))
        assertFalse(ask(scope = day, decided = 3, left = 1))
    }

    @Test
    fun aDayOrEarlierReviewThatClosesEverythingAsksOnDayThreeOrLater() {
        assertTrue(ask(scope = day))
        assertTrue(ask(scope = earlier))
        assertFalse(ask(scope = day, now = installed + dayMs))
    }

    @Test
    fun needsTwoFullDaysSinceInstall() {
        assertFalse(ask(now = installed + 2 * dayMs - 1))
        assertTrue(ask(now = installed + 2 * dayMs))
        // Without a stored first launch there is nothing to count from.
        assertFalse(ask(installedAt = null))
    }

    @Test
    fun oncePerVersionAndAtLeast120DaysApart() {
        val asked = later
        // Same version never again, however long it has been.
        assertFalse(ask(lastAsk = asked, lastVersion = "1.1.0", now = asked + 400 * dayMs))
        // A new version still waits for the 120 days.
        assertFalse(ask(lastAsk = asked, lastVersion = "1.1.0", version = "1.2.0", now = asked + 119 * dayMs))
        assertTrue(ask(lastAsk = asked, lastVersion = "1.1.0", version = "1.2.0", now = asked + 120 * dayMs))
        // An ask from before versions were stored (version unknown) counts from its date, and is not forever.
        assertFalse(ask(lastAsk = asked, lastVersion = null, now = asked + 10 * dayMs))
        assertTrue(ask(lastAsk = asked, lastVersion = null, now = asked + 121 * dayMs))
    }

    @Test
    fun aNewProTouchKeepsItAwayForADay() {
        assertFalse(ask(touch = later - 23 * hourMs))
        assertTrue(ask(touch = later - 24 * hourMs))
        assertTrue(ask(touch = null))
    }

    @Test
    fun neverOverASheetNorAfterAnErrorNorOnTheQuietLaunch() {
        assertFalse(ask(overlay = true))
        assertFalse(ask(error = true))
        assertFalse(ask(quiet = true))
    }

    @Test
    fun aFinishedTaskOnTheThirdDayWithEntriesIsAGoodMomentToo() {
        val d = (1..4).map { LocalDate(2026, 9, it) }
        assertFalse(isWorkDay(d.take(2), d[1]))
        assertTrue(isWorkDay(d.take(3), d[2]))
        // Days after today and repeats do not count.
        assertFalse(isWorkDay(listOf(d[0], d[0], d[1], d[3]), d[1]))
        assertTrue(ask(good = isWorkDay(d, d[3])))
        assertFalse(ask(good = false))
    }

    @Test
    fun theRatingNeverSharesADayWithAProSurface() {
        assertFalse(ask(proToday = true))
    }

    @Test
    fun aFailedRequestIsNotRetriedInTheSameSession() {
        assertFalse(ask(tried = true))
    }

    @Test
    fun updatingFromABuildWithoutTheNewKeys() {
        // Missing keys read as null: nobody is locked out forever, and the quiet launch is the only wait.
        assertTrue(ask(lastAsk = null, lastVersion = null, touch = null))
        assertFalse(ask(lastAsk = null, quiet = true))
    }
}

/** The first-time card that offers the A5 book the day after the first month closed. */
class BookCardTest {
    private val closed = LocalDate(2026, 11, 30)
    private val next = LocalDate(2026, 12, 1)

    private fun show(
        pro: Boolean = false,
        done: Boolean = false,
        first: LocalDate? = closed,
        ask: LocalDate? = null,
        shown: LocalDate? = null,
        today: LocalDate = next,
    ) = shouldShowBookCard(pro, done, first, ask, shown, today)

    @Test
    fun showsFromTheDayAfterTheFirstMonthClosed() {
        assertFalse(show(today = closed))
        assertTrue(show())
        assertTrue(show(today = LocalDate(2026, 12, 9)))
        assertFalse(show(first = null))
    }

    @Test
    fun onlyOnceAndNeverForProOrOnTheRatingDay() {
        assertFalse(show(done = true))
        assertFalse(show(pro = true))
        assertFalse(show(ask = next))
        assertTrue(show(ask = closed))
    }

    @Test
    fun anIgnoredCardGoesAwayByItselfAfterThreeDays() {
        assertTrue(show(shown = next, today = LocalDate(2026, 12, 3)))
        assertFalse(show(shown = next, today = LocalDate(2026, 12, 4)))
    }
}

class EarlyBookCardTest {
    private val days = listOf(LocalDate(2026, 11, 1), LocalDate(2026, 11, 2), LocalDate(2026, 11, 4), LocalDate(2026, 11, 4))
    private val today = LocalDate(2026, 11, 5)

    private fun show(
        pro: Boolean = false,
        done: Boolean = false,
        d: List<LocalDate> = days,
        count: Int = 5,
        ask: LocalDate? = null,
        shown: LocalDate? = null,
        now: LocalDate = today,
    ) = shouldShowEarlyBookCard(pro, done, d, count, ask, shown, now)

    @Test
    fun needsThreeDaysAndFiveEntriesAndWaitsForTheNextDay() {
        assertTrue(show())
        assertFalse(show(now = LocalDate(2026, 11, 4)))
        assertFalse(show(count = 4))
        assertFalse(show(d = days.take(2)))
    }

    @Test
    fun onceNeverForProNorOnTheRatingDay() {
        assertFalse(show(done = true))
        assertFalse(show(pro = true))
        assertFalse(show(ask = today))
    }

    @Test
    fun theFirstAppearanceWaitsForAGoodMomentButTheStayDoesNot() {
        assertFalse(shouldShowEarlyBookCard(false, false, days, 5, null, null, today, goodMoment = false))
        assertTrue(shouldShowEarlyBookCard(false, false, days, 5, null, null, today, goodMoment = true))
        assertTrue(shouldShowEarlyBookCard(false, false, days, 5, null, today, LocalDate(2026, 11, 6), goodMoment = false))
    }

    @Test
    fun sittingOnHoyOnLaterDaysIsNotForever() {
        assertTrue(show(shown = today, now = LocalDate(2026, 11, 7)))
        assertFalse(show(shown = today, now = LocalDate(2026, 11, 8)))
    }
}

/** The one quiet return of the Pro card: 7 days after the last Pro surface, after a good moment. */
class WinBackTest {
    private val dayMs = 24L * 60 * 60 * 1000
    private val seen = 2_000_000_000L
    private val today = LocalDate(2026, 12, 10)

    private fun back(
        pro: Boolean = false,
        good: Boolean = true,
        rated: Boolean = false,
        lastSeen: Long? = seen,
        lastBack: Long? = null,
        count: Int = 0,
        ask: LocalDate? = null,
        now: Long = seen + 7 * dayMs,
    ) = shouldShowWinBack(pro, good, rated, lastSeen, lastBack, count, ask, today, now)

    @Test
    fun sevenDaysAfterTheLastSurfaceAndOnlyAfterAGoodMoment() {
        assertTrue(back())
        assertFalse(back(now = seen + 7 * dayMs - 1))
        assertFalse(back(good = false))
        // Never shown anything: nothing to return to, the first-time card owns that.
        assertFalse(back(lastSeen = null))
    }

    @Test
    fun neverForProNorOnARatingDayNorTwiceAWeekNorForever() {
        assertFalse(back(pro = true))
        assertFalse(back(rated = true))
        assertFalse(back(ask = today))
        assertFalse(back(lastBack = seen + 7 * dayMs - dayMs, now = seen + 7 * dayMs))
        assertTrue(back(lastBack = seen, now = seen + 14 * dayMs))
        assertFalse(back(count = WINBACK_MAX))
    }
}

/** A realistic first fortnight of a free person who writes every day, put through the pure rules. */
class FirstFortnightTest {
    private val dayMs = 24L * 60 * 60 * 1000
    private val installed = 5_000_000_000L
    private val start = LocalDate(2026, 12, 1)

    @Test
    fun cardOnDayFourThenRatingThenTheReturnOnDayThirteen() {
        val entryDays = (0..13).map { LocalDate(2026, 12, 1 + it) }
        // Day 1 and 2 (index 0 and 1): no card, whatever else.
        assertFalse(shouldShowEarlyBookCard(false, false, entryDays.take(2), 8, null, null, start.plus(DatePeriod(days = 1))))
        // The third day with entries is index 2: the card comes the day after.
        assertFalse(shouldShowEarlyBookCard(false, false, entryDays.take(3), 12, null, null, start.plus(DatePeriod(days = 2))))
        val cardDay = start.plus(DatePeriod(days = 3))
        assertTrue(shouldShowEarlyBookCard(false, false, entryDays.take(4), 16, null, null, cardDay))
        val touch = installed + 3 * dayMs
        // A Day review that same day is too soon after the touch; the next day it may ask.
        fun ask(at: Long) = shouldAskReview(true, installed, at, null, null, "1.0.0", touch, false, false, false, false, false)
        assertFalse(ask(touch + 5 * 60 * 60 * 1000))
        assertTrue(ask(touch + dayMs))
        // The card was left alone for three days and is gone; the return needs 7 days and a good moment.
        val last = touch + 2 * dayMs
        assertFalse(shouldShowEarlyBookCard(false, false, entryDays, 40, null, cardDay, start.plus(DatePeriod(days = 6))))
        assertFalse(shouldShowWinBack(false, true, false, last, null, 0, null, start.plus(DatePeriod(days = 8)), last + 6 * dayMs))
        assertTrue(shouldShowWinBack(false, true, false, last, null, 0, null, start.plus(DatePeriod(days = 9)), last + 7 * dayMs))
        // The same day the rating was asked for, it stays away.
        assertFalse(shouldShowWinBack(false, true, true, last, null, 0, start.plus(DatePeriod(days = 9)), start.plus(DatePeriod(days = 9)), last + 7 * dayMs))
    }
}

/** The quiet launch is one launch, and the keys of the 1.0.0 build migrate without spamming or locking out. */
class OfferStateTest {
    private class Fake(val m: MutableMap<String, String> = mutableMapOf()) : OfferPrefs {
        override fun bool(key: String) = m[key] == "1"
        override fun setBool(key: String, value: Boolean) { m[key] = if (value) "1" else "0" }
        override fun string(key: String) = m[key]
        override fun setString(key: String, value: String?) { if (value == null) m.remove(key) else m[key] = value }
    }

    @Test
    fun theFirstLaunchIsQuietAndTheNextForegroundIsNot() {
        Offers.resetForTest()
        Offers.begin(1_000L, Fake())
        assertTrue(Offers.quiet)
        Offers.resumed(away = false)
        assertFalse(Offers.quiet)
        // A cold start with the flag already set is not quiet at all.
        Offers.resetForTest()
        Offers.begin(2_000L, Fake(mutableMapOf(PREF_OFFERS_INIT to "1")))
        assertFalse(Offers.quiet)
    }

    @Test
    fun aTaskDoneOnAGoodDayWantsTheRatingUnlessQuiet() {
        val days = (1..3).map { LocalDate(2026, 9, it) }
        Offers.resetForTest()
        Offers.begin(1_000L, Fake(mutableMapOf(PREF_OFFERS_INIT to "1")))
        Offers.taskCompleted(days.take(2), days[1])
        assertFalse(Offers.ratingWanted)
        Offers.taskCompleted(days, days[2])
        assertTrue(Offers.ratingWanted)
        assertTrue(Offers.goodMoment)
        Offers.resetForTest()
        Offers.begin(1_000L, Fake())
        Offers.taskCompleted(days, days[2])
        assertFalse(Offers.ratingWanted)
    }

    @Test
    fun keysOfThe100BuildStartCountingFromTheUpdate() {
        val old = Fake(mutableMapOf(PREF_REVIEW_ASKED to "1", PREF_BOOK_CARD_DAY to "2026-11-10"))
        Offers.resetForTest()
        Offers.begin(5_000L, old)
        assertEquals("5000", old.string(PREF_REVIEW_LAST_MS))
        assertEquals("5000", old.string(PREF_PRO_SEEN_MS))
        assertTrue(old.bool(PREF_OFFERS_INIT))
        // Someone who never saw either gets neither invented.
        val fresh = Fake()
        Offers.resetForTest()
        Offers.begin(5_000L, fresh)
        assertNull(fresh.string(PREF_REVIEW_LAST_MS))
        assertNull(fresh.string(PREF_PRO_SEEN_MS))
    }
}
