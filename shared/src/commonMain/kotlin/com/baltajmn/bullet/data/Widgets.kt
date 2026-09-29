package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Journal
import kotlinx.datetime.LocalDate

/**
 * The only channel to the widgets (docs/tecnico.md 6.13). They read widget.json and nothing else, so
 * wherever the diary lives the widgets cannot reach it, on either platform.
 */
expect fun writeWidgetState(json: String)

/** Tells the system the state changed, and books the repaint at the next change of day. */
expect fun refreshWidgets()

/** A failure here can never take a save down with it: the widget is put right by the next one. */
fun syncWidgets(j: Journal, isPro: Boolean, today: LocalDate) {
    runCatching {
        writeWidgetState(WidgetJson.encodeToString(WidgetState.serializer(), widgetState(j, isPro, today)))
        refreshWidgets()
    }
}
