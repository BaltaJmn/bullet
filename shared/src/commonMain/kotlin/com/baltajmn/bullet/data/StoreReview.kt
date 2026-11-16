package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.ReviewScope
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * The system's own rating prompt (docs/tecnico.md 7). The system decides whether it shows: asking is all
 * the app does. Returns true when the request was made, false when there was nothing to ask on (no
 * foreground Activity or scene), so the caller only spends the ask once it really happened.
 */
expect object StoreReview {
    fun request(): Boolean
}

/** Whole days an install must have lived before the system prompt may be asked for (rule 11). */
const val REVIEW_MIN_DAYS = 2

private const val DAY_MS = 24L * 60 * 60 * 1000

/** Remembers when this install first opened. Existing installs count from the first launch of the build that has it. */
fun noteFirstLaunch(nowMs: Long = Clock.System.now().toEpochMilliseconds()) {
    runCatching {
        if (Prefs.string(PREF_FIRST_LAUNCH)?.toLongOrNull() == null) Prefs.setString(PREF_FIRST_LAUNCH, nowMs.toString())
    }
}

/** True once [REVIEW_MIN_DAYS] full days have passed since [installedAtMs]; an unknown date is not enough. */
fun installedLongEnough(installedAtMs: Long?, nowMs: Long): Boolean =
    installedAtMs != null && nowMs - installedAtMs >= REVIEW_MIN_DAYS * DAY_MS

/**
 * docs/tecnico.md 6.6: the first Month review that ends with nothing open, after deciding at least one
 * task, and never again in the install's life. A review that opened already empty proves nothing, and
 * a Day review is not the moment someone shows they stay. On top of that (rule 11): two full days since
 * install, no paywall open, and not on the day the book card showed on Hoy.
 */
fun shouldAskReview(
    asked: Boolean,
    scope: ReviewScope,
    decided: Int,
    left: Int,
    installedAtMs: Long?,
    nowMs: Long,
    paywallOpen: Boolean,
    bookCardShownToday: Boolean,
): Boolean =
    !asked && scope is ReviewScope.Month && decided > 0 && left == 0 &&
        installedLongEnough(installedAtMs, nowMs) && !paywallOpen && !bookCardShownToday

/**
 * Called when a review reaches its end. Records the day the first month was closed (what the book card
 * waits for) and asks for the rating when everything allows it. The ask is marked spent only after the
 * request was really made.
 */
fun afterReview(scope: ReviewScope, decided: Int, left: Int, today: LocalDate, paywallOpen: Boolean) {
    if (scope is ReviewScope.Month && decided > 0 && left == 0 && Prefs.string(PREF_FIRST_MONTH_DAY) == null) {
        Prefs.setString(PREF_FIRST_MONTH_DAY, today.toString())
    }
    val now = Clock.System.now().toEpochMilliseconds()
    val ask = shouldAskReview(
        asked = Prefs.bool(PREF_REVIEW_ASKED),
        scope = scope,
        decided = decided,
        left = left,
        installedAtMs = Prefs.string(PREF_FIRST_LAUNCH)?.toLongOrNull(),
        nowMs = now,
        paywallOpen = paywallOpen,
        bookCardShownToday = Prefs.string(PREF_BOOK_CARD_DAY) == today.toString(),
    )
    if (!ask) return
    if (StoreReview.request()) {
        Prefs.setBool(PREF_REVIEW_ASKED, true)
        Prefs.setString(PREF_REVIEW_ASK_DAY, today.toString())
    }
}

/**
 * The one-time card on Hoy after the first month review closed: for people without Pro, from the day
 * after that review (never the same day), once, and never on the day the rating was asked for.
 */
fun shouldShowBookCard(
    isPro: Boolean,
    done: Boolean,
    firstMonthDay: LocalDate?,
    reviewAskDay: LocalDate?,
    today: LocalDate,
): Boolean =
    !isPro && !done && firstMonthDay != null && today >= firstMonthDay.plus(DatePeriod(days = 1)) && reviewAskDay != today

/** Reads the stored days and decides; a bad stored value counts as missing. */
fun bookCardDue(isPro: Boolean, today: LocalDate): Boolean = runCatching {
    shouldShowBookCard(
        isPro = isPro,
        done = Prefs.bool(PREF_BOOK_CARD_DONE),
        firstMonthDay = Prefs.string(PREF_FIRST_MONTH_DAY)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        reviewAskDay = Prefs.string(PREF_REVIEW_ASK_DAY)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        today = today,
    )
}.getOrDefault(false)

/** The card is on screen: remembers the day, so the rating is not asked on it. */
fun noteBookCardShown(today: LocalDate) {
    runCatching { Prefs.setString(PREF_BOOK_CARD_DAY, today.toString()) }
}

/** The card was answered (See Pro or Not now): it never comes back. */
fun finishBookCard() {
    runCatching { Prefs.setBool(PREF_BOOK_CARD_DONE, true) }
}
