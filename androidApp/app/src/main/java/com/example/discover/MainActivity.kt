package com.example.discover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discover.ui.screens.DiscoverScreen
import com.example.discover.ui.theme.DiscoverTheme
import com.example.discover.viewmodel.DiscoverViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DiscoverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiscoverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DiscoverScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForeground()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackground()
    }
}
