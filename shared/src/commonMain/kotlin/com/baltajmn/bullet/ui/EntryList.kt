package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.baltajmn.bullet.data.BobbinRepository
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

/**
 * The entry list and the capture row (docs/pantallas.md 5, docs/tecnico.md 3): shared by Hoy (#21,
 * #22), and later by Mes (#24) and Colección (#30).
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
 */
@Composable
fun CaptureRow(place: Place, dayKey: Any) {
    // A new day, month block or collection is a new field: neither its text nor its focus carries
    // over from another one.
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
