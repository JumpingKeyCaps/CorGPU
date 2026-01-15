package com.lebaillyapp.corgpu.benchmark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.corgpu.benchmark.domain.model.MatrixBenchmarkResult

/**
 * # ScalabilityChart
 *
 * A composable responsible for displaying **the scalability of CPU vs GPU** performance
 * across historical matrix benchmark runs.
 *
 * ## Purpose
 * - Show trends of computation time (ms) as matrix size increases
 * - Highlight the **crossover point** where GPU becomes faster than CPU
 * - Provide visual cues for performance comparison
 *
 * ## Notes / Expectations
 * - `history`: list of MatrixBenchmarkResult, typically accumulated in ViewModel
 * - Future implementation should:
 *     1. Sort results by matrixSize
 *     2. Plot CPU times (e.g., blue line) vs GPU total times (e.g., orange line)
 *     3. Optionally highlight crossoverPoint (first N where GPU < CPU)
 *     4. Include axes, grid lines, and labels for readability
 * - This is purely **presentation layer**; no calculations beyond simple mapping
 * - Can be combined with `LegendItem` for color-coded guide
 *
 * @param history List of benchmark results to plot
 * @param modifier Optional Modifier for layout adjustments
 */
@Composable
fun ScalabilityChart(
    history: List<MatrixBenchmarkResult>,
    modifier: Modifier = Modifier
) {
    // TODO: Implement chart drawing
    // Expected features:
    // - Line or bar representation of CPU vs GPU times
    // - Highlight crossover point
    // - Properly scale axes based on min/max values
    // - Include labels or tooltips if necessary
}


/**
 * # LegendItem
 *
 * Small composable to represent a **color-coded label** for charts.
 *
 * ## Purpose
 * - Used in combination with ScalabilityChart to indicate which line/color
 *   corresponds to CPU or GPU.
 * - Circular color indicator with adjacent text label
 *
 * ## Notes
 * - Circle drawn using Surface with RoundedCornerShape(50)
 * - Fixed size 12.dp × 12.dp, text aligned with small padding for readability
 * - Can be reused for other charts with multiple lines/colors
 *
 * @param color Color of the legend indicator
 * @param label Text label describing the color (e.g., "CPU", "GPU")
 */
@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(top = 4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(50),
                color = color
            ) {}
        }
        Text(
            label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}