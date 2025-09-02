package com.example.discover.ui.screens

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Import your new components
import com.example.discover.ui.components.AddWebsiteDialog
import com.example.discover.ui.components.ControlButtons
import com.example.discover.ui.components.TopDiscoverBar // Ensure this is imported
import com.example.discover.ui.components.WebViewArea    // Ensure this is imported
import com.example.discover.ui.components.WebsiteCard
import com.example.discover.ui.theme.BackgroundDark
import com.example.discover.ui.theme.Spacing
import com.example.discover.ui.theme.TextPrimary
import com.example.discover.ui.theme.TextSecondary
import com.example.discover.viewmodel.DiscoverViewModel
import com.example.discover.viewmodel.UserInteractionState

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel
) {
    val currentWebsite by viewModel.currentWebsite.collectAsStateWithLifecycle()
    val showAddWebsiteDialog by viewModel.showAddWebsiteDialog.collectAsStateWithLifecycle()
    val showWebView by viewModel.showWebView.collectAsStateWithLifecycle()
    val currentWebViewUrl by viewModel.currentWebViewUrl.collectAsStateWithLifecycle()
    val userInteractionState by viewModel.currentUserInteractionState.collectAsStateWithLifecycle()

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.toastMessageShown()
        }
    }

    // This Column will be the root for either the main content or the WebView structure
    Column(modifier = Modifier.fillMaxSize()) {
        if (showWebView && currentWebViewUrl != null) {
            // When showing WebView, display the TopDiscoverBar and then the WebViewArea
            TopDiscoverBar(
                isLiked = userInteractionState == UserInteractionState.LIKED,
                isDisliked = userInteractionState == UserInteractionState.DISLIKED,
                // Assuming onDiscoverClick in the context of the WebView means loading a new random site
                onDiscoverClick = { viewModel.loadRandomWebsite() },
                onDislikeClick = { viewModel.dislikeWebsite() },
                onLikeClick = { viewModel.likeWebsite() },
                onClose = { viewModel.closeWebView() } // This closes the WebView view
            )
            WebViewArea(
                url = currentWebViewUrl!!,
                onCloseArea = { viewModel.closeWebView() },
                onWebViewHistoryBack = { viewModel.updateNavigatedPreviousWebsite() } // BackHandler in WebViewArea will call this
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
                        text = "🌐 Discover",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "Discover amazing links from around the web!\nNo more personalized recommendation algorithms!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))

                    ControlButtons(
                        onPreviousClick = { viewModel.loadPreviousWebsite() },
                        onRandomClick = { viewModel.loadRandomWebsite() },
                        onNextClick = { viewModel.loadNextWebsite() },
                        onAddWebsiteClick = { viewModel.showAddWebsiteDialog() })

                    // Show current website card only if there is a current website and WebView is not shown
                    currentWebsite?.let {
                        WebsiteCard(
                            website = it,
                            onLikeClick = { viewModel.likeWebsite() },
                            onDislikeClick = { viewModel.dislikeWebsite() },
                            onWebsiteClick = { viewModel.openWebsite() } // This will set showWebView = true
                        )
                    }
                }

                // Add website dialog - positioned within the Box to overlay content
                if (showAddWebsiteDialog) {
                    AddWebsiteDialog(
                        onDismiss = { viewModel.hideAddWebsiteDialog() },
                        onAddWebsite = { name, url, description ->
                            viewModel.addWebsite(name, url, description)
                        })
                }
            }
        }
    }
}
