package com.baltajmn.bullet.data

import platform.Foundation.NSDateComponents
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time
import com.baltajmn.bullet.i18n.S

private const val REMINDER_ID = "bobbin-reflection"

/**
 * One repeating calendar trigger (docs/tecnico.md 6.12): the text never changes, so iOS can repeat it on
 * its own and nothing has to wake up to book tomorrow's. Rebuilt on every start and every return to the
 * foreground, so a change of hour in Settings or of clock is always picked up.
 */
actual object Reminders {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    actual fun sync() {
        val settings = BobbinRepository.journal.settings
        center.removePendingNotificationRequestsWithIdentifiers(listOf(REMINDER_ID))
        if (!settings.reminderOn || permission() != NotifyPermission.GRANTED) return

        val content = UNMutableNotificationContent().apply {
            setTitle(S.reminderTitle)
            setBody(S.reminderBody)
            setSound(UNNotificationSound.defaultSound)
        }
        val at = NSDateComponents().apply {
            hour = settings.reminderHour.toLong()
            minute = settings.reminderMinute.toLong()
        }
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(
                REMINDER_ID,
                content,
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(at, repeats = true),
            ),
            null,
        )
    }

    /**
     * iOS only answers in a callback, and Settings needs the answer to paint its switch. The callback
     * arrives on a queue of its own, so waiting for it here cannot block it.
     */
    actual fun permission(): NotifyPermission {
        var status: Long? = null
        val done = dispatch_semaphore_create(0)
        center.getNotificationSettingsWithCompletionHandler { settings ->
            status = settings?.authorizationStatus
            dispatch_semaphore_signal(done)
        }
        dispatch_semaphore_wait(done, dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC.toLong()))
        return when (status) {
            UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral -> NotifyPermission.GRANTED
            UNAuthorizationStatusNotDetermined, null -> NotifyPermission.CAN_ASK
            else -> NotifyPermission.DENIED
        }
    }

    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
            dispatch_async(dispatch_get_main_queue()) { onResult(granted) }
        }
    }

    actual fun openSystemSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
