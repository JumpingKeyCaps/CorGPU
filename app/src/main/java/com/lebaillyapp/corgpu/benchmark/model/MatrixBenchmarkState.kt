package com.lebaillyapp.corgpu.benchmark.model

/**
 * États possibles de l'écran de benchmark
 */
sealed class MatrixBenchmarkState {
    /**
     * État initial - prêt à lancer un benchmark
     */
    data object Idle : MatrixBenchmarkState()

    /**
     * Calcul en cours
     * @param matrixSize Taille de la matrice en cours de calcul
     */
    data class Computing(val matrixSize: Int) : MatrixBenchmarkState()

    /**
     * Benchmark terminé avec succès
     * @param result Résultat du benchmark
     * @param history Historique des benchmarks (pour le graphique)
     */
    data class Success(
        val result: MatrixBenchmarkResult,
        val history: List<MatrixBenchmarkResult> = emptyList()
    ) : MatrixBenchmarkState()

    /**
     * Erreur pendant le benchmark
     * @param message Message d'erreur
     * @param matrixSize Taille de la matrice qui a causé l'erreur
     */
    data class Error(
        val message: String,
        val matrixSize: Int
    ) : MatrixBenchmarkState()
}

/**
 * Data class pour les données du graphique de scalabilité
 */
data class ScalabilityChartData(
    val cpuPoints: List<Pair<Int, Long>>,  // (matrixSize, timeMs)
    val gpuPoints: List<Pair<Int, Long>>,  // (matrixSize, timeMs)
    val crossoverPoint: Int? = null         // Point où GPU devient plus rapide
) {
    companion object {
        fun fromHistory(history: List<MatrixBenchmarkResult>): ScalabilityChartData {
            val sortedHistory = history.sortedBy { it.matrixSize }

            val cpuPoints = sortedHistory.map { it.matrixSize to it.cpuTimeMs }
            val gpuPoints = sortedHistory.map { it.matrixSize to it.gpuTotalTimeMs }

            // Trouver le crossover point (première taille où GPU < CPU)
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