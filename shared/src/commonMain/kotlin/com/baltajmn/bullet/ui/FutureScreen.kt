package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.FUTURE_MONTHS
import com.baltajmn.bullet.model.FUTURE_MONTHS_MAX
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.futureBlock
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.placeDay
import com.baltajmn.bullet.ui.theme.Spread
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.isWideScreen
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

/**
 * The Future Log (docs/pantallas.md 8, docs/tecnico.md 6.5): a block per month starting the one after
 * this one, [FUTURE_MONTHS] at a time up to [FUTURE_MONTHS_MAX]. Touching a month's name makes it where
 * the composer writes; the next month is the one to start with. Nothing on this page moves or notifies
 * on its own: an entry leaves the Future Log only because someone decided it in a review or its sheet.
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
    val nextMonth = monthOf(today).plus(1, DateTimeUnit.MONTH)
    var picked by remember(nextMonth) { mutableStateOf(nextMonth) }
    var pickSignal by remember { mutableStateOf(0) }

    Page(
        tab = true,
        spread = true,
        bottom = { Composer(Place.Future(picked), today, futureMonth = picked, focusSignal = pickSignal) },
    ) {
        TopBar(onSearch, onSettings)
        PageHead(S.tabFuture, S.futureSubtitle(shown), S.futureExplain)

        val block: @Composable ColumnScope.(YearMonth) -> Unit = { month ->
            val isPicked = month == picked
            Column(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 10.dp, top = 10.dp).clip(RoundedCornerShape(16.dp))
                    .then(if (isPicked) Modifier.background(MaterialTheme.colorScheme.surfaceVariant) else Modifier)
                    .padding(bottom = 8.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                        .semantics { selected = isPicked }
                        .clickable(role = Role.Button) { picked = month; pickSignal++ }
                        .padding(start = 20.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(S.monthTitle(month), style = Type.Body.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                    if (isPicked) Text(S.writingHere, style = Type.Label.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.primary))
                }
                val entries = journal.futureBlock(month)
                if (entries.isEmpty()) {
                    Text(S.futureEmpty, style = Type.Secondary.copy(fontSize = 14.sp), modifier = Modifier.padding(start = 20.dp, bottom = 4.dp))
                } else {
                    // The dated ones first, in the calendar's order, then the rest in the order written.
                    val (dated, undated) = entries.partition { it.placeDay != null }
                    dated.forEach { entry ->
                        Box(scrollHereWhen(linkTo == entry.place, onLinkHandled)) {
                            Column {
                                EntryRow(entry, journal, today, onNavigateTo)
                                Text(S.onDay(entry.placeDay ?: 0), style = Type.Secondary.copy(fontSize = 12.sp), modifier = Modifier.padding(start = 70.dp, bottom = 4.dp))
                            }
                        }
                    }
                    val undatedPlace = Place.Future(month)
                    Box(scrollHereWhen(linkTo == undatedPlace, onLinkHandled)) {
                        EntryListSection(undated, undatedPlace, journal, today, onNavigateTo)
                    }
                }
            }
        }
        val months = futureMonths(today, shown)
        if (isWideScreen()) {
            // Alternating left and right, in order, like the Future Log of a paper notebook opened flat
            // (docs/pantallas.md 21).
            Spread(
                left = { months.filterIndexed { i, _ -> i % 2 == 0 }.forEach { block(it) } },
                right = { months.filterIndexed { i, _ -> i % 2 == 1 }.forEach { block(it) } },
            )
        } else {
            months.forEach { block(it) }
        }

        if (shown < FUTURE_MONTHS_MAX) {
            Box(Modifier.padding(start = HEAD_START - 8.dp, top = 8.dp)) {
                QuietButton(S.showMoreMonths, { onShownChange(minOf(shown + FUTURE_MONTHS, FUTURE_MONTHS_MAX)) })
            }
        }
    }
}
