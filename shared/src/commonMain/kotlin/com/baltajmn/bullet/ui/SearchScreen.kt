package com.baltajmn.bullet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.sp
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
import com.baltajmn.bullet.ui.theme.page
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

    val groups = search(journal, query, filters)

    Page(tab = false) {
        BackBar(S.back, onBack)
        SearchField(query, onChange = { query = it }, onClear = { query = "" })

        FlowRow(Modifier.fillMaxWidth().padding(start = HEAD_START, end = 16.dp)) {
            FilterChip(SearchFilter.OPEN, S.filterOpen, filters) { filters = filters.toggle(it) }
            FilterChip(SearchFilter.PRIORITY, S.signifierPriority, filters) { filters = filters.toggle(it) }
            FilterChip(SearchFilter.INSPIRATION, S.signifierInspiration, filters) { filters = filters.toggle(it) }
            FilterChip(SearchFilter.EXPLORE, S.signifierExplore, filters) { filters = filters.toggle(it) }
        }

        when {
            query.isBlank() && filters.isEmpty() -> EmptyText(S.searchEmpty)
            groups.isEmpty() -> EmptyText(S.searchNothing)
        }

        groups.forEach { group ->
            val place = placeOf(group.place)
            Box(Modifier.fillMaxWidth().clickable(role = Role.Button) { onOpenGroup(place) }) {
                Eyebrow(groupLabel(group.place, journal))
            }
            // Every gesture but dragging: reordering makes no sense in a list of results from several
            // pages (docs/pantallas.md 12).
            group.entries.forEach { entry -> EntryRow(entry, journal, today, onNavigateTo) }
        }
    }
}

private fun Set<SearchFilter>.toggle(f: SearchFilter) = if (f in this) this - f else this + f

/** docs/pantallas.md 12: the field under the back button, focused, and the keyboard's search key only lowers it. */
@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, onClear: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp).height(46.dp)
            .fieldFrame(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline).padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(S.searchHint, style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            BasicTextField(
                value = query,
                onValueChange = { onChange(it.oneLine()) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                textStyle = Type.Ink.copy(fontSize = 16.sp),
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
