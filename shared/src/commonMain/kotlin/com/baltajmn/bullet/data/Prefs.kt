package com.baltajmn.bullet.data

/**
 * What belongs to this phone and not to the diary (docs/tecnico.md 7): it survives wiping the data and
 * importing a backup, because neither changes what this install has already asked or bought.
 */
expect object Prefs {
    fun bool(key: String): Boolean
    fun setBool(key: String, value: Boolean)
    fun string(key: String): String?

    /** Null removes the key. */
    fun setString(key: String, value: String?)
}

/** The last Pro entitlement the store confirmed (docs/tecnico.md 6.16). Wiping the diary keeps it. */
const val PREF_PRO = "pro"

/** Whether the rating was ever asked for (#56). Kept as the old build wrote it; the ask is now gated by [PREF_REVIEW_LAST_MS]. */
const val PREF_REVIEW_ASKED = "reviewAsked"

/** Android 13+: whether the notification permission was ever asked, to tell "can ask" from "denied" (6.12). */
const val PREF_NOTIFY_ASKED = "notifyAsked"

/** Whether the first start's guide was finished or skipped (docs/pantallas.md 13.1): it shows once per install. */
const val PREF_GUIDE_SEEN = "guideSeen"

/** Whether Hoy's hint about the dot and the text was answered or made moot by using them (docs/pantallas.md 6.3). */
const val PREF_HINT_SEEN = "hintSeen"

/** Epoch millis of this install's first launch, as a string: the rating waits two full days after it. */
const val PREF_FIRST_LAUNCH = "firstLaunch"

/** ISO day the first Month review closed with nothing open: the book card waits for the day after. */
const val PREF_FIRST_MONTH_DAY = "firstMonthDay"

/** ISO day the rating was asked for, so the book card never shares it. */
const val PREF_REVIEW_ASK_DAY = "reviewAskDay"

/** ISO day the first-time Pro card was first on screen; it stays on Hoy for a few days from there. */
const val PREF_BOOK_CARD_DAY = "bookCardDay"

/** Whether the book card was answered. */
const val PREF_BOOK_CARD_DONE = "bookCardDone"

/** Epoch millis (string) of the last system rating request: 120 days must pass before the next. */
const val PREF_REVIEW_LAST_MS = "reviewLastMs"

/** App version the rating was last asked for: the next version may ask again. */
const val PREF_REVIEW_VERSION = "reviewVersion"

/** Epoch millis (string) of the last NEW Pro touch: the rating stays away for 24 hours after it. */
const val PREF_PRO_TOUCH_MS = "proTouchMs"

/** Epoch millis (string) of the last time any Pro surface was shown: the quiet return counts 7 days from it. */
const val PREF_PRO_SEEN_MS = "proSeenMs"

/** Epoch millis (string) and count of the quiet returns shown so far. */
const val PREF_WINBACK_MS = "winbackMs"
const val PREF_WINBACK_COUNT = "winbackCount"

/** Set once the offer state exists; its absence marks the launch of an update or a new install, which stays quiet. */
const val PREF_OFFERS_INIT = "offersInit"

/** ISO day (logical) a Pro surface was last shown: the rating never shares it. */
const val PREF_PRO_SEEN_DAY = "proSeenDay"
