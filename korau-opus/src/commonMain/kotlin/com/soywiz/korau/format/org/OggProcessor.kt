package com.soywiz.korau.format.org

import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.io.util.*
import korlibs.memory.*
import kotlinx.coroutines.flow.*

object OggProcessor {
    data class OggPacket(
        val version: Int,
        val headerType: Int,
        val granulePosition: Long,
        val bitstreamSerialNumber: Int,
        val pageSequenceNumber: Int,
        val checksum: Int,
        val segmentsSize: UByteArray,
        val data: ByteArray,
    ) {
        val continued: Boolean get() = headerType.hasBitSet(0)
        val firstPageLogicalBitstream: Boolean get() = headerType.hasBitSet(1)
        val lastPageOfLogicalBitstream: Boolean get() = headerType.hasBitSet(2)
    }

    suspend fun readPackets(s: AsyncStream): Flow<OggPacket> = flow {
        while (s.getAvailable() > 0L) {
            emit(readPacket(s))
        }
    }

    suspend fun readPacket(s: AsyncStream): OggPacket {
        //println("AVAILABLE: " + s.getAvailable())
        val ss = s.readBytesExact(27).openFastStream()
        val magic = ss.readStringz(4)
        if (magic != "OggS") error("Not an Ogg stream but '$magic'")
        val version = ss.readU8()
        val headerType = ss.readU8()
        val granulePosition = ss.readS64LE()
        val bitstreamSerialNumber = ss.readS32LE()
        val pageSequenceNumber = ss.readS32LE()
        val checksum = ss.readS32LE()
        val nsegments = ss.readU8()
        val segmentsSize = s.readBytesExact(nsegments)
        val totalDataSize = segmentsSize.sumOf { it.toInt() and 0xFF }
        val bytes = s.readBytesExact(totalDataSize)

        return OggPacket(
            version,
            headerType,
            granulePosition,
            bitstreamSerialNumber,
            pageSequenceNumber,
            checksum,
            segmentsSize.toUByteArray(),
            bytes,
        )
        //println("version=$version, headerType=$headerType, granulePosition=$granulePosition, bitstreamSerialNumber=$bitstreamSerialNumber, pageSequenceNumber=$pageSequenceNumber, checksum=$checksum, nsegments=$nsegments")
        //println(segmentsSize.toList())
        //for (n in 0 until segmentsSize.size) {
        //    val segmentSize = segmentsSize[n].toInt() and 0xFF
        //    println("bytes=${bytes.toString(Charsets.UTF8)}")
        //}
    }
}


fun FastByteArrayInputStream.readStringzFix(charset: Charset = UTF8): String {
    var out = ByteArrayBuilder()
    while (hasMore) {
        val byte = readU8().toByte()
        if (byte == 0.toByte()) break
        out.append(byte)
    }
    return out.toByteArray().toString(charset)
}
