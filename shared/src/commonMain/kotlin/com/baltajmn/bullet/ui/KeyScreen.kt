package com.baltajmn.bullet.ui

import com.baltajmn.bullet.ui.theme.isWideScreen
import com.baltajmn.bullet.data.onIos
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.ui.theme.Type

/**
 * The notebook's key (docs/pantallas.md 13): every symbol with its plain name and what it means, and the
 * method's own name for it where it has one. Opened from Ajustes; nothing opens it on its own.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyScreen(onBack: () -> Unit) {
    BackHandler(true, onBack)
    Page(tab = false) {
        BackBar(S.back, onBack)
        PageHead(S.keyTitle, S.keyWhat)

        Eyebrow(S.keyWrite)
        KeyRow({ BulletGlyph(Bullet.TASK, TaskStatus.OPEN) }, S.bulletTask, S.keyTask)
        KeyRow({ BulletGlyph(Bullet.EVENT, TaskStatus.OPEN) }, S.bulletEvent, S.keyEvent)
        KeyRow({ BulletGlyph(Bullet.NOTE, TaskStatus.OPEN) }, S.bulletNote, S.keyNote)

        Eyebrow(S.keyHappened)
        KeyRow({ BulletGlyph(Bullet.TASK, TaskStatus.DONE) }, S.stateDone, S.keyDone)
        KeyRow({ BulletGlyph(Bullet.TASK, TaskStatus.MIGRATED) }, S.movedName, S.keyMoved)
        KeyRow({ BulletGlyph(Bullet.TASK, TaskStatus.SCHEDULED) }, S.otherMonthName, S.keyOtherMonth)
        KeyRow({ DiscardGlyph() }, S.stateDiscarded, S.keyDiscarded)

        Eyebrow(S.keyMargin)
        KeyRow({ SignifierGlyph(Signifier.PRIORITY) }, S.signifierPriority, S.keyPriority)
        KeyRow({ SignifierGlyph(Signifier.INSPIRATION) }, S.signifierInspiration, S.keyInspiration)
        KeyRow({ SignifierGlyph(Signifier.EXPLORE) }, S.signifierExplore, S.keyExplore)

        Text(boldMarked(S.keyPrefixes), style = Type.Secondary.copy(lineHeight = 19.sp), modifier = Modifier.padding(start = HEAD_START, end = 20.dp, top = 16.dp))

        // Shortcuts are for a keyboard, which a wide screen is the one to have (docs/pantallas.md 23.15).
        if (isWideScreen()) {
            Eyebrow(S.keysTitle)
            KeyRow({ ShortcutText("N") }, S.keyNew, null)
            KeyRow({ ShortcutText("F") }, S.keySearch, null)
            KeyRow({ ShortcutText("1-4") }, S.keyTabs, null)
            KeyRow({ ShortcutText("[ ]") }, S.keyPrevNext, null)
            KeyRow({ Text("Esc", style = Type.Label.copy(fontSize = 12.sp)) }, S.keyBack, null)
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** One row of the key: the symbol in its own column, the name, and what it means under it. */
@Composable
private fun KeyRow(glyph: @Composable () -> Unit, name: String, means: String?) {
    Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
        // A shortcut ("Ctrl+1-4", or any at a large font scale) is wider than a glyph: it widens its row.
        Box(Modifier.widthIn(min = 40.dp).heightIn(min = 22.dp), contentAlignment = Alignment.Center) { glyph() }
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = Type.Body.copy(fontWeight = FontWeight.Medium, lineHeight = 20.sp))
            if (means != null) Text(means, style = Type.Secondary.copy(lineHeight = 18.sp))
        }
    }
}

/** A shortcut as each platform writes it, in the glyph column. */
@Composable
private fun ShortcutText(key: String) = Text(shortcutLabel(key, onIos), style = Type.Label.copy(fontSize = 12.sp), maxLines = 1)
