package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * The system's own rating prompt (docs/tecnico.md 7). The system decides whether it shows: asking is all
 * the app does. Returns true when the request was made, false when there was nothing to ask on (no
 * foreground Activity or scene), so the caller only spends the ask once it really happened.
 * [onLaunched] gets true only once the system flow was really launched; a failure along the way gets false.
 */
expect object StoreReview {
    fun request(onLaunched: (Boolean) -> Unit)
}

/** Whole days an install must have lived before the system prompt may be asked for (rule 11). */
const val REVIEW_MIN_DAYS = 2

/** The system prompt is asked at most once per app version and never twice within this many days. */
const val REVIEW_GAP_DAYS = 120

/** Hours a new Pro touch keeps the rating away. */
const val PRO_TOUCH_QUIET_HOURS = 24

/** Days after the last Pro surface before the one quiet return may show, and between two of them. */
const val WINBACK_AFTER_DAYS = 7

/** The quiet return never shows more than this many times in the install's life. */
const val WINBACK_MAX = 3

/** Days (counting the first) the first-time card stays on Hoy before it counts as ignored. */
const val BOOK_CARD_VISIBLE_DAYS = 3

/** A wish to ask for the rating that waited this long for a calm screen is dropped. */
const val RATING_WAIT_MS = 10L * 60 * 1000

/** A calm screen must hold this long before the request, so it never lands mid-typing. */
const val CALM_BEFORE_ASK_MS = 2500L

/** An error this recent keeps the rating away. */
private const val ERROR_QUIET_MS = 30L * 60 * 1000

/** Time away that starts a new session for the one-surface-per-session rule. */
const val NEW_SESSION_AFTER_MINUTES = 30

private const val DAY_MS = 24L * 60 * 60 * 1000

private fun nowMs() = Clock.System.now().toEpochMilliseconds()

private fun storedMs(key: String): Long? = runCatching { Prefs.string(key)?.toLongOrNull() }.getOrNull()

private fun storedDay(key: String): LocalDate? = runCatching { Prefs.string(key)?.let { LocalDate.parse(it) } }.getOrNull()

/** Remembers when this install first opened. Existing installs count from the first launch of the build that has it. */
fun noteFirstLaunch(nowMs: Long = nowMs()) {
    runCatching {
        if (Prefs.string(PREF_FIRST_LAUNCH)?.toLongOrNull() == null) Prefs.setString(PREF_FIRST_LAUNCH, nowMs.toString())
    }
}

/** True once [REVIEW_MIN_DAYS] full days have passed since [installedAtMs]; an unknown date is not enough. */
fun installedLongEnough(installedAtMs: Long?, nowMs: Long): Boolean =
    installedAtMs != null && nowMs - installedAtMs >= REVIEW_MIN_DAYS * DAY_MS

/** A review that ends with nothing left open, after deciding at least one task: the success the offers wait for. */
fun isGoodMoment(scope: ReviewScope, decided: Int, left: Int): Boolean =
    (scope is ReviewScope.Month || scope is ReviewScope.Day || scope is ReviewScope.Earlier) && decided > 0 && left == 0

/** Distinct days with entries, today included, that make finishing a task count as getting something done. */
const val GOOD_DAYS = 3

/**
 * Finishing a task is a good moment once the notebook has seen at least [GOOD_DAYS] distinct days with
 * entries up to [today]: someone who comes back and gets something done, with or without reviews.
 */
fun isWorkDay(entryDays: Collection<LocalDate>, today: LocalDate): Boolean =
    entryDays.filter { it <= today }.distinct().size >= GOOD_DAYS

/**
 * Whether the system rating prompt may be asked for now. [goodMoment] is a review that closed everything
 * (see [isGoodMoment]) or a task finished on a day that is at least the 3rd with entries, from day 3 of
 * the install. Once per app version and 120 days apart ([lastAskMs], [lastAskVersion]; a missing value
 * counts as never). Nothing about Pro gates it, so someone who never buys is reached, and so is someone
 * who did. Kept away: within 24 hours of a new Pro touch ([lastProTouchMs]) or on a day a Pro surface was
 * shown ([proShownToday]), over any sheet or dialog, right after an error, on the launch that first ran
 * this logic ([quiet]) and once it was already tried this session ([tried]).
 */
