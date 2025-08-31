package com.example.discover.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.discover.ui.theme.*

// --- Using Approximation ---
private val APPROX_SINGLE_CONTROL_BUTTON_WIDTH =
    Spacing.small * 14 // Adjusted estimate based on "⬅️ Previous" or "🎲 Random"
private val MIN_INTER_BUTTON_SPACING_ROW = Spacing.small
private const val NUMBER_OF_MAIN_CONTROL_BUTTONS = 3

// Calculate the threshold dynamically based on the approximations
private val DYNAMIC_CONTROL_BUTTONS_THRESHOLD =
    (APPROX_SINGLE_CONTROL_BUTTON_WIDTH * NUMBER_OF_MAIN_CONTROL_BUTTONS) + (MIN_INTER_BUTTON_SPACING_ROW * (NUMBER_OF_MAIN_CONTROL_BUTTONS - 1))
// Example: (8 * 14 * 3) + (8 * 2) = 336 + 16 = 352 dp

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
            .padding(Spacing.small), // Using theme spacing
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            // Use the dynamically calculated threshold
            if (this.maxWidth < DYNAMIC_CONTROL_BUTTONS_THRESHOLD) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.small) // Using theme spacing
                ) {
                    PreviousButton(
                        onClick = onPreviousClick, modifier = Modifier.fillMaxWidth()
                    ) // Fill width in Column
                    RandomButton(
                        onClick = onRandomClick, modifier = Modifier.fillMaxWidth()
                    )   // Fill width in Column
                    NextButton(
                        onClick = onNextClick, modifier = Modifier.fillMaxWidth()
                    )       // Fill width in Column
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly, // Or Arrangement.spacedBy for consistent spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // When in a Row, you might not want them to always fill max width individually
                    // unless that's the design. `weight(1f)` makes them share space.
                    PreviousButton(onClick = onPreviousClick, modifier = Modifier.weight(1f))
                    RandomButton(onClick = onRandomClick, modifier = Modifier.weight(1f))
                    NextButton(onClick = onNextClick, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.medium)) // Using theme spacing

        Button(
            onClick = onAddWebsiteClick, colors = ButtonDefaults.buttonColors(
                containerColor = SuccessColor, contentColor = TextPrimary
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Add Website", fontWeight = FontWeight.Bold)
        }
    }
}

// Button composables - ensure their padding and content are considered in APPROX_SINGLE_CONTROL_BUTTON_WIDTH
@Composable
private fun PreviousButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDark, contentColor = TextPrimary
        ), modifier = modifier
    ) {
        Text("⬅️Previous", fontSize = MaterialTheme.typography.labelMedium.fontSize)
    }
}

@Composable
private fun RandomButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreenDark, contentColor = TextPrimary
        ), modifier = modifier
    ) {
        Text("🎲Random", fontSize = MaterialTheme.typography.labelMedium.fontSize)
    }
}

@Composable
private fun NextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDark, contentColor = TextPrimary
        ), modifier = modifier
    ) {
        Text("Next➡️", fontSize = MaterialTheme.typography.labelMedium.fontSize)
    }
}
