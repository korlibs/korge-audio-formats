package korlibs.audio.mod

/*
import korlibs.audio.format.WAV
import korlibs.audio.internal.coerceToShort
import korlibs.audio.internal.toSampleShort
import korlibs.audio.mod.readMOD
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.AudioSamples
import korlibs.audio.sound.AudioSamplesInterleaved
import korlibs.audio.sound.infinitePlaybackTimes
import korlibs.audio.sound.toData
import korlibs.io.async.suspendTest
import korlibs.io.file.std.resourcesVfs
import korlibs.io.file.writeToFile
import doIOTest
import kotlin.test.Test

class XMTest {
    @Test
    fun test() = suspendTest({ doIOTest }) {
        //val sound = resourcesVfs["GUITAROU.MOD"].readMOD()
        //sound.playAndWait(times = infinitePlaybackTimes)
        val bytes = resourcesVfs["transatlantic.xm"].readBytes()
        //val bytes = resourcesVfs["poliamber.xm"].readBytes()
        val xm = XM()
        xm.load(bytes)

        val NSAMPLES = 16
        //val NSAMPLES = 128
        //val NSAMPLES = 16000
        //val NSAMPLES = 44100 * 24

        val e = XM.AudioEvent(44100, 0.0, XM.AudioBuffer(NSAMPLES))
        xm.audio_cb(e)

        //WAV.encodeToByteArray(xm.createAudioStream().toData(NSAMPLES)).writeToFile("/tmp/lol2.wav")
        //println(ev.outputBuffer.channels[0].toList())
        //println(ev)
    }
}
*/
