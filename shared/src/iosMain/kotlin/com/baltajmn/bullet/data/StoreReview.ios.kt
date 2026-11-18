package com.baltajmn.bullet.data

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

actual object StoreReview {
    actual fun request(onLaunched: (Boolean) -> Unit) {
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: return onLaunched(false)
        SKStoreReviewController.requestReviewInScene(scene)
        onLaunched(true)
    }
}
