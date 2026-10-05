package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Place
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 31. Captura desde fuera (docs/tecnico.md 10, 12.5, #67).
class OutsideCaptureTest {
    @Test
    fun withoutProNothingIsWrittenAndWithProItLandsInToday() {
        BobbinRepository.load(MemoryFiles())
        BobbinRepository.isPro = false
        assertEquals(OutsideCapture.NeedsPro, BobbinRepository.captureFromOutside("o Dentista"))
        assertTrue(BobbinRepository.journal.entries.isEmpty())

        BobbinRepository.isPro = true
        assertEquals(OutsideCapture.Saved, BobbinRepository.captureFromOutside("o Dentista"))
        val e = BobbinRepository.journal.entries.single()
        assertEquals(Bullet.EVENT, e.bullet)
        assertEquals("Dentista", e.text)
        assertEquals(Place.Daily(BobbinRepository.today()), e.place)

        assertEquals(OutsideCapture.Empty, BobbinRepository.captureFromOutside("  - "))
        assertEquals(1, BobbinRepository.journal.entries.size)
        BobbinRepository.isPro = false
    }
}
