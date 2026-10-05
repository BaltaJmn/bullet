package com.baltajmn.bullet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Settings
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The one file in the user's own cloud (docs/tecnico.md 12.7). */
const val SYNC_NAME = "bobbin-sync.json"

/** The named copy that holds the last version both sides agreed on. */
internal const val SYNC_BASE = "sync-base"

const val PREF_SYNC_ON = "syncOn"
const val PREF_SYNC_LAST = "syncLast"

/** What the cloud copy is right now. */
sealed interface Remote {
    /** Nothing written there yet. */
    data object Missing : Remote

    /** There, or maybe there, but not readable yet (iCloud still gathering or downloading, offline). */
    data object Pending : Remote

    data class Text(val text: String) : Remote
}

/**
 * The cloud copy on each platform: iCloud Drive's container on iOS, a file picked with the system
 * picker on Android. Reading and writing block, so they are only called off the main thread.
 */
expect object SyncFile {
    /** Android: the picker, creating the file or opening one; iOS: whether iCloud Drive answers. */
    fun connect(create: Boolean, onDone: (Boolean) -> Unit)
    fun disconnect()
    fun read(): Remote
    fun write(text: String): Boolean

    /** iOS calls [onChange] when iCloud brings a new version; Android has nothing to watch. */
    fun watch(onChange: () -> Unit)
}

/** What one sync has to do (docs/tecnico.md 12.7). */
sealed interface SyncStep {
    data object Nothing : SyncStep
    data object Push : SyncStep
    data class Pull(val remote: Journal) : SyncStep
    data class Conflict(val remote: Journal) : SyncStep
}

/** The lock and the reminder belong to each phone: one may have no screen lock, or no permission. */
internal fun Journal.withDeviceSettings(device: Settings): Journal = copy(
    settings = settings.copy(
        reminderOn = device.reminderOn,
        reminderHour = device.reminderHour,
        reminderMinute = device.reminderMinute,
        reminderOffered = device.reminderOffered,
        lockOn = device.lockOn,
    ),
)

/** What two diaries are compared on: everything but [withDeviceSettings]. */
internal fun Journal.syncedPart(): Journal = withDeviceSettings(Settings())

/**
 * The table of docs/tecnico.md 12.7. Without a [base] (the first sync on this device) only an empty
 * diary takes the cloud's without asking; anything else with something different asks.
 */
fun syncStep(local: Journal, remote: Journal?, base: Journal?): SyncStep {
    if (remote == null) return SyncStep.Push
    val l = local.syncedPart()
    val r = remote.syncedPart()
    val b = base?.syncedPart()
    return when {
        l == r -> SyncStep.Nothing
        b == null -> if (local.entries.isEmpty() && local.collections.isEmpty()) SyncStep.Pull(remote) else SyncStep.Conflict(remote)
        r == b -> SyncStep.Push
        l == b -> SyncStep.Pull(remote)
        else -> SyncStep.Conflict(remote)
    }
}

/** Both sides changed: the dialog of docs/pantallas.md 23.7 holds the two until someone chooses. */
class SyncConflict(val local: Journal, val remote: Journal)

/**
 * Sync (Pro, docs/tecnico.md 12.7): at launch, on every return to the app and after every save. Never
 * a silent merge: when both sides changed it stops and asks, and nothing is written until then.
 */
object Sync {
    var on by mutableStateOf(runCatching { Prefs.bool(PREF_SYNC_ON) }.getOrDefault(false))
        private set

    /** Epoch milliseconds of the last time both sides agreed, for the row of Settings. */
    var lastAt by mutableStateOf(runCatching { Prefs.string(PREF_SYNC_LAST)?.toLongOrNull() }.getOrNull())
        private set

    var conflict by mutableStateOf<SyncConflict?>(null)
        private set

    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    private val lock = Mutex()
    private var soon: Job? = null
    private var started = false

    /** App calls this once: after every save, and whenever iOS says the cloud copy changed. */
    fun start() {
        if (started) return
        started = true
        BobbinRepository.onSaved = { later() }
        SyncFile.watch { later() }
        later()
    }

