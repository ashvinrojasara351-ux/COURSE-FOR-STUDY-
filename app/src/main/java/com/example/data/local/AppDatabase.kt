package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.LessonProgress
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserNote

@Database(
    entities = [
        Course::class,
        Lesson::class,
        Enrollment::class,
        LessonProgress::class,
        UserNote::class,
        PaymentTransaction::class,
        CourseReview::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edupulse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
