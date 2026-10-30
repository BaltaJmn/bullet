package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.INDEX_ICONS
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.JournalJson
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.TrackerRow
import com.baltajmn.bullet.model.entriesAt
import com.baltajmn.bullet.model.trackerThread
import kotlinx.datetime.YearMonth
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

const val PACKET_VERSION = 1
const val LIST_EXTENSION = "bobbin"
const val LIST_MIME = "application/vnd.baltajmn.bobbin+json"

/** The key that tells a sent list from a backup: [readBackup] looks for it before `schemaVersion`. */
internal const val PACKET_MARK = "bobbin"

private const val NAME_MAX = 40

@Serializable
data class PacketEntry(
    @SerialName("i") val id: String,
    @SerialName("b") val bullet: Bullet = Bullet.TASK,
    @SerialName("s") val status: TaskStatus = TaskStatus.OPEN,
    @SerialName("g") val signifiers: Set<Signifier> = emptySet(),
    @SerialName("x") val text: String,
    @SerialName("c") val createdAt: Long,
    @SerialName("u") val updatedAt: Long,
)

/**
 * One list on its way to someone else (docs/tecnico.md 12.11): a collection and its entries in a short
 * format of its own, so that nothing else of the diary ever travels with it.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ListPacket(
    @EncodeDefault @SerialName(PACKET_MARK) val version: Int = PACKET_VERSION,
    @SerialName("id") val collectionId: String,
    @SerialName("t") val title: String,
    @SerialName("k") val kind: CollectionKind = CollectionKind.NOTES,
    @SerialName("u") val updatedAt: Long,
    @SerialName("n") val icon: String? = null,
    @SerialName("w") val blank: Boolean = false,
    /** In the list's order. */
    @SerialName("e") val entries: List<PacketEntry> = emptyList(),
    /** TRACKER only: the titles of its rows. */
    @SerialName("r") val rows: List<String> = emptyList(),
)

sealed interface PacketRead {
    data class Ok(val packet: ListPacket) : PacketRead
    data object TooNew : PacketRead
    data object Damaged : PacketRead
    data object Empty : PacketRead
}

/** What receiving [ListPacket] would do, for the summary before anything is written. */
sealed interface Received {
    data class New(val entries: Int) : Received
    data class Merge(val added: Int, val updated: Int, val same: Int) : Received
}

/**
 * The list [collectionId] as it goes out, or null when there is nothing to send. As it is: ids, states
 * and times kept, so the same list coming back merges. [blank]: new ids, every task open, as a new list.
 * A tracker always goes blank, as its rows without a single mark. Never `from`, `theme`, threads,
 * archive, settings or any other page.
 */
fun packList(j: Journal, collectionId: String, blank: Boolean, now: Long, newId: (prefix: String) -> String): ListPacket? {
    val c = j.collections.find { it.id == collectionId } ?: return null
    if (c.kind == CollectionKind.TRACKER) {
        val tail = j.trackerThread(c.id).lastOrNull() ?: c
        val rows = tail.rows.map { it.title }.takeIf { it.isNotEmpty() } ?: return null
        return ListPacket(collectionId = newId("c"), title = tail.title, kind = CollectionKind.TRACKER, updatedAt = now, icon = tail.icon, blank = true, rows = rows)
    }
    val entries = j.entriesAt(Place.InCollection(c.id)).takeIf { it.isNotEmpty() } ?: return null
    if (blank) {
        return ListPacket(
            collectionId = newId("c"),
            title = c.title,
            updatedAt = now,
            icon = c.icon,
            blank = true,
            entries = entries.map { e ->
                PacketEntry(newId("e"), e.bullet, TaskStatus.OPEN, e.signifiers, e.text, createdAt = now, updatedAt = now)
            },
        )
    }
    return ListPacket(
        collectionId = c.id,
        title = c.title,
        updatedAt = c.updatedAt,
        icon = c.icon,
        entries = entries.map { e -> PacketEntry(e.id, e.bullet, e.status, e.signifiers, e.text, e.createdAt, e.updatedAt) },
    )
}

fun encodePacket(p: ListPacket): String = JournalJson.encodeToString(ListPacket.serializer(), p)

/** Whether a picked file's JSON is a sent list and not a backup. */
internal fun isPacket(root: JsonObject): Boolean = root.containsKey(PACKET_MARK)

/** The checks of docs/tecnico.md 12.11 on the text of a `.bobbin`. Text is never cut: it is an import. */
fun readPacket(text: String): PacketRead {
    val root = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return PacketRead.Damaged
    return readPacket(root)
}

