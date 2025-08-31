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

// You can keep the tag and enum here or move them to a common place if used elsewhere.
private const val WEB_VIEW_AREA_TAG = "WebViewArea" // Renamed for clarity

enum class WebViewInternalAction {
    NONE,
    LOAD_URL,
    CLEAR_THEN_LOAD_URL,
    CLEAR_ONLY
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewArea(
    url: String,
    onCloseArea: () -> Unit // Callback for when this area determines it wants to be closed
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var webViewInstance: WebView? by remember { mutableStateOf(null) }

    // Signals the AndroidView's update block for specific loading actions.
    var internalWebViewAction by remember(url) { mutableStateOf(WebViewInternalAction.NONE) }

    val currentAppWebViewClient = remember(url) {
        Log.d(WEB_VIEW_AREA_TAG, "Creating WebViewClient for URL: $url")
        object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                currentLoadingUrl: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, currentLoadingUrl, favicon)
                if (currentLoadingUrl != null && currentLoadingUrl != "about:blank") {
                    Log.d(
                        WEB_VIEW_AREA_TAG,
                        "Client($url) - Page started: $currentLoadingUrl. Progress to 0."
                    )
                    webViewProgress = 0
                }
            }

            override fun onPageFinished(view: WebView?, currentFinishedUrl: String?) {
                super.onPageFinished(view, currentFinishedUrl)
                if (currentFinishedUrl != null && currentFinishedUrl != "about:blank") {
                    Log.d(
                        WEB_VIEW_AREA_TAG,
                        "Client($url) - Page finished: $currentFinishedUrl. Progress to 100."
                    )
                    webViewProgress = 100
                }
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Client($url) - Page finished (generic): $currentFinishedUrl. Title: ${view?.title}"
                )
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    Log.e(
                        WEB_VIEW_AREA_TAG,
                        "Client($url) - Error: ${request.url}. Code: ${error?.errorCode}, Desc: ${error?.description}"
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
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Client($url) - Override: $requestedUrlString. Main prop URL for this client: $url"
                )

                if (requestedUri != null && requestedUrlString != null && requestedUrlString != view?.url && requestedUrlString != "about:blank") {
                    val mainPropUri = try {
                        url.toUri()
                    } catch (e: Exception) {
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "Client($url) - Error parsing mainPropUri from url: $url",
                            e
                        )
                        null
                    }
                    val isEffectivelySameSiteAsMainTarget = mainPropUri != null &&
                            requestedUri.host?.replace(
                                "www.",
                                ""
                            ) == mainPropUri.host?.replace("www.", "") &&
                            (requestedUri.scheme == mainPropUri.scheme || (listOf(
                                "http",
                                "https"
                            ).contains(requestedUri.scheme) && listOf("http", "https").contains(
                                mainPropUri.scheme
                            )))

