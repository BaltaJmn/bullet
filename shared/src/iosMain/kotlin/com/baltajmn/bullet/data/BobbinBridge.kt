package com.baltajmn.bullet.data

import kotlinx.coroutines.runBlocking

/**
 * The little that Swift needs to ask Kotlin (docs/tecnico.md 7). Swift owns the window and the
 * notification delegate, so links and taps on the reminder arrive through here.
 */
object BobbinBridge {
    /** For the cover over the task switcher, which Swift paints before Compose could (docs/tecnico.md 6.15). */
    fun isLockOn(): Boolean = BobbinRepository.journal.settings.lockOn

    /** Assigned by iOSApp.swift: WidgetCenter belongs to Swift, and Kotlin only asks. */
    var reloadWidgets: (() -> Unit)? = null

    /** onOpenURL, and the reminder's tap as `bobbin://review`. Anything else is left alone. */
    fun open(url: String) {
        parseLink(url)?.let { Route.pending = it }
    }

    /**
     * The App Intent "Note in Bobbin" (docs/tecnico.md 12.5). The system may suspend the app as soon as
     * the intent answers, so a saved entry is on disk before this returns.
     */
    fun capture(text: String): OutsideCapture {
        val result = BobbinRepository.captureFromOutside(text)
        if (result == OutsideCapture.Saved) runBlocking { BobbinRepository.flush() }
        return result
    }
}
