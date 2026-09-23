package com.baltajmn.bullet.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Applied when capturing and when editing, never when reading or importing (docs/tecnico.md 4.1). */
const val TEXT_LIMIT = 500

/** The capture row's counter only shows past this many code points (docs/pantallas.md 5.2). */
const val COUNTER_FROM = 450

/** A closed set on purpose: the three bullets of the method, nothing else (docs/tecnico.md 11). */
@Serializable
enum class Bullet {
    @SerialName("task") TASK,
    @SerialName("event") EVENT,
    @SerialName("note") NOTE,
}

/** Valid only when [Entry.bullet] is [Bullet.TASK]; an EVENT or a NOTE always reads as OPEN. */
@Serializable
enum class TaskStatus {
    @SerialName("open") OPEN,
    @SerialName("done") DONE,
    @SerialName("migrated") MIGRATED,
    @SerialName("scheduled") SCHEDULED,
    @SerialName("irrelevant") IRRELEVANT,
}

@Serializable
enum class Signifier {
    @SerialName("priority") PRIORITY,
    @SerialName("inspiration") INSPIRATION,
    @SerialName("explore") EXPLORE,
}

/** The bullet journal's minimal unit: one line, its status and its signifiers (SPEC 2). */
@Serializable
data class Entry(
    val id: String,
    val bullet: Bullet = Bullet.TASK,
    val text: String,
    val status: TaskStatus = TaskStatus.OPEN,
    val signifiers: Set<Signifier> = emptySet(),
    val place: Place,
    val order: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val from: String? = null,
    val gone: Boolean = false,
)

/**
 * What a freshly decoded [Entry] becomes: a file written by another client can carry a [status]
 * that makes no sense for an EVENT or a NOTE. It is corrected here, never rejected, because the
 * load path must survive whatever a sibling importer or an old schema wrote (docs/tecnico.md 4.1).
 */
fun Entry.normalized(): Entry =
    if (bullet != Bullet.TASK && status != TaskStatus.OPEN) copy(status = TaskStatus.OPEN) else this

/** Toggles a TASK between OPEN and DONE. An EVENT or a NOTE never has a status to toggle (SPEC 2.3). */
fun Entry.toggleDone(): Entry =
    if (bullet != Bullet.TASK) this
    else copy(status = if (status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)

/** Screen order within one place (docs/tecnico.md 6.3): `order`, then `createdAt`, then `id`. */
val ENTRY_ORDER: Comparator<Entry> = compareBy({ it.order }, { it.createdAt }, { it.id })

/** An entry is always a single line: every line break becomes a space (docs/tecnico.md 4.1). */
fun String.oneLine(): String = replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ')

/** Characters as a person counts them: a surrogate pair (most emoji) is one. */
fun String.codePointCount(): Int {
    var n = 0
    var i = 0
    while (i < length) {
        i += widthAt(i)
        n++
    }
    return n
}

/**
 * The first [n] code points, never splitting a surrogate pair, and never stopping right before a
 * code point that belongs to the previous one: a heart without its VS16 or a thumb without its
 * skin tone is a different character.
 *
 * ponytail: a flag (two regional indicators) cut at the exact boundary still loses its second
 * half. Needs a grapheme breaker if it ever shows up.
 */
fun String.clampCodePoints(n: Int): String {
    var i = 0
    var taken = 0
    while (i < length && taken < n) {
        i += widthAt(i)
        taken++
    }
    while (i in 1 until length && (codePointAt(i).continuesCluster() || this[i - 1].code == ZWJ)) i = startBefore(i)
    return substring(0, i)
}

/** What the field accepts, and where its cursor goes. */
data class Edit(val text: String, val cursor: Int)

/**
 * The edit the field accepts. [old] is the text before the keystroke, [new] what the keyboard or
 * the paste proposes and [cursor] where the keyboard left the cursor in [new]. Only the inserted
 * segment is trimmed, so pasting into a full line never cuts its end, and a text already over the
 * limit (imported or dictated) can shrink but not grow.
 */
fun limitEdit(old: String, new: String, cursor: Int, limit: Int = TEXT_LIMIT): Edit {
    val allowed = maxOf(limit, old.codePointCount())
    if (new.codePointCount() <= allowed) return Edit(new, cursor)
    // The insertion ends at the cursor, so what follows it was already there. Guessing it from the
    // common prefix instead eats the next letter when the paste starts with that same letter.
    val tail = new.length - cursor
    var q: Int
    var p: Int
    if (cursor in 0..new.length && tail <= old.length && old.endsWith(new.substring(cursor))) {
        q = tail
        p = minOf(new.commonPrefixWith(old).length, cursor, old.length - q)
    } else {
        // A keyboard that reports a cursor not matching the text: fall back to the longest prefix.
        p = new.commonPrefixWith(old).length
        q = minOf(new.commonSuffixWith(old).length, new.length - p, old.length - p)
    }
    if (p > 0 && new[p - 1].isHighSurrogate()) p--
    if (q > 0 && new[new.length - q].isLowSurrogate()) q--
    val head = new.substring(0, p)
    val end = new.substring(new.length - q)
    val room = (allowed - (head + end).codePointCount()).coerceAtLeast(0)
    val kept = new.substring(p, new.length - q).clampCodePoints(room)
    return Edit(head + kept + end, p + kept.length)
}

private const val ZWJ = 0x200D

private fun String.widthAt(i: Int) = if (this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) 2 else 1

private fun String.codePointAt(i: Int): Int =
    if (widthAt(i) == 2) 0x10000 + ((this[i].code - 0xD800) shl 10) + (this[i + 1].code - 0xDC00) else this[i].code

private fun String.startBefore(i: Int) = if (i >= 2 && this[i - 1].isLowSurrogate() && this[i - 2].isHighSurrogate()) i - 2 else i - 1

/** Combining marks, VS16, the zero width joiner and the skin tones only ever extend what precedes them. */
private fun Int.continuesCluster() = this in 0x300..0x36F || this == 0xFE0F || this == ZWJ || this in 0x1F3FB..0x1F3FF
