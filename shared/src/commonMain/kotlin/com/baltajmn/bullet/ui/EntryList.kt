package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.ui.theme.Type
import kotlin.math.abs
import kotlinx.datetime.LocalDate

/**
 * The entry and the list of a place (docs/pantallas.md 5.1), shared by every page that shows entries.
 */

/** Gesture 4 (docs/pantallas.md 4): past this much vertical travel after the long press, it's a drag, not a press that opens the sheet. */
internal val DRAG_SLOP = 12.dp

/** A place as a sentence reads it (docs/textos.md 4), with a collection's title looked up in [journal]. */
fun placeLabel(place: Place, journal: Journal, today: LocalDate): String =
    S.placeLabel(place, today, (place as? Place.InCollection)?.let { p -> journal.collections.find { it.id == p.id }?.title.orEmpty() })

/**
 * One entry (docs/pantallas.md 5.1). The glyph closes an open task and reopens a done one, with the
 * undo line; on a migrated or scheduled task it follows the copy; on anything else it opens the sheet,
 * as the text always does. Under the text, where a moved task went ("Pasada a mañana") and where a
 * copy came from ("Viene de ayer"). A long press that moves lifts the row to reorder it (gesture 4);
 * one that stays still opens the sheet too. [still] is the review's reading mode: no gesture at all.
 */
@Composable
fun EntryRow(
    entry: Entry,
    journal: Journal,
    today: LocalDate,
    onNavigateTo: (Place) -> Unit,
    margin: Boolean = true,
    still: Boolean = false,
    showFrom: Boolean = true,
    isDragged: Boolean = false,
    dragOffsetPx: Float = 0f,
    onDragStart: () -> Unit = {},
    onDragChanged: (Float) -> Unit = {},
    onDragFinished: () -> Unit = {},
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    val task = entry.bullet == Bullet.TASK
    val landedCopy = if (task && entry.status in setOf(TaskStatus.MIGRATED, TaskStatus.SCHEDULED)) copyOf(journal, entry.id) else null
    val original = entry.from?.takeIf { showFrom }?.let { id -> journal.entries.find { it.id == id && !it.gone } }
    val open = { EntrySheetState.open(entry.id) }
    val glyphTap: () -> Unit = when {
        task && entry.status == TaskStatus.OPEN -> ({
            BobbinRepository.undoable(S.toastDone) { BobbinRepository.toggleDone(entry.id) }
            TapHint.dismiss()
        })
        task && entry.status == TaskStatus.DONE -> ({ BobbinRepository.undoable(S.reopenHow) { BobbinRepository.toggleDone(entry.id) } })
        landedCopy != null -> ({ onNavigateTo(landedCopy.place) })
        else -> open
    }
    val selected = EntrySheetState.id == entry.id
    val closed = task && entry.status != TaskStatus.OPEN
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    val wentTo = landedCopy?.let { placeLabel(it.place, journal, today) }
    val cameFrom = original?.let { placeLabel(it.place, journal, today) }
    val metaText = when {
        wentTo != null -> if (entry.status == TaskStatus.SCHEDULED) S.scheduledTo(wentTo) else S.movedTo(wentTo)
        cameFrom != null -> S.cameFrom(cameFrom)
        else -> null
    }

    val dragSlopPx = with(LocalDensity.current) { DRAG_SLOP.toPx() }
    val base = Modifier.fillMaxWidth().padding(start = 6.dp, end = 12.dp)
    val rowModifier = when {
        isDragged -> base.zIndex(1f).graphicsLayer { translationY = dragOffsetPx }
            .clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        selected -> base.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        else -> base
    }

    // One node per entry (docs/pantallas.md 22): what it is and says, the sheet as its main action, and
    // what the glyph and dragging do as named actions, so nothing needs a precise touch or a long press.
    val a11y = if (still) {
        Modifier.clearAndSetSemantics { contentDescription = S.entryDescription(entry.bullet, entry.status, entry.signifiers, entry.text) }
    } else {
        Modifier.clearAndSetSemantics {
            contentDescription = S.entryDescription(entry.bullet, entry.status, entry.signifiers, entry.text) +
                (metaText?.let { ". $it" } ?: "")
            onClick(label = S.a11yOptions) { open(); true }
            customActions = buildList {
                if (task && entry.status == TaskStatus.OPEN) add(CustomAccessibilityAction(S.a11yComplete) { glyphTap(); true })
                if (task && entry.status == TaskStatus.DONE) add(CustomAccessibilityAction(S.a11yReopen) { glyphTap(); true })
                if (landedCopy != null) add(CustomAccessibilityAction(S.actionGoToCopy) { onNavigateTo(landedCopy.place); true })
                onMoveUp?.let { add(CustomAccessibilityAction(S.a11yMoveUp) { it(); true }) }
                onMoveDown?.let { add(CustomAccessibilityAction(S.a11yMoveDown) { it(); true }) }
            }
        }
    }
    val gestures = if (still) {
        Modifier
    } else {
        Modifier.pointerInput(entry.id) {
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
                onDragEnd = { if (dragging) onDragFinished() else open() },
                onDragCancel = { if (dragging) onDragFinished() },
            )
        }
    }

    Row(rowModifier.then(a11y).then(gestures)) {
        if (margin) {
            Row(Modifier.widthIn(min = 22.dp).heightIn(min = 40.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                entry.signifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
            }
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).then(if (still) Modifier else Modifier.clickable(role = Role.Button, onClick = glyphTap)),
            contentAlignment = Alignment.Center,
        ) { BulletGlyph(entry.bullet, entry.status) }
        Column(Modifier.weight(1f).then(if (still) Modifier else Modifier.clickable(role = Role.Button, onClick = open))) {
            Text(
                entry.text,
                style = Type.Ink.copy(
                    color = if (closed) muted else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (entry.status == TaskStatus.IRRELEVANT) TextDecoration.LineThrough else TextDecoration.None,
                ),
                modifier = Modifier.padding(start = 2.dp, end = 8.dp, top = 8.dp, bottom = if (metaText != null) 0.dp else 8.dp),
            )
            if (metaText != null) {
                if (landedCopy != null && !still) {
                    // The place is the link, wherever the language puts it in the sentence.
                    val core = wentTo.orEmpty().removePrefix("el ").removePrefix("le ")
                    val at = metaText.indexOf(core).takeIf { it >= 0 } ?: metaText.length
                    val link = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
                    Text(
                        buildAnnotatedString {
                            append(metaText.take(at))
                            withStyle(link) { append(metaText.drop(at)) }
                        },
                        style = Type.Secondary,
                        modifier = Modifier.clickable(role = Role.Button) { onNavigateTo(landedCopy.place) }
                            .padding(start = 2.dp, end = 8.dp, bottom = 8.dp),
                    )
                } else {
                    Text(metaText, style = Type.Secondary, modifier = Modifier.padding(start = 2.dp, end = 8.dp, bottom = 8.dp))
                }
            }
        }
    }
}

