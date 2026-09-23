package com.baltajmn.bullet.data

import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual object Storage : JournalFiles {
    /** Host tests point this at a temporary folder; the app always uses filesDir. */
    internal var rootOverride: File? = null

    private val dir: File get() = rootOverride ?: AndroidContext.value.filesDir
    private val file get() = File(dir, "journal.json")
    private val backup get() = File(dir, "journal.bak.json")
    private val temp get() = File(dir, "journal.tmp.json")

    override fun read(): String? = file.textOrNull()

    override fun readPrevious(): String? = backup.textOrNull()

    override fun write(text: String) {
        writeTemp(temp, text)
        if (file.exists() && !file.renameTo(backup)) error("could not rotate the backup")
        if (!temp.renameTo(file)) error("could not move the new file into place")
    }

    override fun restoreMain(text: String) {
        writeTemp(temp, text)
        if (!temp.renameTo(file)) error("could not restore the main file")
    }

    @OptIn(ExperimentalTime::class)
    override fun quarantine() {
        val stamp = timestamp()
        val corrupt = File(dir, "corrupt").apply { mkdirs() }
        file.takeIf { it.exists() }?.renameTo(File(corrupt, "journal-$stamp.json"))
        backup.takeIf { it.exists() }?.renameTo(File(corrupt, "journal-$stamp.bak.json"))
    }

    override fun keepCopy(name: String, text: String) {
        val target = File(dir, "journal.$name.json")
        val tmp = File(dir, "journal.$name.tmp.json")
        writeTemp(tmp, text)
        if (!tmp.renameTo(target)) error("could not save the $name copy")
    }

    override fun readCopy(name: String): String? = File(dir, "journal.$name.json").textOrNull()

    override fun wipe() {
        file.delete()
        backup.delete()
        temp.delete()
        dir.listFiles { f -> f.name.startsWith("journal.") && f.name.endsWith(".json") }
            ?.forEach { it.delete() }
        File(dir, "corrupt").deleteRecursively()
    }

    @OptIn(ExperimentalTime::class)
    private fun timestamp(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "%04d%02d%02d-%02d%02d%02d".format(
            now.year, now.month.ordinal + 1, now.day, now.hour, now.minute, now.second,
        )
    }

    /** Flushed to disk before any rename, so a power cut cannot leave a renamed empty file. */
    private fun writeTemp(target: File, text: String) {
        FileOutputStream(target).use {
            it.write(text.encodeToByteArray())
            it.fd.sync()
        }
    }

    private fun File.textOrNull(): String? = takeIf { it.exists() }?.readText()
}
