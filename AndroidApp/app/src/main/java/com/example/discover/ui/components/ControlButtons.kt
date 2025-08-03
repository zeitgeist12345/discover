package com.example.discover.ui.components

import androidx.compose.foundation.BorderStroke
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
        // Use BoxWithConstraints to get the available width
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content (Row or Column)
        ) {
            // Define a threshold for when to switch to a Column
            // This is an arbitrary value; you'll need to test and adjust
            // based on your button sizes and desired behavior.
            val thresholdWidth = 360.dp // Example threshold

            if (this.maxWidth < thresholdWidth) {
                // Not enough space for a Row, use a Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Add some spacing between buttons
                ) {
                    PreviousButton(onClick = onPreviousClick)
                    RandomButton(onClick = onRandomClick)
                    NextButton(onClick = onNextClick)
                }
            } else {
                // Enough space, use a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviousButton(onClick = onPreviousClick, modifier = Modifier.weight(1f))
                    RandomButton(onClick = onRandomClick, modifier = Modifier.weight(1f))
                    NextButton(onClick = onNextClick, modifier = Modifier.weight(1f))
                }
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

// Extracted Button composables for clarity and reusability
@Composable
private fun PreviousButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDark,
            contentColor = TextPrimary
        ),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.padding(horizontal = 4.dp) // Add some padding if they are in a row
    ) {
        Text("⬅️ Previous")
    }
}

@Composable
private fun RandomButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreenDark,
            contentColor = TextPrimary
        ),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text("🎲 Random", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDark,
            contentColor = TextPrimary
        ),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text("Next ➡️")
    }
}