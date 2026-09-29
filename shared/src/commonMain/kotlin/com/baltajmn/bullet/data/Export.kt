package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.SCHEMA_VERSION
import com.baltajmn.bullet.model.clampCodePoints
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.futureBlock
import com.baltajmn.bullet.model.monthDays
import com.baltajmn.bullet.model.placeDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

const val EXPORT_PREFIX = "bobbin"
const val JOURNAL_NAME = "journal.json"

/** `bobbin-AAAA-MM-DD.zip`, with the logical date of today (docs/tecnico.md 4.3). */
fun exportName(today: LocalDate) = "$EXPORT_PREFIX-$today.zip"

private const val NAME_MAX = 60

/**
 * One Markdown per month with anything on it (docs/tecnico.md 4.4): the calendar, the tasks, each day
 * and the Future Log block, in the method's ASCII so it reads the same in any editor in fifty years.
 * Never translated. Null for a month with nothing, and an empty section is not written.
 */
fun monthMarkdown(j: Journal, m: YearMonth): String? {
    fun items(entries: List<com.baltajmn.bullet.model.Entry>, day: Boolean = false) =
        entries.joinToString("") { e -> "- " + asciiEntry(e, if (day) e.placeDay?.let { "($it)" } else null) + "\n" }

    val sections = mutableListOf<String>()
    val calendar = (1..monthDays(m)).mapNotNull { d -> j.entriesAt(Place.Monthly(m, d)).takeIf { it.isNotEmpty() }?.let { d to it } }
    if (calendar.isNotEmpty()) sections += "## Calendar\n" + calendar.joinToString("") { (d, es) -> "\n### $d\n" + items(es) }
    j.entriesAt(Place.Monthly(m)).takeIf { it.isNotEmpty() }?.let { sections += "## Tasks\n" + items(it) }
    (1..monthDays(m)).forEach { d ->
        val date = LocalDate(m.year, m.month, d)
        j.entriesAt(Place.Daily(date)).takeIf { it.isNotEmpty() }?.let { sections += "## $date\n" + items(it) }
    }
    j.futureBlock(m).takeIf { it.isNotEmpty() }?.let { sections += "## Future log\n" + items(it, day = true) }
    if (sections.isEmpty()) return null
    return "# $m\n\n" + sections.joinToString("\n")
}

/** A notes collection is its title and its entries; a tracker, its month as a table with an `x` per mark. */
fun collectionMarkdown(j: Journal, c: BulletCollection): String {
    val month = c.month
    if (c.kind == CollectionKind.TRACKER && month != null) {
        val days = 1..monthDays(month)
        return buildString {
            append("# ${c.title} $month\n\n")
            append("| |").append(days.joinToString("") { " $it |" }).append("\n")
            append("|---|").append(days.joinToString("") { "---|" }).append("\n")
            c.rows.forEach { row -> append("| ${row.title} |").append(days.joinToString("") { if (it in row.days) " x |" else "  |" }).append("\n") }
        }
    }
    return "# ${c.title}\n\n" + j.entriesAt(Place.InCollection(c.id)).joinToString("") { "- " + asciiEntry(it) + "\n" }
}

/**
 * The name of a collection's file (docs/tecnico.md 4.4): what a file system refuses becomes `-`, cut to
 * 60 code points, the id if nothing is left, the month after a tracker's, and ` 2`, ` 3` on repeats.
 */
fun collectionFileNames(collections: List<BulletCollection>): List<String> {
    val used = mutableMapOf<String, Int>()
    return collections.map { c ->
        val clean = c.title.map { ch -> if (ch in "/\\:*?\"<>|" || ch.code < 0x20 || ch.code == 0x7F) '-' else ch }
            .joinToString("").trim().clampCodePoints(NAME_MAX).ifEmpty { c.id }
        val base = if (c.kind == CollectionKind.TRACKER && c.month != null) "$clean ${c.month}" else clean
        val n = (used[base] ?: 0) + 1
        used[base] = n
        if (n == 1) base else "$base $n"
    }
}

/** The months with anything in Daily, Monthly or Future, ascending. */
private fun Journal.exportMonths(): List<YearMonth> = entries.filter { !it.gone }.mapNotNull { e ->
    when (val p = e.place) {
        is Place.Daily -> YearMonth(p.date.year, p.date.month)
        is Place.Monthly -> p.month
        is Place.Future -> p.month
        is Place.InCollection -> null
    }
}.distinct().sorted()

/**
 * Writes the backup into [sink] entry by entry, in the order of docs/tecnico.md 4.3: `journal.json`
 * (settings and skeletons included, it is what the app reads back), the months and the collections.
 */
fun exportZip(journal: Journal, sink: (ByteArray) -> Unit) {
    val zip = ZipWriter(sink)
    zip.add(JOURNAL_NAME, JournalJson.encodeToString(Journal.serializer(), journal).encodeToByteArray())
    journal.exportMonths().forEach { m ->
        monthMarkdown(journal, m)?.let { zip.add("months/$m.md", it.encodeToByteArray()) }
    }
    val collections = journal.collections.sortedBy { it.createdAt }
    collectionFileNames(collections).zip(collections).forEach { (name, c) ->
        zip.add("collections/$name.md", collectionMarkdown(journal, c).encodeToByteArray())
    }
    zip.finish()
}