/**
 * One place's entries, draggable to reorder (gesture 4, docs/pantallas.md 4): the list makes room row
 * by row as the lifted one passes, and nothing is written until the finger lifts. [entries] is assumed
 * already sorted by [com.baltajmn.bullet.model.ENTRY_ORDER].
 */
@Composable
fun EntryListSection(
    entries: List<Entry>,
    place: Place,
    journal: Journal,
    today: LocalDate,
    onNavigateTo: (Place) -> Unit,
    margin: Boolean = true,
) {
    val ids = entries.map { it.id }
    var order by remember(ids) { mutableStateOf(ids) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { 40.dp.toPx() }
    val byId = entries.associateBy { it.id }

    Column {
        order.forEach { id ->
            val entry = byId[id] ?: return@forEach
            EntryRow(
                entry = entry,
                journal = journal,
                today = today,
                onNavigateTo = onNavigateTo,
                margin = margin,
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
                    if (order != ids) BobbinRepository.reorder(place, order)
                },
                // The accessible alternative to dragging: one place up or down.
                onMoveUp = order.indexOf(id).takeIf { it > 0 }?.let { i -> { BobbinRepository.reorder(place, order.swapped(i, i - 1)) } },
                onMoveDown = order.indexOf(id).takeIf { it < order.lastIndex }?.let { i -> { BobbinRepository.reorder(place, order.swapped(i, i + 1)) } },
            )
        }
    }
}

private fun List<String>.swapped(a: Int, b: Int): List<String> = toMutableList().also { it[a] = this[b]; it[b] = this[a] }

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
 * "Un día de este mes" in the sheet and the composer of Futuro.
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
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.text.isEmpty()) Text(S.dayField, style = Type.Secondary)
                inner()
            }
        },
    )
}

/** A text action in `primary` (docs/pantallas.md 1.3): 48dp tall like every target (22), no background. */
@Composable
fun TextAction(label: String, onClick: () -> Unit) {
    Box(Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary))
    }
}
