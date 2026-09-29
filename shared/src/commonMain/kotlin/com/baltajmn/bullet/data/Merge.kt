package com.baltajmn.bullet.data

import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.SCHEMA_VERSION
import com.baltajmn.bullet.model.TaskStatus

data class MergeResult(
    val journal: Journal,
    /** Entries and collections only the backup had. */
    val added: Int,
    /** On both sides, and changed. */
    val updated: Int,
    /** On both sides, and unchanged. */
    val same: Int,
)

/**
 * Importing never deletes (docs/tecnico.md 6.9): a union by `id`, the most recent `updatedAt` wins with
 * a tie going to the device, and **a closed state never goes back to OPEN**, whichever side is newer.
 * What only the device has is not touched.
 */
fun merge(device: Journal, incoming: Journal): MergeResult {
    var added = 0
    var updated = 0
    var same = 0

    fun <T> union(mine: List<T>, theirs: List<T>, id: (T) -> String, pick: (T, T) -> T): List<T> {
        val byId = theirs.associateBy(id)
        val merged = mine.map { m ->
            val t = byId[id(m)] ?: return@map m
            val r = pick(m, t)
            if (r == m) same++ else updated++
            r
        }
        val known = mine.map(id).toSet()
        val fresh = theirs.filter { id(it) !in known }
        added += fresh.size
        return merged + fresh
    }

    val entries = union(device.entries, incoming.entries, { it.id }) { m, t ->
        val winner = if (t.updatedAt > m.updatedAt) t else m
        val other = if (winner === t) m else t
        if (winner.bullet == Bullet.TASK && winner.status == TaskStatus.OPEN && other.status != TaskStatus.OPEN) {
            winner.copy(status = other.status)
        } else {
            winner
        }
    }
    val collections = union(device.collections, incoming.collections, { it.id }) { m, t -> if (t.updatedAt > m.updatedAt) t else m }

    // A reinstall being restored takes the backup's settings; a diary in use keeps its own.
    val restoring = device.entries.isEmpty() && device.collections.isEmpty()
    return MergeResult(
        journal = device.copy(
            schemaVersion = SCHEMA_VERSION,
            entries = entries,
            collections = collections,
            settings = if (restoring) incoming.settings else device.settings,
            months = if (restoring) incoming.months else device.months,
        ),
        added = added,
        updated = updated,
        same = same,
    )
}
