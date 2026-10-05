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
    val months = S.monthNames(lang).map(::fold).joinToString("|")
    val weekdays = S.weekdayNames(lang).map(::fold)

    fun find(pattern: String): MatchResult? = Regex("(?<![a-z0-9])(?:$pattern)(?![a-z0-9])").find(folded)
    fun hint(m: MatchResult, date: LocalDate?) = date?.let { DateHint(it, origin[m.range.first]..origin[m.range.last]) }
    fun monthIndex(name: String) = S.monthNames(lang).map(::fold).indexOf(name) + 1

    val dayMonth = when (lang) {
        "en" -> "(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?($months)|($months)\\s+(?:the\\s+)?(\\d{1,2})(?:st|nd|rd|th)?"
        "de" -> "(\\d{1,2})\\.\\s*($months)"
        "fr" -> "(\\d{1,2})(?:er)?\\s+($months)"
        else -> "(\\d{1,2})\\s+de\\s+($months)"
    }
    find(dayMonth)?.let { m ->
        val g = m.groupValues
        val (day, month) = if (g[1].isNotEmpty()) g[1] to g[2] else g[4] to g[3]
        return hint(m, nextDayMonth(day.toInt(), monthIndex(month), today))
    }

    val figures = if (lang == "de") "(\\d{1,2})\\.(\\d{1,2})\\." else "(\\d{1,2})/(\\d{1,2})"
    find(figures)?.let { m ->
        val (a, b) = m.groupValues[1].toInt() to m.groupValues[2].toInt()
        // In English 3/14 is the month first; in the other four, the day.
        return hint(m, if (lang == "en") nextDayMonth(b, a, today) else nextDayMonth(a, b, today))
    }

    // "segunda-feira" is also just "segunda" in Portuguese.
    val weekdayPattern = weekdays.joinToString("|") { if (it.endsWith("-feira")) it.removeSuffix("-feira") + "(?:-feira)?" else it }
    find("($weekdayPattern)")?.let { m ->
        val said = m.groupValues[1]
        val iso = weekdays.indexOfFirst { it == said || it.removeSuffix("-feira") == said } + 1
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
        else -> "le\\s+(\\d{1,2})(?:er)?"
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
