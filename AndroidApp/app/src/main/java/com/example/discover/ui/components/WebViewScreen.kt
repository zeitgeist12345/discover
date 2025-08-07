package com.example.discover.ui.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.*
import kotlinx.coroutines.launch

// For remote debugging WebView in Chrome: chrome://inspect
// In your Application class or an early initialization point:
// if (BuildConfig.DEBUG) {
//     WebView.setWebContentsDebuggingEnabled(true)
// }

private const val WEB_VIEW_SCREEN_TAG = "WebViewScreen"

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
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableStateOf(0) } // For progress bar
    var pageTitle by remember { mutableStateOf<String?>(null) } // To store page title

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Custom WebViewClient
    val appWebViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webProgress = 0 // Reset progress and show indicator
                Log.d(WEB_VIEW_SCREEN_TAG, "Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webProgress = 100 // Hide progress indicator
                pageTitle = view?.title
                Log.d(WEB_VIEW_SCREEN_TAG, "Page finished loading: $url. Title: ${view?.title}")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    val errorCode = error?.errorCode
                    val description = error?.description
                    Log.e(
                        WEB_VIEW_SCREEN_TAG,
                        "Error loading page: ${request.url}, Code: $errorCode, Desc: $description"
                    )
                    // Optionally, you could show an error message in the UI here
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val requestedUrl = request?.url?.toString()
                Log.d(WEB_VIEW_SCREEN_TAG, "URL Clicked: $requestedUrl")
                // Load all URLs within this WebView.
                // You could add logic here to open certain URLs in an external browser
                // e.g., if (!requestedUrl.startsWith("http")) { Intent(Intent.ACTION_VIEW)...; return true }
                return false // Return false to load the URL in the current WebView
            }
        }
    }

    // Custom WebChromeClient
    val appWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                webProgress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                pageTitle = title
                Log.d(WEB_VIEW_SCREEN_TAG, "Received page title: $title")
            }

            // Handle JavaScript alerts, confirms, prompts for better integration
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                // No coroutineScope.launch needed here as AlertDialog.Builder.show()
                // will run on the main thread if called from there (which WebChromeClient callbacks are)
                // However, to prevent UI hangs if there's any complex logic before showing the dialog,
                // or if you want to ensure it's explicitly on the main thread for dialogs,
                // you can use:
                // view?.post { /* dialog code here */ }
                // For simplicity, direct call is often fine for these callbacks.

                AlertDialog.Builder(context) // Use android.app.AlertDialog.Builder
                    .setTitle("Alert")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        result?.confirm()
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        result?.cancel()
                        it.dismiss()
                    }
                    .setCancelable(false) // Or true, depending on desired behavior
                    .show()
                return true // We handled it
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                AlertDialog.Builder(context) // Use android.app.AlertDialog.Builder
                    .setTitle("Confirm")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        result?.confirm()
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        result?.cancel()
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        result?.cancel()
                        it.dismiss()
                    }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: android.webkit.JsPromptResult?
            ): Boolean {
                val editText = android.widget.EditText(context)
                editText.setText(defaultValue)

                AlertDialog.Builder(context) // Use android.app.AlertDialog.Builder
                    .setTitle("Prompt")
                    .setMessage(message)
                    .setView(editText) // Add EditText for user input
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        result?.confirm(editText.text.toString())
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        result?.cancel()
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        result?.cancel()
                        it.dismiss()
                    }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                Log.d(WEB_VIEW_SCREEN_TAG, "Permission request for: ${request?.origin}, Resources: ${request?.resources?.joinToString()}")
                // For simplicity, denying. In a real app, you'd request Android runtime permissions
                // and then call request.grant(request.resources) or request.deny().
                // Example:
                // if (request?.resources?.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) == true) {
                //   // Request camera permission
                // }
                request?.deny()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            shadowElevation = 4.dp // Add some shadow to distinguish header
        ) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = statusBarPadding.calculateTopPadding() + Spacing.small,
                        start = Spacing.medium,
                        end = Spacing.medium,
                        bottom = Spacing.small
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 Discover", // You could use pageTitle here if desired
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onDiscoverClick)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDislikeClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dislike",
                            tint = if (isDisliked) ErrorColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Like",
                            tint = if (isLiked) SuccessColor else TextSecondary,
                            modifier = Modifier.size(Spacing.large)
                        )
                    }
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Close")
                }
            }
        }

        // Progress Bar
        if (webProgress < 100) { // Show only while loading
            LinearProgressIndicator(
                progress = { webProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen, // Customize color
                trackColor = SurfaceDark.copy(alpha = 0.5f)
            )
        }

        // WebView
        AndroidView(
            factory = { factoryContext ->
                WebView(factoryContext).apply {
                    webViewRef = this // Store reference

                    // Apply WebViewClient and WebChromeClient
                    webViewClient = appWebViewClient
                    webChromeClient = appWebChromeClient

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true // For localStorage/sessionStorage
                        loadWithOverviewMode = true // Zoom out to show the whole page
                        useWideViewPort = true // Makes the WebView viewport like a desktop browser
                        setSupportZoom(true) // Allow pinch-to-zoom
                        builtInZoomControls = true // Allow zoom controls
                        displayZoomControls = false // Hide the on-screen +/- buttons (pinch zoom still works)

                        // Other potentially useful settings:
                        allowFileAccess = true // Allow access to file system (use with caution)
                        javaScriptCanOpenWindowsAutomatically = false // Prevent pop-ups unless through user gesture
                        mediaPlaybackRequiresUserGesture = true // Prevent autoplay of media
                        // userAgentString = "YourCustomUserAgent/1.0" // If you need to customize
                    }
                    // Initial load is handled in update block
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f), // Ensure WebView takes remaining space
            update = { webView ->
                // Check if the URL is different to prevent unnecessary reloads
                // (e.g., on configuration change if not handled by remember)
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )
    }
}
