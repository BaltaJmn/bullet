package com.baltajmn.bullet.i18n

import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** "es-ES", "en-GB": only the language matters here. */
actual fun systemLanguage(): String {
    val l = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: return "en"
    // Traditional Chinese is not translated: "zh-Hant" lets normalizeLanguage send it to English.
    return if (l.startsWith("zh") && ("Hant" in l || ("Hans" !in l && listOf("-TW", "-HK", "-MO").any { it in l }))) "zh-Hant" else l.take(2)
}

/** `firstWeekday` numbers the days from Sunday (1) to Saturday (7); [DayOfWeek] wants ISO, Monday (1) to Sunday (7). */
actual fun systemFirstDayOfWeek(): DayOfWeek =
    DayOfWeek((NSCalendar.currentCalendar.firstWeekday.toInt() + 5) % 7 + 1)
