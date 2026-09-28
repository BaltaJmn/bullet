package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.SCHEMA_VERSION
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.capture
import com.baltajmn.bullet.model.captureNote
import com.baltajmn.bullet.model.delete
import com.baltajmn.bullet.model.discard
import com.baltajmn.bullet.model.editText
import com.baltajmn.bullet.model.logicalDate
import com.baltajmn.bullet.model.migrate
import com.baltajmn.bullet.model.newId
import com.baltajmn.bullet.model.reopen
import com.baltajmn.bullet.model.reorder
import com.baltajmn.bullet.model.restoreDeleted
import com.baltajmn.bullet.model.schedule
import com.baltajmn.bullet.model.toggleDone
import com.baltajmn.bullet.model.toggleSignifier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** How long Deshacer stays offered after a delete (docs/pantallas.md 5.8, docs/tecnico.md 6.4). */
const val UNDO_MS = 5_000L

/**
 * What a delete leaves behind for Deshacer (docs/tecnico.md 6.4): [before] is the exact prior
 * snapshot, for the common case where nothing else changed meanwhile; [deleted] is the entry
 * itself, in case something did and only it needs putting back.
 */
class PendingUndo internal constructor(internal val before: Journal, internal val after: Journal, val deleted: Entry)

/**
 * Single source of truth. The whole diary is one JSON file (docs/tecnico.md 6.14); state changes
 * at once on the caller's thread and a single writer persists the latest snapshot off it, so two
 * saves never cross and never rotate the backup twice.
 */
object BobbinRepository {

    var journal by mutableStateOf(Journal())
        private set

    /** Set right after a delete, for the line of docs/pantallas.md 5.8; null once [UNDO_MS] passes or [undo] runs. */
    var pendingUndo by mutableStateOf<PendingUndo?>(null)
        private set

    private var undoToken = 0

    /** The last write failed. Today shows it; the next change retries. */
    var saveFailed by mutableStateOf(false)
        private set

    /** Neither the file nor the backup could be read. Both were moved aside and the diary starts empty. */
    var corrupt by mutableStateOf(false)
        private set

    /** The file's schemaVersion is higher than this build knows. The file was never touched. */
    var updateNeeded by mutableStateOf(false)
        private set

    /** A schema conversion step threw. The file is exactly as it was, in its old format. */
    var migrationFailed by mutableStateOf(false)
        private set

    // Default, not Main: persisting never touches Compose state, and a Main dispatcher is not
    // guaranteed to exist outside a real app (host tests have none).
    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    private val writeLock = Mutex()
    private var files: JournalFiles = Storage

    /** Set on updateNeeded or migrationFailed: nothing can be written until the next successful load. */
    private var readOnly = false
    private var written: Journal? = null

    /**
     * Reads the diary, falling back to the backup, and never writes over a file it could not read
     * or understand (docs/tecnico.md 6.14). [steps] is a parameter, not always [SCHEMA_STEPS], so
     * the whole mechanism can be proven with steps of its own before any real one ever ships.
     */
    fun load(files: JournalFiles = Storage, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS) {
        this.files = files
        updateNeeded = false
        migrationFailed = false
        readOnly = false
        pendingUndo = null

        val main = files.read()
        val rawVersion = main?.let(::peekSchemaVersion)

        if (rawVersion != null && rawVersion > SCHEMA_VERSION) {
            updateNeeded = true
            readOnly = true
            return
        }

        var loaded = if (rawVersion != null && rawVersion < SCHEMA_VERSION) {
            migrateOldFormat(files, main, steps)
        } else {
            decode(main)
        }
        if (migrationFailed) return

        var previous: String? = null
        if (loaded == null) {
            previous = files.readPrevious()
            loaded = decode(previous)
            if (loaded != null) runCatching { files.restoreMain(previous!!) }
        }
        when {
            loaded != null -> {
                journal = loaded
                corrupt = false
            }
            main == null && previous == null -> {
                journal = Journal()
                corrupt = false
            }
            else -> {
                files.quarantine()
                journal = Journal()
                corrupt = true
            }
        }
        written = journal
    }

    fun dismissCorrupt() {
        corrupt = false
    }

    /** "Today" everywhere in the app (docs/tecnico.md 6.1). `App.kt` recalls this on every `ON_RESUME`. */
    @OptIn(ExperimentalTime::class)
    fun today(): LocalDate = logicalDate(Clock.System.now(), TimeZone.currentSystemDefault(), journal.settings.dayStartHour)

    @OptIn(ExperimentalTime::class)
    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * UI entry point for docs/tecnico.md 6.2 "Crear": reads the clock and a fresh id, then hands off
     * to [Journal.capture]. Returns whether it actually saved, so the capture row knows whether to
     * clear itself: "Con el campo vacío o solo prefijos, Intro no hace nada" (docs/pantallas.md 5.2).
     */
    fun capture(input: String, place: Place, picked: Bullet? = null): Boolean {
        var saved = false
        edit { j ->
            j.capture(input, place, picked, now = now(), newId = newId("e", j.entries.map { it.id }.toSet()))
                ?.also { saved = true }
        }
        return saved
    }

    // --- Estados, signifiers, migrar, programar, descartar, editar y borrar (docs/tecnico.md 6.3,
    // 6.4), the UI entry points for ui/EntrySheet.kt (#22). Each reads the clock and a fresh id only
    // when the model function underneath actually needs one, then hands off to [Journal].

