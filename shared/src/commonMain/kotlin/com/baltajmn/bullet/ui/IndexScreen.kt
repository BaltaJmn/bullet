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
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onOpenMonth: (YearMonth) -> Unit,
    onOpenCollection: (String) -> Unit,
    onCreated: (String) -> Unit,
) {
    val journal = BobbinRepository.journal
    val items = journal.indexItems(S::monthTitle)
    val months = items.filterIsInstance<IndexItem.Month>()
    val collections = items.filterIsInstance<IndexItem.Collection>()
    val (archived, active) = collections.partition { it.archived }
    // Folded again on every visit (docs/pantallas.md 9: "Se pliega al salir").
    var archivedOpen by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf<CollectionKind?>(null) }

    Page(tab = true) {
        TopBar(onSearch, onSettings)
        PageHead(S.tabIndex, S.indexSubtitle)

        Eyebrow(S.indexMonths)
        Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
            if (months.isEmpty()) Text(S.indexEmpty, style = Type.Secondary, modifier = Modifier.padding(start = 6.dp, top = 4.dp))
            months.forEach { IndexRow(Glyph.MONTH, it.title) { onOpenMonth(it.month) } }
        }

        Eyebrow(S.indexLists, S.indexListsHint)
        Column(Modifier.padding(start = HEAD_START, end = 12.dp)) {
            active.filter { it.kind != CollectionKind.TRACKER }.forEach { IndexRow(Glyph.LIST, it.title) { onOpenCollection(it.id) } }
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
            active.filter { it.kind == CollectionKind.TRACKER }.forEach { IndexRow(Glyph.GRID, it.title) { onOpenCollection(it.id) } }
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
                        IndexRow(if (it.kind == CollectionKind.TRACKER) Glyph.GRID else Glyph.LIST, it.title, dim = true) { onOpenCollection(it.id) }
                    }
                }
            }
        }
    }
}

/** One page of the Index (docs/pantallas.md 9): what kind it is as an icon, its title, and a chevron. */
@Composable
private fun IndexRow(icon: Glyph, title: String, dim: Boolean = false, onClick: () -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 2.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(icon)
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = Type.Ink.copy(color = if (dim) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
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
