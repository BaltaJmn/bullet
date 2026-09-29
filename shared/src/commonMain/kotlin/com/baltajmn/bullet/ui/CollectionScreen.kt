package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.collectionShare
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.COLLECTION_TITLE_MAX
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.trackerPage
import com.baltajmn.bullet.model.trackerThread
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activeCover
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * One list (docs/pantallas.md 10.1): the same rows and the same composer as Hoy, in `InCollection(id)`,
 * so writing in a list feels like writing in the Daily Log because it is the same two components. A
 * tracker (10.2) is [TrackerScreen], in the same place of the stack.
 *
 * [justCreated] is true when the Index made this one a moment ago, which is the only time the page
 * opens with the keyboard up (10.1).
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
    val entries = journal.entriesAt(place)
    var sharing by remember { mutableStateOf<ShareContent?>(null) }
    var renaming by remember(id) { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Page(
        tab = false,
        scroll = scroll,
        bottom = {
            Composer(
                place = place,
                today = today,
                list = collection.title,
                autoFocus = justCreated,
                onSaved = { scope.launch { delay(60); scroll.animateScrollTo(scroll.maxValue) } },
            )
        },
    ) {
        BackBar(S.tabIndex, onBack) { GlyphButton(Glyph.MORE, S.a11yMoreActions, { moreOpen = true }) }
        if (renaming) {
            TitleField(
                collection.title,
                onSave = { BobbinRepository.renameCollection(id, it); renaming = false },
                modifier = Modifier.padding(start = HEAD_START, end = 12.dp, top = 6.dp).fillMaxWidth(),
            )
        } else {
            // Touching the title renames it, as the sheet's "Cambiar el nombre" does.
            PageHead(
                collection.title,
                subtitle = if (collection.archived) "${S.listSubtitle}. ${S.archivedNote}" else S.listSubtitle,
                titleModifier = Modifier.clickable(role = Role.Button) { renaming = true },
            )
        }
        Spacer(Modifier.height(10.dp))
        if (entries.isEmpty()) EmptyText(S.listEmpty) else EntryListSection(entries, place, journal, today, onNavigateTo)
    }

    if (moreOpen) {
        MoreSheet(
            kind = S.listSubtitle,
            title = collection.title,
            archived = collection.archived,
            onClose = { moreOpen = false },
            onShare = if (entries.isNotEmpty()) ({ moreOpen = false; sharing = collectionShare(journal, id) }) else null,
            onRename = { moreOpen = false; renaming = true },
            onArchive = { BobbinRepository.archiveCollection(id, !collection.archived); moreOpen = false; onBack() },
            onDelete = { BobbinRepository.deleteCollection(id); moreOpen = false },
        )
    }
    sharing?.let { ShareSheet(it, onClose = { sharing = null }) }
}

/** docs/pantallas.md 10.1: renaming in line, capped at `COLLECTION_TITLE_MAX`. An empty title does not save. */
@Composable
private fun TitleField(initial: String, onSave: (String) -> Unit, modifier: Modifier = Modifier, style: TextStyle = Type.PageTitle) {
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
 * The `MORE` sheet of a list or a tracker (docs/pantallas.md 10.1): share, rename, archive and delete.
 * None asks for confirmation; delete goes back to the Index with the undo line (5.8).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreSheet(
    kind: String,
    title: String,
    archived: Boolean,
    onClose: () -> Unit,
    onShare: (() -> Unit)?,
    onRename: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        SheetHeader(kind, title, onClose)
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).padding(bottom = 18.dp)) {
            onShare?.let { ActionRow(S.shareList, icon = { GlyphIcon(Glyph.SHARE) }, onClick = it) }
            ActionRow(S.rename, icon = { GlyphIcon(Glyph.EDIT) }, onClick = onRename)
            ActionRow(if (archived) S.unarchive else S.archive, icon = { GlyphIcon(Glyph.BOOK) }, onClick = onArchive)
            ActionRow(S.deleteCollection, S.deleteHow, icon = { GlyphIcon(Glyph.TRASH) }, onClick = onDelete)
        }
    }
}

