package com.lebaillyapp.corgpu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebaillyapp.corgpu.benchmark.ui.screen.MatrixBenchmarkScreen
import com.lebaillyapp.corgpu.benchmark.viewmodel.MatrixBenchmarkViewModel
import com.lebaillyapp.corgpu.ui.theme.CorGPUTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CorGPUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MatrixBenchmarkViewModel = viewModel()
                    MatrixBenchmarkScreen(viewModel = viewModel)
                }
            }
        }
    }
}

