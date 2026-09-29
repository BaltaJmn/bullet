package com.baltajmn.bullet.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.UNDO_MS
import com.baltajmn.bullet.data.UndoKind
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.activeCover
import com.baltajmn.bullet.ui.theme.coverSoft
import com.baltajmn.bullet.ui.theme.page
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.coroutines.delay

/**
 * The pieces every page is built from (docs/pantallas.md 1.4, 3.2, 3.3): the page itself with its line
 * of confirmation, the header, the notice cards, the buttons and the action rows of a sheet.
 */

/** Where a page's title and headings start; entries start further left and put their glyph under it. */
val HEAD_START = 22.dp

/**
 * A page: its paper scrolls, the confirmation line floats over the bottom of it, and [bottom] (the
 * composer) sits under it. A [tab] page leaves the bottom inset to the tab bar, except while the
 * keyboard is up and the bar is gone; every other page takes all the insets itself.
 */
@Composable
fun Page(
    tab: Boolean,
    scroll: ScrollState = rememberScrollState(),
    spread: Boolean = false,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets = if (tab) {
        Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)).imePadding()
    } else {
        Modifier.safeDrawingPadding()
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).then(insets)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll).paper().page(spread)) {
                content()
                Spacer(Modifier.height(72.dp))
            }
            ToastLine(Modifier.align(Alignment.BottomCenter))
        }
        bottom()
    }
}

/** The tabs' header (docs/pantallas.md 3.2): search and settings, the two everyone recognises. */
@Composable
fun TopBar(onSearch: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp), horizontalArrangement = Arrangement.End) {
        GlyphButton(Glyph.SEARCH, S.a11ySearch, onSearch)
        GlyphButton(Glyph.SETTINGS, S.a11ySettings, onSettings)
    }
}

/** A page that opens over the tabs: back, named after where it goes, and whatever [trailing] adds. */
@Composable
fun BackBar(label: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onBack)
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphIcon(Glyph.BACK, size = 22.dp)
            Spacer(Modifier.width(2.dp))
            Text(label, style = Type.Body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/**
 * Title, what the page is, and on a few pages a sentence of what it is for (docs/pantallas.md 3.2).
 * [dot] marks today; [nav] holds the arrows; [action] is a link beside the subtitle ("Volver a hoy").
 */
@Composable
fun PageHead(
    title: String,
    subtitle: String? = null,
    explain: String? = null,
    dot: Boolean = false,
    action: Pair<String, () -> Unit>? = null,
    titleModifier: Modifier = Modifier,
    nav: @Composable RowScope.() -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().padding(start = HEAD_START, end = 12.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            if (dot) {
                Box(Modifier.size(9.dp).background(activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro).color, CircleShape))
                Spacer(Modifier.width(10.dp))
            }
            Text(title, style = Type.PageTitle, modifier = titleModifier.weight(1f).semantics { heading() })
            nav()
        }
        if (subtitle != null || action != null) {
            FlowRow(verticalArrangement = Arrangement.Center, itemVerticalAlignment = Alignment.CenterVertically) {
                if (subtitle != null) Text(subtitle, style = Type.Secondary, modifier = Modifier.padding(end = 12.dp))
                action?.let { (label, onClick) -> LinkText(label, onClick) }
            }
        }
        if (explain != null) {
            Text(explain, style = Type.Secondary.copy(fontSize = 14.sp, lineHeight = 20.sp), modifier = Modifier.padding(top = 8.dp).widthIn(max = 320.dp))
        }
    }
}

/** A link in running text: `primary`, underlined, 48 tall to the touch. */
@Composable
fun LinkText(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.heightIn(min = 40.dp).clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.CenterStart) {
        Text(
            label,
            style = Type.Secondary.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
        )
    }
}

/** A section label: uppercase, spaced, with an optional note beside it in normal case. */
@Composable
fun Eyebrow(text: String, note: String? = null, modifier: Modifier = Modifier) {
    FlowRow(
        modifier.fillMaxWidth().padding(start = HEAD_START + 6.dp, end = 16.dp, top = 22.dp, bottom = 4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text.uppercase(), style = Type.Eyebrow, modifier = Modifier.padding(end = 8.dp).semantics { heading() })
        if (note != null) Text(note, style = Type.Secondary.copy(fontSize = 12.sp))
    }
}

/** A quiet sentence where a list is empty, saying what to do next. */
@Composable
fun EmptyText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = Type.Secondary.copy(fontSize = 14.sp, lineHeight = 21.sp), modifier = modifier.padding(start = HEAD_START + 6.dp, end = 20.dp, top = 12.dp))
}

/** The one filled button: `primary`, a pill. [block] fills the width, as at the foot of a review or of the guide. */
@Composable
fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, block: Boolean = false) {
    Box(
        modifier
            .then(if (block) Modifier.fillMaxWidth().height(50.dp) else Modifier.height(40.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = Type.Label.copy(color = MaterialTheme.colorScheme.onPrimary, fontSize = if (block) 15.sp else 14.sp))
    }
}

/** A button with no fill, in `primary`: the second choice beside a [PrimaryButton], or "Entendido". */
@Composable
fun QuietButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(18.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = Type.Label.copy(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp))
    }
}

