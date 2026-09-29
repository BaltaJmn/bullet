package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.ReviewScope

/** The system's own rating prompt (docs/tecnico.md 7). The system decides whether it shows: asking is all the app does. */
expect object StoreReview {
    fun request()
}

/**
 * docs/tecnico.md 6.6: the first Month review that ends with nothing open, after deciding at least one
 * task, and never again in the install's life. A review that opened already empty proves nothing, and
 * a Day review is not the moment someone shows they stay.
 */
fun shouldAskReview(asked: Boolean, scope: ReviewScope, decided: Int, left: Int): Boolean =
    !asked && scope is ReviewScope.Month && decided > 0 && left == 0

/** Marks the flag before asking, so a crash inside the system's prompt can never make it ask twice. */
fun askReviewAfter(scope: ReviewScope, decided: Int, left: Int) {
    if (!shouldAskReview(Prefs.bool(PREF_REVIEW_ASKED), scope, decided, left)) return
    Prefs.setBool(PREF_REVIEW_ASKED, true)
    StoreReview.request()
}
