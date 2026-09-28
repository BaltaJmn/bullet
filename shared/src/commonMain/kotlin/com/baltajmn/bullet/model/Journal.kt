package com.baltajmn.bullet.model

import kotlin.random.Random
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// = 1 + SCHEMA_STEPS.size (SCHEMA_STEPS lives in data/Storage.kt, #15, docs/tecnico.md 6.14).
// It stays a plain constant, not a computed one: model/ never depends on data/.
const val SCHEMA_VERSION = 1
const val REMINDER_DEFAULT_HOUR = 21
const val REMINDER_DEFAULT_MINUTE = 0

@Serializable
enum class CollectionKind {
    @SerialName("notes") NOTES,
    @SerialName("tracker") TRACKER,
}

@Serializable
data class TrackerRow(val id: String, val title: String, val days: Set<Int> = emptySet())

@Serializable
data class BulletCollection(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val archived: Boolean = false,
    val kind: CollectionKind = CollectionKind.NOTES,
    /** The previous collection of the thread this one continues (6.7, 6.18, 12.1). */
    val threadFrom: String? = null,
    /** TRACKER only: the month this page belongs to (6.18). */
    val month: YearMonth? = null,
    /** TRACKER only. */
    val rows: List<TrackerRow> = emptyList(),
    val icon: String? = null,
    val theme: String? = null,
)

@Serializable
data class Settings(
    /** 0..6; out of range reads as [DAY_START_DEFAULT]. */
    val dayStartHour: Int = DAY_START_DEFAULT,
    /** ISO 1..7; null means the system's own first day of the week. */
    val firstDayOfWeek: Int? = null,
    val reminderOn: Boolean = false,
    val reminderHour: Int = REMINDER_DEFAULT_HOUR,
    val reminderMinute: Int = REMINDER_DEFAULT_MINUTE,
    val reminderOffered: Boolean = false,
    val lockOn: Boolean = false,
    val cover: String = "sage",
    val paper: String = "dotted",
    /** The month whose Future Log review was finished (6.5). */
    val futureSeen: YearMonth? = null,
    val questionsUsed: List<Int> = emptyList(),
    /** The day each new notebook started (v1.1, 12.2). */
    val notebooks: List<LocalDate> = emptyList(),
)

/** v1.2, 12.9. */
@Serializable
data class IndexMark(val icon: String? = null, val theme: String? = null)

/**
 * The pure container for the whole diary: no disk, no platform, no clock. [BobbinRepository]
 * (#14) is the only thing allowed to read or write one from storage.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Journal(
    @EncodeDefault val schemaVersion: Int = SCHEMA_VERSION,
    @EncodeDefault val entries: List<Entry> = emptyList(),
    @EncodeDefault val collections: List<BulletCollection> = emptyList(),
    val settings: Settings = Settings(),
    /** v1.2, 12.9; key "yyyy-MM". */
    val months: Map<String, IndexMark> = emptyMap(),
)

val JournalJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
    explicitNulls = false
}

/** A fresh id such as "e-1a2b3c4d", retried until it is not already in [taken]. */
fun newId(prefix: String, taken: Set<String> = emptySet()): String {
    while (true) {
        val id = "$prefix-" + Random.nextInt().toUInt().toString(16).padStart(8, '0')
        if (id !in taken) return id
    }
}

// --- Estados y signifiers (docs/tecnico.md 6.3), used by ui/EntrySheet.kt (#22) --------------------

/** OPEN to DONE and back; anything else is rejected (docs/tecnico.md 6.3's table). */
fun Journal.toggleDone(id: String, now: Long): Journal? {
    val e = entries.find { it.id == id && !it.gone } ?: return null
    if (e.bullet != Bullet.TASK || e.status !in setOf(TaskStatus.OPEN, TaskStatus.DONE)) return null
    return copy(entries = entries.map { if (it.id == id) it.toggleDone().copy(updatedAt = now) else it })
}

/** DONE or IRRELEVANT back to OPEN. A MIGRATED or SCHEDULED task never reopens: its copy already exists. */
fun Journal.reopen(id: String, now: Long): Journal? {
    val e = entries.find { it.id == id && !it.gone } ?: return null
    if (e.bullet != Bullet.TASK || e.status !in setOf(TaskStatus.DONE, TaskStatus.IRRELEVANT)) return null
    return copy(entries = entries.map { if (it.id == id) it.copy(status = TaskStatus.OPEN, updatedAt = now) else it })
}

/** Puts or removes [s]. Any entry, not only a task, and any status: the sheet offers it everywhere. */
fun Journal.toggleSignifier(id: String, s: Signifier, now: Long): Journal? {
    val e = entries.find { it.id == id && !it.gone } ?: return null
    val signifiers = if (s in e.signifiers) e.signifiers - s else e.signifiers + s
    return copy(entries = entries.map { if (it.id == id) it.copy(signifiers = signifiers, updatedAt = now) else it })
}

/**
 * [String.oneLine] and [limitEdit] against the entry's own previous text, same as typing it
 * (docs/pantallas.md 5.4): pasting something far longer than [TEXT_LIMIT] still clamps. Empty after
 * `trim` does not save, so the caller's field keeps showing the entry exactly as it was.
 */
fun Journal.editText(id: String, text: String, now: Long): Journal? {
    val e = entries.find { it.id == id && !it.gone } ?: return null
    val oneLined = text.oneLine()
    val edited = limitEdit(e.text, oneLined, oneLined.length).text.trim()
    if (edited.isEmpty()) return null
    return copy(entries = entries.map { if (it.id == id) it.copy(text = edited, updatedAt = now) else it })
}

/**
 * Removes [id] (docs/tecnico.md 6.4). Nothing pointing at it: dropped outright. Something's `from`
 * still pointing at it: kept as a skeleton (`gone = true`, no text, no signifiers) so that entry
 * keeps its link and its [migrationCount]. Deleting the last thing that pointed at a skeleton removes
 * it too, in the same change, cascading through a chain of skeletons if that empties another one.
 */
fun Journal.delete(id: String, now: Long): Journal? {
    entries.find { it.id == id && !it.gone } ?: return null
    val isReferenced = entries.any { it.id != id && it.from == id }
    val afterRemoval = if (isReferenced) {
        entries.map { if (it.id == id) it.copy(gone = true, text = "", signifiers = emptySet(), updatedAt = now) else it }
    } else {
        entries.filterNot { it.id == id }
    }
    return copy(entries = afterRemoval.dropOrphanSkeletons())
}

private fun List<Entry>.dropOrphanSkeletons(): List<Entry> {
    var current = this
    while (true) {
        val referenced = current.mapNotNull { it.from }.toSet()
        val next = current.filterNot { it.gone && it.id !in referenced }
        if (next.size == current.size) return next
        current = next
    }
}

/** The entry a migration or a schedule landed, if it is still there (docs/tecnico.md 6.3): the non skeleton entry whose `from` is [id]. */
fun copyOf(j: Journal, id: String): Entry? = j.entries.find { it.from == id && !it.gone }
