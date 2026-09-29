package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper

/**
 * The notebook's key page (docs/pantallas.md 13, #31): one page to read, every glyph on it, and how
 * each one is written or done. Nothing opens it on its own, it has no steps, and closing it marks
 * nothing as seen: it is a page of the notebook, not an onboarding.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyScreen(onBack: () -> Unit) {
    BackHandler(true, onBack)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).paper().page()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
            }
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                Text(S.keyTitle, style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp))
            }
            Spacer(Modifier.height(gridUnit))

            Section(S.keyBullets)
            KeyRow(glyph = { BulletGlyph(Bullet.TASK, TaskStatus.OPEN) }, name = S.bulletTask, how = S.keyTaskHow)
            KeyRow(glyph = { BulletGlyph(Bullet.EVENT, TaskStatus.OPEN) }, name = S.bulletEvent, how = S.keyEventHow)
            KeyRow(glyph = { BulletGlyph(Bullet.NOTE, TaskStatus.OPEN) }, name = S.bulletNote, how = S.keyNoteHow)

            Section(S.keyStates)
            KeyRow(
                glyph = { BulletGlyph(Bullet.TASK, TaskStatus.DONE) },
                name = S.glyphName(Bullet.TASK, TaskStatus.DONE),
                how = S.keyDoneHow,
            )
            KeyRow(
                glyph = { BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) },
                name = S.glyphName(Bullet.TASK, TaskStatus.MIGRATED),
                how = S.keyMigratedHow,
                // Only the migrated row says where its arrow leads (docs/pantallas.md 13).
                extra = S.keyMigratedLink,
            )
            KeyRow(
                glyph = { BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) },
                name = S.glyphName(Bullet.TASK, TaskStatus.SCHEDULED),
                how = S.keyScheduledHow,
            )
            KeyRow(
                glyph = { DiscardGlyph() },
                name = S.glyphName(Bullet.TASK, TaskStatus.IRRELEVANT),
                how = S.keyDiscardedHow,
            )

            Section(S.keySignifiers)
            KeyRow(signifier = Signifier.PRIORITY, name = S.signifierPriority, how = S.keyPriorityHow)
            KeyRow(signifier = Signifier.INSPIRATION, name = S.signifierInspiration, how = S.keyInspirationHow)
            KeyRow(signifier = Signifier.EXPLORE, name = S.signifierExplore, how = S.keyExploreHow)

            Section(S.keyGestures)
            listOf(S.keyGestureTap, S.keyGestureHold, S.keyGestureSwipe, S.keyGestureDrag, S.keyTapText).forEach {
                Text(it, style = Type.Body, modifier = Modifier.padding(start = 48.dp, end = 24.dp, bottom = 8.dp))
            }
            Spacer(Modifier.height(gridUnit * 2))
        }
    }
}

/** A section heading in `Eyebrow`, with `2u` of air above it (docs/pantallas.md 13). */
@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(gridUnit * 2))
    Text(title.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(start = 48.dp))
    Spacer(Modifier.height(gridUnit))
}

/**
 * One row of the key (docs/pantallas.md 13): the glyph in its own column and, from x72, the name with
 * how it is written underneath. A [signifier] goes in the left margin instead of the bullet column,
 * which is where it lives on a real entry (1.4).
 */
@Composable
private fun KeyRow(
    name: String,
    how: String,
    glyph: (@Composable () -> Unit)? = null,
    signifier: Signifier? = null,
    extra: String? = null,
) {
    Row(Modifier.fillMaxWidth().heightIn(min = gridUnit * 2).padding(end = 24.dp)) {
        if (signifier != null) {
            Row(Modifier.widthIn(min = 48.dp), horizontalArrangement = Arrangement.End) { SignifierGlyph(signifier) }
            Spacer(Modifier.width(24.dp))
        } else {
            Spacer(Modifier.width(48.dp))
            Box(Modifier.width(24.dp)) { glyph?.invoke() }
        }
        Column(Modifier.weight(1f)) {
            Text(name, style = Type.Body)
            Text(how, style = Type.Secondary)
            if (extra != null) Text(extra, style = Type.Secondary)
        }
    }
}