    fun toggleDone(id: String) = edit { j -> j.toggleDone(id, now()) }
    fun reopen(id: String) = edit { j -> j.reopen(id, now()) }
    fun toggleSignifier(id: String, s: Signifier) = edit { j -> j.toggleSignifier(id, s, now()) }
    fun discard(id: String) = edit { j -> j.discard(id, now()) }

    /** Empty after `trim` leaves the entry exactly as it was (docs/pantallas.md 5.4). */
    fun editText(id: String, text: String) = edit { j -> j.editText(id, text, now()) }

    /** The reflection note of a review (docs/tecnico.md 6.6). Returns whether it saved, like [capture]. */
    fun captureNote(text: String, place: Place): Boolean {
        var saved = false
        edit { j ->
            j.captureNote(text, place, now = now(), newId = newId("e", j.entries.map { it.id }.toSet()))
                ?.also { saved = true }
        }
        return saved
    }

    /** The one way the UI changes an ajuste: `settings { it.copy(futureSeen = m) }`. */
    fun settings(change: (Settings) -> Settings) = edit { j -> j.copy(settings = change(j.settings)) }

    /** Dragging an entry (gesture 4, docs/pantallas.md 4, #23): nothing is written until the finger lifts. */
    fun reorder(place: Place, ids: List<String>) = edit { j -> j.reorder(place, ids, now()) }

    /**
     * Removing the original of a migration never breaks its copy: [Journal.delete] keeps a skeleton
     * instead of dropping it outright (docs/tecnico.md 6.4). No confirmation, but [UNDO_MS] to
     * [undo] it, tracked in [pendingUndo] for the line of docs/pantallas.md 5.8.
     */
    fun delete(id: String) {
        val original = journal.entries.find { it.id == id && !it.gone } ?: return
        val before = journal
        edit { j -> j.delete(id, now()) }
        pendingUndo = PendingUndo(before, journal, original)
        val token = ++undoToken
        scope.launch {
            delay(UNDO_MS)
            if (undoToken == token) pendingUndo = null
        }
    }

    /**
     * Restores exactly [PendingUndo.before] when nothing else has changed the diary since the
     * delete, or just [PendingUndo.deleted] back into the current one, at its old order and status,
     * when something has (docs/tecnico.md 6.4). A second delete replaces [pendingUndo] outright
     * (docs/pantallas.md 5.8): the first one's own snapshot is gone, so there is nothing left for
     * its timer to clear.
     */
    fun undo() {
        val pending = pendingUndo ?: return
        pendingUndo = null
        val restored = if (journal === pending.after) pending.before else journal.restoreDeleted(pending.deleted)
        journal = restored
        scope.launch { persist() }
    }

    /** True only when [Journal.migrate] actually accepted [to] (docs/pantallas.md 5.7): the sheet uses it to know whether to close. */
    fun migrate(id: String, to: Place): Boolean {
        var moved = false
        edit { j -> j.migrate(id, to, today(), now(), newId("e", j.entries.map { it.id }.toSet()))?.also { moved = true } }
        return moved
    }

    /** Same as [migrate], towards `Place.Future(month, day)`. */
    fun schedule(id: String, month: YearMonth, day: Int?): Boolean {
        var scheduled = false
        edit { j -> j.schedule(id, month, day, today(), now(), newId("e", j.entries.map { it.id }.toSet()))?.also { scheduled = true } }
        return scheduled
    }

    /**
     * Runs [steps] on the raw JSON, keeps a "pre-migration" copy before touching anything, and
     * writes the converted diary back so the next load sees the current schema directly. A step
     * that throws, or a result that will not decode, leaves journal.json exactly as it was: nothing
     * is written before every step and the final decode have already succeeded.
     */
    private fun migrateOldFormat(files: JournalFiles, raw: String, steps: List<(JsonObject) -> JsonObject>): Journal? {
        return try {
            files.keepCopy("pre-migration", raw)
            val rawObject = Json.parseToJsonElement(raw).jsonObject
            val migrated = migrateSchema(rawObject, steps)
            val decoded = JournalJson.decodeFromJsonElement(Journal.serializer(), migrated)
            files.write(encode(decoded))
            decoded
        } catch (e: Exception) {
            runCatching { files.restoreMain(raw) }
            migrationFailed = true
            readOnly = true
            null
        }
    }

    private fun peekSchemaVersion(text: String): Int? = try {
        Json.parseToJsonElement(text).jsonObject["schemaVersion"]?.jsonPrimitive?.intOrNull ?: SCHEMA_VERSION
    } catch (e: Exception) {
        null
    }

    /** [change] returning null leaves the diary untouched: the caller decided there was nothing to do. */
    fun edit(change: (Journal) -> Journal?) {
        val next = change(journal) ?: return
        journal = next
        scope.launch { persist() }
    }

    /** Writes the latest state now. Going to the background calls this. */
    suspend fun flush() = persist()

    private suspend fun persist() = writeLock.withLock {
        if (readOnly) return@withLock
        val snapshot = journal
        if (snapshot === written) return@withLock
        val ok = withContext(Dispatchers.IO) { runCatching { files.write(encode(snapshot)) }.isSuccess }
        if (ok) {
            written = snapshot
            saveFailed = false
        } else {
            saveFailed = true
        }
    }

    internal fun encode(journal: Journal): String = JournalJson.encodeToString(Journal.serializer(), journal)

    private fun decode(text: String?): Journal? = text?.let {
        try {
            JournalJson.decodeFromString(Journal.serializer(), it)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
