package com.baltajmn.bullet

import com.baltajmn.bullet.data.Link
import com.baltajmn.bullet.data.nextReminder
import com.baltajmn.bullet.data.parseLink
import com.baltajmn.bullet.i18n.S
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
