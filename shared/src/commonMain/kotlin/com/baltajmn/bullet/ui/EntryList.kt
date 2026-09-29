package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.UndoKind
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.COUNTER_FROM
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.codePointCount
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.rapidParse
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import kotlin.math.abs
import kotlinx.datetime.LocalDate

/**
 * The entry list and the capture row (docs/pantallas.md 5, docs/tecnico.md 3): shared by Hoy (#21,
 * #22) and Mes (#24), and later by Futuro (#25) and Colección (#30).
 */

/** Gesture 4 (docs/pantallas.md 4): past this much vertical travel after the long press, it's a drag, not a tap that opens the sheet. */
private val DRAG_SLOP = 12.dp

/**
 * One entry (docs/pantallas.md 5.1). The glyph is gesture 1: it toggles an open or done task,
 * follows a migrated or scheduled one to [copyOf], and does nothing for a note, an event or a
 * discarded task, whose "diana no existe". Tapping the text edits it in line (gesture "tocar el
 * texto", 5.4, #23). A long press that stays still opens the sheet (gesture 2, #22); one that then
 * moves past [DRAG_SLOP] lifts the row instead (gesture 4, #23) and reports its travel to
 * [onDragChanged] for [EntryListSection] to reorder live.
 */
@Composable
fun EntryRow(
    entry: Entry,
    journal: Journal,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onLongPress: () -> Unit,
    onToggleDone: () -> Unit,
    onNavigateTo: (Place) -> Unit,
    isDragged: Boolean = false,
    dragOffsetPx: Float = 0f,
    onDragStart: () -> Unit = {},
    onDragChanged: (Float) -> Unit = {},
    onDragFinished: () -> Unit = {},
) {
    val landedCopy = if (entry.bullet == Bullet.TASK && entry.status in setOf(TaskStatus.MIGRATED, TaskStatus.SCHEDULED)) {
        copyOf(journal, entry.id)
    } else {
        null
    }
    val glyphTap: (() -> Unit)? = when {
        entry.bullet == Bullet.TASK && entry.status in setOf(TaskStatus.OPEN, TaskStatus.DONE) -> onToggleDone
        landedCopy != null -> ({ onNavigateTo(landedCopy.place) })
        else -> null
    }
    val dragSlopPx = with(LocalDensity.current) { DRAG_SLOP.toPx() }
    val base = Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)
    val rowModifier = if (isDragged) {
        base.zIndex(1f).graphicsLayer { translationY = dragOffsetPx }
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    } else {
        base
    }

    Row(
        rowModifier.pointerInput(entry.id) {
            var total = 0f
            var dragging = false
            detectDragGesturesAfterLongPress(
                onDragStart = { total = 0f; dragging = false },
                onDrag = { change, amount ->
                    change.consume()
                    if (!dragging) {
                        total += amount.y
                        if (abs(total) > dragSlopPx) {
                            dragging = true
                            onDragStart()
                            onDragChanged(total)
                        }
                    } else {
                        onDragChanged(amount.y)
                    }
                },
                onDragEnd = { if (dragging) onDragFinished() else onLongPress() },
                onDragCancel = { if (dragging) onDragFinished() },
            )
        },
    ) {
        Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) {
            entry.signifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
        }
        Box(if (glyphTap != null) Modifier.clickable(role = Role.Button, onClick = glyphTap) else Modifier) {
            BulletGlyph(entry.bullet, entry.status)
        }
        if (isEditing) {
            EntryEditField(entry.text, onSave = onSaveEdit, modifier = Modifier.fillMaxWidth().weight(1f))
        } else {
            EntryText(
                entry,
                wentToText(landedCopy?.place, journal),
                onClick = onStartEdit,
                modifier = Modifier.padding(top = 2.dp).weight(1f),
            )
        }
    }
}

@Composable
private fun EntryText(entry: Entry, wentTo: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dimmed = entry.bullet == Bullet.TASK && entry.status in setOf(TaskStatus.DONE, TaskStatus.MIGRATED, TaskStatus.SCHEDULED)
    val struck = entry.status == TaskStatus.IRRELEVANT
    val base = if (dimmed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = base, textDecoration = if (struck) TextDecoration.LineThrough else TextDecoration.None)) {
            append(entry.text)
        }
        if (wentTo != null) {
            append("  ")
            withStyle(SpanStyle(color = secondary)) { append(wentTo) }
        }
    }
    Text(text, style = Type.Ink, modifier = modifier.clickable(role = Role.Button, onClick = onClick))
}

/**
 * One place's entries, draggable to reorder (docs/pantallas.md 4's gesture 4, #23): the list makes
 * room row by row as the lifted one passes, and nothing is written until the finger lifts
 * ([onReorder] then gets the ids in their new order). [entries] is assumed already sorted by
 * [com.baltajmn.bullet.model.ENTRY_ORDER].
 */
