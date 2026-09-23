package com.baltajmn.bullet.data

import kotlinx.serialization.json.JsonObject

/**
 * SCHEMA_STEPS[i] converts a journal from schema i+1 to i+2. Each step is independent, receives
 * and returns a JsonObject, sets the new schemaVersion and fills any field that version added with
 * its own default, never with null (docs/tecnico.md 6.14). Empty until a schema change ships; the
 * mechanism itself is proven in tests with steps of its own (test 21).
 */
val SCHEMA_STEPS: List<(JsonObject) -> JsonObject> = emptyList()

/** Applies [steps] one at a time (1 to 2, 2 to 3...), never with a direct jump to the final schema,
 * so each step is tested alone and a new one lands without touching the others. */
fun migrateSchema(json: JsonObject, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS): JsonObject =
    steps.fold(json) { acc, step -> step(acc) }

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