    /** Several saves in a row make one sync. */
    fun later() {
        soon?.cancel()
        soon = scope.launch {
            delay(1_500)
            run()
        }
    }

    /** Switching it on: Android picks the file first ([create] or open one); iOS needs iCloud Drive. */
    fun turnOn(create: Boolean, onDone: (Boolean) -> Unit) {
        SyncFile.connect(create) { ok ->
            if (ok) {
                // A new file or a new device starts from nothing agreed: the first sync decides.
                BobbinRepository.setSyncBase(null)
                switchTo(true)
                later()
            }
            onDone(ok)
        }
    }

    /** Off on this device; the cloud copy stays where it is. Wiping the diary does this too. */
    fun turnOff() {
        switchTo(false)
        conflict = null
        SyncFile.disconnect()
        BobbinRepository.setSyncBase(null)
    }

    suspend fun run() = lock.withLock {
        if (!on || !BobbinRepository.isPro || conflict != null) return@withLock
        val remote = when (val r = withContext(Dispatchers.IO) { SyncFile.read() }) {
            Remote.Pending -> return@withLock
            Remote.Missing -> null
            // A copy this build cannot read (damaged, or from a newer Bobbin) is left alone, never overwritten.
            is Remote.Text -> (parseBackup(r.text, allowEmpty = true) as? ReadBackup.Ok)?.journal ?: return@withLock
        }
        BobbinRepository.flush()
        val local = BobbinRepository.journal
        val base = BobbinRepository.syncBase()
        when (val step = syncStep(local, remote, base)) {
            SyncStep.Nothing -> agreed(local)
            SyncStep.Push -> push(local)
            is SyncStep.Pull -> if (BobbinRepository.replaceFromSync(local, step.remote)) {
                agreed(BobbinRepository.journal)
                Reminders.sync()
            }
            is SyncStep.Conflict -> conflict = SyncConflict(local, step.remote)
        }
    }

    /** "Usar este teléfono": this device's diary goes up as it is now. */
    fun keepThis() = resolve { push(BobbinRepository.journal) }

    /** "Usar el otro": the cloud's comes down, with this one kept as `pre-sync`. */
    fun keepOther() = resolve { c ->
        if (BobbinRepository.replaceFromSync(BobbinRepository.journal, c.remote)) {
            agreed(BobbinRepository.journal)
            Reminders.sync()
        }
    }

    /** Closing the dialog decides nothing: the next sync finds the same two sides and asks again. */
    fun dismissConflict() {
        conflict = null
    }

    /** What "Juntar los dos" would do, for the summary shown before it does it (docs/pantallas.md 15.2). */
    fun mergePreview(c: SyncConflict): MergeResult = merge(BobbinRepository.journal, c.remote)

    /** "Juntar los dos", after the summary: the merge of 6.9 here, as `pre-sync`, and then up. */
    fun mergeBoth() = resolve { c ->
        val merged = merge(BobbinRepository.journal, c.remote).journal
        if (BobbinRepository.replaceFromSync(BobbinRepository.journal, merged)) push(BobbinRepository.journal)
    }

    private fun resolve(action: suspend (SyncConflict) -> Unit) {
        val c = conflict ?: return
        scope.launch {
            lock.withLock { action(c) }
            conflict = null
        }
    }

    private suspend fun push(j: Journal) {
        if (withContext(Dispatchers.IO) { SyncFile.write(BobbinRepository.encode(j)) }) agreed(j)
    }

    @OptIn(ExperimentalTime::class)
    private fun agreed(j: Journal) {
        BobbinRepository.setSyncBase(j)
        val now = Clock.System.now().toEpochMilliseconds()
        lastAt = now
        runCatching { Prefs.setString(PREF_SYNC_LAST, now.toString()) }
    }

    private fun switchTo(value: Boolean) {
        on = value
        runCatching { Prefs.setBool(PREF_SYNC_ON, value) }
    }
}
