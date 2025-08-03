package com.example.discover.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discover.ui.theme.*

@Composable
fun ControlButtons(
    onPreviousClick: () -> Unit,
    onRandomClick: () -> Unit,
    onNextClick: () -> Unit,
    onAddWebsiteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onPreviousClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Text("⬅️ Previous")
            }
            
            Button(
                onClick = onRandomClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreenDark,
                    contentColor = TextPrimary
                )
            ) {
                Text("🎲 Random", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onNextClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Text("Next ➡️")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add website button
        Button(
            onClick = onAddWebsiteClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessColor,
                contentColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Add Website", fontWeight = FontWeight.Bold)
        }
    }
} 