package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.FUTURE_MONTHS
import com.baltajmn.bullet.model.FUTURE_MONTHS_MAX
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Which entry's sheet is open, app wide: one at a time, and the row it belongs to shows it. Opening
 * one is also using what Hoy's hint explains, so the hint goes.
 */
object EntrySheetState {
    var id by mutableStateOf<String?>(null)
        private set

    fun open(entryId: String) {
        id = entryId
        TapHint.dismiss()
    }

    fun close() {
        id = null
    }
}

/** The status actions an entry's own state allows (docs/pantallas.md 5.6). An event or a note offers none. */
enum class StatusAction { DONE, MOVE, OTHER_MONTH, ELSEWHERE, DISCARD, REOPEN, GO_TO_COPY }

fun statusActionsFor(entry: Entry, copyExists: Boolean): List<StatusAction> {
    if (entry.bullet != Bullet.TASK) return emptyList()
    return when (entry.status) {
        TaskStatus.OPEN -> listOf(StatusAction.DONE, StatusAction.MOVE, StatusAction.OTHER_MONTH, StatusAction.ELSEWHERE, StatusAction.DISCARD)
        TaskStatus.DONE, TaskStatus.IRRELEVANT -> listOf(StatusAction.REOPEN)
        TaskStatus.MIGRATED, TaskStatus.SCHEDULED -> if (copyExists) listOf(StatusAction.GO_TO_COPY) else emptyList()
    }
}

/**
 * The one move the sheet offers by name (docs/pantallas.md 5.6): a task of today or later goes to the
 * next day, anything else to today. Every other destination is under "Pasar a otro sitio".
 */
fun moveTarget(entry: Entry, today: LocalDate): Place {
    val p = entry.place
    return if (p is Place.Daily && p.date >= today) Place.Daily(p.date.plus(1, DateTimeUnit.DAY)) else Place.Daily(today)
}

internal fun moveLabel(entry: Entry, today: LocalDate): String {
    val p = entry.place
    return when {
        p is Place.Daily && p.date == today -> S.moveTomorrow
        p is Place.Daily && p.date > today -> S.moveNextDay
        else -> S.moveToday
    }
}

private enum class SheetMode { MAIN, MONTHS, ELSEWHERE, LISTS, MARKS, EDIT }

/**
 * The sheet of an entry (docs/pantallas.md 5.6), opened by touching its text: what it is, and every
 * action with what it will do written under it and the mark it leaves as its icon. Each action closes
 * the sheet and leaves the undo line; the ones with a choice to make open it in the same place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheetHost(today: LocalDate, onNavigateTo: (Place) -> Unit) {
    val id = EntrySheetState.id ?: return
    val journal = BobbinRepository.journal
    val entry = journal.entries.find { it.id == id && !it.gone }
    if (entry == null) {
        LaunchedEffect(id) { EntrySheetState.close() }
        return
    }
    var mode by remember(id) { mutableStateOf(SheetMode.MAIN) }
    val close = EntrySheetState::close

    ModalBottomSheet(
        onDismissRequest = close,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(36.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline))
        },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 18.dp)) {
            SheetHeader(S.entryKind(entry.bullet, entry.status, Signifier.PRIORITY in entry.signifiers), entry.text, close)
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                val back = { mode = SheetMode.MAIN }
                when (mode) {
                    SheetMode.MAIN -> MainActions(entry, journal, today, close, onNavigateTo) { mode = it }
                    SheetMode.MONTHS -> MonthChoices(entry, today, close, back)
                    SheetMode.ELSEWHERE -> ElsewhereChoices(entry, journal, today, close, back) { mode = SheetMode.LISTS }
                    SheetMode.LISTS -> ListChoices(entry, journal, today, close) { mode = SheetMode.ELSEWHERE }
                    SheetMode.MARKS -> MarkChoices(entry, close, back)
                    SheetMode.EDIT -> EditText(entry, close, back)
                }
            }
        }
    }
}

/** What the sheet is about (docs/pantallas.md 5.6): its kind in plain words and the entry itself. */
@Composable
fun SheetHeader(kind: String, text: String, onClose: () -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Row(
        Modifier.fillMaxWidth()
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx()) }
            .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
    ) {
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(kind.uppercase(), style = Type.Eyebrow.copy(lineHeight = 16.sp))
            Text(text, style = Type.Ink.copy(fontSize = 18.sp), modifier = Modifier.padding(top = 3.dp))
        }
        GlyphButton(Glyph.CLOSE, S.close, onClose)
    }
}

