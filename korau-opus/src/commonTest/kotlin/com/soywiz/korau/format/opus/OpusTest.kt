package com.soywiz.korau.format.opus

import com.soywiz.korio.async.*
import com.soywiz.korio.stream.*
import com.soywiz.krypto.encoding.*
import kotlin.test.*

class OpusTest {
	val smallFile = "T2dnUwACAAAAAAAAAABxNV5nAAAAAIjXVq0BE09wdXNIZWFkAQI4AUSsAAAAAABP" +
			"Z2dTAAAAAAAAAAAAAHE1XmcBAAAAiVqDTwP///5PcHVzVGFncw0AAABsaWJvcHVz" +
			"IDEuMi4xAgAAACYAAABFTkNPREVSPW9wdXNlbmMgZnJvbSBvcHVzLXRvb2xzIDAu" +
			"MS4xMBsAAABFTkNPREVSX09QVElPTlM9LS1iaXRyYXRlIDYAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE9nZ1MAAIC7AAAAAAAAcTVeZwIAAAB2" +
			"z0rBMhMVERISExASEBMQEBMUFBYTEBQQFhQXFhkTFhcUFxcXFBYXExQTFRgWExYU" +
			"GhUWFBcXCIUHtb7Lp3nCHrTJwbpfOml+Cwi2fjnspIiB+Gs4fxtHjCvpCkVLgAiv" +
			"zsDqUI0e68cETPVJ+58cCK6aSvr+4r63xB6jsqXY9D0gCK6aSvr+4skZeCw9Gypi" +
			"ZkuACK60sBifeIfpKJ/URwOLga696Aiumkr6/uK+xeOKdfyVlYIIrppK+v7iyMib" +
			"ED3ale0WgUAIrppK+v7ivr6l9Ua5ZnisCK6aSvr+4sjITx6dHsJhF0teKAiumkr6" +
			"+mco3yPgFBCUfiAIrppK+v7ixh4GCSmRLMDCCK6aSvr+4skZtt8/1bjMvye5+Qiu" +
			"mkr6/uK+vqX6a+B4bLMonw5YCK6aSvr+4sjIlYCMsYH3UsKBQeIIrppK+v7iyTp/" +
			"0/qO72oyWGcY4ECQCK6aSvr+4sYeRH0QJs8OnFkzrAiumkr6/uLJFE5zQW/frmUI" +
			"rppK+v7ixhiIiry2WQKmGupQkgiumkr6/uLIyEnMKFsaWYAIrrSwGJ94hJDYBBIb" +
			"+LhdbQJydJHGCK6aSvr+4r4g6NmEv6D/OzRfLKAIrppK+v7iyMibOQWRwfqhdOFE" +
			"FHkqDAiwWrfHlv5oSRaDp5S8DFdp2ZJU5IAIsE0JBPVQnOIR34bNlP1nPrI7vmPf" +
			"t+AQCLBat8eW/mj+FFmm7nmbk+aMOAiwWrfKwPtOeP1gWMMcHwgsXYJQjJcIsFq3" +
			"x5b+Z6JWPn+CGPD8Sx+QegRdfgiwWrfHlv5noltxEND5FGzUfDj7CLBNCQT1UJzi" +
			"EhqBiqia5eyV2zh40SwIsFq3x5b+Z6JRradE6+RH2BTCvLCWgQiwWrfKwPtOeP2n" +
			"VTQvvYYnPJUT6qxQCLBat8eW/meiYAksDbOrXJli3kAIsFq3x5b+Z6JRpOjE/Dzz" +
			"wO0Ftw5BCLBNCQT1UJziErtypYAV0EIHjx5JusQIsFq3x5b+Z6JWJpdXazMQAMXg" +
			"CLBat8rA+054/WAxJaXl0E57KvkIsFq3x5b+Z6JWPnqNJfgPxWG2CLBat8eW/mei" +
			"UZdqfrpahIKgJyiQCLBNCQT1UJziEm86UECR0T9+0EAmiLFpCLBat8eW/meiVjpO" +
			"Iw2T6FIHQeWq4AiwWrfKwPtOeP1XixBl7Vrm5GAIsFq3x5b+Z6JWPRVyucH5H9KL" +
			"uQpACLBat8eW/meiVj6Q3dlFMNYh8yAIsE0JBPVQnOISLc9T+3U/aBHKMAVA86wB" +
			"aAiwWrfHlv5nolFElKEGf1fzj8wjzgiwWrfKwPtOeP3cI/RXOS7F4kSYGYAIsFq3" +
			"x5b+Z6JWPUWmf3AvuvzgoAiwWrfHlv5nolt4B37dw5ALoojvW8peCLBNCQT1UJzi" +
			"Em9R0GstK8H2YKvSTEhPZ2dTAAS4vAAAAAAAAHE1XmcDAAAAKYx7ZAEcCLZbLKqn" +
			"sQc44eEK/Bb0IzlokzjwLaO7vH+sjQ==".fromBase64()

	@Test
	@Ignore
	fun name() = suspendTest {
		val data = Opus.decode(smallFile.openAsync())!!
		println(data)
	}
}