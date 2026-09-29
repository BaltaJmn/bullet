package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.baltajmn.bullet.ui.theme.coverSoft
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.ui.theme.Cover
import com.baltajmn.bullet.ui.theme.Paper
import com.baltajmn.bullet.ui.theme.activeCover
import com.baltajmn.bullet.ui.theme.activePaper
import com.baltajmn.bullet.ui.theme.canUse
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.billing.Billing
import com.baltajmn.bullet.data.readBackup
import com.baltajmn.bullet.data.merge
import com.baltajmn.bullet.data.exportZip
import com.baltajmn.bullet.data.exportName
import com.baltajmn.bullet.data.ReadBackup
import com.baltajmn.bullet.data.PickResult
import com.baltajmn.bullet.data.MergeResult
import com.baltajmn.bullet.data.ImportProblem
import com.baltajmn.bullet.data.FilePicker
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.baltajmn.bullet.data.Reminders
import com.baltajmn.bullet.data.NotifyPermission
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.data.AppInfo
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.Lock
import com.baltajmn.bullet.data.PRIVACY_URL
import com.baltajmn.bullet.data.SIBLINGS
import com.baltajmn.bullet.data.fold
import com.baltajmn.bullet.data.storeUrl
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

/** The dialog width of docs/pantallas.md 15, shared by every dialog of the app. */
internal const val DIALOG_MAX_WIDTH_DP = 576

