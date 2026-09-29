package com.baltajmn.bullet.data

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val REQUEST_CODE = 7001

actual object Reminders {

    /** Set by MainActivity: only an Activity can ask for POST_NOTIFICATIONS or know if it may ask again. */
    var host: WeakReference<ComponentActivity>? = null
    var launchRequest: (() -> Unit)? = null
    private var waiting: ((Boolean) -> Unit)? = null

    actual fun sync() {
        val context = AndroidContext.value
        val manager = context.getSystemService(AlarmManager::class.java)
        manager.cancel(pendingIntent(context))
        val settings = BobbinRepository.journal.settings
        if (!settings.reminderOn || permission() != NotifyPermission.GRANTED) return

        val zone = TimeZone.currentSystemDefault()
        val at = nextReminder(Clock.System.now().toLocalDateTime(zone), settings.reminderHour, settings.reminderMinute)
        // Inexact on purpose: an exact alarm needs SCHEDULE_EXACT_ALARM, and a nudge to go over the day
        // can arrive a few minutes late without anyone noticing (docs/tecnico.md 6.12).
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant(zone).toEpochMilliseconds(), pendingIntent(context))
    }

    actual fun permission(): NotifyPermission {
        val context = AndroidContext.value
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return if (NotificationManagerCompat.from(context).areNotificationsEnabled()) NotifyPermission.GRANTED else NotifyPermission.DENIED
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return NotifyPermission.GRANTED
        }
        // Once asked, the system only shows its dialog again while it says a rationale is due.
        val rationale = host?.get()?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS) } == true
        return if (!Prefs.bool(PREF_NOTIFY_ASKED) || rationale) NotifyPermission.CAN_ASK else NotifyPermission.DENIED
    }

    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        val launch = launchRequest
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || launch == null) {
            onResult(permission() == NotifyPermission.GRANTED)
            return
        }
        waiting = onResult
        launch()
    }

    /** MainActivity's permission launcher lands here. */
    fun onRequestResult(granted: Boolean) {
        Prefs.setBool(PREF_NOTIFY_ASKED, true)
        waiting?.invoke(granted)
        waiting = null
    }

    actual fun openSystemSettings() {
        val context = AndroidContext.value
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