                    if (!isEffectivelySameSiteAsMainTarget) {
                        Log.i(
                            WEB_VIEW_AREA_TAG,
                            "Client($url) - External attempt for $requestedUrlString (main host: ${mainPropUri?.host})"
                        )
                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    requestedUri
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                            view?.stopLoading()
                            return true
                        } catch (e: ActivityNotFoundException) {
                            Log.e(
                                WEB_VIEW_AREA_TAG,
                                "Client($url) - No app for $requestedUrlString",
                                e
                            )
                            return false
                        }
                    } else {
                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "Client($url) - Same-site redirect for $requestedUrlString. WebView will handle."
                        )
                        return false
                    }
                }
                return false
            }
        }
    }

    val appWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (view?.url != null && view.url != "about:blank") {
                    webViewProgress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(WEB_VIEW_AREA_TAG, "ChromeClient - Title: $title for ${view?.url}")
            }

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

            override fun onPermissionRequest(request: PermissionRequest?) {
                Log.w(
                    WEB_VIEW_AREA_TAG,
                    "Denying permission: ${request?.origin} for ${request?.resources?.joinToString()}"
                )
                request?.deny()
            }
        }
    }

    LaunchedEffect(url) {
        Log.d(WEB_VIEW_AREA_TAG, "LaunchedEffect (URL changed): $url")
        val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"

        if (isPdf) {
            internalWebViewAction = WebViewInternalAction.CLEAR_ONLY
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                })
                Log.i(WEB_VIEW_AREA_TAG, "Launched PDF intent for: $url")
            } catch (e: ActivityNotFoundException) {
                Log.e(WEB_VIEW_AREA_TAG, "No app for PDF: $url. Falling back to Google Docs.", e)
                Toast.makeText(context, "No app for PDF. Trying Google Docs.", Toast.LENGTH_LONG)
                    .show()
                internalWebViewAction = WebViewInternalAction.LOAD_URL
            }
        } else {
            internalWebViewAction =
                if (webViewInstance?.url == "about:blank" || webViewInstance?.url == null) {
                    WebViewInternalAction.CLEAR_THEN_LOAD_URL
                } else {
                    WebViewInternalAction.LOAD_URL
                }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, url) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "ON_RESUME for URL: $url."
                )
                if (webViewInstance != null) {
                    val currentWebViewActualUrl = webViewInstance?.url
                    val targetUrlForDisplay =
                        if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                            "https://docs.google.com/gview?embedded=true&url=${
                                android.net.Uri.encode(
                                    url
                                )
                            }"
                        } else {
                            url
                        }

                    if (currentWebViewActualUrl != targetUrlForDisplay
                    ) {
                        Log.i(
                            WEB_VIEW_AREA_TAG,
                            "ON_RESUME: Content ($currentWebViewActualUrl) differs from target ($targetUrlForDisplay). Signaling LOAD_URL."
                        )
//                        internalWebViewAction = WebViewInternalAction.LOAD_URL
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // This Column now acts as the root for the content area (WebView or Placeholder)
    // It will be placed below the TopDiscoverBar by the parent Composable.
    Column(modifier = Modifier.fillMaxSize()) {
        // Progress Bar is part of this content column
        if (webViewProgress < 100 && (webViewInstance?.url != null && webViewInstance?.url != "about:blank")) {
            LinearProgressIndicator(
                progress = { webViewProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen, // Or your theme color
                trackColor = SurfaceDark.copy(alpha = 0.3f) // Or your theme color
            )
        }

        AndroidView(
            factory = { contextForFactory ->
                Log.d(WEB_VIEW_AREA_TAG, "AndroidView Factory: Creating WebView instance.")
                WebView(contextForFactory).apply {
                    webViewInstance = this
                    // Set background color to mitigate white flash during transitions.
                    // Replace #121212 with your actual SurfaceDark hex color from your theme.
//                        setBackgroundColor("#121212".toColorInt()) // Example: Dark Gray

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        allowFileAccess = false
                        javaScriptCanOpenWindowsAutomatically = false
                        mediaPlaybackRequiresUserGesture = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f), // WebView takes up remaining space in this Column
            update = { webView ->
                if (webView.webViewClient != currentAppWebViewClient) {
                    webView.webViewClient = currentAppWebViewClient
                }
                if (webView.webChromeClient != appWebChromeClient) {
                    webView.webChromeClient = appWebChromeClient
                }

                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "AndroidView Update. Action: $internalWebViewAction, URL: $url, WebView URL: ${webView.url}"
                )

                val actionToExecute = internalWebViewAction
                if (actionToExecute != WebViewInternalAction.NONE) {
                    internalWebViewAction = WebViewInternalAction.NONE
                }

                when (actionToExecute) {
                    WebViewInternalAction.CLEAR_ONLY -> {
                        if (webView.url != "about:blank") {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Update: CLEAR_ONLY -> Loading about:blank."
                            )
                            webView.loadUrl("about:blank")
                        }
                    }

                    WebViewInternalAction.LOAD_URL, WebViewInternalAction.CLEAR_THEN_LOAD_URL -> {
                        val targetUrlToLoad = if (MimeTypeMap.getFileExtensionFromUrl(url)
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

                        if (actionToExecute == WebViewInternalAction.CLEAR_THEN_LOAD_URL && webView.url != "about:blank") {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Update: CLEAR_THEN_LOAD_URL -> Clearing before loading $targetUrlToLoad."
                            )
                            webView.loadUrl("about:blank")
                        }

                        if (webView.url != targetUrlToLoad || (webView.url == "about:blank" && targetUrlToLoad != "about:blank")) {
                            Log.d(WEB_VIEW_AREA_TAG, "Update: Loading target $targetUrlToLoad.")
                            webView.loadUrl(targetUrlToLoad)
                        } else {
                            Log.d(
                                WEB_VIEW_AREA_TAG,
                                "Update: WebView already on target $targetUrlToLoad. No action from signal."
                            )
                        }
                    }

                    WebViewInternalAction.NONE -> {
                        if (webView.url == "about:blank") {
                            val targetUrlToLoad = if (MimeTypeMap.getFileExtensionFromUrl(url)
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
                            if (targetUrlToLoad != "about:blank") {
                                Log.w(
                                    WEB_VIEW_AREA_TAG,
                                    "Update (NONE action): WebView is blank but target is $targetUrlToLoad. Forcing load."
                                )
                                webView.loadUrl(targetUrlToLoad)
                            }
                        }
                    }
                }
            }
        )
    }

    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onCloseArea() // Use the callback to signal closure to the parent
        }
    }
}
