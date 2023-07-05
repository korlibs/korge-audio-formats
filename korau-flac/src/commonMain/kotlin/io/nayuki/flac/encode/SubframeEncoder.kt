/* 
 * FLAC library (Java)
 * 
 * Copyright (c) Project Nayuki
 * https://www.nayuki.io/page/flac-library-java
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (see COPYING.txt and COPYING.LESSER.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */
package io.nayuki.flac.encode

/*
import korlibs.io.lang.*

/* 
 * Calculates/estimates the encoded size of a subframe of audio sample data, and also performs the encoding to an output stream.
 */
abstract class SubframeEncoder protected constructor(shift: Int, depth: Int) {
    /*---- Instance members ----*/
    protected val sampleShift // Number of bits to shift each sample right by. In the range [0, sampleDepth].
            : Int
    protected val sampleDepth // Stipulate that each audio sample fits in a signed integer of this width. In the range [1, 33].
            : Int

    // Constructs a subframe encoder on some data array with the given right shift (wasted bits) and sample depth.
    // Note that every element of the array must fit in a signed depth-bit integer and have at least 'shift' trailing binary zeros.
    // After the encoder object is created and when encode() is called, it must receive the same array length and values (but the object reference can be different).
    // Subframe encoders should not retain a reference to the sample data array because the higher-level encoder may request and
    // keep many size estimates coupled with encoder objects, but only utilize a small number of encoder objects in the end.
    init {
        if ((depth < 1) || (depth > 33) || (shift < 0) || (shift > depth)) throw IllegalArgumentException()
        sampleShift = shift
        sampleDepth = depth
    }

    // Encodes the given vector of audio sample data to the given bit output stream
    // using the current encoding method (dictated by subclasses and field values).
    // This requires the data array to have the same values (but not necessarily be the same object reference)
    // as the array that was passed to the constructor when this encoder object was created.
    abstract fun encode(samples: LongArray, out: BitOutputStream)

    // Writes the subframe header to the given output stream, based on the given
    // type code (uint6) and this object's sampleShift field (a.k.a. wasted bits per sample).
    protected fun writeTypeAndShift(type: Int, out: BitOutputStream) {
        // Check arguments
        if ((type ushr 6) != 0) throw IllegalArgumentException()

        // Write some fields
        out.writeInt(1, 0)
        out.writeInt(6, type)

        // Write shift value in quasi-unary
        if (sampleShift == 0) out.writeInt(1, 0) else {
            out.writeInt(1, 1)
            for (i in 0 until (sampleShift - 1)) out.writeInt(1, 0)
            out.writeInt(1, 1)
        }
    }

    // Writes the given value to the output stream as a signed (sampleDepth-sampleShift) bit integer.
    // Note that the value to being written is equal to the raw sample value shifted right by sampleShift.
    protected fun writeRawSample(`val`: Long, out: BitOutputStream) {
        val width: Int = sampleDepth - sampleShift
        if (width < 1 || width > 33) throw IllegalStateException()
        val temp: Long = `val` shr (width - 1)
        if (temp != 0L && temp != -1L) throw IllegalArgumentException()
        if (width <= 32) out.writeInt(width, `val`.toInt()) else {  // width == 33
            out.writeInt(1, (`val` ushr 32).toInt())
            out.writeInt(32, `val`.toInt())
        }
    }

