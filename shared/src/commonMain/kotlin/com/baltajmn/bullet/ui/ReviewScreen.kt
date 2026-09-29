package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.askReviewAfter
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.FUTURE_MONTHS
import com.baltajmn.bullet.model.FUTURE_MONTHS_MAX
import com.baltajmn.bullet.model.MIGRATION_SHOWN_FROM
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.futureMonth
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.futureWaiting
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.notePlace
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.placeDay
import com.baltajmn.bullet.model.reviewDays
import com.baltajmn.bullet.model.reviewQueue
import com.baltajmn.bullet.ui.theme.Type
import kotlinx.datetime.LocalDate

private enum class Step { INTRO, TASKS, END }

/**
 * A review (docs/pantallas.md 11, docs/tecnico.md 6.6): reread the period first, then one open task at a
 * time with five ways out, each saying what it will do, and nothing anywhere that decides two. The queue
 * comes from [reviewQueue] every recomposition, so each decision already changed its own task and there
 * is no progress to save; "Decidir luego" only lives in this session, and the task stays open.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewScreen(scope: ReviewScope, today: LocalDate, onClose: () -> Unit, onToToday: () -> Unit) {
    BackHandler(true, onClose)
    val journal = BobbinRepository.journal
    val month = (scope as? ReviewScope.Month)?.month
    var step by remember { mutableStateOf(Step.INTRO) }
    var later by remember { mutableStateOf(emptySet<String>()) }
    // Counted once, at open (docs/pantallas.md 11.2): each decision takes a task out of the queue.
    val total = remember { journal.reviewQueue(scope).size }
    val queue = journal.reviewQueue(scope).filterNot { it.id in later }
    val label = when (scope) {
        is ReviewScope.Month -> S.reviewMonth(scope.month)
        is ReviewScope.Earlier -> S.reviewEarlier
        is ReviewScope.Day -> S.reviewDay
    }

    when (step) {
        Step.INTRO -> {
            var note by remember { mutableStateOf("") }
            Page(
                tab = false,
                bottom = {
                    Footer {
                        PrimaryButton(S.decideTasks(total), {
                            if (note.isNotBlank()) BobbinRepository.captureNote(note, scope.notePlace())
                            step = if (total > 0) Step.TASKS else Step.END
                        }, block = true)
                        QuietButton(S.notNow, onClose, Modifier.align(Alignment.CenterHorizontally))
                    }
                },
            ) {
                ReviewTop(onClose)
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(label.uppercase(), style = Type.Eyebrow)
                    Text(S.rereadTitle(month), style = Type.Heading, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp).semantics { heading() })
                    Text(S.rereadLead(total), style = Type.Secondary.copy(fontSize = 15.sp, lineHeight = 22.sp))
                }
                Spacer(Modifier.height(8.dp))
                Column(Modifier.padding(start = 8.dp, end = 8.dp)) {
                    journal.reviewDays(scope).forEach { (date, entries) ->
                        Eyebrow(S.dayTitle(date))
                        entries.forEach { EntryRow(it, journal, today, onNavigateTo = {}, still = true, showFrom = false) }
                    }
                    // Only a month has a calendar and tasks of its own to reread.
                    if (month != null) {
                        val calendar = (1..monthDays(month)).flatMap { journal.entriesAt(Place.Monthly(month, it)) }
                        if (calendar.isNotEmpty()) {
                            Eyebrow(S.calendarTitle)
                            calendar.forEach { EntryRow(it, journal, today, onNavigateTo = {}, still = true, showFrom = false) }
                        }
                        val tasks = journal.entriesAt(Place.Monthly(month))
                        if (tasks.isNotEmpty()) {
                            Eyebrow(S.monthTasks)
                            tasks.forEach { EntryRow(it, journal, today, onNavigateTo = {}, still = true, showFrom = false) }
                        }
                    }
                }
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(S.rereadNote(month), style = Type.Secondary, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                    NoteField(note) { note = it }
                }
            }
        }

        Step.TASKS -> {
            val entry = queue.firstOrNull()
            LaunchedEffect(entry == null) { if (entry == null) step = Step.END }
            if (entry == null) return
            var months by remember(entry.id) { mutableStateOf(false) }
            val position = (total - queue.size + 1).coerceIn(1, maxOf(total, 1))
            Page(tab = false) {
                ReviewTop(onClose, S.taskOf(position, total), position, total)
                TaskUnderReview(originOf(entry), entry, journal.migrationCount(entry.id))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    if (months) {
                        ScheduleChoices(today, entry) { months = false }
                    } else {
                        ActionRow(S.actionDone, S.doneStaysIn(month), icon = { BulletGlyph(Bullet.TASK, TaskStatus.DONE) }) {
                            BobbinRepository.undoable(S.toastDone) { BobbinRepository.toggleDone(entry.id) }
                        }
                        val to = moveTarget(entry, today)
                        val where = placeLabel(to, journal, today)
                        ActionRow(moveLabel(entry, today), "${S.copiesTo(where)} ${S.leavesMark(">", month)}", icon = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) {
                            BobbinRepository.undoable(S.sentence(S.movedTo(where))) { BobbinRepository.migrate(entry.id, to) }
                        }
                        ActionRow(S.moveOtherMonth, "${S.otherMonthHow} ${S.leavesMark("<", month)}", icon = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }) {
                            months = true
                        }
                        ActionRow(S.actionDiscard, S.discardHow, icon = { DiscardGlyph() }) {
                            BobbinRepository.undoable(S.toastDiscarded) { BobbinRepository.discard(entry.id) }
                        }
                        ActionRow(S.decideLater, S.decideLaterHow, icon = { GlyphIcon(Glyph.LATER) }) { later = later + entry.id }
                    }
                }
            }
        }

        Step.END -> {
            val left = journal.reviewQueue(scope).size
            // The first month closed with nothing left open is when the rating is asked for, once (6.6).
            LaunchedEffect(Unit) { askReviewAfter(scope, decided = total - left, left = left) }
            Page(tab = false, bottom = { Footer { PrimaryButton(S.backToToday, onToToday, block = true) } }) {
                ReviewTop(onClose)
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(S.reviewFinished.uppercase(), style = Type.Eyebrow)
                    Text(S.reviewEndTitle(month, left), style = Type.Heading, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp).semantics { heading() })
                    Text(S.reviewEndLead(left), style = Type.Secondary.copy(fontSize = 15.sp, lineHeight = 22.sp))
                }
            }
        }
    }
}

/** "Llevar a otro mes" inside a review: the same months as the sheet, and back to the five ways out. */
@Composable
private fun ScheduleChoices(today: LocalDate, entry: Entry, onBack: () -> Unit) {
    var shown by remember { mutableStateOf(FUTURE_MONTHS) }
    futureMonths(today, shown).forEach { m ->
        ActionRow(S.monthTitle(m), S.waitsIn(m), icon = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }) {
            BobbinRepository.undoable(S.toastScheduled(m)) { BobbinRepository.schedule(entry.id, m, null) }
            onBack()
        }
    }
    if (shown < FUTURE_MONTHS_MAX) {
        ActionRow(S.showMoreMonths, icon = { GlyphIcon(Glyph.PLUS) }) { shown = minOf(shown + FUTURE_MONTHS, FUTURE_MONTHS_MAX) }
    }
    ActionRow(S.backToOptions, icon = { GlyphIcon(Glyph.BACK) }, onClick = onBack)
}

