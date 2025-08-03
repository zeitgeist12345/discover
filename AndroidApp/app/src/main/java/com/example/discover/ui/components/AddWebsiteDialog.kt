package com.example.discover.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.discover.ui.theme.*

@Composable
fun AddWebsiteDialog(
    onDismiss: () -> Unit,
    onAddWebsite: (name: String, url: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "➕ Add New Website",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Website Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = PrimaryGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    singleLine = true
                )
                
                // URL field
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = PrimaryGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true
                )
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = PrimaryGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    minLines = 3,
                    maxLines = 5
                )
                
                // Error message
                if (error != null) {
                    Text(
                        text = error!!,
                        color = ErrorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || url.isBlank() || description.isBlank()) {
                        error = "Please fill in all required fields"
                        return@Button
                    }
                    
                    isLoading = true
                    error = null
                    
                    onAddWebsite(name, url, description)
                    isLoading = false
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreenDark,
                    contentColor = TextPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Website")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary
    )
} 