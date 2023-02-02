package com.soywiz.korau.format.opus

import com.soywiz.korau.format.*
import com.soywiz.korau.format.org.concentus.*
import com.soywiz.korau.sound.*
import com.soywiz.korio.stream.*
import kotlin.math.*

object Opus : OpusAudioFormatBase()

open class OpusAudioFormatBase : AudioFormat("opus") {
	override suspend fun decodeStream(data: AsyncStream, props: AudioDecodingProps): AudioStream? {
		return AudioStreamOpusDecoder(48000, 2, data)
	}

	class AudioStreamOpusDecoder(rate: Int, channels: Int, val data: AsyncStream) : AudioStream(rate, channels) {
		val decoder = OpusDecoder(rate, channels)
		val dataPacket = ByteArray(4096)

		override suspend fun clone(): AudioStream = AudioStreamOpusDecoder(rate, channels, data.duplicate())

		override suspend fun read(out: AudioSamples, offset: Int, length: Int): Int {
			val spcm = ShortArray(min(decoder.frame_size, 5760) * channels)


			val dataLen = data.read(dataPacket, 0, dataPacket.size)
			val read = decoder.decode(dataPacket, 0, dataLen, spcm, 0, length, false)

			// @TODO: Optimize this, and add a method to korau to make this simpler (like write a ShortArray with a stride)
			AudioSamplesInterleaved(decoder.channels, read, spcm).separated(out)

			//for (ch in 0 until channels) {
			//	for (n in 0 until read) {
			//		out[ch, n] = spcm[n * 2 + ch]
			//	}
			//}
			return read
		}
	}
}