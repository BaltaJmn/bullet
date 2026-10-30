package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.model.WeekDay
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.lastWeekAllowed
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.weekOf
import com.baltajmn.bullet.model.weekStart
import com.baltajmn.bullet.model.writeTarget
import com.baltajmn.bullet.ui.theme.Spread
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.isSpread
import com.baltajmn.bullet.ui.theme.activeCover
import com.baltajmn.bullet.ui.theme.coverSoft
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Día or Semana under Hoy's subtitle (docs/pantallas.md 23.13). Remembered for the session, Día at every
 * start.
 */
@Composable
fun ViewSwitch(week: Boolean, onChange: (Boolean) -> Unit) =
    TwoChoice(S.viewDay, S.viewWeek, second = week, onChange = onChange, modifier = Modifier.padding(start = HEAD_START, top = 6.dp))

/**
 * Two options with their names, the chosen one on the cover's wash like the tab bar: Día or Semana, and
 * Publicación or Historia when sharing. [second] says which one is chosen.
 */
@Composable
fun TwoChoice(first: String, secondLabel: String, second: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val wash = coverSoft(activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro))
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(false to first, true to secondLabel).forEach { (isSecond, label) ->
            val active = isSecond == second
            Box(
                Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(18.dp)).background(if (active) wash else Color.Transparent)
                    .semantics { selected = active }
                    .clickable(role = Role.Tab) { if (!active) onChange(isSecond) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = Type.Label.copy(
                        fontSize = 14.sp,
                        color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

/**
 * Hoy as a week (docs/pantallas.md 23.13, docs/tecnico.md 12.13): the seven days of [anchor]'s week, each
 * with everything that carries its date. Touching a day's heading puts the composer there, in the place
 * [writeTarget] says. It opens without the keyboard: a week is read before it is written.
 */
@Composable
fun WeekPage(
    today: LocalDate,
    anchor: LocalDate,
    onAnchorChange: (LocalDate) -> Unit,
    onDayMode: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onReview: (ReviewScope) -> Unit,
    onNavigateTo: (Place) -> Unit,
) {
    val journal = BobbinRepository.journal
    val first = firstDayOfWeek(journal.settings, systemFirstDayOfWeek())
    val start = weekStart(anchor, first)
    val end = start.plus(6, DateTimeUnit.DAY)
    val thisWeek = weekStart(today, first)
    val last = lastWeekAllowed(today, first)
    val days = journal.weekOf(start, first)
    val spread = isSpread()
    var chosen by remember(start) { mutableStateOf(if (today in start..end) today else start) }
    val scroll = rememberScrollState()

    fun previous() = onAnchorChange(start.minus(7, DateTimeUnit.DAY))
    fun next() { if (start < last) onAnchorChange(start.plus(7, DateTimeUnit.DAY)) }

    Page(
        tab = true,
        scroll = scroll,
        spread = spread,
        bottom = {
            Composer(place = writeTarget(chosen, today), today = today)
        },
    ) {
        TopBar(onSearch, onSettings)
        PageHead(
            title = S.weekTitle(start),
            subtitle = S.weekRange(start, end),
            action = if (start == thisWeek) null else S.backToThisWeek to { onAnchorChange(today) },
            titleModifier = Modifier.pointerInputHorizontalSwipe(start, threshold = 72.dp, onSwipeRight = ::previous, onSwipeLeft = ::next),
        ) {
            GlyphButton(Glyph.BACK, S.a11yPreviousWeek, ::previous)
            if (start < last) GlyphButton(Glyph.FORWARD, S.a11yNextWeek, ::next) else Spacer(Modifier.width(48.dp))
        }
        ViewSwitch(week = true) { onDayMode() }
        if (start == thisWeek) Notices(journal, today, onReview, offerReminder = false, onOfferAnswered = {})
        Spacer(Modifier.heightIn(min = 4.dp))
        val block: @Composable (WeekDay) -> Unit = { day ->
            WeekDayBlock(
                day = day,
                showMonth = day.date == start || day.date.day == 1,
                isToday = day.date == today,
                chosen = day.date == chosen,
                today = today,
                onChoose = { chosen = if (chosen == day.date && today in start..end) today else day.date },
                onNavigateTo = onNavigateTo,
            )
        }
        // A weekly spread of paper: the first three days on the left, the other four on the right (12.15).
        if (spread) {
            Spread(left = { days.take(3).forEach { block(it) } }, right = { days.drop(3).forEach { block(it) } })
        } else {
            days.forEach { block(it) }
        }
    }
}

/** One day of the week: its heading, which chooses where to write, and its entries by place. */
@Composable
internal fun WeekDayBlock(
    day: WeekDay,
    showMonth: Boolean,
    isToday: Boolean,
    chosen: Boolean,
    today: LocalDate,
    onChoose: () -> Unit,
    onNavigateTo: (Place) -> Unit,
) {
    val journal = BobbinRepository.journal
    val d = day.date
    val m = monthOf(d)
    val label = S.weekDayLabel(d, withMonth = showMonth).uppercase() + if (isToday) " · " + S.weekToday.uppercase() else ""
    Row(
        Modifier.padding(start = HEAD_START, end = 12.dp, top = 14.dp).fillMaxWidth().heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (chosen) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .semantics { selected = chosen }
            .clickable(role = Role.Button, onClick = onChoose)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isToday) {
            Box(Modifier.size(8.dp).background(activeCover(journal.settings, BobbinRepository.isPro).color, CircleShape))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = Type.Eyebrow, modifier = Modifier.semantics { heading() })
    }
    if (day.daily.isNotEmpty()) EntryListSection(day.daily, Place.Daily(d), journal, today, onNavigateTo)
    if (day.calendar.isNotEmpty()) {
        PlaceNote(S.inCalendar)
        EntryListSection(day.calendar, Place.Monthly(m, d.day), journal, today, onNavigateTo)
    }
    if (day.future.isNotEmpty()) {
        PlaceNote(S.inFuture)
        EntryListSection(day.future, Place.Future(m, d.day), journal, today, onNavigateTo)
    }
}

/** Where the entries below live, when it is not the day's own Daily Log. */
@Composable
private fun PlaceNote(text: String) {
    Column(Modifier.padding(start = HEAD_START + 6.dp, top = 4.dp)) {
        Text(text, style = Type.Secondary.copy(fontSize = 12.sp))
    }
}
