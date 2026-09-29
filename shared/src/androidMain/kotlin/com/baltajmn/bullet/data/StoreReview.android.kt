package com.baltajmn.bullet.data

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import java.lang.ref.WeakReference

actual object StoreReview {
    /** Set by MainActivity: the review flow is launched over a live Activity, and holding it would leak it. */
    var host: WeakReference<Activity>? = null

    actual fun request() {
        val activity = host?.get() ?: return
        val manager = ReviewManagerFactory.create(activity)
        // Play decides whether it shows and says nothing either way; a failure is silence too.
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful && !activity.isFinishing) manager.launchReviewFlow(activity, task.result)
        }
    }
}
