package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val instructorName: String,
    val instructorTitle: String,
    val category: String,
    val level: String,
    val rating: Double,
    val reviewCount: Int,
    val enrolledCount: Int,
    val price: Double,
    val originalPrice: Double,
    val durationHours: Double,
    val lessonsCount: Int,
    val thumbnailName: String, // e.g. "img_course_ai", "img_hero_banner"
    val isBestseller: Boolean = false,
    val isFeatured: Boolean = false,
    val isUserCreated: Boolean = false,
    val tags: String = "",
    val skillsLearned: String = "",
    val videoHostingBadge: String = "EduPulse Cloud HLS (CDN 4K/60)"
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val moduleTitle: String,
    val moduleIndex: Int,
    val lessonIndex: Int,
    val title: String,
    val durationMinutes: Int,
    val videoStreamUrl: String,
    val isFreePreview: Boolean = false,
    val description: String = "",
    val resolutionBadge: String = "1080p 60fps HD"
)

@Entity(tableName = "enrollments")
data class Enrollment(
    @PrimaryKey
    val courseId: String,
    val enrolledAt: Long = System.currentTimeMillis(),
    val progressPercent: Int = 0,
    val lastLessonId: String = "",
    val isCompleted: Boolean = false,
    val certificateId: String? = null,
    val certificateIssueDate: Long? = null
)

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey
    val id: String, // "$courseId-$lessonId"
    val courseId: String,
    val lessonId: String,
    val isCompleted: Boolean = false,
    val watchPositionSec: Long = 0,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_notes")
data class UserNote(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val lessonId: String,
    val timestampSec: Int,
    val noteText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey
    val transactionId: String,
    val courseId: String,
    val courseTitle: String,
    val amount: Double,
    val discount: Double,
    val tax: Double,
    val totalPaid: Double,
    val currency: String = "USD",
    val paymentMethod: String,
    val cardLast4: String,
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis(),
    val invoiceNumber: String
)

@Entity(tableName = "course_reviews")
data class CourseReview(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val studentName: String,
    val rating: Int, // 1 to 5
    val reviewText: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerifiedBuyer: Boolean = true,
    val helpfulCount: Int = 0
)
