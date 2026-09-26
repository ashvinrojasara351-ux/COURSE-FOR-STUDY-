package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.LessonProgress
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserNote
import com.example.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    EXPLORE,
    MY_LEARNING,
    CREATOR_STUDIO,
    TRANSACTIONS
}

data class CheckoutUiState(
    val course: Course? = null,
    val isVisible: Boolean = false,
    val paymentMethod: String = "Credit / Debit Card",
    val cardNumber: String = "4242 •••• •••• 4242",
    val cardHolder: String = "Ashvin Rojasara",
    val cardExpiry: String = "08/29",
    val cardCvv: String = "889",
    val promoCode: String = "",
    val discount: Double = 0.0,
    val promoAppliedMessage: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: PaymentTransaction? = null,
    val isSuccess: Boolean = false
)

data class ReviewDialogUiState(
    val courseId: String = "",
    val courseTitle: String = "",
    val isVisible: Boolean = false,
    val rating: Int = 5,
    val studentName: String = "Ashvin Rojasara",
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CourseRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CourseRepository(db.courseDao())
        viewModelScope.launch {
            repository.ensureInitialDataLoaded()
        }
    }

    // Navigation & View State
    private val _currentScreen = MutableStateFlow(Screen.EXPLORE)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()

    private val _activeLessonId = MutableStateFlow<String?>(null)
    val activeLessonId: StateFlow<String?> = _activeLessonId.asStateFlow()

    private val _isPlayingVideo = MutableStateFlow(false)
    val isPlayingVideo: StateFlow<Boolean> = _isPlayingVideo.asStateFlow()

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Data streams
    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEnrollments: StateFlow<List<Enrollment>> = repository.allEnrollments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<PaymentTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered courses based on search & category
    val filteredCourses: StateFlow<List<Course>> = combine(
        allCourses,
        _searchQuery,
        _selectedCategory
    ) { courses, query, category ->
        courses.filter { course ->
            val matchesQuery = query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.description.contains(query, ignoreCase = true) ||
                    course.instructorName.contains(query, ignoreCase = true) ||
                    course.tags.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || course.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Enrolled courses details
    val enrolledCourses: StateFlow<List<Pair<Course, Enrollment>>> = combine(
        allCourses,
        allEnrollments
    ) { courses, enrollments ->
        enrollments.mapNotNull { enrollment ->
            val course = courses.find { it.id == enrollment.courseId }
            if (course != null) course to enrollment else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Checkout State
    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    // Review Dialog State
    private val _reviewDialogState = MutableStateFlow(ReviewDialogUiState())
    val reviewDialogState: StateFlow<ReviewDialogUiState> = _reviewDialogState.asStateFlow()

    // Certificate Viewer
    private val _certificateCourse = MutableStateFlow<Course?>(null)
    val certificateCourse: StateFlow<Course?> = _certificateCourse.asStateFlow()

    // Navigation actions
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openCourseDetail(courseId: String) {
        _selectedCourseId.value = courseId
    }

    fun closeCourseDetail() {
        _selectedCourseId.value = null
    }

    fun openVideoPlayer(lessonId: String) {
        _activeLessonId.value = lessonId
        _isPlayingVideo.value = true
    }

    fun closeVideoPlayer() {
        _activeLessonId.value = null
        _isPlayingVideo.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Repository getters for specific details
    fun getLessonsForCourse(courseId: String) = repository.getLessons(courseId)
    fun getLesson(lessonId: String) = repository.getLesson(lessonId)
    fun getEnrollment(courseId: String) = repository.getEnrollment(courseId)
    fun getLessonProgressForCourse(courseId: String) = repository.getLessonProgressForCourse(courseId)
    fun getLessonNotes(lessonId: String) = repository.getLessonNotes(lessonId)
    fun getCourseReviews(courseId: String) = repository.getCourseReviews(courseId)

    // Lesson Progress & Notes
    fun markLessonWatched(courseId: String, lessonId: String, watchPosSec: Long, markCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateLessonWatchProgress(courseId, lessonId, watchPosSec, markCompleted)
        }
    }

    fun addNote(courseId: String, lessonId: String, timestampSec: Int, noteText: String) {
        viewModelScope.launch {
            repository.addNote(courseId, lessonId, timestampSec, noteText)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    // Reviews System
    fun openReviewDialog(course: Course) {
        _reviewDialogState.value = ReviewDialogUiState(
            courseId = course.id,
            courseTitle = course.title,
            isVisible = true,
            rating = 5,
            studentName = "Ashvin Rojasara",
            reviewText = ""
        )
    }

    fun updateReviewRating(rating: Int) {
        _reviewDialogState.value = _reviewDialogState.value.copy(rating = rating.coerceIn(1, 5))
    }

    fun updateReviewText(text: String) {
        _reviewDialogState.value = _reviewDialogState.value.copy(reviewText = text)
    }

    fun updateReviewStudentName(name: String) {
        _reviewDialogState.value = _reviewDialogState.value.copy(studentName = name)
    }

    fun closeReviewDialog() {
        _reviewDialogState.value = _reviewDialogState.value.copy(isVisible = false)
    }

    fun submitReview() {
        val state = _reviewDialogState.value
        if (state.reviewText.trim().length < 5) {
            _reviewDialogState.value = state.copy(errorMessage = "Please enter at least 5 characters for your review.")
            return
        }

        _reviewDialogState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            repository.addReview(
                courseId = state.courseId,
                studentName = state.studentName,
                rating = state.rating,
                reviewText = state.reviewText.trim()
            )
            _reviewDialogState.value = ReviewDialogUiState(isVisible = false)
        }
    }

    fun markReviewHelpful(reviewId: String) {
        viewModelScope.launch {
            repository.incrementReviewHelpful(reviewId)
        }
    }

    // Checkout & Payment
    fun openCheckout(course: Course) {
        _checkoutState.value = CheckoutUiState(
            course = course,
            isVisible = true,
            promoCode = "",
            discount = 0.0,
            promoAppliedMessage = null,
            isProcessing = false,
            isSuccess = false
        )
    }

    fun closeCheckout() {
        _checkoutState.value = _checkoutState.value.copy(isVisible = false)
    }

    fun setPaymentMethod(method: String) {
        _checkoutState.value = _checkoutState.value.copy(paymentMethod = method)
    }

    fun applyPromoCode(code: String) {
        val course = _checkoutState.value.course ?: return
        val trimmed = code.trim().uppercase()
        when (trimmed) {
            "LEARN2026", "EDUPULSE" -> {
                val discount = (course.price * 0.20 * 100).toInt() / 100.0
                _checkoutState.value = _checkoutState.value.copy(
                    promoCode = trimmed,
                    discount = discount,
                    promoAppliedMessage = "20% discount applied ($${String.format("%.2f", discount)} off)!"
                )
            }
            "FIRST10" -> {
                val discount = 10.0.coerceAtMost(course.price * 0.5)
                _checkoutState.value = _checkoutState.value.copy(
                    promoCode = trimmed,
                    discount = discount,
                    promoAppliedMessage = "$10 instant welcome discount applied!"
                )
            }
            else -> {
                _checkoutState.value = _checkoutState.value.copy(
                    promoAppliedMessage = "Invalid code. Try 'LEARN2026' for 20% off."
                )
            }
        }
    }

    fun processPayment() {
        val current = _checkoutState.value
        val course = current.course ?: return

        _checkoutState.value = current.copy(isProcessing = true)

        viewModelScope.launch {
            // Simulate 3D Secure / token authorization handshake (800ms)
            kotlinx.coroutines.delay(800)

            val txn = repository.processCoursePurchase(
                course = course,
                paymentMethodName = current.paymentMethod,
                cardLast4 = if (current.paymentMethod.contains("Card")) "4242" else "GPay",
                discountAmount = current.discount
            )

            _checkoutState.value = _checkoutState.value.copy(
                isProcessing = false,
                isSuccess = true,
                completedTransaction = txn
            )
        }
    }

    // Creator Studio: Publish New Course
    fun publishCourse(
        title: String,
        subtitle: String,
        description: String,
        category: String,
        price: Double,
        instructorName: String,
        lessonsList: List<Lesson>
    ) {
        viewModelScope.launch {
            val courseId = "course-user-" + UUID.randomUUID().toString().take(6)
            val newCourse = Course(
                id = courseId,
                title = title,
                subtitle = subtitle,
                description = description,
                instructorName = instructorName.ifBlank { "Ashvin Rojasara" },
                instructorTitle = "Course Author & Verified Educator",
                category = category,
                level = "All Levels",
                rating = 5.0,
                reviewCount = 0,
                enrolledCount = 0,
                price = price,
                originalPrice = price * 1.5,
                durationHours = (lessonsList.sumOf { it.durationMinutes } / 60.0).coerceAtLeast(1.0),
                lessonsCount = lessonsList.size.coerceAtLeast(1),
                thumbnailName = "img_course_ai",
                isBestseller = false,
                isFeatured = false,
                isUserCreated = true,
                tags = "$category, Online Course, EduPulse Hosted",
                skillsLearned = "Industry-standard skills;Real-world project implementation;End-to-end practical mastery",
                videoHostingBadge = "EduPulse Cloud HLS (CDN 4K • Encrypted)"
            )

            val adjustedLessons = lessonsList.mapIndexed { index, lesson ->
                lesson.copy(
                    id = "$courseId-l${index + 1}",
                    courseId = courseId,
                    lessonIndex = index + 1
                )
            }

            repository.publishCourse(newCourse, adjustedLessons)
            _selectedCourseId.value = courseId
        }
    }

    // Certificate Viewer
    fun viewCertificate(course: Course) {
        _certificateCourse.value = course
    }

    fun dismissCertificate() {
        _certificateCourse.value = null
    }
}
