package com.baltajmn.bullet.data

/**
 * The diary file and its previous version, in the app's private storage and never in the App
 * Group: widgets only ever see widget.json (docs/tecnico.md 4.2).
 *
 * There is no cloud copy, so this file is the user's bullet journal. A write can never leave the
 * main file truncated, and a file that could not be read is moved aside, never overwritten.
 */
interface JournalFiles {
    /** journal.json, or null when it does not exist. */
    fun read(): String?

    /** journal.bak.json: the version before the last write. */
    fun readPrevious(): String?

    /** Atomic: the current file becomes the .bak and the new text replaces it in one rename. */
    fun write(text: String)

    /** Rewrites journal.json from a good source without rotating the .bak, so the good copy survives. */
    fun restoreMain(text: String)

    /** Moves both files to corrupt/journal-<yyyyMMdd-HHmmss>.json and .bak.json. */
    fun quarantine()

    /** journal.<name>.json: "pre-migration", "pre-import", "pre-sync". */
    fun keepCopy(name: String, text: String)

    fun readCopy(name: String): String?

    /** Deletes journal.json, .bak, .tmp, every named copy and corrupt/. */
    fun wipe()
}

expect object Storage : JournalFiles
