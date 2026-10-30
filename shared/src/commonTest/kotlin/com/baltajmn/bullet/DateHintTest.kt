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
        Triple("it", "Cena venerdì", d(9, 25)),
        Triple("it", "Compleanno 14 marzo", d(3, 14, 2027)),
        Triple("it", "Pagare il 14", d(10, 14)),
        Triple("nl", "Etentje vrijdag", d(9, 25)),
        Triple("nl", "Verjaardag 14 maart", d(3, 14, 2027)),
        Triple("pl", "Kolacja w piątek", d(9, 25)),
        Triple("pl", "Basen w środę", d(9, 30)),
        Triple("pl", "Urodziny 14 marca", d(3, 14, 2027)),
        Triple("ru", "Ужин в пятницу", d(9, 25)),
        Triple("ru", "День рождения 14 марта", d(3, 14, 2027)),
        Triple("ru", "Заплатить 14-го", d(10, 14)),
        Triple("tr", "Cuma akşam yemeği", d(9, 25)),
        Triple("tr", "Cumartesi pazar", d(9, 26)),
        Triple("tr", "Doğum günü 14 Mart", d(3, 14, 2027)),
        Triple("id", "Makan malam hari Jumat", d(9, 25)),
        Triple("id", "Ulang tahun 14 Maret", d(3, 14, 2027)),
        Triple("ja", "金曜日に夕食", d(9, 25)),
        Triple("ja", "誕生日 3月14日", d(3, 14, 2027)),
        Triple("ja", "会議 10/2", d(10, 2)),
        Triple("ja", "14日に払う", d(10, 14)),
        Triple("ko", "금요일 저녁", d(9, 25)),
        Triple("ko", "생일 3월 14일", d(3, 14, 2027)),
        Triple("ko", "회의 10/2", d(10, 2)),
        Triple("it", "Chiamare Ana domani", d(9, 24)),
        Triple("nl", "Overmorgen tandarts", d(9, 25)),
        Triple("pl", "Basen jutro", d(9, 24)),
        Triple("ru", "Позвонить завтра", d(9, 24)),
        Triple("ru", "послезавтра врач", d(9, 25)),
        Triple("tr", "Yarın bankayı ara", d(9, 24)),
        Triple("id", "Telepon Ana besok", d(9, 24)),
        Triple("ja", "明日電話する", d(9, 24)),
        Triple("ja", "明後日 歯医者", d(9, 25)),
        Triple("ko", "내일 전화하기", d(9, 24)),
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
        // "завтра" inside "завтрак" (breakfast) is no tomorrow: Cyrillic letters are word letters too.
        assertNull(dateHint("Завтрак с Аней", "ru", today))
    }
}
