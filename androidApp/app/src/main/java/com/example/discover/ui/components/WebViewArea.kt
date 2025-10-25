package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.webkit.WebChromeClient // Required for onProgressChanged
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient // Required for onPageStarted, onPageFinished
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.PrimaryGreen
import com.example.discover.ui.theme.SurfaceDark

@SuppressLint("SetJavaScriptEnabled", "QueryPermissionsNeeded")
@Composable
fun WebViewArea(
    webView: WebView,
    url: String,
    onCloseArea: () -> Unit,
    onWebViewHistoryBack: () -> Unit
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(webView, url) {
        val targetUrl = url.ifBlank { "about:blank" }
        if (webView.url != targetUrl) {
            webView.loadUrl(targetUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        // --- THIS IS THE FIX ---
        // We removed the unnecessary Box wrapper.
        // AnimatedVisibility is now a direct child of Column.
        // We give it a fixed height so it always occupies the same space,
        // preventing the layout from shifting when it appears or disappears.
        AnimatedVisibility(
            visible = webViewProgress in 1..99,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp) // This ensures the space is always reserved
        ) {
            LinearProgressIndicator(
                progress = { webViewProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = SurfaceDark.copy(alpha = 0.3f)
            )
        }
        // --- END OF FIX ---

        // The AndroidView now simply PLACES the existing WebView.
        AndroidView(
            factory = { webView },
            modifier = Modifier.weight(1f),
            update = { view ->
                // The update block ensures the clients are correctly set.
                view.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(v: WebView?, newProgress: Int) {
                        webViewProgress = newProgress
                    }
                }
                view.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val requestedUrl = request?.url ?: return false
                        if (requestedUrl.scheme != "http" && requestedUrl.scheme != "https") {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, requestedUrl))
                                return true
                            } catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                                Toast.makeText(context, "No app found to open this URL.", Toast.LENGTH_SHORT).show()
                                return true
                            }
                        }
                        return false
                    }
                }
            }
        )
    }

    BackHandler(enabled = true) {
        if (webView.canGoBack()) {
            webView.goBack()
            onWebViewHistoryBack()
        } else {
            onCloseArea()
        }
    }
}