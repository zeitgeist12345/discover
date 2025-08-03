package com.example.discover.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discover.ui.components.*
import com.example.discover.ui.theme.*
import com.example.discover.viewmodel.DiscoverViewModel
import com.example.discover.viewmodel.UserInteractionState // Import the enum

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel
) {
    val websites by viewModel.websites.collectAsStateWithLifecycle()
    val currentWebsite by viewModel.currentWebsite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val showAddWebsiteDialog by viewModel.showAddWebsiteDialog.collectAsStateWithLifecycle()
    val showWebView by viewModel.showWebView.collectAsStateWithLifecycle()
    val currentWebViewUrl by viewModel.currentWebViewUrl.collectAsStateWithLifecycle()
    // Collect the user interaction state
    val userInteractionState by viewModel.currentUserInteractionState.collectAsStateWithLifecycle()

    // Handle WebView display
    if (showWebView && currentWebViewUrl != null) {
        WebViewScreen(
            url = currentWebViewUrl!!,
            // Pass the derived boolean states
            isLiked = userInteractionState == UserInteractionState.LIKED,
            isDisliked = userInteractionState == UserInteractionState.DISLIKED,
            onDiscoverClick = { viewModel.loadRandomWebsite() }, // Keep if you want this button in WebView header
            onDislikeClick = { viewModel.dislikeWebsite() },
            onLikeClick = { viewModel.likeWebsite() },
            onClose = { viewModel.closeWebView() }
        )
        return // Return here to only show WebView when it's active
    }

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Get status bar padding
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = statusBarPadding.calculateTopPadding() + Spacing.medium,
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = statusBarPadding.calculateTopPadding() + Spacing.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "🌐 Discover",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Discover amazing links from around the web!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No more personalized recommendation algorithms!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Websites open automatically in the in-app browser",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            // Background update indicator
            if (isUpdating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.small),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Updating websites...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }
            }

            // Loading state (only for initial load)
            // Consider checking if websites.isEmpty() as well for a true "initial" loading state
            if (isLoading && websites.isEmpty()) { // Added websites.isEmpty() for better initial load indication
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Text(
                        text = "Loading amazing links...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Error state
            else if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorColor.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ℹ️ Info",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = error!!,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.loadWebsites() // Or a more specific refresh action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreenDark,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }
            // Content state
            else if (currentWebsite != null) {
                // Control buttons
                ControlButtons(
                    onPreviousClick = { viewModel.loadPreviousWebsite() },
                    onRandomClick = { viewModel.loadRandomWebsite() },
                    onNextClick = { viewModel.loadNextWebsite() },
                    onAddWebsiteClick = { viewModel.showAddWebsiteDialog() }
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Current website card
                WebsiteCard(
                    website = currentWebsite!!,
                    onLikeClick = { viewModel.likeWebsite() },
                    onDislikeClick = { viewModel.dislikeWebsite() },
                    onWebsiteClick = { viewModel.openWebsite() } // This will trigger the WebView
                )
            }
            // Empty state (no websites and not loading and no error)
            else if (websites.isEmpty() && !isLoading && error == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No websites available",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Button(
                        onClick = { viewModel.loadWebsites() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreenDark,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Load Websites")
                    }
                }
            }
        }

        // Add website dialog
        if (showAddWebsiteDialog) {
            AddWebsiteDialog(
                onDismiss = { viewModel.hideAddWebsiteDialog() },
                onAddWebsite = { name, url, description ->
                    viewModel.addWebsite(name, url, description)
                }
            )
        }
    }
}
