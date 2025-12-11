package com.lebaillyapp.corgpu.benchmark.domain.gpu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper pour exécuter des multiplications de matrices sur GPU via AGSL shaders
 * Nécessite Android 13+ (API 33) pour RuntimeShader
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GpuMatrixHelper(private val context: Context) {

    private var shader: RuntimeShader? = null

    init {
        loadShader()
    }

    /**
     * Charge le shader AGSL depuis res/raw
     */
    private fun loadShader() {
        try {
            val shaderCode = context.resources.openRawResource(
                context.resources.getIdentifier("matrix_multiply", "raw", context.packageName)
            ).bufferedReader().use {
                it.readText()
            }
            shader = RuntimeShader(shaderCode)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load matrix_multiply shader from res/raw", e)
        }
    }

    /**
     * Multiplie deux matrices sur GPU: C = A × B
     *
     * @param matrixA Matrice A (array 2D) avec valeurs dans [0, 1]
     * @param matrixB Matrice B (array 2D) avec valeurs dans [0, 1]
     * @return Résultat C et temps de calcul (compute + transfer)
     */
    suspend fun multiplyMatrices(
        matrixA: Array<FloatArray>,
        matrixB: Array<FloatArray>
    ): Pair<Array<FloatArray>, GpuTimings> = withContext(Dispatchers.Default) {
        val size = matrixA.size
        require(size == matrixA[0].size && size == matrixB.size && size == matrixB[0].size) {
            "Matrices must be square and same size"
        }

        val timings = GpuTimings()

        // 1. Transfer CPU -> GPU (encode en bitmaps)
        val transferStart = System.nanoTime()
        val bitmapA = matrixToBitmap(matrixA)
        val bitmapB = matrixToBitmap(matrixB)
        val bitmapC = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        timings.transferToGpuNs = System.nanoTime() - transferStart

        // 2. Configure shader
        val configStart = System.nanoTime()
        shader?.let { s ->
            s.setFloatUniform("matrixSize", size.toFloat())
            s.setInputShader("inputA", createBitmapShader(bitmapA))
            s.setInputShader("inputB", createBitmapShader(bitmapB))
        } ?: throw IllegalStateException("Shader not loaded")
        timings.shaderConfigNs = System.nanoTime() - configStart

        // 3. Execute shader (GPU compute)
        val computeStart = System.nanoTime()
        val canvas = Canvas(bitmapC)
        shader?.let { canvas.drawPaint(android.graphics.Paint().apply { setShader(it) }) }
        timings.gpuComputeNs = System.nanoTime() - computeStart

        // 4. Transfer GPU -> CPU (decode bitmap avec dénormalisation)
        val transferBackStart = System.nanoTime()
        val result = bitmapToMatrix(bitmapC, size)
        timings.transferFromGpuNs = System.nanoTime() - transferBackStart

        // Cleanup
        bitmapA.recycle()
        bitmapB.recycle()
        bitmapC.recycle()

        return@withContext result to timings
    }

    /**
     * Encode une matrice en Bitmap (canal rouge = valeur)
     * Matrice d'entrée doit avoir des valeurs dans [0, 1]
     */
    private fun matrixToBitmap(matrix: Array<FloatArray>): Bitmap {
        val size = matrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (y in 0 until size) {
            for (x in 0 until size) {
                // Valeurs déjà normalisées dans [0, 1]
                val value = matrix[y][x].coerceIn(0f, 1f)
                val gray = (value * 255f).toInt()
                val color = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }

    /**
     * Decode un Bitmap en matrice (canal rouge = valeur)
     * Le shader normalise par matrixSize, donc on dénormalise ici
     */
    private fun bitmapToMatrix(bitmap: Bitmap, size: Int): Array<FloatArray> {
        val matrix = Array(size) { FloatArray(size) }

        for (y in 0 until size) {
            for (x in 0 until size) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                // Le shader a divisé par matrixSize, on multiplie pour récupérer la vraie valeur
                matrix[y][x] = (red / 255f) * size
            }
        }

        return matrix
    }

    /**
     * Crée un BitmapShader à partir d'un Bitmap
     */
    private fun createBitmapShader(bitmap: Bitmap): android.graphics.BitmapShader {
        return android.graphics.BitmapShader(
            bitmap,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
    }

    /**
     * Cleanup du shader
     */
    fun release() {
        shader = null
    }
}