/**
 * Settings (docs/pantallas.md 14, #32). Every one of them has a sensible default from the moment the
 * app is installed, which is what makes this the only screen someone can go their whole life without
 * opening. Everything saved here lives in `Journal.settings`, so it travels whole in an export (#44);
 * only Pro lives outside, in `Prefs`.
 *
 * The rows that need machinery of their own arrive with it: the notebook's covers and papers (#49),
 * and Bobbin Pro and restoring a purchase (#47, #48).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onKey: () -> Unit, onGuide: () -> Unit) {
    BackHandler(true, onBack)
    val settings = BobbinRepository.journal.settings
    val systemWeekStart = systemFirstDayOfWeek()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val uiScope = rememberCoroutineScope()
    // The backup's own dialogs (docs/pantallas.md 15.2, 15.3), each with what it has to say.
    var importPreview by remember { mutableStateOf<Pair<Journal, MergeResult>?>(null) }
    var importing by remember { mutableStateOf(false) }
    var importDone by remember { mutableStateOf<Int?>(null) }
    var importProblem by remember { mutableStateOf<ImportProblem?>(null) }
    var exportFailed by remember { mutableStateOf(false) }
    var restoring by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf<Boolean?>(null) }

    // Read again on every return from the system settings, so granting the permission there shows
    // here without restarting (docs/tecnico.md 6.12).
    var permissionCheck by remember { mutableStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissionCheck++ }
    val permission = remember(permissionCheck) { Reminders.permission() }
    // A permission taken away in the system turns the setting off the next time Settings shows,
    // instead of leaving a switch that says on and a reminder that never comes.
    LaunchedEffect(permission, settings.reminderOn) {
        if (settings.reminderOn && permission != NotifyPermission.GRANTED) {
            BobbinRepository.settings { it.copy(reminderOn = false) }
            Reminders.sync()
        }
    }
    val reminderOn = settings.reminderOn && permission == NotifyPermission.GRANTED

    fun setReminder(on: Boolean) {
        BobbinRepository.settings { it.copy(reminderOn = on) }
        Reminders.sync()
    }

    fun turnReminderOn() = when (permission) {
        NotifyPermission.GRANTED -> setReminder(true)
        NotifyPermission.CAN_ASK -> Reminders.requestPermission { granted ->
            permissionCheck++
            if (granted) setReminder(true)
        }
        NotifyPermission.DENIED -> Reminders.openSystemSettings()
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).paper().page()) {
            BackBar(S.back, onBack)
            PageHead(S.settingsTitle)

            // Help first: the two pages that explain the app, for whoever lands here looking for them.
            SettingsSection(S.sectionHelp)
            SettingsRow(title = S.guideAgain, subtitle = S.guideAgainSub, onClick = onGuide)
            SettingsRow(title = S.keyWhat, subtitle = S.keyRowSub, onClick = onKey)

            SettingsSection(S.sectionDay)
            SettingsRow(
                title = S.dayStartRow,
                subtitle = S.dayStartAt(settings.dayStartHour),
                onClick = { dialog = SettingsDialog.DAY_START },
            )
            SettingsRow(
                title = S.weekStartRow,
                subtitle = settings.firstDayOfWeek
                    ?.let { S.weekStartDay(DayOfWeek(it)) }
                    ?: S.weekStartSystem(systemWeekStart),
                onClick = { dialog = SettingsDialog.WEEK_START },
            )

            SettingsSection(S.sectionReminder)
            SettingsRow(
                title = S.reminderRow,
                // Off from the install (docs/pantallas.md 14). On, the row opens the time; off, it turns it on.
                subtitle = when {
                    permission == NotifyPermission.DENIED -> S.reminderDenied
                    reminderOn -> S.reminderAt(settings.reminderHour, settings.reminderMinute)
                    else -> S.reminderOff
                },
                onClick = { if (reminderOn) dialog = SettingsDialog.REMINDER_TIME else turnReminderOn() },
                trailing = {
                    Switch(
                        checked = reminderOn,
                        onCheckedChange = { on -> if (on) turnReminderOn() else setReminder(false) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
                },
            )
            if (permission == NotifyPermission.DENIED) {
                Box(Modifier.padding(start = HEAD_START - 2.dp)) { TextAction(S.openSystemSettings, Reminders::openSystemSettings) }
            }

            SettingsSection(S.sectionPrivacy)
            // No screen lock on the phone, no switch to turn on: there would be nothing to ask for.
            val lockAvailable = remember(permissionCheck) { Lock.isAvailable() }
            fun setLock(on: Boolean) {
                if (!on) {
                    BobbinRepository.settings { it.copy(lockOn = false) }
                    return
                }
                // Only saved once the phone has said yes, so a lock nobody can open is never switched on.
                Lock.authenticate { ok -> if (ok) BobbinRepository.settings { it.copy(lockOn = true) } }
            }
            SettingsRow(
                title = S.lockRow,
                subtitle = if (lockAvailable || settings.lockOn) S.lockSubtitle else S.lockUnavailable,
                onClick = if (lockAvailable || settings.lockOn) ({ setLock(!settings.lockOn) }) else null,
                trailing = {
                    Switch(
                        checked = settings.lockOn,
                        onCheckedChange = ::setLock,
                        enabled = lockAvailable || settings.lockOn,
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
                },
            )

            SettingsSection(S.sectionNotebook)
            NotebookLook(settings)

            if (FilePicker.available) {
                SettingsSection(S.sectionBackup)
                val journal = BobbinRepository.journal
                val nothing = journal.entries.none { !it.gone } && journal.collections.isEmpty()
                SettingsRow(
                    title = S.exportRow,
                    subtitle = if (nothing) S.exportNothing else S.exportSubtitle,
                    onClick = if (nothing) null else ({
                        uiScope.launch {
                            // Everything written first, then a snapshot: the zip is the diary as it is now.
                            BobbinRepository.flush()
                            val snapshot = BobbinRepository.journal
                            FilePicker.exportZip(exportName(BobbinRepository.today()), { sink -> exportZip(snapshot, sink) }) {
                                // Cancelling is not failing: it says nothing (docs/pantallas.md 15.3).
                                if (it == PickResult.Failed) exportFailed = true
                            }
                        }
                    }),
                )
                SettingsRow(
                    title = S.importRow,
                    subtitle = S.importSubtitle,
                    onClick = {
                        var read: ReadBackup? = null
                        FilePicker.importFile({ source -> read = readBackup(source) }) { picked ->
                            when (val r = read) {
                                null -> if (picked == PickResult.Failed) importProblem = ImportProblem.NOT_BACKUP
                                is ReadBackup.Failed -> importProblem = r.problem
                                // Nothing is applied yet: this is the merge done dry, for the summary.
                                is ReadBackup.Ok -> importPreview = r.journal to merge(BobbinRepository.journal, r.journal)
                            }
                        }
                    },
                )
            }

            SettingsSection(S.sectionPro)
            val isPro = BobbinRepository.isPro
            SettingsRow(
                title = S.proRow,
                subtitle = if (isPro) S.proOwned else S.proSubtitle,
                onClick = if (isPro) null else ({ Paywall.show() }),
            )
            // Always, with Pro too: both stores ask for it to be reachable without buying first.
            SettingsRow(
                title = S.restoreRow,
                subtitle = null,
                onClick = {
                    if (!restoring) {
                        restoring = true
                        uiScope.launch {
                            restored = Billing.restore()
                            restoring = false
                        }
                    }
                },
            )

            // "Más apps" only exists while some sister app has a page on this platform (docs/tecnico.md 6.16).
            val siblings = SIBLINGS.filter { it.storeUrl != null }
            if (siblings.isNotEmpty()) {
                SettingsSection(S.sectionMoreApps)
                siblings.forEach { sibling ->
                    SettingsRow(
                        title = sibling.name,
                        subtitle = sibling.tagline,
                        onClick = { sibling.storeUrl?.let(AppInfo::open) },
                    )
                }
            }

            SettingsSection(S.sectionAbout)
            SettingsRow(title = S.privacyRow, subtitle = null, onClick = { AppInfo.open(PRIVACY_URL) })
            SettingsRow(title = S.version(AppInfo.version), subtitle = null, onClick = null)

            // On its own at the end, after 2u, in onBackground and never in red (docs/pantallas.md 14).
            Spacer(Modifier.height(gridUnit * 2))
            SettingsRow(title = S.wipeRow, subtitle = S.wipeSubtitle, onClick = { dialog = SettingsDialog.WIPE })

            Spacer(Modifier.height(gridUnit * 2))
        }
    }

    importPreview?.let { (incoming, result) ->
        MessageDialog(
            title = S.importTitle,
            text = S.importSummary(result.added, result.updated, result.same),
            confirm = if (importing) S.working else S.importAction,
            onConfirm = {
                if (!importing) {
                    importing = true
                    uiScope.launch {
                        val changed = BobbinRepository.applyImport(incoming)
                        importing = false
                        importPreview = null
                        importDone = changed
                    }
                }
            },
            // Cancelling after the summary leaves the diary exactly as it was: nothing was applied.
            onDismiss = { if (!importing) importPreview = null },
        )
    }
    importDone?.let { MessageDialog(title = S.importTitle, text = S.importDone(it), onDismiss = { importDone = null }) }
    importProblem?.let { MessageDialog(title = S.importFailedTitle, text = importProblemText(it), onDismiss = { importProblem = null }) }
    restored?.let { MessageDialog(title = S.restoreRow, text = if (it) S.restoreDone else S.restoreNothing, onDismiss = { restored = null }) }
    if (exportFailed) MessageDialog(title = S.exportRow, text = S.exportFailed, onDismiss = { exportFailed = false })

    when (dialog) {
        null -> Unit
        // 00:00 to 06:00, seven rows (docs/pantallas.md 14). Changing it moves no entry: only which
        // day today is (docs/tecnico.md 6.1).
        SettingsDialog.DAY_START -> ChoiceDialog(
            title = S.dayStartRow,
            options = (0..6).map { hour -> Choice(S.dayStartAt(hour), hour == settings.dayStartHour) { BobbinRepository.settings { it.copy(dayStartHour = hour) } } },
            onClose = { dialog = null },
        )
        SettingsDialog.WEEK_START -> ChoiceDialog(
            title = S.weekStartRow,
            options = listOf(
                Choice(S.weekStartFollowSystem, settings.firstDayOfWeek == null) {
                    BobbinRepository.settings { it.copy(firstDayOfWeek = null) }
                },
            ) + DayOfWeek.entries.map { day ->
                Choice(S.weekStartDay(day), settings.firstDayOfWeek == day.isoDayNumber) {
                    BobbinRepository.settings { it.copy(firstDayOfWeek = day.isoDayNumber) }
                }
            },
            onClose = { dialog = null },
        )
        SettingsDialog.REMINDER_TIME -> ReminderTimeDialog(
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            onClose = { dialog = null },
            onPick = { hour, minute ->
                BobbinRepository.settings { it.copy(reminderHour = hour, reminderMinute = minute) }
                Reminders.sync()
            },
        )
        SettingsDialog.WIPE -> WipeDialog(
            onClose = { dialog = null },
            onWipe = {
                BobbinRepository.wipe()
                dialog = null
                // Back to Hoy with an empty diary, without restarting (docs/pantallas.md 15.4).
                onBack()
            },
        )
    }
}

/** A Wednesday the 23rd, so the preview says the same day in every language (docs/pantallas.md 14.1). */
private val PREVIEW_DAY = LocalDate(2026, 9, 23)

