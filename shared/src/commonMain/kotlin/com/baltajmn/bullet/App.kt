package com.baltajmn.bullet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.Glyph
import com.baltajmn.bullet.ui.GlyphButton
import com.baltajmn.bullet.ui.TodayScreen
import com.baltajmn.bullet.ui.theme.BobbinTheme
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.gridUnit
import kotlinx.coroutines.launch

/**
 * Ten destinations and no more (SPEC 5, docs/pantallas.md 3): four tabs at the bottom, and
 * everything else opens as a single screen above them. Only [TODAY] has a real page so far
 * (#21); the rest land issue by issue and show a bare placeholder with just their title until
 * then.
 */
enum class Screen { TODAY, MONTH, FUTURE, INDEX, COLLECTION, REVIEW, SEARCH, KEY, SETTINGS, PRO }

private val TABS = listOf(Screen.TODAY, Screen.MONTH, Screen.FUTURE, Screen.INDEX)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    remember { BobbinRepository.load() }
    val scope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { scope.launch { BobbinRepository.flush() } }

    var today by remember { mutableStateOf(BobbinRepository.today()) }
    var viewedDay by remember { mutableStateOf(today) }
    // Coming back to the foreground is the only guaranteed moment a backgrounded app can catch a
    // day change (docs/tecnico.md 6.1, test 38). If Hoy was showing today, it follows to the new
    // one; a past day someone was reading stays put.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val wasToday = viewedDay == today
        today = BobbinRepository.today()
        if (wasToday) viewedDay = today
    }

    var tab by remember { mutableStateOf(Screen.TODAY) }
    // At most one screen open above the tab bar (docs/pantallas.md 3): each one owns its own
    // BackHandler, so one that still has a sheet or a dialog of its own open can close that first.
    var overlay by remember { mutableStateOf<Screen?>(null) }

    // Nothing left to pop once the overlay is closed: Mes, Futuro e Índice fall back to Hoy, and
    // Hoy looking at another day falls back to today. On today, in Hoy, this lets the system close
    // the app (docs/pantallas.md 3: "Atrás").
    BackHandler(overlay == null && (tab != Screen.TODAY || viewedDay != today)) {
        if (tab != Screen.TODAY) tab = Screen.TODAY else viewedDay = today
    }

    BobbinTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        Screen.TODAY -> TodayScreen(
                            today = today,
                            viewedDay = viewedDay,
                            onViewedDayChange = { viewedDay = it },
                            onKey = { overlay = Screen.KEY },
                            onSearch = { overlay = Screen.SEARCH },
                            onSettings = { overlay = Screen.SETTINGS },
                            onReview = { overlay = Screen.REVIEW },
                        )
                        else -> PlaceholderTab(tab)
                    }
                }
                TabBar(active = tab, onSelect = { tab = it })
            }

            overlay?.let { PlaceholderOverlay(it) { overlay = null } }
        }
    }
}

/** docs/pantallas.md 3.1: four equal labels, the active one marked, no icons. */
@Composable
private fun TabBar(active: Screen, onSelect: (Screen) -> Unit) {
    val line = MaterialTheme.colorScheme.outlineVariant
    Row(
        Modifier.fillMaxWidth().height(gridUnit * 2)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind { drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) },
    ) {
        TABS.forEach { screen ->
            val isActive = screen == active
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clickable(role = Role.Tab, onClick = { onSelect(screen) }),
                contentAlignment = Alignment.Center,
            ) {
                Column(Modifier.width(IntrinsicSize.Min), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        tabLabel(screen),
                        style = Type.Body.copy(
                            color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    if (isActive) {
                        Box(
                            Modifier.padding(top = 4.dp).fillMaxWidth().height(2.dp)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}

private fun tabLabel(screen: Screen): String = when (screen) {
    Screen.TODAY -> S.tabToday
    Screen.MONTH -> S.tabMonth
    Screen.FUTURE -> S.tabFuture
    Screen.INDEX -> S.tabIndex
    else -> error("$screen is not a tab")
}

/** Mes, Futuro e Índice until #24, #25 and #29 give them a real page. */
@Composable
private fun PlaceholderTab(screen: Screen) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text(tabLabel(screen), style = Type.PageTitle)
    }
}

/**
 * Colección, Revisar, Buscar, Clave, Ajustes y Pro until #23, #26/#27/#28, #35, #31, #32 and #48
 * build them. Each keeps its own [BackHandler], as the real screens will (docs/pantallas.md 3):
 * one that opens a sheet or a dialog of its own can close that first without leaving the screen.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlaceholderOverlay(screen: Screen, onBack: () -> Unit) {
    BackHandler(true, onBack)
    // Buscar (pantallas.md 12) and Colección (10) have no fixed title of their own; Colección is
    // also unreachable yet, with no Índice, Buscar result or migrated link to open it from.
    val title = when (screen) {
        Screen.KEY -> S.keyTitle
        Screen.SETTINGS -> S.settingsTitle
        Screen.PRO -> S.proTitle
        Screen.REVIEW -> S.reflectTitle()
        else -> null
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(gridUnit * 2), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack)
            }
            title?.let { Text(it, style = Type.PageTitle, modifier = Modifier.padding(start = 48.dp)) }
        }
    }
}