fun shouldAskReview(
    goodMoment: Boolean,
    installedAtMs: Long?,
    nowMs: Long,
    lastAskMs: Long?,
    lastAskVersion: String?,
    version: String,
    lastProTouchMs: Long?,
    proShownToday: Boolean,
    overlayOpen: Boolean,
    recentError: Boolean,
    quiet: Boolean,
    tried: Boolean,
): Boolean =
    goodMoment && installedLongEnough(installedAtMs, nowMs) &&
        !overlayOpen && !recentError && !quiet && !tried && !proShownToday &&
        (lastAskMs == null || (version != lastAskVersion && nowMs - lastAskMs >= REVIEW_GAP_DAYS * DAY_MS)) &&
        (lastProTouchMs == null || nowMs - lastProTouchMs >= PRO_TOUCH_QUIET_HOURS * 60 * 60 * 1000L)

/**
 * Called when a review reaches its end. Records the day the first month was closed (what the book card
 * waits for), opens the door for the offers and asks for the rating when everything allows it.
 */
fun afterReview(scope: ReviewScope, decided: Int, left: Int, today: LocalDate, overlayOpen: Boolean) {
    if (scope is ReviewScope.Month && decided > 0 && left == 0 && Prefs.string(PREF_FIRST_MONTH_DAY) == null) {
        Prefs.setString(PREF_FIRST_MONTH_DAY, today.toString())
    }
    val good = isGoodMoment(scope, decided, left)
    if (good) Offers.goodMoment = true
    askReviewIfDue(good, today, overlayOpen)
}

/**
 * Asks the system for the rating if [goodMoment] and every guard allow it. The ask is marked spent only
 * once the system flow was really launched; a failure spends nothing and is not retried this session.
 */
fun askReviewIfDue(goodMoment: Boolean, today: LocalDate, overlayOpen: Boolean) {
    Offers.ratingWanted = false
    val now = nowMs()
    val version = runCatching { AppInfo.version }.getOrDefault("")
    val ask = shouldAskReview(
        goodMoment = goodMoment,
        installedAtMs = storedMs(PREF_FIRST_LAUNCH),
        nowMs = now,
        lastAskMs = storedMs(PREF_REVIEW_LAST_MS),
        lastAskVersion = runCatching { Prefs.string(PREF_REVIEW_VERSION) }.getOrNull(),
        version = version,
        lastProTouchMs = storedMs(PREF_PRO_TOUCH_MS),
        proShownToday = Offers.proShownThisSession || storedDay(PREF_PRO_SEEN_DAY) == today,
        overlayOpen = overlayOpen || Sync.conflict != null,
        recentError = BobbinRepository.saveFailed || BobbinRepository.corrupt || Offers.errorNow(now),
        quiet = Offers.quiet,
        tried = Offers.ratingTried,
    )
    if (!ask) return
    Offers.ratingTried = true
    StoreReview.request { launched ->
        if (!launched) return@request
        runCatching {
            Prefs.setBool(PREF_REVIEW_ASKED, true)
            Prefs.setString(PREF_REVIEW_LAST_MS, now.toString())
            Prefs.setString(PREF_REVIEW_VERSION, version)
            Prefs.setString(PREF_REVIEW_ASK_DAY, today.toString())
        }
        Offers.ratedThisSession = true
    }
}

/** Whether the first-time card is still inside its window: first shown [shownDay], gone after [BOOK_CARD_VISIBLE_DAYS]. */
fun bookCardWindowOpen(shownDay: LocalDate?, today: LocalDate): Boolean =
    shownDay == null || today < shownDay.plus(DatePeriod(days = BOOK_CARD_VISIBLE_DAYS))

/**
 * The first-time card on Hoy after the first month review closed: for people without Pro, from the day
 * after that review (never the same day), answered once, never on the day the rating was asked for, and
 * gone by itself after [BOOK_CARD_VISIBLE_DAYS] days if ignored.
 */
fun shouldShowBookCard(
    isPro: Boolean,
    done: Boolean,
    firstMonthDay: LocalDate?,
    reviewAskDay: LocalDate?,
    shownDay: LocalDate?,
    today: LocalDate,
): Boolean =
    !isPro && !done && firstMonthDay != null && today >= firstMonthDay.plus(DatePeriod(days = 1)) &&
        reviewAskDay != today && bookCardWindowOpen(shownDay, today)

/** Entries needed, on at least [BOOK_CARD_DAYS] different days, before the early book card shows. */
const val BOOK_CARD_ENTRIES = 5
const val BOOK_CARD_DAYS = 3

/**
 * The same card for someone who writes but never closes a month: from the day after the 3rd day with
 * entries, once the notebook holds at least [BOOK_CARD_ENTRIES] and, the first time, after a good moment
 * of the session ([goodMoment]). Never on the rating's day.
 */
