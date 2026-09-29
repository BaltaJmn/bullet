package com.baltajmn.bullet.data

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
}
