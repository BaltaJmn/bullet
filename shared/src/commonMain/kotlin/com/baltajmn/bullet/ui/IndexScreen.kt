package com.baltajmn.bullet.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import com.baltajmn.bullet.model.groupByTheme
import com.baltajmn.bullet.model.monthMigrated
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.COLLECTION_TITLE_MAX
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.canCreateTracker
import com.baltajmn.bullet.model.clampCodePoints
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activeCover
import com.baltajmn.bullet.ui.theme.coverSoft
import kotlinx.datetime.YearMonth

/**
 * The Index (docs/pantallas.md 9, docs/tecnico.md 6.7): the months that have something, the lists and
 * the trackers, each group in the order it was started. Never alphabetical, and with no page number
 * anywhere, because this paper has no physical pages to number.
 */
@Composable
fun IndexScreen(
    today: LocalDate,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onOpenMonth: (YearMonth) -> Unit,
    onOpenCollection: (String) -> Unit,
    onCreated: (String) -> Unit,
) {
    val journal = BobbinRepository.journal
    val all = journal.indexItems(S::monthTitle)
    // The themes in the order they first appear; with none, the Index is exactly v1.0's (12.9).
    val themes = all.mapNotNull { it.theme }.distinct()
    var themeFilter by remember { mutableStateOf<String?>(null) }
    if (themeFilter != null && themeFilter !in themes) themeFilter = null
    val items = themeFilter?.let { t -> all.filter { it.theme == t } } ?: all
    val months = items.filterIsInstance<IndexItem.Month>()
    val collections = items.filterIsInstance<IndexItem.Collection>()
    val (archived, active) = collections.partition { it.archived }
    // Folded again on every visit (docs/pantallas.md 9: "Se pliega al salir").
    var archivedOpen by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf<CollectionKind?>(null) }
    var marking by remember { mutableStateOf<IndexItem.Month?>(null) }

    Page(tab = true) {
        TopBar(onSearch, onSettings)
        PageHead(S.tabIndex, S.indexSubtitle)
        if (themes.isNotEmpty()) {
            FlowRow(Modifier.fillMaxWidth().padding(start = HEAD_START, end = 16.dp, top = 4.dp)) {
                themes.forEach { t -> ThemeChip(t, on = t == themeFilter) { themeFilter = if (t == themeFilter) null else t } }
            }
        }

        Eyebrow(S.indexMonths)
        Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
            if (months.isEmpty()) Text(S.indexEmpty, style = Type.Secondary, modifier = Modifier.padding(start = 6.dp, top = 4.dp))
            months.forEach {
                IndexRow(
                    Glyph.MONTH,
                    it.title,
                    mark = it.icon,
                    underline = journal.monthMigrated(it.month, today),
                    onLongClick = { marking = it },
                ) { onOpenMonth(it.month) }
            }
        }

        Eyebrow(S.indexLists, S.indexListsHint)
        Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
            ByTheme(active.filter { it.kind != CollectionKind.TRACKER }, grouped = themes.isNotEmpty() && themeFilter == null) {
                IndexRow(Glyph.LIST, it.title, mark = it.icon, indent = it.threaded) { onOpenCollection(it.id) }
            }
            if (adding == CollectionKind.NOTES) {
                NameField(S.listNameHint) { title ->
                    val made = BobbinRepository.createCollection(title) ?: return@NameField false
                    adding = null
                    Flash.show(S.listCreated)
                    onCreated(made)
                    true
                }
            } else {
                AddRow(S.newList) { adding = CollectionKind.NOTES }
            }
        }

        Eyebrow(S.indexTrackers, S.indexTrackersHint)
        Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
            ByTheme(active.filter { it.kind == CollectionKind.TRACKER }, grouped = themes.isNotEmpty() && themeFilter == null) {
                IndexRow(Glyph.GRID, it.title, mark = it.icon) { onOpenCollection(it.id) }
            }
            // Without Pro and with one tracker already, the row is the door to Pro and not a field:
            // nothing is typed that could then not be kept (docs/tecnico.md 6.18).
            val locked = !canCreateTracker(journal, BobbinRepository.isPro)
            if (adding == CollectionKind.TRACKER && !locked) {
                NameField(S.trackerNameHint) { title ->
                    val made = BobbinRepository.createCollection(title, CollectionKind.TRACKER) ?: return@NameField false
                    adding = null
                    onCreated(made)
                    true
                }
            } else {
                AddRow(S.newTracker, pro = locked) {
                    if (locked) Paywall.show { adding = CollectionKind.TRACKER } else adding = CollectionKind.TRACKER
                }
            }
        }

        if (archived.isNotEmpty()) {
            Box(Modifier.padding(start = HEAD_START - 6.dp, top = 16.dp)) {
                QuietButton(S.archivedToggle(archived.size), { archivedOpen = !archivedOpen })
            }
            if (archivedOpen) {
                Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
                    archived.forEach {
                        IndexRow(if (it.kind == CollectionKind.TRACKER) Glyph.GRID else Glyph.LIST, it.title, mark = it.icon, dim = true, indent = it.threaded) { onOpenCollection(it.id) }
                    }
                }
            }
        }
    }

    marking?.let { m ->
        IconThemeSheet(
            title = m.title,
            icon = m.icon,
            theme = m.theme,
            onSave = { icon, theme -> BobbinRepository.markMonth(m.month, icon, theme) },
            onClose = { marking = null },
        )
    }
}

