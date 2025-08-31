package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap // Required for WebViewClient
import android.net.Uri // Required for Uri parsing
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient // Required for onProgressChanged
import android.webkit.WebResourceRequest // Required for shouldOverrideUrlLoading
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
import kotlin.text.endsWith // Ensure this is imported for .endsWith
import kotlin.text.lowercase

private const val WEB_VIEW_AREA_TAG = "WebViewArea"

enum class WebViewInternalAction {
    NONE, LOAD_URL, CLEAR_THEN_LOAD_URL, CLEAR_ONLY
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewArea(
    url: String, // The target URL from ViewModel
    onCloseArea: () -> Unit
) {
    var webViewProgress by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var webViewInstanceFromFactory: WebView? by remember { mutableStateOf(null) }
    var internalWebViewAction by remember(url) { mutableStateOf(WebViewInternalAction.NONE) }
    var pendingUrlAfterBlank by remember { mutableStateOf<String?>(null) }

    val localWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (view?.url != null && view.url != "about:blank") {
                    webViewProgress = newProgress
                } else if (view?.url == "about:blank") {
                    webViewProgress = 0
                }
            }
        }
    }

    val localWebViewClient = remember(url) {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, currentUrl: String?, favicon: Bitmap?) {
                super.onPageStarted(view, currentUrl, favicon)
                Log.d(WEB_VIEW_AREA_TAG, "Client: onPageStarted: $currentUrl, pending: $pendingUrlAfterBlank")
                if (currentUrl != null && currentUrl != "about:blank") {
                    webViewProgress = 0
                }
            }

            override fun onPageFinished(view: WebView?, currentFinishedUrl: String?) {
                super.onPageFinished(view, currentFinishedUrl)
                Log.d(WEB_VIEW_AREA_TAG, "Client: onPageFinished: $currentFinishedUrl, pending: $pendingUrlAfterBlank")
                if (currentFinishedUrl == "about:blank" && pendingUrlAfterBlank != null) {
                    val urlToLoad = pendingUrlAfterBlank
                    pendingUrlAfterBlank = null
                    Log.i(WEB_VIEW_AREA_TAG, "Client: Finished 'about:blank'. Loading pending: $urlToLoad")
                    view?.loadUrl(urlToLoad!!)
                } else if (currentFinishedUrl != null && currentFinishedUrl != "about:blank") {
                    webViewProgress = 100
                }
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val requestedFullUrlString = request?.url?.toString() ?: return false
                val requestedUri: Uri = request.url

                // The 'url' parameter of the WebViewArea Composable is the initial URL.
                val initialUrlProp: String = url // Capture the Composable's url prop

                Log.i(WEB_VIEW_AREA_TAG, "shouldOverrideUrlLoading: Requested: '$requestedFullUrlString', Initial Prop URL: '$initialUrlProp', Scheme: '${requestedUri.scheme}', Host: '${requestedUri.host}'")

                val scheme = requestedUri.scheme?.lowercase()
                val host = requestedUri.host?.lowercase()

                // 1. Handle YouTube explicitly
                if ((scheme == "youtube" || scheme == "vnd.youtube") ||
                    ((scheme == "http" || scheme == "https") &&
                            (host != null && (host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtu.be")))
                ) {
                    Log.i(WEB_VIEW_AREA_TAG, "YouTube link pattern detected: $requestedFullUrlString. Attempting to open externally.")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, requestedUri))
                        view?.stopLoading()
                        return true
                    } catch (e: ActivityNotFoundException) {
                        Log.e(WEB_VIEW_AREA_TAG, "YouTube app/handler not found for '$requestedFullUrlString'. Allowing WebView if http/s.", e)
                        return !(scheme == "http" || scheme == "https")
                    } catch (e: Exception) {
                        Log.e(WEB_VIEW_AREA_TAG, "Error opening YouTube link '$requestedFullUrlString'. Allowing WebView if http/s.", e)
                        return !(scheme == "http" || scheme == "https")
                    }
                }

                // 2. Handle non-http/https schemes (generic external attempt)
                if (scheme != "http" && scheme != "https") {
                    Log.i(WEB_VIEW_AREA_TAG, "Non-http(s) scheme: '$requestedFullUrlString'. Attempting generic external app.")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, requestedUri))
                        view?.stopLoading()
                        return true
                    } catch (e: ActivityNotFoundException) {
                        Log.e(WEB_VIEW_AREA_TAG, "No app for scheme '$scheme' for: '$requestedFullUrlString'", e)
                        Toast.makeText(context, "No app found to open this link.", Toast.LENGTH_SHORT).show()
                        return true
                    } catch (e: Exception) {
                        Log.e(WEB_VIEW_AREA_TAG, "Error opening '$requestedFullUrlString' with generic scheme.", e)
                        return true
                    }
                }

                // 3. Handle http/https links: Check if navigating to a different host
                val initialHost = try { initialUrlProp.toUri().host?.lowercase()?.replace("www.","") } catch (e: Exception) { e.printStackTrace(); null }
                val requestedHost = host?.replace("www.","")

                if (initialHost != null && requestedHost != null && initialHost != requestedHost) {
                    Log.i(WEB_VIEW_AREA_TAG, "Different host navigation: Initial: '$initialHost', Requested: '$requestedHost' for URL '$requestedFullUrlString'. Opening externally.")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, requestedUri))
                        view?.stopLoading()
                        return true // Handled: opened in external browser
                    } catch (e: ActivityNotFoundException) {
                        Log.e(WEB_VIEW_AREA_TAG, "No browser found for $requestedFullUrlString (should be rare). Allowing WebView.", e)
                        return false // Should be very rare, but let WebView try
                    } catch (e: Exception) {
                        Log.e(WEB_VIEW_AREA_TAG, "Error opening different host link $requestedFullUrlString externally. Allowing WebView.", e)
                        return false // Fallback to WebView
                    }
                } else {
                    // Same host or initialHost couldn't be parsed (e.g. initial 'url' was not a valid URI for host extraction)
                    Log.d(WEB_VIEW_AREA_TAG, "Same host or unable to compare hosts. Letting WebView handle: $requestedFullUrlString")
                    return false // Let WebView handle (navigation within the same site)
                }

                // Fallback, should ideally not be reached if logic above is complete
            }


        }
    }

    LaunchedEffect(url) {
        Log.d(WEB_VIEW_AREA_TAG, "LaunchedEffect (URL prop changed): $url")
        val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"
        // No specific YouTube check here needed for setting initial action,
        // shouldOverrideUrlLoading will handle YouTube URLs when WebView attempts to load them.

        pendingUrlAfterBlank = null

        if (isPdf) {
            internalWebViewAction = WebViewInternalAction.CLEAR_ONLY
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Log.i(WEB_VIEW_AREA_TAG, "Launched PDF intent for: $url")
            } catch (e: ActivityNotFoundException) {
                Log.e(WEB_VIEW_AREA_TAG, "No app for PDF: $url. Falling back to Google Docs.", e)
                Toast.makeText(context, "No app for PDF. Trying Google Docs.", Toast.LENGTH_LONG).show()
                internalWebViewAction = WebViewInternalAction.CLEAR_THEN_LOAD_URL // For Google Docs
            }
        } else {
            // For all non-PDFs (including YouTube, regular sites), default to clear then load.
            // shouldOverrideUrlLoading will intercept if it's YouTube and try to launch app.
            // If YouTube app launch fails, WebView will proceed with CLEAR_THEN_LOAD_URL.
            val currentActualUrl = webViewInstanceFromFactory?.url
            val targetForGDocs = if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}" else url

            if (currentActualUrl == "about:blank" || currentActualUrl == null || currentActualUrl == "" || (currentActualUrl != targetForGDocs && currentActualUrl != url)) {
                Log.d(WEB_VIEW_AREA_TAG, "LaunchedEffect: Setting CLEAR_THEN_LOAD_URL for $url")
                internalWebViewAction = WebViewInternalAction.CLEAR_THEN_LOAD_URL
            } else {
                Log.d(WEB_VIEW_AREA_TAG, "LaunchedEffect: WebView already on $url or equivalent. Setting action to NONE.")
                internalWebViewAction = WebViewInternalAction.NONE
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, url) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(WEB_VIEW_AREA_TAG, "ON_RESUME for URL prop: $url. Current WebView URL: ${webViewInstanceFromFactory?.url}")
                if (webViewInstanceFromFactory != null && pendingUrlAfterBlank == null) {
                    val currentWebViewActualUrl = webViewInstanceFromFactory?.url
                    var targetUrlForDisplayInWebView = url
                    val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"
                    val isYouTube = url.contains("youtu.be") || url.contains("youtube.com")

                    // Determine if Google Docs fallback is implicitly the target
                    var isGDocsFallback = false
                    if (isPdf) {
                        try {
                            // Check if an app can handle the PDF URI directly
                            val pdfIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                            val resolveInfo = context.packageManager.queryIntentActivities(pdfIntent, 0)
                            if (resolveInfo.isEmpty()) { // No direct handler, assume GDocs
                                isGDocsFallback = true
                            }
                        } catch (e: Exception) { // Problem with URI, unlikely for valid PDF URL
                            e.printStackTrace()
                            isGDocsFallback = true // Assume GDocs if URI check fails
                        }
                        if (isGDocsFallback) {
                            targetUrlForDisplayInWebView = "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                        }
                    }

                    // If it's a YouTube link or a PDF that is *not* a GDocs fallback (meaning it should be external), don't try to reload from onResume.
                    if (isYouTube || (isPdf && !isGDocsFallback)) {
                        Log.d(WEB_VIEW_AREA_TAG, "ON_RESUME: URL ($url) is likely external type. No auto-reload from onResume.")
                    }
                    else if (currentWebViewActualUrl != targetUrlForDisplayInWebView && targetUrlForDisplayInWebView != "about:blank") {
                        Log.i(WEB_VIEW_AREA_TAG, "ON_RESUME: Content ($currentWebViewActualUrl) differs from GDocs/target ($targetUrlForDisplayInWebView). Signaling LOAD_URL.")
                        // internalWebViewAction = WebViewInternalAction.LOAD_URL // Be cautious with this
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        if (webViewProgress < 100 && webViewProgress > 0 &&
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
                    webViewInstanceFromFactory = this
                    this.webViewClient = localWebViewClient
                    this.webChromeClient = localWebChromeClient
                    settings.apply {
                        javaScriptEnabled = true; domStorageEnabled = true; loadWithOverviewMode = true
                        useWideViewPort = true; setSupportZoom(true); builtInZoomControls = true
                        displayZoomControls = false; allowFileAccess = false
                        javaScriptCanOpenWindowsAutomatically = false
                        mediaPlaybackRequiresUserGesture = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize().weight(1f),
            update = { webView ->
                if (webView.webViewClient != localWebViewClient) webView.webViewClient = localWebViewClient
                if (webView.webChromeClient != localWebChromeClient) webView.webChromeClient = localWebChromeClient

                val actionToExecute = internalWebViewAction
                if (actionToExecute != WebViewInternalAction.NONE) {
                    Log.d(WEB_VIEW_AREA_TAG, "Update Block. Action: $actionToExecute, URL Prop: $url, WebView URL: ${webView.url}, Pending: $pendingUrlAfterBlank")
                    internalWebViewAction = WebViewInternalAction.NONE
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
                                "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                            } else { url }
                        if (webView.url != targetUrlToLoad || (webView.url == "about:blank" && targetUrlToLoad != "about:blank")) {
                            Log.d(WEB_VIEW_AREA_TAG, "Action LOAD_URL: Loading target '$targetUrlToLoad'.")
                            pendingUrlAfterBlank = null
                            webView.loadUrl(targetUrlToLoad)
                        }
                    }
                    WebViewInternalAction.CLEAR_THEN_LOAD_URL -> {
                        val targetUrlToLoad =
                            if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                            } else { url }

                        if (webView.url == targetUrlToLoad && pendingUrlAfterBlank == null) {
                            Log.d(WEB_VIEW_AREA_TAG, "Action CLEAR_THEN_LOAD_URL: Already on target '$targetUrlToLoad'.")
                            return@AndroidView
                        }
                        if (pendingUrlAfterBlank == targetUrlToLoad && webView.url == "about:blank") {
                            Log.d(WEB_VIEW_AREA_TAG, "Action CLEAR_THEN_LOAD_URL: 'about:blank' already loaded for pending '$targetUrlToLoad'.")
                            return@AndroidView
                        }
                        Log.d(WEB_VIEW_AREA_TAG, "Action CLEAR_THEN_LOAD_URL: Setting '$targetUrlToLoad' as pending and loading 'about:blank'.")
                        pendingUrlAfterBlank = targetUrlToLoad
                        if (webView.url != "about:blank") {
                            webView.loadUrl("about:blank")
                        } else {
                            Log.d(WEB_VIEW_AREA_TAG, "Action CLEAR_THEN_LOAD_URL: WebView is already 'about:blank'. Client onPageFinished should handle loading '$pendingUrlAfterBlank'.")
                        }
                    }
                    WebViewInternalAction.NONE -> {
                        if (webView.url == "about:blank" && url.isNotBlank() && pendingUrlAfterBlank == null) {
                            val targetForBlank = if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                            } else { url }
                            if (targetForBlank != "about:blank") {
                                Log.w(WEB_VIEW_AREA_TAG, "Action NONE: WebView blank, URL prop is '$url'. Forcing load of '$targetForBlank'.")
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
