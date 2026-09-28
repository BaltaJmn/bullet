package com.baltajmn.bullet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.FUTURE_MONTHS
import com.baltajmn.bullet.model.FUTURE_MONTHS_MAX
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.MIGRATION_SHOWN_FROM
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.copyOf
import com.baltajmn.bullet.model.futureMonths
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val SHEET_MAX_WIDTH_DP = 576

private enum class SheetMode { ACTIONS, MIGRATE, SCHEDULE }

/** The status actions [entry]'s own state allows (docs/pantallas.md 5.6's table). An event or a note offers none. */
enum class StatusAction { MIGRATE, SCHEDULE, DISCARD, REOPEN, GO_TO_COPY }

fun statusActionsFor(entry: Entry, copyExists: Boolean): List<StatusAction> {
    if (entry.bullet != Bullet.TASK) return emptyList()
    return when (entry.status) {
        TaskStatus.OPEN -> listOf(StatusAction.MIGRATE, StatusAction.SCHEDULE, StatusAction.DISCARD)
        TaskStatus.DONE, TaskStatus.IRRELEVANT -> listOf(StatusAction.REOPEN)
        TaskStatus.MIGRATED, TaskStatus.SCHEDULED -> if (copyExists) listOf(StatusAction.GO_TO_COPY) else emptyList()
    }
}

/**
 * The long press sheet (docs/pantallas.md 5.6, #22): status actions first, then the three
 * signifiers, then edit and delete, always in this order and never a task status for an event or a
 * note. [today] is what migrate and schedule validate destinations against; [onEdit] starts the
 * inline edit of 5.4 (the field itself lives in `EntryRow`); [onGoToCopy] follows a migrated or
 * scheduled task's copy. Delete removes at once, no confirmation (docs/pantallas.md 5.6): the five
 * second Deshacer banner is #23's, once `BobbinRepository` grows an undo snapshot to show it from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    entry: Entry,
    journal: Journal,
    today: LocalDate,
    onClose: () -> Unit,
    onEdit: (String) -> Unit,
    onGoToCopy: (Place) -> Unit,
) {
    var mode by remember(entry.id) { mutableStateOf(SheetMode.ACTIONS) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = SHEET_MAX_WIDTH_DP.dp).fillMaxWidth().padding(horizontal = 24.dp)) {
                when (mode) {
                    SheetMode.ACTIONS -> ActionsContent(
                        entry = entry,
                        journal = journal,
                        onClose = onClose,
                        onEdit = onEdit,
                        onGoToCopy = onGoToCopy,
                        onMigrate = { mode = SheetMode.MIGRATE },
                        onSchedule = { mode = SheetMode.SCHEDULE },
                    )
                    SheetMode.MIGRATE -> MigrateDestinations(entry, today, onBack = { mode = SheetMode.ACTIONS }, onDone = onClose)
                    SheetMode.SCHEDULE -> ScheduleDestinations(entry, today, onBack = { mode = SheetMode.ACTIONS }, onDone = onClose)
                }
                Spacer(Modifier.height(gridUnit))
            }
        }
    }
}

@Composable
private fun ActionsContent(
    entry: Entry,
    journal: Journal,
    onClose: () -> Unit,
    onEdit: (String) -> Unit,
    onGoToCopy: (Place) -> Unit,
    onMigrate: () -> Unit,
    onSchedule: () -> Unit,
) {
    val landedCopy = copyOf(journal, entry.id)

    Spacer(Modifier.height(gridUnit))
    EntryPreview(entry)
    if (entry.bullet == Bullet.TASK) {
        val count = journal.migrationCount(entry.id)
        if (count >= MIGRATION_SHOWN_FROM) {
            Text(S.migratedTimes(count), style = Type.Secondary, modifier = Modifier.padding(top = 4.dp, start = 48.dp))
        }
    }
    Spacer(Modifier.height(gridUnit))

    statusActionsFor(entry, landedCopy != null).forEach { action ->
        when (action) {
            StatusAction.MIGRATE -> SheetRow(glyph = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }, label = S.actionMigrate, onClick = onMigrate)
            StatusAction.SCHEDULE -> SheetRow(glyph = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }, label = S.actionSchedule, onClick = onSchedule)
            StatusAction.DISCARD -> SheetRow(
                glyph = { DiscardGlyph() },
                label = S.actionDiscard,
                onClick = { BobbinRepository.discard(entry.id); onClose() },
            )
            StatusAction.REOPEN -> SheetRow(
                glyph = { BulletGlyph(Bullet.TASK, TaskStatus.OPEN) },
                label = S.actionReopen,
                onClick = { BobbinRepository.reopen(entry.id); onClose() },
            )
            StatusAction.GO_TO_COPY -> SheetRow(
                glyph = { BulletGlyph(Bullet.TASK, entry.status) },
                label = S.actionGoToCopy,
                onClick = { landedCopy?.let { onGoToCopy(it.place) }; onClose() },
            )
        }
    }

    Signifier.entries.forEach { s ->
        val active = s in entry.signifiers
        SheetRow(
            glyph = { SignifierGlyph(s) },
            label = signifierLabel(s),
            trailing = if (active) { { GlyphIcon(Glyph.CHECK, tint = MaterialTheme.colorScheme.onBackground) } } else null,
            onClick = { BobbinRepository.toggleSignifier(entry.id, s) },
        )
    }

    SheetRow(glyph = null, label = S.actionEdit, onClick = { onEdit(entry.id); onClose() })
    SheetRow(glyph = null, label = S.actionDelete, onClick = { BobbinRepository.delete(entry.id); onClose() })
}

private fun signifierLabel(s: Signifier): String = when (s) {
    Signifier.PRIORITY -> S.signifierPriority
    Signifier.INSPIRATION -> S.signifierInspiration
    Signifier.EXPLORE -> S.signifierExplore
}

@Composable
private fun EntryPreview(entry: Entry) {
    Row(Modifier.fillMaxWidth()) {
        Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) {
            entry.signifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
        }
        BulletGlyph(entry.bullet, entry.status)
        Text(
            entry.text,
            style = Type.Ink,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp).weight(1f),
        )
    }
}

/** One row of the sheet (docs/pantallas.md 5.6): the glyph in the bullet column, the name in `Body` from x72. */
@Composable
private fun SheetRow(glyph: (@Composable () -> Unit)?, label: String, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = gridUnit * 2).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(72.dp).padding(start = 48.dp)) { glyph?.invoke() }
        Text(label, style = Type.Body, modifier = Modifier.weight(1f))
        trailing?.invoke()
        Spacer(Modifier.width(24.dp))
    }
}

