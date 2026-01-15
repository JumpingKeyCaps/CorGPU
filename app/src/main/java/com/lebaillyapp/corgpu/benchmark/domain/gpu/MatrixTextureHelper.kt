package com.lebaillyapp.corgpu.benchmark.domain.gpu

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color

/**
 * # MatrixTextureHelper
 *
 * Utility object responsible for **encoding and decoding numerical matrices**
 * to and from GPU-compatible textures using `Bitmap.Config.RGBA_F16`.
 *
 * This class acts as a **bridge between CPU-side data structures** (`Array<FloatArray>`)
 * and **GPU-side representations** (textures sampled by AGSL shaders).
 *
 * ## Core Responsibility
 * - Encode a 2D float matrix into a GPU-readable texture
 * - Decode a GPU-produced texture back into a CPU-side matrix
 *
 * It deliberately uses the **simplest possible API surface**
 * (`setPixel()` / `getPixel()`) to keep the code readable and educational.
 *
 * ## Precision Model
 * - Only the **red channel** is used
 * - Values are stored as **16-bit floating point (Float16)**
 * - Other channels (G, B, A) are unused except for alpha = 1.0
 *
 * This is sufficient for:
 * - Demonstrating GPU parallelism
 * - Matrix multiplication benchmarks
 *
 * But it is **not suitable** for:
 * - High-precision numerical computing
 * - Scientific or financial workloads
 *
 * ## Performance Trade-off (Important)
 * `setPixel()` and `getPixel()`:
 * - Trigger **JNI calls per pixel**
 * - Are extremely slow for large textures
 *
 * This is a **deliberate design choice**:
 * - Prioritizes clarity over performance
 * - Avoids ~100 lines of manual Float16 packing logic
 *
 * A ByteBuffer-based implementation could yield **10–50× speedups**,
 * but would significantly increase complexity and reduce approachability.
 *
 * This helper is therefore **not optimized by design**.
 */
object MatrixTextureHelper {

    /**
     * ## Converts a 2D float matrix into an RGBA_F16 bitmap.
     *
     * ### Encoding Strategy
     * - Each matrix element maps to exactly one pixel
     * - The value is stored in the **red channel**
     * - Green and blue channels are set to 0.0
     * - Alpha is set to 1.0 to ensure valid pixel output
     *
     * ### Coordinate Mapping
     * - `matrix[y][x]` → pixel at `(x, y)`
     * - This mapping must match the coordinate system
     *   used inside the AGSL shader
     *
     * ### Performance Notes
     * - This performs `size × size` JNI calls
     * - Cost grows quadratically with matrix size
     * - Acceptable for benchmarking and demos, not production
     */
    @SuppressLint("UseKtx")
    fun toFloat16Texture(matrix: Array<FloatArray>, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGBA_F16)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val v = matrix[y][x]
                // Encode the matrix value into the red channel (Float16 precision)
                bmp.setPixel(x, y, Color.valueOf(v, 0f, 0f, 1f).toArgb())
            }
        }
        return bmp
    }

    /**
     * # Decodes an RGBA_F16 bitmap back into a 2D float matrix.
     *
     * ### Decoding Strategy
     * - Reads one pixel per matrix element
     * - Extracts the **red channel only**
     * - Ignores green, blue, and alpha channels
     *
     * ### Assumptions
     * - The bitmap was produced by a compatible GPU shader
     * - The shader wrote the computed value into the red channel
     * - Bitmap dimensions match the expected matrix size
     *
     * ### Synchronization Consideration
     * - This method assumes GPU execution has completed
     * - Caller must ensure proper GPU → CPU synchronization
     *   before invoking this method
     *
     * ### Performance Notes
     * - Same JNI overhead as encoding
     * - Intended for correctness and clarity, not throughput
     */
    @SuppressLint("UseKtx")
    fun fromFloat16Texture(bmp: Bitmap, size: Int): Array<FloatArray> {
        val out = Array(size) { FloatArray(size) }
        for (y in 0 until size) {
            for (x in 0 until size) {
                // Read back the red channel as the computed matrix value
                val c = Color.valueOf(bmp.getPixel(x, y))
                out[y][x] = c.red()
            }
        }
        return out
    }
}
