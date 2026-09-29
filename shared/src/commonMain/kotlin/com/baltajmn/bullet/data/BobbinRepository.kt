package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.addTrackerRow
import com.baltajmn.bullet.model.deleteTrackerRow
import com.baltajmn.bullet.model.monthOf
import com.baltajmn.bullet.model.renameTrackerRow
import com.baltajmn.bullet.model.moveTrackerRow
import com.baltajmn.bullet.model.restoreTrackerRow
import com.baltajmn.bullet.model.toggleTrackerDay
import com.baltajmn.bullet.model.trackerThread
import com.baltajmn.bullet.model.withTrackerPage
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.SCHEMA_VERSION
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.archiveCollection
import com.baltajmn.bullet.model.capture
import com.baltajmn.bullet.model.captureNote
import com.baltajmn.bullet.model.createCollection
import com.baltajmn.bullet.model.delete
import com.baltajmn.bullet.model.deleteCollection
import com.baltajmn.bullet.model.discard
import com.baltajmn.bullet.model.editText
import com.baltajmn.bullet.model.logicalDate
import com.baltajmn.bullet.model.migrate
import com.baltajmn.bullet.model.newId
import com.baltajmn.bullet.model.renameCollection
import com.baltajmn.bullet.model.reopen
import com.baltajmn.bullet.model.reorder
import com.baltajmn.bullet.model.restoreCollection
import com.baltajmn.bullet.model.restoreDeleted
import com.baltajmn.bullet.model.schedule
import com.baltajmn.bullet.model.toggleDone
import com.baltajmn.bullet.model.toggleSignifier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
/** What the undo line is offering to put back (docs/pantallas.md 5.8): its text says which. */
enum class UndoKind { ENTRY, COLLECTION, ROW }

/**
 * One undoable delete. [before] is the whole diary as it was, which is what [BobbinRepository.undo]
 * restores when nothing else has written since; [restore] is the narrower repair for when something
 * has, and each kind of delete brings its own (docs/tecnico.md 6.4, 6.7).
 */
