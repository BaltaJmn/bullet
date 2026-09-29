package com.baltajmn.bullet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.PREF_HINT_SEEN
import com.baltajmn.bullet.data.Prefs
import com.baltajmn.bullet.data.Reminders
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.dayShare
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.ofDay
import com.baltajmn.bullet.model.openTasksBefore
import com.baltajmn.bullet.model.openTasksOfMonth
import com.baltajmn.bullet.model.unclosedMonth
import com.baltajmn.bullet.ui.theme.Type
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Hoy's one-time hint (docs/pantallas.md 6.3). It goes with "Entendido", or as soon as the dot or the
 * text are used, since then there is nothing left to explain. Kept per install, like the guide.
 */
object TapHint {
    var seen by mutableStateOf(runCatching { Prefs.bool(PREF_HINT_SEEN) }.getOrDefault(true))
        private set

    fun dismiss() {
        if (seen) return
        seen = true
        runCatching { Prefs.setBool(PREF_HINT_SEEN, true) }
    }
}

/**
 * The Daily Log, the page seen the most (docs/pantallas.md 6). [today] is the logical day `App.kt`
 * tracks; [viewedDay] is whichever day this page shows: any earlier day, today, or at most tomorrow.
 * The composer writes on [viewedDay] and, on today, opens with the keyboard up unless [autoFocus] is
 * off because the guide is still in front.
 */
