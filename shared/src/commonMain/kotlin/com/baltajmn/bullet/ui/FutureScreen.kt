package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.FUTURE_MONTHS
import com.baltajmn.bullet.model.FUTURE_MONTHS_MAX
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.futureBlock
import com.baltajmn.bullet.model.placeDay
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.Spread
import com.baltajmn.bullet.ui.theme.isWideScreen
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * The Future Log (docs/pantallas.md 8, docs/tecnico.md 6.5, #25): a block per month starting the one
 * after this one, [FUTURE_MONTHS] at a time up to [FUTURE_MONTHS_MAX]. Nothing on this page moves or
 * notifies on its own: an entry leaves the Future Log only because someone migrated it by hand, one
 * entry at a time (SPEC 2). [shown] lives in `App` so following a scheduled task's link can widen the
 * view enough to reach the month it landed in.
 */
@Composable
fun FutureScreen(
    today: LocalDate,
    shown: Int,
    onShownChange: (Int) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onNavigateTo: (Place) -> Unit,
    linkTo: Place?,
    onLinkHandled: () -> Unit,
) {
    val journal = BobbinRepository.journal
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper().page(spread = true)) {
            TabHeaderIcons(onSearch, onSettings)
            // No subtitle: the Future Log is not a month (docs/pantallas.md 8).
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.tabFuture, style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp))
            }
            Spacer(Modifier.height(gridUnit))

            val block: @Composable ColumnScope.(YearMonth) -> Unit = { month ->
                Text(S.monthTitle(month).uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
                FutureBlock(
                    month = month,
                    journal = journal,
                    editingId = editingId,
                    onStartEdit = { editingId = it },
                    onSaveEdit = { id, text -> BobbinRepository.editText(id, text); editingId = null },
                    onLongPress = { sheetEntryId = it },
                    onNavigateTo = onNavigateTo,
                    linkTo = linkTo,
                    onLinkHandled = onLinkHandled,
                )
                Spacer(Modifier.height(gridUnit))
            }
            val months = futureMonths(today, shown)
            if (isWideScreen()) {
                // Alternating left and right, in order, like the Future Log of a paper notebook
                // opened flat (docs/pantallas.md 21).
                Spread(
                    left = { months.filterIndexed { i, _ -> i % 2 == 0 }.forEach { block(it) } },
                    right = { months.filterIndexed { i, _ -> i % 2 == 1 }.forEach { block(it) } },
                )
            } else {
                months.forEach { block(it) }
            }

            if (shown < FUTURE_MONTHS_MAX) {
                Box(Modifier.padding(start = 40.dp)) {
                    TextAction(S.showMoreMonths) { onShownChange(minOf(shown + FUTURE_MONTHS, FUTURE_MONTHS_MAX)) }
                }
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

/**
 * One month's block (docs/pantallas.md 8): the entries with a day, each showing its own day in the
 * date column, then the ones without, then the block's capture row, whose date column is the numeric
 * day field. A day that the month does not have shows `dayOutOfRange` and creates nothing.
 */
@Composable
private fun FutureBlock(
    month: YearMonth,
    journal: Journal,
    editingId: String?,
    onStartEdit: (String) -> Unit,
    onSaveEdit: (String, String) -> Unit,
    onLongPress: (String) -> Unit,
    onNavigateTo: (Place) -> Unit,
    linkTo: Place?,
    onLinkHandled: () -> Unit,
) {
    var dayText by remember(month) { mutableStateOf("") }
    var error by remember(month) { mutableStateOf<String?>(null) }
    val block = journal.futureBlock(month)
    val (dated, undated) = block.partition { it.placeDay != null }

    // Their order is the calendar's, so these rows are not draggable: no EntryListSection here.
    dated.forEach { entry ->
        val day = entry.placeDay ?: return@forEach
        DatedRow(
            modifier = scrollHereWhen(linkTo == entry.place, onLinkHandled),
            date = { DayNumber(LocalDate(month.year, month.month, day)) },
        ) {
            EntryRow(
                entry = entry,
                journal = journal,
                isEditing = entry.id == editingId,
                onStartEdit = { onStartEdit(entry.id) },
                onSaveEdit = { text -> onSaveEdit(entry.id, text) },
                onLongPress = { onLongPress(entry.id) },
                onToggleDone = { BobbinRepository.toggleDone(entry.id) },
                onNavigateTo = onNavigateTo,
            )
        }
    }

    val undatedPlace = Place.Future(month)
    DatedRow(modifier = scrollHereWhen(linkTo == undatedPlace, onLinkHandled)) {
        EntryListSection(
            entries = undated,
            place = undatedPlace,
            journal = journal,
            editingId = editingId,
            onStartEdit = onStartEdit,
            onSaveEdit = onSaveEdit,
            onLongPress = onLongPress,
            onToggleDone = { BobbinRepository.toggleDone(it) },
            onNavigateTo = onNavigateTo,
            onReorder = BobbinRepository::reorder,
        )
    }

    DatedRow(
        date = {
            Box(Modifier.height(gridUnit * 2), contentAlignment = Alignment.CenterStart) {
                // The field is the date column itself, 48 wide (docs/pantallas.md 8).
                DayField(dayText, modifier = Modifier.width(48.dp).padding(start = 4.dp)) { dayText = it; error = null }
            }
        },
    ) {
        val day = dayText.toIntOrNull()
        CaptureRow(
            place = Place.Future(month, day),
            // Keyed by the month, not the place: typing a day must not wipe the text already there.
            dayKey = month,
            autoFocus = false,
            beforeSave = {
                error = day?.takeIf { it !in 1..monthDays(month) }?.let { S.dayOutOfRange(month, it) }
                error == null
            },
        )
        error?.let { Text(it, style = Type.Secondary) }
    }
}
