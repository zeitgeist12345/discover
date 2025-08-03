package com.example.discover.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.*

@Composable
fun WebViewScreen(
    url: String,
    onClose: () -> Unit,
    onLikeClick: () -> Unit = {},
    onDislikeClick: () -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Single row header with title, like/dislike icons, and close button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark
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
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                // Like/Dislike icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dislike icon
                    IconButton(
                        onClick = {
                            isDisliked = !isDisliked
                            if (isDisliked) isLiked = false
                            onDislikeClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dislike",
                            tint = if (isDisliked) ErrorColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }

                    // Like icon
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            if (isLiked) isDisliked = false
                            onLikeClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Like",
                            tint = if (isLiked) SuccessColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                }

                // Close button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = TextPrimary
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