package com.baltajmn.bullet.data

/**
 * What belongs to this phone and not to the diary (docs/tecnico.md 7): it survives wiping the data and
 * importing a backup, because neither changes what this install has already asked or bought.
 */
expect object Prefs {
    fun bool(key: String): Boolean
    fun setBool(key: String, value: Boolean)
}

/** The last Pro entitlement the store confirmed (docs/tecnico.md 6.16). Wiping the diary keeps it. */
const val PREF_PRO = "pro"

/** Whether the rating was ever asked for (#56): once in the install's life. */
const val PREF_REVIEW_ASKED = "reviewAsked"

/** Android 13+: whether the notification permission was ever asked, to tell "can ask" from "denied" (6.12). */
const val PREF_NOTIFY_ASKED = "notifyAsked"

/** Whether the first start's guide was finished or skipped (docs/pantallas.md 13.1): it shows once per install. */
const val PREF_GUIDE_SEEN = "guideSeen"

/** Whether Hoy's hint about the dot and the text was answered or made moot by using them (docs/pantallas.md 6.3). */
const val PREF_HINT_SEEN = "hintSeen"
