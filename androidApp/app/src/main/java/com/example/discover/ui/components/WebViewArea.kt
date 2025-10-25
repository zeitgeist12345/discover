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
import kotlin.text.endsWith // Ensure this is imported for .endsWith
import kotlin.text.lowercase

private const val WEB_VIEW_AREA_TAG = "WebViewArea"

enum class WebViewInternalAction {
    NONE, LOAD_URL, CLEAR_THEN_LOAD_URL, CLEAR_ONLY
}

@SuppressLint("SetJavaScriptEnabled", "QueryPermissionsNeeded")
@Composable
fun WebViewArea(
    url: String, // The target URL from ViewModel
    onCloseArea: () -> Unit, onWebViewHistoryBack: () -> Unit
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
                Log.d(
                    WEB_VIEW_AREA_TAG,
                    "Client: onPageStarted: $currentUrl, pending: $pendingUrlAfterBlank"
                )
                if (currentUrl != null && currentUrl != "about:blank") {
                    webViewProgress = 0
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
                    webViewProgress = 100
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val requestedFullUrlString = request?.url?.toString() ?: return false
                val requestedUriFromWebView: Uri = request.url // URI given by WebView

                val initialUrlProp: String = url

                Log.i(
                    WEB_VIEW_AREA_TAG,
                    "shouldOverrideUrlLoading: Requested: '$requestedFullUrlString', Initial Prop URL: '$initialUrlProp', Scheme: '${requestedUriFromWebView.scheme}', Host: '${requestedUriFromWebView.host}'"
                )

                val scheme = requestedUriFromWebView.scheme?.lowercase()
                val host = requestedUriFromWebView.host?.lowercase()

                // --- MODIFIED SECTION FOR INTENT URIs ---
                // 2. Handle "intent://" scheme (Android Intent URIs)
                if ("intent" == scheme) {
                    Log.i(WEB_VIEW_AREA_TAG, "Android Intent URI detected: $requestedFullUrlString")
                    try {
                        val intent =
                            Intent.parseUri(requestedFullUrlString, Intent.URI_INTENT_SCHEME)
                        // intent.addCategory(Intent.CATEGORY_BROWSABLE) // Might be needed for some intents
                        // intent.setComponent(null) // Prevent SecurityException if component is set
                        // intent.setSelector(null)  // Prevent SecurityException if selector is set

                        // Check if there's an Activity to handle this Intent
                        if (intent.resolveActivity(context.packageManager) != null) {
                            // To prevent browser hijacking, ensure it's not trying to re-launch your own app
                            // unless explicitly desired. For now, we'll assume external.
                            // You might add more safety checks if needed.
                            context.startActivity(intent)
                            Log.d(
                                WEB_VIEW_AREA_TAG, "Successfully launched intent from Intent URI."
                            )
                            view?.stopLoading()
                            return true
                        } else {
                            Log.w(
                                WEB_VIEW_AREA_TAG,
                                "No Activity found to handle parsed Intent URI: $requestedFullUrlString"
                            )
                            // Fallback: Try to extract a fallback URL if present in the intent URI (S.browser_fallback_url)
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            if (fallbackUrl != null) {
                                Log.i(
                                    WEB_VIEW_AREA_TAG,
                                    "Attempting to load fallback URL: $fallbackUrl"
                                )
                                view?.loadUrl(fallbackUrl)
                                return true // We are handling it by loading the fallback
                            }
                            Toast.makeText(context, "Cannot open this link.", Toast.LENGTH_SHORT)
                                .show()
                            return true // Handled by showing toast
                        }
                    } catch (e: Exception) { // Catches URISyntaxException and others
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "Error parsing or handling Intent URI: '$requestedFullUrlString'",
                            e
                        )
                        Toast.makeText(
                            context, "Cannot open this link (format error).", Toast.LENGTH_SHORT
                        ).show()
                        return true // "Handled" it by showing a Toast
                    }
                }
                // --- END OF MODIFIED SECTION ---

                // 3. Handle other non-http/https schemes (generic external attempt)
                if (scheme != "http" && scheme != "https") { // Already handled "intent", "youtube", "vnd.youtube"
                    Log.i(
                        WEB_VIEW_AREA_TAG,
                        "Other non-http(s) scheme: '$requestedFullUrlString'. Attempting generic external app."
                    )
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, requestedUriFromWebView))
                        view?.stopLoading()
                        return true
                    } catch (e: ActivityNotFoundException) {
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "No app for scheme '$scheme' for: '$requestedFullUrlString'",
                            e
                        )
                        Toast.makeText(
                            context, "No app found to open this link.", Toast.LENGTH_SHORT
                        ).show()
                        return true
                    } catch (e: Exception) {
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "Error opening '$requestedFullUrlString' with generic scheme.",
                            e
                        )
                        return true
                    }
                }

                // 4. Handle http/https links (includes different host check)
                // This part remains the same as your previous version.
                val initialHost = try {
                    initialUrlProp.toUri().host?.lowercase()?.replace("www.", "")
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                val requestedHost =
                    host // host is already lowercased and from requestedUriFromWebView

                if (initialHost != null && requestedHost != null && initialHost != requestedHost.replace(
                        "www.", ""
                    )
                ) {
                    Log.i(
                        WEB_VIEW_AREA_TAG,
                        "Different host navigation: Initial: '$initialHost', Requested: '$requestedHost' for URL '$requestedFullUrlString'. Opening externally."
                    )
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, requestedUriFromWebView))
                        view?.stopLoading()
                        return true
                    } catch (e: ActivityNotFoundException) {
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "No browser found for $requestedFullUrlString. Allowing WebView.",
                            e
                        )
                        return false
                    } catch (e: Exception) {
                        Log.e(
                            WEB_VIEW_AREA_TAG,
                            "Error opening different host link $requestedFullUrlString externally. Allowing WebView.",
                            e
                        )
                        return false
                    }
                } else {
                    Log.d(
                        WEB_VIEW_AREA_TAG,
                        "Same host or unable to compare hosts for http/s. Letting WebView handle: $requestedFullUrlString"
                    )
                    return false // Let WebView handle (navigation within the same site or initial load)
                }
            }
        }
    }

    // --- REVISED LaunchedEffect ---
    LaunchedEffect(url, webViewInstanceFromFactory) { // Depend on webViewInstance too
        if (webViewInstanceFromFactory == null) return@LaunchedEffect // WebView not ready

        Log.d(
            WEB_VIEW_AREA_TAG,
            "LaunchedEffect for prop '$url'. Current WebView URL: ${webViewInstanceFromFactory?.url}"
        )

        val isPdf = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf"
        pendingUrlAfterBlank = null // Ensure reset for a new URL prop

        if (isPdf) {
            // Try to launch PDF externally
            var launchedExternally = false
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Log.i(WEB_VIEW_AREA_TAG, "Launched PDF intent for: $url")
                launchedExternally = true
            } catch (e: ActivityNotFoundException) {
                Log.e(
                    WEB_VIEW_AREA_TAG, "No app for PDF: $url. Will fall back to Google Docs.", e
                )
                // Will load GDocs via CLEAR_THEN_LOAD_URL or LOAD_URL below
            }
            // If PDF launched externally, we want to clear WebView
            if (launchedExternally) {
                internalWebViewAction = WebViewInternalAction.CLEAR_ONLY
                return@LaunchedEffect // Done for this LaunchedEffect pass
            }
            // If not launched externally, it's a GDocs PDF load, treat like other initial loads.
        }

        // This is the initial load for this 'url' prop
        // Determine if this URL type might be handled externally by shouldOverrideUrlLoading
        val scheme = url.toUri().scheme?.lowercase()
        val host = url.toUri().host?.lowercase()
        val mightBeExternal =
            (scheme == "youtube" || scheme == "vnd.youtube" || scheme == "intent" || ((scheme == "http" || scheme == "https") && (host != null && (host == "youtube.com" || host.endsWith(
                ".youtube.com"
            ) || host == "youtu.be")))) || isPdf // PDFs for GDocs also benefit from clear_then_load

        if (mightBeExternal) {
            // For types that might go external, or GDocs PDF, clear first.
            // This ensures if shouldOverrideUrlLoading hands off, WebView is clean.
            // If it doesn't hand off (e.g., YouTube app not found), the pending load will occur.
            Log.d(
                WEB_VIEW_AREA_TAG,
                "LaunchedEffect: Initial load for '$url' (might be external or GDocs PDF). Setting CLEAR_THEN_LOAD_URL."
            )
            internalWebViewAction = WebViewInternalAction.CLEAR_THEN_LOAD_URL
        } else {
            // For a regular new URL from ViewModel that won't go external, load directly.
            Log.d(
                WEB_VIEW_AREA_TAG,
                "LaunchedEffect: Initial load for '$url' (regular internal). Setting LOAD_URL."
            )
            internalWebViewAction = WebViewInternalAction.LOAD_URL
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

        AndroidView(factory = { contextForFactory ->
            Log.d(WEB_VIEW_AREA_TAG, "AndroidView Factory: Creating WebView instance.")
            WebView(contextForFactory).apply {
                webViewInstanceFromFactory = this
                this.webViewClient = localWebViewClient
                this.webChromeClient = localWebChromeClient
                // --- THIS IS THE FIX ---
                // Add this line to make the WebView's background see-through
                // until the actual web page content is rendered.
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                // ---------------------
                settings.apply {
                    javaScriptEnabled = true; domStorageEnabled = true; loadWithOverviewMode = true
                    useWideViewPort = true; setSupportZoom(true); builtInZoomControls = true
                    displayZoomControls = false; allowFileAccess = false
                    javaScriptCanOpenWindowsAutomatically = false
                    mediaPlaybackRequiresUserGesture = true
                }
            }
        }, modifier = Modifier
            .fillMaxSize()
            .weight(1f), update = { webView ->
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
                    }
                }

                WebViewInternalAction.CLEAR_THEN_LOAD_URL -> {
                    val targetUrlToLoad =
                        if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                            "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
                        } else {
                            url
                        }

                    if (webView.url == targetUrlToLoad && pendingUrlAfterBlank == null) {
                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "Action CLEAR_THEN_LOAD_URL: Already on target '$targetUrlToLoad'."
                        )
                        return@AndroidView
                    }
                    if (pendingUrlAfterBlank == targetUrlToLoad && webView.url == "about:blank") {
                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "Action CLEAR_THEN_LOAD_URL: 'about:blank' already loaded for pending '$targetUrlToLoad'."
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
                        Log.d(
                            WEB_VIEW_AREA_TAG,
                            "Action CLEAR_THEN_LOAD_URL: WebView is already 'about:blank'. Client onPageFinished should handle loading '$pendingUrlAfterBlank'."
                        )
                    }
                }

                WebViewInternalAction.NONE -> {
                    if (webView.url == "about:blank" && url.isNotBlank() && pendingUrlAfterBlank == null) {
                        val targetForBlank = if (MimeTypeMap.getFileExtensionFromUrl(url)
                                ?.lowercase() == "pdf"
                        ) {
                            "https://docs.google.com/gview?embedded=true&url=${Uri.encode(url)}"
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
        })
    }

    BackHandler(enabled = true) {
        if (webViewInstanceFromFactory?.canGoBack() == true && pendingUrlAfterBlank == null) {
            Log.d(WEB_VIEW_AREA_TAG, "BackHandler: WebView can go back. Calling webView.goBack()")
            webViewInstanceFromFactory?.goBack()
            onWebViewHistoryBack()
        } else {
            Log.d(
                WEB_VIEW_AREA_TAG,
                "BackHandler: WebView cannot go back or pending op. Closing area."
            )
            onCloseArea()
        }
    }
}
