package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import need.rider.R
import org.junit.Assert.assertEquals
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
    assertEquals("Food Rider", appName)
  }

  @Test
  fun `launch main activity`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(need.rider.MainActivity::class.java)
    scenario.onActivity { activity ->
      org.junit.Assert.assertNotNull(activity)
    }
  }
}
