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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(), color = SurfaceDark, shadowElevation = 4.dp
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
                "🌐 Discover",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDiscoverClick)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                IconButton(onClick = onDislikeClick) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Dislike",
                        tint = if (isDisliked) ErrorColor else TextSecondary,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
                IconButton(onClick = onLikeClick) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Like",
                        tint = if (isLiked) SuccessColor else TextSecondary,
                        modifier = Modifier.size(Spacing.large)
                    )
                }
            }
            Button(
                onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) { Text("X") }
        }
    }
}