@Composable
fun EntryListSection(
    entries: List<Entry>,
    place: Place,
    journal: Journal,
    editingId: String?,
    onStartEdit: (String) -> Unit,
    onSaveEdit: (String, String) -> Unit,
    onLongPress: (String) -> Unit,
    onToggleDone: (String) -> Unit,
    onNavigateTo: (Place) -> Unit,
    onReorder: (Place, List<String>) -> Unit,
) {
    val ids = entries.map { it.id }
    var order by remember(ids) { mutableStateOf(ids) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { (gridUnit * 2).toPx() }
    val byId = entries.associateBy { it.id }

    Column {
        order.forEach { id ->
            val entry = byId[id] ?: return@forEach
            EntryRow(
                entry = entry,
                journal = journal,
                isEditing = id == editingId,
                onStartEdit = { onStartEdit(id) },
                onSaveEdit = { text -> onSaveEdit(id, text) },
                onLongPress = { onLongPress(id) },
                onToggleDone = { onToggleDone(id) },
                onNavigateTo = onNavigateTo,
                isDragged = id == draggingId,
                dragOffsetPx = dragOffset,
                onDragStart = { draggingId = id; dragOffset = 0f },
                onDragChanged = { delta ->
                    dragOffset += delta
                    val from = order.indexOf(id)
                    var to = from
                    while (dragOffset - (to - from) * rowHeightPx > rowHeightPx / 2 && to < order.lastIndex) to++
                    while (dragOffset - (to - from) * rowHeightPx < -rowHeightPx / 2 && to > 0) to--
                    if (to != from) {
                        order = order.toMutableList().also { it.removeAt(from); it.add(to, id) }
                        dragOffset -= (to - from) * rowHeightPx
                    }
                },
                onDragFinished = {
                    draggingId = null
                    dragOffset = 0f
                    if (order != ids) onReorder(place, order)
                },
            )
        }
    }
}

/** docs/pantallas.md 5.1: where a migrated or scheduled task's copy landed, or null once it no longer exists. */
private fun wentToText(place: Place?, journal: Journal): String? = when (place) {
    null -> null
    is Place.Daily -> S.wentToDay(place.date)
    is Place.Monthly -> S.wentToMonth(place.month)
    is Place.Future -> S.wentToFuture(place.month, place.day)
    is Place.InCollection -> journal.collections.find { it.id == place.id }?.title
}

/** docs/pantallas.md 5.4: same style and tope as capturing, no prefixes; Intro or losing focus saves. */
@Composable
private fun EntryEditField(initial: String, onSave: (String) -> Unit, modifier: Modifier = Modifier) {
    var value by remember(initial) { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    var saved by remember(initial) { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    fun commit() {
        if (saved) return
        saved = true
        onSave(value.text)
    }

    BasicTextField(
        value = value,
        onValueChange = { new ->
            val edit = limitEdit(value.text.oneLine(), new.text.oneLine(), new.selection.end, TEXT_LIMIT)
            value = TextFieldValue(edit.text, TextRange(edit.cursor))
        },
        modifier = modifier.focusRequester(focus)
            .onFocusChanged { if (!it.isFocused) commit() }
            .onPreviewKeyEvent { event ->
                val enter = event.key == Key.Enter || event.key == Key.NumPadEnter
                if (enter && event.type == KeyEventType.KeyDown) {
                    commit()
                    true
                } else {
                    enter
                }
            },
        textStyle = Type.Ink,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { commit() }),
    )
}

/**
 * The capture row (docs/pantallas.md 5.2, 5.3): the next empty row of the page, a live glyph that
 * follows `rapidParse` as it types, and the three way Task/Event/Note selector above the keyboard.
 * Never a signifier here (docs/pantallas.md 5.3, #22): those only come from the sheet, once created.
 * [autoFocus] is false for the one always there at the end of Mes: that page opens "sin foco ni
 * teclado" (docs/pantallas.md 7.1), unlike Hoy (#21) or a day of Mes opened on purpose.
 * [beforeSave] returning false refuses the capture without clearing the field: a Future Log block
 * uses it for a day its month does not have, which "no crea nada al pulsar Intro" (8, #25).
 */
@Composable
fun CaptureRow(place: Place, dayKey: Any, autoFocus: Boolean = true, focusSignal: Int = 0, beforeSave: () -> Boolean = { true }) {
    // A new day, month block or collection is a new field: neither its text nor its focus carries
    // over from another one.
    key(dayKey) {
        var value by remember { mutableStateOf(TextFieldValue("")) }
        var picked by remember { mutableStateOf(Bullet.TASK) }
        var focused by remember { mutableStateOf(false) }
        val focus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        // [focusSignal] goes up when a link asks for the keyboard again (`bobbin://today?focus`).
        LaunchedEffect(focusSignal) {
            if (autoFocus || focusSignal > 0) {
                focus.requestFocus()
                keyboard?.show()
            }
        }

        val parsed = rapidParse(value.text, picked)
        val previewBullet = parsed?.bullet ?: picked
        val previewSignifiers = parsed?.signifiers.orEmpty()
        val count = value.text.codePointCount()

        fun submit() {
            if (!beforeSave()) return
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
            Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                BulletChoice(Bullet.TASK, S.bulletTask, picked == Bullet.TASK) { picked = Bullet.TASK }
                BulletChoice(Bullet.EVENT, S.bulletEvent, picked == Bullet.EVENT) { picked = Bullet.EVENT }
                BulletChoice(Bullet.NOTE, S.bulletNote, picked == Bullet.NOTE) { picked = Bullet.NOTE }
                Spacer(Modifier.weight(1f))
                if (count >= COUNTER_FROM) Text(S.counter(count, TEXT_LIMIT), style = Type.Secondary)
            }
        }
    }
}

/** A text action in `primary` (docs/pantallas.md 1.3): 40dp tall, no background. */
@Composable
fun TextAction(label: String, onClick: () -> Unit) {
    Box(Modifier.heightIn(min = 40.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary))
    }
}

