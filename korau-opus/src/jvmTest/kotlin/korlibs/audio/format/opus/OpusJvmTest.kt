package korlibs.audio.format.opus

import com.soywiz.korau.format.org.*
import korlibs.audio.format.org.concentus.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.io.stream.*
import java.io.*
import java.nio.file.*
import kotlin.test.*


class OpusJvmTest {
    @Test
    fun test() = suspendTest {
        val processor = OpusOggProcessor(2)
        val s = resourcesVfs["opus1.opus"].readBytes().openAsync()
        while (s.getAvailable() > 0L) {
            println(processor.readAndDecodePacket(s))
        }
        //processor.samples.toData(processor.rate)
    }

    @Test
    @Ignore
    fun testEncodeDecode() = suspendTest {
        val fileIn = korlibs.io.file.std.resourcesVfs["48Khz-Stereo.raw"].readBytes().inputStream()
        val encoder = OpusEncoder(48000, 2, OpusApplication.OPUS_APPLICATION_AUDIO)
        //val fileIn = korlibs.io.file.std.resourcesVfs["8Khz-Mono.raw"].readBytes().inputStream()
        //val encoder = OpusEncoder(8000, 1, OpusApplication.OPUS_APPLICATION_AUDIO)
        encoder.bitrate = 96000
        encoder.forceMode = OpusMode.MODE_CELT_ONLY
        encoder.signalType = OpusSignal.OPUS_SIGNAL_MUSIC
        encoder.complexity = 0

        val decoder = OpusDecoder(48000, 2)

        val fileCompOutFile = Files.createTempFile("prefix", ".opus.raw").toFile()
        val fileCompOut = FileOutputStream(fileCompOutFile)

        val fileOutFile = Files.createTempFile("prefix", "raw").toFile()
        val fileOut = FileOutputStream(fileOutFile)
        println("fileCompOutFile=$fileCompOutFile")
        println("fileOutFile=$fileOutFile")
        val packetSamples = 960
        val inBuf = ByteArray(packetSamples * 2 * 2)
        val data_packet = ByteArray(1275)
        val start = System.currentTimeMillis()
        while (fileIn.available() >= inBuf.size) {
            val bytesRead: Int = fileIn.read(inBuf, 0, inBuf.size)
            val pcm: ShortArray = BytesToShorts(inBuf, 0, inBuf.size)
            val bytesEncoded: Int = encoder.encode(pcm, 0, packetSamples, data_packet, 0, 1275)
            System.out.println("$bytesEncoded bytes encoded");
            val samplesDecoded: Int = decoder.decode(data_packet, 0, bytesEncoded, pcm, 0, packetSamples, false)
            System.out.println("$samplesDecoded samples decoded");
            val bytesOut: ByteArray = ShortsToBytes(pcm)
            fileCompOut.write(data_packet, 0, bytesEncoded)
            fileOut.write(bytesOut, 0, bytesOut.size)
        }

        val end = System.currentTimeMillis()
        println("Time was " + (end - start) + "ms")
        fileIn.close()
        fileOut.close()
        fileCompOut.close()
        println("Done!")
    }


    /// <summary>
    /// Converts interleaved byte samples (such as what you get from a capture device)
    /// into linear short samples (that are much easier to work with)
    /// </summary>
    /// <param name="input"></param>
    /// <returns></returns>
    fun BytesToShorts(input: ByteArray): ShortArray? {
        return BytesToShorts(input, 0, input.size)
    }

    /// <summary>
    /// Converts interleaved byte samples (such as what you get from a capture device)
    /// into linear short samples (that are much easier to work with)
    /// </summary>
    /// <param name="input"></param>
    /// <returns></returns>
    fun BytesToShorts(input: ByteArray, offset: Int, length: Int): ShortArray {
        val processedValues = ShortArray(length / 2)
        for (c in processedValues.indices) {
            val a = (input[c * 2 + offset].toInt() and 0xFF).toShort()
            val b = (input[c * 2 + 1 + offset].toInt() shl 8).toShort()
            processedValues[c] = (a.toInt() or b.toInt()).toShort()
        }
        return processedValues
    }

    /// <summary>
    /// Converts linear short samples into interleaved byte samples, for writing to a file, waveout device, etc.
    /// </summary>
    /// <param name="input"></param>
    /// <returns></returns>
    fun ShortsToBytes(input: ShortArray): ByteArray {
        return ShortsToBytes(input, 0, input.size)
    }

    /// <summary>
    /// Converts linear short samples into interleaved byte samples, for writing to a file, waveout device, etc.
    /// </summary>
    /// <param name="input"></param>
    /// <returns></returns>
    fun ShortsToBytes(input: ShortArray, offset: Int, length: Int): ByteArray {
        val processedValues = ByteArray(length * 2)
        for (c in 0 until length) {
            processedValues[c * 2] = (input[c + offset].toInt() and 0xFF).toByte()
            processedValues[c * 2 + 1] = (input[c + offset].toInt() shr 8 and 0xFF).toByte()
        }
        return processedValues
    }
}