/**
 * A tracker (docs/pantallas.md 10.2, docs/tecnico.md 6.18): one page per month of the same thread, a row
 * per habit and a box per day, the whole month in sight and scrolled to today. It opens on this month's
 * page, which is the blank of [trackerPage] until something is marked or a row changes. No share here: a
 * shared page is made of entries (17), and the tracker's table goes out with the export (16.2).
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

    var renaming by remember(id) { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    var sheetRowId by remember { mutableStateOf<String?>(null) }
    var renamingRowId by remember { mutableStateOf<String?>(null) }

    Page(tab = false) {
        BackBar(S.tabIndex, onBack) { GlyphButton(Glyph.MORE, S.a11yMoreActions, { moreOpen = true }) }
        if (renaming) {
            TitleField(
                page.title,
                onSave = { BobbinRepository.renameTracker(tail.id, it); renaming = false },
                modifier = Modifier.padding(start = HEAD_START, end = 12.dp, top = 6.dp).fillMaxWidth(),
            )
        } else {
            PageHead(
                page.title,
                subtitle = S.trackerSubtitle(month) + if (tail.archived) ". ${S.archivedNote}" else "",
                explain = S.trackerExplain,
                titleModifier = Modifier.clickable(role = Role.Button) { renaming = true },
            ) {
                // The thread's pages: back and forward only where there is one (docs/pantallas.md 10.2).
                if (previous != null) GlyphButton(Glyph.BACK, S.a11yPreviousMonth, { month = previous })
                if (next != null) GlyphButton(Glyph.FORWARD, S.a11yNextMonth, { month = next })
            }
        }

        TrackerGrid(page, today, renamingRowId, onOpenRow = { sheetRowId = it }) { rowId, title ->
            BobbinRepository.renameTrackerRow(page, rowId, title)
            renamingRowId = null
        }
        Column(Modifier.padding(start = HEAD_START, end = 12.dp, top = 8.dp)) {
            var adding by remember(page.id) { mutableStateOf(justCreated) }
            if (adding) {
                NameField(S.trackerRowHint, S.add) { BobbinRepository.addTrackerRow(page, it) }
            } else {
                QuietButton("+ ${S.trackerRowHint}", { adding = true }, Modifier.padding(start = 0.dp))
            }
        }
    }

    if (moreOpen) {
        MoreSheet(
            kind = S.trackerSubtitle(month),
            title = page.title,
            archived = tail.archived,
            onClose = { moreOpen = false },
            onShare = null,
            onRename = { moreOpen = false; renaming = true },
            onArchive = { BobbinRepository.archiveCollection(tail.id, !tail.archived); moreOpen = false; onBack() },
            onDelete = { BobbinRepository.deleteTracker(tail.id); moreOpen = false },
        )
    }
    sheetRowId?.let { rowId ->
        val index = page.rows.indexOfFirst { it.id == rowId }
        val row = page.rows.getOrNull(index)
        if (row == null) {
            sheetRowId = null
        } else {
            TrackerRowSheet(
                title = row.title,
                canUp = index > 0,
                canDown = index < page.rows.lastIndex,
                onClose = { sheetRowId = null },
                onRename = { renamingRowId = rowId; sheetRowId = null },
                onUp = { BobbinRepository.moveTrackerRow(page, rowId, index - 1); sheetRowId = null },
                onDown = { BobbinRepository.moveTrackerRow(page, rowId, index + 1); sheetRowId = null },
                onDelete = { BobbinRepository.deleteTrackerRow(page, rowId); sheetRowId = null },
            )
        }
    }
}

private val CELL = 28.dp
private val CELL_GAP = 4.dp
private val ROW_HEIGHT = 36.dp

/**
 * The month as a table (docs/pantallas.md 10.2): the rows' names stay put on the left while the days
 * scroll, today's column is outlined, and a box is one tap. A row's name opens its sheet.
 */
