package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.trackerPage
import com.baltajmn.bullet.model.trackerThread
import com.baltajmn.bullet.ui.theme.activeCover
import kotlin.math.abs
import kotlinx.datetime.YearMonth
import androidx.compose.foundation.clickable
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
import com.baltajmn.bullet.data.collectionShare
import com.baltajmn.bullet.data.ShareContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.COLLECTION_TITLE_MAX
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.LocalDate

/**
 * One collection (docs/pantallas.md 10.1, #30): the same list and the same capture row as Hoy, in
 * `InCollection(id)`, so writing in a collection feels like writing in the Daily Log because it is the
 * same two components. A tracker (10.2) is [TrackerScreen], in the same place of the stack.
 *
 * [justCreated] is true when the Index made this collection a moment ago, which is the only time the
 * page opens with the keyboard up (10.1).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CollectionScreen(
    id: String,
    today: LocalDate,
    justCreated: Boolean,
    onBack: () -> Unit,
    onNavigateTo: (Place) -> Unit,
) {
    BackHandler(true, onBack)
    val journal = BobbinRepository.journal
    val collection = journal.collections.find { it.id == id }

    // Deleting it from the sheet leaves this page with nothing to show: back to the Index, where the
    // undo line is waiting (docs/pantallas.md 10.1).
    LaunchedEffect(collection == null) { if (collection == null) onBack() }
    if (collection == null) return
    if (collection.kind == CollectionKind.TRACKER) {
        TrackerScreen(id, today, justCreated, onBack)
        return
    }

    val place = Place.InCollection(id)
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf<ShareContent?>(null) }
    var renaming by remember(id) { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper().page()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
                Spacer(Modifier.weight(1f))
                if (journal.entriesAt(place).isNotEmpty()) {
                    GlyphButton(Glyph.SHARE, S.a11yShare, { sharing = collectionShare(journal, id) })
                }
                GlyphButton(Glyph.MORE, S.a11yMoreActions, { moreOpen = true })
            }

            // Tapping the title edits it in line, exactly as tapping an entry's text does (5.4).
            Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                if (renaming) {
                    CollectionTitleField(
                        collection.title,
                        onSave = { BobbinRepository.renameCollection(id, it); renaming = false },
                        modifier = Modifier.padding(start = 48.dp, end = 24.dp).fillMaxWidth(),
                    )
                } else {
                    Text(
                        collection.title,
                        style = Type.PageTitle,
                        modifier = Modifier.padding(start = 48.dp, end = 24.dp)
                            .clickable(role = Role.Button) { renaming = true },
                    )
                }
            }
            if (collection.archived) {
                Text(S.archivedNote, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
            }
            Spacer(Modifier.height(gridUnit))

            EntryListSection(
                entries = journal.entriesAt(place),
                place = place,
                journal = journal,
                editingId = editingId,
                onStartEdit = { editingId = it },
                onSaveEdit = { entryId, text -> BobbinRepository.editText(entryId, text); editingId = null },
                onLongPress = { sheetEntryId = it },
                onToggleDone = { BobbinRepository.toggleDone(it) },
                onNavigateTo = onNavigateTo,
                onReorder = BobbinRepository::reorder,
            )
            CaptureRow(place, dayKey = id, autoFocus = justCreated)
            Spacer(Modifier.height(gridUnit * 2))
        }
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }

    if (moreOpen) {
        CollectionSheet(
            archived = collection.archived,
            onClose = { moreOpen = false },
            onArchive = { BobbinRepository.archiveCollection(id, !collection.archived); moreOpen = false; onBack() },
            onDelete = { BobbinRepository.deleteCollection(id); moreOpen = false },
        )
    }

    sharing?.let { ShareSheet(it, onClose = { sharing = null }) }

    val sheetEntry = sheetEntryId?.let { entryId -> journal.entries.find { it.id == entryId && !it.gone } }
    if (sheetEntryId != null && sheetEntry == null) {
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

/** docs/pantallas.md 10.1: renaming in line, like editing an entry, capped at `COLLECTION_TITLE_MAX`. An empty title does not save. */
@Composable
private fun CollectionTitleField(initial: String, onSave: (String) -> Unit, modifier: Modifier = Modifier, style: TextStyle = Type.PageTitle) {
    var value by remember(initial) { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    BasicTextField(
        value = value,
        onValueChange = { new ->
            val edit = limitEdit(value.text.oneLine(), new.text.oneLine(), new.selection.end, COLLECTION_TITLE_MAX)
            value = TextFieldValue(edit.text, TextRange(edit.cursor))
        },
        modifier = modifier.focusRequester(focus).onFocusChanged { if (!it.isFocused) onSave(value.text) },
        textStyle = style,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSave(value.text) }),
    )
}