/**
 * docs/pantallas.md 23.9: with themes, a block of the Index is grouped under an `Eyebrow` per theme and
 * the pages without one last, each group in creation order ([groupByTheme]); without, it is one list.
 */
@Composable
private fun ByTheme(items: List<IndexItem.Collection>, grouped: Boolean, row: @Composable (IndexItem.Collection) -> Unit) {
    if (!grouped) {
        items.forEach { row(it) }
        return
    }
    groupByTheme(items).forEach { (theme, group) ->
        Text(
            (theme ?: S.themeNone).uppercase(),
            style = Type.Eyebrow.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 2.dp).semantics { heading() },
        )
        group.forEach { row(it as IndexItem.Collection) }
    }
}

/** One theme of the filter (docs/pantallas.md 23.9), as Buscar's: 48 tall, a 2dp mark under it when on. */
@Composable
private fun ThemeChip(theme: String, on: Boolean, onClick: () -> Unit) {
    val ink = if (on) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        Modifier.heightIn(min = 48.dp).semantics { selected = on }.clickable(role = Role.Button, onClick = onClick).padding(start = 6.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            theme,
            style = Type.Body.copy(color = ink),
            modifier = if (on) Modifier.drawBehind {
                val y = size.height + 4.dp.toPx()
                drawLine(ink, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
            } else Modifier,
        )
    }
}

/**
 * One page of the Index (docs/pantallas.md 9): what kind it is as an icon (or the mark chosen for it,
 * 23.9), its title, and a chevron. A past month migrated through is underlined, as on paper.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IndexRow(
    icon: Glyph,
    title: String,
    mark: String? = null,
    dim: Boolean = false,
    indent: Boolean = false,
    underline: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val line = MaterialTheme.colorScheme.outline
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            // A thread's later lists sit 24 under the first one (docs/pantallas.md 23.1).
            .padding(start = if (indent) 24.dp else 0.dp)
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .combinedClickable(role = Role.Button, onLongClickLabel = onLongClick?.let { S.iconAndTheme }, onLongClick = onLongClick, onClick = onClick)
            .padding(start = 2.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark != null) IndexIcon(mark) else GlyphIcon(icon)
        Spacer(Modifier.width(12.dp))
        var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
        Text(
            title,
            style = Type.Ink.copy(color = if (dim) muted else MaterialTheme.colorScheme.onBackground),
            onTextLayout = { layout = it },
            modifier = Modifier.weight(1f).padding(vertical = 12.dp).then(
                if (underline) Modifier.drawBehind {
                    // 1dp, 3dp under the baseline of every line of the title (docs/pantallas.md 23.9).
                    val l = layout ?: return@drawBehind
                    for (i in 0 until l.lineCount) {
                        val y = l.getLineBaseline(i) + 3.dp.toPx()
                        drawLine(muted, Offset(l.getLineLeft(i), y), Offset(l.getLineRight(i), y), 1.dp.toPx())
                    }
                } else Modifier,
            ),
        )
        GlyphIcon(Glyph.FORWARD, size = 18.dp)
    }
}

/** "Nueva lista", "Nuevo seguimiento": the row that asks for a name, with PRO beside it when it is the door to Pro. */
@Composable
private fun AddRow(label: String, pro: Boolean = false, onClick: () -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 2.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.PLUS, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary), modifier = Modifier.weight(1f))
        if (pro) ProChip()
    }
}

@Composable
private fun ProChip() {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(coverSoft(activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro)))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text("PRO", style = Type.Label.copy(fontSize = 10.sp, lineHeight = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp))
    }
}

/**
 * The name of a new list, tracker or tracker row, and nothing else (docs/pantallas.md 9). Focused on
 * arrival; [onCreate] returning true empties it for the next one.
 */
@Composable
internal fun NameField(hint: String, action: String = S.create, onCreate: (String) -> Boolean) {
    var value by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val line = MaterialTheme.colorScheme.outline
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .drawBehind { drawLine(line, Offset(0f, size.height + 8.dp.toPx()), Offset(size.width, size.height + 8.dp.toPx()), 1.dp.toPx()) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.weight(1f).height(42.dp).fieldFrame(MaterialTheme.colorScheme.surface, line, 12.dp).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) Text(hint, style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            BasicTextField(
                value = value,
                onValueChange = { value = it.oneLine().clampCodePoints(COLLECTION_TITLE_MAX) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = Type.Ink.copy(fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (onCreate(value)) value = "" }),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.height(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary)
                .clickable(role = Role.Button) { if (onCreate(value)) value = "" }.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) { Text(action, style = Type.Label.copy(color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)) }
    }
}
