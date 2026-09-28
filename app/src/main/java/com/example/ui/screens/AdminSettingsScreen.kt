package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    uiState: UiState,
    onSaveSetting: (key: String, value: String) -> Unit,
    onToggleMaintenance: () -> Unit
) {
    var apiKey by remember(uiState.settingsMap) {
        mutableStateOf(uiState.settingsMap["gemini_api_key"] ?: "")
    }
    var chatLimit by remember(uiState.settingsMap) {
        mutableStateOf(uiState.settingsMap["daily_chat_limit"] ?: "50")
    }
    var imageLimit by remember(uiState.settingsMap) {
        mutableStateOf(uiState.settingsMap["daily_image_limit"] ?: "5")
    }
    var videoLimit by remember(uiState.settingsMap) {
        mutableStateOf(uiState.settingsMap["daily_video_limit"] ?: "2")
    }
    var selectedModel by remember(uiState.settingsMap) {
        mutableStateOf(uiState.settingsMap["model_text"] ?: "gemini-3.5-flash")
    }

    val availableModels = listOf(
        "gemini-3.5-flash" to "Gemini 3.5 Flash (Fast, General)",
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Complex STEM, Code)",
        "gemini-2.5-flash-image" to "Gemini 2.5 Flash Image"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_settings_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Admin Configuration & Cost Controls",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Configure AI provider adapters, rate limits, model routing, and maintenance states.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Maintenance Mode Toggle Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.maintenanceMode) StatusWarning.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (uiState.maintenanceMode) StatusWarning else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Maintenance Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (uiState.maintenanceMode) StatusWarning else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Temporarily pause user generation endpoints across Chat, Image, and Video.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.maintenanceMode,
                        onCheckedChange = { onToggleMaintenance() },
                        modifier = Modifier.testTag("maintenance_mode_switch")
                    )
                }
            }
        }

        // Provider Credentials Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryCyan)
                        Text(
                            text = "Provider Connection & Keys",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Defaults to GEMINI_API_KEY from environment secrets. You can override with a custom key below:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Gemini API Key Override") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_api_key_input")
                    )

                    Button(
                        onClick = { onSaveSetting("gemini_api_key", apiKey.trim()) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("admin_save_key_button")
                    ) {
                        Text("Save Key")
                    }
                }
            }
        }

        // Model Selection Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Active Chat Model Selection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    availableModels.forEach { (modelId, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedModel == modelId,
                                onClick = {
                                    selectedModel = modelId
                                    onSaveSetting("model_text", modelId)
                                },
                                modifier = Modifier.testTag("radio_model_$modelId")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(modelId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Configurable Usage Limits Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Daily Rate Limits & Cost Control",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    OutlinedTextField(
                        value = chatLimit,
                        onValueChange = { chatLimit = it },
                        label = { Text("Daily Chat Prompts Limit") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_chat_limit_input")
                    )

                    OutlinedTextField(
                        value = imageLimit,
                        onValueChange = { imageLimit = it },
                        label = { Text("Daily Image Generations Limit") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_image_limit_input")
                    )

                    OutlinedTextField(
                        value = videoLimit,
                        onValueChange = { videoLimit = it },
                        label = { Text("Daily Video Generations Limit") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_video_limit_input")
                    )

                    Button(
                        onClick = {
                            onSaveSetting("daily_chat_limit", chatLimit)
                            onSaveSetting("daily_image_limit", imageLimit)
                            onSaveSetting("daily_video_limit", videoLimit)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("admin_save_limits_button")
                    ) {
                        Text("Save Quotas")
                    }
                }
            }
        }

        // Platform & Database Statistics
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Local System Telemetry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text("• Stored Conversations: ${uiState.conversations.size}", fontSize = 12.sp)
                    Text("• Saved Image Records: ${uiState.imageGenerations.size}", fontSize = 12.sp)
                    Text("• Saved Video Records: ${uiState.videoGenerations.size}", fontSize = 12.sp)
                    Text("• Today's Active Date: ${uiState.todayUsage?.dateString ?: "N/A"}", fontSize = 12.sp)
                }
            }
        }
    }
}
