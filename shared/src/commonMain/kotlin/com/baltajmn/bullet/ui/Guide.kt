package com.baltajmn.bullet.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.ui.theme.Paper
import com.baltajmn.bullet.ui.theme.Type
import com.baltajmn.bullet.ui.theme.paper
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * The guide of the first start (docs/pantallas.md 13.1): four steps, each a moving picture of a made-up
 * page and one sentence, and nothing to set up. It can be skipped from the first step; the last one
 * lands in Hoy with the keyboard up. Ajustes opens it again.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GuideScreen(today: LocalDate, onDone: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val last = step == S.guideTitles.lastIndex
    BackHandler(true) { if (step > 0) step-- else onDone() }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).paper(Paper.Dotted)
            .safeDrawingPadding().padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 22.dp),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(S.appName, style = Type.Ink.copy(fontSize = 18.sp), modifier = Modifier.weight(1f))
            Box(
                Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClick = onDone).padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) { Text(S.skip, style = Type.Body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
            when (step) {
                0 -> KindsPicture()
                1 -> DonePicture()
                2 -> MovePicture(today)
                else -> ReviewPicture(today)
            }
        }
        Text(S.guideTitles[step], style = Type.Heading.copy(fontSize = 27.sp, lineHeight = 31.sp), modifier = Modifier.padding(top = 22.dp, bottom = 8.dp).semantics { heading() })
        Text(
            S.guideTexts[step],
            style = Type.Secondary.copy(fontSize = 15.sp, lineHeight = 22.sp),
            modifier = Modifier.widthIn(max = 320.dp).heightIn(min = 66.dp),
        )
        Row(
            Modifier.padding(top = 18.dp, bottom = 16.dp).semantics { contentDescription = S.guideStep(step + 1, S.guideTitles.size) },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            S.guideTitles.indices.forEach { k ->
                Box(
                    Modifier.height(6.dp).width(if (k == step) 18.dp else 6.dp).clip(RoundedCornerShape(3.dp))
                        .background(if (k == step) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (step > 0) QuietButton(S.previous, { step-- })
            PrimaryButton(if (last) S.guideStart else S.next, { if (last) onDone() else step++ }, Modifier.weight(1f), block = true)
        }
    }
}

/** The card every picture is drawn on: a small page, on the surface, a little raised. */
@Composable
private fun Picture(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier.widthIn(max = 292.dp).fillMaxWidth().shadow(18.dp, shape, ambientColor = Color(0x33282218), spotColor = Color(0x33282218))
            .clip(shape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 14.dp),
        content = content,
    )
}

@Composable
private fun PictureLabel(text: String) =
    Text(text.uppercase(), style = Type.Eyebrow.copy(lineHeight = 16.sp), modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))

/** A line of the picture: the glyph in its column, the text in ink, and a small tag at the end. */
@Composable
private fun PictureLine(
    text: String,
    tag: String? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    glyph: @Composable () -> Unit,
) {
    Row(modifier.fillMaxWidth().heightIn(min = 38.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(32.dp), contentAlignment = Alignment.Center) { glyph() }
        Text(text, style = Type.Ink.copy(fontSize = 16.sp, lineHeight = 22.sp, color = color), modifier = Modifier.weight(1f))
        if (tag != null) Tag(tag)
    }
}

@Composable
private fun Tag(text: String) = Text(
    text,
    style = Type.Secondary.copy(fontSize = 12.sp, lineHeight = 16.sp),
    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 9.dp, vertical = 3.dp),
)

/** 0 to 1 over [ms], forever: every picture reads its moment from this one clock. */
@Composable
private fun loop(ms: Int): Float {
    val t by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(ms, easing = LinearEasing)))
    return t
}

/** How far [t] is between [from] and [to], held at 0 before and 1 after. */
private fun ramp(t: Float, from: Float, to: Float) = ((t - from) / (to - from)).coerceIn(0f, 1f)

/** Step 1: the three kinds arrive one after the other, each with its symbol and its name. */
@Composable
private fun KindsPicture() {
    val t = loop(5000)
    Picture {
        PictureLabel(S.tabToday)
        listOf(
            Triple(Bullet.TASK, S.guideInk, S.bulletTask),
            Triple(Bullet.EVENT, S.guideDinner, S.bulletEvent),
            Triple(Bullet.NOTE, S.guideOpens, S.bulletNote),
        ).forEachIndexed { k, (bullet, text, tag) ->
            val shown = ramp(t, k * 0.24f, k * 0.24f + 0.08f) * (1 - ramp(t, 0.9f, 1f))
            PictureLine(text, tag, Modifier.graphicsLayer { alpha = shown; translationY = (1 - shown) * 6.dp.toPx() }) {
                BulletGlyph(bullet, TaskStatus.OPEN)
            }
        }
    }
}

