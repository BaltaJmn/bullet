package com.baltajmn.bullet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.INDEX_ICONS
import com.baltajmn.bullet.model.THEME_MAX
import com.baltajmn.bullet.model.clampCodePoints
import com.baltajmn.bullet.model.oneLine
import com.baltajmn.bullet.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.bullet.ui.theme.Type
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The twelve marks of the Index (docs/tecnico.md 12.9, docs/pantallas.md 23.9), drawn like the method's
 * glyphs: a 24 unit box, 1.5 unit strokes, round caps. A pen could draw any of them in one go.
 */
@Composable
fun IndexIcon(id: String, size: Dp = 20.dp, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Canvas(Modifier.size(size)) {
        val u = this.size.width / 24f
        val stroke = Stroke(width = 1.5f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        fun path(build: Path.() -> Unit) = drawPath(Path().apply(build), tint, style = stroke)
        fun line(vararg pts: Pair<Float, Float>) = path {
            pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
        }
        fun ring(x: Float, y: Float, r: Float) = drawCircle(tint, radius = r * u, center = p(x, y), style = stroke)

        when (id) {
            "star" -> path {
                for (k in 0 until 10) {
                    val r = if (k % 2 == 0) 9f else 3.9f
                    val a = -PI / 2 + k * PI / 5
                    val x = (12 + r * cos(a)).toFloat() * u
                    val y = (12.8f + r * sin(a)).toFloat() * u
                    if (k == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            "heart" -> path {
                moveTo(12f * u, 19.5f * u)
                cubicTo(5f * u, 15f * u, 3f * u, 11f * u, 4.5f * u, 8f * u)
                cubicTo(6f * u, 5f * u, 10.5f * u, 5f * u, 12f * u, 8.5f * u)
                cubicTo(13.5f * u, 5f * u, 18f * u, 5f * u, 19.5f * u, 8f * u)
                cubicTo(21f * u, 11f * u, 19f * u, 15f * u, 12f * u, 19.5f * u)
                close()
            }
            "book" -> {
                line(12f to 7.5f, 12f to 19f)
                line(12f to 7.5f, 10f to 5.5f, 4.5f to 5.5f, 4.5f to 17f, 10f to 17f, 12f to 19f)
                line(12f to 7.5f, 14f to 5.5f, 19.5f to 5.5f, 19.5f to 17f, 14f to 17f, 12f to 19f)
            }
            "home" -> {
                line(4f to 11.5f, 12f to 4.5f, 20f to 11.5f)
                line(6.5f to 9.5f, 6.5f to 19.5f, 17.5f to 19.5f, 17.5f to 9.5f)
                line(10.5f to 19.5f, 10.5f to 14.5f, 13.5f to 14.5f, 13.5f to 19.5f)
            }
            "work" -> {
                drawRoundRect(tint, topLeft = p(3.5f, 8f), size = Size(17f * u, 11.5f * u), cornerRadius = CornerRadius(2f * u), style = stroke)
                line(9f to 8f, 9f to 5.5f, 15f to 5.5f, 15f to 8f)
                line(3.5f to 13f, 20.5f to 13f)
            }
            "travel" -> {
                ring(12f, 12f, 8f)
                drawOval(tint, topLeft = p(8.5f, 4f), size = Size(7f * u, 16f * u), style = stroke)
                line(4f to 12f, 20f to 12f)
            }
            "money" -> {
                drawRoundRect(tint, topLeft = p(3f, 7f), size = Size(18f * u, 10f * u), cornerRadius = CornerRadius(1.5f * u), style = stroke)
                ring(12f, 12f, 2.5f)
            }
            "health" -> line(
                9.5f to 4f, 14.5f to 4f, 14.5f to 9.5f, 20f to 9.5f, 20f to 14.5f, 14.5f to 14.5f,
                14.5f to 20f, 9.5f to 20f, 9.5f to 14.5f, 4f to 14.5f, 4f to 9.5f, 9.5f to 9.5f, 9.5f to 4f,
            )
            "food" -> {
                line(5f to 10f, 5.5f to 18f, 15.5f to 18f, 16f to 10f, 5f to 10f)
                path { addArc(Rect(14.5f * u, 11f * u, 20f * u, 16f * u), -90f, 180f) }
                line(8.5f to 7.5f, 8.5f to 5f)
                line(12.5f to 7.5f, 12.5f to 5f)
            }
            "music" -> {
                ring(7f, 17.5f, 2.5f)
                ring(16.5f, 15.5f, 2.5f)
                line(9.5f to 17.5f, 9.5f to 6.5f, 19f to 4.5f, 19f to 15.5f)
            }
            "idea" -> {
                path {
                    moveTo(9.5f * u, 16f * u)
                    cubicTo(9.5f * u, 13.5f * u, 6f * u, 12.5f * u, 6f * u, 9f * u)
                    cubicTo(6f * u, 5.5f * u, 9f * u, 3.5f * u, 12f * u, 3.5f * u)
                    cubicTo(15f * u, 3.5f * u, 18f * u, 5.5f * u, 18f * u, 9f * u)
                    cubicTo(18f * u, 12.5f * u, 14.5f * u, 13.5f * u, 14.5f * u, 16f * u)
                    close()
                }
                line(10f to 19f, 14f to 19f)
                line(11f to 21.5f, 13f to 21.5f)
            }
            "people" -> {
                ring(9f, 8.5f, 3f)
                path { addArc(Rect(3f * u, 13.5f * u, 15f * u, 25.5f * u), 180f, 180f) }
                ring(16.5f, 9.5f, 2.5f)
                line(16f to 14f, 17f to 14f)
                path { addArc(Rect(14.5f * u, 14f * u, 21.5f * u, 25f * u), 270f, 90f) }
            }
        }
    }
}

/**
 * "Icono y tema" (docs/pantallas.md 23.9): the twelve marks in a 4 x 3 grid of 48dp targets, "Sin icono",
 * and the theme in one line of [THEME_MAX]. An icon is saved on the tap; the theme when the sheet closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconThemeSheet(title: String, icon: String?, theme: String?, onSave: (String?, String?) -> Unit, onClose: () -> Unit) {
    var picked by remember { mutableStateOf(icon) }
    var typed by remember { mutableStateOf(theme.orEmpty()) }
    val close = {
        onSave(picked, typed)
        onClose()
    }
    ModalBottomSheet(
        onDismissRequest = close,
        sheetMaxWidth = MAX_CONTENT_WIDTH,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() },
    ) {
        SheetHeader(S.iconAndTheme, title, close)
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            INDEX_ICONS.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { id ->
                        val on = id == picked
                        Box(
                            Modifier.weight(1f).height(56.dp)
                                .semantics {
                                    contentDescription = S.iconName(id)
                                    selected = on
                                }
                                .clickable(role = Role.RadioButton) { picked = if (on) null else id; onSave(picked, typed) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                                    .background(if (on) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                IndexIcon(id, size = 24.dp, tint = if (on) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            ActionRow(S.iconNone, icon = { GlyphIcon(Glyph.CLOSE) }, trailing = if (picked == null) ({ GlyphIcon(Glyph.CHECK, tint = MaterialTheme.colorScheme.onBackground) }) else null) {
                picked = null
                onSave(null, typed)
            }
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)
                    .fieldFrame(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.outline, 12.dp).padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (typed.isEmpty()) Text(S.themeHint, style = Type.Ink.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                BasicTextField(
                    value = typed,
                    onValueChange = { typed = it.oneLine().clampCodePoints(THEME_MAX) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = Type.Ink.copy(fontSize = 16.sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { close() }),
                )
            }
        }
    }
}
