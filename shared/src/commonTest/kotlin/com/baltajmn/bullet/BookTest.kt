package com.baltajmn.bullet

import com.baltajmn.bullet.book.BOOK_H
import com.baltajmn.bullet.book.BOOK_UNIT
import com.baltajmn.bullet.book.BOOK_W
import com.baltajmn.bullet.book.Mark
import com.baltajmn.bullet.book.layoutBook
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth

// 48. Libro (docs/tecnico.md 10, 12.6, #68): lo que se puede comprobar sin dibujar.
class BookTest {
    /** Wider than Literata on purpose: if it fits with this, it fits with the real font. */
    private val measure = { text: String, size: Float -> text.length * size * 0.6f }

    private val journal = Journal(
        entries = (0 until 2000).map {
            val month = it % 12 + 1
            Entry(
                id = "e-$it",
                text = "Entrada $it " + "palabra ".repeat(it % 9) + if (it % 50 == 0) "x".repeat(120) else "",
                place = Place.Daily(LocalDate(2026, month, it % 28 + 1)),
                createdAt = month * 1_000_000L + it,
                updatedAt = 0,
            )
        },
        collections = listOf(BulletCollection("c-1", "Lecturas", createdAt = 99_000_000L)),
    )

    @Test
    fun twelveMonthsOfTwoThousandEntriesFitThePageAndTheIndexPointsWhereEachStarts() {
        val book = layoutBook(journal, cover = 0xFFB6D6ABL, paper = "dotted", tz = TimeZone.UTC, measure = measure)
        assertTrue(book.size in 60..400, "${book.size} pages")
        assertTrue(book.pages.first().first() is Mark.Fill)

        for (page in book.pages) for (mark in page) if (mark is Mark.Text) {
            assertTrue(mark.x + measure(mark.text, mark.size) <= BOOK_W - BOOK_UNIT, "out of the margin: ${mark.text}")
            assertTrue(mark.baseline <= BOOK_H, "below the page: ${mark.text}")
        }

        // The Index line of March says the page where "March 2026" is the title.
        val march = S.monthTitle(YearMonth(2026, 3))
        val number = book.pages.flatten().filterIsInstance<Mark.Text>()
            .zipWithNext().first { (a, b) -> a.text == march && b.baseline == a.baseline }.second.text.toInt()
        val titled = book.pages.indexOfFirst { page -> page.any { it is Mark.Text && it.text == march && it.size > 15f } } + 1
        assertEquals(titled, number)
    }

    @Test
    fun anEmptyDiaryIsStillACoverAKeyAndAnIndex() {
        val book = layoutBook(Journal(), cover = 0xFFB6D6ABL, paper = "blank", tz = TimeZone.UTC, measure = measure)
        assertEquals(3, book.size)
        assertTrue(book.paper.isEmpty())
    }
}
