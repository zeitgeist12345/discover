/* Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

package com.example.discover.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.discover.ui.theme.ErrorColor
import com.example.discover.ui.theme.Spacing
import com.example.discover.ui.theme.SuccessColor
import com.example.discover.ui.theme.SurfaceDark
import com.example.discover.ui.theme.TextPrimary
import com.example.discover.ui.theme.TextSecondary

@Composable
fun TopDiscoverBar(
    isLiked: Boolean,
    isDisliked: Boolean,
    onDiscoverClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(), color = SurfaceDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding() + Spacing.small,
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.small
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🌏 Discover",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDiscoverClick)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                IconButton(onClick = onDislikeClick) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Dislikes",
                        tint = if (isDisliked) ErrorColor else TextSecondary,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
                IconButton(onClick = onLikeClick) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Likes",
                        tint = if (isLiked) SuccessColor else TextSecondary,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
                IconButton(onClick = onOpenInBrowser) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        "Open in browser",
                        tint = TextSecondary,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = ErrorColor,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
            }
        }
    }
}
