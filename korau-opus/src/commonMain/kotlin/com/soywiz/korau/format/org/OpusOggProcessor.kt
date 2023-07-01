package com.soywiz.korau.format.org

import korlibs.audio.format.*
import korlibs.audio.format.org.concentus.*
import korlibs.audio.format.org.gragravarr.ogg.*
import korlibs.audio.format.org.gragravarr.opus.*
import korlibs.audio.sound.*
import korlibs.io.file.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import kotlin.math.*

class OpusOggProcessor {
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

    suspend fun readPacket(s: AsyncStream) {
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

                println("head=$head")

                //decoder.opus_decoder_init(head.inputSampleRate, head.channelCount)
                decoder.opus_decoder_init(48000, 2)

                return
            }
            "OpusTags" -> {
                fun FastByteArrayInputStream.readStr(): String = readBytesExact(readS32LE()).toString(UTF8)

                val vendor = ss.readStr()
                println("VENDOR:$vendor")
                val count = ss.readS32LE()
                for (n in 0 until count) {
                    println("TAG:" + ss.readStr())
                }
                return
            }
            else -> {
                val pcmSamplePosition = packet.granulePosition - head.preSkip
                println("pcmSamplePosition=$pcmSamplePosition, granulePosition=${packet.granulePosition}, preSkip=${head.preSkip}")
                // 'PCM sample position' = 'granule position' - 'pre-skip' .
                var pos = 0

                println(packet.segmentsSize.map { it.toInt() })
                //val packetInfo = OpusPacketInfo.parseOpusPacket(packet.data, 0, 8)
                //println("packetInfo=$packetInfo")
                //println(OpusPacketInfo.getEncoderMode(packet.data, 0))
                //val audioData = OpusAudioData(packet.data)
                //println("audioData=${audioData.numberOfFrames} : ${audioData.numberOfSamples}")

                //val dataPacket = packet.data.sliceArray(8 until packet.data.size)

                //val TOC = packet.data[0]

                val Fs = 48000
                val channels = 2
                //println(decoder.frame_size)
                //val spcm = ShortArray(min(decoder.frame_size, 5760) * channels)
                val spcm = ShortArray(max(Fs / 400, 5760) * channels)

                println("DATA_SIZE: ${packet.data.size}")

                val deque = AudioSamplesDeque(2)

                for (n in 0 until packet.segmentsSize.size) {
                    val segmentSize = packet.segmentsSize[n].toInt() and 0xFF
                    println("SEGMENT[$n]: $segmentSize")

                    //val info = OpusPacketInfo.parseOpusPacket(segmentData, 0, min(1275, packet.data.size))

                    //println("info=$info : ${info.Frames.size}")

                    //for (frame in info.Frames) {
                        val read = decoder.decode(packet.data, pos, segmentSize, spcm, 0, spcm.size / channels, false)
                    deque.write(AudioSamplesInterleaved(channels, read, spcm))
                        println("READ: $read")
                    //}

                    pos += segmentSize
                }

                println("Fs=${Fs}")
                val data = deque.toData(Fs)
                data.toWav().writeToFile("/tmp/output.wav")
                //error("$opusHead != OpusHead/OpusTags")
                //val dataLen = data.read(dataPacket, 0, dataPacket.size)
            }
        }
    }
}