class PendingUndo internal constructor(
    internal val before: Journal,
    internal val after: Journal,
    val kind: UndoKind,
    /** The entry an entry delete removed. Null when a whole collection went. */
    val deleted: Entry?,
    internal val restore: (Journal) -> Journal,
)

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

    /**
     * The last entitlement known (docs/tecnico.md 6.16). Purchases never block content: this is what
     * was last confirmed, kept until the store says otherwise.
     */
    var isPro by mutableStateOf(false)
        internal set

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

    private var loaded = false

    /**
     * What every door into the process calls: App, a receiver, a widget. Only the first one reads the
     * disk. Reading it again would swap the diary in memory for the one on disk, which can be one
     * write behind, and a recreated Activity or a reminder firing with the app open would lose it.
     */
    fun ensureLoaded() {
        if (loaded) return
        load()
        // Outside the diary, and absent in host tests, which have no Context for Prefs to read.
        isPro = runCatching { Prefs.bool(PREF_PRO) }.getOrDefault(false)
    }

    /**
     * What the store just said about Pro (docs/tecnico.md 6.16). Kept in Prefs, so without network
     * the last known answer stands, and passed to the widgets at once.
     */
    fun updatePro(active: Boolean) {
        if (active == isPro) return
        isPro = active
        runCatching { Prefs.setBool(PREF_PRO, active) }
        syncWidgets(journal, active, today())
    }

    /**
     * Reads the diary, falling back to the backup, and never writes over a file it could not read
     * or understand (docs/tecnico.md 6.14). [steps] is a parameter, not always [SCHEMA_STEPS], so
     * the whole mechanism can be proven with steps of its own before any real one ever ships.
     */
    fun load(files: JournalFiles = Storage, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS) {
        this.files = files
        loaded = true
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
        armUndo(before, UndoKind.ENTRY, original) { it.restoreDeleted(original) }
    }

    /**
     * Deleting a collection takes its entries with it (docs/tecnico.md 6.7), so the narrow repair has to
     * put back the collection and every entry that was in it, not one entry.
     */
    fun deleteCollection(id: String) {
        val gone = journal.collections.find { it.id == id } ?: return
        val its = journal.entries.filter { it.place == Place.InCollection(id) }
        val before = journal
        edit { j -> j.deleteCollection(id, now()) }
        armUndo(before, UndoKind.COLLECTION, deleted = null) { current ->
            its.fold(current.restoreCollection(gone)) { j, e -> j.restoreDeleted(e) }
        }
    }

    /** No confirmation for a delete, but [UNDO_MS] to take it back (docs/pantallas.md 5.8). */
    private fun armUndo(before: Journal, kind: UndoKind, deleted: Entry?, restore: (Journal) -> Journal) {
        pendingUndo = PendingUndo(before, journal, kind, deleted, restore)
        val token = ++undoToken
        scope.launch {
            delay(UNDO_MS)
            if (undoToken == token) pendingUndo = null
        }
    }

    /** Only the title and `updatedAt` change, never `createdAt`: the Index keeps its order (docs/tecnico.md 6.7). */
    fun renameCollection(id: String, title: String) = edit { j -> j.renameCollection(id, title, now()) }

    /** Archiving touches no entry at all (docs/tecnico.md 6.7). */
    fun archiveCollection(id: String, archived: Boolean) = edit { j -> j.archiveCollection(id, archived, now()) }

    /** Returns the new collection's id, or null when the title was empty (docs/tecnico.md 6.7). A tracker starts on this month's page. */
    fun createCollection(title: String, kind: CollectionKind = CollectionKind.NOTES): String? {
        var made: String? = null
        edit { j ->
            val id = collectionId(j)
            j.createCollection(title, now(), id, kind, monthOf(today()))?.also { made = id }
        }
        return made
    }

    private fun collectionId(j: Journal) = newId("c", j.collections.map { it.id }.toSet())

    /**
     * Every change to a tracker page goes through here, in one write: the blank page of [trackerPage] is
     * saved only when [change] actually changes something, so looking at a new month leaves nothing.
     */
    private fun onTrackerPage(page: BulletCollection, change: (Journal, String) -> Journal?): String? {
        var saved: String? = null
        edit { j ->
            val (base, id) = j.withTrackerPage(page, collectionId(j), now())
            change(base, id)?.also { saved = id }
        }
        return saved
    }

    fun toggleTrackerDay(page: BulletCollection, rowId: String, day: Int) {
        onTrackerPage(page) { j, id -> j.toggleTrackerDay(id, rowId, day, now()) }
    }

    fun addTrackerRow(page: BulletCollection, title: String): Boolean =
        onTrackerPage(page) { j, id -> j.addTrackerRow(id, title, newId("r", page.rows.map { it.id }.toSet()), now()) } != null

    fun renameTrackerRow(page: BulletCollection, rowId: String, title: String) {
        onTrackerPage(page) { j, id -> j.renameTrackerRow(id, rowId, title, now()) }
    }

    fun moveTrackerRow(page: BulletCollection, rowId: String, to: Int) {
        onTrackerPage(page) { j, id -> j.moveTrackerRow(id, rowId, to, now()) }
    }

    /** Without confirmation, with the undo line (docs/pantallas.md 10.2, `rowDeleted`). */
    fun deleteTrackerRow(page: BulletCollection, rowId: String) {
        val index = page.rows.indexOfFirst { it.id == rowId }
        if (index < 0) return
        val row = page.rows[index]
        val before = journal
        val id = onTrackerPage(page) { j, id -> j.deleteTrackerRow(id, rowId, now()) } ?: return
        armUndo(before, UndoKind.ROW, deleted = null) { it.restoreTrackerRow(id, row, index, now()) }
    }

    /** A tracker's title is its thread's: every page is renamed, so no month keeps an old name (docs/tecnico.md 6.18). */
    fun renameTracker(id: String, title: String) = edit { j ->
        var renamed: Journal? = null
        for (page in j.trackerThread(id)) renamed = (renamed ?: j).renameCollection(page.id, title, now()) ?: renamed
        renamed
    }

    /** Deleting a tracker deletes every page of its thread and nothing else (docs/tecnico.md 6.18). */
    fun deleteTracker(id: String) {
        val pages = journal.trackerThread(id)
        if (pages.isEmpty()) return
        val before = journal
        edit { j -> pages.reversed().fold(j) { acc, page -> acc.deleteCollection(page.id, now()) } }
        armUndo(before, UndoKind.COLLECTION, deleted = null) { current -> pages.fold(current) { acc, page -> acc.restoreCollection(page) } }
    }

    /**
     * Restores exactly [PendingUndo.before] when nothing else has changed the diary since the
     * delete, or runs [PendingUndo.restore] on the current one when something has: the entry back at
     * its old order and status, or the collection with everything that was in it (docs/tecnico.md 6.4,
     * 6.7). A second delete replaces [pendingUndo] outright
     * (docs/pantallas.md 5.8): the first one's own snapshot is gone, so there is nothing left for
     * its timer to clear.
     */
    fun undo() {
        val pending = pendingUndo ?: return
        pendingUndo = null
        val restored = if (journal === pending.after) pending.before else pending.restore(journal)
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

    /**
     * Leaves an empty diary on this phone (docs/pantallas.md 15.4, #33): `journal.json`, its `.bak`,
     * every named copy and the quarantine folder go, and an empty diary is written in their place, so
     * the app carries on in Hoy without a restart.
     *
     * It does **not** touch the Pro entitlement, which lives in `Prefs` and not in the diary: losing
     * something already paid for is the first thing Bobbin exists to avoid. It also drops any pending
     * undo, whose snapshot is exactly the diary that was asked to go.
     */
    fun wipe(): Job {
        pendingUndo = null
        undoToken++
        corrupt = false
        journal = Journal()
        // Deleting the files and writing the empty diary are one critical section: between the two,
        // another writer taking the lock would put a copy of the diary that was just deleted back on
        // disk. The returned [Job] is when the disk is actually in that state.
        return scope.launch {
            writeLock.withLock {
                withContext(Dispatchers.IO) { runCatching { files.wipe() } }
                written = null
                writeLocked()
            }
        }
        // Clearing widget.json so no widget keeps showing a deleted diary is #40, which is what creates
        // the file: there is nothing there to clear yet.
    }

    /** Writes the latest state now. Going to the background calls this. */
    suspend fun flush() = persist()

    /**
     * Applies a backup already read and shown in summary (docs/tecnico.md 6.9, step 5): the diary as it
     * is right now is kept as `pre-import` first, and the merge is worked out again against it, in case
     * something changed while the summary was open. Returns how many entries and collections changed.
     */
    suspend fun applyImport(incoming: Journal): Int {
        flush()
        val before = journal
        val result = merge(before, incoming)
        withContext(Dispatchers.IO) { files.keepCopy("pre-import", encode(before)) }
        pendingUndo = null
        undoToken++
        journal = result.journal
        flush()
        return result.added + result.updated
    }

    private suspend fun persist() = writeLock.withLock { writeLocked() }

    /** The write itself. The caller already holds [writeLock], so [wipe] can delete and rewrite as one. */
    private suspend fun writeLocked() {
        if (readOnly) return
        val snapshot = journal
        if (snapshot === written) return
        val ok = withContext(Dispatchers.IO) { runCatching { files.write(encode(snapshot)) }.isSuccess }
        if (ok) {
            written = snapshot
            saveFailed = false
            // After every good save and only then: the widgets never show what the disk does not have.
            syncWidgets(snapshot, isPro, today())
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
