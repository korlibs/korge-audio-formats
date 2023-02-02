package com.soywiz.korau.mod

import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import kotlin.test.*

class MODTest {
    @Test
    fun test() = suspendTest({ doIOTest }) {
        val sound = resourcesVfs["GUITAROU.MOD"].readMOD()
        val data = sound.toAudioData(maxSamples = 44100 * 4)
        //WAV.encodeToByteArray(data).writeToFile("/tmp/guitarou.wav")
        //sound.playAndWait()
        //val mod = Protracker()
        //mod.parse(Uint8Buffer(NewInt8Buffer(MemBufferWrap(bytes), 0, bytes.size)))
        //val out = arrayOf(FloatArray(8000), FloatArray(8000))
        //mod.playing = true
        //mod.mix(mod, out)
        //println(out)
    }
}
