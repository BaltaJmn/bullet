package com.baltajmn.bullet

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.capture
import com.baltajmn.bullet.model.rapidParse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

/** docs/tecnico.md 6.2, test 10 (#20, #21). */
class RapidParseTest {

    @Test
    fun noPrefixIsATask() {
        val parsed = rapidParse("buy ink")!!
        assertEquals(Bullet.TASK, parsed.bullet)
        assertEquals(emptySet(), parsed.signifiers)
        assertEquals("buy ink", parsed.text)
    }

    @Test
    fun dashPrefixIsANoteAndIsStripped() {
        val parsed = rapidParse("- a note")!!
        assertEquals(Bullet.NOTE, parsed.bullet)
        assertEquals("a note", parsed.text)
    }

    @Test
    fun oPrefixIsAnEventAndIsStripped() {
        val parsed = rapidParse("o dinner")!!
        assertEquals(Bullet.EVENT, parsed.bullet)
        assertEquals("dinner", parsed.text)
    }

    @Test
    fun starPrefixAddsPriorityAndIsStripped() {
        val parsed = rapidParse("* call the plumber")!!
        assertEquals(Bullet.TASK, parsed.bullet)
        assertEquals(setOf(Signifier.PRIORITY), parsed.signifiers)
        assertEquals("call the plumber", parsed.text)
    }

    @Test
    fun bangPrefixAddsInspirationAndIsStripped() {
        val parsed = rapidParse("! an idea")!!
        assertEquals(setOf(Signifier.INSPIRATION), parsed.signifiers)
        assertEquals("an idea", parsed.text)
    }

    @Test
    fun questionPrefixAddsExploreAndIsStripped() {
        val parsed = rapidParse("? look into this")!!
        assertEquals(setOf(Signifier.EXPLORE), parsed.signifiers)
        assertEquals("look into this", parsed.text)
    }

    @Test
    fun starThenDashAndDashThenStarGiveTheSamePriorityNote() {
        val a = rapidParse("* - texto")!!
        val b = rapidParse("- * texto")!!
        assertEquals(Bullet.NOTE, a.bullet)
        assertEquals(setOf(Signifier.PRIORITY), a.signifiers)
        assertEquals("texto", a.text)
        assertEquals(a, b)
    }

    @Test
    fun allThreeSignifiersWithAnEventInAnyOrder() {
        val parsed = rapidParse("* ! ? o texto")!!
        assertEquals(Bullet.EVENT, parsed.bullet)
        assertEquals(setOf(Signifier.PRIORITY, Signifier.INSPIRATION, Signifier.EXPLORE), parsed.signifiers)
        assertEquals("texto", parsed.text)
    }

    @Test
    fun aDashNotAtTheStartOfTheLineNeverChangesTheBullet() {
        assertEquals(Bullet.TASK, rapidParse("-5 grados")!!.bullet)
        assertEquals("-5 grados", rapidParse("-5 grados")!!.text)
        assertEquals(Bullet.TASK, rapidParse("hola - x")!!.bullet)
        assertEquals("hola - x", rapidParse("hola - x")!!.text)
    }

    @Test
    fun aSecondDashOnceNoteIsAlreadySetIsLeftAsText() {
        val parsed = rapidParse("- - x")!!
        assertEquals(Bullet.NOTE, parsed.bullet)
        assertEquals("- x", parsed.text)
    }

    @Test
    fun onlyAPrefixWithNothingAfterItSavesNothing() {
        assertNull(rapidParse("- "))
        assertNull(rapidParse("* "))
        assertNull(rapidParse(""))
        assertNull(rapidParse("   "))
    }

    @Test
    fun thePickedSelectorFillsInWithoutAPrefixAndAWrittenPrefixWins() {
        assertEquals(Bullet.EVENT, rapidParse("dinner", picked = Bullet.EVENT)!!.bullet)
        assertEquals(Bullet.NOTE, rapidParse("- a note", picked = Bullet.EVENT)!!.bullet)
    }

    @Test
    fun aPastedLineBreakBecomesASpace() {
        val parsed = rapidParse("first\nsecond")!!
        assertEquals("first second", parsed.text)
    }

    // docs/tecnico.md 6.2 "Crear", used by ui/TodayScreen.kt (#21).
    @Test
    fun captureAddsAnOpenEntryAtTheNextOrderOfItsPlace() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val existing = Entry(id = "e-1", text = "primero", place = place, order = 0, createdAt = 1L, updatedAt = 1L)
        val j = Journal(entries = listOf(existing))

        val result = j.capture("- segundo", place, picked = null, now = 2L, newId = "e-2")!!
        val added = result.entries.single { it.id == "e-2" }
        assertEquals(Bullet.NOTE, added.bullet)
        assertEquals("segundo", added.text)
        assertEquals(1, added.order)
    }

    @Test
    fun captureReturnsNullAndChangesNothingWhenThereIsNothingToSave() {
        val place = Place.Daily(LocalDate.parse("2026-09-22"))
        val j = Journal()
        assertNull(j.capture("- ", place, picked = null, now = 1L, newId = "e-1"))
    }
}
