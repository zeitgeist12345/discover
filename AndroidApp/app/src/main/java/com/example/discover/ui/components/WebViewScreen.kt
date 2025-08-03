package com.example.discover.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.* // Keep existing runtime imports
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.*

@Composable
fun WebViewScreen(
    url: String,
    isLiked: Boolean, // Added parameter
    isDisliked: Boolean, // Added parameter
    onDiscoverClick: () -> Unit = {},
    onDislikeClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onClose: () -> Unit
) {
    // Local mutableStateOf for isLiked and isDisliked are removed.
    // The state is now passed as parameters.

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Single row header with title, like/dislike icons, and close button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark // Assuming SurfaceDark is defined in your theme
        ) {
            // Get status bar padding
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = statusBarPadding.calculateTopPadding() + Spacing.small,
                        start = Spacing.medium,
                        end = Spacing.medium,
                        bottom = Spacing.small
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "🌐 Discover",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary, // Assuming TextPrimary is defined in your theme
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onDiscoverClick)
                )

                // Like/Dislike icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dislike icon
                    IconButton(
                        onClick = {
                            // isDisliked = !isDisliked // Logic moved to ViewModel
                            // if (isDisliked) isLiked = false // Logic moved to ViewModel
                            onDislikeClick() // This now calls the ViewModel's function
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dislike",
                            tint = if (isDisliked) ErrorColor else TextSecondary, // Use passed in isDisliked
                            modifier = Modifier.size(Spacing.large)
                        )
                    }

                    // Like icon
                    IconButton(
                        onClick = {
                            // isLiked = !isLiked // Logic moved to ViewModel
                            // if (isLiked) isDisliked = false // Logic moved to ViewModel
                            onLikeClick() // This now calls the ViewModel's function
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Like",
                            tint = if (isLiked) SuccessColor else TextSecondary, // Use passed in isLiked
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                }

                // Close button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor, // Assuming ErrorColor is defined
                        contentColor = TextPrimary  // Assuming TextPrimary is defined
                    )
                ) {
                    Text("Close")
                }
            }
        }

        // WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { webView ->
                webView.loadUrl(url)
            }
        )
    }
}
