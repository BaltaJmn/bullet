package com.baltajmn.bullet.data

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSDataWritingFileProtectionCompleteUntilFirstUserAuthentication
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionCompleteUntilFirstUserAuthentication
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

// Not Complete: with Complete, the save that fires right after the phone locks fails and the
// change is lost. UntilFirstUserAuthentication keeps the files encrypted until the first unlock
// after boot (docs/tecnico.md 6.14).
private val protection = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionCompleteUntilFirstUserAuthentication)
private val WRITE_OPTIONS = NSDataWritingAtomic or NSDataWritingFileProtectionCompleteUntilFirstUserAuthentication

@OptIn(ExperimentalForeignApi::class)
actual object Storage : JournalFiles {
    private val fm get() = NSFileManager.defaultManager

    /** Application Support, never the App Group container. */
    private val root: String by lazy {
        val base = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
            .first() as String
        fm.createDirectoryAtPath(base, true, protection, null)
        base
    }

    private val path get() = "$root/journal.json"
    private val backupPath get() = "$root/journal.bak.json"

    override fun read(): String? = textAt(path)

    override fun readPrevious(): String? = textAt(backupPath)

    override fun write(text: String) {
        if (fm.fileExistsAtPath(path)) {
            fm.removeItemAtPath(backupPath, null)
            if (!fm.copyItemAtPath(path, backupPath, null)) error("could not keep the backup")
        }
        writeAtomically(path, text.encodeToByteArray())
    }

    override fun restoreMain(text: String) = writeAtomically(path, text.encodeToByteArray())

    @OptIn(ExperimentalTime::class)
    override fun quarantine() {
        val stamp = timestamp()
        val corrupt = "$root/corrupt".also { fm.createDirectoryAtPath(it, true, protection, null) }
        if (fm.fileExistsAtPath(path)) fm.moveItemAtPath(path, "$corrupt/journal-$stamp.json", null)
        if (fm.fileExistsAtPath(backupPath)) fm.moveItemAtPath(backupPath, "$corrupt/journal-$stamp.bak.json", null)
    }

    override fun keepCopy(name: String, text: String) =
        writeAtomically("$root/journal.$name.json", text.encodeToByteArray())

    override fun readCopy(name: String): String? = textAt("$root/journal.$name.json")

    override fun wipe() {
        fm.removeItemAtPath(path, null)
        fm.removeItemAtPath(backupPath, null)
        fm.contentsOfDirectoryAtPath(root, null)?.filterIsInstance<String>()?.forEach { name ->
            if (name.startsWith("journal.") && name.endsWith(".json")) fm.removeItemAtPath("$root/$name", null)
        }
        fm.removeItemAtPath("$root/corrupt", null)
    }

    private fun writeAtomically(target: String, bytes: ByteArray) {
        if (!bytes.toNSData().writeToFile(target, WRITE_OPTIONS, null)) error("could not write $target")
    }

    @OptIn(ExperimentalTime::class)
    private fun timestamp(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        fun two(n: Int) = n.toString().padStart(2, '0')
        return "${now.year}${two(now.month.ordinal + 1)}${two(now.day)}-" +
            "${two(now.hour)}${two(now.minute)}${two(now.second)}"
    }

    /**
     * Null only when the file does not exist. A file that exists but cannot be read comes back as
     * text that will not decode, so it is quarantined instead of being taken for a fresh diary and
     * overwritten.
     */
    private fun textAt(filePath: String): String? {
        if (!fm.fileExistsAtPath(filePath)) return null
        return NSData.dataWithContentsOfFile(filePath)?.toByteArray()?.decodeToString() ?: ""
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
}