/**
 * The Future Log review (docs/pantallas.md 11.4, docs/tecnico.md 6.5), opened from the notice of Mes.
 * One entry at a time and three ways out: "Pasar al calendario" is a single `migrate`, "Descartar" a
 * single `discard` or `delete`, and "Dejarla" writes nothing at all. Reaching the end marks the month
 * seen so the notice does not come back; leaving half way through does not.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FutureReviewScreen(today: LocalDate, onClose: () -> Unit) {
    BackHandler(true, onClose)
    val journal = BobbinRepository.journal
    val currentMonth = monthOf(today)
    var left by remember { mutableStateOf(emptySet<String>()) }
    val queue = journal.futureWaiting(today).filterNot { it.id in left }
    val total = remember { journal.futureWaiting(today).size }
    LaunchedEffect(queue.isEmpty()) {
        if (queue.isEmpty()) BobbinRepository.settings { it.copy(futureSeen = currentMonth) }
    }

    val entry = queue.firstOrNull()
    if (entry == null) {
        Page(tab = false, bottom = { Footer { PrimaryButton(S.close, onClose, block = true) } }) {
            ReviewTop(onClose)
            Text(S.futureAllDecided, style = Type.Heading, modifier = Modifier.padding(horizontal = 24.dp))
        }
        return
    }
    val position = (total - queue.size + 1).coerceIn(1, maxOf(total, 1))
    Page(tab = false) {
        ReviewTop(onClose, S.entryOf(position, total), position, total)
        TaskUnderReview(S.fromFuture(entry.futureMonth ?: currentMonth, entry.placeDay), entry, journal.migrationCount(entry.id))
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            // The day travels with the entry only if it is a day of this month: a September entry seen
            // in October has no day to keep (6.5).
            val toDay = entry.placeDay?.takeIf { entry.futureMonth == currentMonth }
            val to = Place.Monthly(currentMonth, toDay)
            val where = placeLabel(to, journal, today)
            ActionRow(S.futureToCalendar, S.goesTo(where), icon = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) {
                BobbinRepository.undoable(S.sentence(S.movedTo(where))) { BobbinRepository.migrate(entry.id, to) }
            }
            ActionRow(S.futureLeave, S.futureLeaveHow, icon = { GlyphIcon(Glyph.LATER) }) { left = left + entry.id }
            // A task keeps its line struck through; an event or a note has no discarded state to wear,
            // so it goes, with the undo line behind it (docs/pantallas.md 11.4).
            if (entry.bullet == Bullet.TASK) {
                ActionRow(S.actionDiscard, S.discardHow, icon = { DiscardGlyph() }) {
                    BobbinRepository.undoable(S.toastDiscarded) { BobbinRepository.discard(entry.id) }
                }
            } else {
                ActionRow(S.actionDiscard, S.deleteHow, icon = { DiscardGlyph() }) { BobbinRepository.delete(entry.id) }
            }
        }
    }
}

/** docs/pantallas.md 11.2: where the task under review comes from. */
private fun originOf(entry: Entry): String = when (val p = entry.place) {
    is Place.Daily -> S.fromDay(p.date)
    is Place.Monthly -> if (p.day == null) S.fromMonthTasks(p.month) else S.fromCalendar(LocalDate(p.month.year, p.month.month, p.day))
    is Place.Future -> S.fromFuture(p.month, p.day)
    is Place.InCollection -> ""
}

