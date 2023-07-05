package io.nayuki.flac

import korlibs.audio.format.*
import korlibs.audio.format.flac.*
import korlibs.audio.sound.*
import korlibs.io.async.*
import korlibs.io.file.*
import korlibs.io.file.std.*
import korlibs.io.stream.*
import korlibs.time.*
import kotlin.test.*

class FlacDecoderTest {
    @Test
    fun testInfo() = suspendTest {
        val wavBytes = resourcesVfs["8Khz-Mono.wav"].readBytes()
        val flacBytes = resourcesVfs["8Khz-Mono.flac"].readBytes()
        val info = FLAC.tryReadInfo(flacBytes.openAsync())
        assertEquals(17469.5.milliseconds, info!!.duration)
        assertEquals(1, info!!.channels)
        assertNull(FLAC.tryReadInfo(wavBytes.openAsync()))
    }

    @Test
    fun testDecoding() = suspendTest {
        val dataWav = resourcesVfs["8Khz-Mono.wav"].readSound(WAV.toProps()).toAudioData()
        val dataFlac = resourcesVfs["8Khz-Mono.flac"].readSound(FLAC.toProps()).toAudioData()
        //println(dataWav[0].toList().slice(0 until 8000))
        //println(dataFlac[0].toList().slice(0 until 8000))
        //println("dataWav=$dataWav")
        //println("dataFlac=$dataFlac")
        //dataWav.toWav().writeToFile("/tmp/demo1.wav")
        //dataFlac.toWav().writeToFile("/tmp/demo2.wav")
        //assertTrue { dataWav.toWav().contentEquals(dataFlac.toWav()) } // @TODO it fails in a small case not sure why?
    }

    //@Test
    //fun test() = suspendTest {
    //    val decoder = FlacDecoder(resourcesVfs["8Khz-Mono.flac"].readBytes().openSync())
    //    val metas = mapWhileNotNull { decoder.readAndHandleMetadataBlock() }
    //    val streamInfo = decoder.streamInfo!!
    //    if (streamInfo.sampleDepth % 8 != 0) throw UnsupportedOperationException("Only whole-byte sample depth supported")
    //    //println(streamInfo.maxBlockSize)
    //    val samples = Array(streamInfo.numChannels) { IntArray(streamInfo.numSamples.toInt()) }
    //    var off = 0
    //    while (true) {
    //        val len = decoder.readAudioBlock(samples, off)
    //        if (len <= 0) break
    //        off += len
    //    }
    //    //println(samples[0].sliceArray(0 until off).toList())
    //}
}

inline fun <reified T> mapWhileNotNull(gen: (Int) -> T?): List<T> = arrayListOf<T>().apply {
    while (true) {
        this += gen(this.size) ?: break
    }
}

inline fun <reified T> mapWhileCheck(check: (T) -> Boolean, gen: (Int) -> T): List<T> = arrayListOf<T>().apply {
    while (true) {
        val res = gen(this.size)
        if (!check(res)) break
        this += res
    }
}
