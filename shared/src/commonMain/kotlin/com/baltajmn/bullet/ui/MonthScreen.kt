package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.futureWaiting
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.monthsWithContent
import com.baltajmn.bullet.model.weekStarts
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The Monthly Log as a list, never a calendar grid (docs/pantallas.md 7, SPEC 2.6, #24): a row per
 * day of [viewedMonth] with its `Monthly(m, día)` entries, then the month's tasks without a day.
 * Nothing here comes from another month: the month starts empty. [onNavigateTo] follows a migrated
 * or scheduled entry's link (5.5) wherever it landed.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MonthScreen(
    today: LocalDate,
    viewedMonth: YearMonth,
    onViewedMonthChange: (YearMonth) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onNavigateTo: (Place) -> Unit,
    onFutureReview: () -> Unit,
    linkTo: Place?,
    onLinkHandled: () -> Unit,
) {
    val journal = BobbinRepository.journal
    val currentMonth = monthOf(today)
    val oldestMonth = (journal.monthsWithContent() + currentMonth).min()
    val weekStartDays = weekStarts(viewedMonth, firstDayOfWeek(journal.settings, systemFirstDayOfWeek()))

    // One entry edits or opens its sheet at a time, as in Hoy (#22, #23); one day at a time has its
    // capture open, and another month closes it (docs/pantallas.md 7.1).
    var editingId by remember { mutableStateOf<String?>(null) }
    var sheetEntryId by remember { mutableStateOf<String?>(null) }
    var capturingDay by remember(viewedMonth) { mutableStateOf<Int?>(null) }
    BackHandler(capturingDay != null) { capturingDay = null }

    // "Desplazado hasta la fila de hoy" (docs/pantallas.md 7.1), on every visit to the current month.
    val todayRow = remember { BringIntoViewRequester() }
    LaunchedEffect(viewedMonth) {
        if (viewedMonth == currentMonth) todayRow.bringIntoView()
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper()) {
            TabHeaderIcons(onSearch, onSettings)
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.monthName(viewedMonth), style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp).weight(1f))
                // An arrow that would not respond is not painted (docs/pantallas.md 2).
                if (viewedMonth > oldestMonth) {
                    GlyphButton(Glyph.BACK, S.a11yPreviousMonth, { onViewedMonthChange(viewedMonth.minus(1, DateTimeUnit.MONTH)) })
                } else {
                    Spacer(Modifier.width(48.dp))
                }
                if (viewedMonth < currentMonth) {
                    GlyphButton(Glyph.FORWARD, S.a11yNextMonth, { onViewedMonthChange(viewedMonth.plus(1, DateTimeUnit.MONTH)) })
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }
            Row(Modifier.fillMaxWidth().height(gridUnit), verticalAlignment = Alignment.CenterVertically) {
                Text(viewedMonth.year.toString(), style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
            }

            // The notice strip of docs/pantallas.md 7.2, only in the current month. The "sin cerrar"
            // line joins it with #27, once unclosedMonth exists.
            if (viewedMonth == currentMonth) {
                val waiting = journal.futureWaiting(today)
                if (waiting.isNotEmpty() && journal.settings.futureSeen != currentMonth) {
                    NoticeLine(S.futureWaiting(waiting.size), action = onFutureReview)
                }
            }

            Spacer(Modifier.height(gridUnit))

            val weekLine = MaterialTheme.colorScheme.outlineVariant
            for (day in 1..monthDays(viewedMonth)) {
                val date = LocalDate(viewedMonth.year, viewedMonth.month, day)
                val isToday = date == today
                val place = Place.Monthly(viewedMonth, day)
                val entries = journal.entriesAt(place)
                val openCapture = { capturingDay = day }

                DatedRow(
                    modifier = if (day != 1 && day in weekStartDays) {
                        Modifier.drawBehind { drawLine(weekLine, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
                    } else {
                        Modifier
                    },
                    date = {
                        Row(
                            Modifier.fillMaxWidth()
                                .then(if (isToday) Modifier.bringIntoViewRequester(todayRow) else Modifier)
                                .then(scrollHereWhen(linkTo == place, onLinkHandled))
                                .clickable(role = Role.Button, onClick = openCapture),
                        ) {
                            DayNumber(date, isToday)
                        }
                    },
                ) {
                    if (entries.isEmpty() && capturingDay != day) {
                        Spacer(Modifier.fillMaxWidth().height(gridUnit * 2).clickable(role = Role.Button, onClick = openCapture))
                    }
                    EntryListSection(
                        entries = entries,
                        place = place,
                        journal = journal,
                        editingId = editingId,
                        onStartEdit = { editingId = it },
                        onSaveEdit = { id, text -> BobbinRepository.editText(id, text); editingId = null },
                        onLongPress = { sheetEntryId = it },
                        onToggleDone = { BobbinRepository.toggleDone(it) },
                        onNavigateTo = onNavigateTo,
                        onReorder = BobbinRepository::reorder,
                    )
                    if (capturingDay == day) CaptureRow(place, dayKey = place)
                }
            }

            Spacer(Modifier.height(gridUnit))
            Text(S.monthTasks.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
            val tasksPlace = Place.Monthly(viewedMonth)
            DatedRow(modifier = scrollHereWhen(linkTo == tasksPlace, onLinkHandled)) {
                EntryListSection(
                    entries = journal.entriesAt(tasksPlace),
                    place = tasksPlace,
                    journal = journal,
                    editingId = editingId,
                    onStartEdit = { editingId = it },
                    onSaveEdit = { id, text -> BobbinRepository.editText(id, text); editingId = null },
                    onLongPress = { sheetEntryId = it },
                    onToggleDone = { BobbinRepository.toggleDone(it) },
                    onNavigateTo = onNavigateTo,
                    onReorder = BobbinRepository::reorder,
                )
                CaptureRow(tasksPlace, dayKey = tasksPlace, autoFocus = false)
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
