package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

const val URL_SCHEME = "bobbin"

/**
 * Where a link asked to land (docs/tecnico.md 7). [focus] puts the keyboard up on Hoy's field. A sent
 * list opened from outside is `receive`, with the text of its file in [payload] (12.11).
 */
data class Link(val screen: String, val focus: Boolean = false, val payload: String? = null)

/** A `.bobbin` file is a few kilobytes; anything far larger is not one, and is not read whole. */
const val RECEIVE_MAX_BYTES = 4 * 1024 * 1024

/** The text of a file opened with Bobbin, on its way to the receive sheet. */
fun receiveLink(text: String): Link = Link("receive", payload = text)

/**
 * Where something outside the app asked to land: a widget, the notification or a link. It is left here
 * because the app may not be running yet, and App picks it up whenever it does.
 */
object Route {
    var pending by mutableStateOf<Link?>(null)
}

/** `bobbin://today` (with an optional `?focus`), `bobbin://review` and `bobbin://pro`; anything else is ignored rather than guessed at. */
fun parseLink(url: String?): Link? {
    val rest = url?.takeIf { it.startsWith("$URL_SCHEME://") }?.removePrefix("$URL_SCHEME://") ?: return null
    val query = rest.substringAfter('?', "").split('&')
    return when (val screen = rest.substringBefore('?').trimEnd('/')) {
        "today" -> Link(screen, focus = "focus" in query)
        "review", "pro" -> Link(screen)
        else -> null
    }
}
