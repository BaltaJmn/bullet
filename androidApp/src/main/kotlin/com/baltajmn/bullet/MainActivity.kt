package com.baltajmn.bullet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import com.baltajmn.bullet.data.AndroidContext
import com.baltajmn.bullet.data.FilePicker
import com.baltajmn.bullet.data.Lock
import com.baltajmn.bullet.data.StoreReview
import com.baltajmn.bullet.data.Reminders
import com.baltajmn.bullet.data.LIST_MIME
import com.baltajmn.bullet.data.Link
import com.baltajmn.bullet.data.RECEIVE_MAX_BYTES
import com.baltajmn.bullet.data.Route
import com.baltajmn.bullet.data.parseLink
import com.baltajmn.bullet.data.receiveLink

// FragmentActivity and not ComponentActivity: BiometricPrompt needs a fragment host.
class MainActivity : FragmentActivity() {

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission(), Reminders::onRequestResult)

    // The Activity only carries the answer across: what is waiting for it lives in FilePicker, which
    // outlives this instance when the system recreates it behind the picker.
    private val createBackup =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip"), FilePicker::onPicked)

    private val createBook =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf"), FilePicker::onPicked)

    private val openBackup =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), FilePicker::onPicked)

    private val createSyncFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json"), FilePicker::onPicked)

    private val openSyncFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), FilePicker::onPicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        Lock.host = WeakReference(this)
        StoreReview.host = WeakReference(this)
        Reminders.host = WeakReference(this)
        Reminders.launchRequest = { askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS) }
        FilePicker.createDocument = { name -> createBackup.launch(name) }
        FilePicker.createPdf = { name -> createBook.launch(name) }
        FilePicker.openDocument = { openBackup.launch(arrayOf("application/zip", "application/json", "*/*")) }
        FilePicker.createSync = { name -> createSyncFile.launch(name) }
        FilePicker.openSync = { openSyncFile.launch(arrayOf("application/json", "*/*")) }
        // A recreated Activity gets the same intent again: the link was already followed.
        if (savedInstanceState == null) linkOf(intent)?.let { Route.pending = it }
        setContent { App() }
    }

    // launchMode is singleTask: the notification or a widget tapped while the app is up arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        linkOf(intent)?.let { Route.pending = it }
    }

    /**
     * A `bobbin://` link from a widget or the notification, or a sent list opened or shared to Bobbin
     * (docs/tecnico.md 12.11). The file is read here, while the permission the other app gave lasts.
     */
    private fun linkOf(intent: Intent?): Link? {
        intent ?: return null
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.takeIf { it.scheme == "content" }
            Intent.ACTION_SEND -> if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            else -> null
        }
        // A file manager that opens with only the uri leaves the type out of the intent it delivers.
        val type = intent.type ?: uri?.let { runCatching { contentResolver.getType(it) }.getOrNull() }
        if (uri == null || type !in setOf(LIST_MIME, "application/json")) return parseLink(intent.dataString)
        val bytes = runCatching { contentResolver.openInputStream(uri)?.use(::readCapped) }.getOrNull() ?: return null
        return receiveLink(bytes.decodeToString())
    }

    /** Up to [RECEIVE_MAX_BYTES], or null for anything larger. `readNBytes` is API 33. */
    private fun readCapped(input: java.io.InputStream): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) return out.toByteArray()
            out.write(buffer, 0, n)
            if (out.size() > RECEIVE_MAX_BYTES) return null
        }
    }

    // The launchers belong to this instance's registry: leaving it in a process wide object would
    // hold the dead Activity and then throw when something tried to launch it.
    override fun onDestroy() {
        FilePicker.createDocument = null
        FilePicker.createPdf = null
        FilePicker.openDocument = null
        FilePicker.createSync = null
        FilePicker.openSync = null
        Reminders.launchRequest = null
        Reminders.host = null
        Lock.host = null
        StoreReview.host = null
        super.onDestroy()
    }
}
