import korlibs.time.measureTime
import korlibs.time.milliseconds
import korlibs.time.seconds
import korlibs.time.toTimeString
import korlibs.audio.format.defaultAudioFormats
import korlibs.audio.mod.MOD
import korlibs.audio.mod.S3M
import korlibs.audio.mod.XM
import korlibs.audio.sound.infinitePlaybackTimes
import korlibs.audio.sound.readMusic
import korlibs.event.Key
import korlibs.korge.input.keys
import korlibs.korge.scene.Scene
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiComboBox
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs

class MainMODScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        measureTime({
            defaultAudioFormats.register(MOD, S3M, XM)
        }) {
            println("Registered sound module track formats in $it")
        }
        println(defaultAudioFormats.extensions)
        val soundsFolder = resourcesVfs["sounds"]
        val sound = measureTime({ soundsFolder["GUITAROU.MOD"].readMusic() }) {
        //val sound = measureTime({ soundsFolder["GUITAROU.MOD"].readSound() }) {
            println("Read music file in $it")
        }
        //val sound = resourcesVfs["sounds/_sunlight_.xm"].readXM()
        //val sound = resourcesVfs["sounds/12oz.s3m"].readS3M()
        //val sound = resourcesVfs["sounds/poliamber.xm"].readXM()
        //val sound = resourcesVfs["sounds/transatlantic.xm"].readXM()
        var channel = sound.play(times = infinitePlaybackTimes)
        //val channel = sound.play(times = 2.playbackTimes)
        lateinit var timer: UIText
        uiVerticalStack(width = 400f) {
            uiComboBox(items = soundsFolder.listNames().filter { it.substringAfterLast('.').lowercase() in defaultAudioFormats.extensions }).also {
                it.onSelectionUpdate { box ->
                    launchImmediately {
                        channel.stop()
                        val sound = measureTime({ soundsFolder[box.selectedItem!!].readMusic() }) {
                            println("Read music file in $it")
                        }
                        channel = sound.play(times = infinitePlaybackTimes)
                    }
                }
            }
            timer = uiText("time: -")
        }
        addUpdater {
            timer.text = "time: ${channel.current.toTimeString()}/${channel.total.toTimeString()}"
        }
        keys {
            down(Key.ENTER) {
                channel.current = 0.milliseconds
            }
            down(Key.LEFT) {
                channel.current = (channel.current - if (it.shift) 10.seconds else 1.seconds) umod channel.total
            }
            down(Key.RIGHT) {
                channel.current += if (it.shift) 10.seconds else 1.seconds
            }
        }
    }
}
