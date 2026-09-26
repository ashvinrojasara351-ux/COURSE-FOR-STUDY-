package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerComponent(
    lesson: Lesson,
    isEnrolled: Boolean,
    onProgressUpdated: (currentSec: Long, isDone: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSec = remember(lesson) { (lesson.durationMinutes * 60).toFloat() }
    var currentSec by remember(lesson) { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableStateOf("1.0x") }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var resolution by remember { mutableStateOf(lesson.resolutionBadge) }
    var showResolutionMenu by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    // Simulation of active video time ticks when playing
    LaunchedEffect(isPlaying, lesson) {
        while (isPlaying) {
            delay(1000)
            val speedFactor = playbackSpeed.replace("x", "").toFloatOrNull() ?: 1.0f
            if (currentSec + speedFactor < totalSec) {
                currentSec += speedFactor
                onProgressUpdated(currentSec.toLong(), false)
            } else {
                currentSec = totalSec
                isPlaying = false
                isCompleted = true
                onProgressUpdated(currentSec.toLong(), true)
            }
        }
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Slate950)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("video_player_box")
    ) {
        // Video Stage Visuals / Simulated Stream Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Slate800, Slate950),
                        radius = 600f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Grid & Stream status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900.copy(alpha = 0.8f),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Emerald500 else Slate400)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "CDN Streaming Active • $resolution" else "Video Paused",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = lesson.title,
                    color = Slate400,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // DRM Watermark (Hosted Video Anti-Piracy Protection)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color(0x99000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "DRM",
                tint = Cyan500,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "DRM • ashvinrojasara351@gmail.com",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Free Preview Badge if not enrolled
        if (!isEnrolled && lesson.isFreePreview) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Emerald500
            ) {
                Text(
                    text = "FREE PREVIEW",
                    color = Slate950,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls || !isPlaying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x7A000000))
            ) {
                // Center Play/Rewind/Forward buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            currentSec = (currentSec - 10f).coerceAtLeast(0f)
                            onProgressUpdated(currentSec.toLong(), false)
                        },
                        modifier = Modifier.testTag("video_rewind_10s")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Main Play/Pause
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Indigo500)
                            .clickable { isPlaying = !isPlaying }
                            .testTag("video_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            currentSec = (currentSec + 10f).coerceAtMost(totalSec)
                            onProgressUpdated(currentSec.toLong(), currentSec >= totalSec)
                        },
                        modifier = Modifier.testTag("video_forward_10s")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Bar Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xE60F172A))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Scrubber Slider
                    Slider(
                        value = currentSec,
                        onValueChange = {
                            currentSec = it
                            onProgressUpdated(it.toLong(), it >= totalSec)
                        },
                        valueRange = 0f..totalSec,
                        colors = SliderDefaults.colors(
                            thumbColor = Indigo500,
                            activeTrackColor = Indigo500,
                            inactiveTrackColor = Slate800
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .testTag("video_progress_slider")
                    )

                    // Timestamps & Quality / Speed Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current / Total Time
                        val curMin = (currentSec / 60).toInt()
                        val curS = (currentSec % 60).toInt()
                        val totMin = (totalSec / 60).toInt()
                        val totS = (totalSec % 60).toInt()
                        Text(
                            text = String.format("%02d:%02d / %02d:%02d", curMin, curS, totMin, totS),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Speed Selector
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clickable { showSpeedMenu = true }
                                        .background(Slate800, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Speed",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = playbackSpeed,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    listOf("0.75x", "1.0x", "1.25x", "1.5x", "2.0x").forEach { spd ->
                                        DropdownMenuItem(
                                            text = { Text(spd) },
                                            onClick = {
                                                playbackSpeed = spd
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Resolution Selector
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clickable { showResolutionMenu = true }
                                        .background(Slate800, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Quality",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = resolution.take(5),
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }

                                DropdownMenu(
                                    expanded = showResolutionMenu,
                                    onDismissRequest = { showResolutionMenu = false }
                                ) {
                                    listOf("4K 60fps UltraHD", "1080p 60fps HD", "720p HD", "Auto (Dynamic CDN)").forEach { res ->
                                        DropdownMenuItem(
                                            text = { Text(res) },
                                            onClick = {
                                                resolution = res
                                                showResolutionMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Mark Completed button
                            IconButton(
                                onClick = {
                                    isCompleted = true
                                    currentSec = totalSec
                                    onProgressUpdated(totalSec.toLong(), true)
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("mark_lesson_completed_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Mark Complete",
                                    tint = if (isCompleted) Emerald500 else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