/**
 * The `MORE` sheet of docs/pantallas.md 10.1: archive or unarchive, and delete. Neither asks for
 * confirmation; delete goes back to the Index with the undo line (5.8). `continueCollection` (v1.1) and
 * `iconAndTheme` (v1.2) join it later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionSheet(archived: Boolean, onClose: () -> Unit, onArchive: () -> Unit, onDelete: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            SheetRow(glyph = null, label = if (archived) S.unarchive else S.archive, onClick = onArchive)
            SheetRow(glyph = null, label = S.deleteCollection, onClick = onDelete)
            Spacer(Modifier.height(gridUnit))
        }
    }
}

/**
 * A tracker (docs/pantallas.md 10.2, docs/tecnico.md 6.18): one page per month of the same thread, rows
 * the person defines and one tap per day. It opens on this month's page, which is the blank of
 * [trackerPage] until something is marked or a row changes. No SHARE here: a shared page is made of
 * entries (17), and the tracker's table goes out with the export (16.2).
 */
@Composable
private fun TrackerScreen(id: String, today: LocalDate, justCreated: Boolean, onBack: () -> Unit) {
    val journal = BobbinRepository.journal
    val thread = journal.trackerThread(id)
    if (thread.isEmpty()) return
    val tail = thread.last()
    val thisMonth = monthOf(today)
    var month by remember(id) { mutableStateOf(maxOf(thisMonth, tail.month ?: thisMonth)) }
    val page = trackerPage(journal, tail, month)
    val months = thread.mapNotNull { it.month }
    val previous = months.filter { it < month }.maxOrNull()
    val next = (months + thisMonth).filter { it > month }.minOrNull()
    var day by remember(month) { mutableStateOf(if (month == thisMonth) today.day else monthDays(month)) }
    val marked = LocalDate(month.year, month.month, day)

    var renaming by remember(id) { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    var sheetRowId by remember { mutableStateOf<String?>(null) }
    var editingRowId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper().page()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
                Spacer(Modifier.weight(1f))
                GlyphButton(Glyph.MORE, S.a11yMoreActions, { moreOpen = true })
            }
            Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                if (renaming) {
                    CollectionTitleField(
                        page.title,
                        onSave = { BobbinRepository.renameTracker(tail.id, it); renaming = false },
                        modifier = Modifier.padding(start = 48.dp, end = 24.dp).fillMaxWidth(),
                    )
                } else {
                    Text(
                        page.title,
                        style = Type.PageTitle,
                        modifier = Modifier.padding(start = 48.dp, end = 24.dp).clickable(role = Role.Button) { renaming = true },
                    )
                }
            }
            // The thread's pages: back and forward only where there is one (docs/pantallas.md 10.2).
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.monthYear(month), style = Type.Secondary, modifier = Modifier.padding(start = 48.dp).weight(1f))
                if (previous != null) GlyphButton(Glyph.BACK, S.a11yPreviousMonth, { month = previous })
                if (next != null) GlyphButton(Glyph.FORWARD, S.a11yNextMonth, { month = next })
            }
            if (tail.archived) {
                Text(S.archivedNote, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
            }
            Spacer(Modifier.height(gridUnit))

            // The marked day, never outside the page's month; swiping the row moves it too (gesture 3).
            val last = monthDays(month)
            Row(
                Modifier.fillMaxWidth().height(gridUnit * 2).pointerInputHorizontalSwipe(
                    month to day,
                    threshold = 72.dp,
                    onSwipeRight = { if (day > 1) day-- },
                    onSwipeLeft = { if (day < last) day++ },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(36.dp))
                if (day > 1) GlyphButton(Glyph.BACK, S.a11yPreviousDay, { day-- }) else Spacer(Modifier.width(48.dp))
                Text(S.dayTitle(marked), style = Type.Body, modifier = Modifier.padding(horizontal = 8.dp))
                if (day < last) GlyphButton(Glyph.FORWARD, S.a11yNextDay, { day++ })
            }
            Spacer(Modifier.height(gridUnit))

            TrackerRows(
                page = page,
                marked = marked,
                editingRowId = editingRowId,
                onSaveRow = { rowId, title -> BobbinRepository.renameTrackerRow(page, rowId, title); editingRowId = null },
                onLongPress = { sheetRowId = it },
            )
            TrackerRowCapture(key = page.id, autoFocus = justCreated) { BobbinRepository.addTrackerRow(page, it) }
            Spacer(Modifier.height(gridUnit * 2))
        }
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }

    if (moreOpen) {
        CollectionSheet(
            archived = tail.archived,
            onClose = { moreOpen = false },
            onArchive = { BobbinRepository.archiveCollection(tail.id, !tail.archived); moreOpen = false; onBack() },
            onDelete = { BobbinRepository.deleteTracker(tail.id); moreOpen = false },
        )
    }
    sheetRowId?.let { rowId ->
        TrackerRowSheet(
            onClose = { sheetRowId = null },
            onEdit = { editingRowId = rowId; sheetRowId = null },
            onDelete = { BobbinRepository.deleteTrackerRow(page, rowId); sheetRowId = null },
        )
    }
}

