package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.clampCodePoints
import com.baltajmn.bullet.model.codePointCount
import com.baltajmn.bullet.model.normalized
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.toggleDone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
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
}
