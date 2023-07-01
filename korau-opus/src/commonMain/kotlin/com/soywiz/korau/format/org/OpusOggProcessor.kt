package com.soywiz.korau.format.org

import korlibs.audio.format.org.concentus.*
import korlibs.audio.sound.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.memory.*
import kotlin.math.*

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

    suspend fun readAndDecodePacket(s: AsyncStream): Int {
        val packet = OggProcessor.readPacket(s)
        val ss = packet.data.openFastStream()
        //println(packet)

        val opusHead = ss.readStringz(8)
        when (opusHead) {
            "OpusHead" -> {
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
                    channelMap)

                //println("head=$head")

                //decoder.opus_decoder_init(head.inputSampleRate, head.channelCount)
                decoder.opus_decoder_init(rate, channels)

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
                val pcmSamplePosition = packet.granulePosition - head.preSkip
                //println("pcmSamplePosition=$pcmSamplePosition, granulePosition=${packet.granulePosition}, preSkip=${head.preSkip}")
                // 'PCM sample position' = 'granule position' - 'pre-skip' .
                var pos = 0

                //println(packet.segmentsSize.map { it.toInt() })
                //val packetInfo = OpusPacketInfo.parseOpusPacket(packet.data, 0, 8)
                //println("packetInfo=$packetInfo")
                //println(OpusPacketInfo.getEncoderMode(packet.data, 0))
                //val audioData = OpusAudioData(packet.data)
                //println("audioData=${audioData.numberOfFrames} : ${audioData.numberOfSamples}")

                //val dataPacket = packet.data.sliceArray(8 until packet.data.size)

                //val TOC = packet.data[0]

                //println(decoder.frame_size)
                //val spcm = ShortArray(min(decoder.frame_size, 5760) * channels)
                val ichannels = head.channelCount
                val spcm = ShortArray(max(rate / 400, 5760) * ichannels)

                //println("DATA_SIZE: ${packet.data.size}")
                //println("OPUS_HEAD: ${head}")

                //val deque = AudioSamplesDeque(channels)

                var samplesDecoded = 0
                for (n in 0 until packet.segmentsSize.size) {
                    val segmentSize = packet.segmentsSize[n].toInt() and 0xFF
                    //println("SEGMENT[$n]: $segmentSize")

                    pending.append(packet.data, pos, segmentSize)

                    if (segmentSize < 255) {
                        val packetData = pending.toByteArray()
                        pending.clear()

                        val read = decoder.decode(packetData, 0, packetData.size, spcm, 0, spcm.size / ichannels, false)
                        samples.write(AudioSamplesInterleaved(ichannels, read, spcm))
                        samplesDecoded += read
                        //println("READ: $read")
                    }

                    //val info = OpusPacketInfo.parseOpusPacket(segmentData, 0, min(1275, packet.data.size))
                    //println("info=$info : ${info.Frames.size}")

                    //for (frame in info.Frames) {
                    //}

                    pos += segmentSize
                }

                //println("Fs=${Fs}")
                //val data = deque.toData(Fs)
                //data.toWav().writeToFile("/tmp/output.wav")
                return samplesDecoded
                //error("$opusHead != OpusHead/OpusTags")
                //val dataLen = data.read(dataPacket, 0, dataPacket.size)
            }
        }
    }
}
