package com.baltajmn.bullet.data

/**
 * The little that Swift needs to ask Kotlin (docs/tecnico.md 7). Swift owns the window and the
 * notification delegate, so links and taps on the reminder arrive through here.
 */
object BobbinBridge {
    /** onOpenURL, and the reminder's tap as `bobbin://review`. Anything else is left alone. */
    fun open(url: String) {
        parseLink(url)?.let { Route.pending = it }
    }
}
