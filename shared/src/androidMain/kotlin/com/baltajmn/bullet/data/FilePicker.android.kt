package com.baltajmn.bullet.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual object FilePicker {

    /** Set by MainActivity: opening and creating a document both need an Activity. */
    var createDocument: ((String) -> Unit)? = null
    var openDocument: (() -> Unit)? = null

    /**
     * The answer of the picker comes back to the Activity, which may be a new instance by then, so
     * what is waiting for it lives here and not in the Activity.
     *
     * ponytail: this survives a rotation, not the process being killed behind the picker. Then the
     * result arrives with nobody waiting and nothing happens, which is the same as cancelling.
     */
    private var waiting: ((Uri?) -> Unit)? = null

    private val scope = MainScope()

    actual val available: Boolean get() = createDocument != null && openDocument != null

    /** Called by MainActivity when the system answers. */
    fun onPicked(uri: Uri?) {
        val answer = waiting ?: return
        waiting = null
        answer(uri)
    }

    actual fun exportZip(
        suggestedName: String,
        write: (sink: (ByteArray) -> Unit) -> Unit,
        onDone: (PickResult) -> Unit,
    ) {
        val launch = createDocument ?: return onDone(PickResult.Failed)
        // One pick at a time: a second one would leave the first picker with nobody listening.
        if (waiting != null) return onDone(PickResult.Cancelled)
        waiting = { uri ->
            if (uri == null) {
                onDone(PickResult.Cancelled)
            } else {
                scope.launch {
                    // Never on the thread that draws: a diary of years is megabytes.
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            // "wt" and not the default "w": without truncating, a shorter backup
                            // written over a longer one leaves the old tail, and a zip is read from
                            // its end, so the file would open as the old one and be unreadable.
                            AndroidContext.value.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                                write { bytes -> out.write(bytes) }
                            } ?: error("no output stream")
                        }.isSuccess
                    }
                    onDone(if (ok) PickResult.Done else PickResult.Failed)
                }
            }
        }
        launch(suggestedName)
    }

    actual fun importFile(read: (source: (Int) -> ByteArray?) -> Unit, onDone: (PickResult) -> Unit) {
        val launch = openDocument ?: return onDone(PickResult.Failed)
        if (waiting != null) return onDone(PickResult.Cancelled)
        waiting = { uri ->
            if (uri == null) {
                onDone(PickResult.Cancelled)
            } else {
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            AndroidContext.value.contentResolver.openInputStream(uri)?.use { input ->
                                read { n ->
                                    val buffer = ByteArray(n)
                                    val got = input.read(buffer, 0, n)
                                    if (got <= 0) null else buffer.copyOf(got)
                                }
                            } ?: error("no input stream")
                        }.isSuccess
                    }
                    onDone(if (ok) PickResult.Done else PickResult.Failed)
                }
            }
        }
        launch()
    }
}
