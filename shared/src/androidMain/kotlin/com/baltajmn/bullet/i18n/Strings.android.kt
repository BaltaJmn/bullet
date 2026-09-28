package com.baltajmn.bullet.i18n

import java.util.Calendar
import java.util.Locale
import kotlinx.datetime.DayOfWeek

actual fun systemLanguage(): String = Locale.getDefault().language

/** [Calendar] numbers the days from Sunday (1) to Saturday (7); [DayOfWeek] wants ISO, Monday (1) to Sunday (7). */
actual fun systemFirstDayOfWeek(): DayOfWeek =
    DayOfWeek((Calendar.getInstance(Locale.getDefault()).firstDayOfWeek + 5) % 7 + 1)