internal fun readPacket(root: JsonObject): PacketRead {
    val version = (root[PACKET_MARK] as? JsonPrimitive)?.intOrNull ?: return PacketRead.Damaged
    if (version > PACKET_VERSION) return PacketRead.TooNew
    val p = try {
        JournalJson.decodeFromJsonElement(ListPacket.serializer(), root)
    } catch (e: Exception) {
        return PacketRead.Damaged
    }
    val ids = p.entries.map { it.id }
    if (p.collectionId.isBlank() || p.title.isBlank() || ids.any { it.isBlank() } || ids.size != ids.toSet().size) return PacketRead.Damaged
    val clean = p.copy(
        icon = p.icon?.takeIf { it in INDEX_ICONS },
        // A tracker carries rows; whatever entries came with one are not its own.
        entries = if (p.kind == CollectionKind.TRACKER) emptyList() else p.entries.map { if (it.bullet == Bullet.TASK) it else it.copy(status = TaskStatus.OPEN) },
        rows = if (p.kind == CollectionKind.TRACKER) p.rows.filter { it.isNotBlank() } else emptyList(),
    )
    if (clean.entries.isEmpty() && clean.rows.isEmpty()) return PacketRead.Empty
    return PacketRead.Ok(clean)
}

/** `viaje-a-lisboa.bobbin`: the title folded to ASCII letters, digits and dashes, or `bobbin-list`. */
fun packetFileName(title: String): String {
    val slug = fold(title).map { if (it in 'a'..'z' || it in '0'..'9') it else '-' }.joinToString("")
        .split('-').filter { it.isNotEmpty() }.joinToString("-").take(NAME_MAX).trimEnd('-')
    return (slug.ifEmpty { "bobbin-list" }) + ".$LIST_EXTENSION"
}

/** The list already in the diary that [p] would merge into: same id and same kind. */
private fun Journal.sameList(p: ListPacket): BulletCollection? =
    collections.find { it.id == p.collectionId && it.kind == p.kind && it.kind == CollectionKind.NOTES }

fun receivePreview(j: Journal, p: ListPacket): Received {
    if (j.sameList(p) == null) return Received.New(if (p.kind == CollectionKind.TRACKER) p.rows.size else p.entries.size)
    val (merged, _) = j.mergePacket(p)
    val before = j.entries.associateBy { it.id }
    val after = merged.entries.associateBy { it.id }
    var added = 0
    var updated = 0
    var same = 0
    p.entries.forEach { e ->
        val was = before[e.id]
        when {
            was == null -> added++
            after[e.id] != was -> updated++
            else -> same++
        }
    }
    return Received.Merge(added, updated, same)
}

/**
 * The diary with [p] received (docs/tecnico.md 12.11). A list it does not have yet comes in as a new page
 * of the Index; the same list back again is merged by [merge], so nothing the receiver has is ever
 * deleted and a closed task never reopens. Settings and `months` are the device's, always.
 */
fun Journal.receive(p: ListPacket, now: Long, month: YearMonth, newId: (prefix: String) -> String): Journal {
    if (sameList(p) != null) return mergePacket(p).first
    val taken = collections.map { it.id }.toSet()
    val id = p.collectionId.takeIf { it !in taken } ?: newId("c")
    val collection = BulletCollection(
        id = id,
        title = p.title,
        // New here, so the Index puts it where it was received, not where it was written.
        createdAt = now,
        updatedAt = p.updatedAt,
        kind = p.kind,
        icon = p.icon,
        month = month.takeIf { p.kind == CollectionKind.TRACKER },
        rows = p.rows.map { TrackerRow(newId("r"), it) },
    )
    val entryIds = entries.map { it.id }.toSet()
    val fresh = p.entries.mapIndexed { i, e ->
        Entry(
            id = e.id.takeIf { it !in entryIds } ?: newId("e"),
            bullet = e.bullet,
            text = e.text,
            status = e.status,
            signifiers = e.signifiers,
            place = Place.InCollection(id),
            order = i,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt,
        )
    }
    return copy(collections = collections + collection, entries = entries + fresh)
}

/**
 * [p] merged into the list it came from. The incoming page only brings its title and icon, so the
 * receiver's archive, theme and thread stay; entries both sides have keep the receiver's order, and the
 * new ones go after the last.
 */
private fun Journal.mergePacket(p: ListPacket): Pair<Journal, MergeResult> {
    val mine = sameList(p)!!
    val place = Place.InCollection(mine.id)
    val byId = entries.associateBy { it.id }
    var next = (entries.filter { it.place == place }.maxOfOrNull { it.order } ?: -1) + 1
    val incoming = Journal(
        collections = listOf(mine.copy(title = p.title, icon = p.icon ?: mine.icon, updatedAt = p.updatedAt)),
        entries = p.entries.map { e ->
            Entry(
                id = e.id,
                bullet = e.bullet,
                text = e.text,
                status = e.status,
                signifiers = e.signifiers,
                place = place,
                order = byId[e.id]?.order ?: next++,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt,
                // The receiver's own link to where a task came from never travels, and never goes.
                from = byId[e.id]?.from,
            )
        },
    )
    val result = merge(this, incoming)
    return result.journal.copy(settings = settings, months = months) to result
}