/**
 * docs/pantallas.md 14.1: every cover and paper can be looked at for free; touching one saves it only
 * if it is free or there is Pro. What is only being looked at lives here, so leaving Settings forgets it.
 */
@Composable
private fun NotebookLook(settings: Settings) {
    val isPro = BobbinRepository.isPro
    val savedCover = activeCover(settings, isPro)
    val savedPaper = activePaper(settings, isPro)
    var cover by remember { mutableStateOf(savedCover) }
    var paper by remember { mutableStateOf(savedPaper) }
    val side = Modifier.padding(start = HEAD_START + 6.dp, end = 24.dp)
    val frame = RoundedCornerShape(12.dp)

    Column(
        side.padding(top = 8.dp).fillMaxWidth().height(gridUnit * 8).clip(frame)
            .border(1.dp, MaterialTheme.colorScheme.outline, frame)
            .background(MaterialTheme.colorScheme.background).paper(paper),
    ) {
        Row(Modifier.height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.padding(start = 12.dp).size(8.dp).background(cover.color, CircleShape))
            Spacer(Modifier.width(16.dp))
            Text(S.dayTitle(PREVIEW_DAY), style = Type.PageTitle, maxLines = 1)
        }
        listOf(Bullet.TASK to S.previewTask, Bullet.EVENT to S.previewEvent).forEach { (bullet, text) ->
            Row(Modifier.height(gridUnit), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(24.dp))
                BulletGlyph(bullet, TaskStatus.OPEN)
                Text(text, style = Type.Ink, maxLines = 1)
            }
        }
        // The active tab as the bar paints it (docs/pantallas.md 3.1): the cover's wash behind the icon.
        Column(Modifier.padding(start = 28.dp, top = 6.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.width(52.dp).height(30.dp).clip(RoundedCornerShape(15.dp)).background(coverSoft(cover)),
                contentAlignment = Alignment.Center,
            ) { GlyphIcon(Glyph.TODAY, size = 21.dp, tint = MaterialTheme.colorScheme.onBackground) }
            Text(S.tabToday, style = Type.Secondary.copy(fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold))
        }
    }

    Cover.entries.chunked(4).forEach { row ->
        Row(side) {
            row.forEach { c ->
                LookSwatch(S.coverName(c.id), looked = c == cover, saved = c == savedCover, shape = CircleShape, cover = c) {
                    cover = c
                    if (canUse(c, isPro)) BobbinRepository.settings { it.copy(cover = c.id) }
                }
            }
        }
    }
    Row(side) {
        Paper.entries.forEach { p ->
            LookSwatch(S.paperName(p.id), looked = p == paper, saved = p == savedPaper, shape = RoundedCornerShape(6.dp), paper = p) {
                paper = p
                if (canUse(p, isPro)) BobbinRepository.settings { it.copy(paper = p.id) }
            }
        }
    }
    Text("${S.coverName(cover.id)}, ${S.paperName(paper.id)}", style = Type.Secondary, modifier = side)
    if (!canUse(cover, isPro) || !canUse(paper, isPro)) {
        Text(S.proLookHint, style = Type.Secondary, modifier = side.padding(top = 8.dp))
        // Buying saves what was being looked at, so the choice is not lost on the way to the store.
        Box(Modifier.padding(start = 40.dp)) {
            TextAction(S.useThis) {
                val c = cover
                val p = paper
                Paywall.show { BobbinRepository.settings { it.copy(cover = c.id, paper = p.id) } }
            }
        }
    }
}

