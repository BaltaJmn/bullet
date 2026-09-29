package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.ShareContent
import com.baltajmn.bullet.data.monthShare
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.futureWaiting
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.monthsWithContent
import com.baltajmn.bullet.model.openTasksOfMonth
import com.baltajmn.bullet.model.unclosedMonth
import com.baltajmn.bullet.model.weekStarts
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activeCover
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The Monthly Log as a list, never a calendar grid (docs/pantallas.md 7, SPEC 2.6): a row per day of
 * [viewedMonth] with its calendar entries, then the month's tasks without a day. Touching a day makes
 * it where the composer writes, with an event suggested; touching it again goes back to the month's
 * tasks. Nothing here comes from another month: the month starts empty.
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
    onReview: (ReviewScope) -> Unit,
    onFutureReview: () -> Unit,
    linkTo: Place?,
    onLinkHandled: () -> Unit,
) {
    val journal = BobbinRepository.journal
    val currentMonth = monthOf(today)
    val oldestMonth = (journal.monthsWithContent() + currentMonth).min()
    val weekStartDays = weekStarts(viewedMonth, firstDayOfWeek(journal.settings, systemFirstDayOfWeek()))
    var sharing by remember { mutableStateOf<ShareContent?>(null) }
    var picked by remember(viewedMonth) { mutableStateOf<Int?>(null) }
    var pickSignal by remember { mutableStateOf(0) }
    BackHandler(picked != null) { picked = null }

    // "Desplazado hasta la fila de hoy" (docs/pantallas.md 7.1), on every visit to the current month.
    // The asked rect runs below the row, so today lands with the rest of the week under it instead of
    // pressed against the composer.
    val todayRow = remember { BringIntoViewRequester() }
    val below = with(LocalDensity.current) { 320.dp.toPx() }
    LaunchedEffect(viewedMonth) { if (viewedMonth == currentMonth) todayRow.bringIntoView(Rect(0f, 0f, 1f, below)) }

    val pickedPlace = picked?.let { Place.Monthly(viewedMonth, it) }
    Page(
        tab = true,
        bottom = {
            Composer(
                place = pickedPlace ?: Place.Monthly(viewedMonth),
                today = today,
                suggested = if (pickedPlace != null) Bullet.EVENT else Bullet.TASK,
                focusSignal = pickSignal,
            )
        },
    ) {
        TopBar(onSearch, onSettings)
        val past = viewedMonth < currentMonth
        PageHead(
            title = S.monthName(viewedMonth),
            subtitle = S.monthSubtitle(viewedMonth, past),
            explain = S.monthExplain,
            action = if (past) S.backToMonth(currentMonth) to { onViewedMonthChange(currentMonth) } else null,
        ) {
            // An arrow that would not respond is not painted (docs/pantallas.md 2).
            if (viewedMonth > oldestMonth) {
                GlyphButton(Glyph.BACK, S.a11yPreviousMonth, { onViewedMonthChange(viewedMonth.minus(1, DateTimeUnit.MONTH)) })
            } else {
                Spacer(Modifier.width(48.dp))
            }
            if (past) {
                GlyphButton(Glyph.FORWARD, S.a11yNextMonth, { onViewedMonthChange(viewedMonth.plus(1, DateTimeUnit.MONTH)) })
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        // The notices of docs/pantallas.md 7.2, only in the current month.
        if (viewedMonth == currentMonth) {
            journal.unclosedMonth(today)?.let { month ->
                NoticeCard(
                    S.unclosedMonth(month, journal.openTasksOfMonth(month).size),
                    S.unclosedBody,
                    listOf(S.reviewMonth(month) to { onReview(ReviewScope.Month(month)) }),
                )
            }
            val waiting = journal.futureWaiting(today)
            if (waiting.isNotEmpty() && journal.settings.futureSeen != currentMonth) {
                NoticeCard(S.futureWaiting(waiting.size), S.futureWaitingBody, listOf(S.reviewFuture to onFutureReview))
            }
        }

        Eyebrow(S.calendarTitle, S.calendarHint)
        val weekLine = MaterialTheme.colorScheme.outline
        Column(Modifier.padding(start = 6.dp, end = 4.dp)) {
            for (day in 1..monthDays(viewedMonth)) {
                val date = LocalDate(viewedMonth.year, viewedMonth.month, day)
                val place = Place.Monthly(viewedMonth, day)
                val entries = journal.entriesAt(place)
                val isPicked = picked == day
                val pick = {
                    picked = if (isPicked) null else day
                    if (!isPicked) pickSignal++
                }
                if (day != 1 && day in weekStartDays) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(1.dp).drawBehind { drawLine(weekLine, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) })
                }
                Row(
                    Modifier.fillMaxWidth()
                        .then(if (date == today) Modifier.bringIntoViewRequester(todayRow) else Modifier)
                        .then(scrollHereWhen(linkTo == place, onLinkHandled))
                        .clip(RoundedCornerShape(12.dp))
                        .then(if (isPicked) Modifier.background(MaterialTheme.colorScheme.surfaceVariant) else Modifier),
                ) {
                    DayCell(date, today, isPicked, entries.size, pick)
                    Column(Modifier.weight(1f)) {
                        if (entries.isEmpty()) {
                            Box(
                                Modifier.fillMaxWidth().height(40.dp).semantics { hideFromAccessibility() }
                                    .clickable(role = Role.Button, onClick = pick).padding(start = 8.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (isPicked) Text(S.writingHere, style = Type.Secondary)
                            }
                        } else {
                            EntryListSection(entries, place, journal, today, onNavigateTo, margin = false)
                        }
                    }
                }
            }
        }

        Eyebrow(S.monthTasks)
        val tasksPlace = Place.Monthly(viewedMonth)
        val tasks = journal.entriesAt(tasksPlace)
        Box(scrollHereWhen(linkTo == tasksPlace, onLinkHandled)) {
            if (tasks.isEmpty()) EmptyText(S.monthTasksEmpty) else EntryListSection(tasks, tasksPlace, journal, today, onNavigateTo)
        }

        val monthPage = monthShare(journal, viewedMonth)
        if (monthPage.rows.isNotEmpty()) ShareRow(S.shareMonth) { sharing = monthPage }
    }

    sharing?.let { ShareSheet(it, onClose = { sharing = null }) }
}

/**
 * The date of a calendar row (docs/pantallas.md 7.1): the weekday's initial and the number, today on the
 * cover's colour, the weekend in grey. Touching it picks the day for the composer.
 */
@Composable
private fun DayCell(date: LocalDate, today: LocalDate, picked: Boolean, count: Int, onPick: () -> Unit) {
    val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    val isToday = date == today
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val weekday = S.weekdayNames()[date.dayOfWeek.ordinal]
    Row(
        Modifier.width(52.dp).height(40.dp)
            .clearAndSetSemantics {
                contentDescription = S.a11yDayRow(date.day, weekday, count) + if (picked) ", ${S.a11ySelected}" else ""
                onClick { onPick(); true }
            }
            .clickable(role = Role.Button, onClick = onPick)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(S.weekdayInitial()[date.dayOfWeek.ordinal], style = Type.Secondary.copy(fontSize = 11.sp), modifier = Modifier.width(14.dp))
        Box(
            Modifier.size(26.dp).clip(CircleShape)
                .then(if (isToday) Modifier.background(activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro).color) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.day.toString(),
                style = Type.Body.copy(
                    fontSize = 14.sp,
                    color = when {
                        isToday -> TODAY_INK
                        weekend -> muted
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    fontFeatureSettings = "tnum",
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Ink on the cover's pastel: dark in both themes, since the pastel is light in both. */
private val TODAY_INK = androidx.compose.ui.graphics.Color(0xFF23301F)
