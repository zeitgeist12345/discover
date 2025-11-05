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
                    // The viewModel() delegate will correctly use AndroidViewModelFactory
                    // if DiscoverViewModel extends AndroidViewModel.
                    // No explicit factory passing is needed here IF you're using the default.
                    val viewModel: DiscoverViewModel = viewModel()
                    // If you had a custom factory before, you'd adapt it.
                    // For example, if you were doing this manually:
                    // val viewModel: DiscoverViewModel = viewModel(
                    //     factory = object : ViewModelProvider.Factory {
                    //         override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    //             return DiscoverViewModel(application) as T
                    //         }
                    //     }
                    // )
                    // The simpler `viewModel()` should now suffice.

                    DiscoverScreen(viewModel = viewModel)
                }
            }
        }
    }
}
