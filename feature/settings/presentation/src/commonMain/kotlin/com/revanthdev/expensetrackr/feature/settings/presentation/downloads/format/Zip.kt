package com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format

/** A growable little-endian byte buffer — the multiplatform stand-in for `ByteArrayOutputStream`. */
internal class ByteBuf(initialCapacity: Int = 8 * 1024) {

    private var array = ByteArray(initialCapacity)
    var size: Int = 0
        private set

    fun u8(value: Int) {
        ensure(1)
        array[size++] = (value and 0xFF).toByte()
    }

    fun u16(value: Int) {
        u8(value)
        u8(value ushr 8)
    }

    fun u32(value: Long) {
        u8((value and 0xFF).toInt())
        u8(((value ushr 8) and 0xFF).toInt())
        u8(((value ushr 16) and 0xFF).toInt())
        u8(((value ushr 24) and 0xFF).toInt())
    }

    fun bytes(source: ByteArray) {
        ensure(source.size)
        source.copyInto(array, size)
        size += source.size
    }

    fun toByteArray(): ByteArray = array.copyOf(size)

    private fun ensure(extra: Int) {
        if (size + extra <= array.size) return
        var capacity = array.size.coerceAtLeast(1)
        while (capacity < size + extra) capacity *= 2
        array = array.copyOf(capacity)
    }
}

/** Standard CRC-32 (the polynomial ZIP uses). */
internal object Crc32 {

    private val TABLE = IntArray(256) { index ->
        var value = index
        repeat(8) {
            value = if (value and 1 != 0) (value ushr 1) xor 0xEDB88320.toInt() else value ushr 1
        }
        value
    }

    fun of(bytes: ByteArray): Long {
        var crc = 0.inv()
        for (byte in bytes) {
            crc = TABLE[(crc xor byte.toInt()) and 0xFF] xor (crc ushr 8)
        }
        return crc.inv().toLong() and 0xFFFFFFFFL
    }
}

/**
 * Builds a ZIP archive with every entry **stored** (no compression).
 *
 * An `.xlsx` file is just a ZIP of XML parts, and Excel, Numbers, LibreOffice and Google Sheets
 * all accept stored entries — which lets us produce a genuine spreadsheet without pulling in a
 * deflate implementation. Reports are small enough that the size cost does not matter.
 */
internal class ZipBuilder {

    private class Entry(val name: ByteArray, val crc: Long, val size: Int, val offset: Int)

    private val out = ByteBuf()
    private val entries = mutableListOf<Entry>()

    fun add(path: String, content: ByteArray) {
        val name = path.encodeToByteArray()
        val crc = Crc32.of(content)
        val offset = out.size

        out.u32(0x04034B50)          // local file header signature
        out.u16(20)                  // version needed to extract
        out.u16(0x0800)              // flags: file name is UTF-8
        out.u16(0)                   // method: stored
        out.u16(0)                   // modification time
        out.u16(0x0021)              // modification date (1980-01-01)
        out.u32(crc)
        out.u32(content.size.toLong())
        out.u32(content.size.toLong())
        out.u16(name.size)
        out.u16(0)                   // extra field length
        out.bytes(name)
        out.bytes(content)

        entries.add(Entry(name, crc, content.size, offset))
    }

    fun add(path: String, xml: String) = add(path, xml.encodeToByteArray())

    fun build(): ByteArray {
        val directoryOffset = out.size
        for (entry in entries) {
            out.u32(0x02014B50)      // central directory header signature
            out.u16(20)              // version made by
            out.u16(20)              // version needed to extract
            out.u16(0x0800)
            out.u16(0)
            out.u16(0)
            out.u16(0x0021)
            out.u32(entry.crc)
            out.u32(entry.size.toLong())
            out.u32(entry.size.toLong())
            out.u16(entry.name.size)
            out.u16(0)               // extra field length
            out.u16(0)               // comment length
            out.u16(0)               // disk number
            out.u16(0)               // internal attributes
            out.u32(0)               // external attributes
            out.u32(entry.offset.toLong())
            out.bytes(entry.name)
        }
        val directorySize = out.size - directoryOffset

        out.u32(0x06054B50)          // end of central directory
        out.u16(0)                   // this disk
        out.u16(0)                   // disk with central directory
        out.u16(entries.size)
        out.u16(entries.size)
        out.u32(directorySize.toLong())
        out.u32(directoryOffset.toLong())
        out.u16(0)                   // comment length

        return out.toByteArray()
    }
}
