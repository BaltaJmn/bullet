package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

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

    // Default, not Main: persisting never touches Compose state, and a Main dispatcher is not
    // guaranteed to exist outside a real app (host tests have none).
    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    private val writeLock = Mutex()
    private var files: JournalFiles = Storage
    private var written: Journal? = null

    /** Reads the diary, falling back to the backup, and never writes over a file it could not read. */
    fun load(files: JournalFiles = Storage) {
        this.files = files
        val main = files.read()
        var loaded = decode(main)
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

    /** [change] returning null leaves the diary untouched: the caller decided there was nothing to do. */
    fun edit(change: (Journal) -> Journal?) {
        val next = change(journal) ?: return
        journal = next
        scope.launch { persist() }
    }

    /** Writes the latest state now. Going to the background calls this. */
    suspend fun flush() = persist()

    private suspend fun persist() = writeLock.withLock {
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
