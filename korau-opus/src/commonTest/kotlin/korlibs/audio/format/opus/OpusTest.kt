package korlibs.audio.format.opus

import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.io.async.*
import korlibs.io.stream.*
import korlibs.encoding.*
import korlibs.io.file.std.*
import kotlin.test.*

class OpusTest {
	val smallFile = """
    T2dnUwACAAAAAAAAAABxNV5nAAAAAIjXVq0BE09wdXNIZWFkAQI4AUSsAAAAAABPZ2dTAAAAAAAAAAAAAHE1XmcBAAAAiVqDTwP///5PcHVzVGFncw0A
    AABsaWJvcHVzIDEuMi4xAgAAACYAAABFTkNPREVSPW9wdXNlbmMgZnJvbSBvcHVzLXRvb2xzIDAuMS4xMBsAAABFTkNPREVSX09QVElPTlM9LS1iaXRy
    YXRlIDYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE9nZ1MAAIC7AAAAAAAAcTVeZwIAAAB2z0rBMhMV
    ERISExASEBMQEBMUFBYTEBQQFhQXFhkTFhcUFxcXFBYXExQTFRgWExYUGhUWFBcXCIUHtb7Lp3nCHrTJwbpfOml+Cwi2fjnspIiB+Gs4fxtHjCvpCkVL
    gAivzsDqUI0e68cETPVJ+58cCK6aSvr+4r63xB6jsqXY9D0gCK6aSvr+4skZeCw9GypiZkuACK60sBifeIfpKJ/URwOLga696Aiumkr6/uK+xeOKdfyV
    lYIIrppK+v7iyMibED3ale0WgUAIrppK+v7ivr6l9Ua5ZnisCK6aSvr+4sjITx6dHsJhF0teKAiumkr6+mco3yPgFBCUfiAIrppK+v7ixh4GCSmRLMDC
    CK6aSvr+4skZtt8/1bjMvye5+Qiumkr6/uK+vqX6a+B4bLMonw5YCK6aSvr+4sjIlYCMsYH3UsKBQeIIrppK+v7iyTp/0/qO72oyWGcY4ECQCK6aSvr+
    4sYeRH0QJs8OnFkzrAiumkr6/uLJFE5zQW/frmUIrppK+v7ixhiIiry2WQKmGupQkgiumkr6/uLIyEnMKFsaWYAIrrSwGJ94hJDYBBIb+LhdbQJydJHG
    CK6aSvr+4r4g6NmEv6D/OzRfLKAIrppK+v7iyMibOQWRwfqhdOFEFHkqDAiwWrfHlv5oSRaDp5S8DFdp2ZJU5IAIsE0JBPVQnOIR34bNlP1nPrI7vmPf
    t+AQCLBat8eW/mj+FFmm7nmbk+aMOAiwWrfKwPtOeP1gWMMcHwgsXYJQjJcIsFq3x5b+Z6JWPn+CGPD8Sx+QegRdfgiwWrfHlv5noltxEND5FGzUfDj7
    CLBNCQT1UJziEhqBiqia5eyV2zh40SwIsFq3x5b+Z6JRradE6+RH2BTCvLCWgQiwWrfKwPtOeP2nVTQvvYYnPJUT6qxQCLBat8eW/meiYAksDbOrXJli
    3kAIsFq3x5b+Z6JRpOjE/DzzwO0Ftw5BCLBNCQT1UJziErtypYAV0EIHjx5JusQIsFq3x5b+Z6JWJpdXazMQAMXgCLBat8rA+054/WAxJaXl0E57KvkI
    sFq3x5b+Z6JWPnqNJfgPxWG2CLBat8eW/meiUZdqfrpahIKgJyiQCLBNCQT1UJziEm86UECR0T9+0EAmiLFpCLBat8eW/meiVjpOIw2T6FIHQeWq4Aiw
    WrfKwPtOeP1XixBl7Vrm5GAIsFq3x5b+Z6JWPRVyucH5H9KLuQpACLBat8eW/meiVj6Q3dlFMNYh8yAIsE0JBPVQnOISLc9T+3U/aBHKMAVA86wBaAiw
    WrfHlv5nolFElKEGf1fzj8wjzgiwWrfKwPtOeP3cI/RXOS7F4kSYGYAIsFq3x5b+Z6JWPUWmf3AvuvzgoAiwWrfHlv5nolt4B37dw5ALoojvW8peCLBN
    CQT1UJziEm9R0GstK8H2YKvSTEhPZ2dTAAS4vAAAAAAAAHE1XmcDAAAAKYx7ZAEcCLZbLKqnsQc44eEK/Bb0IzlokzjwLaO7vH+sjQ==""".fromBase64(ignoreSpaces = true)

	@Test
	fun name() = suspendTest {
		val data = OPUS.decode(smallFile.openAsync())!!
		println(data)
	}

    @Test
    fun test2() = suspendTest {
        val sound = resourcesVfs["opus1.opus"].readSound(OPUS.toProps())
        val data = sound.toAudioData()
        println(data)
    }

    @Test
    fun test3() = suspendTest {
        val sound = resourcesVfs["opus1.opus"].readSound(OPUS.toProps())
        val data = sound.toAudioData()
        println(data)
    }

    @Test
    fun test4() = suspendTest {
        val sound = resourcesVfs["8Khz-Mono.opus"].readSound(OPUS.toProps())
        val data = sound.toAudioData()
        println(data)
    }

    @Test
    fun test4Music() = suspendTest {
        val sound = resourcesVfs["8Khz-Mono.opus"].readMusic(OPUS.toProps())
        val data = sound.toAudioData()
        println(data)
        assertEquals("AudioData(rate=48000, channels=2, samples=839040)", "$data")
    }

    @Test
    fun testStereoIssue() = suspendTest {
        val sound = resourcesVfs["Snowland.opus"].readMusic(OPUS.toProps())
        val data = sound.toAudioData()

        // @TODO: Compare against an Acoustic fingerprint (to support small changes due to implementation rounding errors): https://en.wikipedia.org/wiki/Acoustic_fingerprint
        // The number of samples should be fixed al least though
        assertEquals("AudioData(rate=48000, channels=2, samples=566400)", "$data")
    }

    fun AudioFormat.toProps(): AudioDecodingProps = AudioDecodingProps(formats = this)
}
