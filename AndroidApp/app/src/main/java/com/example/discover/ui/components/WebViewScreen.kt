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
import androidx.compose.foundation.layout.Box // Explicit import for Box
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

// Defines the explicit actions the WebView should take, signaled from LaunchedEffects or other logic.
enum class WebViewInternalAction {
    NONE,                   // No specific action needed from the update block currently.
    LOAD_PRIMARY_URL,       // Load the main 'url' prop (or its Google Docs equivalent for PDFs).
    CLEAR_THEN_LOAD_PRIMARY,// Clear WebView (load about:blank) then LOAD_PRIMARY_URL.
    CLEAR_ONLY              // Only clear WebView (load about:blank), e.g., when showing placeholder.
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String, // The primary URL this screen is meant to display
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

    // Determines if the WebView area should be shown or a placeholder (e.g., for externally opened PDFs).
    // This is primarily driven by the 'url' prop's type.
    var shouldShowWebViewContent by remember(url) {
        val initialFileExtension = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase()
        mutableStateOf(initialFileExtension != "pdf")
    }

    // Signals the AndroidView's update block to perform specific operations.
    // Keyed by 'url' to reset the signal when the primary URL changes.
    var internalWebViewAction by remember(url) { mutableStateOf(WebViewInternalAction.NONE) }

