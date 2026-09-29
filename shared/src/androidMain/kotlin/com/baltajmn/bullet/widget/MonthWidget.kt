package com.baltajmn.bullet.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.baltajmn.bullet.data.WidgetState
import com.baltajmn.bullet.data.readWidgetState
import com.baltajmn.bullet.data.shownMask
import com.baltajmn.bullet.data.widgetToday
import com.baltajmn.bullet.data.widgetView
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.shared.R
import com.baltajmn.bullet.ui.theme.Cover
import kotlinx.datetime.LocalDate

class MonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MonthWidget()
}

/**
 * Pro (docs/pantallas.md 18.2): one dot per day of the month, filled where the day has entries, like
 * the Monthly Log and not a calendar. Only `monthMask` reaches it, never a word of the diary.
 */
class MonthWidget : GlanceAppWidget() {

    // Exact and not Responsive: the dots are a bitmap, painted for the size the launcher gives.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val raw = readWidgetState()
        val today = widgetToday(raw)
        val state = raw?.let { widgetView(it, today) }
        provideContent { Month(state, today) }
    }
}

@Composable
private fun Month(state: WidgetState?, today: LocalDate) {
    val context = LocalContext.current
    val scheme = widgetScheme()
    val ink = ColorProvider(scheme.onBackground)
    val muted = ColorProvider(scheme.onSurfaceVariant)
    val mask = shownMask(state)
    val pro = mask != null
    val month = monthOf(today)
    val density = context.resources.displayMetrics.density
    val size = LocalSize.current
    // What is left under the 24dp header, inside the 8dp padding.
    val width = ((size.width.value - 16) * density).toInt().coerceAtLeast(1)
    val height = ((size.height.value - 16 - 28) * density).toInt().coerceAtLeast(1)

    Column(
        // Without Pro the whole widget is the door to the paywall: a locked grid that opened Hoy would explain nothing.
        modifier = GlanceModifier.fillMaxSize().background(ColorProvider(scheme.background)).padding(8.dp)
            .clickable(actionStartActivity(openLink(context, if (pro) "today" else "pro"))),
    ) {
        Row(GlanceModifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                S.widgetMonthName(month).uppercase(),
                maxLines = 1,
                style = TextStyle(color = muted, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            )
            Spacer(GlanceModifier.defaultWeight())
            Image(ImageProvider(R.drawable.glyph_task), null, GlanceModifier.size(24.dp), colorFilter = ColorFilter.tint(ink))
            Spacer(GlanceModifier.width(4.dp))
            Text("${state?.open ?: 0}", style = TextStyle(color = ink, fontSize = 17.sp, fontWeight = FontWeight.Medium))
        }
        Spacer(GlanceModifier.height(4.dp))
        Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                ImageProvider(
                    monthBitmap(
                        width = width,
                        height = height,
                        density = density,
                        days = monthDays(month),
                        mask = mask,
                        today = today.day,
                        cover = Cover.of(state?.cover ?: Cover.Sage.id).color.toArgb(),
                        outline = scheme.outline.toArgb(),
                        ink = scheme.onBackground.toArgb(),
                    ),
                ),
                contentDescription = null,
            )
            if (!pro) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(S.proTitle, style = TextStyle(color = ink, fontSize = 15.sp, fontWeight = FontWeight.Medium))
                    Text(S.widgetUnlock, style = TextStyle(color = muted, fontSize = 12.sp))
                }
            }
        }
    }
}

private const val PER_ROW = 16

/**
 * Glance has no canvas: the two rows of dots, 1 to 16 and 17 to the last day, drawn into a bitmap
 * of exactly the size they get. A null [mask] is the locked widget: every dot empty, at 40 %.
 */
internal fun monthBitmap(
    width: Int,
    height: Int,
    density: Float,
    days: Int,
    mask: String?,
    today: Int,
    cover: Int,
    outline: Int,
    ink: Int,
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val step = width / PER_ROW.toFloat()
    val radius = 3 * density
    val gap = minOf(step * 1.5f, height / 2f)
    val alpha = if (mask == null) 102 else 255
    val filled = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cover }
    val empty = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = outline
        this.alpha = alpha
        style = Paint.Style.STROKE
        strokeWidth = density
    }
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    for (d in 1..days) {
        val i = d - 1
        val x = step * (i % PER_ROW) + step / 2
        val y = height / 2f + if (i < PER_ROW) -gap / 2 else gap / 2
        if (mask?.getOrNull(i) == '1') canvas.drawCircle(x, y, radius, filled) else canvas.drawCircle(x, y, radius - density / 2, empty)
        if (mask != null && d == today) canvas.drawCircle(x, y, radius + 2.75f * density, ring)
    }
    return bitmap
}
