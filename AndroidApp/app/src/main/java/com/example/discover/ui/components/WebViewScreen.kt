package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.discover.ui.theme.ErrorColor
import com.example.discover.ui.theme.PrimaryGreen
import com.example.discover.ui.theme.Spacing
import com.example.discover.ui.theme.SuccessColor
import com.example.discover.ui.theme.SurfaceDark
import com.example.discover.ui.theme.TextPrimary
import com.example.discover.ui.theme.TextSecondary
import kotlin.text.lowercase

private const val WEB_VIEW_SCREEN_TAG = "WebViewScreen"

enum class WebViewInternalAction {
    NONE,
    LOAD_URL,
    CLEAR_THEN_LOAD_URL,
    CLEAR_ONLY
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    isLiked: Boolean,
    isDisliked: Boolean,
    onDiscoverClick: () -> Unit = {},
    onDislikeClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onClose: () -> Unit
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var webViewInstance: WebView? by remember { mutableStateOf(null) }

    // Determines if the WebView content area should be displayed.
    // True if content is web page/fallback; false if PDF is opened externally (placeholder shown).
    var shouldShowWebViewContent by remember(url) {
        mutableStateOf(MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() != "pdf")
    }

    // Signals the AndroidView's update block for specific loading actions.
    var internalWebViewAction by remember(url) { mutableStateOf(WebViewInternalAction.NONE) }

    val currentAppWebViewClient = remember(url) {
        Log.d(WEB_VIEW_SCREEN_TAG, "Creating WebViewClient for URL: $url")
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, currentLoadingUrl: String?, favicon: Bitmap?) {
                super.onPageStarted(view, currentLoadingUrl, favicon)
                if (shouldShowWebViewContent && currentLoadingUrl != null && currentLoadingUrl != "about:blank") {
                    Log.d(WEB_VIEW_SCREEN_TAG, "Client($url) - Page started: $currentLoadingUrl. Progress to 0.")
                    webViewProgress = 0
                }
            }