/** `CLOSE` on the left and, while there is a task, where the review is: "Tarea 2 de 4" and a dot per task. */
@Composable
private fun ReviewTop(onClose: () -> Unit, position: String? = null, at: Int = 0, total: Int = 0) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphButton(Glyph.CLOSE, S.close, onClose)
        if (position != null) {
            Text(position, style = Type.Secondary, modifier = Modifier.padding(start = 4.dp, end = 8.dp))
            Row(Modifier.clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                val ink = MaterialTheme.colorScheme.onBackground
                val line = MaterialTheme.colorScheme.outline
                val muted = MaterialTheme.colorScheme.onSurfaceVariant
                // ponytail: one dot per task; a review of dozens runs off the edge, a count would scale.
                for (k in 1..minOf(total, 24)) {
                    Box(
                        Modifier.height(6.dp).width(if (k == at) 18.dp else 6.dp).clip(RoundedCornerShape(3.dp))
                            .background(if (k == at) ink else if (k < at) muted else line),
                    )
                }
            }
        }
    }
}

/** The task under review (docs/pantallas.md 11.2): where it comes from, its text large, and how often it was moved. */
@Composable
private fun TaskUnderReview(origin: String, entry: Entry, moved: Int) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Text(origin, style = Type.Secondary, modifier = Modifier.padding(top = 14.dp))
        Text(entry.text, style = Type.Heading, modifier = Modifier.padding(top = 6.dp).semantics { contentDescription = S.entryDescription(entry.bullet, entry.status, entry.signifiers, entry.text) })
        // The only pressure the method applies, and it presses towards a different decision.
        if (moved >= MIGRATION_SHOWN_FROM) {
            Text(
                S.timesMoved(moved),
                style = Type.Secondary.copy(fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.padding(top = 10.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

/** The foot of a review step: its one button, full width, over a hairline. */
@Composable
private fun Footer(content: @Composable ColumnScope.() -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .drawBehind { drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

/** The reflection note (docs/pantallas.md 11.1): one line like any entry, wrapping as it grows. */
@Composable
private fun NoteField(value: String, onChange: (String) -> Unit) {
    var field by remember { mutableStateOf(TextFieldValue(value)) }
    Box(
        Modifier.fillMaxWidth().heightIn(min = 76.dp).fieldFrame(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (field.text.isEmpty()) Text(S.rereadNoteHint, style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
        BasicTextField(
            value = field,
            onValueChange = { new ->
                val edit = limitEdit(field.text.oneLine(), new.text.oneLine(), new.selection.end, TEXT_LIMIT)
                field = TextFieldValue(edit.text, TextRange(edit.cursor))
                onChange(edit.text)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = Type.Ink.copy(fontSize = 16.sp, lineHeight = 22.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
        )
    }
}
