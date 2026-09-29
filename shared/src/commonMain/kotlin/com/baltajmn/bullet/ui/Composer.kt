package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.COUNTER_FROM
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.TEXT_LIMIT
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.codePointCount
import com.baltajmn.bullet.model.limitEdit
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.model.rapidParse
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.page
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * The composer (docs/pantallas.md 5.2): always at the bottom of the page, with the kind in sight and the
 * destination written out, "En hoy", "En el 12 de septiembre", "En octubre". The prefixes of rapid
 * logging still work and light the chip they choose; a signifier prefix becomes a mark in the margin.
 *
 * [place] is where it writes; [futureMonth] turns on the day field of Futuro, and then the place is that
 * month with the day typed, if any. [suggested] is the kind it starts on, and comes back to it whenever
 * [place] changes: Mes suggests an event for a day. [list] is the collection's title when it writes in
 * one. [autoFocus] raises the keyboard on arrival; [focusSignal] does it again whenever it goes up.
 */
@Composable
fun Composer(
    place: Place,
    today: LocalDate,
    list: String? = null,
    futureMonth: YearMonth? = null,
    suggested: Bullet = Bullet.TASK,
    autoFocus: Boolean = false,
    focusSignal: Int = 0,
    onSaved: () -> Unit = {},
) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    var picked by remember(place, suggested) { mutableStateOf(suggested) }
    var dayText by remember(futureMonth) { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusSignal, autoFocus) {
        if (autoFocus || focusSignal > 0) {
            focus.requestFocus()
            keyboard?.show()
        }
    }

    val day = dayText.toIntOrNull()
    val target = futureMonth?.let { Place.Future(it, day) } ?: place
    val label = S.targetLabel(target, today, list)
    val parsed = rapidParse(value.text, picked)
    val shown = parsed?.bullet ?: picked
    val count = value.text.codePointCount()

    fun submit() {
        if (futureMonth != null && day != null && day !in 1..monthDays(futureMonth)) {
            Flash.show(S.dayOutOfRange(futureMonth, day))
            return
        }
        if (BobbinRepository.capture(value.text, target, picked)) {
            value = TextFieldValue("")
            dayText = ""
            Flash.show(S.added(shown, label))
            onSaved()
        }
    }

    val line = MaterialTheme.colorScheme.outline
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .drawBehind { drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
            .page()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KindChip(Bullet.TASK, S.bulletTask, shown == Bullet.TASK) { picked = Bullet.TASK; focus.requestFocus() }
            KindChip(Bullet.EVENT, S.bulletEvent, shown == Bullet.EVENT) { picked = Bullet.EVENT; focus.requestFocus() }
            KindChip(Bullet.NOTE, S.bulletNote, shown == Bullet.NOTE) { picked = Bullet.NOTE; focus.requestFocus() }
            Text(
                buildAnnotatedString {
                    append(S.composeFor + " ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)) { append(label) }
                },
                style = Type.Secondary.copy(fontSize = 12.sp, lineHeight = 16.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (futureMonth != null) {
                Box(
                    Modifier.width(56.dp).height(44.dp).fieldFrame(MaterialTheme.colorScheme.surface, line).padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) { DayField(dayText, Modifier.fillMaxWidth()) { dayText = it } }
                Spacer(Modifier.width(8.dp))
            }
            Box(
                Modifier.weight(1f).height(44.dp).fieldFrame(MaterialTheme.colorScheme.surface, line).padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.text.isEmpty()) Text(S.composeHint(picked), style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant), maxLines = 1)
                BasicTextField(
                    value = value,
                    onValueChange = { new ->
                        val edit = limitEdit(value.text.oneLine(), new.text.oneLine(), new.selection.end, TEXT_LIMIT)
                        value = TextFieldValue(edit.text, TextRange(edit.cursor))
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                        .semantics { contentDescription = "${S.composeHint(picked).removeSuffix("...")}, ${S.composeFor} $label" }
                        // Intro saves; a hardware Enter must not insert the line break oneLine() would
                        // otherwise have to undo (docs/tecnico.md 6.2).
                        .onPreviewKeyEvent { event ->
                            val enter = event.key == Key.Enter || event.key == Key.NumPadEnter
                            if (enter && event.type == KeyEventType.KeyDown) {
                                submit()
                                true
                            } else {
                                enter
                            }
                        },
                    textStyle = Type.Ink.copy(fontSize = 16.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.height(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary)
                    .clickable(role = Role.Button, onClick = ::submit).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(S.add, style = Type.Label.copy(color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp))
            }
        }
        if (count >= COUNTER_FROM) {
            Text(S.counter(count, TEXT_LIMIT), style = Type.Secondary.copy(fontSize = 12.sp), modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
        }
    }
}

/** One of the three kinds, with its own glyph (docs/pantallas.md 5.2): ink filled when it is the one. */
@Composable
private fun KindChip(bullet: Bullet, label: String, on: Boolean, onClick: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    val fg = if (on) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.heightIn(min = 30.dp).clip(RoundedCornerShape(15.dp))
            .then(if (on) Modifier.background(ink) else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(15.dp)))
            .semantics { selected = on }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 2.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BulletGlyph(bullet, TaskStatus.OPEN, tint = fg)
        Text(label, style = Type.Secondary.copy(color = fg, lineHeight = 16.sp))
    }
}
