package com.baltajmn.bullet.ui

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Test 54 (#80): each shortcut with Cmd on iOS and Ctrl on Android, and nothing with the other one. */
class KeysTest {

    private val table = mapOf(
        Key.N to KeyAction.WRITE,
        Key.F to KeyAction.SEARCH,
        Key.One to KeyAction.TODAY,
        Key.Two to KeyAction.MONTH,
        Key.Three to KeyAction.FUTURE,
        Key.Four to KeyAction.INDEX,
        Key.LeftBracket to KeyAction.PREVIOUS,
        Key.RightBracket to KeyAction.NEXT,
    )

    @Test
    fun cmdOnIosAndCtrlOnAndroid() {
        table.forEach { (key, action) ->
            assertEquals(action, keyAction(key, meta = true, ctrl = false, apple = true))
            assertEquals(action, keyAction(key, meta = false, ctrl = true, apple = false))
        }
    }

    @Test
    fun theOtherModifierOrNoneDoesNothing() {
        table.keys.forEach { key ->
            assertNull(keyAction(key, meta = false, ctrl = true, apple = true))
            assertNull(keyAction(key, meta = true, ctrl = false, apple = false))
            assertNull(keyAction(key, meta = false, ctrl = false, apple = true))
            assertNull(keyAction(key, meta = true, ctrl = true, apple = false))
        }
        assertNull(keyAction(Key.A, meta = true, ctrl = false, apple = true))
    }

    @Test
    fun escapeAloneIsBack() {
        assertEquals(KeyAction.BACK, keyAction(Key.Escape, meta = false, ctrl = false, apple = true))
        assertEquals(KeyAction.BACK, keyAction(Key.Escape, meta = false, ctrl = false, apple = false))
        assertNull(keyAction(Key.Escape, meta = true, ctrl = false, apple = true))
    }

    @Test
    fun theKeyPageWritesEachPlatformsShortcut() {
        assertEquals("⌘N", shortcutLabel("N", apple = true))
        assertEquals("Ctrl+N", shortcutLabel("N", apple = false))
    }
}
