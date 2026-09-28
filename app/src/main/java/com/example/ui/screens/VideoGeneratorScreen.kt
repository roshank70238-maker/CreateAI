package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.VideoGenerationEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoGeneratorScreen(
    uiState: UiState,
    onGenerate: (prompt: String, sourceImageUrl: String?, duration: Int, aspectRatio: String) -> Unit,
    onDeleteVideo: (String) -> Unit
) {
    val context = LocalContext.current
    var promptText by remember { mutableStateOf("") }
    var sourceImageUrl by remember { mutableStateOf("") }
    var showImageInput by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableIntStateOf(5) }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }

    val durations = listOf(3, 5, 10)
    val aspectRatios = listOf("16:9", "9:16", "1:1")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("video_generator_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Video Studio Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Video Synthesizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SecondaryTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Veo 3.1 / Pipeline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryTeal
                            )
                        }
                    }

                    // Prompt Input
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                "Describe motion, camera movement, and action (e.g. 'Drone shot descending over misty pine forest at sunrise, cinematic lighting, 4k')...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("video_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    // Optional Image-to-Video
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showImageInput = !showImageInput }
                    ) {
                        Icon(
                            imageVector = if (showImageInput) Icons.Default.ExpandLess else Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = SecondaryTeal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showImageInput) "Hide image-to-video source" else "Optional: Image-to-Video source frame URL",
                            fontSize = 12.sp,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AnimatedVisibility(visible = showImageInput) {
                        OutlinedTextField(
                            value = sourceImageUrl,
                            onValueChange = { sourceImageUrl = it },
                            placeholder = { Text("https://example.com/source_image.jpg", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // Duration & Aspect Ratio Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Duration
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Duration",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                durations.forEach { dur ->
                                    val selected = selectedDuration == dur
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedDuration = dur },
                                        label = { Text("${dur}s", fontSize = 12.sp) },
                                        modifier = Modifier.testTag("duration_${dur}s")
                                    )
                                }
                            }
                        }

                        // Aspect Ratio
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Aspect Ratio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                aspectRatios.forEach { ratio ->
                                    val selected = selectedAspectRatio == ratio
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedAspectRatio = ratio },
                                        label = { Text(ratio, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Progress indicator if generating
                    if (uiState.isVideoGenerating) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Synthesizing video pipeline...",
                                    fontSize = 12.sp,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${uiState.videoGenProgress}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryTeal
                                )
                            }
                            LinearProgressIndicator(
                                progress = { uiState.videoGenProgress / 100f },
                                color = SecondaryTeal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    // Generate Button
                    Button(
                        onClick = {
                            onGenerate(
                                promptText,
                                sourceImageUrl.takeIf { it.isNotBlank() },
                                selectedDuration,
                                selectedAspectRatio
                            )
                        },
                        enabled = promptText.isNotBlank() && !uiState.isVideoGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryTeal,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("video_generate_button")
                    ) {
                        if (uiState.isVideoGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rendering frames...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Video", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Video Generations History
        item {
            Text(
                text = "Video Generations (${uiState.videoGenerations.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (uiState.videoGenerations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No video generations yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Submit a prompt to start generation or test provider configuration.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.videoGenerations, key = { it.id }) { item ->
                VideoGenerationCard(
                    item = item,
                    onDelete = { onDeleteVideo(item.id) },
                    onDownload = {
                        Toast.makeText(context, "Video clip downloaded", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun VideoGenerationCard(
    item: VideoGenerationEntity,
    onDelete: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Video Player Card Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B132B)),
                contentAlignment = Alignment.Center
            ) {
                if (item.videoUrl != null) {
                    // Active Video Preview Player
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SecondaryTeal.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = SecondaryLightTeal,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to Play Loop",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Provider unconfigured or error status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (item.status == "UNCONFIGURED") Icons.Default.TvOff else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (item.status == "UNCONFIGURED") StatusWarning else StatusError,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (item.status == "UNCONFIGURED") "Provider not configured" else "Render error",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = item.errorMessage ?: "Requires external Veo or video provider connection in Settings.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2
                        )
                    }
                }
            }

            // Prompt description
            Text(
                text = item.prompt,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipTag("${item.durationSeconds}s")
                    ChipTag(item.aspectRatio)
                    ChipTag(item.providerName.split("/").first().trim())
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("video_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Video", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("video_delete_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
