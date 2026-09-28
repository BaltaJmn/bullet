package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.MIGRATION_SHOWN_FROM
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.ReviewScope
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.futureMonth
import com.baltajmn.bullet.model.futureWaiting
import com.baltajmn.bullet.model.migrationCount
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.placeDay
import com.baltajmn.bullet.model.reviewQueue
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * The Future Log review (docs/pantallas.md 11.4, docs/tecnico.md 6.5, #26), opened from the
 * `futureWaiting` line of Mes. One entry at a time, three actions, no reflect step and no bulk
 * anything: "Pasar al calendario" is a single `migrate`, "Descartar" a single `discard` or `delete`,
 * and "Dejarla" writes nothing at all.
 *
 * Reaching the end marks the month seen so the line does not come back; leaving half way through does
 * not, so what was left over shows up again next time (6.5).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FutureReviewScreen(today: LocalDate, onClose: () -> Unit) {
    // Back is the same as CLOSE: it leaves keeping whatever was decided, because each decision
    // already changed its entry (docs/pantallas.md 11).
    BackHandler(true, onClose)
    val journal = BobbinRepository.journal
    val currentMonth = monthOf(today)

    // "Dejarla" changes nothing in the diary, so only this session can remember it: without the set,
    // the entry left alone would be the next one offered, forever. Migrating and discarding need no
    // bookkeeping because they take the entry out of futureWaiting on their own.
    var left by remember { mutableStateOf(emptySet<String>()) }
    val queue = journal.futureWaiting(today).filterNot { it.id in left }
    // The queue is counted once, at open (docs/pantallas.md 11.2): migrating or discarding takes an
    // entry out of futureWaiting, so counting it live would shrink the total under the position.
    val total = remember { journal.futureWaiting(today).size }
    val decided = (total - queue.size).coerceIn(0, total)

    LaunchedEffect(queue.isEmpty()) {
        if (queue.isEmpty()) BobbinRepository.settings { it.copy(futureSeen = currentMonth) }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).paper()) {
            ReviewHeader(onClose, position = if (queue.isEmpty()) null else S.reviewPosition(minOf(decided + 1, total), total))

            val entry = queue.firstOrNull()
            if (entry == null) {
                Spacer(Modifier.height(gridUnit))
                Text(S.futureAllDecided, style = Type.Body, modifier = Modifier.padding(start = 48.dp))
                Spacer(Modifier.height(gridUnit))
                Box(Modifier.padding(start = 40.dp)) { TextAction(S.close, onClose) }
            } else {
                Text(
                    S.fromFuture(entry.futureMonth ?: currentMonth, entry.placeDay),
                    style = Type.Eyebrow,
                    modifier = Modifier.padding(start = 48.dp),
                )
                Spacer(Modifier.height(gridUnit))
                ReviewEntry(entry)
                if (entry.bullet == Bullet.TASK) {
                    val count = journal.migrationCount(entry.id)
                    if (count >= MIGRATION_SHOWN_FROM) {
                        Text(S.migratedTimes(count), style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
                    }
                }
                Spacer(Modifier.height(gridUnit * 2))

                // The day travels with the entry only if it is a day of this month: "Pasar al
                // calendario" of a September entry seen in October has no day to keep (6.5).
                val toDay = entry.placeDay?.takeIf { entry.futureMonth == currentMonth }
                ReviewAction(S.futureToCalendar, glyph = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) {
                    BobbinRepository.migrate(entry.id, Place.Monthly(currentMonth, toDay))
                }
                ReviewAction(S.futureLeave, glyph = null) { left = left + entry.id }
                ReviewAction(S.futureDiscard, glyph = { DiscardGlyph() }) {
                    // A task keeps its line struck through; an event or a note has no discarded state
                    // to wear, so it goes with the undo line behind it (docs/pantallas.md 11.4).
                    if (entry.bullet == Bullet.TASK) BobbinRepository.discard(entry.id) else BobbinRepository.delete(entry.id)
                }
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }
}

/**
 * Step 2 of a review (docs/pantallas.md 11.2, docs/tecnico.md 6.6, #27): one open task at a time, five
 * actions, and nothing anywhere that decides two. The queue comes from [reviewQueue] every
 * recomposition, so leaving half way through and coming back offers only what is still open: each
 * action already changed its own task, and there is no progress to save.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewScreen(scope: ReviewScope, today: LocalDate, onClose: () -> Unit) {
    BackHandler(true, onClose)
    val journal = BobbinRepository.journal
    val queue = journal.reviewQueue(scope)
    val total = remember { journal.reviewQueue(scope).size }
    val decided = (total - queue.size).coerceIn(0, total)

    // The destination picker replaces the actions in the same place (docs/pantallas.md 5.7), and a new
    // task always starts back at the actions.
    var mode by remember { mutableStateOf(Destination.NONE) }
    val entry = queue.firstOrNull()
    LaunchedEffect(entry?.id) { mode = Destination.NONE }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().paper()) {
            ReviewHeader(onClose, position = if (entry == null) null else S.reviewPosition(minOf(decided + 1, total), total))

            if (entry == null) {
                Spacer(Modifier.height(gridUnit))
                // A Month review that ends with nothing open is a closed month (docs/pantallas.md 11.3).
                // Asking for the rating the first time one closes is #56.
                val done = (scope as? ReviewScope.Month)?.let { S.monthClosed(it.month) } ?: S.reviewAllDecided
                Text(done, style = Type.Body, modifier = Modifier.padding(start = 48.dp))
                Spacer(Modifier.height(gridUnit))
                Box(Modifier.padding(start = 40.dp)) { TextAction(S.close, onClose) }
            } else {
                Text(originOf(entry), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
                Spacer(Modifier.height(gridUnit))
                ReviewEntry(entry)
                val count = journal.migrationCount(entry.id)
                if (count >= MIGRATION_SHOWN_FROM) {
                    // The only pressure the method applies, and it presses towards a different decision.
                    Text(S.migratedTimes(count), style = Type.Secondary, modifier = Modifier.padding(start = 48.dp))
                }
                Spacer(Modifier.height(gridUnit * 2))

                when (mode) {
                    Destination.NONE -> {
                        ReviewAction(S.reviewDone, glyph = { BulletGlyph(Bullet.TASK, TaskStatus.DONE) }) {
                            BobbinRepository.toggleDone(entry.id)
                        }
                        ReviewAction(S.reviewMigrate, glyph = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }) {
                            mode = Destination.MIGRATE
                        }
                        ReviewAction(S.reviewSchedule, glyph = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }) {
                            mode = Destination.SCHEDULE
                        }
                        ReviewAction(S.reviewDiscard, glyph = { DiscardGlyph() }) { BobbinRepository.discard(entry.id) }
                    }
                    // Both pickers move exactly one task and then the next one is on screen: the queue
                    // no longer holds the decided one.
                    Destination.MIGRATE -> MigrateDestinations(entry, today, onBack = { mode = Destination.NONE }, onDone = { mode = Destination.NONE })
                    Destination.SCHEDULE -> ScheduleDestinations(entry, today, onBack = { mode = Destination.NONE }, onDone = { mode = Destination.NONE })
                }
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
        if (BobbinRepository.pendingUndo != null) {
            UndoBanner(onUndo = BobbinRepository::undo)
        }
    }
}

/** docs/pantallas.md 11.2: where the task under review comes from, in `Eyebrow`. */
private fun originOf(entry: Entry): String = when (val p = entry.place) {
    is Place.Daily -> S.fromDay(p.date)
    is Place.Monthly -> if (p.day == null) S.fromMonthTasks(p.month) else S.fromCalendar(LocalDate(p.month.year, p.month.month, p.day))
    is Place.Future -> S.fromFuture(p.month, p.day)
    is Place.InCollection -> ""
}

/** The header of docs/pantallas.md 11: `CLOSE` on the left, and the position on the right while there is a task. */
@Composable
private fun ReviewHeader(onClose: () -> Unit, position: String?) {
    Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
        GlyphButton(Glyph.CLOSE, S.close, onClose)
        Spacer(Modifier.weight(1f))
        if (position != null) Text(position, style = Type.Secondary, modifier = Modifier.padding(end = 24.dp))
    }
}

/** The entry under review (docs/pantallas.md 11.2): `PageTitle`, its glyph and signifiers on the first baseline. */
@Composable
private fun ReviewEntry(entry: Entry) {
    Row(Modifier.fillMaxWidth().padding(end = 24.dp)) {
        Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) {
            entry.signifiers.sortedBy { it.ordinal }.forEach { SignifierGlyph(it) }
        }
        BulletGlyph(entry.bullet, entry.status)
        Text(entry.text, style = Type.PageTitle, modifier = Modifier.weight(1f))
    }
}

/** One of the review's actions (docs/pantallas.md 11.2): the glyph in the bullet column, the name in `Body` `primary`. */
@Composable
private fun ReviewAction(label: String, glyph: (@Composable () -> Unit)?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = gridUnit * 2).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(72.dp).padding(start = 48.dp)) { glyph?.invoke() }
        Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.primary), modifier = Modifier.weight(1f))
    }
}