/**
 * A notice (docs/pantallas.md 3.3): what is pending in a sentence, what it means under it, and the
 * buttons that open it, each named after what it opens. On the cover's wash, never in red.
 */
@Composable
fun NoticeCard(title: String, body: String? = null, actions: List<Pair<String, () -> Unit>> = emptyList()) {
    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(coverSoft(activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro)))
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
    ) {
        Text(title, style = Type.Label)
        if (body != null) Text(body, style = Type.Secondary.copy(lineHeight = 19.sp), modifier = Modifier.padding(top = 3.dp))
        if (actions.isNotEmpty()) {
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                actions.forEachIndexed { i, (label, onClick) ->
                    if (i == 0) PrimaryButton(label, onClick) else QuietButton(label, onClick)
                }
            }
        }
    }
}

/** Hoy's one-time hint (docs/pantallas.md 6.3): a dashed card, bold where it says what to touch. */
@Composable
fun HintCard(text: String, onOk: () -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Column(
        Modifier.fillMaxWidth().padding(start = HEAD_START + 6.dp, end = 12.dp, top = 14.dp)
            .drawBehind {
                drawRoundRect(
                    line,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                )
            }
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
    ) {
        Text(boldMarked(text), style = Type.Secondary.copy(lineHeight = 19.sp))
        QuietButton(S.gotIt, onOk, Modifier.padding(top = 2.dp).offset(x = (-12).dp))
    }
}

/** Text with ^marked^ stretches in bold (docs/textos.md): how a string says what to touch. */
@Composable
fun boldMarked(text: String): AnnotatedString {
    val ink = MaterialTheme.colorScheme.onBackground
    return buildAnnotatedString {
        text.split('^').forEachIndexed { i, part ->
            if (i % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = ink)) { append(part) } else append(part)
        }
    }
}

/**
 * One action of a sheet or a review (docs/pantallas.md 5.6): what it leaves on the page as its icon,
 * its name, and under the name what it will do. [trailing] is a check for a mark that is on.
 */
@Composable
fun ActionRow(
    label: String,
    how: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick)
            .padding(start = 6.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Box(Modifier.width(40.dp).height(22.dp), contentAlignment = Alignment.Center) { icon?.invoke() }
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = Type.Body.copy(fontWeight = FontWeight.Medium, lineHeight = 20.sp))
            if (how != null) Text(how, style = Type.Secondary.copy(lineHeight = 18.sp), modifier = Modifier.padding(top = 2.dp))
        }
        trailing?.let { Box(Modifier.height(22.dp), contentAlignment = Alignment.Center) { it() } }
    }
}

/**
 * A line that says what just happened, for a few seconds and with no undo: "Tarea añadida en hoy."
 * The undo line of the repository takes its place when an action can be taken back.
 */
object Flash {
    var text by mutableStateOf<String?>(null)
        private set
    private var token by mutableStateOf(0)

    fun show(message: String) {
        text = message
        token++
    }

    internal val current get() = token
    internal fun clear(at: Int) { if (token == at) text = null }
    internal fun drop() { text = null }
}

/**
 * The confirmation line (docs/pantallas.md 5.8): ink on paper turned round, floating over the bottom of
 * the page, with "Deshacer" when the repository has something to put back.
 */
@Composable
fun ToastLine(modifier: Modifier = Modifier) {
    val undo = BobbinRepository.pendingUndo
    LaunchedEffect(undo) { if (undo != null) Flash.drop() }
    val flashAt = Flash.current
    LaunchedEffect(flashAt) {
        delay(UNDO_MS)
        Flash.clear(flashAt)
    }
    val flash = Flash.text
    val message = flash ?: undo?.let {
        when (it.kind) {
            UndoKind.ENTRY -> S.entryDeleted
            UndoKind.COLLECTION -> S.collectionDeleted
            UndoKind.ROW -> S.rowDeleted
            UndoKind.ACTION -> it.message
        }
    } ?: return
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    Row(
        modifier.fillMaxWidth().padding(12.dp).heightIn(min = 46.dp)
            .clip(RoundedCornerShape(14.dp)).background(ink)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = Type.Body.copy(color = paper, fontSize = 14.sp, lineHeight = 19.sp), modifier = Modifier.weight(1f).padding(vertical = 8.dp))
        if (flash == null) {
            Box(
                Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button, onClick = BobbinRepository::undo)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(S.undo, style = Type.Label.copy(color = activeCover(BobbinRepository.journal.settings, BobbinRepository.isPro).color, fontSize = 14.sp))
            }
        }
    }
}

/** A bordered field's frame: surface, the outline, rounded, as in the composer and the sheet's editor. */
fun Modifier.fieldFrame(fill: Color, line: Color, radius: Dp = 14.dp): Modifier =
    this.clip(RoundedCornerShape(radius)).background(fill).border(1.dp, line, RoundedCornerShape(radius))
