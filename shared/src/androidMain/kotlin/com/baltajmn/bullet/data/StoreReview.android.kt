package com.baltajmn.bullet.data

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import java.lang.ref.WeakReference

actual object StoreReview {
    /** Set by MainActivity: the review flow is launched over a live Activity, and holding it would leak it. */
    var host: WeakReference<Activity>? = null

    actual fun request(onLaunched: (Boolean) -> Unit) {
        val activity = host?.get() ?: return onLaunched(false)
        val manager = ReviewManagerFactory.create(activity)
        // Play decides whether it shows and says nothing either way. The ask counts as made only once
        // the flow was really launched; any failure before that spends nothing.
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful || activity.isFinishing) {
                onLaunched(false)
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, task.result).addOnCompleteListener { launched -> onLaunched(launched.isSuccessful) }
        }
    }
}