/** Step 2: a touch on the dot, the x drawn stroke by stroke, and the line goes grey where it is. */
@Composable
private fun DonePicture() {
    val t = loop(3400)
    val undo = 1 - ramp(t, 0.92f, 1f)
    val first = ramp(t, 0.40f, 0.47f) * undo
    val second = ramp(t, 0.47f, 0.54f) * undo
    val grey = ramp(t, 0.48f, 0.56f) * undo
    val ringIn = ramp(t, 0.18f, 0.30f)
    val ringOut = ramp(t, 0.30f, 0.42f)
    val ink = MaterialTheme.colorScheme.onBackground
    val primary = MaterialTheme.colorScheme.primary
    Picture {
        PictureLabel(S.tabToday)
        PictureLine(S.guideQuote, color = lerp(ink, MaterialTheme.colorScheme.onSurfaceVariant, grey)) {
            Canvas(Modifier.size(34.dp)) {
                val ringAlpha = ringIn * (1 - ringOut)
                if (ringAlpha > 0f) {
                    val scale = 1.5f - 0.5f * ringIn - 0.2f * ringOut
                    drawCircle(primary.copy(alpha = ringAlpha), radius = 16.dp.toPx() * scale, style = Stroke(2.dp.toPx()))
                }
                translate(5.dp.toPx(), 5.dp.toPx()) { drawDone(ink, first, second) }
            }
        }
        PictureLine(S.guideInk) { BulletGlyph(Bullet.TASK, TaskStatus.OPEN) }
    }
}

/** Step 3: the task of yesterday turns into a >, and its copy appears on today. */
@Composable
private fun MovePicture(today: LocalDate) {
    val t = loop(4400)
    val back = 1 - ramp(t, 0.94f, 1f)
    val dot = 1 - ramp(t, 0.30f, 0.38f) * back
    val chevron = ramp(t, 0.36f, 0.48f) * back
    val copy = ramp(t, 0.50f, 0.60f) * back
    val ink = MaterialTheme.colorScheme.onBackground
    Picture {
        PictureLabel(S.dayTitle(today.minus(1, DateTimeUnit.DAY)))
        PictureLine(S.guideBank, S.movedName, color = MaterialTheme.colorScheme.onSurfaceVariant) {
            Canvas(Modifier.size(24.dp)) { drawMigrating(ink, dot, chevron) }
        }
        Spacer(Modifier.height(12.dp))
        PictureLabel(S.dayTitle(today))
        PictureLine(S.guideBank, S.guideCopy, Modifier.graphicsLayer { alpha = copy; translationY = (1 - copy) * -8.dp.toPx() }) {
            BulletGlyph(Bullet.TASK, TaskStatus.OPEN)
        }
    }
}

/** Step 4: last month's notice and its four ways out, lit one after the other. */
@Composable
private fun ReviewPicture(today: LocalDate) {
    val t = loop(4800)
    Picture {
        PictureLabel(S.monthName(monthOf(today).minus(1, DateTimeUnit.MONTH)))
        Text(S.guideUnclosed(4), style = Type.Ink.copy(fontSize = 18.sp), modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp))
        // null is the discarded glyph, which is not a status BulletGlyph draws.
        val ways = listOf(TaskStatus.DONE to S.actionDone, TaskStatus.MIGRATED to S.guideToToday, TaskStatus.SCHEDULED to S.guideToMonth, null to S.actionDiscard)
        ways.chunked(2).forEachIndexed { r, pair ->
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { c, (status, label) ->
                    val k = r * 2 + c
                    val lit = ((t - k * 0.25f + 1f) % 1f) < 0.24f
                    Pill(label, lit, Modifier.weight(1f)) { if (status == null) DiscardGlyph() else BulletGlyph(Bullet.TASK, status) }
                }
            }
        }
    }
}

@Composable
private fun Pill(label: String, lit: Boolean, modifier: Modifier, glyph: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier.heightIn(min = 38.dp).clip(shape)
            .background(if (lit) com.baltajmn.bullet.ui.theme.coverSoft(com.baltajmn.bullet.ui.theme.Cover.Sage) else Color.Transparent)
            .border(1.dp, if (lit) com.baltajmn.bullet.ui.theme.Cover.Sage.color else MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        glyph()
        Spacer(Modifier.width(4.dp))
        Text(label, style = Type.Secondary.copy(fontSize = 13.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onBackground), maxLines = 2)
    }
}

private fun DrawScope.stroke() = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.at(x: Float, y: Float) = Offset(x.dp.toPx(), y.dp.toPx())

/** The done glyph of BulletGlyph.kt with its two strokes drawn only [first] and [second] of the way. */
private fun DrawScope.drawDone(ink: Color, first: Float, second: Float) {
    drawCircle(ink, radius = 2.5.dp.toPx(), center = at(12f, 12f))
    if (first > 0f) drawLine(ink, at(8f, 8f), at(8f + 8f * first, 8f + 8f * first), 1.5.dp.toPx(), StrokeCap.Round)
    if (second > 0f) drawLine(ink, at(16f, 8f), at(16f - 8f * second, 8f + 8f * second), 1.5.dp.toPx(), StrokeCap.Round)
}

/** The open dot fading to [dot] while the > of a migrated task is drawn [chevron] of the way. */
private fun DrawScope.drawMigrating(ink: Color, dot: Float, chevron: Float) {
    if (dot > 0f) drawCircle(ink.copy(alpha = ink.alpha * dot), radius = 2.5.dp.toPx(), center = at(12f, 12f))
    if (chevron <= 0f) return
    val path = Path()
    val a = at(10f, 8f)
    val b = at(14.5f, 12f)
    val c = at(10f, 16f)
    path.moveTo(a.x, a.y)
    if (chevron < 0.5f) {
        val p = chevron * 2
        path.lineTo(a.x + (b.x - a.x) * p, a.y + (b.y - a.y) * p)
    } else {
        val p = (chevron - 0.5f) * 2
        path.lineTo(b.x, b.y)
        path.lineTo(b.x + (c.x - b.x) * p, b.y + (c.y - b.y) * p)
    }
    drawPath(path, ink, style = stroke())
}
