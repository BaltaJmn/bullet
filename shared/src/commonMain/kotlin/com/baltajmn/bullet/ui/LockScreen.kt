package com.baltajmn.bullet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import com.baltajmn.bullet.data.Lock
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit

/**
 * Over everything else, with nothing of the diary on it and no paper (docs/pantallas.md 16.1): the
 * name and a way back in. The system dialog comes up on its own, and the action is only there for
 * whoever dismissed it.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    // Composed last, so this one answers Back: the screens underneath would otherwise close their
    // sheets and overlays behind a lock nobody has opened.
    BackHandler(true) {}
    // The field underneath keeps its focus while the app is away, and the keyboard comes back up
    // over the lock with it. Clearing the focus is what sends it down and keeps it down.
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focus.clearFocus(force = true)
        Lock.authenticate { if (it) onUnlocked() }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(S.appName, style = Type.PageTitle)
            Spacer(Modifier.height(gridUnit))
            TextAction(S.unlock) { Lock.authenticate { if (it) onUnlocked() } }
        }
    }
}
