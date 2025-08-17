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
import androidx.activity.compose.BackHandler // Import BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.discover.ui.theme.ErrorColor
import com.example.discover.ui.theme.PrimaryGreen
import com.example.discover.ui.theme.Spacing
import com.example.discover.ui.theme.SuccessColor
import com.example.discover.ui.theme.SurfaceDark
import com.example.discover.ui.theme.TextPrimary
import com.example.discover.ui.theme.TextSecondary

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
    var webProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current
    // rememberCoroutineScope() // Not strictly needed here unless used by other async tasks

    // Hold a reference to the WebView instance
    var webViewInstance: WebView? by remember { mutableStateOf(null) }

    val appWebViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webProgress = 0
                Log.d(WEB_VIEW_SCREEN_TAG, "Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webProgress = 100
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
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val requestedUrl = request?.url?.toString()
                Log.d(WEB_VIEW_SCREEN_TAG, "URL Clicked: $requestedUrl")
                return false
            }
        }
    }

    val appWebChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                webProgress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(WEB_VIEW_SCREEN_TAG, "Received page title: $title")
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                AlertDialog.Builder(context)
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
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                AlertDialog.Builder(context)
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

                AlertDialog.Builder(context)
                    .setTitle("Prompt")
                    .setMessage(message)
                    .setView(editText)
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
                request?.deny()
            }
        }
    }

    // Handle system back button press
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose() // If WebView can't go back, call the onClose lambda
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            shadowElevation = 4.dp
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
                    text = "🌐 Discover",
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
                    onClick = onClose, // This button now behaves consistently with back press if WebView is at start
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Close")
                }
            }
        }

        if (webProgress < 100) {
            LinearProgressIndicator(
                progress = { webProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = SurfaceDark.copy(alpha = 0.5f)
            )
        }

        AndroidView(
            factory = { factoryContext ->
                WebView(factoryContext).apply {
                    webViewInstance = this // Store the WebView instance

                    webViewClient = appWebViewClient
                    webChromeClient = appWebChromeClient

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        allowFileAccess = true
                        javaScriptCanOpenWindowsAutomatically = false
                        mediaPlaybackRequiresUserGesture = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                    // It's also a good idea to clear history when loading a completely new URL
                    // if the previous history isn't relevant to the new content.
                    // webView.clearHistory()
                }
            }
        )
    }
}
