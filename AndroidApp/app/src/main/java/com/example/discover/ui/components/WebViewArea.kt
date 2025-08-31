package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap // Required for WebViewClient
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient // Required for onProgressChanged
import android.webkit.WebView
import android.webkit.WebViewClient // Required for onPageStarted, onPageFinished
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

enum class WebViewInternalAction {
    NONE, LOAD_URL, CLEAR_THEN_LOAD_URL, CLEAR_ONLY
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewArea(
    url: String,
    onCloseArea: () -> Unit
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var webViewInstanceFromFactory: WebView? by remember { mutableStateOf(null) } // To hold the instance from factory for BackHandler
    var internalWebViewAction by remember(url) { mutableStateOf(WebViewInternalAction.NONE) }
    var pendingUrlAfterBlank by remember { mutableStateOf<String?>(null) }


    // Define clients within remember to ensure they are stable across recompositions
    // unless their keys change (if you were to add keys to remember for them).
    val localWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // Only update progress for actual content, not for about:blank
                if (view?.url != null && view.url != "about:blank") {
                    webViewProgress = newProgress
                    Log.d(WEB_VIEW_AREA_TAG, "Progress: $newProgress for ${view.url}")
                } else if (view?.url == "about:blank") {
                    webViewProgress = 0 // Explicitly set to 0 or 100 for blank if desired
                }
            }
        }
    }

    val localWebViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, currentUrl: String?, favicon: Bitmap?) {
                super.onPageStarted(view, currentUrl, favicon)
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Client: onPageStarted: $currentUrl, pending: $pendingUrlAfterBlank"
                )
                if (currentUrl != null && currentUrl != "about:blank") {
                    webViewProgress = 0 // Reset progress when a new page starts loading
                }
            }

            override fun onPageFinished(view: WebView?, currentFinishedUrl: String?) {
                super.onPageFinished(view, currentFinishedUrl)
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Client: onPageFinished: $currentFinishedUrl, pending: $pendingUrlAfterBlank"
                )

                if (currentFinishedUrl == "about:blank" && pendingUrlAfterBlank != null) {
                    val urlToLoad = pendingUrlAfterBlank
                    pendingUrlAfterBlank = null
                    Log.i(
                        WEB_VIEW_AREA_TAG,
                        "Client: Finished 'about:blank'. Loading pending: $urlToLoad"
                    )
                    view?.loadUrl(urlToLoad!!)
                } else if (currentFinishedUrl != null && currentFinishedUrl != "about:blank") {
                    webViewProgress = 100 // Page finished loading
                }
            }

            // You would add shouldOverrideUrlLoading here if needed for external intents
            // For now, focusing on progress bar and clear-then-load.
        }
    }


    LaunchedEffect(url) {
        Log.d(WEB_VIEW_AREA_TAG, "LaunchedEffect (URL prop changed): $url")
        val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"
        pendingUrlAfterBlank = null // Reset pending URL when main URL prop changes

        if (isPdf) {
            internalWebViewAction = WebViewInternalAction.CLEAR_ONLY
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Log.i(WEB_VIEW_AREA_TAG, "Launched PDF intent for: $url")
            } catch (e: ActivityNotFoundException) {
                Log.e(WEB_VIEW_AREA_TAG, "No app for PDF: $url. Falling back to Google Docs.", e)
                Toast.makeText(context, "No app for PDF. Trying Google Docs.", Toast.LENGTH_LONG)
                    .show()
                internalWebViewAction = WebViewInternalAction.CLEAR_THEN_LOAD_URL // For Google Docs
            }
        } else {
            // Check current webView URL to decide if it's an initial load or a new URL
            val currentActualUrl = webViewInstanceFromFactory?.url
            if (currentActualUrl == "about:blank" || currentActualUrl == null || currentActualUrl == "") {
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "WebView is blank or null. Setting CLEAR_THEN_LOAD_URL for $url"
                )
                internalWebViewAction =
                    WebViewInternalAction.CLEAR_THEN_LOAD_URL // If blank, it is an initial load for this new URL
            } else if (currentActualUrl != url &&
                currentActualUrl != "https://docs.google.com/gview?embedded=true&url=${
                    android.net.Uri.encode(
                        url
                    )
                }"
            ) {
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Current URL ($currentActualUrl) differs from new target ($url). Setting CLEAR_THEN_LOAD_URL."
                )
                internalWebViewAction = WebViewInternalAction.CLEAR_THEN_LOAD_URL
            } else {
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "WebView already on $url or equivalent. Setting action to NONE."
                )
                internalWebViewAction =
                    WebViewInternalAction.NONE // Already on target or no action needed
            }
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
                    val targetUrlForDisplayInWebView =
                        if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                            "https://docs.google.com/gview?embedded=true&url=${
                                android.net.Uri.encode(
                                    url
                                )
                            }"
                        } else {
                            url
                        }
                    if (currentWebViewActualUrl != targetUrlForDisplayInWebView && targetUrlForDisplayInWebView != "about:blank") {
                        Log.i(
                            WEB_VIEW_AREA_TAG,
                            "ON_RESUME: Content ($currentWebViewActualUrl) differs from target ($targetUrlForDisplayInWebView). Signaling direct LOAD_URL."
                        )
                        // Avoid CLEAR_THEN_LOAD_URL from onResume to prevent race conditions with pendingUrlAfterBlank.
                        // If a reload is truly needed on resume, it should be a direct one.
                        // However, it's generally safer if LaunchedEffect(url) is the sole driver for new URL loads.
                        // internalWebViewAction = WebViewInternalAction.LOAD_URL
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Condition for showing progress bar
        if (webViewProgress < 100 && webViewProgress > 0 && // Show only during active loading
            (webViewInstanceFromFactory?.url != null && webViewInstanceFromFactory?.url != "about:blank")
        ) {
            LinearProgressIndicator(
                progress = { webViewProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = SurfaceDark.copy(alpha = 0.3f)
            )
        }

        AndroidView(
            factory = { contextForFactory ->
                Log.d(WEB_VIEW_AREA_TAG, "AndroidView Factory: Creating WebView instance.")
                WebView(contextForFactory).apply {
                    webViewInstanceFromFactory = this // Store instance

                    // Crucially, assign your custom clients
                    this.webViewClient = localWebViewClient
                    this.webChromeClient = localWebChromeClient

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true); builtInZoomControls = true; displayZoomControls =
                        false
                        allowFileAccess = false; javaScriptCanOpenWindowsAutomatically = false
                        mediaPlaybackRequiresUserGesture = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            update = { webView ->
                // Ensure clients are set if the webView instance is re-created by Compose
                if (webView.webViewClient != localWebViewClient) webView.webViewClient =
                    localWebViewClient
                if (webView.webChromeClient != localWebChromeClient) webView.webChromeClient =
                    localWebChromeClient


                val actionToExecute = internalWebViewAction
                if (actionToExecute != WebViewInternalAction.NONE) {
                    Log.d(
                        WEB_VIEW_AREA_TAG,
                        "Update Block. Action: $actionToExecute, URL Prop: $url, WebView URL: ${webView.url}, Pending: $pendingUrlAfterBlank"
                    )
                    internalWebViewAction = WebViewInternalAction.NONE // Consume action
                }

                when (actionToExecute) {
                    WebViewInternalAction.CLEAR_ONLY -> {
                        if (webView.url != "about:blank") {
                            Log.d(WEB_VIEW_AREA_TAG, "Action CLEAR_ONLY: Loading 'about:blank'.")
                            pendingUrlAfterBlank = null
                            webView.loadUrl("about:blank")
                        }
                    }

                    WebViewInternalAction.LOAD_URL -> {
                        val targetUrlToLoad =
                            if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                "https://docs.google.com/gview?embedded=true&url=${
                                    android.net.Uri.encode(
                                        url
                                    )
                                }"
                            } else {
                                url
                            }
                        if (webView.url != targetUrlToLoad || (webView.url == "about:blank" && targetUrlToLoad != "about:blank")) {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Action LOAD_URL: Loading target '$targetUrlToLoad'."
                            )
                            pendingUrlAfterBlank = null
                            webView.loadUrl(targetUrlToLoad)
                        } else {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Action LOAD_URL: Already on target '$targetUrlToLoad'."
                            )
                        }
                    }

                    WebViewInternalAction.CLEAR_THEN_LOAD_URL -> {
                        val targetUrlToLoad =
                            if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                "https://docs.google.com/gview?embedded=true&url=${
                                    android.net.Uri.encode(
                                        url
                                    )
                                }"
                            } else {
                                url
                            }

                        if (webView.url == targetUrlToLoad && pendingUrlAfterBlank == null) {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Action CLEAR_THEN_LOAD_URL: Already on target '$targetUrlToLoad'. No clear needed."
                            )
                            return@AndroidView
                        }
                        if (pendingUrlAfterBlank == targetUrlToLoad && webView.url == "about:blank") {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Action CLEAR_THEN_LOAD_URL: 'about:blank' already loaded for pending '$targetUrlToLoad'. Awaiting onPageFinished."
                            )
                            return@AndroidView
                        }

                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "Action CLEAR_THEN_LOAD_URL: Setting '$targetUrlToLoad' as pending and loading 'about:blank'."
                        )
                        pendingUrlAfterBlank = targetUrlToLoad
                        if (webView.url != "about:blank") {
                            webView.loadUrl("about:blank")
                        } else {
                            // If already on "about:blank", onPageFinished for "about:blank" might not fire again with some WebViews.
                            // The client's onPageFinished should still be called. If it reliably isn't for a *second* "about:blank" load,
                            // this might need a direct load of pendingUrl, but let's rely on client first.
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Action CLEAR_THEN_LOAD_URL: WebView is already 'about:blank'. 'onPageFinished' should handle loading '$pendingUrlAfterBlank'."
                            )
                            // To be absolutely sure if onPageFinished("about:blank") doesn't re-fire:
                            // val urlToLoadNow = pendingUrlAfterBlank
                            // pendingUrlAfterBlank = null
                            // webView.loadUrl(urlToLoadNow!!)
                            // But this makes the client logic partially redundant. Test current client first.
                        }
                    }

                    WebViewInternalAction.NONE -> {
                        if (webView.url == "about:blank" && url.isNotBlank() && pendingUrlAfterBlank == null) {
                            val targetForBlank = if (MimeTypeMap.getFileExtensionFromUrl(url)
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
                            if (targetForBlank != "about:blank") {
                                Log.w(
                                    WEB_VIEW_AREA_TAG,
                                    "Action NONE: WebView blank, URL prop is '$url'. Forcing load of '$targetForBlank'."
                                )
                                webView.loadUrl(targetForBlank)
                            }
                        }
                    }
                }
            }
        )
    }

    BackHandler(enabled = true) {
        if (webViewInstanceFromFactory?.canGoBack() == true && pendingUrlAfterBlank == null) {
            webViewInstanceFromFactory?.goBack()
        } else {
            onCloseArea()
        }
    }
}
