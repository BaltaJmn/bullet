package com.baltajmn.bullet.data

import android.content.Intent
import android.net.Uri

private const val PREF_SYNC_URI = "syncUri"

/**
 * A file picked or created with the system picker, Drive included, with no account inside the app
 * (docs/tecnico.md 12.7). The grant is made persistable so it survives a restart.
 */
actual object SyncFile {
    private val resolver get() = AndroidContext.value.contentResolver
    private const val FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private fun uri(): Uri? = Prefs.string(PREF_SYNC_URI)?.let(Uri::parse)

    actual fun connect(create: Boolean, onDone: (Boolean) -> Unit) = FilePicker.pickSync(create) { uri ->
        val ok = uri != null && runCatching { resolver.takePersistableUriPermission(uri, FLAGS) }.isSuccess
        if (ok) Prefs.setString(PREF_SYNC_URI, uri.toString())
        onDone(ok)
    }

    actual fun disconnect() {
        uri()?.let { runCatching { resolver.releasePersistableUriPermission(it, FLAGS) } }
        Prefs.setString(PREF_SYNC_URI, null)
    }

    /** A file just created is empty: nothing written yet. One that cannot be opened now (offline) waits. */
    actual fun read(): Remote {
        val uri = uri() ?: return Remote.Pending
        val text = runCatching { resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
            ?: return Remote.Pending
        return if (text.isBlank()) Remote.Missing else Remote.Text(text)
    }

    actual fun write(text: String): Boolean {
        val uri = uri() ?: return false
        // "wt": a shorter diary written over a longer one must not keep the old tail.
        return runCatching { resolver.openOutputStream(uri, "wt")?.use { it.write(text.encodeToByteArray()) } ?: error("no stream") }.isSuccess
    }

    actual fun watch(onChange: () -> Unit) = Unit
}
