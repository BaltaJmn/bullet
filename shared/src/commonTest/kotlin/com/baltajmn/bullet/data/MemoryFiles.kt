package com.baltajmn.bullet.data

/** In-memory [JournalFiles]: storage logic runs on every target without touching a real disk (test 21). */
class MemoryFiles(
    var main: String? = null,
    var backup: String? = null,
) : JournalFiles {
    val copies = mutableMapOf<String, String>()

    /** What [quarantine] moved aside, kept here only so a test can check nothing was thrown away. */
    var quarantinedMain: String? = null
        private set
    var quarantinedBackup: String? = null
        private set
    var quarantineCalls = 0
        private set
    var wiped = false
        private set

    override fun read(): String? = main

    override fun readPrevious(): String? = backup

    override fun write(text: String) {
        backup = main
        main = text
    }

    override fun restoreMain(text: String) {
        main = text
    }

    override fun quarantine() {
        quarantineCalls++
        quarantinedMain = main
        quarantinedBackup = backup
        main = null
        backup = null
    }

    override fun keepCopy(name: String, text: String) {
        copies[name] = text
    }

    override fun readCopy(name: String): String? = copies[name]

    override fun wipe() {
        main = null
        backup = null
        copies.clear()
        wiped = true
    }
}
