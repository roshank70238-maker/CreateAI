package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    uiState: UiState,
    onNavigate: (ScreenDestination) -> Unit
) {
    val usage = uiState.todayUsage
    val maxImages = uiState.settingsMap["daily_image_limit"]?.toIntOrNull() ?: 5
    val maxVideos = uiState.settingsMap["daily_video_limit"]?.toIntOrNull() ?: 2
    val maxChats = uiState.settingsMap["daily_chat_limit"]?.toIntOrNull() ?: 50

    val chatCount = usage?.chatCount ?: 0
    val imageCount = usage?.imageCount ?: 0
    val videoCount = usage?.videoCount ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Hero Studio Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0F2F44)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(PrimaryCyan, AccentViolet)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CREATIVE ENGINE 3.5",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCyan
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Free Plan",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Text(
                            text = "Next-Generation Multimodal AI Suite",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 28.sp
                        )

                        Text(
                            text = "Chat with cutting-edge models, synthesize photorealistic images, and orchestrate cinematic video pipelines with robust provider abstraction.",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Button(
                                onClick = { onNavigate(ScreenDestination.CHAT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("hero_start_chat_button")
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Chat", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onNavigate(ScreenDestination.IMAGE) },
                                border = BorderStroke(1.dp, PrimaryLightCyan),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.testTag("hero_generate_image_button")
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Image")
                            }
                        }
                    }
                }
            }
        }

        // Daily Cost & Quota Control Cards
        item {
            Text(
                text = "Today's Usage & Cost Controls",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UsageMetricCard(
                    title = "AI Chat",
                    count = chatCount,
                    max = maxChats,
                    unit = "prompts",
                    color = PrimaryCyan,
                    modifier = Modifier.weight(1f)
                )
                UsageMetricCard(
                    title = "Images",
                    count = imageCount,
                    max = maxImages,
                    unit = "gens",
                    color = AccentViolet,
                    modifier = Modifier.weight(1f)
                )
                UsageMetricCard(
                    title = "Videos",
                    count = videoCount,
                    max = maxVideos,
                    unit = "renders",
                    color = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Feature Launchers
        item {
            Text(
                text = "Creation Workspaces",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WorkspaceLauncherCard(
                    title = "AI Chat Workspace",
                    description = "Conversational intelligence powered by Google Gemini with markdown code blocks and conversation history.",
                    icon = Icons.Default.ChatBubbleOutline,
                    badgeText = "Gemini 3.5 Flash",
                    accentColor = PrimaryCyan,
                    onClick = { onNavigate(ScreenDestination.CHAT) }
                )

                WorkspaceLauncherCard(
                    title = "AI Image Generator",
                    description = "Text-to-image synthesis with aspect ratio controls, resolutions, prompt styling, and gallery history.",
                    icon = Icons.Default.Palette,
                    badgeText = "Imagen / Custom Provider",
                    accentColor = AccentViolet,
                    onClick = { onNavigate(ScreenDestination.IMAGE) }
                )

                WorkspaceLauncherCard(
                    title = "AI Video Synthesizer",
                    description = "Cinematic video generation from prompts or image frames. Secure provider integration with status checks.",
                    icon = Icons.Default.Videocam,
                    badgeText = "Veo 3.1 / External Provider",
                    accentColor = SecondaryTeal,
                    onClick = { onNavigate(ScreenDestination.VIDEO) }
                )
            }
        }

        // Recent Generations Reel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Creations & Prompts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(ScreenDestination.HISTORY) }) {
                    Text("View All", fontSize = 13.sp)
                }
            }

            if (uiState.imageGenerations.isEmpty() && uiState.videoGenerations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No generations yet",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Start by chatting or creating your first AI image.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(uiState.imageGenerations.take(5)) { item ->
                        RecentImageCard(item.prompt, item.status, item.aspectRatio) {
                            onNavigate(ScreenDestination.HISTORY)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageMetricCard(
    title: String,
    count: Int,
    max: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "$count / $max",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (count >= max) StatusError else MaterialTheme.colorScheme.onSurface
            )
            LinearProgressIndicator(
                progress = { (count.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f) },
                color = if (count >= max) StatusError else color,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WorkspaceLauncherCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RecentImageCard(
    prompt: String,
    status: String,
    aspectRatio: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF334155))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (status == "COMPLETED") Icons.Default.Image else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (status == "COMPLETED") PrimaryCyan else StatusWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (status == "UNCONFIGURED") "Unconfigured" else status,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
            Text(
                text = prompt,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(aspectRatio, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
