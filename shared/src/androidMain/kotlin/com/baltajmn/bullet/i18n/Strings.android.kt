package com.baltajmn.bullet.i18n

import java.util.Calendar
import java.util.Locale
import kotlinx.datetime.DayOfWeek

actual fun systemLanguage(): String {
    val l = Locale.getDefault()
    // Traditional Chinese is not translated: "zh-Hant" lets normalizeLanguage send it to English.
    return if (l.language == "zh" && (l.script == "Hant" || (l.script != "Hans" && l.country in setOf("TW", "HK", "MO")))) "zh-Hant" else l.language
}

/** [Calendar] numbers the days from Sunday (1) to Saturday (7); [DayOfWeek] wants ISO, Monday (1) to Sunday (7). */
actual fun systemFirstDayOfWeek(): DayOfWeek =
    DayOfWeek((Calendar.getInstance(Locale.getDefault()).firstDayOfWeek + 5) % 7 + 1)
