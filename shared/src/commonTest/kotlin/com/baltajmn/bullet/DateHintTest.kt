package com.baltajmn.bullet

import com.baltajmn.bullet.model.dateHint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

// 30. Fechas en lenguaje natural (docs/tecnico.md 10, 12.4, #66). Hoy es miércoles 23 de septiembre de 2026.
class DateHintTest {
    private val today = LocalDate(2026, 9, 23)
    private fun d(m: Int, day: Int, y: Int = 2026) = LocalDate(y, m, day)

    private val cases = listOf(
        Triple("es", "Llamar a Ana mañana", d(9, 24)),
        Triple("es", "pasado mañana dentista", d(9, 25)),
        Triple("es", "Cena el viernes", d(9, 25)),
        Triple("es", "el lunes por la mañana", d(9, 28)),
        Triple("es", "Recoger el paquete el miércoles", d(9, 30)),
        Triple("es", "Cumple de Leo 14 de marzo", d(3, 14, 2027)),
        Triple("es", "Pagar el 30 de septiembre", d(9, 30)),
        Triple("es", "Pagar el 14", d(10, 14)),
        Triple("es", "reunión 2/10", d(10, 2)),
        Triple("es", "Hoy toca gimnasio", d(9, 23)),
        Triple("en", "Call Ana tomorrow", d(9, 24)),
        Triple("en", "Dinner on Friday", d(9, 25)),
        Triple("en", "Leo's birthday March 14", d(3, 14, 2027)),
        Triple("en", "Pay on the 30th", d(9, 30)),
        Triple("en", "meeting 10/2", d(10, 2)),
        Triple("en", "day after tomorrow", d(9, 25)),
        Triple("pt", "Ligar amanhã", d(9, 24)),
        Triple("pt", "Jantar na sexta", d(9, 25)),
        Triple("pt", "Aniversário 14 de março", d(3, 14, 2027)),
        Triple("pt", "Pagar no dia 14", d(10, 14)),
        Triple("de", "Anruf morgen", d(9, 24)),
        Triple("de", "übermorgen Zahnarzt", d(9, 25)),
        Triple("de", "Essen am Freitag", d(9, 25)),
        Triple("de", "Geburtstag 14. März", d(3, 14, 2027)),
        Triple("de", "Termin 2.10.", d(10, 2)),
        Triple("fr", "Appeler demain", d(9, 24)),
        Triple("fr", "après-demain dentiste", d(9, 25)),
        Triple("fr", "Dîner vendredi", d(9, 25)),
        Triple("fr", "Anniversaire 14 mars", d(3, 14, 2027)),
        Triple("fr", "Payer le 1er", d(10, 1)),
    )

    @Test
    fun eachPhraseGivesItsDate() {
        for ((lang, text, expected) in cases) assertEquals(expected, dateHint(text, lang, today)?.date, "$lang: $text")
    }

    @Test
    fun theRangeIsWhereTheWordsAreInTheOriginalText() {
        val h = dateHint("Llamar a Ana mañana", "es", today)!!
        assertEquals("mañana", "Llamar a Ana mañana".substring(h.range))
    }

    @Test
    fun textWithoutADateGivesNothing() {
        assertNull(dateHint("Comprar pan", "es", today))
        assertNull(dateHint("Leer 30 páginas", "es", today))
        assertNull(dateHint("A las 14:30 el 14:30", "es", today))
        assertNull(dateHint("31 de febrero", "es", today))
        assertNull(dateHint("Buy milk", "en", today))
    }
}
