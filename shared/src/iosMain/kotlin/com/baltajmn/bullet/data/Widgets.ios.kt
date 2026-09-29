package com.baltajmn.bullet.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToURL

const val APP_GROUP = "group.com.baltajmn.bullet"

/**
 * The App Group is the one place the widget extension can read, which is exactly why the diary never
 * goes here: only this file does (docs/tecnico.md 4.2).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun writeWidgetState(json: String) {
    val container = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)
        ?: error("no App Group container")
    val url = container.URLByAppendingPathComponent("widget.json") ?: error("no widget.json path")
    // atomically = true: a widget reading at the same instant sees the old file or the new one.
    NSString.create(string = json).writeToURL(url, true, NSUTF8StringEncoding, null)
}

/** Swift owns WidgetCenter; it hands the call over through BobbinBridge when the app starts. */
actual fun refreshWidgets() {
    BobbinBridge.reloadWidgets?.invoke()
}
