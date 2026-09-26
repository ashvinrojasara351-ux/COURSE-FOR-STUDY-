package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.InitialData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EduPulse", appName)
  }

  @Test
  fun `initial sample courses and reviews are populated`() {
    assertTrue(InitialData.sampleCourses.isNotEmpty())
    assertTrue(InitialData.sampleReviews.isNotEmpty())
    val sampleCourse = InitialData.sampleCourses.first()
    assertTrue(sampleCourse.price > 0)
    assertTrue(sampleCourse.rating in 1.0..5.0)
  }
}
