package com.baltajmn.bullet.data

import android.content.Context

actual object Prefs {
    private val prefs get() = AndroidContext.value.getSharedPreferences("bobbin", Context.MODE_PRIVATE)

    actual fun bool(key: String): Boolean = prefs.getBoolean(key, false)

    actual fun setBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun string(key: String): String? = prefs.getString(key, null)

    actual fun setString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }
}
