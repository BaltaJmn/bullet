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
