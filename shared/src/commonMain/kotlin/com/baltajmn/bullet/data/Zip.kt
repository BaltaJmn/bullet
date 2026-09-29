package com.baltajmn.bullet.data

import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/** A zip that is not one of ours, or one that did not arrive whole. */
class ZipDamaged : Exception("zip damaged")

private const val LOCAL_SIGNATURE = 0x04034b50
private const val CENTRAL_SIGNATURE = 0x02014b50
private const val EOCD_SIGNATURE = 0x06054b50

/** Names travel as UTF-8, which is what bit 11 of the general purpose flag announces. */
private const val UTF8_NAMES = 0x0800

private val CRC_TABLE = IntArray(256) { i ->
    var c = i
    repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor -0x12477ce0 else c ushr 1 }
    c
}

/** Standard CRC-32, resumable: [crc] is what a previous call returned. */
fun crc32(bytes: ByteArray, start: Int = 0, end: Int = bytes.size, crc: Int = 0): Int {
    var c = crc.inv()
    for (i in start until end) c = CRC_TABLE[(c xor bytes[i].toInt()) and 0xFF] xor (c ushr 8)
    return c.inv()
}

/**
 * STORED only, written straight to [sink] entry by entry, so the archive is never whole in memory
 * (docs/tecnico.md 6.11), and any unzipper on any system opens it. Size and CRC go
 * in each local header, never in a data descriptor, so a reader can walk the file front to back.
 */
class ZipWriter(
    private val sink: (ByteArray) -> Unit,
    at: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
) {

    private class Entry(val name: ByteArray, val crc: Int, val size: Int, val offset: Int)

    private val entries = mutableListOf<Entry>()
    private val time = dosTime(at)
    private val date = dosDate(at)
    private var offset = 0

    fun add(name: String, bytes: ByteArray) {
        val raw = name.encodeToByteArray()
        val crc = crc32(bytes)
        val header = Buffer()
        header.int(LOCAL_SIGNATURE)
        header.short(20)
        header.short(UTF8_NAMES)
        header.short(0)
        header.short(time)
        header.short(date)
        header.int(crc)
        header.int(bytes.size)
        header.int(bytes.size)
        header.short(raw.size)
        header.short(0)
        header.bytes(raw)
        entries += Entry(raw, crc, bytes.size, offset)
        write(header.toByteArray())
        write(bytes)
    }

    fun finish() {
        val start = offset
        val central = Buffer()
        entries.forEach { e ->
            central.int(CENTRAL_SIGNATURE)
            central.short(20)
            central.short(20)
            central.short(UTF8_NAMES)
            central.short(0)
            central.short(time)
            central.short(date)
            central.int(e.crc)
            central.int(e.size)
            central.int(e.size)
            central.short(e.name.size)
            central.short(0)
            central.short(0)
            central.short(0)
            central.short(0)
            central.int(0)
            central.int(e.offset)
            central.bytes(e.name)
        }
        val directory = central.toByteArray()
        val end = Buffer()
        end.int(EOCD_SIGNATURE)
        end.short(0)
        end.short(0)
        end.short(entries.size)
        end.short(entries.size)
        end.int(directory.size)
        end.int(start)
        end.short(0)
        write(directory)
        write(end.toByteArray())
    }

    private fun write(bytes: ByteArray) {
        offset += bytes.size
        sink(bytes)
    }
}

/**
 * Reads the zips we write. [source] hands back up to the bytes asked for, or null once the file is
 * over. Anything that does not add up is refused whole: half an imported diary is worse than none.
 */
class ZipReader(private val source: (Int) -> ByteArray?) {

    fun forEach(block: (name: String, bytes: ByteArray) -> Unit) {
        while (true) {
            val signature = read(4).int(0)
            if (signature == CENTRAL_SIGNATURE || signature == EOCD_SIGNATURE) return
            if (signature != LOCAL_SIGNATURE) throw ZipDamaged()

            val header = read(26)
            val flags = header.short(2)
            val method = header.short(4)
            val crc = header.int(10)
            val size = header.int(18)
            val nameLength = header.short(22)
            val extraLength = header.short(24)
            // Bit 0 is encryption, and a data descriptor (bit 3) would mean the size here is a lie.
            if (method != 0 || flags and 0x01 != 0 || flags and 0x08 != 0) throw ZipDamaged()
            if (size < 0) throw ZipDamaged()

            val name = read(nameLength).decodeToString()
            if (extraLength > 0) read(extraLength)
            val bytes = read(size)
            if (crc32(bytes) != crc) throw ZipDamaged()
            block(name, bytes)
        }
    }

    private fun read(n: Int): ByteArray {
        val out = ByteArray(n)
        var got = 0
        while (got < n) {
            val chunk = source(n - got) ?: throw ZipDamaged()
            if (chunk.isEmpty()) throw ZipDamaged()
            chunk.copyInto(out, got)
            got += chunk.size
        }
        return out
    }
}

// --- little endian, which is the only endianness a zip knows ------------------------------------

private class Buffer {
    private val out = mutableListOf<Byte>()

    fun short(v: Int) {
        out += (v and 0xFF).toByte()
        out += ((v ushr 8) and 0xFF).toByte()
    }

    fun int(v: Int) {
        short(v and 0xFFFF)
        short((v ushr 16) and 0xFFFF)
    }

    fun bytes(b: ByteArray) {
        b.forEach { out += it }
    }

    fun toByteArray() = out.toByteArray()
}

private fun ByteArray.short(at: Int): Int = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.int(at: Int): Int = short(at) or (short(at + 2) shl 16)

private fun dosTime(at: LocalDateTime) = (at.hour shl 11) or (at.minute shl 5) or (at.second / 2)

private fun dosDate(at: LocalDateTime) = ((at.year - 1980) shl 9) or (at.month.number shl 5) or at.day
