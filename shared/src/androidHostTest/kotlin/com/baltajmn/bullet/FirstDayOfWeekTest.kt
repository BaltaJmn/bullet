package com.baltajmn.bullet

import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek

// 4. Mes (#24): with no setting, the week starts where the device locale says.
class FirstDayOfWeekTest {
    @Test
    fun theSystemWeekFollowsTheLocale() {
        val saved = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals(DayOfWeek.SUNDAY, systemFirstDayOfWeek())
            Locale.setDefault(Locale.forLanguageTag("es-ES"))
            assertEquals(DayOfWeek.MONDAY, systemFirstDayOfWeek())
        } finally {
            Locale.setDefault(saved)
        }
    }
}
