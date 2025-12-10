package com.lebaillyapp.corgpu.benchmark.ui.components

import androidx.compose.foundation.background
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