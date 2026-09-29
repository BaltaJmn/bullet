package com.baltajmn.bullet.ui.theme

import com.baltajmn.bullet.billing.Billing
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.parseLink
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.ui.Paywall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/** Test 18 (docs/tecnico.md 10): what Pro changes is only what is painted, never what is kept. */
class ProTest {
    @Test
    fun proLookFallsBackWithoutPro() {
        val s = Settings(cover = "rose", paper = "grid")
        assertEquals(Cover.Sage, activeCover(s, isPro = false))
        assertEquals(Paper.Dotted, activePaper(s, isPro = false))
        assertEquals(Cover.Rose, activeCover(s, isPro = true))
        assertEquals(Paper.Grid, activePaper(s, isPro = true))
        // The choice stays in settings, so buying again or restoring brings it back as it was.
        assertEquals("rose", s.cover)
        assertEquals("grid", s.paper)
    }

    @Test
    fun unknownIdsAreTheFreeOnes() {
        assertEquals(Cover.Sage, Cover.of("vermilion"))
        assertEquals(Paper.Dotted, Paper.of("hexagons"))
    }

    @Test
    fun everyProLookNeedsPro() {
        Cover.entries.filter { it != Cover.Sage }.forEach { assertFalse(canUse(it, isPro = false), it.name) }
        Paper.entries.filter { it != Paper.Dotted }.forEach { assertFalse(canUse(it, isPro = false), it.name) }
        assertTrue(canUse(Cover.Sage, isPro = false))
        assertTrue(canUse(Paper.Dotted, isPro = false))
    }

    @Test
    fun paywallOnlyOpensWhenAsked() {
        assertFalse(Paywall.open)
        assertEquals("pro", parseLink("bobbin://pro")?.screen)
        var bought = false
        Paywall.show { bought = true }
        assertTrue(Paywall.open)
        Paywall.close(bought = false)
        assertFalse(Paywall.open)
        // Closing without buying leaves the limit that opened it exactly where it was.
        assertFalse(bought)
    }

    /** #47: with no answer from the store (no network, no key), the last known Pro stays. */
    @Test
    fun proSurvivesAStoreThatDoesNotAnswer() = runBlocking {
        BobbinRepository.updatePro(true)
        try {
            Billing.refresh()
            assertTrue(BobbinRepository.isPro)
            assertTrue(Billing.restore().not() && BobbinRepository.isPro, "a failed restore takes nothing away")
        } finally {
            BobbinRepository.isPro = false
        }
    }
}
