package com.baltajmn.bullet.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.shared.R

class CaptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CaptureWidget()
}

/**
 * "Escribir" (docs/pantallas.md 23.14): a door to write, for everyone. No word of the diary, no number,
 * no widget.json. With Pro it opens the capture over the screen; without, Hoy with the keyboard up.
 */
class CaptureWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(DpSize(48.dp, 48.dp), DpSize(110.dp, 48.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Write() }
    }
}

/** Who opened the capture: the widget falls back to Hoy without Pro, where the tile asks for it. */
const val CAPTURE_FROM = "from"
const val CAPTURE_FROM_WIDGET = "widget"

/** The capture Activity lives in the app module: the widget names it by its class, as it does MainActivity. */
private fun captureIntent(context: Context): Intent = Intent()
    .setComponent(ComponentName(context.packageName, "com.baltajmn.bullet.CaptureActivity"))
    .putExtra(CAPTURE_FROM, CAPTURE_FROM_WIDGET)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

@Composable
private fun Write() {
    val context = LocalContext.current
    val scheme = widgetScheme()
    val ink = ColorProvider(scheme.onBackground)
    val muted = ColorProvider(scheme.onSurfaceVariant)
    val modifier = GlanceModifier.fillMaxSize().background(ColorProvider(scheme.background))
        .clickable(actionStartActivity(captureIntent(context)))
    if (LocalSize.current.width >= 110.dp) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(ImageProvider(R.drawable.glyph_task), null, GlanceModifier.size(24.dp), colorFilter = ColorFilter.tint(ink))
            Spacer(GlanceModifier.width(6.dp))
            Text(S.captureWidgetLabel, maxLines = 1, style = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.Medium))
        }
    } else {
        Column(modifier, verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(ImageProvider(R.drawable.glyph_task), null, GlanceModifier.size(24.dp), colorFilter = ColorFilter.tint(ink))
            Text(S.appName, maxLines = 1, style = TextStyle(color = muted, fontSize = 11.sp))
        }
    }
}
