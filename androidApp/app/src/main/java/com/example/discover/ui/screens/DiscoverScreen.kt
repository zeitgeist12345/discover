/* Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

package com.example.discover.ui.screens

// Import your new components
import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discover.ui.components.AddLinkDialog
import com.example.discover.ui.components.ControlButtons
import com.example.discover.ui.components.TopDiscoverBar
import com.example.discover.ui.components.WebViewArea
import com.example.discover.ui.components.LinkCard
import com.example.discover.ui.theme.BackgroundDark
import com.example.discover.ui.theme.Spacing
import com.example.discover.ui.theme.TextPrimary
import com.example.discover.ui.theme.TextSecondary
import com.example.discover.viewmodel.DiscoverViewModel
import com.example.discover.viewmodel.UserInteractionState

@Composable
fun formatTime(ms: Long): String {
    return if (ms < 3600000) "${ms / 60000}m"
    else "${ms / 3600000}h ${(ms % 3600000) / 60000}m"
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel
) {
    val currentLink by viewModel.currentLink.collectAsStateWithLifecycle()
    val showAddLinkDialog by viewModel.showAddLinkDialog.collectAsStateWithLifecycle()
    val showWebView by viewModel.showWebView.collectAsStateWithLifecycle()
    val initialWebViewUrl by viewModel.currentWebViewUrl.collectAsStateWithLifecycle()
    val userInteractionState by viewModel.currentUserInteractionState.collectAsStateWithLifecycle()
    var liveWebViewUrl by remember { mutableStateOf(initialWebViewUrl) }
    LaunchedEffect(initialWebViewUrl) {
        liveWebViewUrl = initialWebViewUrl
    }
    val isApiAvailable by viewModel.isApiAvailable.collectAsStateWithLifecycle()
    val timeStats by viewModel.timeStats.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dailyTime = formatTime(timeStats.daily)
    val yesterdayTime = formatTime(timeStats.yesterday)
    val weeklyTime = formatTime(timeStats.weekly)
    val monthlyTime = formatTime(timeStats.monthly)
    val yearlyTime = formatTime(timeStats.yearly)
    val totalTime = formatTime(timeStats.total)

    val webView = remember {
        WebView(context).apply {
            // Apply all settings here. They will persist for the lifetime of the WebView.
            settings.apply {
//                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setBackgroundColor(Color.Transparent.toArgb())
                visibility = View.GONE
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

                // Load static HTML with CURRENT stats
                val initialHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width">
                <style>
                    body {
                        padding: 20px;
                        background: #0F0F0F;
                    }
                    .container {
                        text-align: center;
                    }
                    .stats-container {
                        background: #323232;
                        border-radius: 1rem;
                        padding: 1rem;
                    }
                    .stat-item {
                        display: flex;
                        justify-content: space-between;
                        margin: 0.5rem 0.5rem 0 0;
                        color: white;
                    }
                    .stat-value {
                        font-weight: bold;
                    }
                    h1 {
                        font-size: 1.2rem;
                        opacity: 0.8;
                        margin-bottom: 1rem;
                        color: white;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Press 🌏 Discover to explore amazing links!</h1>
                    
                    <div class="stats-container" id="statsContainer">
                        <div class="stat-item">
                            <span>Today:</span>
                            <span class="stat-value" id="dailyStat">${dailyTime}</span>
                        </div>
                        <div class="stat-item">
                            <span>Yesterday:</span>
                            <span class="stat-value" id="yesterdayStat">${yesterdayTime}</span>
                        </div>
                        <div class="stat-item">
                            <span>This Week:</span>
                            <span class="stat-value" id="weeklyStat">${weeklyTime}</span>
                        </div>
                        <div class="stat-item">
                            <span>This Month:</span>
                            <span class="stat-value" id="monthlyStat">${monthlyTime}</span>
                        </div>
                        <div class="stat-item">
                            <span>This Year:</span>
                            <span class="stat-value" id="yearlyStat">${yearlyTime}</span>
                        </div>
                        <div class="stat-item">
                            <span>All Time:</span>
                            <span class="stat-value" id="totalStat">${totalTime}</span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

                loadDataWithBaseURL(
                    null,
                    initialHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.toastMessageShown()
        }
    }

    // This Column will be the root for either the main content or the WebView structure
    Column(modifier = Modifier.fillMaxSize()) {
        if (showWebView) {
            // When showing WebView, display the TopDiscoverBar and then the WebViewArea
            TopDiscoverBar(
                isLiked = userInteractionState == UserInteractionState.LIKED,
                isDisliked = userInteractionState == UserInteractionState.DISLIKED,
                // Assuming onDiscoverClick in the context of the WebView means loading a new random site
                onDiscoverClick = { viewModel.loadRandomLink() },
                onDislikeClick = { viewModel.dislikeLink() },
                onLikeClick = { viewModel.likeLink() },
                onOpenInBrowser = {
                    liveWebViewUrl?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    }
                },
                onClose = { viewModel.closeWebView() } // This closes the WebView view
            )

            WebViewArea(
                viewModel = viewModel,
                webView = webView,
                url = initialWebViewUrl,
                onUrlChanged = { newUrl ->
                    liveWebViewUrl = newUrl
                },
                onCloseArea = { viewModel.closeWebView() },
                onWebViewHistoryBack = { viewModel.updateNavigatedPreviousLink() } // BackHandler in WebViewArea will call this
            )
        } else {
            // Main content (when WebView is not shown)
            // This Box will now be inside the root Column
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fill the space within the parent Column
                    .background(BackgroundDark)
            ) {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

                // --- ADDITION: Status Indicator Light ---
                var indicatorColor = Color.Yellow
                if (isApiAvailable == 1) {
                    indicatorColor = Color.Green
                } else if (isApiAvailable == -1) {
                    indicatorColor = Color.Red
                }

                Box(
                    modifier = Modifier
                        // CORRECT: .align() is now a child of the parent Box scope
                        .align(Alignment.TopEnd) // Changed to TopEnd to match the original likely intent
                        .padding(
                            top = statusBarPadding.calculateTopPadding() + Spacing.small,
                            end = Spacing.medium
                        ) // Add padding for positioning
                        .size(12.dp)
                        .background(color = indicatorColor, shape = CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            // Adjust padding as needed, statusBarPadding is now applied by TopDiscoverBar for its content
                            // For this main screen, we might want different padding or rely on the Box background.
                            top = statusBarPadding.calculateTopPadding() + Spacing.medium, // Or remove if Box background is enough
                            start = Spacing.medium, end = Spacing.medium,
                            // bottom padding might need to account for navigation bar if not handled globally
                            bottom = Spacing.medium + WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Text (This is the main screen's header, distinct from TopDiscoverBar)
                    Text(
                        text = "🌏 Discover",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "Discover amazing links from around the world!\nNo more personalized recommendation algorithms!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))

                    ControlButtons(
                        onPreviousClick = { viewModel.loadPreviousLink() },
                        onRandomClick = { viewModel.loadRandomLink() },
                        onNextClick = { viewModel.loadNextLink() },
                        onAddLinkClick = { viewModel.showAddLinkDialog() })

                    // Show current link card only if there is a current link and WebView is not shown
                    currentLink?.let {
                        LinkCard(
                            link = it,
                            onLikeClick = { viewModel.likeLink() },
                            onDislikeClick = { viewModel.dislikeLink() },
                            onLinkClick = { viewModel.openLink() } // This will set showWebView = true
                        )
                    }
                }

                // Add link dialog - positioned within the Box to overlay content
                if (showAddLinkDialog) {
                    AddLinkDialog(
                        onDismiss = { viewModel.hideAddLinkDialog() },
                        onAddLink = { name, url, description, tags ->
                            viewModel.addLink(name, url, description, tags)
                        })
                }
            }
        }
    }
}
