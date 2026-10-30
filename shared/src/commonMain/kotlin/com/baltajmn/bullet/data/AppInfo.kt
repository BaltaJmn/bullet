package com.baltajmn.bullet.data

import com.baltajmn.bullet.i18n.S

/**
 * The published privacy policy. Play, the App Store and the About section all point at the same URL,
 * so it lives here and not in three places. Source of the page: store/privacy/index.html.
 */
const val PRIVACY_URL = "https://bullet.baltajmn.dev/"

/** The few things about the build itself that the About section needs. */
expect object AppInfo {
    /** Version name as the store shows it, "1.0.0". */
    val version: String

    fun open(url: String)
}

data class Sibling(val name: String, val tagline: String, val androidUrl: String?, val iosUrl: String?)

/** The store of the platform this build runs on, or null while that app has no page there. */
expect val Sibling.storeUrl: String?

/** True on iOS. The paywall names the lock screen widget, which only that platform has. */
expect val onIos: Boolean

/**
 * The sister apps (docs/tecnico.md 6.16). A store URL is filled in only once that app is in production
 * on that store the day Bobbin is published; until then it is null and its row is not shown at all. No
 * banner, no notification, and nothing crossed between them.
 */
val SIBLINGS: List<Sibling>
    get() = listOf(
        Sibling(
            "Quilt", S.siblingQuilt,
            androidUrl = "https://play.google.com/store/apps/details?id=com.baltajmn.habit",
            iosUrl = "https://apps.apple.com/app/id6819302953",
        ),
        Sibling(
            "MoodTraker", S.siblingMood,
            androidUrl = "https://play.google.com/store/apps/details?id=com.baltajmn.mood",
            iosUrl = "https://apps.apple.com/app/id6819302341",
        ),
        Sibling("Purl", S.siblingPurl, androidUrl = null, iosUrl = null),
    )