fun shouldShowEarlyBookCard(
    isPro: Boolean,
    done: Boolean,
    entryDays: Collection<LocalDate>,
    entryCount: Int,
    reviewAskDay: LocalDate?,
    shownDay: LocalDate?,
    today: LocalDate,
    goodMoment: Boolean = true,
): Boolean {
    if (isPro || done || entryCount < BOOK_CARD_ENTRIES || reviewAskDay == today || !bookCardWindowOpen(shownDay, today)) return false
    // Its first appearance waits for a good moment of the session; once on Hoy it stays its days.
    if (shownDay == null && !goodMoment) return false
    val third = entryDays.distinct().sorted().getOrNull(BOOK_CARD_DAYS - 1) ?: return false
    return today > third
}

/**
 * The one quiet return: [WINBACK_AFTER_DAYS] days after a Pro surface was last shown ([lastSeenMs]; never
 * shown means there is nothing to return to), right after a good moment this session, at most once per
 * [WINBACK_AFTER_DAYS] days and [WINBACK_MAX] times in all, never on the rating's day.
 */
fun shouldShowWinBack(
    isPro: Boolean,
    goodMoment: Boolean,
    ratedThisSession: Boolean,
    lastSeenMs: Long?,
    lastWinBackMs: Long?,
    winBackCount: Int,
    reviewAskDay: LocalDate?,
    today: LocalDate,
    nowMs: Long,
): Boolean =
    !isPro && goodMoment && !ratedThisSession && reviewAskDay != today && winBackCount < WINBACK_MAX &&
        lastSeenMs != null && nowMs - lastSeenMs >= WINBACK_AFTER_DAYS * DAY_MS &&
        (lastWinBackMs == null || nowMs - lastWinBackMs >= WINBACK_AFTER_DAYS * DAY_MS)

/** MONTH and ENTRIES are the first-time card; WINBACK is the same card coming back once, quietly. */
enum class BookCardKind { MONTH, ENTRIES, WINBACK }

/**
 * Reads the stored state and decides which card is due, if any; a bad stored value counts as missing.
 * A card this session already put on screen stays until it is answered. Otherwise nothing shows on the
 * launch that first ran this logic, nor a second Pro surface in the same session.
 */
fun bookCardDue(isPro: Boolean, today: LocalDate, journal: Journal, nowMs: Long = nowMs()): BookCardKind? = runCatching {
    if (isPro) return@runCatching null
    val ask = storedDay(PREF_REVIEW_ASK_DAY)
    // A card on screen stays until answered, except on a day the rating was asked for.
    Offers.sessionCard?.let { return@runCatching it.takeIf { ask != today } }
    if (Offers.quiet || Offers.proShownThisSession) return@runCatching null
    val done = Prefs.bool(PREF_BOOK_CARD_DONE)
    val shown = storedDay(PREF_BOOK_CARD_DAY)
    if (shouldShowBookCard(isPro, done, storedDay(PREF_FIRST_MONTH_DAY), ask, shown, today)) return@runCatching BookCardKind.MONTH
    val live = journal.entries.filter { !it.gone }
    val days = live.mapNotNull { (it.place as? Place.Daily)?.date }
    if (shouldShowEarlyBookCard(isPro, done, days, live.size, ask, shown, today, Offers.goodMoment)) return@runCatching BookCardKind.ENTRIES
    val back = shouldShowWinBack(
        isPro, Offers.goodMoment, Offers.ratedThisSession, storedMs(PREF_PRO_SEEN_MS), storedMs(PREF_WINBACK_MS),
        storedMs(PREF_WINBACK_COUNT)?.toInt() ?: 0, ask, today, nowMs,
    )
    if (back) BookCardKind.WINBACK else null
}.getOrNull()

/**
 * What this session has shown and what the phone remembers about Pro surfaces. A "Pro touch" is a Pro
 * surface appearing anew: the card the first time, the return, the paywall opened by a locked feature,
 * Settings or a link. The card sitting on Hoy on later days is not one; it only refreshes [PREF_PRO_SEEN_MS].
 */
object Offers {
    /** True on the launch that first ran this logic (a new install or an update): no offer, no rating. Clears at the next foreground. */
    var quiet = false
        private set
    private var started = false

    /** Only one Pro surface comes by itself per session. */
    var proShownThisSession by mutableStateOf(false)
        private set
    var goodMoment by mutableStateOf(false)
    var ratedThisSession = false

    /** The system flow was already tried this session, whatever came of it: no second attempt. */
    var ratingTried = false

    /** A task was just finished on a good day: the rating waits for a calm screen, then asks. */
    var ratingWanted by mutableStateOf(false)
    var ratingWantedAtMs = 0L
    var sessionCard by mutableStateOf<BookCardKind?>(null)
        private set
    private var errorAtMs: Long? = null

    fun begin(nowMs: Long = nowMs(), prefs: OfferPrefs = PhonePrefs) {
        if (started) return
        started = true
        runCatching {
            if (prefs.bool(PREF_OFFERS_INIT)) return
            quiet = true
            migrateOfferKeys(prefs, nowMs)
        }.onFailure { quiet = true }
    }