    // --- WebViewClient ---
    // This client is re-created whenever the 'url' prop changes.
    // This ensures its internal logic (especially shouldOverrideUrlLoading)
    // has the correct 'url' context for comparisons.
    val currentAppWebViewClient = remember(url) {
        Log.d(WEB_VIEW_SCREEN_TAG, "Creating WebViewClient instance for URL: $url")
        object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                currentLoadingUrl: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, currentLoadingUrl, favicon)
                if (shouldShowWebViewContent && currentLoadingUrl != null && currentLoadingUrl != "about:blank") {
                    Log.d(
                        WEB_VIEW_SCREEN_TAG,
                        "Client($url) - Page load started for: $currentLoadingUrl. Resetting progress."
                    )
                    webViewProgress = 0
                }
            }

            override fun onPageFinished(view: WebView?, currentFinishedUrl: String?) {
                super.onPageFinished(view, currentFinishedUrl)
                if (shouldShowWebViewContent && currentFinishedUrl != null && currentFinishedUrl != "about:blank") {
                    Log.d(
                        WEB_VIEW_SCREEN_TAG,
                        "Client($url) - Page load finished for: $currentFinishedUrl. Progress to 100."
                    )
                    webViewProgress = 100
                }
                Log.d(
                    WEB_VIEW_SCREEN_TAG,
                    "Client($url) - Page finished (generic): $currentFinishedUrl. Title: ${view?.title}"
                )
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && shouldShowWebViewContent) {
                    Log.e(
                        WEB_VIEW_SCREEN_TAG,
                        "Client($url) - Error on: ${request.url}. Code: ${error?.errorCode}, Desc: ${error?.description}"
                    )
                    Toast.makeText(context, "Error: ${error?.description}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val requestedUri = request?.url
                val requestedUrlString = requestedUri?.toString()

                // 'url' in this context is the 'url' prop this WebViewClient instance was created with.
                Log.d(
                    WEB_VIEW_SCREEN_TAG,
                    "Client($url) - shouldOverrideUrlLoading for: $requestedUrlString. Client's main URL: $url"
                )

                if (requestedUri != null && requestedUrlString != null && requestedUrlString != view?.url && requestedUrlString != "about:blank") {
                    val mainPropUri = try {
                        url.toUri()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    // Check if the requested URL is effectively for the same site as the main 'url' prop.
                    // This handles www. subdomain and http/https scheme changes.
                    val isEffectivelySameSiteAsMainTarget = mainPropUri != null &&
                            requestedUri.host?.replace(
                                "www.",
                                ""
                            ) == mainPropUri.host?.replace("www.", "") &&
                            (requestedUri.scheme == mainPropUri.scheme ||
                                    (listOf("http", "https").contains(requestedUri.scheme) &&
                                            listOf("http", "https").contains(mainPropUri.scheme)))

                    if (!isEffectivelySameSiteAsMainTarget) {
                        Log.i(
                            WEB_VIEW_SCREEN_TAG,
                            "Client($url) - Attempting to open external URL: $requestedUrlString (Main site host: ${mainPropUri?.host})"
                        )
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, requestedUri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                            view?.stopLoading() // Stop WebView from further processing this URL.
                            return true // We've handled the URL.
                        } catch (e: ActivityNotFoundException) {
                            Log.e(
                                WEB_VIEW_SCREEN_TAG,
                                "Client($url) - No application found for $requestedUrlString",
                                e
                            )
                            return false // Let WebView try to handle it (might fail or show an error).
                        }
                    } else {
                        // It's a redirect for the main URL (e.g., producthunt.com to www.producthunt.com).
                        // Let the WebView handle this navigation internally.
                        Log.d(
                            WEB_VIEW_SCREEN_TAG,
                            "Client($url) - Requested URL $requestedUrlString is a same-site redirect for main target. WebView will handle."
                        )
                        return false
                    }
                }
                // Default: let WebView handle (e.g. initial load request from our app, or if URL is same as current)
                return false
            }
        }
    }

    // --- WebChromeClient ---
    val appWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // Update progress only if WebView is supposed to be visible and loading actual content.
                if (shouldShowWebViewContent && view?.url != null && view.url != "about:blank") {
                    webViewProgress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(
                    WEB_VIEW_SCREEN_TAG,
                    "WebChromeClient - Received title: $title for ${view?.url}"
                )
            }

            // Optional: Handle JS alerts, confirms, prompts if needed
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                AlertDialog.Builder(context).setTitle("JavaScript Alert").setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .setCancelable(false).show()
                return true
            }
            // Add onJsConfirm, onJsPrompt as needed

            override fun onPermissionRequest(request: PermissionRequest?) { // Deny all permission requests by default
                Log.w(
                    WEB_VIEW_SCREEN_TAG,
                    "Denying permission request for ${request?.origin} and resources ${request?.resources?.joinToString()}"
                )
                request?.deny()
            }
        }
    }

    // --- LaunchedEffect for Primary URL Changes ---
    // This effect runs when the 'url' prop changes, deciding the initial action.
    LaunchedEffect(url) {
        Log.d(WEB_VIEW_SCREEN_TAG, "LaunchedEffect (url changed): New URL is $url")
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase()

        if (fileExtension == "pdf") {
            Log.d(WEB_VIEW_SCREEN_TAG, "URL is PDF: $url. Attempting external open.")
            if (shouldShowWebViewContent) shouldShowWebViewContent =
                false // Hide WebView content area
            internalWebViewAction =
                WebViewInternalAction.CLEAR_ONLY      // Signal to clear the WebView
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Log.i(WEB_VIEW_SCREEN_TAG, "Successfully launched PDF intent for: $url")
            } catch (e: ActivityNotFoundException) {
                Log.e(
                    WEB_VIEW_SCREEN_TAG,
                    "No application for PDF: $url. Falling back to Google Docs.",
                    e
                )
                Toast.makeText(context, "No app for PDF. Trying Google Docs.", Toast.LENGTH_LONG)
                    .show()
                if (!shouldShowWebViewContent) shouldShowWebViewContent =
                    true // Show WebView for fallback
                internalWebViewAction =
                    WebViewInternalAction.LOAD_PRIMARY_URL // Signal to load Google Docs URL
            }
        } else { // Non-PDF URL
            Log.d(WEB_VIEW_SCREEN_TAG, "URL is non-PDF: $url.")
            if (!shouldShowWebViewContent) shouldShowWebViewContent =
                true // Ensure WebView content area is visible

            // If the WebView was previously showing 'about:blank' (e.g., after a PDF),
            // signal to clear it properly before loading the new URL.
            internalWebViewAction =
                if (webViewInstance?.url == "about:blank" || webViewInstance?.url == null) {
                    WebViewInternalAction.CLEAR_THEN_LOAD_PRIMARY
                } else {
                    WebViewInternalAction.LOAD_PRIMARY_URL
                }
        }
    }

    // --- DisposableEffect for Lifecycle (ON_RESUME) ---
    // Handles scenarios where the app is resumed and the WebView might need to refresh its content.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webViewInstance, url, shouldShowWebViewContent) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(WEB_VIEW_SCREEN_TAG, "Lifecycle ON_RESUME for URL: $url")
                if (shouldShowWebViewContent && webViewInstance != null) {
                    val currentWebViewActualUrl = webViewInstance?.url
                    val targetUrlForDisplayInWebView =
                        if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                            // This case implies PDF fallback to Google Docs
                            "https://docs.google.com/gview?embedded=true&url=${
                                android.net.Uri.encode(
                                    url
                                )
                            }"
                        } else {
                            url // Original non-PDF URL
                        }

                    // If WebView isn't already showing the correct target URL (and it's not 'about:blank' waiting for initial load)
                    if (currentWebViewActualUrl != targetUrlForDisplayInWebView
                    ) {
                        Log.i(
                            WEB_VIEW_SCREEN_TAG,
                            "ON_RESUME: WebView content ($currentWebViewActualUrl) differs from target ($targetUrlForDisplayInWebView). Signaling LOAD_PRIMARY_URL."
                        )
                        internalWebViewAction = WebViewInternalAction.LOAD_PRIMARY_URL
                    } else {
                        Log.d(
                            WEB_VIEW_SCREEN_TAG,
                            "ON_RESUME: WebView content matches target or is appropriately blank. No action needed."
                        )
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- UI Structure ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        // Header Surface
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + Spacing.small,
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
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Dislike",
                            tint = if (isDisliked) ErrorColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            "Like",
                            tint = if (isLiked) SuccessColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) { Text("Close") }
            }
        }

        // WebView or Placeholder
        if (shouldShowWebViewContent) {
            // Progress Bar
            if (webViewProgress < 100 && (webViewInstance?.url != null && webViewInstance?.url != "about:blank")) {
                LinearProgressIndicator(
                    progress = { webViewProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryGreen,
                    trackColor = SurfaceDark.copy(alpha = 0.3f)
                )
            }

            // WebView via AndroidView
            AndroidView(
                factory = { contextForFactory ->
                    Log.d(
                        WEB_VIEW_SCREEN_TAG,
                        "AndroidView Factory: Creating new WebView instance."
                    )
                    WebView(contextForFactory).apply {
                        webViewInstance = this // Keep a reference to the WebView instance.
                        // Apply settings once during factory creation.
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true       // For sites that use localStorage.
                            loadWithOverviewMode = true    // Zooms out to show the full page.
                            useWideViewPort =
                                true         // Makes the viewport behave like a desktop browser.
                            setSupportZoom(true)           // Allows pinch-to-zoom.
                            builtInZoomControls =
                                true     // Show zoom controls (often disabled with displayZoomControls).
                            displayZoomControls = false    // Hide on-screen zoom buttons.
                            allowFileAccess =
                                false        // CRITICAL: Set to false for security unless explicitly needed for file:/// URLs.
                            javaScriptCanOpenWindowsAutomatically = false // Prevent pop-ups.
                            mediaPlaybackRequiresUserGesture = true // User must tap to play media.
                            // Consider: cacheMode = WebSettings.LOAD_DEFAULT or WebSettings.LOAD_CACHE_ELSE_NETWORK
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // WebView takes remaining space.
                update = { webView ->
                    // Ensure the correct WebViewClient and WebChromeClient are attached.
                    // This is crucial if currentAppWebViewClient is re-remembered due to 'url' change.
                    if (webView.webViewClient != currentAppWebViewClient) {
                        Log.d(
                            WEB_VIEW_SCREEN_TAG,
                            "AndroidView Update: Attaching WebViewClient for Composable URL: $url"
                        )
                        webView.webViewClient = currentAppWebViewClient
                    }
                    if (webView.webChromeClient != appWebChromeClient) { // Assuming appWebChromeClient doesn't depend on 'url'
                        webView.webChromeClient = appWebChromeClient
                    }

                    Log.d(
                        WEB_VIEW_SCREEN_TAG,
                        "AndroidView Update. Action: $internalWebViewAction, Composable URL: $url, WebView's actual URL: ${webView.url}"
                    )

                    val actionToProcess = internalWebViewAction
                    // Consume the signal immediately to prevent reprocessing on recomposition without state change.
                    if (actionToProcess != WebViewInternalAction.NONE) {
                        internalWebViewAction = WebViewInternalAction.NONE
                    }

                    when (actionToProcess) {
                        WebViewInternalAction.CLEAR_ONLY -> {
                            if (webView.url != "about:blank") {
                                Log.d(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update processing CLEAR_ONLY: Loading 'about:blank'."
                                )
                                webView.loadUrl("about:blank")
                            }
                        }

                        WebViewInternalAction.LOAD_PRIMARY_URL, WebViewInternalAction.CLEAR_THEN_LOAD_PRIMARY -> {
                            val targetUrlForWebView = if (MimeTypeMap.getFileExtensionFromUrl(url)
                                    ?.lowercase() == "pdf"
                            ) {
                                "https://docs.google.com/gview?embedded=true&url=${
                                    android.net.Uri.encode(
                                        url
                                    )
                                }" // PDF fallback
                            } else {
                                url // Standard URL
                            }

                            if (actionToProcess == WebViewInternalAction.CLEAR_THEN_LOAD_PRIMARY && webView.url != "about:blank") {
                                Log.d(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update processing CLEAR_THEN_LOAD_PRIMARY: Clearing then loading $targetUrlForWebView."
                                )
                                webView.loadUrl("about:blank") // Clear first.
                                // The actual load of targetUrlForWebView will happen if it's different.
                            }

                            // Load if WebView is not already on the target, or if it was cleared and now needs to load.
                            if (webView.url != targetUrlForWebView || (webView.url == "about:blank" && targetUrlForWebView != "about:blank")) {
                                Log.d(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update processing LOAD (or after CLEAR_THEN_LOAD): Loading $targetUrlForWebView."
                                )
                                webView.loadUrl(targetUrlForWebView)
                            } else {
                                Log.d(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update: WebView already on target $targetUrlForWebView or no load needed. No action from signal."
                                )
                            }
                        }

                        WebViewInternalAction.NONE -> {
                            // This block handles cases where the Composable updates but no explicit action was signaled.
                            // This might be due to ON_RESUME not triggering an action because URL matched,
                            // or if 'url' prop itself didn't change but a recomposition occurred.
                            // If WebView is blank and should be showing content, this is a final safety net.
                            val targetUrlForWebView = if (MimeTypeMap.getFileExtensionFromUrl(url)
                                    ?.lowercase() == "pdf"
                            ) {
                                "https://docs.google.com/gview?embedded=true&url=${
                                    android.net.Uri.encode(
                                        url
                                    )
                                }"
                            } else {
                                url
                            }

                            if (webView.url == "about:blank" && targetUrlForWebView != "about:blank") {
                                Log.w(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update (NONE action): WebView is 'about:blank' but target is $targetUrlForWebView. Forcing load."
                                )
                                webView.loadUrl(targetUrlForWebView)
                            } else {
                                Log.d(
                                    WEB_VIEW_SCREEN_TAG,
                                    "Update (NONE action): No explicit action, current WebView URL: ${webView.url}. Target: $targetUrlForWebView"
                                )
                            }
                        }
                    }
                }
            )
        } else {
            // Placeholder UI when shouldShowWebViewContent is false (e.g., for externally opened PDFs).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Content may be opening in an external application.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
            // When the placeholder is shown, ensure the underlying WebView (if it exists) is cleared.
            LaunchedEffect(webViewInstance) {
                if (webViewInstance?.url != "about:blank") {
                    Log.d(
                        WEB_VIEW_SCREEN_TAG,
                        "Placeholder visible: Clearing actual WebView instance to 'about:blank'."
                    )
                    webViewInstance?.loadUrl("about:blank")
                }
            }
        }
    }

    // System Back Button Handler
    BackHandler(enabled = true) {
        if (shouldShowWebViewContent && webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose() // Fallback to closing the screen.
        }
    }
}
