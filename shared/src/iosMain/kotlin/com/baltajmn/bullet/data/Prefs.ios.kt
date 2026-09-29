package com.baltajmn.bullet.data

import platform.Foundation.NSUserDefaults

actual object Prefs {
    actual fun bool(key: String): Boolean = NSUserDefaults.standardUserDefaults.boolForKey(key)

    actual fun setBool(key: String, value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = key)
    }
}
