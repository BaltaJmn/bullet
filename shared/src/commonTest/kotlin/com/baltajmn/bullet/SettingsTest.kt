package com.baltajmn.bullet

import com.baltajmn.bullet.model.DAY_START_DEFAULT
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.logicalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Los ajustes (#32). Viven todos dentro de `Journal.settings`, que es lo que hace que viajen enteros
 * en una exportacion (#44) y que ninguno necesite un almacen aparte.
 */
class SettingsTest {
    @Test
    fun everySettingHasADefaultFromTheInstall() {
        val s = Settings()
        assertEquals(DAY_START_DEFAULT, s.dayStartHour)
        // null no es "sin elegir": es "el del sistema", que ya es una respuesta.
        assertEquals(null, s.firstDayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, firstDayOfWeek(s, system = DayOfWeek.SUNDAY))
        assertTrue(!s.reminderOn && !s.lockOn)
    }

    // El inicio del dia logico cambia que dia es hoy, y nada mas.
    @Test
    fun changingTheStartOfTheDayRegroupsTheDayWithoutTouchingAnyEntry() {
        val lateNight = LocalDateTime(2026, 9, 23, 2, 30)
        assertEquals(LocalDate.parse("2026-09-22"), logicalDate(lateNight, dayStartHour = 4))
        assertEquals(LocalDate.parse("2026-09-23"), logicalDate(lateNight, dayStartHour = 0))

        val entries = listOf(
            Entry(id = "e-1", text = "de madrugada", place = Place.Daily(LocalDate.parse("2026-09-22")), createdAt = 100L, updatedAt = 100L),
            Entry(id = "e-2", text = "del dia", place = Place.Daily(LocalDate.parse("2026-09-23")), createdAt = 200L, updatedAt = 200L),
        )
        val before = Journal(entries = entries)
        val after = before.copy(settings = before.settings.copy(dayStartHour = 0))

        // Ni el lugar, ni el texto, ni el updatedAt de ninguna entrada se mueven al cambiar el ajuste.
        assertEquals(before.entries, after.entries)
    }

    @Test
    fun theSettingsTravelInsideTheJournalItself() {
        val journal = Journal(
            entries = listOf(Entry(id = "e-1", text = "algo", place = Place.Daily(LocalDate.parse("2026-09-22")), createdAt = 100L, updatedAt = 100L)),
            settings = Settings(dayStartHour = 6, firstDayOfWeek = 7, reminderOn = true, reminderHour = 22, reminderMinute = 30, lockOn = true),
        )
        val text = JournalJson.encodeToString(Journal.serializer(), journal)
        val back = JournalJson.decodeFromString(Journal.serializer(), text)

        // Exportar es escribir este mismo Journal (#44), asi que si sobrevive aqui, viaja.
        assertEquals(journal.settings, back.settings)
        assertTrue(text.contains("dayStartHour"))
    }
}
