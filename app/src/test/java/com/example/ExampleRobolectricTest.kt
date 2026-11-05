package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("NIJO PVT", appName)
  }

  @Test
  fun `cleanHostAddress parses various QR payloads correctly`() {
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("http://192.168.1.2:4000"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("192.168.1.2:4000"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("http://192.168.1.2:4000/upload"))
    assertEquals("192.168.1.5:4000", com.example.ui.components.cleanHostAddress("https://192.168.1.5:4000/events/"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("http://192.168.1.2:4000/status"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("/ 192.168.1.2:4000"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("/192.168.1.2:4000"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("http:///192.168.1.2:4000/"))
    assertEquals("192.168.1.2:4000", com.example.ui.components.cleanHostAddress("192.168.1.2"))
    assertEquals("192.168.1.5:4000", com.example.ui.components.cleanHostAddress("http://192.168.1.5"))
  }

  @Test
  fun `buildFullUrl prevents URL duplication and splitting`() {
    assertEquals("http://192.168.1.2:4000/upload", com.example.ui.components.buildFullUrl("/ 192.168.1.2:4000", "/upload"))
    assertEquals("http://192.168.1.2:4000/upload", com.example.ui.components.buildFullUrl("192.168.1.2:4000/", "upload"))
    assertEquals("http://192.168.1.2:4000/upload", com.example.ui.components.buildFullUrl("http://192.168.1.2:4000/upload", "/upload"))
    assertEquals("http://192.168.1.2:4000/events", com.example.ui.components.buildFullUrl("/192.168.1.2:4000/", "/events"))
    assertEquals("http://192.168.1.2:4000/upload", com.example.ui.components.buildFullUrl("192.168.1.2", "/upload"))
  }

  @Test
  fun `formatUserFriendlyError sanitizes raw java socket errors`() {
    val rawSocketErr = "Failed to connect to /192.168.1.2:4000"
    val friendly = com.example.ui.components.formatUserFriendlyError(rawSocketErr, "192.168.1.2:4000")
    org.junit.Assert.assertFalse(friendly.contains("to /192.168.1.2:4000"))
    org.junit.Assert.assertFalse(friendly.contains("/ 192.168.1.2:4000"))
    org.junit.Assert.assertFalse(friendly.contains("connect to /"))
    org.junit.Assert.assertTrue(friendly.contains("http://192.168.1.2:4000"))
  }

  @Test
  fun `host ip persists in shared preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("nijo_file_sharing_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("saved_host_ip", "192.168.1.2:4000").commit()
    assertEquals("192.168.1.2:4000", prefs.getString("saved_host_ip", null))
  }
}
