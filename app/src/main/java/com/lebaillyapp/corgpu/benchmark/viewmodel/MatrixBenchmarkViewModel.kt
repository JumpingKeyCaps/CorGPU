package com.lebaillyapp.corgpu.benchmark.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.corgpu.benchmark.domain.gpu.MatrixTextureHelper
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkResult
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.system.measureNanoTime

class MatrixBenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val benchmarkHistory = mutableListOf<MatrixBenchmarkResult>()
    private val _state = MutableStateFlow<MatrixBenchmarkState>(MatrixBenchmarkState.Idle)
    val state: StateFlow<MatrixBenchmarkState> = _state

    fun runBenchmark(size: Int) {
        if (_state.value is MatrixBenchmarkState.Computing) return

        viewModelScope.launch {
            try {
                _state.value = MatrixBenchmarkState.Computing(size)

                val matrixA = generateRandomMatrix(size)
                val matrixB = generateRandomMatrix(size)
                val memoryMb = (size * size * 4 * 3) / (1024f * 1024f)

                val cpuTime = benchmarkCpu(matrixA, matrixB)
                val (gpuOut, gpuMs) = benchmarkGpuUnrolled(matrixA, matrixB)

                val result = MatrixBenchmarkResult(
                    matrixSize = size,
                    cpuTimeMs = cpuTime,
                    gpuTotalTimeMs = gpuMs,
                    gpuComputeTimeMs = gpuMs,
                    memoryAllocatedMb = memoryMb
                )

                benchmarkHistory.add(result)
                _state.value = MatrixBenchmarkState.Success(result, benchmarkHistory.toList())
            } catch (e: Exception) {
                _state.value = MatrixBenchmarkState.Error(e.message ?: "Unknown error", size)
            }
        }
    }

    private suspend fun benchmarkCpu(matrixA: Array<FloatArray>, matrixB: Array<FloatArray>): Long =
        withContext(Dispatchers.Default) {
            val size = matrixA.size
            val result = Array(size) { FloatArray(size) }
            val time = measureNanoTime {
                for (i in 0 until size) {
                    for (j in 0 until size) {
                        var sum = 0f
                        for (k in 0 until size) sum += matrixA[i][k] * matrixB[k][j]
                        result[i][j] = sum
                    }
                }
            }
            time / 1_000_000
        }

    private suspend fun benchmarkGpuUnrolled(matrixA: Array<FloatArray>, matrixB: Array<FloatArray>): Pair<Array<FloatArray>, Long> =
        withContext(Dispatchers.Default) {
            val size = matrixA.size
            val bmpA = MatrixTextureHelper.toFloat16Texture(matrixA, size)
            val bmpB = MatrixTextureHelper.toFloat16Texture(matrixB, size)
            val shaderSrc = this@MatrixBenchmarkViewModel::class.java
                .classLoader!!
                .getResource("matrix_multiply_unroll.agsl")!!
                .readText()

            val shader = RuntimeShader(shaderSrc)
            shader.setInputShader("texA", BitmapShader(bmpA, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
            shader.setInputShader("texB", BitmapShader(bmpB, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
            shader.setIntUniform("size", size)

            val output = createBitmap(size, size, Bitmap.Config.RGBA_F16)
            val gpuMs = measureNanoTime {
                val canvas = Canvas(output)
                val paint = Paint().apply { setShader(shader) }
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            } / 1_000_000L

            val result = MatrixTextureHelper.fromFloat16Texture(output, size)
            result to gpuMs
        }

    private fun generateRandomMatrix(size: Int): Array<FloatArray> =
        Array(size) { FloatArray(size) { Random.nextFloat() } }

    fun resetState() { _state.value = MatrixBenchmarkState.Idle }
    fun clearHistory() { benchmarkHistory.clear(); _state.value = MatrixBenchmarkState.Idle }
}