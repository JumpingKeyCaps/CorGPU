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


@Composable
fun ScalabilityChart(
    history: List<MatrixBenchmarkResult>,
    modifier: Modifier = Modifier
) {
  //todo---
}

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