package com.baltajmn.bullet.data

/**
 * What belongs to this phone and not to the diary (docs/tecnico.md 7): it survives wiping the data and
 * importing a backup, because neither changes what this install has already asked or bought.
 */
expect object Prefs {
    fun bool(key: String): Boolean
    fun setBool(key: String, value: Boolean)
}

/** Android 13+: whether the notification permission was ever asked, to tell "can ask" from "denied" (6.12). */
const val PREF_NOTIFY_ASKED = "notifyAsked"
