package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.GroupKey
import com.baltajmn.bullet.data.SearchFilter
import com.baltajmn.bullet.data.search
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.LocalDate

/**
 * Search (docs/pantallas.md 12, docs/tecnico.md 6.8, #35): the whole diary, in memory, while you type.
 * A `#tag` is searched by typing it, so there is no tag manager and no screen for tags at all.
 *
 * [onOpenGroup] takes the page a group came from: that is the only thing the results do that the diary
 * itself would not.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    today: LocalDate,
    onBack: () -> Unit,
    onOpenGroup: (Place) -> Unit,
    onNavigateTo: (Place) -> Unit,
) {
    BackHandler(true, onBack)
    val journal = BobbinRepository.journal
    var query by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(emptySet<SearchFilter>()) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }

    val groups = search(journal, query, filters)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper().page()) {
            SearchField(query, onChange = { query = it }, onClear = { query = "" }, onBack = onBack)

            FlowRow(Modifier.fillMaxWidth().padding(start = 48.dp, end = 24.dp)) {
                FilterChip(SearchFilter.OPEN, S.filterOpen, filters) { filters = filters.toggle(it) }
                FilterChip(SearchFilter.PRIORITY, S.signifierPriority, filters) { filters = filters.toggle(it) }
                FilterChip(SearchFilter.INSPIRATION, S.signifierInspiration, filters) { filters = filters.toggle(it) }
                FilterChip(SearchFilter.EXPLORE, S.signifierExplore, filters) { filters = filters.toggle(it) }
            }
            Spacer(Modifier.height(gridUnit))

            when {
                query.isBlank() && filters.isEmpty() ->
                    Text(S.searchEmpty, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp, end = 24.dp))
                groups.isEmpty() ->
                    Text(S.searchNothing, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp, end = 24.dp))
            }

            groups.forEach { group ->
                val place = placeOf(group.place)
                Row(
                    Modifier.fillMaxWidth().height(gridUnit * 2)
                        .clickable(role = Role.Button) { onOpenGroup(place) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(groupLabel(group.place, journal).uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp, end = 24.dp))
                }
                // Every gesture but dragging: reordering makes no sense in a list of results from
                // several pages (docs/pantallas.md 12).
                group.entries.forEach { entry ->
                    EntryRow(
                        entry = entry,
                        journal = journal,
                        isEditing = entry.id == editingId,
                        onStartEdit = { editingId = entry.id },
                        onSaveEdit = { text -> BobbinRepository.editText(entry.id, text); editingId = null },
                        onLongPress = { sheetEntryId = entry.id },
                        onToggleDone = { BobbinRepository.toggleDone(entry.id) },
                        onNavigateTo = onNavigateTo,
                    )
                }
                Spacer(Modifier.height(gridUnit))
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }

    val sheetEntry = sheetEntryId?.let { id -> journal.entries.find { it.id == id && !it.gone } }
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

private fun Set<SearchFilter>.toggle(f: SearchFilter) = if (f in this) this - f else this + f

/** docs/pantallas.md 12: the field sits in the title row, focused, and the keyboard's search key only lowers it. */
@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, onClear: () -> Unit, onBack: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        GlyphButton(Glyph.BACK, S.a11yBack, onBack)
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(S.searchHint, style = Type.Ink.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            BasicTextField(
                value = query,
                onValueChange = { onChange(it.oneLine()) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = Type.Ink,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
        if (query.isNotEmpty()) GlyphButton(Glyph.CLOSE, S.close, onClear)
    }
}

/**
 * One filter (docs/pantallas.md 12): its glyph and its name, 48 tall, with the 2dp mark under it when
 * it is on. They combine with AND.
 */
@Composable
private fun FilterChip(filter: SearchFilter, label: String, active: Set<SearchFilter>, onToggle: (SearchFilter) -> Unit) {
    val on = filter in active
    val ink = if (on) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.heightIn(min = 48.dp).clickable(role = Role.Button) { onToggle(filter) }.padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(Modifier.width(24.dp)) {
            when (filter) {
                SearchFilter.OPEN -> BulletGlyph(Bullet.TASK, TaskStatus.OPEN, tint = ink)
                SearchFilter.PRIORITY -> SignifierGlyph(Signifier.PRIORITY, tint = ink)
                SearchFilter.INSPIRATION -> SignifierGlyph(Signifier.INSPIRATION, tint = ink)
                SearchFilter.EXPLORE -> SignifierGlyph(Signifier.EXPLORE, tint = ink)
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = Type.Body.copy(color = ink),
            modifier = if (on) {
                Modifier.drawBehind {
                    val y = size.height + 4.dp.toPx()
                    drawRect(ink, topLeft = Offset(0f, y), size = Size(size.width, 2.dp.toPx()))
                }
            } else {
                Modifier
            },
        )
    }
}

/** The label of a group (docs/pantallas.md 12), and the page it opens. */
private fun groupLabel(key: GroupKey, journal: Journal): String = when (key) {
    is GroupKey.Day -> S.longDateWithYear(key.date)
    is GroupKey.Month -> S.monthGroup(key.m)
    is GroupKey.FutureMonth -> S.futureGroup(key.m)
    is GroupKey.InCollection -> journal.collections.find { it.id == key.id }?.title.orEmpty()
}

private fun placeOf(key: GroupKey): Place = when (key) {
    is GroupKey.Day -> Place.Daily(key.date)
    is GroupKey.Month -> Place.Monthly(key.m)
    is GroupKey.FutureMonth -> Place.Future(key.m)
    is GroupKey.InCollection -> Place.InCollection(key.id)
}
