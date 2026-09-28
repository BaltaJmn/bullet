package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
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
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.LocalDate

/**
 * One collection (docs/pantallas.md 10.1, #30): the same list and the same capture row as Hoy, in
 * `InCollection(id)`, so writing in a collection feels like writing in the Daily Log because it is the
 * same two components. A tracker (10.2) lands with #50.
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

    val place = Place.InCollection(id)
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }
    var renaming by remember(id) { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
                Spacer(Modifier.weight(1f))
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
private fun CollectionTitleField(initial: String, onSave: (String) -> Unit, modifier: Modifier = Modifier) {
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
        textStyle = Type.PageTitle,
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
