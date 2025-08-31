package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebView
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
                            var targetUrlToLoad = url
                            if (MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() == "pdf") {
                                targetUrlToLoad =
                                    "https://docs.google.com/gview?embedded=true&url=${
                                        android.net.Uri.encode(
                                            url
                                        )
                                    }"
                            }
                            Log.w(
                                WEB_VIEW_AREA_TAG,
                                "Update (NONE action): WebView is blank but target is $targetUrlToLoad. Forcing load."
                            )
                            webView.loadUrl(targetUrlToLoad)
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
