package com.baltajmn.bullet.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.shared.R

private const val CHANNEL_ID = "bobbin-reflection"
private const val NOTIFICATION_ID = 1

/**
 * The day's nudge (docs/pantallas.md 19): the same fixed text every day, locked or not, which never
 * quotes a word of the diary. Then it books tomorrow's.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        BobbinRepository.ensureLoaded()
        notify(context)
        Reminders.sync()
    }

    private fun notify(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, S.reminderChannel, NotificationManager.IMPORTANCE_DEFAULT),
            )
        }

        // An explicit intent to our own Activity with the link in `data`: no intent-filter for the
        // scheme, so no other app can open a bobbin:// link (docs/tecnico.md 7).
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.setData(Uri.parse("$URL_SCHEME://review"))
        val tap = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.parseColor("#3F7A69"))
            .setContentTitle(S.reminderTitle)
            .setContentText(S.reminderBody)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

/** Alarms do not survive a reboot, a reinstall or a change of clock, so book it again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        BobbinRepository.ensureLoaded()
        Reminders.sync()
    }
}
