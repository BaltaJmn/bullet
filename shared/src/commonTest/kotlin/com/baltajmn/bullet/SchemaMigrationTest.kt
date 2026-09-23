package com.baltajmn.bullet

import com.baltajmn.bullet.data.BobbinRepository
import com.baltajmn.bullet.data.MemoryFiles
import com.baltajmn.bullet.data.migrateSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive

// 21. Almacen: carga, reparacion y esquema (schema half: #15).
class SchemaMigrationTest {

    private val v0 = """{"schemaVersion":0,"legacyText":"hola"}"""

    // Turns a made-up version 0 into the current version 1, on purpose without "collections": the
    // decode must fall back to Journal's own default, never a null or a parse failure.
    private val fakeStepV0ToV1: (JsonObject) -> JsonObject = { obj ->
        val text = obj["legacyText"]?.jsonPrimitive?.content ?: ""
        buildJsonObject {
            put("schemaVersion", 1)
            put(
                "entries",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("id", "e-1")
                            put("text", text)
                            put("place", buildJsonObject { put("daily", "2026-01-01") })
                            put("createdAt", 0)
                            put("updatedAt", 0)
                        },
                    )
                },
            )
        }
    }

    @Test
    fun migrateSchemaAppliesEveryStepInOrderNeverWithADirectJump() {
        val stampStep: (String) -> (JsonObject) -> JsonObject = { mark ->
            { obj -> buildJsonObject { obj.forEach { (k, v) -> put(k, v) }; put(mark, JsonPrimitive(true)) } }
        }
        val result = migrateSchema(buildJsonObject { put("schemaVersion", 0) }, listOf(stampStep("step1"), stampStep("step2")))

        assertTrue(result["step1"]?.jsonPrimitive?.content.toBoolean())
        assertTrue(result["step2"]?.jsonPrimitive?.content.toBoolean())
    }

    @Test
    fun aFakeStepMigratesVersionZeroAndKeepsAPreMigrationCopy() {
        val files = MemoryFiles(main = v0)

        BobbinRepository.load(files, steps = listOf(fakeStepV0ToV1))

        assertFalse(BobbinRepository.migrationFailed)
        assertFalse(BobbinRepository.updateNeeded)
        assertEquals("hola", BobbinRepository.journal.entries.single().text)
        // The field the fake step never set falls back to Journal's own default, not null.
        assertTrue(BobbinRepository.journal.collections.isEmpty())
        assertEquals(v0, files.copies["pre-migration"])
        assertTrue(files.main!!.contains("\"schemaVersion\":1"))
    }

    @Test
    fun aThrowingStepFailsTheMigrationAndLeavesTheFileExactlyAsItWas() {
        val files = MemoryFiles(main = v0)
        val throwingStep: (JsonObject) -> JsonObject = { throw IllegalStateException("boom") }

        BobbinRepository.load(files, steps = listOf(throwingStep))

        assertTrue(BobbinRepository.migrationFailed)
        assertFalse(BobbinRepository.corrupt)
        assertEquals(v0, files.main)
        assertEquals(v0, files.copies["pre-migration"])
    }

    @Test
    fun aHigherSchemaVersionIsTooNewAndNothingIsWritten() {
        val newer = """{"schemaVersion":99,"entries":[]}"""
        val files = MemoryFiles(main = newer)

        BobbinRepository.load(files)

        assertTrue(BobbinRepository.updateNeeded)
        assertFalse(BobbinRepository.migrationFailed)
        assertFalse(BobbinRepository.corrupt)
        assertEquals(newer, files.main)
        assertNull(files.backup)

        // Read-only: an edit right after a TooNew load must not reach the file.
        kotlinx.coroutines.runBlocking {
            BobbinRepository.flush()
        }
        assertEquals(newer, files.main)
    }
}
