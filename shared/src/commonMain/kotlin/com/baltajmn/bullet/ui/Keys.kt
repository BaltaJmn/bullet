package com.baltajmn.bullet.ui

import androidx.compose.ui.input.key.Key

/** What a hardware keyboard can ask for (docs/tecnico.md 12.15). Everything here is also a touch away. */
enum class KeyAction { WRITE, SEARCH, TODAY, MONTH, FUTURE, INDEX, PREVIOUS, NEXT, BACK }

/**
 * The shortcut [key] makes with its modifiers, or null. The modifier is Cmd on iOS ([apple]) and Ctrl on
 * Android, never both and never the other one, so a shortcut never steals a key the system already uses.
 * Esc is back, alone.
 */
fun keyAction(key: Key, meta: Boolean, ctrl: Boolean, apple: Boolean): KeyAction? {
    if (key == Key.Escape) return if (!meta && !ctrl) KeyAction.BACK else null
    val held = if (apple) meta && !ctrl else ctrl && !meta
    if (!held) return null
    return when (key) {
        Key.N -> KeyAction.WRITE
        Key.F -> KeyAction.SEARCH
        Key.One -> KeyAction.TODAY
        Key.Two -> KeyAction.MONTH
        Key.Three -> KeyAction.FUTURE
        Key.Four -> KeyAction.INDEX
        Key.LeftBracket -> KeyAction.PREVIOUS
        Key.RightBracket -> KeyAction.NEXT
        else -> null
    }
}

/** How the Key page writes a shortcut: `⌘N` on iOS, `Ctrl+N` on Android. */
fun shortcutLabel(key: String, apple: Boolean): String = if (apple) "⌘$key" else "Ctrl+$key"
