package com.baltajmn.bullet.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.baltajmn.bullet.model.DAY_START_DEFAULT
import com.baltajmn.bullet.model.logicalDate
import com.baltajmn.bullet.model.nextDayStart
import com.baltajmn.bullet.widget.MonthWidget
import com.baltajmn.bullet.widget.TodayWidget
import java.io.File
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

private const val DAY_REQUEST_CODE = 7002

private val widgetDir: File get() = Storage.rootOverride ?: AndroidContext.value.filesDir

/** filesDir, next to the diary, written the same atomic way: `.tmp` and rename (docs/tecnico.md 4.2). */
actual fun writeWidgetState(json: String) {
    val temp = File(widgetDir, "widget.tmp.json")
    temp.writeText(json)
    if (!temp.renameTo(File(widgetDir, "widget.json"))) error("could not move widget.json into place")
}

/** The only thing a widget is allowed to read. Missing or unreadable is null, and the widget paints zeros. */
fun readWidgetState(): WidgetState? {
    val text = runCatching { File(widgetDir, "widget.json").readText() }.getOrNull() ?: return null
    return runCatching { WidgetJson.decodeFromString(WidgetState.serializer(), text) }.getOrNull()
}

/** The logical day the widget side sees, with the hour the app last wrote (docs/tecnico.md 6.13). */
fun widgetToday(st: WidgetState?): LocalDate =
    logicalDate(Clock.System.now(), TimeZone.currentSystemDefault(), st?.dayStartHour ?: DAY_START_DEFAULT)

actual fun refreshWidgets() {
    val context = AndroidContext.value
    CoroutineScope(Dispatchers.Default).launch {
        TodayWidget().updateAll(context)
        MonthWidget().updateAll(context)
    }
    // updatePeriodMillis is 0: the one repaint nobody asks for is the change of day, and this alarm
    // does not wake the phone for it.
    val zone = TimeZone.currentSystemDefault()
    val hour = readWidgetState()?.dayStartHour ?: DAY_START_DEFAULT
    val at = nextDayStart(logicalDate(Clock.System.now(), zone, hour), hour, zone)
    context.getSystemService(AlarmManager::class.java)
        .set(AlarmManager.RTC, at.toEpochMilliseconds(), dayChange(context))
}

private fun dayChange(context: Context): PendingIntent = PendingIntent.getBroadcast(
    context,
    DAY_REQUEST_CODE,
    Intent(context, WidgetDayReceiver::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

/** The day changed: repaint, which applies `widgetView` to yesterday's file, and book the next one. */
class WidgetDayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        refreshWidgets()
    }
}
