package com.baltajmn.bullet.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.closeFile
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.fileHandleForWritingAtPath
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual object FilePicker {

    private val scope = MainScope()

    actual val available: Boolean get() = rootController != null

    actual fun exportZip(
        suggestedName: String,
        write: (sink: (ByteArray) -> Unit) -> Unit,
        onDone: (PickResult) -> Unit,
    ) {
        val root = rootController ?: return onDone(PickResult.Failed)
        if (held != null) return onDone(PickResult.Cancelled)
        val path = NSTemporaryDirectory() + suggestedName
        scope.launch {
            val written = withContext(Dispatchers.IO) {
                val fm = NSFileManager.defaultManager
                fm.removeItemAtPath(path, null)
                fm.createFileAtPath(path, null, null)
                val handle = NSFileHandle.fileHandleForWritingAtPath(path)
                if (handle == null) {
                    false
                } else {
                    var ok = true
                    write { bytes -> if (ok) ok = handle.write(bytes) }
                    handle.closeFile()
                    ok
                }
            }
            if (!written) {
                withContext(Dispatchers.IO) { NSFileManager.defaultManager.removeItemAtPath(path, null) }
                onDone(PickResult.Failed)
                return@launch
            }
            // The picker moves the temporary file where the user says; only then is the backup real.
            present(UIDocumentPickerViewController(forExportingURLs = listOf(NSURL.fileURLWithPath(path))), root) { urls ->
                NSFileManager.defaultManager.removeItemAtPath(path, null)
                onDone(if (urls.isEmpty()) PickResult.Cancelled else PickResult.Done)
            }
        }
    }

    actual fun importFile(read: (source: (Int) -> ByteArray?) -> Unit, onDone: (PickResult) -> Unit) {
        val root = rootController ?: return onDone(PickResult.Failed)
        if (held != null) return onDone(PickResult.Cancelled)
        present(UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeZIP, UTTypeJSON)), root) { urls ->
            val url = urls.firstOrNull()
            val path = url?.path
            if (path == null) {
                onDone(PickResult.Cancelled)
                return@present
            }
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    // A file from outside the sandbox has to be unlocked before it can be read.
                    val unlocked = url.startAccessingSecurityScopedResource()
                    val handle = NSFileHandle.fileHandleForReadingAtPath(path)
                    if (handle != null) {
                        read { n -> handle.read(n)?.takeIf { it.isNotEmpty() } }
                        handle.closeFile()
                    }
                    if (unlocked) url.stopAccessingSecurityScopedResource()
                    handle != null
                }
                onDone(if (ok) PickResult.Done else PickResult.Failed)
            }
        }
    }

    private var held: PickerDelegate? = null

    private val rootController: UIViewController?
        get() = UIApplication.sharedApplication.keyWindow?.rootViewController

    private fun present(
        controller: UIDocumentPickerViewController,
        root: UIViewController,
        onResult: (List<NSURL>) -> Unit,
    ) {
        // The picker holds its delegate weakly, so it has to be kept alive here. One slot, so a
        // second pick while one is open is refused rather than orphaning the live picker.
        val delegate = PickerDelegate { urls ->
            held = null
            onResult(urls)
        }
        held = delegate
        controller.delegate = delegate
        root.presentViewController(controller, true, null)
    }

    private class PickerDelegate(
        private val onResult: (List<NSURL>) -> Unit,
    ) : NSObject(), UIDocumentPickerDelegateProtocol {

        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            onResult(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onResult(emptyList())
        }
    }
}

/**
 * The error returning calls, never writeData: or readDataOfLength:. Those signal a full disk by
 * raising an Objective-C exception, which Kotlin/Native does not turn into a Kotlin one, so
 * runCatching would not see it and the app would be killed instead of saying it could not save.
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSFileHandle.write(bytes: ByteArray): Boolean = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    writeData(bytes.toNSData(), error.ptr)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSFileHandle.read(n: Int): ByteArray? = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    val data: NSData? = readDataUpToLength(n.toULong(), error.ptr)
    data?.toByteArray()
}
