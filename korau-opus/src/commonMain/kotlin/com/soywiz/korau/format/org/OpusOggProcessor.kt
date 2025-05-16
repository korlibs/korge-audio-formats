package com.soywiz.korau.format.org

import korlibs.audio.format.org.concentus.*
import korlibs.audio.sound.*
import korlibs.datastructure.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.memory.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

// https://xiph.org/ogg/doc/oggstream.html
// https://xiph.org/ogg/doc/framing.html
// https://wiki.xiph.org/OggOpus
// https://datatracker.ietf.org/doc/html/rfc3533
// https://datatracker.ietf.org/doc/html/rfc7845
class OpusOggProcessor(
    val channels: Int = 2,
    val rate: Int = 48000,
) {
    data class OpusHead(
        val version: Int = 0,
        val channelCount: Int = 0,
        val preSkip: Int = 0,
        val inputSampleRate: Int = 0,
        val outputGain: Int = 0,
        val channelMap: Int = 0,
    )

    var head: OpusHead = OpusHead()
    val decoder = OpusDecoder()
    var pending = ByteArrayBuilder()
    var vendor: String? = null
    var tags: List<String> = emptyList()
    val samples = AudioSamplesDeque(channels)
    var lastPcmSamplePosition = 0L
    val filePositions = doubleArrayListOf()
    val pcmPositions = doubleArrayListOf()

    suspend fun seek(s: AsyncStream, positionInSamples: Long) {
        samples.clear()

        //if (positionInSamples == 0L) return

        if (filePositions.isEmpty()) {
            s.position = 0L
            while (s.getAvailable() > 0) {
                val lastPosition = s.position
                readAndDecodePacket(s, decode = false)
                filePositions.add(lastPosition.toDouble())
                pcmPositions.add(lastPcmSamplePosition.toDouble())
            }
        }

        //println("filePositions: $filePositions")
        //println("pcmPositions: $pcmPositions")

        val index = genericBinarySearchLeft(0, filePositions.size) { pcmPositions[it].compareTo(positionInSamples) }
        if (index in 0 until filePositions.size) {
            s.position = filePositions[index].toLong()
            //println(" SEEK to ${s.position} to get positionInSamples=$positionInSamples")
        }
    }

    suspend fun readAndDecodePacket(s: AsyncStream, decode: Boolean = true): Int {
        val packet = readPacket(s)
        val ss = packet.data.openFastStream()
        //println(packet)

        val opusHead = ss.readStringz(8)
        when (opusHead) {
            "OpusHead" -> {
                lastPcmSamplePosition = 0L

                val version = ss.readU8()
                val channelCount = ss.readU8()
                val preSkip = ss.readU16LE()
                val inputSampleRate = ss.readS32LE()
                val outputGain = ss.readU16LE()
                val channelMap = ss.readU8()

                head = OpusHead(version,
                    channelCount,
                    preSkip,
                    inputSampleRate,
                    outputGain,
                    channelMap
                )

                //println("head=$head")

                //decoder.opus_decoder_init(head.inputSampleRate, head.channelCount)
                //decoder.opus_decoder_init(rate, channels)
                decoder.opus_decoder_init(rate, head.channelCount)

                return 0
            }
            "OpusTags" -> {
                fun FastByteArrayInputStream.readStr(): String = readBytesExact(readS32LE()).toString(UTF8)

                vendor = ss.readStr()
                //println("VENDOR:$vendor")
                tags = (0 until ss.readS32LE()).map { ss.readStr() }
                //for (n in 0 until count) println("TAG:" + ss.readStr())
                return 0
            }
            else -> {
                val pcmSamplePosition = (packet.granulePosition / head.channelCount) - head.preSkip
                lastPcmSamplePosition = pcmSamplePosition
                if (decode) {
                    var pos = 0
                    val ichannels = head.channelCount
                    val spcm = ShortArray(max(rate / 400, 5760) * ichannels)

                    var samplesDecoded = 0
                    for (n in 0 until packet.segmentsSize.size) {
                        val segmentSize = packet.segmentsSize[n].toInt() and 0xFF

                        pending.append(packet.data, pos, segmentSize)

                        if (segmentSize < 255) {
                            val packetData = pending.toByteArray()
                            pending.clear()

                            val read = decoder.decode(packetData, 0, packetData.size, spcm, 0, spcm.size / ichannels, false)
                            samples.write(AudioSamplesInterleaved(ichannels, read * ichannels, AudioSampleArray(spcm)))
                            samplesDecoded += read
                            //println("READ: $read")
                        }

                        pos += segmentSize
                    }

                    return samplesDecoded
                } else {
                    return -1
                }
            }
        }
    }

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

    companion object {
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
}
