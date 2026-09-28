package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.filterIndex
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.YearMonth

/**
 * The Index (docs/pantallas.md 9, docs/tecnico.md 6.7, #29): the months that have something and the
 * collections, in the order they were started. Never alphabetical, and with no page number anywhere,
 * because this paper has no physical pages to number.
 *
 * The `newTracker` row lands with #50, which is what brings trackers and the Pro gate of 6.18.
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
    var query by remember { mutableStateOf("") }
    // Folded again on every visit (docs/pantallas.md 9: "Se pliega al salir").
    var archivedOpen by remember { mutableStateOf(false) }

    val shown = filterIndex(items, query)
    val (archived, active) = shown.partition { it is IndexItem.Collection && it.archived }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper()) {
            TabHeaderIcons(onSearch, onSettings)
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.tabIndex, style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp))
            }
            Spacer(Modifier.height(gridUnit))

            // Nothing to filter in an empty diary, so no field to ignore either.
            if (items.isNotEmpty()) {
                FilterField(query, onChange = { query = it }, onClear = { query = "" })
                Spacer(Modifier.height(gridUnit))
            }

            when {
                items.isEmpty() -> Text(S.indexEmpty, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
                shown.isEmpty() -> Text(S.indexNoMatch, style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
            }

            active.forEach { item -> IndexRow(item) { open(item, onOpenMonth, onOpenCollection) } }

            // Only the title, and Intro creates it and opens it with its capture focused
            // (docs/pantallas.md 9). Hidden while filtering, like the archived block.
            if (query.isEmpty()) {
                NewCollectionRow { title ->
                    BobbinRepository.createCollection(title)?.let(onCreated)
                }
            }

            if (archived.isNotEmpty()) {
                Spacer(Modifier.height(gridUnit))
                Row(
                    Modifier.fillMaxWidth().height(gridUnit * 2)
                        .clickable(role = Role.Button) { archivedOpen = !archivedOpen },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        S.archivedToggle(archived.size),
                        style = Type.Body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(start = 48.dp),
                    )
                }
                if (archivedOpen) {
                    archived.forEach { item ->
                        IndexRow(item, dim = true) { open(item, onOpenMonth, onOpenCollection) }
                    }
                }
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
    }
}

private fun open(item: IndexItem, onOpenMonth: (YearMonth) -> Unit, onOpenCollection: (String) -> Unit) = when (item) {
    is IndexItem.Month -> onOpenMonth(item.month)
    is IndexItem.Collection -> onOpenCollection(item.id)
}

/** docs/pantallas.md 9: the capture row that creates a collection. It asks for a title and nothing else. */
@Composable
private fun NewCollectionRow(onCreate: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).padding(start = 48.dp, end = 24.dp)) {
            if (value.isEmpty()) {
                Text(S.newCollection, style = Type.Ink.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            BasicTextField(
                value = value,
                onValueChange = { value = it.oneLine() },
                modifier = Modifier.fillMaxWidth(),
                textStyle = Type.Ink,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCreate(value); value = "" }),
            )
        }
    }
}

/** docs/pantallas.md 9: the filter, with `CLOSE` on the right once it has something to clear. */
@Composable
private fun FilterField(query: String, onChange: (String) -> Unit, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).padding(start = 48.dp)) {
            if (query.isEmpty()) {
                Text(S.indexFilterHint, style = Type.Ink.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            BasicTextField(
                value = query,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = Type.Ink,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true,
            )
        }
        if (query.isNotEmpty()) GlyphButton(Glyph.CLOSE, S.close, onClear)
    }
}

/**
 * One row of the Index (docs/pantallas.md 9): the title from x48, a dotted leader, and what kind of page
 * it is on the right. The leader and the label sit on the bottom of the title, so a title that wraps
 * keeps them on its last line instead of floating between the two.
 */
@Composable
private fun IndexRow(item: IndexItem, dim: Boolean = false, onClick: () -> Unit) {
    val dots = MaterialTheme.colorScheme.onSurfaceVariant
    val title = if (dim) Type.Ink.copy(color = dots) else Type.Ink

    Row(
        Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 48.dp, end = 24.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(item.title, style = title, modifier = Modifier.weight(1f, fill = false))
        Box(
            Modifier.weight(1f).height(gridUnit).padding(horizontal = 8.dp).drawBehind {
                // 1.5dp circles every 6dp, on the line the title ends on.
                val radius = 1.5.dp.toPx() / 2
                val step = 6.dp.toPx()
                val y = size.height - 8.dp.toPx()
                var x = radius
                while (x < size.width) {
                    drawCircle(dots, radius = radius, center = Offset(x, y))
                    x += step
                }
            },
        )
        Text(kindLabel(item), style = Type.Secondary, modifier = Modifier.padding(bottom = 6.dp))
    }
}

private fun kindLabel(item: IndexItem): String = when (item) {
    is IndexItem.Month -> S.indexMonth
    is IndexItem.Collection -> if (item.kind == CollectionKind.TRACKER) S.indexTracker else S.indexCollection
}