/**
 * One cover (a 32dp circle of its colour) or one paper (a 32dp square with its pattern) in a 48dp
 * target: a 2dp ring 3dp out on the one being looked at, `CHECK` inside the saved one.
 */
@Composable
private fun LookSwatch(
    name: String,
    looked: Boolean,
    saved: Boolean,
    shape: Shape,
    cover: Cover? = null,
    paper: Paper? = null,
    onClick: () -> Unit,
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val line = MaterialTheme.colorScheme.outline
    val ring = if (shape == CircleShape) CircleShape else RoundedCornerShape(11.dp)
    Box(
        Modifier.size(48.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (saved) "$name, ${S.a11ySelected}" else name
                selected = saved
            }
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (looked) Box(Modifier.size(42.dp).border(2.dp, ink, ring))
        val swatch = Modifier.size(32.dp).clip(shape)
        if (cover != null) {
            Box(swatch.background(cover.color))
        } else if (paper != null) {
            Box(
                swatch.background(MaterialTheme.colorScheme.background).border(1.dp, line, shape)
                    .drawBehind { miniPaper(paper, line) },
            )
        }
        if (saved) GlyphIcon(Glyph.CHECK, tint = ink.copy(alpha = 0.7f))
    }
}

/** The paper's pattern at a third of its size, so four fit in a 32dp square. */
private fun DrawScope.miniPaper(kind: Paper, color: Color) {
    val step = 8.dp.toPx()
    val stroke = 1.dp.toPx()
    var at = step
    while (at < size.width) {
        when (kind) {
            Paper.Dotted -> {
                var x = step
                while (x < size.width) {
                    drawCircle(color, radius = stroke, center = Offset(x, at))
                    x += step
                }
            }
            Paper.Lined -> drawLine(color, Offset(0f, at), Offset(size.width, at), stroke)
            Paper.Grid -> {
                drawLine(color, Offset(0f, at), Offset(size.width, at), stroke)
                drawLine(color, Offset(at, 0f), Offset(at, size.height), stroke)
            }
            Paper.Blank -> return
        }
        at += step
    }
}

