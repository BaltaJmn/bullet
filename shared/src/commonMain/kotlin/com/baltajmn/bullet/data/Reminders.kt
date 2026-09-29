package com.baltajmn.bullet.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/**
 * The next [hour]:[minute] strictly after [now], in local time (docs/tecnico.md 6.12). The day is added
 * to the local date and not to an instant, so across a change of clock it is still 21:00 on the wall.
 */
fun nextReminder(now: LocalDateTime, hour: Int, minute: Int): LocalDateTime {
    val at = LocalTime(hour, minute)
    return if (now.time < at) LocalDateTime(now.date, at) else LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), at)
}

enum class NotifyPermission { GRANTED, CAN_ASK, DENIED }

/**
 * One notification a day, off by default, with a fixed text that never quotes the diary
 * (docs/tecnico.md 6.12). Both platforms read the settings themselves, so every caller only says
 * "things changed".
 */
expect object Reminders {
    /** Re-books from the settings; cancels when the reminder is off or the permission is not there. */
    fun sync()
    fun permission(): NotifyPermission

    /** Only from the offer on Hoy or the switch in Settings, never at start-up. */
    fun requestPermission(onResult: (Boolean) -> Unit)
    fun openSystemSettings()
}
