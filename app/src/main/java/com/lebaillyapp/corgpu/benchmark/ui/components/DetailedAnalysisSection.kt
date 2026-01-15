package com.lebaillyapp.corgpu.benchmark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkResult

/**
 * # DetailedAnalysisSection
 *
 * A composable section that provides **in-depth metrics** of a single
 * matrix benchmark result.
 *
 * ## Purpose
 * - Break down GPU benchmark timings and CPU/GPU comparison metrics
 * - Display memory allocation and matrix size
 * - Organize information in a **grid-like layout** for easy readability
 *
 * ## Layout Notes
 * - Column vertical arrangement with spacing of 12.dp between sections
 * - Section header: bold white text ("DETAILED ANALYSIS")
 * - Metrics displayed inside a rounded Surface (dark background)
 * - Each row contains 2 AnalysisItem composables, spaced evenly
 * - AnalysisItem: label (gray, small), value (white, medium-bold)
 *
 * ## Metrics Displayed
 * 1. Shader Compute Time (GPU compute-only)
 * 2. Transfer Overhead (% of total GPU time spent transferring)
 * 3. Transfer Cost (ms)
 * 4. Speedup (GPU vs CPU ratio)
 * 5. Memory Allocation (approximate MB)
 * 6. Matrix Size (NxN)
 *
 * ## Subtleties / Considerations
 * - Percentages and speedup are formatted to **1 decimal place**
 * - Memory allocation includes rough estimate of buffers (3 matrices × float32)
 * - UI layout uses weight=1f for all items in a row to **ensure equal spacing**
 * - Surface uses RoundedCornerShape for consistent card-like aesthetics
 * - Does **not perform any calculation**; purely displays pre-computed metrics
 *
 * @param result The MatrixBenchmarkResult containing all relevant metrics
 * @param modifier Optional Modifier for layout adjustments
 */
@Composable
fun DetailedAnalysisSection(
    result: MatrixBenchmarkResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "DETAILED ANALYSIS",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnalysisItem(
                        label = "Shader Compute Time",
                        value = "${result.gpuComputeTimeMs} ms",
                        modifier = Modifier.weight(1f)
                    )
                    AnalysisItem(
                        label = "Transfer Overhead",
                        value = "${String.format("%.1f", result.transferOverheadPercent)}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnalysisItem(
                        label = "Transfer Cost",
                        value = "${result.transferOverheadMs} ms",
                        modifier = Modifier.weight(1f)
                    )
                    AnalysisItem(
                        label = "Speedup",
                        value = "${String.format("%.1f", result.speedup)}x",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnalysisItem(
                        label = "Memory Allocation",
                        value = "+${String.format("%.1f", result.memoryAllocatedMb)}MB",
                        modifier = Modifier.weight(1f)
                    )
                    AnalysisItem(
                        label = "Matrix Size",
                        value = "${result.matrixSize}x${result.matrixSize}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


/**
 * # AnalysisItem
 *
 * A small UI component for displaying a **single label-value pair**.
 *
 * ## Purpose
 * - Used by DetailedAnalysisSection to display individual metrics
 * - Separates label (gray, small) from value (white, medium-bold)
 * - Can be reused for other grid or row layouts
 *
 * ## Notes
 * - Vertical spacing between label and value is 4.dp
 * - Value font is slightly larger and bold to draw attention
 *
 * @param label Metric name (e.g., "Speedup")
 * @param value Metric value (e.g., "1.8x")
 * @param modifier Optional Modifier for layout control
 */
@Composable
private fun AnalysisItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}