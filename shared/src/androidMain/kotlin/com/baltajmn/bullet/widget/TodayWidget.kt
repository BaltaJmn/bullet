package com.baltajmn.bullet.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.material3.ColorScheme
import com.baltajmn.bullet.data.URL_SCHEME
import com.baltajmn.bullet.data.WidgetState
import com.baltajmn.bullet.data.readWidgetState
import com.baltajmn.bullet.data.widgetToday
import com.baltajmn.bullet.data.widgetView
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.shared.R
import com.baltajmn.bullet.ui.theme.Cover
import com.baltajmn.bullet.ui.theme.Dark
import com.baltajmn.bullet.ui.theme.Light
import kotlinx.datetime.LocalDate

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TodayWidget()
}

/**
 * Free (docs/pantallas.md 18.1): today's open, done and events as numbers next to their glyphs, and
 * nothing else. It only knows what widget.json says, and widget.json never carries a word of the diary.
 */
class TodayWidget : GlanceAppWidget() {

    // The smallest cell the launcher allows, and the widths and heights where a label or the review
    // line start to fit. A half painted line of text is worse than one line less.
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 110.dp), DpSize(150.dp, 110.dp), DpSize(110.dp, 150.dp), DpSize(150.dp, 150.dp)),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // The file may be days old if the app has not run: widgetView is what makes it right anyway.
        val raw = readWidgetState()
        val today = widgetToday(raw)
        val state = raw?.let { widgetView(it, today) }
        provideContent { Today(state, today) }
    }
}

/** Glance 1.1.1 has no day and night ColorProvider, so the scheme is read from the host's configuration. */
@Composable
internal fun widgetScheme(): ColorScheme {
    val context = LocalContext.current
    val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    return if (night) Dark else Light
}

/** The widget lives in the app's own package, which is the only Activity it may name. */
internal fun openLink(context: Context, link: String): Intent = Intent(Intent.ACTION_VIEW)
    .setComponent(ComponentName(context.packageName, "com.baltajmn.bullet.MainActivity"))
    .setData(Uri.parse("$URL_SCHEME://$link"))

@Composable
private fun Today(state: WidgetState?, today: LocalDate) {
    val context = LocalContext.current
    val scheme = widgetScheme()
    val ink = ColorProvider(scheme.onBackground)
    val muted = ColorProvider(scheme.onSurfaceVariant)
    val size = LocalSize.current
    val labels = size.width >= 150.dp

    Column(
        // 8 and not 16: from Android 12 the host already insets the widget (docs/pantallas.md 18).
        modifier = GlanceModifier.fillMaxSize().background(ColorProvider(scheme.background)).padding(8.dp)
            .clickable(actionStartActivity(openLink(context, "today?focus"))),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(dot(8, Cover.of(state?.cover ?: Cover.Sage.id).color.toArgb())), null)
            Spacer(GlanceModifier.width(6.dp))
            Text(S.widgetDate(today), style = TextStyle(color = muted, fontSize = 12.sp, fontWeight = FontWeight.Medium))
        }
        Spacer(GlanceModifier.height(4.dp))
        CountRow(R.drawable.glyph_task, state?.open ?: 0, if (labels) S.widgetOpen(state?.open ?: 0) else null, ink, muted)
        CountRow(R.drawable.glyph_done, state?.done ?: 0, if (labels) S.widgetDone(state?.done ?: 0) else null, ink, muted)
        CountRow(R.drawable.glyph_event, state?.events ?: 0, if (labels) S.widgetEvents(state?.events ?: 0) else null, ink, muted)
        if (state?.reviewPending == true && size.height >= 150.dp) {
            Spacer(GlanceModifier.defaultWeight())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(ImageProvider(R.drawable.glyph_migrated), null, GlanceModifier.size(24.dp), colorFilter = ColorFilter.tint(ink))
                Text(S.widgetReview, style = TextStyle(color = muted, fontSize = 12.sp))
            }
        }
    }
}

/** One of the three rows of 24: the glyph, the number and, when it fits, what the number counts. */
@Composable
private fun CountRow(glyph: Int, n: Int, label: String?, ink: ColorProvider, muted: ColorProvider) {
    Row(GlanceModifier.height(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(ImageProvider(glyph), null, GlanceModifier.size(24.dp), colorFilter = ColorFilter.tint(ink))
        Spacer(GlanceModifier.width(4.dp))
        Text("$n", style = TextStyle(color = ink, fontSize = 17.sp, fontWeight = FontWeight.Medium))
        if (label != null) {
            Spacer(GlanceModifier.width(6.dp))
            Text(label, maxLines = 1, style = TextStyle(color = muted, fontSize = 13.sp))
        }
    }
}

/** Glance has no canvas, so the cover's dot is drawn into a bitmap. Sizes in dp, drawn at 3x. */
internal fun dot(dp: Int, color: Int): Bitmap {
    val size = dp * 3
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawCircle(size / 2f, size / 2f, size / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    return bitmap
}
