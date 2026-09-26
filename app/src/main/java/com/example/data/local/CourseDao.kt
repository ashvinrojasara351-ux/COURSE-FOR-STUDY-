package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.LessonProgress
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserNote
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // Courses
    @Query("SELECT * FROM courses ORDER BY isFeatured DESC, rating DESC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: String): Flow<Course?>

    @Query("SELECT * FROM courses WHERE isFeatured = 1")
    fun getFeaturedCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE category = :category ORDER BY rating DESC")
    fun getCoursesByCategory(category: String): Flow<List<Course>>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourse(courseId: String)

    // Lessons
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY moduleIndex ASC, lessonIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: String): Flow<Lesson?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    // Enrollments
    @Query("SELECT * FROM enrollments ORDER BY enrolledAt DESC")
    fun getAllEnrollments(): Flow<List<Enrollment>>

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId")
    fun getEnrollment(courseId: String): Flow<Enrollment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: Enrollment)

    @Update
    suspend fun updateEnrollment(enrollment: Enrollment)

    // Lesson Progress
    @Query("SELECT * FROM lesson_progress WHERE courseId = :courseId")
    fun getLessonProgressForCourse(courseId: String): Flow<List<LessonProgress>>

    @Query("SELECT * FROM lesson_progress WHERE id = :progressId")
    fun getLessonProgress(progressId: String): Flow<LessonProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonProgress(progress: LessonProgress)

    // Notes
    @Query("SELECT * FROM user_notes WHERE lessonId = :lessonId ORDER BY timestampSec ASC")
    fun getNotesForLesson(lessonId: String): Flow<List<UserNote>>

    @Query("SELECT * FROM user_notes WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getNotesForCourse(courseId: String): Flow<List<UserNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNote)

    @Query("DELETE FROM user_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    // Transactions
    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions WHERE transactionId = :id")
    fun getTransactionById(id: String): Flow<PaymentTransaction?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransaction)

    // Reviews
    @Query("SELECT * FROM course_reviews WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getReviewsForCourse(courseId: String): Flow<List<CourseReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: com.example.data.model.CourseReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<com.example.data.model.CourseReview>)

    @Query("UPDATE courses SET rating = :newRating, reviewCount = :newCount WHERE id = :courseId")
    suspend fun updateCourseRating(courseId: String, newRating: Double, newCount: Int)

    @Query("UPDATE course_reviews SET helpfulCount = helpfulCount + 1 WHERE id = :reviewId")
    suspend fun incrementHelpfulCount(reviewId: String)
}