            override fun onPageFinished(view: WebView?, currentFinishedUrl: String?) {
                super.onPageFinished(view, currentFinishedUrl)
                if (shouldShowWebViewContent && currentFinishedUrl != null && currentFinishedUrl != "about:blank") {
                    Log.d(WEB_VIEW_SCREEN_TAG, "Client($url) - Page finished: $currentFinishedUrl. Progress to 100.")
                    webViewProgress = 100
                }
                Log.d(WEB_VIEW_SCREEN_TAG, "Client($url) - Page finished (generic): $currentFinishedUrl. Title: ${view?.title}")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && shouldShowWebViewContent) {
                    Log.e(WEB_VIEW_SCREEN_TAG, "Client($url) - Error: ${request.url}. Code: ${error?.errorCode}, Desc: ${error?.description}")
                    Toast.makeText(context, "Error: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val requestedUri = request?.url
                val requestedUrlString = requestedUri?.toString()
                Log.d(WEB_VIEW_SCREEN_TAG, "Client($url) - Override: $requestedUrlString. Main prop URL for this client: $url")

                if (requestedUri != null && requestedUrlString != null && requestedUrlString != view?.url && requestedUrlString != "about:blank") {
                    val mainPropUri = try { url.toUri() } catch (e: Exception) {
                        Log.e(WEB_VIEW_SCREEN_TAG, "Client($url) - Error parsing mainPropUri from url: $url", e)
                        null
                    }
                    val isEffectivelySameSiteAsMainTarget = mainPropUri != null &&
                            requestedUri.host?.replace("www.", "") == mainPropUri.host?.replace("www.", "") &&
                            (requestedUri.scheme == mainPropUri.scheme || (listOf("http", "https").contains(requestedUri.scheme) && listOf("http", "https").contains(mainPropUri.scheme)))

                    if (!isEffectivelySameSiteAsMainTarget) {
                        Log.i(WEB_VIEW_SCREEN_TAG, "Client($url) - External attempt for $requestedUrlString (main host: ${mainPropUri?.host})")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, requestedUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                            view?.stopLoading()
                            return true
                        } catch (e: ActivityNotFoundException) {
                            Log.e(WEB_VIEW_SCREEN_TAG, "Client($url) - No app for $requestedUrlString", e)
                            return false // Let WebView try to load it
                        }
                    } else {
                        Log.d(WEB_VIEW_SCREEN_TAG, "Client($url) - Same-site redirect for $requestedUrlString. WebView will handle.")
                        return false // It's a redirect for the main URL, let WebView handle it
                    }
                }
                return false // Default: Let WebView handle
            }
        }
    }

    val appWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (shouldShowWebViewContent && view?.url != null && view.url != "about:blank") {
                    webViewProgress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(WEB_VIEW_SCREEN_TAG, "ChromeClient - Title: $title for ${view?.url}")
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                AlertDialog.Builder(context).setTitle("JavaScript Alert").setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .setCancelable(false).show()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                Log.w(WEB_VIEW_SCREEN_TAG, "Denying permission: ${request?.origin} for ${request?.resources?.joinToString()}")
                request?.deny()
            }
        }
    }

    LaunchedEffect(url) {
        Log.d(WEB_VIEW_SCREEN_TAG, "LaunchedEffect (URL changed): $url")
        val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"

        shouldShowWebViewContent = !isPdf // Set visibility based on PDF status

        if (isPdf) {
            internalWebViewAction = WebViewInternalAction.CLEAR_ONLY // Clear WebView if placeholder is to be shown
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                Log.i(WEB_VIEW_SCREEN_TAG, "Launched PDF intent for: $url")
            } catch (e: ActivityNotFoundException) {
                Log.e(WEB_VIEW_SCREEN_TAG, "No app for PDF: $url. Falling back to Google Docs.", e)
                Toast.makeText(context, "No app for PDF. Trying Google Docs.", Toast.LENGTH_LONG).show()
                shouldShowWebViewContent = true // Revert to show WebView for Google Docs fallback
                internalWebViewAction = WebViewInternalAction.LOAD_URL
            }
        } else { // Non-PDF
            // If WebView was showing placeholder (or is uninitialized), clear before loading.
            internalWebViewAction = if (webViewInstance?.url == "about:blank" || webViewInstance?.url == null) {
                WebViewInternalAction.CLEAR_THEN_LOAD_URL
            } else {
                WebViewInternalAction.LOAD_URL
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, url) { // Re-run if lifecycle owner or URL changes
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(WEB_VIEW_SCREEN_TAG, "ON_RESUME for URL: $url. shouldShowWebViewContent: $shouldShowWebViewContent")
                if (shouldShowWebViewContent && webViewInstance != null) { // Only if WebView should be visible
                    val currentWebViewActualUrl = webViewInstance?.url
                    val targetUrlForDisplay = if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                        // This implies fallback to Google Docs for a PDF
                        "https://docs.google.com/gview?embedded=true&url=${android.net.Uri.encode(url)}"
                    } else { url }

                    if (currentWebViewActualUrl != targetUrlForDisplay) { // Avoid no-op if both blank
                        Log.i(WEB_VIEW_SCREEN_TAG, "ON_RESUME: Content ($currentWebViewActualUrl) differs from target ($targetUrlForDisplay). Signaling LOAD_URL.")
                        internalWebViewAction = WebViewInternalAction.LOAD_URL
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
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        Surface( // Header
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + Spacing.small,
                        start = Spacing.medium,
                        end = Spacing.medium,
                        bottom = Spacing.small
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🌐 Discover",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onDiscoverClick)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    IconButton(onClick = onDislikeClick) {
                        Icon(Icons.Default.KeyboardArrowDown, "Dislike", tint = if (isDisliked) ErrorColor else TextSecondary, modifier = Modifier.size(Spacing.large))
                    }
                    IconButton(onClick = onLikeClick) {
                        Icon(Icons.Default.KeyboardArrowUp, "Like", tint = if (isLiked) SuccessColor else TextSecondary, modifier = Modifier.size(Spacing.large))
                    }
                }
                Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)) { Text("Close") }
            }
        }

        if (shouldShowWebViewContent) {
            // Show progress bar only if content is being loaded and not yet complete
            if (webViewProgress < 100 && (webViewInstance?.url != null && webViewInstance?.url != "about:blank")) {
                LinearProgressIndicator(
                    progress = { webViewProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryGreen,
                    trackColor = SurfaceDark.copy(alpha = 0.3f)
                )
            }

            AndroidView(
                factory = { contextForFactory ->
                    Log.d(WEB_VIEW_SCREEN_TAG, "AndroidView Factory: Creating WebView instance.")
                    WebView(contextForFactory).apply {
                        webViewInstance = this
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = false // Security best practice
                            javaScriptCanOpenWindowsAutomatically = false
                            mediaPlaybackRequiresUserGesture = true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize().weight(1f),
                update = { webView ->
                    // Ensure the correct clients are attached
                    if (webView.webViewClient != currentAppWebViewClient) {
                        webView.webViewClient = currentAppWebViewClient
                    }
                    if (webView.webChromeClient != appWebChromeClient) {
                        webView.webChromeClient = appWebChromeClient
                    }

                    Log.d(WEB_VIEW_SCREEN_TAG, "AndroidView Update. Action: $internalWebViewAction, URL: $url, WebView URL: ${webView.url}")

                    val actionToExecute = internalWebViewAction
                    if (actionToExecute != WebViewInternalAction.NONE) {
                        internalWebViewAction = WebViewInternalAction.NONE // Consume the signal
                    }

                    when (actionToExecute) {
                        WebViewInternalAction.CLEAR_ONLY -> {
                            if (webView.url != "about:blank") {
                                Log.d(WEB_VIEW_SCREEN_TAG, "Update: CLEAR_ONLY -> Loading about:blank.")
                                webView.loadUrl("about:blank")
                            }
                        }
                        WebViewInternalAction.LOAD_URL, WebViewInternalAction.CLEAR_THEN_LOAD_URL -> {
                            val targetUrlToLoad = if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                "https://docs.google.com/gview?embedded=true&url=${android.net.Uri.encode(url)}"
                            } else { url }

                            if (actionToExecute == WebViewInternalAction.CLEAR_THEN_LOAD_URL && webView.url != "about:blank") {
                                Log.d(WEB_VIEW_SCREEN_TAG, "Update: CLEAR_THEN_LOAD_URL -> Clearing before loading $targetUrlToLoad.")
                                webView.loadUrl("about:blank") // Clear first
                            }

                            // Load if WebView is not already on the target, or if it was just cleared and target is not blank
                            if (webView.url != targetUrlToLoad || (webView.url == "about:blank" && targetUrlToLoad != "about:blank")) {
                                Log.d(WEB_VIEW_SCREEN_TAG, "Update: Loading target $targetUrlToLoad.")
                                webView.loadUrl(targetUrlToLoad)
                            } else {
                                Log.d(WEB_VIEW_SCREEN_TAG, "Update: WebView already on target $targetUrlToLoad or no load signaled. No action.")
                            }
                        }
                        WebViewInternalAction.NONE -> {
                            // Safety net: if WebView is blank but should be showing content.
                            // This usually means an initial load or a post-resume load didn't get explicitly signaled or processed.
                            if (webView.url == "about:blank" && shouldShowWebViewContent) {
                                val targetUrlToLoad = if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                     // This implies fallback for PDF
                                    "https://docs.google.com/gview?embedded=true&url=${android.net.Uri.encode(url)}"
                                } else { url }

                                if (targetUrlToLoad != "about:blank") {
                                    Log.w(WEB_VIEW_SCREEN_TAG, "Update (NONE action): WebView is blank but target is $targetUrlToLoad. Forcing load.")
                                    webView.loadUrl(targetUrlToLoad)
                                }
                            }
                        }
                    }
                }
            )
        } else { // Placeholder UI (e.g., for PDFs opened externally)
            Box(
                modifier = Modifier.fillMaxSize().weight(1f).padding(Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Content may be opening in an external application.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
            // When placeholder is shown, ensure the actual WebView instance is cleared.
            LaunchedEffect(webViewInstance) {
                if (webViewInstance?.url != "about:blank") {
                    Log.d(WEB_VIEW_SCREEN_TAG, "Placeholder visible: Clearing WebView instance to 'about:blank'.")
                    webViewInstance?.loadUrl("about:blank")
                }
            }
        }
    }

    BackHandler(enabled = true) {
        if (shouldShowWebViewContent && webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }
}