/**
 * A row of a page with the date column of docs/pantallas.md 1.4 (Mes, Futuro): [date] fills 0..48
 * (nothing for a continuation row), and [content] starts at 48, so an [EntryRow] inside lands its
 * signifiers, bullet and text 48dp further right without knowing a date column exists.
 */
@Composable
fun DatedRow(modifier: Modifier = Modifier, date: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Row(modifier.fillMaxWidth()) {
        Box(Modifier.width(48.dp)) { date?.invoke() }
        Column(Modifier.weight(1f), content = content)
    }
}

/** The date column itself (docs/pantallas.md 1.4, 7.1): the number right aligned in 0..24 with tabular digits, the weekday initial centered in 24..48. */
@Composable
fun DayNumber(date: LocalDate, isToday: Boolean = false) {
    Row(Modifier.height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        Text(
            date.day.toString(),
            style = Type.Body.copy(
                color = if (isToday) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                fontFeatureSettings = "tnum",
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.width(24.dp),
        )
        Text(S.weekdayInitial()[date.dayOfWeek.ordinal], style = Type.Secondary, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
    }
}

/**
 * docs/pantallas.md 5.5: a link to a migrated or scheduled copy leaves the page "desplazada hasta
 * ella". The row that owns the linked place takes this modifier with [active] true; it scrolls itself
 * into view once and calls [onDone], so scrolling by hand afterwards stays where it is left.
 */
@Composable
fun scrollHereWhen(active: Boolean, onDone: () -> Unit): Modifier {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(active) {
        if (active) {
            requester.bringIntoView()
            onDone()
        }
    }
    return Modifier.bringIntoViewRequester(requester)
}

/**
 * A two digit day field (docs/pantallas.md 5.7, 8): digits only, never a calendar picker. Shared by
 * the sheet's destination picker (#22) and the date column of a Future Log block's capture (#25).
 */
@Composable
fun DayField(text: String, modifier: Modifier = Modifier.width(40.dp), onChange: (String) -> Unit) {
    var value by remember(text) { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    BasicTextField(
        value = value,
        onValueChange = { new ->
            val digits = new.text.filter(Char::isDigit).take(2)
            value = TextFieldValue(digits, TextRange(digits.length))
            onChange(digits)
        },
        modifier = modifier,
        textStyle = Type.Body,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(),
        decorationBox = { inner ->
            if (value.text.isEmpty()) Text(S.dayField, style = Type.Secondary)
            inner()
        },
    )
}

/** The icon row of a tab without `KEY` (docs/pantallas.md 3.2): `SEARCH` and `SETTINGS`. `SHARE` joins with #43. */
@Composable
fun TabHeaderIcons(onSearch: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        GlyphButton(Glyph.SEARCH, S.a11ySearch, onSearch)
        GlyphButton(Glyph.SETTINGS, S.a11ySettings, onSettings)
    }
}

/**
 * A line of a notice strip (docs/pantallas.md 6.3, 7.2): plain text, text with an action beside it, or
 * a whole line that is the action. Shared by Hoy (#21) and Mes (#26).
 */
@Composable
fun NoticeLine(text: String, actionLabel: String? = null, action: (() -> Unit)? = null) {
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

/**
 * docs/pantallas.md 5.8: `undo` and what went, shown while `BobbinRepository.pendingUndo` is set. The
 * text follows the kind, so the same line serves an entry (#23) and a whole collection (#30).
 */
@Composable
fun UndoBanner(onUndo: () -> Unit) {
    val kind = BobbinRepository.pendingUndo?.kind ?: return
    val line = MaterialTheme.colorScheme.outlineVariant
    Row(
        Modifier.fillMaxWidth().height(gridUnit * 2)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind { drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val what = if (kind == UndoKind.COLLECTION) S.collectionDeleted else S.entryDeleted
        Text(what, style = Type.Body, modifier = Modifier.padding(start = 24.dp).weight(1f))
        TextAction(S.undo, onUndo)
        Spacer(Modifier.width(16.dp))
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
