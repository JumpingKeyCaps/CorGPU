package com.lebaillyapp.corgpu.benchmark.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.corgpu.benchmark.domain.gpu.GpuMatrixHelper
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkResult
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MatrixBenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val gpuHelper = GpuMatrixHelper(application.applicationContext)

    // État de l'UI
    private val _state = MutableStateFlow<MatrixBenchmarkState>(MatrixBenchmarkState.Idle)
    val state: StateFlow<MatrixBenchmarkState> = _state.asStateFlow()

    // Historique des benchmarks pour le graphique
    private val benchmarkHistory = mutableListOf<MatrixBenchmarkResult>()

    /**
     * Lance un benchmark complet : CPU vs GPU+Coroutines
     */
    fun runBenchmark(matrixSize: Int) {
        if (_state.value is MatrixBenchmarkState.Computing) {
            return // Déjà en cours
        }

        viewModelScope.launch {
            try {
                _state.value = MatrixBenchmarkState.Computing(matrixSize)

                // Génération des matrices aléatoires
                val matrixA = generateRandomMatrix(matrixSize)
                val matrixB = generateRandomMatrix(matrixSize)

                // Calcul mémoire allouée (approximatif)
                val memoryMb = (matrixSize * matrixSize * 4 * 3) / (1024f * 1024f) // 3 matrices float32

                // Benchmark CPU (coroutines avec async)
                val cpuTime = benchmarkCpu(matrixA, matrixB)

                // Benchmark GPU (coroutines + GPU avec async)
                val (gpuTotalTime, gpuComputeTime) = benchmarkGpu(matrixA, matrixB)

                // Créer le résultat
                val result = MatrixBenchmarkResult(
                    matrixSize = matrixSize,
                    cpuTimeMs = cpuTime,
                    gpuTotalTimeMs = gpuTotalTime,
                    gpuComputeTimeMs = gpuComputeTime,
                    memoryAllocatedMb = memoryMb
                )

                // Ajouter à l'historique
                benchmarkHistory.add(result)

                // Mettre à jour l'état
                _state.value = MatrixBenchmarkState.Success(
                    result = result,
                    history = benchmarkHistory.toList()
                )

            } catch (e: Exception) {
                _state.value = MatrixBenchmarkState.Error(
                    message = e.message ?: "Unknown error",
                    matrixSize = matrixSize
                )
            }
        }
    }

    /**
     * Benchmark CPU : Multiplication naive en triple boucle avec async
     * Utilise async pour comparaison équitable avec l'approche GPU
     */
    private suspend fun benchmarkCpu(
        matrixA: Array<FloatArray>,
        matrixB: Array<FloatArray>
    ): Long = withContext(Dispatchers.Default) {
        val startTime = System.nanoTime()

        // Utiliser async pour orchestration similaire au GPU
        val deferred = async {
            val size = matrixA.size
            val result = Array(size) { FloatArray(size) }

            // Multiplication matricielle naive O(n³)
            for (i in 0 until size) {
                for (j in 0 until size) {
                    var sum = 0f
                    for (k in 0 until size) {
                        sum += matrixA[i][k] * matrixB[k][j]
                    }
                    result[i][j] = sum
                }
            }
            result
        }

        // Attendre le résultat (comme pour GPU)
        deferred.await()

        val endTime = System.nanoTime()
        return@withContext (endTime - startTime) / 1_000_000 // Convertir en ms
    }

    /**
     * Benchmark GPU : Délégation au shader via async
     * Retourne (temps total, temps compute seul)
     */
    private suspend fun benchmarkGpu(
        matrixA: Array<FloatArray>,
        matrixB: Array<FloatArray>
    ): Pair<Long, Long> = withContext(Dispatchers.Default) {
        // Utiliser async pour orchestration non-bloquante
        val deferred = async {
            gpuHelper.multiplyMatrices(matrixA, matrixB)
        }

        // Attendre le résultat
        val (_, timings) = deferred.await()

        return@withContext Pair(timings.totalMs, timings.computeMs)
    }

    /**
     * Génère une matrice aléatoire NxN avec valeurs normalisées [0, 1]
     */
    private fun generateRandomMatrix(size: Int): Array<FloatArray> {
        return Array(size) {
            FloatArray(size) {
                Random.nextFloat()
            }
        }
    }

    /**
     * Reset l'état vers Idle
     */
    fun resetState() {
        _state.value = MatrixBenchmarkState.Idle
    }

    /**
     * Clear l'historique des benchmarks
     */
    fun clearHistory() {
        benchmarkHistory.clear()
        _state.value = MatrixBenchmarkState.Idle
    }

    /**
     * Cleanup
     */
    override fun onCleared() {
        super.onCleared()
        gpuHelper.release()
    }
}