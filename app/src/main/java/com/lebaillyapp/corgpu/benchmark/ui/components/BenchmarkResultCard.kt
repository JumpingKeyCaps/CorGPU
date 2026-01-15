package com.lebaillyapp.corgpu.benchmark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * # BenchmarkResultCard
 *
 * A composable to display **individual benchmark results** in a card format.
 *
 * ## Purpose
 * - Show benchmark title (CPU/GPU) with an icon
 * - Display execution time in milliseconds prominently
 * - Provide a simple visual bar to indicate relative performance
 * - Color-coded to match CPU/GPU distinction
 *
 * ## Design Notes
 * 1. Uses a fixed **height (140.dp)** and rounded corners for UI consistency
 * 2. Card background is dark (`0xFF1E1E1E`) to emphasize colored highlights
 * 3. Top row includes an icon (`Settings`) as a placeholder for future status/action
 * 4. Execution time uses **large, bold font (32.sp)** to draw attention
 * 5. Visual bar:
 *    - Background: `0xFF2A2A2A` (dark gray)
 *    - Foreground: colored bar proportional to value (here hardcoded 80%)
 *    - Rounded corners for aesthetics
 *
 * ## Parameters
 * @param title Title text (e.g., "CPU Time" or "GPU Time")
 * @param timeMs Execution time in milliseconds
 * @param color Primary color for text, icon, and progress bar
 * @param modifier Optional Modifier for further layout adjustments
 *
 * ## Notes on Subtleties
 * - Hardcoded fill ratio (`0.8f`) in progress bar is currently **static**
 *   for demo purposes; can be updated dynamically based on max benchmark value
 * - Icon is a **visual cue**, not interactive
 * - Card is **self-contained**; does not require external state
 */
@Composable
fun BenchmarkResultCard(
    title: String,
    timeMs: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .border(2.dp, color, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header avec icône
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Temps
            Column {
                Text(
                    "$timeMs ms",
                    color = color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                // Barre de progression visuelle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .fillMaxHeight()
                            .background(color, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}