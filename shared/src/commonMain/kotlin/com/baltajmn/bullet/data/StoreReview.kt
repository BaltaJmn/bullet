package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
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
 * docs/tecnico.md 6.6: the first Month or Day review that ends with nothing open, after deciding at
 * least one task, and never again in the install's life. A review that opened already empty proves
 * nothing. A Day review that closes everything is the daily proof that someone stays, so it asks too
 * (the two full days since install below put it on day 3 or later). On top of that (rule 11): no
 * paywall open, and not on the day the book card showed on Hoy.
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
    !asked && (scope is ReviewScope.Month || scope is ReviewScope.Day) && decided > 0 && left == 0 &&
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

/** Entries needed, on at least [BOOK_CARD_DAYS] different days, before the early book card shows. */
const val BOOK_CARD_ENTRIES = 5
const val BOOK_CARD_DAYS = 3

/**
 * The same one-time card for someone who writes but never closes a month: from the day after the 3rd
 * day with entries, once the notebook holds at least [BOOK_CARD_ENTRIES]. Never on the rating's day.
 */
fun shouldShowEarlyBookCard(
    isPro: Boolean,
    done: Boolean,
    entryDays: Collection<LocalDate>,
    entryCount: Int,
    reviewAskDay: LocalDate?,
    today: LocalDate,
): Boolean {
    if (isPro || done || entryCount < BOOK_CARD_ENTRIES || reviewAskDay == today) return false
    val third = entryDays.distinct().sorted().getOrNull(BOOK_CARD_DAYS - 1) ?: return false
    return today > third
}

enum class BookCardKind { MONTH, ENTRIES }

private fun storedDay(key: String): LocalDate? = Prefs.string(key)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

/** Reads the stored days and decides which card is due, if any; a bad stored value counts as missing. */
fun bookCardDue(isPro: Boolean, today: LocalDate, journal: Journal): BookCardKind? = runCatching {
    val done = Prefs.bool(PREF_BOOK_CARD_DONE)
    val ask = storedDay(PREF_REVIEW_ASK_DAY)
    if (shouldShowBookCard(isPro, done, storedDay(PREF_FIRST_MONTH_DAY), ask, today)) return@runCatching BookCardKind.MONTH
    val live = journal.entries.filter { !it.gone }
    val days = live.mapNotNull { (it.place as? Place.Daily)?.date }
    if (shouldShowEarlyBookCard(isPro, done, days, live.size, ask, today)) BookCardKind.ENTRIES else null
}.getOrNull()

/** The card is on screen: remembers the day, so the rating is not asked on it. */
fun noteBookCardShown(today: LocalDate) {
    runCatching { Prefs.setString(PREF_BOOK_CARD_DAY, today.toString()) }
}

/** The card was answered (See Pro or Not now): it never comes back. */
fun finishBookCard() {
    runCatching { Prefs.setBool(PREF_BOOK_CARD_DONE, true) }
}
