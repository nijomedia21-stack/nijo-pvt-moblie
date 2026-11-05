package com.example

import com.example.ui.viewmodel.AppWorkspaceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWorkspaceModes() {
    val studyMode = AppWorkspaceMode.STUDY_POLYTECHNIC
    val designerMode = AppWorkspaceMode.FREELANCE_DESIGNER

    assertNotNull(studyMode)
    assertNotNull(designerMode)
    assertEquals(2, AppWorkspaceMode.entries.size)
  }
}