/**
 * docs/pantallas.md 5.7, "Migrar": Hoy, Mañana, tareas de este mes y un día de este mes. "A una
 * colección" waits for #30, which is what gives the sheet an actual list of collections to migrate
 * into and a way to create one on the spot.
 */
@Composable
private fun MigrateDestinations(entry: Entry, today: LocalDate, onBack: () -> Unit, onDone: () -> Unit) {
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val thisMonth = Place.Monthly(monthOf(today))
    var dayText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        SheetRow(glyph = null, label = S.back, onClick = onBack)
        if (entry.place != Place.Daily(today)) {
            SheetRow(glyph = null, label = S.toToday, onClick = { if (BobbinRepository.migrate(entry.id, Place.Daily(today))) onDone() })
        }
        if (entry.place != Place.Daily(tomorrow)) {
            SheetRow(glyph = null, label = S.toTomorrow, onClick = { if (BobbinRepository.migrate(entry.id, Place.Daily(tomorrow))) onDone() })
        }
        if (entry.place != thisMonth) {
            SheetRow(glyph = null, label = S.toThisMonth, onClick = { if (BobbinRepository.migrate(entry.id, thisMonth)) onDone() })
        }
        Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2).padding(start = 72.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(S.toDayOfMonth, style = Type.Body, modifier = Modifier.weight(1f))
            DayField(dayText) { dayText = it }
            Spacer(Modifier.width(8.dp))
            TextAction(S.migrateAction) {
                error = null
                val day = dayText.toIntOrNull()
                val month = monthOf(today)
                when {
                    day == null -> Unit
                    day !in 1..monthDays(month) -> error = S.dayOutOfRange(month, day)
                    LocalDate(month.year, month.month, day) < today -> error = S.dayPast
                    else -> if (BobbinRepository.migrate(entry.id, Place.Monthly(month, day))) onDone()
                }
            }
        }
        error?.let { Text(it, style = Type.Secondary, modifier = Modifier.padding(start = 72.dp)) }
    }
}

/** docs/pantallas.md 5.7, "Programar": an optional day, then a month per row, six at a time up to [FUTURE_MONTHS_MAX]. */
@Composable
private fun ScheduleDestinations(entry: Entry, today: LocalDate, onBack: () -> Unit, onDone: () -> Unit) {
    var dayText by remember { mutableStateOf("") }
    var shown by remember { mutableStateOf(FUTURE_MONTHS) }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        SheetRow(glyph = null, label = S.back, onClick = onBack)
        Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2).padding(start = 72.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(S.dayFieldOptional, style = Type.Body, modifier = Modifier.weight(1f))
            DayField(dayText) { dayText = it }
        }
        error?.let { Text(it, style = Type.Secondary, modifier = Modifier.padding(start = 72.dp)) }

        futureMonths(today, shown).forEach { month ->
            SheetRow(
                glyph = null,
                label = S.monthTitle(month),
                onClick = {
                    error = null
                    val day = dayText.toIntOrNull()
                    if (day != null && day !in 1..monthDays(month)) {
                        error = S.dayOutOfRange(month, day)
                    } else if (BobbinRepository.schedule(entry.id, month, day)) {
                        onDone()
                    }
                },
            )
        }
        if (shown < FUTURE_MONTHS_MAX) {
            TextAction(S.showMoreMonths) { shown = minOf(shown + FUTURE_MONTHS, FUTURE_MONTHS_MAX) }
        }
    }
}
