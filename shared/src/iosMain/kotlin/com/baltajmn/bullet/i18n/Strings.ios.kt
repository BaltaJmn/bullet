package com.baltajmn.bullet.i18n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** "es-ES", "en-GB": only the language matters here. */
actual fun systemLanguage(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.take(2) ?: "en"