@Composable
private fun TrackerGrid(
    page: BulletCollection,
    today: LocalDate,
    renamingRowId: String?,
    onOpenRow: (String) -> Unit,
    onRenamed: (String, String) -> Unit,
) {
    val month = page.month ?: monthOf(today)
    val days = monthDays(month)
    val todayDay = if (monthOf(today) == month) today.day else null
    val scroll = rememberScrollState()
    val step = with(LocalDensity.current) { (CELL + CELL_GAP).toPx() }
    LaunchedEffect(page.id) { todayDay?.let { scroll.scrollTo(((it - 5).coerceAtLeast(0) * step).toInt()) } }
    val ink = MaterialTheme.colorScheme.onBackground
    val line = MaterialTheme.colorScheme.outline
    val cover = activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro).color

    Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 8.dp)) {
        Column(Modifier.widthIn(max = 150.dp)) {
            Spacer(Modifier.height(ROW_HEIGHT))
            page.rows.forEach { row ->
                Box(
                    Modifier.height(ROW_HEIGHT).clip(RoundedCornerShape(8.dp))
                        .semantics { contentDescription = "${row.title}, ${S.a11yTrackerMarked(row.days.sorted())}" }
                        .clickable(role = Role.Button) { onOpenRow(row.id) }
                        .padding(start = 6.dp, end = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (row.id == renamingRowId) {
                        TitleField(row.title, onSave = { onRenamed(row.id, it) }, style = Type.Ink.copy(fontSize = 15.sp), modifier = Modifier.width(140.dp))
                    } else {
                        Text(row.title, style = Type.Ink.copy(fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Column(Modifier.weight(1f).horizontalScroll(scroll)) {
            Row(Modifier.height(ROW_HEIGHT), horizontalArrangement = Arrangement.spacedBy(CELL_GAP), verticalAlignment = Alignment.CenterVertically) {
                for (d in 1..days) {
                    Text(
                        "$d",
                        style = Type.Secondary.copy(fontSize = 11.sp, fontFeatureSettings = "tnum", color = if (d == todayDay) ink else MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(CELL),
                    )
                }
            }
            page.rows.forEach { row ->
                Row(Modifier.height(ROW_HEIGHT), horizontalArrangement = Arrangement.spacedBy(CELL_GAP), verticalAlignment = Alignment.CenterVertically) {
                    for (d in 1..days) {
                        val on = d in row.days
                        val date = LocalDate(month.year, month.month, d)
                        Box(
                            Modifier.size(CELL).clip(RoundedCornerShape(8.dp))
                                .then(if (on) Modifier.background(cover) else Modifier)
                                .border(if (d == todayDay) 1.5.dp else 1.dp, if (d == todayDay) ink else if (on) cover else line, RoundedCornerShape(8.dp))
                                .semantics {
                                    contentDescription = S.a11yTrackerCell(row.title, date, on)
                                    toggleableState = if (on) ToggleableState.On else ToggleableState.Off
                                }
                                .clickable(role = Role.Checkbox) { BobbinRepository.toggleTrackerDay(page, row.id, d) },
                        )
                    }
                }
            }
        }
    }
}

/** A tracker row's sheet (docs/pantallas.md 10.2): rename, move up or down, or delete with the undo line. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackerRowSheet(
    title: String,
    canUp: Boolean,
    canDown: Boolean,
    onClose: () -> Unit,
    onRename: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        SheetHeader(S.indexTrackers, title, onClose)
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).padding(bottom = 18.dp)) {
            ActionRow(S.rename, icon = { GlyphIcon(Glyph.EDIT) }, onClick = onRename)
            if (canUp) ActionRow(S.a11yMoveUp, icon = { GlyphIcon(Glyph.BACK) }, onClick = onUp)
            if (canDown) ActionRow(S.a11yMoveDown, icon = { GlyphIcon(Glyph.FORWARD) }, onClick = onDown)
            ActionRow(S.rowDelete, S.deleteHow, icon = { GlyphIcon(Glyph.TRASH) }, onClick = onDelete)
        }
    }
}
