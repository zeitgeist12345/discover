package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient // Required for onProgressChanged
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient // Required for onPageStarted, onPageFinished
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discover.ui.theme.PrimaryGreen
import com.example.discover.ui.theme.SurfaceDark
import com.example.discover.viewmodel.DiscoverViewModel

@SuppressLint("SetJavaScriptEnabled", "QueryPermissionsNeeded")
@Composable
fun WebViewArea(
    viewModel: DiscoverViewModel,
    webView: WebView,
    url: String?,
    onUrlChanged: (String) -> Unit,
    onCloseArea: () -> Unit,
    onWebViewHistoryBack: () -> Unit
) {
    val isWebViewLoading by viewModel.isWebViewLoading.collectAsStateWithLifecycle()
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(webView, url) {
        val targetUrl = url ?: "about:blank"

        // Check if it's a PDF and handle it BEFORE loading
        if (targetUrl.endsWith(".pdf", ignoreCase = true) ||
            targetUrl.contains(".pdf?", ignoreCase = true)) {

            // Use Google Docs viewer for PDFs
            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" +
                    java.net.URLEncoder.encode(targetUrl, "UTF-8")
            webView.loadUrl(googleDocsUrl)
        } else if (webView.url != targetUrl) {
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

        // 1. Create a Box with a fixed height to reserve the space.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            // 2. The AnimatedVisibility now lives inside the Box.
            //    It no longer needs a height modifier itself.
            androidx.compose.animation.AnimatedVisibility(visible = webViewProgress in 1..99) {
                LinearProgressIndicator(
                    progress = { webViewProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryGreen,
                    trackColor = SurfaceDark.copy(alpha = 0.3f)
                )
            }
        }

        // The AndroidView now simply PLACES the existing WebView.
        AndroidView(factory = { webView }, modifier = Modifier.weight(1f), update = { view ->
            // The update block ensures the clients are correctly set.
            view.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(v: WebView?, newProgress: Int) {
                    if (!isWebViewLoading || webView.url != "about:blank") {
                        webViewProgress = newProgress
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    // onReceivedTitle is a very reliable callback that fires after most
                    // navigation events, including those via JavaScript's History API.
                    // We use it as a trigger to ask the WebView for its current URL.
                    view?.url?.let { currentUrl ->
                        onUrlChanged(currentUrl)
                    }
                }
            }
            view.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // Report the URL as soon as navigation starts.
                    // This is good for immediate feedback.
                    url?.let { onUrlChanged(it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Also report the URL when the page finishes loading.
                    // This captures the final URL after any redirects.
                    url?.let { onUrlChanged(it) }

                    // The WebView has finished a draw pass, so it's safe to make it visible.
                    if (isWebViewLoading) {
                        viewModel.onWebViewPageVisible()
                        // Flicker is very annoying. It was better before.
                        webView.visibility = View.VISIBLE
                        webView.setBackgroundColor(Color.White.toArgb())
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    val requestedUrl = request?.url ?: return false
                    if (requestedUrl.scheme != "http" && requestedUrl.scheme != "https") {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, requestedUrl))
                            return true
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            Toast.makeText(
                                context, "No app found to open this URL.", Toast.LENGTH_SHORT
                            ).show()
                            return true
                        }
                    }
                    return false
                }
            }
        })
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