private enum class SettingsDialog { DAY_START, WEEK_START, REMINDER_TIME, WIPE }

/** A section label in `Eyebrow` with `2u` of air above it (docs/pantallas.md 14). */
@Composable
private fun SettingsSection(title: String) {
    Spacer(Modifier.height(gridUnit))
    Eyebrow(title)
}

/** One settings row (docs/pantallas.md 14): title, optional subtitle, optional control, and the whole row responds. */
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = HEAD_START + 6.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(title, style = Type.Body)
            if (subtitle != null) Text(subtitle, style = Type.Secondary)
        }
        if (trailing != null) {
            Spacer(Modifier.width(16.dp))
            trailing()
        }
    }
}

/**
 * docs/pantallas.md 15.4, #33: two steps and no red anywhere. The first says what goes and what stays;
 * the second asks for the word, so nothing is deleted by one mistaken tap. `wipeAction` only responds
 * once the field, folded with [fold], is the word in that language.
 */
@Composable
private fun WipeDialog(onClose: () -> Unit, onWipe: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    val word = S.wipeWord

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.widthIn(max = DIALOG_MAX_WIDTH_DP.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        title = { Text(if (confirming) S.wipeConfirmTitle(word) else S.wipeTitle, style = Type.Body) },
        text = {
            if (confirming) {
                BasicTextField(
                    value = typed,
                    onValueChange = { typed = it.oneLine() },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = Type.Ink,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    singleLine = true,
                )
            } else {
                Text(S.wipeText, style = Type.Body)
            }
        },
        confirmButton = {
            if (confirming) {
                DialogAction(S.wipeAction, enabled = fold(typed.trim()) == fold(word), onClick = onWipe)
            } else {
                DialogAction(S.wipeContinue) { confirming = true }
            }
        },
        dismissButton = { DialogAction(S.cancel, onClick = onClose) },
    )
}

