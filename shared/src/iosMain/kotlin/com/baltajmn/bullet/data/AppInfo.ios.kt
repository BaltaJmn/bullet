package com.baltajmn.bullet.data

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object AppInfo {

    actual val version: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""

    actual fun open(url: String) {
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

actual val Sibling.storeUrl: String? get() = iosUrl

actual val onIos: Boolean = true
