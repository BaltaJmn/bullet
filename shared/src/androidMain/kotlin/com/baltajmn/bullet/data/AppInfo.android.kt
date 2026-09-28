package com.baltajmn.bullet.data

import android.content.Intent
import androidx.core.net.toUri

actual object AppInfo {

    actual val version: String
        get() = runCatching {
            val context = AndroidContext.value
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""

    actual fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { AndroidContext.value.startActivity(intent) }
    }
}

actual val Sibling.storeUrl: String? get() = androidUrl

actual val onIos: Boolean = false
