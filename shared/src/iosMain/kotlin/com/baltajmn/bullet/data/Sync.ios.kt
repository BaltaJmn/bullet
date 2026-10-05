package com.baltajmn.bullet.data

import kotlin.concurrent.Volatile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorWritingForReplacing
import platform.Foundation.NSFileManager
import platform.Foundation.NSMetadataItem
import platform.Foundation.NSMetadataItemFSNameKey
import platform.Foundation.NSMetadataItemURLKey
import platform.Foundation.NSMetadataQuery
import platform.Foundation.NSMetadataQueryDidFinishGatheringNotification
import platform.Foundation.NSMetadataQueryDidUpdateNotification
import platform.Foundation.NSMetadataQueryUbiquitousDocumentsScope
import platform.Foundation.NSMetadataUbiquitousItemDownloadingStatusCurrent
import platform.Foundation.NSMetadataUbiquitousItemDownloadingStatusKey
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSPredicate
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private const val CONTAINER = "iCloud.com.baltajmn.bullet"

/**
 * `Documents/bobbin-sync.json` in the app's iCloud Drive container (docs/tecnico.md 12.7). The file
 * is only trusted to be missing once [NSMetadataQuery] has finished gathering: before that, a new
 * device would see nothing and write its empty diary over the other's.
 */
@OptIn(ExperimentalForeignApi::class)
actual object SyncFile {
    @Volatile private var gathered = false
    @Volatile private var found: NSURL? = null
    @Volatile private var current = false
    @Volatile private var query: NSMetadataQuery? = null
    private val observers = mutableListOf<Any>()
    private var onChange: (() -> Unit)? = null

    /** Blocks, so never on the main thread. Null without iCloud Drive or without the entitlement. */
    private fun container(): NSURL? =
        NSFileManager.defaultManager.URLForUbiquityContainerIdentifier(CONTAINER)?.URLByAppendingPathComponent("Documents", isDirectory = true)

    actual fun connect(create: Boolean, onDone: (Boolean) -> Unit) {
        MainScope().launch {
            val ok = withContext(Dispatchers.Default) { container() != null }
            if (ok) startQuery()
            onDone(ok)
        }
    }

    actual fun disconnect() {
        dispatch_async(dispatch_get_main_queue()) {
            query?.stopQuery()
            query = null
            observers.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
            observers.clear()
            gathered = false
            found = null
            current = false
        }
    }

    actual fun read(): Remote {
        if (query == null) {
            if (container() == null) return Remote.Pending
            dispatch_async(dispatch_get_main_queue()) { startQuery() }
            return Remote.Pending
        }
        if (!gathered) return Remote.Pending
        val url = found ?: return Remote.Missing
        if (!current) {
            NSFileManager.defaultManager.startDownloadingUbiquitousItemAtURL(url, null)
            return Remote.Pending
        }
        var text: String? = null
        NSFileCoordinator(filePresenter = null).coordinateReadingItemAtURL(url, options = 0u, error = null) { at ->
            text = at?.let { NSString.stringWithContentsOfURL(it, NSUTF8StringEncoding, null) }
        }
        return text?.let { Remote.Text(it) } ?: Remote.Pending
    }

    actual fun write(text: String): Boolean {
        val url = found ?: container()?.let { dir ->
            NSFileManager.defaultManager.createDirectoryAtURL(dir, withIntermediateDirectories = true, attributes = null, error = null)
            dir.URLByAppendingPathComponent(SYNC_NAME)
        } ?: return false
        var ok = false
        NSFileCoordinator(filePresenter = null).coordinateWritingItemAtURL(url, options = NSFileCoordinatorWritingForReplacing, error = null) { at ->
            ok = at != null && NSString.create(string = text).writeToURL(at, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        }
        return ok
    }

    actual fun watch(onChange: () -> Unit) {
        this.onChange = onChange
    }

    /** Main thread only: the query runs on the run loop it was started from. */
    private fun startQuery() {
        if (query != null) return
        val q = NSMetadataQuery()
        q.searchScopes = listOf(NSMetadataQueryUbiquitousDocumentsScope)
        q.predicate = NSPredicate.predicateWithFormat("%K == %@", NSString.create(string = NSMetadataItemFSNameKey), NSString.create(string = SYNC_NAME))
        listOf(NSMetadataQueryDidFinishGatheringNotification, NSMetadataQueryDidUpdateNotification).forEach { name ->
            observers += NSNotificationCenter.defaultCenter.addObserverForName(name, q, NSOperationQueue.mainQueue) { _ -> update(q) }
        }
        query = q
        q.startQuery()
    }

    private fun update(q: NSMetadataQuery) {
        q.disableUpdates()
        val item = if (q.resultCount > 0u) q.resultAtIndex(0u) as? NSMetadataItem else null
        found = item?.valueForAttribute(NSMetadataItemURLKey) as? NSURL
        current = item?.valueForAttribute(NSMetadataUbiquitousItemDownloadingStatusKey) == NSMetadataUbiquitousItemDownloadingStatusCurrent
        gathered = true
        q.enableUpdates()
        onChange?.invoke()
    }
}
