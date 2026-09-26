package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.LessonProgress
import com.example.data.model.UserNote
import com.example.ui.components.ReviewSection
import com.example.ui.components.VideoPlayerComponent
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    course: Course,
    lessons: List<Lesson>,
    enrollment: Enrollment?,
    progressList: List<LessonProgress>,
    notes: List<UserNote>,
    reviews: List<CourseReview>,
    activeLessonId: String?,
    isPlayingVideo: Boolean,
    onBack: () -> Unit,
    onEnrollClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onVideoProgressUpdate: (lessonId: String, currentSec: Long, isDone: Boolean) -> Unit,
    onAddNote: (lessonId: String, text: String) -> Unit,
    onDeleteNote: (noteId: String) -> Unit,
    onWriteReviewClick: () -> Unit,
    onReviewHelpfulClick: (String) -> Unit,
    onViewCertificateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnrolled = enrollment != null
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Curriculum, 1: Reviews, 2: Notes
    val expandedModules = remember { mutableStateMapOf<String, Boolean>() }

    // Active lesson or fallback to first free preview
    val currentPlayingLesson = remember(activeLessonId, lessons) {
        lessons.find { it.id == activeLessonId } ?: lessons.firstOrNull { it.isFreePreview } ?: lessons.firstOrNull()
    }

    val completedLessonIds = remember(progressList) {
        progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
    }

    // Group lessons by module
    val modules = remember(lessons) {
        lessons.groupBy { it.moduleTitle }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = course.title,
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("course_detail_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Sticky CTA Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("course_detail_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEnrolled) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enrolled (${enrollment?.progressPercent ?: 0}% Done)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                            Text(
                                text = "${completedLessonIds.size} of ${lessons.size} lessons completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (enrollment?.isCompleted == true) {
                            Button(
                                onClick = onViewCertificateClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("view_certificate_button")
                            ) {
                                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Slate900)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Certificate", color = Slate900, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val nextLesson = lessons.firstOrNull { it.id !in completedLessonIds } ?: lessons.firstOrNull()
                                    if (nextLesson != null) onLessonClick(nextLesson)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("resume_learning_button")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Continue Learning", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Not enrolled: price + checkout button
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", course.price)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (course.originalPrice > course.price) {
                                    Text(
                                        text = "$${String.format("%.2f", course.originalPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = Slate400
                                    )
                                }
                            }
                            Text(
                                text = "Lifetime Access • Full Video Stream",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }

                        Button(
                            onClick = onEnrollClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("enroll_now_checkout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Secure Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Video Player Top Hero
            item {
                if (isPlayingVideo && currentPlayingLesson != null) {
                    VideoPlayerComponent(
                        lesson = currentPlayingLesson,
                        isEnrolled = isEnrolled,
                        onProgressUpdated = { sec, done ->
                            onVideoProgressUpdate(currentPlayingLesson.id, sec, done)
                        }
                    )
                } else {
                    // Preview Banner Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        val drawableId = if (course.thumbnailName == "img_course_ai") R.drawable.img_course_ai else R.drawable.img_hero_banner
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = course.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x990F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Indigo500)
                                        .clickable {
                                            val previewLesson = lessons.firstOrNull { it.isFreePreview } ?: lessons.firstOrNull()
                                            if (previewLesson != null) onLessonClick(previewLesson)
                                        }
                                        .testTag("play_preview_video_hero"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Preview",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Watch Free Preview Video",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = course.videoHostingBadge,
                                    color = Cyan500,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Course Info Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = course.category,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Rating overview (Displayed prominently as requested!)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("course_rating_header")
                        ) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = "Rating", tint = Amber500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", course.rating),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Amber500
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${course.reviewCount} reviews)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = course.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instructor Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Indigo500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = course.instructorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = course.instructorTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Course Specs Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Outlined.AccessTime, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${course.durationHours} Hours", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Outlined.OndemandVideo, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${lessons.size} Lessons", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Certificate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // What You'll Learn Box
                    if (course.skillsLearned.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "What You'll Master",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                course.skillsLearned.split(";").forEach { skill ->
                                    if (skill.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Emerald500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = skill.trim(),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Navigation Tabs: Curriculum vs Student Reviews vs Notes
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Indigo500,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Curriculum (${lessons.size})", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_curriculum")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reviews (${reviews.size})", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_reviews")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Notes (${notes.size})", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_notes")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tab 0: Curriculum Lessons
            if (selectedTab == 0) {
                modules.forEach { (moduleTitle, moduleLessons) ->
                    item {
                        val isExpanded = expandedModules[moduleTitle] ?: true
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedModules[moduleTitle] = !isExpanded }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = moduleTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${moduleLessons.size} lessons • ${moduleLessons.sumOf { it.durationMinutes }} mins",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    moduleLessons.forEach { lesson ->
                                        val isCompleted = lesson.id in completedLessonIds
                                        val isAccessible = isEnrolled || lesson.isFreePreview
                                        val isCurrentlyPlaying = lesson.id == activeLessonId

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isCurrentlyPlaying) Indigo500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = isAccessible) { onLessonClick(lesson) }
                                                .testTag("lesson_item_${lesson.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isCompleted -> Emerald500.copy(alpha = 0.2f)
                                                                isCurrentlyPlaying -> Indigo500
                                                                isAccessible -> MaterialTheme.colorScheme.surfaceVariant
                                                                else -> Slate800
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            isCompleted -> Icons.Default.CheckCircle
                                                            isCurrentlyPlaying -> Icons.Default.PlayArrow
                                                            isAccessible -> Icons.Default.PlayCircle
                                                            else -> Icons.Default.Lock
                                                        },
                                                        contentDescription = null,
                                                        tint = when {
                                                            isCompleted -> Emerald500
                                                            isCurrentlyPlaying -> Color.White
                                                            isAccessible -> Indigo500
                                                            else -> Slate400
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = lesson.title,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${lesson.durationMinutes} mins • ${lesson.resolutionBadge}",
                                                            fontSize = 11.sp,
                                                            color = Slate400
                                                        )
                                                        if (!isEnrolled && lesson.isFreePreview) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "Free Preview",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Emerald500
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: Student Reviews Section (The core requested feature!)
            if (selectedTab == 1) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ReviewSection(
                            course = course,
                            reviews = reviews,
                            isEnrolled = isEnrolled,
                            onWriteReviewClick = onWriteReviewClick,
                            onHelpfulClick = onReviewHelpfulClick
                        )
                    }
                }
            }

            // Tab 2: Timestamped In-Video Notes
            if (selectedTab == 2) {
                item {
                    var newNoteText by remember { mutableStateOf("") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Course Study Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Take private timestamped notes while watching video lectures.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Add note field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newNoteText,
                                onValueChange = { newNoteText = it },
                                placeholder = { Text("Write note for current lesson...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_note_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newNoteText.isNotBlank() && currentPlayingLesson != null) {
                                        onAddNote(currentPlayingLesson.id, newNoteText.trim())
                                        newNoteText = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                                modifier = Modifier.testTag("save_note_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (notes.isEmpty()) {
                            Text(
                                text = "No notes yet for this course. Type above to add your first bookmark!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                notes.forEach { note ->
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                val min = note.timestampSec / 60
                                                val s = note.timestampSec % 60
                                                Text(
                                                    text = "⏱ Note at ${String.format("%02d:%02d", min, s)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Cyan500
                                                )
                                                Text(
                                                    text = note.noteText,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            IconButton(onClick = { onDeleteNote(note.id) }) {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
