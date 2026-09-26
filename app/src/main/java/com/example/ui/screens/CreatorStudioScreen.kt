package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.UUID

data class NewLessonInput(
    val title: String,
    val durationMinutes: Int,
    val isFreePreview: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioScreen(
    onPublishCourse: (
        title: String,
        subtitle: String,
        description: String,
        category: String,
        price: Double,
        instructorName: String,
        lessons: List<Lesson>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateWizard by remember { mutableStateOf(false) }

    // Form inputs
    var courseTitle by remember { mutableStateOf("") }
    var courseSubtitle by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mobile & Web") }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("49.99") }
    var instructorName by remember { mutableStateOf("Ashvin Rojasara") }

    // Lessons to upload
    val lessonsList = remember {
        mutableStateListOf(
            NewLessonInput("Lesson 1: Architecture Overview & Tool Setup", 15, true),
            NewLessonInput("Lesson 2: Core Components & Data Modeling", 24, false),
            NewLessonInput("Lesson 3: Complete Project Walkthrough", 35, false)
        )
    }

    var newLessonTitle by remember { mutableStateOf("") }
    var newLessonDuration by remember { mutableStateOf("20") }
    var newLessonIsFree by remember { mutableStateOf(false) }

    var isPublishing by remember { mutableStateOf(false) }
    var publishSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("creator_studio_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Creator Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Host video lessons & sell to global students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateWizard = !showCreateWizard },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("toggle_create_course_wizard")
                ) {
                    Icon(imageVector = if (showCreateWizard) Icons.Default.Delete else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showCreateWizard) "Close" else "New Course", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Creator Metrics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                            Text("SALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                        }
                        Text("$18,420", fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("30d Revenue", fontSize = 10.sp, color = Slate400)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
                            Text("STUDENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Indigo500)
                        }
                        Text("2,840", fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Active Learners", fontSize = 10.sp, color = Slate400)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            Text("RATING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Amber500)
                        }
                        Text("4.94 ★", fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("1,420 reviews", fontSize = 10.sp, color = Slate400)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Integrated Video CDN Hosting Status Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald500))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EduPulse Cloud Video CDN (HLS/DRM)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("100% Uptime", color = Emerald500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { 0.38f },
                        color = Cyan500,
                        trackColor = Slate800,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("38.2 GB of 100 GB Video Storage Used", color = Slate400, fontSize = 10.sp)
                        Text("Adaptive Bitrate Active", color = Cyan500, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Publish Success Message
        publishSuccessMessage?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald500.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = Emerald500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Course Creation Wizard Card
        if (showCreateWizard) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("publish_course_wizard_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Publish & Host New Course",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fill in course details and upload/link your video lessons.",
                            fontSize = 11.sp,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Course Title
                        OutlinedTextField(
                            value = courseTitle,
                            onValueChange = { courseTitle = it },
                            label = { Text("Course Title") },
                            placeholder = { Text("e.g. Kotlin Multiplatform Mastery 2026") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_course_title_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Course Subtitle
                        OutlinedTextField(
                            value = courseSubtitle,
                            onValueChange = { courseSubtitle = it },
                            label = { Text("Subtitle / Catchphrase") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_course_subtitle_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category & Price Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { selectedCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("new_course_category_input")
                            )

                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Price ($)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f).testTag("new_course_price_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        OutlinedTextField(
                            value = courseDescription,
                            onValueChange = { courseDescription = it },
                            label = { Text("Course Description") },
                            minLines = 3,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_course_desc_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Video Lessons Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Video Lessons (${lessonsList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Encrypted HLS", fontSize = 11.sp, color = Cyan500, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of added lessons
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            lessonsList.forEachIndexed { idx, lesson ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(lesson.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("${lesson.durationMinutes} mins • ${if (lesson.isFreePreview) "Free Preview" else "Paid Only"}", fontSize = 10.sp, color = Slate400)
                                        }
                                        IconButton(
                                            onClick = { lessonsList.removeAt(idx) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add Lesson Sub-form
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newLessonTitle,
                                onValueChange = { newLessonTitle = it },
                                placeholder = { Text("New lesson title...", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = newLessonDuration,
                                onValueChange = { newLessonDuration = it },
                                placeholder = { Text("Mins", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(64.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (newLessonTitle.isNotBlank()) {
                                        lessonsList.add(
                                            NewLessonInput(
                                                title = newLessonTitle.trim(),
                                                durationMinutes = newLessonDuration.toIntOrNull() ?: 15,
                                                isFreePreview = newLessonIsFree
                                            )
                                        )
                                        newLessonTitle = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Publish Button
                        Button(
                            onClick = {
                                if (courseTitle.isNotBlank()) {
                                    val price = priceText.toDoubleOrNull() ?: 49.99
                                    val convertedLessons = lessonsList.mapIndexed { idx, it ->
                                        Lesson(
                                            id = "new-lesson-$idx",
                                            courseId = "",
                                            moduleTitle = "Module 1: Complete Curriculum",
                                            moduleIndex = 1,
                                            lessonIndex = idx + 1,
                                            title = it.title,
                                            durationMinutes = it.durationMinutes,
                                            videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                            isFreePreview = it.isFreePreview,
                                            description = "Hosted video stream lesson.",
                                            resolutionBadge = "1080p 60fps HD"
                                        )
                                    }

                                    onPublishCourse(
                                        courseTitle.trim(),
                                        courseSubtitle.ifBlank { "Comprehensive video course" },
                                        courseDescription.ifBlank { "Full video course hosted on EduPulse Cloud HLS." },
                                        selectedCategory,
                                        price,
                                        instructorName,
                                        convertedLessons
                                    )

                                    publishSuccessMessage = "Course '$courseTitle' published live to the Marketplace!"
                                    showCreateWizard = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("publish_course_submit_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Course to Marketplace", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
