package korlibs.audio.format.opus

import com.soywiz.korau.format.org.*
import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.memory.*
import kotlin.collections.indexOf

object OPUS : OpusAudioFormatBase()

open class OpusAudioFormatBase : AudioFormat("opus") {
    override suspend fun tryReadInfo(data: AsyncStream, props: AudioDecodingProps): Info? {
        val bytes = data.sliceStart().readBytesUpTo(0x40)
        if (bytes.indexOf("OggS".toByteArray()) != 0) return null
        if (bytes.indexOf("OpusHead".toByteArray()) < 0) return null
        val processor = OpusOggProcessor()
        processor.readAndDecodePacket(data)

        return Info(null, processor.channels)
    }

    override suspend fun decodeStreamInternal(data: AsyncStream, props: AudioDecodingProps): AudioStream? {
		return AudioStreamOpusDecoder(48000, 2, data)
	}

	class AudioStreamOpusDecoder(rate: Int, channels: Int, val data: AsyncStream) : AudioStream(rate, channels) {
        val processor = OpusOggProcessor(channels, rate)

		override suspend fun clone(): AudioStream = AudioStreamOpusDecoder(rate, channels, data.duplicate())

		override suspend fun read(out: AudioSamples, offset: Int, length: Int): Int {
            var n = 0
            while (processor.samples.availableRead < length && data.getAvailable() > 0L) {
                processor.readAndDecodePacket(data)
                n++
                if (n >= 1024) break
            }

            if (processor.samples.availableRead <= 0) return -1
            return processor.samples.read(out, offset, length)
		}
	}
}
