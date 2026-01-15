package com.lebaillyapp.corgpu.benchmark.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * # MatrixBenchmarkResult
 *
 * Immutable value object representing the **final outcome of a matrix
 * multiplication benchmark**, comparing a CPU-based coroutine implementation
 * against a GPU-based AGSL implementation.
 *
 * ## Role in the Architecture
 * This model is the **single source of truth** for:
 * - Performance comparison
 * - UI rendering
 * - Logging and diagnostics
 *
 * It deliberately aggregates **pre-computed metrics** instead of raw timings,
 * ensuring that interpretation logic is centralized and consistent.
 *
 * ## What This Model Represents
 * - A *completed* benchmark run
 * - A comparison between **CPU orchestration + CPU compute**
 *   and **CPU orchestration + GPU compute**
 * - A snapshot in time, not a streaming metric
 *
 * ## What This Model Does NOT Do
 * - It does not measure time
 * - It does not run benchmarks
 * - It does not decide which approach is "better"
 *
 * It only **describes results**, leaving interpretation to the caller or UI.
 */
data class MatrixBenchmarkResult(
    val matrixSize: Int,

    // Total execution time of the CPU-only implementation,
    // including coroutine orchestration overhead.
    val cpuTimeMs: Long,

    // Total GPU pipeline time in milliseconds:
    // CPU → GPU transfer + shader setup + GPU compute + GPU → CPU transfer.
    val gpuTotalTimeMs: Long,

    // Pure GPU compute time in milliseconds,
    // excluding memory transfers and CPU-side setup.
    val gpuComputeTimeMs: Long,

    // Derived value representing CPU ↔ GPU transfer overhead.
    // This is intentionally computed here to keep interpretation centralized.
    val transferOverheadMs: Long = gpuTotalTimeMs - gpuComputeTimeMs,

    // Percentage of total GPU time spent on memory transfers.
    // Useful to visualize when GPU acceleration becomes worthwhile.
    val transferOverheadPercent: Float = if (gpuTotalTimeMs > 0) {
        (transferOverheadMs.toFloat() / gpuTotalTimeMs.toFloat()) * 100f
    } else 0f,

    // Speedup factor:
    // > 1.0 → GPU faster
    // < 1.0 → CPU faster
    //
    // This value compares total wall-clock time,
    // not raw compute time, ensuring a fair comparison.
    val speedup: Float = if (gpuTotalTimeMs > 0) {
        cpuTimeMs.toFloat() / gpuTotalTimeMs.toFloat()
    } else 0f,

    // Approximate memory allocated during the benchmark (in megabytes).
    // Intended for trend analysis, not exact accounting.
    val memoryAllocatedMb: Float,

    // Timestamp (epoch millis) marking when the benchmark completed.
    // Useful for logs, history, and result ordering.
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * ## Returns a **human-readable performance summary** based on the speedup value.
     *
     * #### Interpretation Rules
     * - speedup > 1.0 → GPU wins
     * - speedup < 1.0 → CPU wins
     * - speedup == 1.0 → parity
     *
     * This method is UI-friendly and intentionally opinionated,
     * but does not affect raw metrics.
     */
    fun getSpeedupMessage(): String {
        return when {
            speedup > 1f -> "GPU is ${String.format("%.2f", speedup)}x Faster!"
            speedup < 1f -> "CPU is ${String.format("%.2f", 1f / speedup)}x Faster!"
            else -> "Same Performance"
        }
    }

    /**
     * ## Returns a formatted multi-line log string summarizing the benchmark.
     *
     * Intended for:
     * - Debug logs
     * - Console output
     * - Performance tracing
     *
     * Not intended for structured logging or persistence.
     */
    fun toLogString(): String {
        return buildString {
            appendLine("[${formatTimestamp()}] Benchmark N=$matrixSize completed.")
            appendLine("CPU Time: ${cpuTimeMs}ms")
            appendLine("GPU Time: ${gpuTotalTimeMs}ms (Compute: ${gpuComputeTimeMs}ms, Transfer: ${transferOverheadMs}ms)")
            appendLine("Speedup: ${String.format("%.2f", speedup)}x")
        }
    }


    /**
     * Formats the benchmark timestamp into a locale-aware,
     * human-readable date string.
     *
     * This is intentionally kept private to avoid leaking
     * presentation logic outside the model.
     */
    private fun formatTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}