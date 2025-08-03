package com.example.discover.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.*

@Composable
fun WebViewScreen(
    url: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with close button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 Discover",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
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