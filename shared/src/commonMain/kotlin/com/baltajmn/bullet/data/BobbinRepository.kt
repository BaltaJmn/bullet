package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.SCHEMA_VERSION
import com.baltajmn.bullet.model.capture
import com.baltajmn.bullet.model.logicalDate
import com.baltajmn.bullet.model.newId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Single source of truth. The whole diary is one JSON file (docs/tecnico.md 6.14); state changes
 * at once on the caller's thread and a single writer persists the latest snapshot off it, so two
 * saves never cross and never rotate the backup twice.
 */
object BobbinRepository {

    var journal by mutableStateOf(Journal())
        private set

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

    /**
     * UI entry point for docs/tecnico.md 6.2 "Crear": reads the clock and a fresh id, then hands off
     * to [Journal.capture]. Returns whether it actually saved, so the capture row knows whether to
     * clear itself: "Con el campo vacío o solo prefijos, Intro no hace nada" (docs/pantallas.md 5.2).
     */
    @OptIn(ExperimentalTime::class)
    fun capture(input: String, place: Place, picked: Bullet? = null): Boolean {
        var saved = false
        edit { j ->
            j.capture(input, place, picked, now = Clock.System.now().toEpochMilliseconds(), newId = newId("e", j.entries.map { it.id }.toSet()))
                ?.also { saved = true }
        }
        return saved
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