@Composable
private fun MainActions(
    entry: Entry,
    journal: Journal,
    today: LocalDate,
    close: () -> Unit,
    onNavigateTo: (Place) -> Unit,
    go: (SheetMode) -> Unit,
) {
    val id = entry.id
    val landedCopy = copyOf(journal, id)
    statusActionsFor(entry, landedCopy != null).forEach { action ->
        when (action) {
            StatusAction.DONE -> ActionRow(S.actionDone, S.doneHow, icon = { BulletGlyph(Bullet.TASK, TaskStatus.DONE) }) {
                close()
                BobbinRepository.undoable(S.toastDone) { BobbinRepository.toggleDone(id) }
            }
            StatusAction.MOVE -> {
                val to = moveTarget(entry, today)
                val where = placeLabel(to, journal, today)
                ActionRow(moveLabel(entry, today), "${S.copiesTo(where)} ${S.leavesMark(">")}", icon = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) {
                    close()
                    BobbinRepository.undoable(S.sentence(S.movedTo(where))) { BobbinRepository.migrate(id, to) }
                }
            }
            StatusAction.OTHER_MONTH -> ActionRow(
                S.moveOtherMonth,
                "${S.otherMonthHow} ${S.leavesMark("<")}",
                icon = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) },
            ) { go(SheetMode.MONTHS) }
            StatusAction.ELSEWHERE -> ActionRow(S.moveElsewhere, S.elsewhereHow, icon = { GlyphIcon(Glyph.FORWARD) }) { go(SheetMode.ELSEWHERE) }
            StatusAction.DISCARD -> ActionRow(S.actionDiscard, S.discardHow, icon = { DiscardGlyph() }) {
                close()
                BobbinRepository.undoable(S.toastDiscarded) { BobbinRepository.discard(id) }
            }
            StatusAction.REOPEN -> ActionRow(
                if (entry.status == TaskStatus.DONE) S.actionReopen else S.actionRecover,
                S.reopenHow,
                icon = { BulletGlyph(Bullet.TASK, TaskStatus.OPEN) },
            ) {
                close()
                BobbinRepository.undoable(S.reopenHow) { BobbinRepository.reopen(id) }
            }
            StatusAction.GO_TO_COPY -> landedCopy?.let { copy ->
                ActionRow(S.actionGoToCopy, S.copyIsAt(placeLabel(copy.place, journal, today)), icon = { GlyphIcon(Glyph.FORWARD) }) {
                    close()
                    onNavigateTo(copy.place)
                }
            }
        }
    }

    val priority = Signifier.PRIORITY in entry.signifiers
    ActionRow(
        if (priority) S.priorityOff else S.priorityOn,
        if (priority) S.priorityOffHow else S.priorityOnHow,
        icon = { SignifierGlyph(Signifier.PRIORITY) },
    ) {
        close()
        BobbinRepository.undoable(if (priority) S.toastPriorityOff else S.toastPriorityOn) { BobbinRepository.toggleSignifier(id, Signifier.PRIORITY) }
    }
    ActionRow(S.otherMarks, S.otherMarksHow, icon = { SignifierGlyph(Signifier.INSPIRATION) }) { go(SheetMode.MARKS) }
    ActionRow(S.actionEdit, icon = { GlyphIcon(Glyph.EDIT) }) { go(SheetMode.EDIT) }
    ActionRow(S.actionDelete, S.deleteHow, icon = { GlyphIcon(Glyph.TRASH) }) {
        close()
        BobbinRepository.delete(id)
    }
}

@Composable
private fun BackRow(onBack: () -> Unit) = ActionRow(S.backToOptions, icon = { GlyphIcon(Glyph.BACK) }, onClick = onBack)

/** "Llevar a otro mes" (docs/pantallas.md 5.7): six months at a time, up to [FUTURE_MONTHS_MAX]. */
@Composable
private fun MonthChoices(entry: Entry, today: LocalDate, close: () -> Unit, onBack: () -> Unit) {
    var shown by remember { mutableStateOf(FUTURE_MONTHS) }
    futureMonths(today, shown).forEach { month ->
        ActionRow(S.monthTitle(month), S.waitsIn(month), icon = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }) {
            close()
            BobbinRepository.undoable(S.toastScheduled(month)) { BobbinRepository.schedule(entry.id, month, null) }
        }
    }
    if (shown < FUTURE_MONTHS_MAX) {
        ActionRow(S.showMoreMonths, icon = { GlyphIcon(Glyph.PLUS) }) { shown = minOf(shown + FUTURE_MONTHS, FUTURE_MONTHS_MAX) }
    }
    BackRow(onBack)
}

/**
 * "Pasar a otro sitio" (docs/pantallas.md 5.7): today, tomorrow, this month's tasks, a day of this month
 * typed by hand, or a list. The entry's own place is never offered.
 */
