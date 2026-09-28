package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.baltajmn.bullet.data.AppInfo
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.PRIVACY_URL
import com.baltajmn.bullet.data.SIBLINGS
import com.baltajmn.bullet.data.storeUrl
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.i18n.systemFirstDayOfWeek
import com.baltajmn.bullet.model.firstDayOfWeek
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
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
 * The rows that need machinery of their own arrive with it: `lockRow` (#39), the notebook's covers and
 * papers (#49), export and import (#44, #45), Bobbin Pro and restoring a purchase (#47, #48), and the
 * wipe row (#33).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    BackHandler(true, onBack)
    val settings = BobbinRepository.journal.settings
    val systemWeekStart = systemFirstDayOfWeek()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).paper()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
            }
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.settingsTitle, style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp))
            }

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
                // Off from the install (docs/pantallas.md 14). What it schedules is #36 and #38.
                subtitle = if (settings.reminderOn) S.reminderAt(settings.reminderHour, settings.reminderMinute) else S.reminderOff,
                onClick = { BobbinRepository.settings { it.copy(reminderOn = !it.reminderOn) } },
                trailing = {
                    Switch(
                        checked = settings.reminderOn,
                        onCheckedChange = { on -> BobbinRepository.settings { it.copy(reminderOn = on) } },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
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

            Spacer(Modifier.height(gridUnit * 2))
        }
    }

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
    }
}

private enum class SettingsDialog { DAY_START, WEEK_START }

/** A section label in `Eyebrow` with `2u` of air above it (docs/pantallas.md 14). */
@Composable
private fun SettingsSection(title: String) {
    Spacer(Modifier.height(gridUnit * 2))
    Text(title.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
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
            .padding(start = 48.dp, end = 24.dp),
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
    TextButton(onClick = onClick, enabled = enabled) {
        Text(
            label,
            style = Type.Body.copy(
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
