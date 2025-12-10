package com.lebaillyapp.corgpu.benchmark.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Résultat d'un benchmark de multiplication de matrices
 */
data class MatrixBenchmarkResult(
    val matrixSize: Int,

    // Temps CPU (coroutines pures)
    val cpuTimeMs: Long,

    // Temps GPU total (include transfers)
    val gpuTotalTimeMs: Long,

    // Temps GPU compute seul (sans transfers)
    val gpuComputeTimeMs: Long,

    // Overhead du transfer CPU<->GPU
    val transferOverheadMs: Long = gpuTotalTimeMs - gpuComputeTimeMs,

    // Pourcentage de l'overhead
    val transferOverheadPercent: Float = if (gpuTotalTimeMs > 0) {
        (transferOverheadMs.toFloat() / gpuTotalTimeMs.toFloat()) * 100f
    } else 0f,

    // Speedup (combien de fois plus rapide)
    val speedup: Float = if (gpuTotalTimeMs > 0) {
        cpuTimeMs.toFloat() / gpuTotalTimeMs.toFloat()
    } else 0f,

    // Mémoire allouée (en MB)
    val memoryAllocatedMb: Float,

    // Timestamp du benchmark
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Retourne un message formaté du speedup
     */
    fun getSpeedupMessage(): String {
        return when {
            speedup > 1f -> "GPU is ${String.format("%.2f", speedup)}x Faster!"
            speedup < 1f -> "CPU is ${String.format("%.2f", 1f / speedup)}x Faster!"
            else -> "Same Performance"
        }
    }

    /**
     * Log formaté pour affichage
     */
    fun toLogString(): String {
        return buildString {
            appendLine("[${formatTimestamp()}] Benchmark N=$matrixSize completed.")
            appendLine("CPU Time: ${cpuTimeMs}ms")
            appendLine("GPU Time: ${gpuTotalTimeMs}ms (Compute: ${gpuComputeTimeMs}ms, Transfer: ${transferOverheadMs}ms)")
            appendLine("Speedup: ${String.format("%.2f", speedup)}x")
        }
    }

    private fun formatTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}