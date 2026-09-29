package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.baltajmn.bullet.data.Reminders
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
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
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The Daily Log, the page seen the most (docs/pantallas.md 6, #21). [today] is the logical day
 * `App.kt` tracks; [viewedDay] is whichever day this page currently shows: any earlier day, today,
 * or at most tomorrow (a migration target that has to stay reachable, docs/pantallas.md 6.1).
 */
@Composable
fun TodayScreen(
    today: LocalDate,
    viewedDay: LocalDate,
    onViewedDayChange: (LocalDate) -> Unit,
    onKey: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onReview: (ReviewScope) -> Unit,
    onNavigateTo: (Place) -> Unit,
    reminderOfferOn: LocalDate?,
    onReminderOffer: (LocalDate?) -> Unit,
    focusSignal: Int,
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

    // Only one entry edits or opens its sheet at a time; #22, #23.
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper().page()) {
            HeaderIcons(onKey, onSearch, onSettings)
            TitleRow(
                day = viewedDay,
                isToday = isToday,
                canGoForward = viewedDay < maxDay,
                onPrevious = { onViewedDayChange(viewedDay.minus(1, DateTimeUnit.DAY)) },
                onNext = { if (viewedDay < maxDay) onViewedDayChange(viewedDay.plus(1, DateTimeUnit.DAY)) },
                onSwipedBack = { if (viewedDay < maxDay) onViewedDayChange(viewedDay.plus(1, DateTimeUnit.DAY)) },
                onSwipedForward = { onViewedDayChange(viewedDay.minus(1, DateTimeUnit.DAY)) },
                onReturnToToday = { onViewedDayChange(today) },
            )
            SubtitleRow(viewedDay, isToday) { onViewedDayChange(today) }

            if (journal.entries.isEmpty()) {
                Spacer(Modifier.height(gridUnit))
                Text(S.prefixHint, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
            } else if (isToday) {
                NoticeStrip(
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

            Spacer(Modifier.height(gridUnit))
            EntryListSection(
                entries = daily,
                place = Place.Daily(viewedDay),
                journal = journal,
                editingId = editingId,
                onStartEdit = { editingId = it },
                onSaveEdit = { id, text -> BobbinRepository.editText(id, text); editingId = null },
                onLongPress = { sheetEntryId = it },
                onToggleDone = { BobbinRepository.toggleDone(it) },
                onNavigateTo = onNavigateTo,
                onReorder = BobbinRepository::reorder,
            )

            CaptureRow(place = Place.Daily(viewedDay), dayKey = viewedDay, focusSignal = focusSignal)

            if (calendarLine.isNotEmpty()) {
                Spacer(Modifier.height(gridUnit))
                Text(S.calendarToday.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
                EntryListSection(
                    entries = calendarLine,
                    place = Place.Monthly(monthOf(viewedDay), viewedDay.day),
                    journal = journal,
                    editingId = editingId,
                    onStartEdit = { editingId = it },
                    onSaveEdit = { id, text -> BobbinRepository.editText(id, text); editingId = null },
                    onLongPress = { sheetEntryId = it },
                    onToggleDone = { BobbinRepository.toggleDone(it) },
                    onNavigateTo = onNavigateTo,
                    onReorder = BobbinRepository::reorder,
                )
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
        // Above the tab bar or the accessory row, whichever is at the bottom right now
        // (docs/pantallas.md 5.8): both sit right after this weighted column in the layout.
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }

    val sheetEntry = sheetEntryId?.let { id -> journal.entries.find { it.id == id && !it.gone } }
    if (sheetEntryId != null && sheetEntry == null) {
        // The entry closed the sheet on itself (deleted from elsewhere): nothing left to show.
        sheetEntryId = null
    } else if (sheetEntry != null) {
        EntrySheet(
            entry = sheetEntry,
            journal = journal,
            today = today,
            onClose = { sheetEntryId = null },
            onEdit = { editingId = it },
            onGoToCopy = onNavigateTo,
        )
    }
}

@Composable
private fun HeaderIcons(onKey: () -> Unit, onSearch: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        // SHARE lands with #43: ofDay(d) has nothing to share to yet.
        GlyphButton(Glyph.KEY, S.a11yKey, onKey)
        GlyphButton(Glyph.SEARCH, S.a11ySearch, onSearch)
        GlyphButton(Glyph.SETTINGS, S.a11ySettings, onSettings)
    }
}

@Composable
private fun TitleRow(
    day: LocalDate,
    isToday: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSwipedBack: () -> Unit,
    onSwipedForward: () -> Unit,
    onReturnToToday: () -> Unit,
) {
    // Gesture 3 (docs/pantallas.md 4): 72dp of horizontal travel, at scale 1, commits a day change.
    // Scoped to the title row only, per the issue: the entry list keeps its own vertical scroll.
    val swipeThreshold = 72.dp
    Row(
        Modifier.fillMaxWidth().height(gridUnit * 2)
            .pointerInputHorizontalSwipe(day, threshold = swipeThreshold, onSwipeRight = onSwipedBack, onSwipeLeft = onSwipedForward),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isToday) {
            Box(Modifier.padding(start = 12.dp).size(8.dp).background(MaterialTheme.colorScheme.primary, shape = CircleShape))
            Spacer(Modifier.width(16.dp))
        } else {
            Spacer(Modifier.width(36.dp))
        }
        Text(
            S.dayTitle(day),
            style = Type.PageTitle,
            modifier = Modifier.weight(1f).clickable(enabled = !isToday, role = Role.Button, onClick = onReturnToToday),
        )
        GlyphButton(Glyph.BACK, S.a11yPreviousDay, onPrevious)
        if (canGoForward) GlyphButton(Glyph.FORWARD, S.a11yNextDay, onNext) else Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun SubtitleRow(day: LocalDate, isToday: Boolean, onBackToToday: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(gridUnit), verticalAlignment = Alignment.CenterVertically) {
        Text(S.monthYear(monthOf(day)), style = Type.Secondary, modifier = Modifier.padding(start = 48.dp).weight(1f))
        if (!isToday) TextAction(S.backToToday, onBackToToday)
    }
}

/** One notice at a time is never the rule here (docs/pantallas.md 6.3): every line that applies shows, in order. */
@Composable
private fun NoticeStrip(
    journal: Journal,
    today: LocalDate,
    onReview: (ReviewScope) -> Unit,
    offerReminder: Boolean,
    onOfferAnswered: (Boolean) -> Unit,
) {
    if (BobbinRepository.corrupt) {
        NoticeLine(S.noticeCorrupt, S.ok, BobbinRepository::dismissCorrupt)
    }
    if (BobbinRepository.saveFailed) {
        NoticeLine(S.noticeSaveFailed)
    }
    if (offerReminder) {
        val settings = journal.settings
        // Two answers do not fit on the notice's one line, so they go on the line under it.
        Column(Modifier.fillMaxWidth().padding(start = 48.dp, end = 24.dp)) {
            Text(S.offerReminder(settings.reminderHour, settings.reminderMinute), style = Type.Body)
            Row(Modifier.offset(x = (-8).dp)) {
                TextAction(S.notNow) { onOfferAnswered(false) }
                TextAction(S.yes) { onOfferAnswered(true) }
            }
        }
    }
    journal.unclosedMonth(today)?.let { month ->
        NoticeLine(S.unclosedMonth(month, journal.openTasksOfMonth(month).size), action = { onReview(ReviewScope.Month(month)) })
    }
    val earlier = journal.openTasksBefore(today)
    if (earlier.isNotEmpty()) {
        NoticeLine(S.earlierOpen(earlier.size), action = { onReview(ReviewScope.Earlier(today)) })
    }
}


/** Gesture 3 (docs/pantallas.md 4): 72dp of horizontal travel commits a day change; a plain tap still falls through. */
private fun Modifier.pointerInputHorizontalSwipe(
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