    /** Coming back to the foreground ends the quiet launch; a long time away is also a new session. */
    fun resumed(away: Boolean) {
        quiet = false
        if (away) newSession()
    }

    /** Coming back after a long time away is a new session. */
    fun newSession() {
        proShownThisSession = false
        goodMoment = false
        ratedThisSession = false
        ratingTried = false
        ratingWanted = false
        sessionCard = null
    }

    /** A task was finished: on the 3rd day with entries or later it is a good moment, and may ask for the rating. */
    fun taskCompleted(entryDays: Collection<LocalDate>, today: LocalDate) {
        if (!isWorkDay(entryDays, today)) return
        goodMoment = true
        if (!quiet && !ratingTried) {
            ratingWanted = true
            ratingWantedAtMs = nowMs()
        }
    }

    private fun markSeen(nowMs: Long, today: LocalDate?) {
        Prefs.setString(PREF_PRO_SEEN_MS, nowMs.toString())
        if (today != null) Prefs.setString(PREF_PRO_SEEN_DAY, today.toString())
    }

    /** The paywall just opened, by any door. */
    fun proTouched(nowMs: Long = nowMs()) {
        proShownThisSession = true
        runCatching {
            Prefs.setString(PREF_PRO_TOUCH_MS, nowMs.toString())
            markSeen(nowMs, BobbinRepository.today())
        }
    }

    /** The card is on screen: a new touch only the first time it is ever shown, or when the return shows. */
    fun cardShown(kind: BookCardKind, today: LocalDate, nowMs: Long = nowMs()) {
        if (sessionCard == kind) return
        sessionCard = kind
        proShownThisSession = true
        runCatching {
            val first = kind == BookCardKind.WINBACK || Prefs.string(PREF_BOOK_CARD_DAY) == null
            if (kind != BookCardKind.WINBACK && Prefs.string(PREF_BOOK_CARD_DAY) == null) Prefs.setString(PREF_BOOK_CARD_DAY, today.toString())
            if (kind == BookCardKind.WINBACK) {
                Prefs.setString(PREF_WINBACK_MS, nowMs.toString())
                Prefs.setString(PREF_WINBACK_COUNT, ((storedMs(PREF_WINBACK_COUNT) ?: 0L) + 1).toString())
            }
            if (first) Prefs.setString(PREF_PRO_TOUCH_MS, nowMs.toString())
            markSeen(nowMs, today)
        }
    }

    /** The card was answered, See Pro or Not now. The first-time card never comes back; the return is bounded by its own count. */
    fun cardAnswered() {
        if (sessionCard != BookCardKind.WINBACK) runCatching { Prefs.setBool(PREF_BOOK_CARD_DONE, true) }
        sessionCard = null
    }

    /** A purchase, a restore or the store failed: not the moment to ask for anything. */
    fun noteError(nowMs: Long = nowMs()) {
        errorAtMs = nowMs
    }

    fun errorNow(nowMs: Long): Boolean = errorAtMs?.let { nowMs - it < ERROR_QUIET_MS } == true

    /** Test hook: a fresh process. */
    internal fun resetForTest() {
        started = false
        quiet = false
        errorAtMs = null
        newSession()
    }
}

/** The few preferences the offer state reads, so the migration can be tested without a phone. */
interface OfferPrefs {
    fun bool(key: String): Boolean
    fun setBool(key: String, value: Boolean)
    fun string(key: String): String?
    fun setString(key: String, value: String?)
}

object PhonePrefs : OfferPrefs {
    override fun bool(key: String) = Prefs.bool(key)
    override fun setBool(key: String, value: Boolean) = Prefs.setBool(key, value)
    override fun string(key: String) = Prefs.string(key)
    override fun setString(key: String, value: String?) = Prefs.setString(key, value)
}

/**
 * Keys the previous build did not have read as missing; give them a start that spams nobody and locks
 * nobody out: a rating asked before counts from [nowMs], a card seen before too. Ends by setting the flag.
 */
fun migrateOfferKeys(p: OfferPrefs, nowMs: Long) {
    if (p.bool(PREF_REVIEW_ASKED) && p.string(PREF_REVIEW_LAST_MS) == null) p.setString(PREF_REVIEW_LAST_MS, nowMs.toString())
    if (p.string(PREF_PRO_SEEN_MS) == null && (p.string(PREF_BOOK_CARD_DAY) != null || p.bool(PREF_BOOK_CARD_DONE))) {
        p.setString(PREF_PRO_SEEN_MS, nowMs.toString())
    }
    p.setBool(PREF_OFFERS_INIT, true)
}
