package korlibs.audio.format.opus

import com.soywiz.korau.format.org.*
import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.memory.*
import korlibs.time.*

object OPUS : OpusAudioFormatBase()

open class OpusAudioFormatBase : AudioFormat("opus") {
    companion object {
        val OggS = "OggS".toByteArray()
        val OpusHead = "OpusHead".toByteArray()
    }

    override suspend fun tryReadInfo(data: AsyncStream, props: AudioDecodingProps): Info? {
        val bytes = data.sliceStart().readBytesUpTo(0x40)
        if (bytes.indexOf(OggS) != 0) return null
        if (bytes.indexOf(OpusHead) < 0) return null

        val processor = OpusOggProcessor()
        processor.readAndDecodePacket(data, decode = false)
        val header = processor.head

        var totalTime: TimeSpan? = null

        if (props.exactTimings != false) {
            for (lastChunkSize in listOf(0x200, 0x2000, 0x4000)) {
                val lastChunk = data.sliceStart(kotlin.math.max(0L, data.getLength() - lastChunkSize)).readAll()
                val index = lastChunk.lastIndexOf(OggS)
                if (index >= 0) {
                    val packet = OggProcessor.readPacket(lastChunk.openAsync().sliceStart(index.toLong()))
                    //val pcmSamplePosition = (packet.granulePosition / header.channelCount) - header.preSkip
                    val pcmSamplePosition = packet.granulePosition - header.preSkip
                    totalTime = (pcmSamplePosition.toDouble() / 48000.toDouble()).seconds
                    break
                }
            }
        }

        return Info(totalTime, header.channelCount)
    }

    override suspend fun decodeStreamInternal(data: AsyncStream, props: AudioDecodingProps): AudioStream? {
        val info = tryReadInfo(data.sliceStart()) ?: return null
		return AudioStreamOpusDecoder(48000, 2, data, info)
	}

	class AudioStreamOpusDecoder(rate: Int, channels: Int, val data: AsyncStream, val info: Info) : AudioStream(rate, channels) {
        val processor = OpusOggProcessor(channels, rate)

        override val totalLengthInSamples: Long?
            get() = info.duration?.let { (it.seconds * rate).toLong() }

        private var seeked = true
        var _currentPositionInSamples: Long = 0L

        override var currentPositionInSamples: Long
            get() = _currentPositionInSamples
            set(value) {
                seeked = true
                _currentPositionInSamples = value
            }

        override suspend fun clone(): AudioStream = AudioStreamOpusDecoder(rate, channels, data.duplicate(), info)

		override suspend fun read(out: AudioSamples, offset: Int, length: Int): Int {
            if (seeked) {
                seeked = false
                processor.seek(data, _currentPositionInSamples)
            }

            var n = 0
            while (processor.samples.availableRead < length && data.getAvailable() > 0L) {
                processor.readAndDecodePacket(data)
                n++
                if (n >= 1024) break
            }

            if (processor.samples.availableRead <= 0) return -1
            val read = processor.samples.read(out, offset, length)
            if (read > 0) {
                _currentPositionInSamples += read
            }
            return read
		}
	}
}

private fun ByteArray.lastIndexOf(sub: ByteArray, starting: Int = 0): Int = array_lastIndexOf(starting, size, sub.size) { n, m -> this[n] == sub[m] }

@PublishedApi
internal inline fun array_lastIndexOf(starting: Int, selfSize: Int, subSize: Int, crossinline equal: (n: Int, m: Int) -> Boolean): Int {
    for (n in (selfSize - subSize - 1) downTo starting) {
        var eq = 0
        for (m in 0 until subSize) {
            if (!equal(n + m, m)) {
                break
            }
            eq++
        }
        if (eq == subSize) {
            return n
        }
    }
    return -1
}
