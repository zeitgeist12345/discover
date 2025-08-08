package com.example.discover.ui.screens

import android.widget.Toast // Import Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discover.ui.components.*
import com.example.discover.ui.theme.*
import com.example.discover.viewmodel.DiscoverViewModel
import com.example.discover.viewmodel.UserInteractionState

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel
) {
    val websites by viewModel.websites.collectAsStateWithLifecycle()
    val currentWebsite by viewModel.currentWebsite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUpdating by viewModel.isUpdating.collectAsStateWithLifecycle()
    val showAddWebsiteDialog by viewModel.showAddWebsiteDialog.collectAsStateWithLifecycle()
    val showWebView by viewModel.showWebView.collectAsStateWithLifecycle()
    val currentWebViewUrl by viewModel.currentWebViewUrl.collectAsStateWithLifecycle()
    val userInteractionState by viewModel.currentUserInteractionState.collectAsStateWithLifecycle()

    // Collect the toast message state
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current // Get context for Toast

    // Effect to show Toast when toastMessage changes
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.toastMessageShown() // Important: Notify ViewModel that message was shown
        }
    }

    // Handle WebView display
    if (showWebView && currentWebViewUrl != null) {
        WebViewScreen(
            url = currentWebViewUrl!!,
            isLiked = userInteractionState == UserInteractionState.LIKED,
            isDisliked = userInteractionState == UserInteractionState.DISLIKED,
            onDiscoverClick = { viewModel.loadRandomWebsite() },
            onDislikeClick = { viewModel.dislikeWebsite() },
            onLikeClick = { viewModel.likeWebsite() },
            onClose = { viewModel.closeWebView() }
        )
        return
    }

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = statusBarPadding.calculateTopPadding() + Spacing.medium,
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = statusBarPadding.calculateTopPadding() + Spacing.medium // Consider if double padding is needed
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // This centers content if it's smaller than screen
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
                        .padding(Spacing.small), // Consider if this padding is good with Column padding
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

            // Loading state
            if (isLoading && websites.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth() // Take full width to center content
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
                    onWebsiteClick = { viewModel.openWebsite() }
                )
            }
            // Empty state
            else if (websites.isEmpty()) { // Ensure this condition is mutually exclusive enough
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
