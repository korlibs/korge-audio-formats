import korlibs.audio.format.*
import korlibs.audio.format.mp3.*
import korlibs.audio.format.opus.*
import korlibs.audio.mod.*
import korlibs.audio.sound.*
import korlibs.io.file.std.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.time.*

class MainOpusScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        measureTime({
            defaultAudioFormats.register(MOD, S3M, XM, OPUS)
        }) {
            println("Registered sound module track formats in $it")
        }
        //println(": " + MP3.tryReadInfo(resourcesVfs["sounds/8Khz-Mono.opus"].open()))
        //println(": " + MP3Decoder.tryReadInfo(resourcesVfs["sounds/8Khz-Mono.opus"].open()))
        //val data = resourcesVfs["sounds/8Khz-Mono.opus"].readSound(AudioDecodingProps(formats = OPUS)).toAudioData()
        val data = resourcesVfs["sounds/8Khz-Mono.opus"].readSound()
        data.play()
    }
}
