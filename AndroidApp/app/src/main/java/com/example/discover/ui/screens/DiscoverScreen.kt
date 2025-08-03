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
    
    // Handle WebView display
    if (showWebView && currentWebViewUrl != null) {
        WebViewScreen(
            url = currentWebViewUrl!!,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "🌐 Discover",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Background update indicator
            if (isUpdating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Updating websites...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Loading state (only for initial load)
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ℹ️ Info",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                viewModel.clearError()
                                viewModel.loadWebsites()
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current website card
                WebsiteCard(
                    website = currentWebsite!!,
                    onLikeClick = { viewModel.likeWebsite() },
                    onDislikeClick = { viewModel.dislikeWebsite() },
                    onWebsiteClick = { viewModel.openWebsite() }
                )
            }
            // Empty state (shouldn't happen with static websites)
            else if (websites.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No websites available",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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