package com.lebaillyapp.corgpu.benchmark.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkResult
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkState
import com.lebaillyapp.corgpu.benchmark.ui.components.BenchmarkResultCard
import com.lebaillyapp.corgpu.benchmark.ui.components.DetailedAnalysisSection
import com.lebaillyapp.corgpu.benchmark.viewmodel.MatrixBenchmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixBenchmarkScreen(
    viewModel: MatrixBenchmarkViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var matrixSize by remember { mutableIntStateOf(512) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CorGPU Benchmark",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab (statique pour l'instant)
            TabSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Matrix Size Slider
            MatrixSizeSlider(
                size = matrixSize,
                onSizeChange = { matrixSize = it },
                enabled = state !is MatrixBenchmarkState.Computing
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Computing Progress ou Résultats
            when (val currentState = state) {
                is MatrixBenchmarkState.Idle -> {
                    RunBenchmarkButton(
                        onClick = { viewModel.runBenchmark(matrixSize) }
                    )
                }

                is MatrixBenchmarkState.Computing -> {
                    ComputingProgress(matrixSize = currentState.matrixSize)
                }

                is MatrixBenchmarkState.Success -> {
                    ResultsSection(
                        result = currentState.result,
                        history = currentState.history
                    )
                }

                is MatrixBenchmarkState.Error -> {
                    ErrorSection(
                        message = currentState.message,
                        onRetry = { viewModel.runBenchmark(currentState.matrixSize) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabSection() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A2A2A)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Matrix Mult.",
                selected = true,
                onClick = { }
            )
            TabButton(
                text = "Image Convolution",
                selected = false,
                enabled = false,
                onClick = { }
            )
            TabButton(
                text = "Batch Ops",
                selected = false,
                enabled = false,
                onClick = { }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF4CAF50) else Color.Transparent,
            contentColor = if (selected) Color.White else Color.Gray,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFF555555)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun MatrixSizeSlider(
    size: Int,
    onSizeChange: (Int) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            "Matrix Size N: $size x $size",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = size.toFloat(),
            onValueChange = { onSizeChange(it.toInt()) },
            valueRange = 64f..1024f,
            steps = 14, // 64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768, 832, 896, 960, 1024
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF555555)
            )
        )
    }
}

@Composable
private fun RunBenchmarkButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "Run Benchmark",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ComputingProgress(matrixSize: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Calculating...",
            color = Color(0xFF64B5F6),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF64B5F6)
        )

        Text(
            "Matrix Size: $matrixSize x $matrixSize",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ResultsSection(
    result: MatrixBenchmarkResult,
    history: List<MatrixBenchmarkResult>
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Performance Comparison
            Text(
                "PERFORMANCE COMPARISON",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BenchmarkResultCard(
                    title = "CPU Coroutines (Total)",
                    timeMs = result.cpuTimeMs,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )

                BenchmarkResultCard(
                    title = "GPU + Coroutines (Total)",
                    timeMs = result.gpuTotalTimeMs,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            // Speedup
            Text(
                result.getSpeedupMessage(),
                color = Color(0xFF4CAF50),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Graphique de scalabilité
            if (history.size >= 2) {
              //  ScalabilityChart(history = history)
            }

            // Detailed Analysis
            DetailedAnalysisSection(result = result)

            // Logs
            LogsSection(log = result.toLogString())
        }
    }
}

@Composable
private fun LogsSection(log: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E1E1E)
    ) {
        Text(
            log,
            color = Color(0xFF90CAF9),
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Error",
            color = Color(0xFFEF5350),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            message,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF5350)
            )
        ) {
            Text("Retry")
        }
    }
}