@Composable
private fun ElsewhereChoices(entry: Entry, journal: Journal, today: LocalDate, close: () -> Unit, onBack: () -> Unit, onLists: () -> Unit) {
    val month = monthOf(today)
    fun move(to: Place) {
        val where = placeLabel(to, journal, today)
        close()
        BobbinRepository.undoable(S.sentence(S.movedTo(where))) { BobbinRepository.migrate(entry.id, to) }
    }
    listOf(
        S.toToday to Place.Daily(today),
        S.toTomorrow to Place.Daily(today.plus(1, DateTimeUnit.DAY)),
        S.toThisMonth to Place.Monthly(month),
    ).filter { it.second != entry.place }.forEach { (label, to) ->
        ActionRow(label, placeLabel(to, journal, today).replaceFirstChar { it.uppercase() }, icon = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) { move(to) }
    }

    var dayText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 6.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }
        Spacer(Modifier.width(6.dp))
        Text(S.toDayOfMonth, style = Type.Body, modifier = Modifier.weight(1f))
        Box(
            Modifier.width(56.dp).height(40.dp).fieldFrame(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.outline, 12.dp).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) { DayField(dayText, Modifier.fillMaxWidth()) { dayText = it; error = null } }
        QuietButton(S.moveAction, {
            val day = dayText.toIntOrNull()
            when {
                day == null -> Unit
                day !in 1..monthDays(month) -> error = S.dayOutOfRange(month, day)
                LocalDate(month.year, month.month, day) < today -> error = S.dayPast
                else -> move(Place.Monthly(month, day))
            }
        })
    }
    error?.let { Text(it, style = Type.Secondary, modifier = Modifier.padding(start = 52.dp, bottom = 6.dp)) }
    ActionRow(S.toCollection, icon = { GlyphIcon(Glyph.LIST) }, onClick = onLists)
    BackRow(onBack)
}

/**
 * "A una lista" (docs/pantallas.md 5.7): the lists that are not archived, in the order they were
 * started, and a field that starts a new one and moves the task there in one go. Trackers hold no
 * entries, so they are not offered.
 */
@Composable
private fun ListChoices(entry: Entry, journal: Journal, today: LocalDate, close: () -> Unit, onBack: () -> Unit) {
    fun moveTo(id: String) {
        val to = Place.InCollection(id)
        val where = placeLabel(to, BobbinRepository.journal, today)
        close()
        BobbinRepository.undoable(S.sentence(S.movedTo(where))) { BobbinRepository.migrate(entry.id, to) }
    }
    journal.collections.filter { !it.archived && it.kind == CollectionKind.NOTES && entry.place != Place.InCollection(it.id) }
        .sortedBy { it.createdAt }
        .forEach { c -> ActionRow(c.title, icon = { GlyphIcon(Glyph.LIST) }) { moveTo(c.id) } }

    var title by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.weight(1f).height(44.dp).fieldFrame(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.outline, 12.dp).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (title.isEmpty()) Text(S.newList, style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            BasicTextField(
                value = title,
                onValueChange = { title = it.oneLine() },
                modifier = Modifier.fillMaxWidth(),
                textStyle = Type.Ink.copy(fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { BobbinRepository.createCollection(title)?.let(::moveTo) }),
            )
        }
        QuietButton(S.create, { BobbinRepository.createCollection(title)?.let(::moveTo) })
    }
    BackRow(onBack)
}

/** Inspiration and explore (docs/pantallas.md 5.6), each with a check while it is on. */
@Composable
private fun MarkChoices(entry: Entry, close: () -> Unit, onBack: () -> Unit) {
    listOf(Signifier.INSPIRATION to S.keyInspiration, Signifier.EXPLORE to S.keyExplore).forEach { (s, how) ->
        val on = s in entry.signifiers
        ActionRow(
            S.glyphName(s),
            how,
            icon = { SignifierGlyph(s) },
            trailing = if (on) ({ GlyphIcon(Glyph.CHECK, tint = MaterialTheme.colorScheme.onBackground) }) else null,
        ) {
            close()
            BobbinRepository.undoable(S.toastSaved) { BobbinRepository.toggleSignifier(entry.id, s) }
        }
    }
    BackRow(onBack)
}

/** docs/pantallas.md 5.4: the same tope as writing, no prefixes; an empty text leaves the entry as it was. */
@Composable
private fun EditText(entry: Entry, close: () -> Unit, onBack: () -> Unit) {
    var value by remember(entry.id) { mutableStateOf(TextFieldValue(entry.text, TextRange(0, entry.text.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    fun save() {
        close()
        if (value.text.isNotBlank() && value.text != entry.text) {
            BobbinRepository.undoable(S.toastSaved) { BobbinRepository.editText(entry.id, value.text) }
        }
    }
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.weight(1f).heightIn(min = 44.dp).fieldFrame(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.outline, 12.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { new ->
                    val edit = limitEdit(value.text.oneLine(), new.text.oneLine(), new.selection.end, TEXT_LIMIT)
                    value = TextFieldValue(edit.text, TextRange(edit.cursor))
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = Type.Ink.copy(fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.height(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary)
                .clickable(role = Role.Button, onClick = ::save).padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) { Text(S.save, style = Type.Label.copy(color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)) }
    }
    BackRow(onBack)
}