/** docs/pantallas.md 14: Material3's time picker in a dialog, 24 hours like every time the app writes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(hour: Int, minute: Int, onClose: () -> Unit, onPick: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.widthIn(max = DIALOG_MAX_WIDTH_DP.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        title = { Text(S.reminderRow, style = Type.Body) },
        text = { TimePicker(state = state) },
        confirmButton = { DialogAction(S.ok) { onPick(state.hour, state.minute); onClose() } },
        dismissButton = { DialogAction(S.cancel, onClick = onClose) },
    )
}

private fun importProblemText(problem: ImportProblem): String = when (problem) {
    ImportProblem.NOT_BACKUP -> S.importNotBackup
    ImportProblem.DAMAGED -> S.importDamaged
    ImportProblem.TOO_NEW -> S.importTooNew
    ImportProblem.EMPTY -> S.importEmpty
    ImportProblem.SIBLING_PURL -> S.importIsSibling("Purl")
    ImportProblem.SIBLING_MOOD -> S.importIsSibling("MoodTraker")
    ImportProblem.SIBLING_QUILT -> S.importIsSibling("Quilt")
}

/**
 * A dialog that says one thing (docs/pantallas.md 15): with [confirm], two buttons and [onDismiss] as
 * `cancel`; without it, only `ok`.
 */
@Composable
private fun MessageDialog(title: String, text: String, onDismiss: () -> Unit, confirm: String? = null, onConfirm: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = DIALOG_MAX_WIDTH_DP.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        title = { Text(title, style = Type.Body) },
        text = { Text(text, style = Type.Body) },
        confirmButton = { if (confirm != null) DialogAction(confirm, onClick = onConfirm) else DialogAction(S.ok, onClick = onDismiss) },
        dismissButton = if (confirm != null) ({ DialogAction(S.cancel, onClick = onDismiss) }) else null,
    )
}

internal class Choice(val label: String, val current: Boolean, val onPick: () -> Unit)

/**
 * A one setting list (docs/pantallas.md 15.5): the row's own title, an option per row with `CHECK` on
 * the current one, and no buttons at all. Touching one saves and closes; outside or back closes
 * without changing anything.
 */
@Composable
internal fun ChoiceDialog(title: String, options: List<Choice>, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.widthIn(max = DIALOG_MAX_WIDTH_DP.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        title = { Text(title, style = Type.Body) },
        confirmButton = {},
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                options.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = gridUnit * 2)
                            .clickable(role = Role.Button) { option.onPick(); onClose() },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(option.label, style = Type.Body, modifier = Modifier.weight(1f))
                        Box(Modifier.width(24.dp)) {
                            if (option.current) GlyphIcon(Glyph.CHECK, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        },
    )
}

/** A dialog button in `primary`, never red, whatever it does (docs/pantallas.md 15). */
@Composable
internal fun DialogAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(
            label,
            style = Type.Body.copy(
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
