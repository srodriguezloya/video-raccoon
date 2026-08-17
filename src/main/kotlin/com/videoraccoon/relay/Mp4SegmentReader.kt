package com.videoraccoon.relay

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream

// Groups the raw MP4 boxes go2rtc streams (ftyp, moov, then repeating
// moof/mdat pairs per frame - verified against a live stream) into the two
// shapes a client needs: one init segment (ftyp+moov), then one segment per
// media fragment (moof+mdat). Bytes are forwarded exactly as received - this
// only locates segment boundaries, it never re-encodes anything.
//
// Assumes 32-bit box sizes (no 64-bit extended-size boxes): true for this
// per-frame fragment stream, where every box is at most a few KB.
class Mp4SegmentReader(inputStream: InputStream) {
    private val input = DataInputStream(inputStream)

    fun readNextSegment(): ByteArray? {
        val buffer = ByteArrayOutputStream()
        while (true) {
            val box = readBox() ?: return if (buffer.size() > 0) buffer.toByteArray() else null
            buffer.write(box.bytes)
            if (box.type == "moov" || box.type == "mdat") {
                return buffer.toByteArray()
            }
        }
    }

    private fun readBox(): Mp4Box? {
        val header = ByteArray(8)
        if (!readFully(header)) return null

        val size = ((header[0].toLong() and 0xFF) shl 24) or
            ((header[1].toLong() and 0xFF) shl 16) or
            ((header[2].toLong() and 0xFF) shl 8) or
            (header[3].toLong() and 0xFF)
        val type = String(header, 4, 4, Charsets.US_ASCII)

        val payload = ByteArray((size - 8).toInt())
        if (!readFully(payload)) return null

        return Mp4Box(type, header + payload)
    }

    private fun readFully(dest: ByteArray): Boolean {
        var offset = 0
        while (offset < dest.size) {
            val read = input.read(dest, offset, dest.size - offset)
            if (read == -1) return false
            offset += read
        }
        return true
    }
}

private data class Mp4Box(val type: String, val bytes: ByteArray)
