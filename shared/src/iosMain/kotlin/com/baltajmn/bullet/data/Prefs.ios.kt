package com.baltajmn.bullet.data

import platform.Foundation.NSUserDefaults

actual object Prefs {
    actual fun bool(key: String): Boolean = NSUserDefaults.standardUserDefaults.boolForKey(key)

    actual fun setBool(key: String, value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = key)
    }

    actual fun string(key: String): String? = NSUserDefaults.standardUserDefaults.stringForKey(key)

    actual fun setString(key: String, value: String?) {
        if (value == null) NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        else NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }
}
