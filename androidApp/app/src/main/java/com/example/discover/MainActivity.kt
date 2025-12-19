package com.example.discover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discover.ui.screens.DiscoverScreen
import com.example.discover.ui.theme.DiscoverTheme
import com.example.discover.viewmodel.DiscoverViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiscoverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: DiscoverViewModel = viewModel()
                    DiscoverScreen(viewModel = viewModel)
                }
            }
        }
    }
}
