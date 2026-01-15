package com.lebaillyapp.corgpu.benchmark.domain.gpu

/**
 * # GpuTimings
 *
 * Holds a **detailed timing breakdown** of a GPU-based computation executed via AGSL,
 * measured from the CPU side.
 *
 * This class is intentionally **purely descriptive**:
 * - It does **not** perform any timing by itself
 * - It does **not** interact with GPU or shaders
 * - It simply aggregates timestamps collected at strategic points in the pipeline
 *
 * ## Purpose
 * The goal of this structure is to **separate GPU cost components** that are often
 * incorrectly lumped together into a single "GPU time" value.
 *
 * By isolating each phase, we can:
 * - Identify whether performance is limited by **data transfer** or **GPU execution**
 * - Compare CPU vs GPU execution **fairly**
 * - Find the crossover point where GPU computation becomes beneficial
 *
 * ## Measured Phases
 * The timings stored here reflect the full lifecycle of a GPU task:
 *
 * 1. **CPU → GPU transfer**
 *    - Upload of input buffers (textures, bitmaps, uniforms)
 *    - Memory synchronization and driver overhead
 *
 * 2. **Shader configuration**
 *    - Shader creation / recompilation
 *    - Uniform binding
 *    - RenderEffect or runtime setup
 *
 * 3. **Pure GPU computation**
 *    - Actual shader execution
 *    - Parallel fragment processing on the GPU
 *    - Excludes CPU-side overhead and memory transfers
 *
 * 4. **GPU → CPU transfer**
 *    - Readback of rendered output
 *    - Bitmap extraction or buffer mapping
 *
 * ## Important Notes
 * - All values are stored in **nanoseconds** to preserve precision
 * - Conversion to milliseconds is provided for UI and reporting
 * - Timings are **device-dependent** and influenced by GPU drivers
 * - GPU execution is asynchronous by nature, so accurate measurement
 *   requires proper synchronization before sampling timestamps
 *
 * This class is used as a **diagnostic tool**, not a performance oracle.
 */
data class GpuTimings(
    var transferToGpuNs: Long = 0L,      // Time spent uploading data from CPU memory to GPU-accessible memory
    var shaderConfigNs: Long = 0L,        // Time spent configuring shader state and GPU execution context
    var gpuComputeNs: Long = 0L,          // Time spent executing the shader on the GPU (pure compute cost)
    var transferFromGpuNs: Long = 0L      // Time spent reading results back from GPU to CPU memory
) {

    /**
     * ## Total GPU-related execution time in **nanoseconds**.
     *
     * Includes:
     * - CPU → GPU transfer
     * - Shader configuration
     * - GPU execution
     * - GPU → CPU transfer
     *
     * This is the value that should be compared against
     * a **CPU-only total execution time** for fair benchmarking.
     */
    val totalNs: Long
        get() = transferToGpuNs + shaderConfigNs + gpuComputeNs + transferFromGpuNs

    /**
     * ## Total GPU-related execution time in **milliseconds**.
     *
     * Convenience accessor for UI display and logs.
     * Precision loss is acceptable at this stage.
     */
    val totalMs: Long
        get() = totalNs / 1_000_000

    /**
     * ## Pure GPU computation time in **milliseconds**.
     *
     * This excludes:
     * - Memory transfers
     * - Shader setup
     *
     * Use this value to evaluate **raw GPU compute efficiency**.
     */
    val computeMs: Long
        get() = gpuComputeNs / 1_000_000

    /**
     * ## Total memory transfer overhead in **milliseconds**.
     *
     * Includes:
     * - CPU → GPU upload
     * - GPU → CPU readback
     *
     * This value is critical to determine whether a workload
     * is large enough to amortize transfer costs.
     */
    val transferMs: Long
        get() = (transferToGpuNs + transferFromGpuNs) / 1_000_000
}
