package com.baltajmn.bullet

import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.clampCodePoints
import com.baltajmn.bullet.model.codePointCount
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.logicalDate
import com.baltajmn.bullet.model.nextDayStart
import com.baltajmn.bullet.model.normalized
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.toggleDone
import com.baltajmn.bullet.model.weekStarts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

// A smiling face, U+1F642, built from its surrogates so no emoji lives in the source.
private val SMILE = "${0xD83D.toChar()}${0xDE42.toChar()}"

private val json = Json { ignoreUnknownKeys = true }

private fun entry(
    place: Place,
    bullet: Bullet = Bullet.TASK,
    status: TaskStatus = TaskStatus.OPEN,
    text: String = "texto",
) = Entry(
    id = "e-3f9a1c2e",
    bullet = bullet,
    text = text,
    status = status,
    signifiers = setOf(Signifier.PRIORITY, Signifier.EXPLORE),
    place = place,
    order = 3,
    createdAt = 1790064000000,
    updatedAt = 1790150400000,
    from = "e-00000000",
)

class ModelTest {

    // 1. Serialization: the Entry and Place half. Journal joins in #12.
    @Test
    fun entryRoundTripKeepsEveryField() {
        val text = "Dijo \"vale\" y llamó $SMILE, café y niño"
        for (place in listOf(
            Place.Daily(LocalDate.parse("2026-09-22")),
            Place.Monthly(YearMonth.parse("2026-10")),
            Place.Monthly(YearMonth.parse("2026-10"), day = 3),
            Place.Future(YearMonth.parse("2027-02")),
            Place.Future(YearMonth.parse("2027-02"), day = 14),
            Place.InCollection("c-1d2e7a40"),
        )) {
            for (bullet in Bullet.entries) {
                for (status in TaskStatus.entries) {
                    val e = entry(place, bullet = bullet, status = status, text = text)
                    val encoded = json.encodeToString(Entry.serializer(), e)
                    assertEquals(e, json.decodeFromString(Entry.serializer(), encoded))
                }
            }
        }
    }

    @Test
    fun placeWritesExactlyOneKeyPlusOptionalDay() {
        val daily = json.encodeToString(Place.serializer(), Place.Daily(LocalDate.parse("2026-09-22")))
        assertEquals("""{"daily":"2026-09-22"}""", daily)

        val monthNoDay = json.encodeToString(Place.serializer(), Place.Monthly(YearMonth.parse("2026-10")))
        assertEquals("""{"monthly":"2026-10"}""", monthNoDay)

        val monthWithDay = json.encodeToString(Place.serializer(), Place.Monthly(YearMonth.parse("2026-10"), 3))
        assertEquals("""{"monthly":"2026-10","day":3}""", monthWithDay)

        val collection = json.encodeToString(Place.serializer(), Place.InCollection("c-1d2e7a40"))
        assertEquals("""{"collection":"c-1d2e7a40"}""", collection)
    }

    @Test
    fun monthlyWithAndWithoutDayAreDistinctAfterARoundTrip() {
        val withDay = Place.Monthly(YearMonth.parse("2026-10"), 3)
        val withoutDay = Place.Monthly(YearMonth.parse("2026-10"))
        assertNotEquals(withDay, withoutDay)

        val decodedWithDay = json.decodeFromString(Place.serializer(), json.encodeToString(Place.serializer(), withDay))
        val decodedWithoutDay = json.decodeFromString(Place.serializer(), json.encodeToString(Place.serializer(), withoutDay))
        assertEquals(withDay, decodedWithDay)
        assertEquals(withoutDay, decodedWithoutDay)
        assertNotEquals(decodedWithDay, decodedWithoutDay)
    }

