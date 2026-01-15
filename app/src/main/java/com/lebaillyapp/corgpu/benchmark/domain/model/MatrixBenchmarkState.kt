package com.lebaillyapp.corgpu.benchmark.domain.model

/**
 * # MatrixBenchmarkState
 *
 * Sealed hierarchy representing the **complete state machine**
 * of the matrix benchmark screen.
 *
 * ## Purpose
 * This model defines **all possible UI-visible states**
 * during the lifecycle of a benchmark execution.
 *
 * By using a sealed class:
 * - State exhaustiveness is enforced at compile time
 * - UI rendering becomes deterministic
 * - Illegal or intermediate states are impossible
 *
 * ## State Flow (Typical)
 * ```
 * Idle → Computing → Success
 *              ↘︎ Error
 * ```
 *
 * Each state is mutually exclusive and fully describes
 * what the UI is allowed to render.
 */
sealed class MatrixBenchmarkState {
    /**
     * ## Initial and idle state.
     *
     * The system is ready to start a benchmark,
     * but no computation is currently running.
     *
     * UI expectations:
     * - Controls enabled
     * - No progress indicator
     * - No active computation
     */
    data object Idle : MatrixBenchmarkState()

    /**
     * ## Active computation state.
     *
     * Indicates that a benchmark is currently running
     * for a given matrix size.
     *
     * @param matrixSize Size of the matrix being processed
     *
     * UI expectations:
     * - Disable user input that would restart computation
     * - Show progress or loading indicator
     * - Clearly communicate the active matrix size
     */
    data class Computing(val matrixSize: Int) : MatrixBenchmarkState()

    /**
     * ## Successful benchmark completion state.
     *
     * Holds both the latest benchmark result and
     * an optional execution history.
     *
     * @param result Most recent benchmark result
     * @param history Accumulated benchmark results,
     *                typically used for visualization
     *                (charts, trends, comparisons)
     *
     * UI expectations:
     * - Display detailed results
     * - Update charts and metrics
     * - Allow the user to trigger new benchmarks
     */
    data class Success(
        val result: MatrixBenchmarkResult,
        val history: List<MatrixBenchmarkResult> = emptyList()
    ) : MatrixBenchmarkState()

    /**
     * ## Error state.
     *
     * Indicates that a benchmark failed during execution.
     *
     * @param message Human-readable error description
     * @param matrixSize Matrix size that caused the failure
     *
     * UI expectations:
     * - Display error feedback
     * - Preserve context (matrix size)
     * - Allow recovery or retry
     */
    data class Error(
        val message: String,
        val matrixSize: Int
    ) : MatrixBenchmarkState()
}




/**
 * # ScalabilityChartData
 *
 * Data model used to represent **CPU vs GPU scalability trends**
 * across multiple benchmark runs.
 *
 * ## Purpose
 * This structure prepares benchmark history for direct
 * consumption by charting components.
 *
 * It converts raw benchmark results into:
 * - Ordered data points
 * - Comparable CPU and GPU curves
 * - An optional crossover point
 *
 * This model contains **no UI logic** and no rendering code.
 */
data class ScalabilityChartData(
    val cpuPoints: List<Pair<Int, Long>>,  // (matrixSize, timeMs)
    val gpuPoints: List<Pair<Int, Long>>,  // (matrixSize, timeMs)
    val crossoverPoint: Int? = null         // Point où GPU devient plus rapide
) {
    /**
     * ## Builds scalability chart data from benchmark history.
     *
     * ## Processing Steps
     * 1. Sort benchmark results by matrix size
     * 2. Extract CPU execution points
     * 3. Extract GPU total execution points
     * 4. Detect the first crossover point where GPU < CPU
     *
     * ## Interpretation
     * - CPU and GPU curves are directly comparable
     * - GPU uses **total time**, not pure compute time
     * - Crossover point marks when GPU acceleration
     *   becomes beneficial in real-world conditions
     *
     * @param history List of past benchmark results
     * @return Structured chart-ready data
     */
    companion object {
        fun fromHistory(history: List<MatrixBenchmarkResult>): ScalabilityChartData {
            val sortedHistory = history.sortedBy { it.matrixSize }

            // Map results to (matrixSize, executionTime) pairs
            val cpuPoints = sortedHistory.map { it.matrixSize to it.cpuTimeMs }
            val gpuPoints = sortedHistory.map { it.matrixSize to it.gpuTotalTimeMs }

            // Detect the first matrix size where GPU total time beats CPU time
            val crossover = sortedHistory.firstOrNull {
                it.gpuTotalTimeMs < it.cpuTimeMs
            }?.matrixSize

            return ScalabilityChartData(
                cpuPoints = cpuPoints,
                gpuPoints = gpuPoints,
                crossoverPoint = crossover
            )
        }
    }
}