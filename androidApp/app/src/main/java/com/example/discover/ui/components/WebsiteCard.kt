package com.example.discover.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.discover.data.Link
import com.example.discover.ui.theme.*

@Composable
fun WebsiteCard(
    website: Link, onLikeClick: () -> Unit, onDislikeClick: () -> Unit, onWebsiteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Website name
            Text(
                text = website.name,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = website.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 14.sp
            )

            // Tags
            Text(
                text = "Tags: " + website.tags.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // URL

            SelectionContainer {
                Text(
                    text = website.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                        .clickable(
                            onClick = onWebsiteClick // Use the passed-in onWebsiteClick lambda
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dislikes button
                Button(
                    onClick = onDislikeClick, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, contentColor = TextPrimary
                    ), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Text("👎 ${website.dislikesMobile}")
                }

                // Views count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${website.views + 1}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "views",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Likes button
                Button(
                    onClick = onLikeClick, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, contentColor = TextPrimary
                    ), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Text("👍 ${website.likesMobile}")
                }
            }
        }
    }
} 