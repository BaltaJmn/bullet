package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.futureWaiting
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.ofDay
import com.baltajmn.bullet.model.openTasksBefore
import com.baltajmn.bullet.model.unclosedMonth
import com.baltajmn.bullet.ui.theme.activeCover
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The whole of `widget.json` (docs/tecnico.md 4.2): numbers, dates, booleans and one fixed id, never a
 * word of the diary. A new field goes here and in `BobbinStore.swift` in the same commit, and
 * `WIDGET_SAMPLE` changes with them.
 */
@Serializable
data class WidgetState(
    val date: String,
    val open: Int,
    val done: Int,
    val events: Int,
    val month: String,
    val monthMask: String,
    val reviewPending: Boolean,
    val isPro: Boolean,
    val cover: String,
    val dayStartHour: Int,
)

// encodeDefaults because Swift decodes every field.
val WidgetJson = Json { encodeDefaults = true; explicitNulls = false }

/** docs/tecnico.md 6.13. `monthMask` is worked out here once; neither widget computes it again. */
fun widgetState(j: Journal, isPro: Boolean, today: LocalDate): WidgetState {
    val day = j.ofDay(today)
    val m = monthOf(today)
    val mask = CharArray(monthDays(m)) { i -> if (j.ofDay(LocalDate(m.year, m.month, i + 1)).isNotEmpty()) '1' else '0' }
    return WidgetState(
        date = today.toString(),
        open = day.count { it.bullet == Bullet.TASK && it.status == TaskStatus.OPEN },
        done = day.count { it.bullet == Bullet.TASK && it.status == TaskStatus.DONE },
        events = day.count { it.bullet == Bullet.EVENT },
        month = m.toString(),
        monthMask = mask.concatToString(),
        reviewPending = j.openTasksBefore(today).isNotEmpty() || j.unclosedMonth(today) != null ||
            (j.futureWaiting(today).isNotEmpty() && j.settings.futureSeen != m),
        isPro = isPro,
        cover = activeCover(j.settings, isPro).id,
        dayStartHour = j.settings.dayStartHour,
    )
}

/**
 * What a widget paints now, without the app (docs/tecnico.md 6.13): the file may be from yesterday.
 * Yesterday's open tasks are now a review waiting. The same rule is in `BobbinStore.swift`.
 */
fun widgetView(st: WidgetState, today: LocalDate): WidgetState {
    if (st.date == today.toString()) return st
    val m = monthOf(today)
    val sameMonth = st.month == m.toString()
    return st.copy(
        date = today.toString(),
        open = 0,
        done = 0,
        events = 0,
        reviewPending = st.reviewPending || st.open > 0,
        month = m.toString(),
        monthMask = if (sameMonth) st.monthMask else "0".repeat(monthDays(m)),
    )
}
