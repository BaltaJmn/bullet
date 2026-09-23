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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.COUNTER_FROM
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.codePointCount
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.ofDay
import com.baltajmn.bullet.model.openTasksBefore
import com.baltajmn.bullet.model.rapidParse
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
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
    onReview: () -> Unit,
) {
    val journal = BobbinRepository.journal
    val isToday = viewedDay == today
    val maxDay = today.plus(1, DateTimeUnit.DAY)

    val daily = journal.ofDay(viewedDay).filter { it.place is Place.Daily }
    val calendarLine = journal.ofDay(viewedDay).filter { it.place is Place.Monthly }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper()) {
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
                NoticeStrip(journal, today, onReview)
            }

            Spacer(Modifier.height(gridUnit))
            daily.forEach { entry -> EntryRow(entry) }

            CaptureRow(place = Place.Daily(viewedDay), dayKey = viewedDay)

            if (calendarLine.isNotEmpty()) {
                Spacer(Modifier.height(gridUnit))
                Text(S.calendarToday.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
                calendarLine.forEach { entry -> EntryRow(entry) }
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
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
private fun NoticeStrip(journal: Journal, today: LocalDate, onReview: () -> Unit) {
    if (BobbinRepository.corrupt) {
        NoticeLine(S.noticeCorrupt, S.ok, BobbinRepository::dismissCorrupt)
    }
    if (BobbinRepository.saveFailed) {
        NoticeLine(S.noticeSaveFailed)
    }
    val earlier = journal.openTasksBefore(today)
    if (earlier.isNotEmpty()) {
        NoticeLine(S.earlierOpen(earlier.size), action = onReview)
    }
}

@Composable
private fun NoticeLine(text: String, actionLabel: String? = null, action: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 48.dp).height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        if (action != null && actionLabel == null) {
            // The whole line is the action (unclosed month, earlier open tasks): pantallas 6.3.
            Text(text, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary), modifier = Modifier.clickable(role = Role.Button, onClick = action))
        } else {
            Text(text, style = Type.Body)
            if (actionLabel != null && action != null) {
                Spacer(Modifier.width(8.dp))
                TextAction(actionLabel, action)
            }
        }
    }
}

/** A read only row: tap to toggle, long press for the sheet and drag to reorder land with #22 and #23. */
@Composable
private fun EntryRow(entry: Entry) {
    Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)) {
        Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) {
            entry.signifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
        }
        BulletGlyph(entry.bullet, entry.status)
        val dimmed = entry.bullet == Bullet.TASK &&
            entry.status in setOf(TaskStatus.DONE, TaskStatus.MIGRATED, TaskStatus.SCHEDULED)
        val struck = entry.status == TaskStatus.IRRELEVANT
        Text(
            entry.text,
            style = Type.Ink.copy(
                color = if (dimmed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (struck) TextDecoration.LineThrough else TextDecoration.None,
            ),
            modifier = Modifier.padding(top = 2.dp).weight(1f),
        )
    }
}

/**
 * The capture row (docs/pantallas.md 5.2, 5.3): the next empty row of the page, a live glyph that
 * follows `rapidParse` as it types, and the three way Task/Event/Note selector above the keyboard.
 */
@Composable
private fun CaptureRow(place: Place, dayKey: LocalDate) {
    // A new day is a new field: neither its text nor its focus carries over from another one.
    key(dayKey) {
        var value by remember { mutableStateOf(TextFieldValue("")) }
        var picked by remember { mutableStateOf(Bullet.TASK) }
        var focused by remember { mutableStateOf(false) }
        val focus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focus.requestFocus()
            keyboard?.show()
        }

        val parsed = rapidParse(value.text, picked)
        val previewBullet = parsed?.bullet ?: picked
        val previewSignifiers = parsed?.signifiers.orEmpty()
        val count = value.text.codePointCount()

        fun submit() {
            val saved = BobbinRepository.capture(value.text, place, picked)
            if (saved) {
                value = TextFieldValue("")
                picked = Bullet.TASK
            }
        }

        Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)) {
            Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) {
                previewSignifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
            }
            BulletGlyph(
                previewBullet,
                TaskStatus.OPEN,
                tint = if (value.text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
            )
            Box(Modifier.weight(1f)) {
                if (value.text.isEmpty()) Text(S.captureHint, style = Type.Ink.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                BasicTextField(
                    value = value,
                    onValueChange = { new ->
                        val edit = limitEdit(value.text.oneLine(), new.text.oneLine(), new.selection.end, TEXT_LIMIT)
                        value = TextFieldValue(edit.text, TextRange(edit.cursor))
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                        .onFocusChanged { focused = it.isFocused }
                        // Intro saves; a hardware Enter must not insert the line break oneLine() would
                        // otherwise have to undo (docs/tecnico.md 6.2).
                        .onPreviewKeyEvent { event ->
                            val enter = event.key == Key.Enter || event.key == Key.NumPadEnter
                            if (enter && event.type == KeyEventType.KeyDown) {
                                submit()
                                true
                            } else {
                                enter
                            }
                        },
                    textStyle = Type.Ink,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
            }
        }

        if (focused) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                BulletChoice(Bullet.TASK, S.bulletTask, picked == Bullet.TASK) { picked = Bullet.TASK }
                BulletChoice(Bullet.EVENT, S.bulletEvent, picked == Bullet.EVENT) { picked = Bullet.EVENT }
                BulletChoice(Bullet.NOTE, S.bulletNote, picked == Bullet.NOTE) { picked = Bullet.NOTE }
                Spacer(Modifier.weight(1f))
                if (count >= COUNTER_FROM) Text(S.counter(count, TEXT_LIMIT), style = Type.Secondary)
            }
        }
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

/** A text action in `primary` (docs/pantallas.md 1.3): 40dp tall, no background. */
@Composable
private fun TextAction(label: String, onClick: () -> Unit) {
    Box(Modifier.heightIn(min = 40.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun BulletChoice(bullet: Bullet, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BulletGlyph(
            bullet,
            TaskStatus.OPEN,
            tint = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = Type.Body.copy(color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant))
    }
}