    /*---- Helper structure ----*/ // Represents options for how to search the encoding parameters for a subframe. It is used directly by
    // SubframeEncoder.computeBest() and indirectly by its sub-calls. Objects of this class are immutable.
    class SearchOptions(
        minFixedOrder: Int,
        maxFixedOrder: Int,
        minLpcOrder: Int,
        maxLpcOrder: Int,
        lpcRoundVars: Int,
        maxRiceOrder: Int
    ) {
        /*-- Fields --*/ // The range of orders to test for fixed prediction mode, possibly none.
        // The values satisfy (minFixedOrder = maxFixedOrder = -1) || (0 <= minFixedOrder <= maxFixedOrder <= 4).
        val minFixedOrder: Int
        val maxFixedOrder: Int

        // The range of orders to test for linear predictive coding (LPC) mode, possibly none.
        // The values satisfy (minLpcOrder = maxLpcOrder = -1) || (1 <= minLpcOrder <= maxLpcOrder <= 32).
        // Note that the FLAC subset format requires maxLpcOrder <= 12 when sampleRate <= 48000.
        val minLpcOrder: Int
        val maxLpcOrder: Int

        // How many LPC coefficient variables to try rounding both up and down.
        // In the range [0, 30]. Note that each increase by one will double the search time!
        val lpcRoundVariables: Int

        // The maximum partition order used in Rice coding. The minimum is not configurable and always 0.
        // In the range [0, 15]. Note that the FLAC subset format requires maxRiceOrder <= 8.
        val maxRiceOrder: Int

        /*-- Constructors --*/ // Constructs a search options object based on the given values,
        // throwing an IllegalArgumentException if and only if they are nonsensical.
        init {
            // Check argument ranges
            if ((minFixedOrder != -1 || maxFixedOrder != -1) &&
                !((0 <= minFixedOrder) && (minFixedOrder <= maxFixedOrder) && (maxFixedOrder <= 4))
            ) throw IllegalArgumentException()
            if ((minLpcOrder != -1 || maxLpcOrder != -1) &&
                !((1 <= minLpcOrder) && (minLpcOrder <= maxLpcOrder) && (maxLpcOrder <= 32))
            ) throw IllegalArgumentException()
            if (lpcRoundVars < 0 || lpcRoundVars > 30) throw IllegalArgumentException()
            if (maxRiceOrder < 0 || maxRiceOrder > 15) throw IllegalArgumentException()

            // Copy arguments to fields
            this.minFixedOrder = minFixedOrder
            this.maxFixedOrder = maxFixedOrder
            this.minLpcOrder = minLpcOrder
            this.maxLpcOrder = maxLpcOrder
            lpcRoundVariables = lpcRoundVars
            this.maxRiceOrder = maxRiceOrder
        }

        companion object {
            /*-- Constants for recommended defaults --*/ // Note that these constants are for convenience only, and offer little promises in terms of API stability.
            // For example, there is no expectation that the set of search option names as a whole,
            // or the values of each search option will remain the same from version to version.
            // Even if a search option retains the same value across code versions, the underlying encoder implementation
            // can change in such a way that the encoded output is not bit-identical or size-identical across versions.
            // Therefore, treat these search options as suggestions that strongly influence the encoded FLAC output,
            // but *not* as firm guarantees that the same audio data with the same options will forever produce the same result.
            // These search ranges conform to the FLAC subset format.
            val SUBSET_ONLY_FIXED: SearchOptions = SearchOptions(0, 4, -1, -1, 0, 8)
            val SUBSET_MEDIUM: SearchOptions = SearchOptions(0, 1, 2, 8, 0, 5)
            val SUBSET_BEST: SearchOptions = SearchOptions(0, 1, 2, 12, 0, 8)
            val SUBSET_INSANE: SearchOptions = SearchOptions(0, 4, 1, 12, 4, 8)

            // These cannot guarantee that an encoded file conforms to the FLAC subset (i.e. they are lax).
            val LAX_MEDIUM: SearchOptions = SearchOptions(0, 1, 2, 22, 0, 15)
            val LAX_BEST: SearchOptions = SearchOptions(0, 1, 2, 32, 0, 15)
            val LAX_INSANE: SearchOptions = SearchOptions(0, 1, 2, 32, 4, 15)
        }
    }

    companion object {
        /*---- Static functions ----*/ // Computes/estimates the best way to encode the given vector of audio sample data at the given sample depth under
        // the given search criteria, returning a size estimate plus a new encoder object associated with that size.
        fun computeBest(samples: LongArray, sampleDepth: Int, opt: SearchOptions): SizeEstimate<SubframeEncoder?>? {
            // Check arguments
            if (sampleDepth < 1 || sampleDepth > 33) throw IllegalArgumentException()
            for (x: Long in samples) {
                var x = x shr (sampleDepth - 1)
                if (x != 0L && x != -1L) // Check that the input actually fits the indicated sample depth
                    throw IllegalArgumentException()
            }

            // Encode with constant if possible
            var result: SizeEstimate<SubframeEncoder?>? = ConstantEncoder.Companion.computeBest(samples, 0, sampleDepth)
            if (result != null) return result

            // Detect number of trailing zero bits
            val shift: Int = computeWastedBits(samples)

            // Start with verbatim as fallback
            result = VerbatimEncoder.Companion.computeBest(samples, shift, sampleDepth)

            // Try fixed prediction encoding
            run {
                var order: Int = opt.minFixedOrder
                while (0 <= order && order <= opt.maxFixedOrder) {
                    val temp: SizeEstimate<SubframeEncoder?> = FixedPredictionEncoder.Companion.computeBest(
                        samples, shift, sampleDepth, order, opt.maxRiceOrder
                    )
                    result = result!!.minimum(temp)
                    order++
                }
            }

            // Try linear predictive coding
            val fdp: FastDotProduct = FastDotProduct(samples, kotlin.math.max(opt.maxLpcOrder, 0))
            var order: Int = opt.minLpcOrder
            while (0 <= order && order <= opt.maxLpcOrder) {
                val temp: SizeEstimate<SubframeEncoder?> = LinearPredictiveEncoder.Companion.computeBest(
                    samples, shift, sampleDepth, order, kotlin.math.min(opt.lpcRoundVariables, order), fdp, opt.maxRiceOrder
                )
                result = result!!.minimum(temp)
                order++
            }

            // Return the encoder found with the lowest bit length
            return result
        }

        // Looks at each value in the array and computes the minimum number of trailing binary zeros
        // among all the elements. For example, computedwastedBits({0b10, 0b10010, 0b1100}) = 1.
        // If there are no elements or every value is zero (the former actually implies the latter), then
        // the return value is 0. This is because every zero value has an infinite number of trailing zeros.
        private fun computeWastedBits(data: LongArray): Int {
            var accumulator: Long = 0
            for (x: Long in data) accumulator = accumulator or x
            if (accumulator == 0L) return 0 else {
                val result: Int = accumulator.countTrailingZeroBits()
                assert(result in 0..63)
                return result
            }
        }
    }
}
*/