@Composable
fun TodayScreen(
    today: LocalDate,
    viewedDay: LocalDate,
    onViewedDayChange: (LocalDate) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onReview: (ReviewScope) -> Unit,
    onNavigateTo: (Place) -> Unit,
    reminderOfferOn: LocalDate?,
    onReminderOffer: (LocalDate?) -> Unit,
    focusSignal: Int,
    autoFocus: Boolean,
) {
    val journal = BobbinRepository.journal
    val isToday = viewedDay == today

    // Once, after the first bullet is saved (docs/pantallas.md 6.3). Marked as offered the moment it
    // shows: closing the app without answering is also an answer, and it does not come back.
    val firstBulletSaved = journal.entries.any { !it.gone }
    LaunchedEffect(isToday, firstBulletSaved, journal.settings.reminderOffered) {
        if (isToday && firstBulletSaved && !journal.settings.reminderOffered) {
            BobbinRepository.settings { it.copy(reminderOffered = true) }
            onReminderOffer(today)
        }
    }
    val maxDay = today.plus(1, DateTimeUnit.DAY)
    val daily = journal.ofDay(viewedDay).filter { it.place is Place.Daily }
    val calendarLine = journal.ofDay(viewedDay).filter { it.place is Place.Monthly }
    var sharing by remember { mutableStateOf<ShareContent?>(null) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Page(
        tab = true,
        scroll = scroll,
        bottom = {
            Composer(
                place = Place.Daily(viewedDay),
                today = today,
                autoFocus = autoFocus && isToday,
                focusSignal = focusSignal,
                // The new line is the last one: bring it into sight above the composer.
                onSaved = { scope.launch { delay(60); scroll.animateScrollTo(scroll.maxValue) } },
            )
        },
    ) {
        TopBar(onSearch, onSettings)
        PageHead(
            title = S.dayTitle(viewedDay),
            subtitle = S.daySubtitle(viewedDay, today),
            dot = isToday,
            action = if (isToday) null else S.backToToday to { onViewedDayChange(today) },
            // Gesture 3 (docs/pantallas.md 4): swiping the title changes the day.
            titleModifier = Modifier.pointerInputHorizontalSwipe(
                viewedDay,
                threshold = 72.dp,
                onSwipeRight = { onViewedDayChange(viewedDay.minus(1, DateTimeUnit.DAY)) },
                onSwipeLeft = { if (viewedDay < maxDay) onViewedDayChange(viewedDay.plus(1, DateTimeUnit.DAY)) },
            ),
        ) {
            GlyphButton(Glyph.BACK, S.a11yPreviousDay, { onViewedDayChange(viewedDay.minus(1, DateTimeUnit.DAY)) })
            if (viewedDay < maxDay) {
                GlyphButton(Glyph.FORWARD, S.a11yNextDay, { onViewedDayChange(viewedDay.plus(1, DateTimeUnit.DAY)) })
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        if (isToday) {
            Notices(
                journal = journal,
                today = today,
                onReview = onReview,
                offerReminder = reminderOfferOn == today,
                onOfferAnswered = { yes ->
                    onReminderOffer(null)
                    if (yes) {
                        Reminders.requestPermission { granted ->
                            // Without the permission the reminder stays off (docs/tecnico.md 6.12).
                            if (granted) {
                                BobbinRepository.settings { it.copy(reminderOn = true) }
                                Reminders.sync()
                            }
                        }
                    }
                },
            )
        }

        Spacer(Modifier.heightIn(min = 10.dp))
        if (daily.isEmpty()) {
            EmptyText("${S.dayEmpty(viewedDay, today)} ${S.writeBelow}")
        } else {
            EntryListSection(daily, Place.Daily(viewedDay), journal, today, onNavigateTo)
        }
        if (isToday && daily.isNotEmpty() && !TapHint.seen) HintCard(S.hintTap, TapHint::dismiss)

        if (calendarLine.isNotEmpty()) {
            Eyebrow(S.calendarToday)
            EntryListSection(calendarLine, Place.Monthly(monthOf(viewedDay), viewedDay.day), journal, today, onNavigateTo)
        }
        if (daily.isNotEmpty() || calendarLine.isNotEmpty()) ShareRow(S.shareDay) { sharing = dayShare(journal, viewedDay) }
    }

    sharing?.let { ShareSheet(it, onClose = { sharing = null }) }
}

/** Every notice that applies shows, in order (docs/pantallas.md 6.3). */
@Composable
private fun Notices(
    journal: Journal,
    today: LocalDate,
    onReview: (ReviewScope) -> Unit,
    offerReminder: Boolean,
    onOfferAnswered: (Boolean) -> Unit,
) {
    if (BobbinRepository.corrupt) NoticeCard(S.noticeCorrupt, actions = listOf(S.ok to BobbinRepository::dismissCorrupt))
    if (BobbinRepository.saveFailed) NoticeCard(S.noticeSaveFailed)
    if (offerReminder) {
        val settings = journal.settings
        NoticeCard(
            S.offerReminder(settings.reminderHour, settings.reminderMinute),
            actions = listOf(S.yes to { onOfferAnswered(true) }, S.notNow to { onOfferAnswered(false) }),
        )
    }
    journal.unclosedMonth(today)?.let { month ->
        NoticeCard(
            S.unclosedMonth(month, journal.openTasksOfMonth(month).size),
            S.unclosedBody,
            listOf(S.reviewMonth(month) to { onReview(ReviewScope.Month(month)) }),
        )
    }
    val earlier = journal.openTasksBefore(today)
    if (earlier.isNotEmpty()) {
        NoticeCard(S.earlierOpen(earlier.size), S.earlierBody, listOf(S.reviewEarlier to { onReview(ReviewScope.Earlier(today)) }))
    }
}

/** Sharing a page (docs/pantallas.md 17), at its foot: it is something done with the page, not a way in. */
@Composable
fun ShareRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.padding(start = HEAD_START, top = 18.dp).heightIn(min = 44.dp).clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(start = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.SHARE, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(label, style = Type.Label.copy(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp))
    }
}

/** Gesture 3 (docs/pantallas.md 4): 72dp of horizontal travel commits a day change; a plain tap still falls through. */
internal fun Modifier.pointerInputHorizontalSwipe(
    key: Any?,
    threshold: Dp,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
): Modifier = this.pointerInput(key) {
    var total = 0f
    detectHorizontalDragGestures(
        onDragStart = { total = 0f },
        onHorizontalDrag = { change, dragAmount ->
            total += dragAmount
            change.consume()
        },
        onDragEnd = {
            val px = threshold.toPx()
            when {
                total >= px -> onSwipeRight()
                total <= -px -> onSwipeLeft()
            }
        },
    )
}