/** Why a file could not be imported (docs/tecnico.md 4.6). Each one has its own text. */
enum class ImportProblem { NOT_BACKUP, DAMAGED, TOO_NEW, EMPTY, SIBLING_PURL, SIBLING_MOOD, SIBLING_QUILT }

sealed interface ReadBackup {
    data class Ok(val journal: Journal) : ReadBackup
    data class Failed(val problem: ImportProblem) : ReadBackup
}

/**
 * Reads a picked file whole into a [Journal] or says why not, without touching the diary (4.6). A zip
 * keeps only `journal.json` in memory; a loose `journal.json` is read as it is.
 */
fun readBackup(source: (Int) -> ByteArray?, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS): ReadBackup {
    val head = source(4) ?: return ReadBackup.Failed(ImportProblem.NOT_BACKUP)
    val isZip = head.size == 4 && head[0] == 0x50.toByte() && head[1] == 0x4B.toByte() && head[2] == 3.toByte() && head[3] == 4.toByte()
    // What was already read goes back in front of the rest, so the reader sees the file whole.
    var pending: ByteArray? = head
    val whole: (Int) -> ByteArray? = { n ->
        val p = pending
        if (p != null) {
            pending = null
            if (p.size <= n) p else p.copyOf(n).also { pending = p.copyOfRange(n, p.size) }
        } else {
            source(n)
        }
    }

    val text: String
    if (isZip) {
        var journal: String? = null
        var sibling = false
        try {
            ZipReader(whole).forEach { name, bytes ->
                if (name == JOURNAL_NAME) journal = bytes.decodeToString()
                if (name == "entries.json") sibling = true
            }
        } catch (e: ZipDamaged) {
            return ReadBackup.Failed(ImportProblem.DAMAGED)
        }
        text = journal ?: return ReadBackup.Failed(if (sibling) ImportProblem.SIBLING_PURL else ImportProblem.NOT_BACKUP)
    } else {
        val bytes = mutableListOf<ByteArray>()
        while (true) bytes += whole(64 * 1024) ?: break
        text = bytes.fold(ByteArray(0)) { acc, b -> acc + b }.decodeToString()
        if (!text.trimStart().startsWith("{")) return ReadBackup.Failed(ImportProblem.NOT_BACKUP)
    }
    return parseBackup(text, steps)
}

/** The checks of docs/tecnico.md 4.6 on the text of a `journal.json`. */
fun parseBackup(text: String, steps: List<(JsonObject) -> JsonObject> = SCHEMA_STEPS): ReadBackup {
    val root = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull()
        ?: return ReadBackup.Failed(ImportProblem.NOT_BACKUP)
    siblingOf(root)?.let { return ReadBackup.Failed(it) }
    val version = (root["schemaVersion"] as? JsonPrimitive)?.intOrNull
    if (version == null || root["entries"] !is JsonArray) return ReadBackup.Failed(ImportProblem.NOT_BACKUP)
    if (version > SCHEMA_VERSION) return ReadBackup.Failed(ImportProblem.TOO_NEW)

    val journal = try {
        // An older schema is converted in memory with the same steps as the disk, and nothing is written.
        val current = if (version < SCHEMA_VERSION) migrateSchema(root, steps.drop(version - 1)) else root
        JournalJson.decodeFromJsonElement(Journal.serializer(), current)
    } catch (e: Exception) {
        return ReadBackup.Failed(ImportProblem.DAMAGED)
    }
    val entryIds = journal.entries.map { it.id }
    val collectionIds = journal.collections.map { it.id }.toSet()
    val damaged = entryIds.any { it.isEmpty() } || journal.collections.any { it.id.isEmpty() } ||
        entryIds.size != entryIds.toSet().size || journal.collections.size != collectionIds.size ||
        journal.entries.any { e -> !e.gone && (e.place as? Place.InCollection)?.let { it.id !in collectionIds } == true }
    if (damaged) return ReadBackup.Failed(ImportProblem.DAMAGED)
    if (journal.entries.isEmpty() && journal.collections.isEmpty()) return ReadBackup.Failed(ImportProblem.EMPTY)
    return ReadBackup.Ok(journal)
}

/** A sister app's backup by its shape (docs/tecnico.md 4.5): in v1.0 it is refused with its own text. */
private fun siblingOf(root: JsonObject): ImportProblem? = when {
    (root["version"] as? JsonPrimitive)?.intOrNull != null && root["entries"] is JsonObject -> ImportProblem.SIBLING_PURL
    (root["app"] as? JsonPrimitive)?.contentOrNull == "mood" -> ImportProblem.SIBLING_MOOD
    root["habits"] is JsonArray -> ImportProblem.SIBLING_QUILT
    else -> null
}
