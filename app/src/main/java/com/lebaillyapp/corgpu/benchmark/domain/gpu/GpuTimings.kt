package com.lebaillyapp.corgpu.benchmark.domain.gpu

/**
 * Timings détaillés des opérations GPU
 */
data class GpuTimings(
    var transferToGpuNs: Long = 0L,      // CPU -> GPU
    var shaderConfigNs: Long = 0L,        // Configuration shader
    var gpuComputeNs: Long = 0L,          // Calcul GPU pur
    var transferFromGpuNs: Long = 0L      // GPU -> CPU
) {
    val totalNs: Long
        get() = transferToGpuNs + shaderConfigNs + gpuComputeNs + transferFromGpuNs

    val totalMs: Long
        get() = totalNs / 1_000_000

    val computeMs: Long
        get() = gpuComputeNs / 1_000_000

    val transferMs: Long
        get() = (transferToGpuNs + transferFromGpuNs) / 1_000_000
}