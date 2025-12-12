package com.lebaillyapp.corgpu.benchmark.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.lebaillyapp.corgpu.benchmark.domain.gpu.MatrixTextureHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime
import androidx.core.graphics.createBitmap

class MatrixViewModel : ViewModel() {

    data class Result(
        val cpuMs: Double,
        val gpuMs: Double,
        val output: FloatArray
    )

    suspend fun benchmark(size: Int): Result = withContext(Dispatchers.Default) {
        val count = size * size
        val A = FloatArray(count) { (it % 7 + 1).toFloat() }
        val B = FloatArray(count) { (it % 5 + 1).toFloat() }

        val cpuOut = FloatArray(count)
        val cpuTime = measureNanoTime { multiplyCpu(A, B, cpuOut, size) } / 1_000_000.0
        val (gpuOut, gpuMs) = multiplyGpu(A, B, size)

        Result(cpuTime, gpuMs, gpuOut)
    }

    private fun multiplyCpu(a: FloatArray, b: FloatArray, out: FloatArray, size: Int) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                var sum = 0f
                for (k in 0 until size) sum += a[r * size + k] * b[k * size + c]
                out[r * size + c] = sum
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun multiplyGpu(A: FloatArray, B: FloatArray, size: Int): Pair<FloatArray, Double> =
        withContext(Dispatchers.Default) {

            val texA = MatrixTextureHelper.toFloat16Texture(A, size)
            val texB = MatrixTextureHelper.toFloat16Texture(B, size)

            val shaderSrc = this@MatrixViewModel::class.java
                .classLoader!!
                .getResource("matrix_multiply.agsl")!!
                .readText()

            val rtShader = RuntimeShader(shaderSrc)

            val shaderA = android.graphics.BitmapShader(texA, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
            val shaderB = android.graphics.BitmapShader(texB, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)

            rtShader.setInputShader("texA", shaderA)
            rtShader.setInputShader("texB", shaderB)
            rtShader.setIntUniform("size", size)

            val output = createBitmap(size, size, Bitmap.Config.RGBA_F16)

            val gpuMs = measureNanoTime {
                val canvas = Canvas(output)
                val paint = Paint().apply { shader = rtShader }
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            } / 1_000_000.0

            val result = MatrixTextureHelper.fromFloat16Texture(output, size)
            result to gpuMs
        }
}