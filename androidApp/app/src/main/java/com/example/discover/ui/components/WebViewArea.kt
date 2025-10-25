package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri // Required for Uri parsing
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient // Required for onProgressChanged
import android.webkit.WebView
import android.webkit.WebViewClient // Required for onPageStarted, onPageFinished
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.discover.ui.theme.PrimaryGreen
import com.example.discover.ui.theme.SurfaceDark
import kotlin.text.lowercase

private const val WEB_VIEW_AREA_TAG = "WebViewArea"

@SuppressLint("SetJavaScriptEnabled", "QueryPermissionsNeeded")
@Composable
fun WebViewArea(
    webView: WebView, // <-- Accept the pre-made WebView
    url: String, // The target URL from ViewModel
    onCloseArea: () -> Unit, onWebViewHistoryBack: () -> Unit
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var webViewInstanceFromFactory: WebView? by remember { mutableStateOf(null) }
    var pendingUrlAfterBlank by remember { mutableStateOf<String?>(null) }

    // This is the ONLY place we command the WebView.
    // It runs when the URL changes.
    LaunchedEffect(url, webView) {
        // Only load if the URL is different. This prevents reloads.
        // And ensure URL is not empty, which we pass when hidden.
        if (webView.url != url && url.isNotBlank()) {
            webView.loadUrl(url)
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, url) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "ON_RESUME for URL prop: $url. Current WebView URL: ${webViewInstanceFromFactory?.url}"
                )
                if (webViewInstanceFromFactory != null && pendingUrlAfterBlank == null) {
                    val currentWebViewActualUrl = webViewInstanceFromFactory?.url
                    var targetUrlForDisplayInWebView = url
                    val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"

                    // Determine if Google Docs fallback is implicitly the target
                    var isGDocsFallback = false
                    if (isPdf) {
                        try {
                            // Check if an app can handle the PDF URI directly
                            val pdfIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                            val resolveInfo =
                                context.packageManager.queryIntentActivities(pdfIntent, 0)
                            if (resolveInfo.isEmpty()) { // No direct handler, assume GDocs
                                isGDocsFallback = true
                            }
                        } catch (e: Exception) { // Problem with URI, unlikely for valid PDF URL
                            e.printStackTrace()
                            isGDocsFallback = true // Assume GDocs if URI check fails
                        }
                        if (isGDocsFallback) {
                            targetUrlForDisplayInWebView =
                                "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                        }
                    }

                    // If it's a YouTube link or a PDF that is *not* a GDocs fallback (meaning it should be external), don't try to reload from onResume.
                    if (isPdf && !isGDocsFallback) {
                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "ON_RESUME: URL ($url) is likely external type. No auto-reload from onResume."
                        )
                    } else if (currentWebViewActualUrl != targetUrlForDisplayInWebView && targetUrlForDisplayInWebView != "about:blank") {
                        Log.i(
                            WEB_VIEW_AREA_TAG,
                            "ON_RESUME: Content ($currentWebViewActualUrl) differs from GDocs/target ($targetUrlForDisplayInWebView). Signaling LOAD_URL."
                        )
                        // internalWebViewAction = WebViewInternalAction.LOAD_URL // Be cautious with this
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        if (webViewProgress < 100 && webViewProgress > 0 && (webViewInstanceFromFactory?.url != null && webViewInstanceFromFactory?.url != "about:blank")) {
            LinearProgressIndicator(
                progress = { webViewProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = SurfaceDark.copy(alpha = 0.3f)
            )
        }
        // The AndroidView now simply PLACES the existing WebView.
        AndroidView(
            factory = { webView }, // The factory just returns the instance.
            modifier = Modifier.weight(1f),
            update = { view ->
                // The update block ensures the clients are correctly set.
                view.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(v: WebView?, newProgress: Int) {
                        webViewProgress = newProgress
                    }
                }
                view.webViewClient = object : WebViewClient() {
                    // Your onPageStarted, onPageFinished, shouldOverrideUrlLoading logic here.
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
