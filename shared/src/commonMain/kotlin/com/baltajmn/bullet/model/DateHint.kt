package com.baltajmn.bullet.model

import com.baltajmn.bullet.data.fold
import com.baltajmn.bullet.i18n.S
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/** A date found in what is being typed, and where it is in that text (docs/tecnico.md 12.4). */
data class DateHint(val date: LocalDate, val range: IntRange)

/**
 * The date a capture mentions, read on the phone in [lang] (docs/tecnico.md 12.4, docs/textos.md 23):
 * a day and month by name or in figures, a weekday, "pasado mañana", "mañana", "hoy", or a bare day of the
 * month, tried in that order so the most precise one wins ("el lunes por la mañana" is Monday). It is
 * only ever offered: the caller shows it as a pill and nothing is scheduled unless that is touched.
 * Null when there is nothing, or the date does not exist.
 */
fun dateHint(text: String, lang: String, today: LocalDate): DateHint? {
    val (folded, origin) = foldWithOrigin(text)
    // Folded names are letters, hyphens, spaces and an apostrophe: nothing a pattern would read as syntax.
    // Polish and Russian name the month of a date in the genitive ("14 marca", "14 марта").
    val monthList = S.monthNamesInDate(lang).map(::fold)
    val months = monthList.joinToString("|")
    val weekdays = S.weekdayNames(lang).map(::fold)

    // A word ends at anything that is not a Latin or Cyrillic letter or a digit; Japanese and Korean
    // write without spaces, so for them a kanji or a hangul next to the expression is no boundary.
    val letter = "a-z0-9\u00c0-\u024f\u0400-\u04ff"
    fun find(pattern: String): MatchResult? = Regex("(?<![$letter])(?:$pattern)(?![$letter])").find(folded)
    fun hint(m: MatchResult, date: LocalDate?) = date?.let { DateHint(it, origin[m.range.first]..origin[m.range.last]) }

    val dayMonth = when (lang) {
        "en" -> "(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?($months)|($months)\\s+(?:the\\s+)?(\\d{1,2})(?:st|nd|rd|th)?"
        "de" -> "(\\d{1,2})\\.\\s*($months)"
        "fr" -> "(\\d{1,2})(?:er)?\\s+($months)"
        "es", "pt" -> "(\\d{1,2})\\s+de\\s+($months)"
        "ja" -> "($months)\\s*(\\d{1,2})日"
        "ko" -> "($months)\\s*(\\d{1,2})일"
        else -> "(\\d{1,2})\\.?\\s+($months)"
    }
    find(dayMonth)?.let { m ->
        // The day is the group made of digits only; the month, the other one ("9月" has digits too).
        val parts = m.groupValues.drop(1).filter { it.isNotEmpty() }
        val day = parts.first { it.all(Char::isDigit) }
        val month = parts.first { !it.all(Char::isDigit) }
        return hint(m, nextDayMonth(day.toInt(), monthList.indexOf(month) + 1, today))
    }

    val figures = if (lang == "de") "(\\d{1,2})\\.(\\d{1,2})\\." else "(\\d{1,2})/(\\d{1,2})"
    find(figures)?.let { m ->
        val (a, b) = m.groupValues[1].toInt() to m.groupValues[2].toInt()
        // In English, Japanese and Korean 3/14 is the month first; in the others, the day.
        return hint(m, if (lang in setOf("en", "ja", "ko")) nextDayMonth(b, a, today) else nextDayMonth(a, b, today))
    }

    // "segunda-feira" is also just "segunda" in Portuguese; Russian and Polish say a weekday after
    // "в" and "w" in the accusative ("в пятницу", "w środę").
    val weekdayForms = weekdays.map {
        when {
            it.endsWith("-feira") -> it.removeSuffix("-feira") + "(?:-feira)?"
            lang == "ru" && it.endsWith("а") -> it.dropLast(1) + "[ау]"
            lang == "pl" && it.endsWith("a") -> it.dropLast(1) + "[aę]"
            else -> it
        }
    }
    find("(${weekdayForms.joinToString("|")})")?.let { m ->
        val said = m.groupValues[1]
        val iso = weekdayForms.indexOfFirst { Regex(it).matches(said) } + 1
        val ahead = ((iso - today.dayOfWeek.isoDayNumber) + 7) % 7
        return hint(m, today.plus(DatePeriod(days = if (ahead == 0) 7 else ahead)))
    }

    find(fold(S.dateWordDayAfterTomorrow(lang)))?.let { return hint(it, today.plus(DatePeriod(days = 2))) }
    find(fold(S.dateWordTomorrow(lang)))?.let { return hint(it, today.plus(DatePeriod(days = 1))) }
    find(fold(S.dateWordToday(lang)))?.let { return hint(it, today) }

    val bareDay = when (lang) {
        "en" -> "(?:on\\s+)?the\\s+(\\d{1,2})(?:st|nd|rd|th)?"
        "es" -> "el\\s+(\\d{1,2})"
        "pt" -> "(?:no\\s+)?dia\\s+(\\d{1,2})"
        "de" -> "am\\s+(\\d{1,2})\\."
        "fr" -> "le\\s+(\\d{1,2})(?:er)?"
        "it" -> "il\\s+(\\d{1,2})"
        "nl" -> "de\\s+(\\d{1,2})(?:e|ste)?"
        "ru" -> "(\\d{1,2})-?го"
        "id" -> "tanggal\\s+(\\d{1,2})"
        "ja" -> "(\\d{1,2})日"
        "ko" -> "(\\d{1,2})일"
        // Polish and Turkish say a bare day only with a declined number: no hint for it.
        else -> return null
    }
    // Not "el 14:30" nor "el 14,5": a bare number followed by more number is not a day.
    find("$bareDay(?![.:,/]?\\d)")?.let { m -> return hint(m, nextDay(m.groupValues[1].toInt(), today)) }
    return null
}

/** [day] of [month] this year, or next year when it is already gone. Null when that day does not exist. */
private fun nextDayMonth(day: Int, month: Int, today: LocalDate): LocalDate? {
    if (month !in 1..12) return null
    for (year in today.year..today.year + 1) {
        if (day !in 1..monthDays(YearMonth(year, month))) continue
        val d = LocalDate(year, month, day)
        if (d >= today) return d
    }
    return null
}

/** The next [day] of a month from [today] on, skipping the months that do not have it (a 31). */
private fun nextDay(day: Int, today: LocalDate): LocalDate? {
    if (day !in 1..31) return null
    var m = monthOf(today)
    repeat(3) {
        if (day <= monthDays(m)) {
            val d = LocalDate(m.year, m.month, day)
            if (d >= today) return d
        }
        m = m.plus(1, DateTimeUnit.MONTH)
    }
    return null
}

/** [fold] one character at a time, keeping for each folded character the index it came from. */
private fun foldWithOrigin(text: String): Pair<String, IntArray> {
    val out = StringBuilder()
    val origin = ArrayList<Int>()
    for ((i, c) in text.withIndex()) {
        val f = fold(c.toString())
        out.append(f)
        repeat(f.length) { origin += i }
    }
    return out.toString() to origin.toIntArray()
}
