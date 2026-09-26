package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayLesson
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CheckoutUiState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.CertificateDialog
import com.example.ui.components.CheckoutModal
import com.example.ui.components.WriteReviewDialog
import com.example.ui.screens.CourseDetailScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.MyLearningScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.Indigo500
import com.example.ui.theme.MyApplicationTheme

data class NavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                EduPulseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EduPulseApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedCourseId by viewModel.selectedCourseId.collectAsStateWithLifecycle()
    val activeLessonId by viewModel.activeLessonId.collectAsStateWithLifecycle()
    val isPlayingVideo by viewModel.isPlayingVideo.collectAsStateWithLifecycle()

    val courses by viewModel.filteredCourses.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val enrolledCourses by viewModel.enrolledCourses.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val reviewDialogState by viewModel.reviewDialogState.collectAsStateWithLifecycle()
    val certificateCourse by viewModel.certificateCourse.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem(Screen.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
        NavItem(Screen.MY_LEARNING, "My Learning", Icons.Filled.PlayLesson, Icons.Outlined.PlayLesson, "nav_learning"),
        NavItem(Screen.CREATOR_STUDIO, "Studio", Icons.Filled.VideoCall, Icons.Outlined.VideoCall, "nav_studio"),
        NavItem(Screen.TRANSACTIONS, "Receipts", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_receipts")
    )

    // Handle Android system back gesture when course detail is open
    if (selectedCourseId != null) {
        BackHandler {
            viewModel.closeCourseDetail()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (selectedCourseId == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
                    ) {
                        navItems.forEach { item ->
                            val isSelected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Indigo500,
                                    selectedTextColor = Indigo500,
                                    indicatorColor = Indigo500.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets.navigationBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedCourseId != null) {
                    val course = allCourses.find { it.id == selectedCourseId }
                    if (course != null) {
                        val lessons by viewModel.getLessonsForCourse(course.id).collectAsStateWithLifecycle(emptyList())
                        val enrollment by viewModel.getEnrollment(course.id).collectAsStateWithLifecycle(null)
                        val progressList by viewModel.getLessonProgressForCourse(course.id).collectAsStateWithLifecycle(emptyList())
                        val notes by viewModel.getLessonNotes(activeLessonId ?: "").collectAsStateWithLifecycle(emptyList())
                        val reviews by viewModel.getCourseReviews(course.id).collectAsStateWithLifecycle(emptyList())

                        CourseDetailScreen(
                            course = course,
                            lessons = lessons,
                            enrollment = enrollment,
                            progressList = progressList,
                            notes = notes,
                            reviews = reviews,
                            activeLessonId = activeLessonId,
                            isPlayingVideo = isPlayingVideo,
                            onBack = { viewModel.closeCourseDetail() },
                            onEnrollClick = { viewModel.openCheckout(course) },
                            onLessonClick = { lesson -> viewModel.openVideoPlayer(lesson.id) },
                            onVideoProgressUpdate = { lessonId, sec, done ->
                                viewModel.markLessonWatched(course.id, lessonId, sec, done)
                            },
                            onAddNote = { lessonId, text ->
                                viewModel.addNote(course.id, lessonId, 60, text)
                            },
                            onDeleteNote = { noteId ->
                                viewModel.deleteNote(noteId)
                            },
                            onWriteReviewClick = {
                                viewModel.openReviewDialog(course)
                            },
                            onReviewHelpfulClick = { reviewId ->
                                viewModel.markReviewHelpful(reviewId)
                            },
                            onViewCertificateClick = {
                                viewModel.viewCertificate(course)
                            }
                        )
                    }
                } else {
                    when (currentScreen) {
                        Screen.EXPLORE -> {
                            ExploreScreen(
                                courses = courses,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCategorySelect = { viewModel.setSelectedCategory(it) },
                                onCourseClick = { courseId -> viewModel.openCourseDetail(courseId) }
                            )
                        }

                        Screen.MY_LEARNING -> {
                            MyLearningScreen(
                                enrolledCourses = enrolledCourses,
                                onCourseClick = { courseId -> viewModel.openCourseDetail(courseId) },
                                onWriteReviewClick = { course -> viewModel.openReviewDialog(course) },
                                onViewCertificateClick = { course -> viewModel.viewCertificate(course) },
                                onExploreClick = { viewModel.navigateTo(Screen.EXPLORE) }
                            )
                        }

                        Screen.CREATOR_STUDIO -> {
                            CreatorStudioScreen(
                                onPublishCourse = { title, sub, desc, cat, price, instructor, lessons ->
                                    viewModel.publishCourse(title, sub, desc, cat, price, instructor, lessons)
                                }
                            )
                        }

                        Screen.TRANSACTIONS -> {
                            TransactionsScreen(
                                transactions = transactions
                            )
                        }
                    }
                }
            }
        }

        // Global Checkout BottomSheet Modal
        CheckoutModal(
            state = checkoutState,
            onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
            onApplyPromo = { viewModel.applyPromoCode(it) },
            onPayClicked = { viewModel.processPayment() },
            onDismiss = { viewModel.closeCheckout() },
            onContinueLearning = {
                val purchasedCourse = checkoutState.course
                viewModel.closeCheckout()
                if (purchasedCourse != null) {
                    viewModel.openCourseDetail(purchasedCourse.id)
                }
            }
        )

        // Global Write Review Dialog
        WriteReviewDialog(
            state = reviewDialogState,
            onRatingChange = { viewModel.updateReviewRating(it) },
            onStudentNameChange = { viewModel.updateReviewStudentName(it) },
            onReviewTextChange = { viewModel.updateReviewText(it) },
            onSubmit = { viewModel.submitReview() },
            onDismiss = { viewModel.closeReviewDialog() }
        )

        // Global Certificate Modal
        CertificateDialog(
            course = certificateCourse,
            onDismiss = { viewModel.dismissCertificate() }
        )
    }
}