/**
 * The rows of a tracker page, 4u each: title and the cell of the marked day, the month's strip, and a
 * blank line. Long press opens the row's sheet; moving after it lifts the row to reorder (gesture 4).
 */
@Composable
private fun TrackerRows(
    page: BulletCollection,
    marked: LocalDate,
    editingRowId: String?,
    onSaveRow: (String, String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    val ids = page.rows.map { it.id }
    var order by remember(ids) { mutableStateOf(ids) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { (gridUnit * 4).toPx() }
    val slopPx = with(LocalDensity.current) { DRAG_SLOP.toPx() }
    val byId = page.rows.associateBy { it.id }
    val ink = MaterialTheme.colorScheme.onBackground
    val faint = MaterialTheme.colorScheme.onSurfaceVariant
    val cover = activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro).color

    Column {
        order.forEach { rowId ->
            val row = byId[rowId] ?: return@forEach
            val isMarked = marked.day in row.days
            val dragged = rowId == draggingId
            Column(
                Modifier.fillMaxWidth()
                    .then(
                        if (dragged) {
                            Modifier.zIndex(1f).graphicsLayer { translationY = dragOffset }
                                .background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline)
                        } else {
                            Modifier
                        },
                    )
                    .pointerInput(rowId, page) {
                        var total = 0f
                        var dragging = false
                        detectDragGesturesAfterLongPress(
                            onDragStart = { total = 0f; dragging = false },
                            onDrag = { change, amount ->
                                change.consume()
                                if (!dragging) {
                                    total += amount.y
                                    if (abs(total) > slopPx) {
                                        dragging = true
                                        draggingId = rowId
                                        dragOffset = total
                                    }
                                } else {
                                    dragOffset += amount.y
                                    val from = order.indexOf(rowId)
                                    var to = from
                                    while (dragOffset - (to - from) * rowHeightPx > rowHeightPx / 2 && to < order.lastIndex) to++
                                    while (dragOffset - (to - from) * rowHeightPx < -rowHeightPx / 2 && to > 0) to--
                                    if (to != from) {
                                        order = order.toMutableList().also { it.removeAt(from); it.add(to, rowId) }
                                        dragOffset -= (to - from) * rowHeightPx
                                    }
                                }
                            },
                            onDragEnd = {
                                if (dragging) {
                                    draggingId = null
                                    dragOffset = 0f
                                    if (order != ids) BobbinRepository.moveTrackerRow(page, rowId, order.indexOf(rowId))
                                } else {
                                    onLongPress(rowId)
                                }
                            },
                            onDragCancel = { draggingId = null; dragOffset = 0f },
                        )
                    },
            ) {
                Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                    if (rowId == editingRowId) {
                        CollectionTitleField(
                            row.title,
                            onSave = { onSaveRow(rowId, it) },
                            modifier = Modifier.padding(start = 48.dp).weight(1f),
                            style = Type.Ink,
                        )
                    } else {
                        // The strip is not a node of its own: the row says which days are marked (docs/pantallas.md 19).
                        Text(
                            row.title,
                            style = Type.Ink,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 48.dp).weight(1f)
                                .semantics { contentDescription = "${row.title}, ${S.a11yTrackerMarked(row.days.sorted())}" },
                        )
                    }
                    Box(
                        Modifier.size(48.dp)
                            .semantics { contentDescription = S.a11yTrackerCell(row.title, marked, isMarked) }
                            .clickable(role = Role.Checkbox) { BobbinRepository.toggleTrackerDay(page, rowId, marked.day) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.size(10.dp)) {
                            if (isMarked) {
                                drawCircle(ink)
                            } else {
                                val stroke = 1.5.dp.toPx()
                                drawCircle(faint, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
                            }
                        }
                    }
                    Spacer(Modifier.width(24.dp))
                }
                val days = monthDays(YearMonth(marked.year, marked.month))
                Canvas(Modifier.padding(start = 48.dp, end = 24.dp).fillMaxWidth().height(gridUnit)) {
                    val cell = minOf(8.dp.toPx(), size.width / days)
                    val y = size.height / 2
                    for (d in 1..days) {
                        val x = cell * (d - 1) + cell / 2
                        if (d in row.days) drawCircle(ink, radius = 2.5.dp.toPx(), center = Offset(x, y))
                        else drawCircle(faint, radius = 1.dp.toPx(), center = Offset(x, y))
                        if (d == marked.day) {
                            val under = y + 5.dp.toPx()
                            drawLine(cover, Offset(x - cell / 2 + 1, under), Offset(x + cell / 2 - 1, under), strokeWidth = 2.dp.toPx())
                        }
                    }
                }
                Spacer(Modifier.height(gridUnit))
            }
        }
    }
}

/** The capture at the end of a tracker: a new row per Intro, no prefixes, and the keyboard stays up. */
@Composable
private fun TrackerRowCapture(key: String, autoFocus: Boolean, onAdd: (String) -> Boolean) {
    var value by remember(key) { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (autoFocus) focus.requestFocus() }
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).padding(start = 48.dp, end = 24.dp)) {
            if (value.isEmpty()) Text(S.trackerRowHint, style = Type.Ink.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            BasicTextField(
                value = value,
                onValueChange = { value = it.oneLine() },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = Type.Ink,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (onAdd(value)) value = "" }),
            )
        }
    }
}

/** Long press on a tracker row (docs/pantallas.md 10.2): edit its name, or delete it with the undo line. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackerRowSheet(onClose: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            SheetRow(glyph = null, label = S.actionEdit, onClick = onEdit)
            SheetRow(glyph = null, label = S.rowDelete, onClick = onDelete)
            Spacer(Modifier.height(gridUnit))
        }
    }
}