    @Test
    fun placeWithTwoKeysOrADayOutOfTheMonthDoesNotDecode() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(Place.serializer(), """{"daily":"2026-09-22","monthly":"2026-10"}""")
        }
        assertFailsWith<SerializationException> {
            // 2026-04 has 30 days.
            json.decodeFromString(Place.serializer(), """{"monthly":"2026-04","day":31}""")
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString(Place.serializer(), """{"daily":"2026-09-22","day":1}""")
        }
    }

    // 2. Entry invariants.
    @Test
    fun anEventOrANoteAlwaysNormalizesToOpen() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        for (bullet in listOf(Bullet.EVENT, Bullet.NOTE)) {
            for (status in TaskStatus.entries) {
                assertEquals(TaskStatus.OPEN, entry(place, bullet = bullet, status = status).normalized().status)
            }
        }
        // A TASK keeps whatever status it already has.
        assertEquals(TaskStatus.DONE, entry(place, bullet = Bullet.TASK, status = TaskStatus.DONE).normalized().status)
    }

    @Test
    fun toggleDoneOnlyEverTouchesATask() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val task = entry(place, bullet = Bullet.TASK, status = TaskStatus.OPEN)
        assertEquals(TaskStatus.DONE, task.toggleDone().status)
        assertEquals(TaskStatus.OPEN, task.toggleDone().toggleDone().status)

        for (bullet in listOf(Bullet.EVENT, Bullet.NOTE)) {
            val e = entry(place, bullet = bullet, status = TaskStatus.OPEN)
            assertEquals(e, e.toggleDone())
        }
    }

    @Test
    fun oneLineTurnsEveryBreakIntoASpace() {
        assertEquals("a b c", "a\nb\r\nc".oneLine())
        assertEquals("a b", "a\rb".oneLine())
        assertEquals("sin saltos", "sin saltos".oneLine())
    }

    @Test
    fun anEmojiCountsAsOne() {
        assertEquals(1, SMILE.codePointCount())
        assertEquals(3, "a${SMILE}b".codePointCount())
    }

    @Test
    fun theCutAtFiveHundredNeverSplitsAnEmojiRightAtTheEdge() {
        val text = "a".repeat(TEXT_LIMIT - 1) + SMILE + "b".repeat(10)
        val cut = text.clampCodePoints(TEXT_LIMIT)
        assertEquals(TEXT_LIMIT, cut.codePointCount())
        assertTrue(cut.endsWith(SMILE))
    }

    // 1. Serialization: the Journal half.
    @Test
    fun emptyJournalRoundTripsToTheExactMinimalJson() {
        val encoded = JournalJson.encodeToString(Journal.serializer(), Journal())
        assertEquals("""{"schemaVersion":1,"entries":[],"collections":[]}""", encoded)
        assertEquals(Journal(), JournalJson.decodeFromString(Journal.serializer(), encoded))
    }

    @Test
    fun journalOmitsDefaultsButAlwaysKeepsTheContainerFields() {
        val plain = Entry(
            id = "e-11111111",
            text = "simple",
            place = Place.Daily(LocalDate.parse("2026-09-22")),
            createdAt = 1L,
            updatedAt = 1L,
        )
        val j = Journal(entries = listOf(plain))
        val encoded = JournalJson.encodeToString(Journal.serializer(), j)

        assertEquals(j, JournalJson.decodeFromString(Journal.serializer(), encoded))
        assertTrue("\"schemaVersion\":1" in encoded)
        assertTrue("\"collections\":[]" in encoded)
        assertFalse("\"settings\"" in encoded)
        assertFalse("\"from\"" in encoded)
        assertFalse("\"gone\"" in encoded)
        assertFalse("\"signifiers\"" in encoded)
        assertFalse("\"order\"" in encoded)
        assertFalse("\"bullet\"" in encoded)
        assertFalse("\"status\"" in encoded)
    }

    @Test
    fun journalRoundTripKeepsEntriesAndCollections() {
        val text = "Dijo \"vale\" $SMILE café"
        val j = Journal(
            entries = listOf(entry(Place.Daily(LocalDate.parse("2026-09-22")), text = text)),
            collections = listOf(BulletCollection(id = "c-1d2e7a40", title = "Lecturas", createdAt = 1L)),
        )
        val encoded = JournalJson.encodeToString(Journal.serializer(), j)
        assertEquals(j, JournalJson.decodeFromString(Journal.serializer(), encoded))
    }

    // 3. Logical day and week.
    @Test
    fun dayStartsAtFourByDefault() {
        assertEquals(LocalDate.parse("2026-09-22"), logicalDate(LocalDateTime(2026, 9, 23, 2, 30)))
        assertEquals(LocalDate.parse("2026-09-22"), logicalDate(LocalDateTime(2026, 9, 23, 3, 59)))
        assertEquals(LocalDate.parse("2026-09-23"), logicalDate(LocalDateTime(2026, 9, 23, 4, 0)))
    }

    @Test
    fun dayStartHourZeroMeansMidnight() {
        assertEquals(LocalDate.parse("2026-09-23"), logicalDate(LocalDateTime(2026, 9, 23, 0, 0), dayStartHour = 0))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun daylightSavingDoesNotMoveTheCutoff() {
        // Europe/Madrid jumps from 02:00 to 03:00 on 2027-03-28: the wall clock never reads between
        // them, but the 04:00 cutoff still falls where the local clock says, not by subtracting hours.
        val madrid = TimeZone.of("Europe/Madrid")
        assertEquals(LocalDate.parse("2027-03-27"), logicalDate(Instant.parse("2027-03-28T00:59:59Z"), madrid, dayStartHour = 4))
        assertEquals(LocalDate.parse("2027-03-27"), logicalDate(Instant.parse("2027-03-28T01:00:00Z"), madrid, dayStartHour = 4))
        assertEquals(LocalDate.parse("2027-03-28"), logicalDate(Instant.parse("2027-03-28T02:00:00Z"), madrid, dayStartHour = 4))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun nextDayStartIsDayStartHourTheFollowingDay() {
        val tz = TimeZone.of("Europe/Madrid")
        val next = nextDayStart(LocalDate.parse("2026-09-23"), 4, tz)
        assertEquals(LocalDateTime(2026, 9, 24, 4, 0), next.toLocalDateTime(tz))
    }

    @Test
    fun firstDayOfWeekFallsBackToTheSystemOnlyWhenNull() {
        assertEquals(DayOfWeek.TUESDAY, firstDayOfWeek(Settings(firstDayOfWeek = null), DayOfWeek.TUESDAY))
        assertEquals(DayOfWeek.SUNDAY, firstDayOfWeek(Settings(firstDayOfWeek = 7), DayOfWeek.MONDAY))
    }

    @Test
    fun weekStartsListsTheDaysThatBeginAWeekUnderEachFirstDay() {
        val sep = YearMonth.parse("2026-09")
        assertEquals(setOf(7, 14, 21, 28), weekStarts(sep, DayOfWeek.MONDAY))
        assertEquals(setOf(6, 13, 20, 27), weekStarts(sep, DayOfWeek.SUNDAY))
    }
}
