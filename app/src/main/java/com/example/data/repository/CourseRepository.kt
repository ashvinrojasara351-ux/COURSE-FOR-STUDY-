package com.example.data.repository

import com.example.data.InitialData
import com.example.data.local.CourseDao
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.LessonProgress
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CourseRepository(private val courseDao: CourseDao) {

    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val allEnrollments: Flow<List<Enrollment>> = courseDao.getAllEnrollments()
    val allTransactions: Flow<List<PaymentTransaction>> = courseDao.getAllTransactions()

    fun getCourse(courseId: String): Flow<Course?> = courseDao.getCourseById(courseId)

    fun getLessons(courseId: String): Flow<List<Lesson>> = courseDao.getLessonsForCourse(courseId)

    fun getLesson(lessonId: String): Flow<Lesson?> = courseDao.getLessonById(lessonId)

    fun getEnrollment(courseId: String): Flow<Enrollment?> = courseDao.getEnrollment(courseId)

    fun getLessonProgressForCourse(courseId: String): Flow<List<LessonProgress>> =
        courseDao.getLessonProgressForCourse(courseId)

    fun getLessonNotes(lessonId: String): Flow<List<UserNote>> =
        courseDao.getNotesForLesson(lessonId)

    fun getCourseReviews(courseId: String): Flow<List<CourseReview>> =
        courseDao.getReviewsForCourse(courseId)

    suspend fun ensureInitialDataLoaded() {
        if (courseDao.getCourseCount() == 0) {
            courseDao.insertCourses(InitialData.sampleCourses)
            courseDao.insertLessons(InitialData.sampleLessons)
            courseDao.insertReviews(InitialData.sampleReviews)

            // Seed an active enrollment with progress for immediate rich demonstration
            val sampleEnrollment = Enrollment(
                courseId = "course-ai-agents",
                enrolledAt = System.currentTimeMillis() - 86400000L * 4,
                progressPercent = 25,
                lastLessonId = "ai-2",
                isCompleted = false
            )
            courseDao.insertEnrollment(sampleEnrollment)
            courseDao.insertLessonProgress(
                LessonProgress(
                    id = "course-ai-agents-ai-1",
                    courseId = "course-ai-agents",
                    lessonId = "ai-1",
                    isCompleted = true,
                    watchPositionSec = 1080,
                    lastWatchedAt = System.currentTimeMillis() - 86400000L * 2
                )
            )
            courseDao.insertNote(
                UserNote(
                    id = "note-sample-1",
                    courseId = "course-ai-agents",
                    lessonId = "ai-1",
                    timestampSec = 345,
                    noteText = "Key takeaway: Multi-turn agents require tool state grounding to prevent hallucinated argument passing.",
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                )
            )

            // Seed transaction for this purchase
            val sampleTxn = PaymentTransaction(
                transactionId = "TXN-2026-98104",
                courseId = "course-ai-agents",
                courseTitle = "Production AI Agents & LLM Orchestration",
                amount = 59.99,
                discount = 0.0,
                tax = 3.60,
                totalPaid = 63.59,
                paymentMethod = "Visa ending in 4242",
                cardLast4 = "4242",
                status = "COMPLETED",
                timestamp = System.currentTimeMillis() - 86400000L * 4,
                invoiceNumber = "INV-2026-0881"
            )
            courseDao.insertTransaction(sampleTxn)
        }
    }

    suspend fun processCoursePurchase(
        course: Course,
        paymentMethodName: String,
        cardLast4: String,
        discountAmount: Double
    ): PaymentTransaction {
        val tax = (course.price - discountAmount) * 0.06
        val totalPaid = (course.price - discountAmount + tax).coerceAtLeast(0.0)
        val txnId = "TXN-" + System.currentTimeMillis().toString().takeLast(6)
        val invoiceNo = "INV-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + "-" + (100..999).random()

        val transaction = PaymentTransaction(
            transactionId = txnId,
            courseId = course.id,
            courseTitle = course.title,
            amount = course.price,
            discount = discountAmount,
            tax = tax,
            totalPaid = totalPaid,
            paymentMethod = paymentMethodName,
            cardLast4 = cardLast4,
            status = "COMPLETED",
            timestamp = System.currentTimeMillis(),
            invoiceNumber = invoiceNo
        )
        courseDao.insertTransaction(transaction)

        val enrollment = Enrollment(
            courseId = course.id,
            enrolledAt = System.currentTimeMillis(),
            progressPercent = 0,
            lastLessonId = "",
            isCompleted = false
        )
        courseDao.insertEnrollment(enrollment)

        return transaction
    }

    suspend fun updateLessonWatchProgress(
        courseId: String,
        lessonId: String,
        watchPositionSec: Long,
        markAsCompleted: Boolean
    ) {
        val progressId = "$courseId-$lessonId"
        val existing = courseDao.getLessonProgress(progressId).firstOrNull()
        val isDone = markAsCompleted || (existing?.isCompleted == true)
        courseDao.insertLessonProgress(
            LessonProgress(
                id = progressId,
                courseId = courseId,
                lessonId = lessonId,
                isCompleted = isDone,
                watchPositionSec = watchPositionSec,
                lastWatchedAt = System.currentTimeMillis()
            )
        )

        // Recalculate enrollment progress
        val allLessons = courseDao.getLessonsForCourse(courseId).firstOrNull() ?: emptyList()
        val allProgress = courseDao.getLessonProgressForCourse(courseId).firstOrNull() ?: emptyList()
        val completedCount = allProgress.count { it.isCompleted }
        val percent = if (allLessons.isNotEmpty()) {
            ((completedCount.toFloat() / allLessons.size) * 100).toInt().coerceIn(0, 100)
        } else 0

        val currentEnrollment = courseDao.getEnrollment(courseId).firstOrNull()
        val isNowComplete = percent >= 100
        val certId = if (isNowComplete && currentEnrollment?.certificateId == null) {
            "CERT-EDUPULSE-" + UUID.randomUUID().toString().take(8).uppercase()
        } else currentEnrollment?.certificateId

        val certDate = if (isNowComplete && currentEnrollment?.certificateIssueDate == null) {
            System.currentTimeMillis()
        } else currentEnrollment?.certificateIssueDate

        val updatedEnrollment = Enrollment(
            courseId = courseId,
            enrolledAt = currentEnrollment?.enrolledAt ?: System.currentTimeMillis(),
            progressPercent = percent,
            lastLessonId = lessonId,
            isCompleted = isNowComplete,
            certificateId = certId,
            certificateIssueDate = certDate
        )
        courseDao.insertEnrollment(updatedEnrollment)
    }

    suspend fun addReview(
        courseId: String,
        studentName: String,
        rating: Int,
        reviewText: String
    ) {
        val review = CourseReview(
            id = UUID.randomUUID().toString(),
            courseId = courseId,
            studentName = studentName.ifBlank { "Verified Learner" },
            rating = rating.coerceIn(1, 5),
            reviewText = reviewText,
            createdAt = System.currentTimeMillis(),
            isVerifiedBuyer = true,
            helpfulCount = 0
        )
        courseDao.insertReview(review)

        // Dynamically recalculate average rating for this course
        val currentReviews = courseDao.getReviewsForCourse(courseId).firstOrNull() ?: listOf(review)
        val totalReviews = currentReviews + review
        val avgRating = ((totalReviews.map { it.rating }.average()) * 10).toInt() / 10.0
        val count = totalReviews.size
        courseDao.updateCourseRating(courseId, avgRating, count)
    }

    suspend fun incrementReviewHelpful(reviewId: String) {
        courseDao.incrementHelpfulCount(reviewId)
    }

    suspend fun addNote(courseId: String, lessonId: String, timestampSec: Int, noteText: String) {
        val note = UserNote(
            id = UUID.randomUUID().toString(),
            courseId = courseId,
            lessonId = lessonId,
            timestampSec = timestampSec,
            noteText = noteText,
            createdAt = System.currentTimeMillis()
        )
        courseDao.insertNote(note)
    }

    suspend fun deleteNote(noteId: String) {
        courseDao.deleteNote(noteId)
    }

    suspend fun publishCourse(course: Course, lessons: List<Lesson>) {
        courseDao.insertCourse(course)
        courseDao.insertLessons(lessons)
    }
}
