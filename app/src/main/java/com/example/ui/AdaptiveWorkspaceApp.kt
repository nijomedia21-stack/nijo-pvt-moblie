package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CustomNamedAiFloatingWidget
import com.example.ui.components.FilesScreen
import com.example.ui.components.FreelanceDesignScreen
import com.example.ui.components.HomeScreen
import com.example.ui.components.MalayalamCalendarScreen
import com.example.ui.components.StudyScreen
import com.example.ui.components.TasksScreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NeonMint
import com.example.ui.theme.OmniSyncTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividViolet
import com.example.ui.theme.VividVioletLight
import com.example.ui.viewmodel.AppWorkspaceMode
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.launch

enum class NijoTab(val title: String) {
  HOME("Home"),
  TASKS("Tasks"),
  CALENDAR("Calendar"),
  STUDY("Study"),
  FILES("Static Files")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdaptiveWorkspaceApp(
  workspaceViewModel: WorkspaceViewModel,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // 5 screens in exact user order: 0: HOME, 1: TASKS, 2: CALENDAR, 3: STUDY, 4: STATIC FILES
  val pagerState = rememberPagerState(initialPage = 0) { 5 }
  var aiCustomName by remember { mutableStateOf("Nijo Copilot") }
  var showAiChatDialog by remember { mutableStateOf(false) }

  // Lambda to trigger PDF upload in study screen
  var studyPdfUploadTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

  val currentWorkspaceMode by workspaceViewModel.workspaceMode.collectAsState()
  val remoteConfig by workspaceViewModel.remoteAppConfig.collectAsState()
  val isDesignMode = currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER

  OmniSyncTheme(
    primaryColorHex = remoteConfig.themeColorHex,
    fontStyle = remoteConfig.fontStyle
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .background(CleanBackground)
        .statusBarsPadding()
        .navigationBarsPadding(),
      containerColor = CleanBackground,
      bottomBar = {
        NavigationBar(
          containerColor = CleanSurface,
          contentColor = TextPrimary,
          modifier = Modifier
            .border(1.dp, CleanOutline)
            .testTag("mobile_bottom_nav")
        ) {
          // 0: HOME (ഹോം)
          NavigationBarItem(
            selected = pagerState.currentPage == 0,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(0)
              }
            },
            icon = {
              Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                "Home",
                fontSize = 11.sp,
                fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyPrimary,
              selectedTextColor = NavyPrimary,
              indicatorColor = NavyLight,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_home")
          )

          // 1: TASKS (ടാസ്ക്)
          NavigationBarItem(
            selected = pagerState.currentPage == 1,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(1)
              }
            },
            icon = {
              Icon(
                Icons.Default.TaskAlt,
                contentDescription = "Tasks",
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                "Tasks",
                fontSize = 11.sp,
                fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyPrimary,
              selectedTextColor = NavyPrimary,
              indicatorColor = NavyLight,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_tasks")
          )

          // 2: CALENDAR (കലണ്ടർ)
          NavigationBarItem(
            selected = pagerState.currentPage == 2,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(2)
              }
            },
            icon = {
              Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Calendar",
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                "Calendar",
                fontSize = 11.sp,
                fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyPrimary,
              selectedTextColor = NavyPrimary,
              indicatorColor = NavyLight,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_calendar")
          )

          // 3: STUDY (സ്റ്റഡി)
          NavigationBarItem(
            selected = pagerState.currentPage == 3,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(3)
              }
            },
            icon = {
              Icon(
                if (isDesignMode) Icons.Default.Palette else Icons.Default.School,
                contentDescription = if (isDesignMode) "Design" else "Study",
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                if (isDesignMode) "Design" else "Study",
                fontSize = 11.sp,
                fontWeight = if (pagerState.currentPage == 3) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = if (isDesignMode) VividViolet else NavyPrimary,
              selectedTextColor = if (isDesignMode) VividViolet else NavyPrimary,
              indicatorColor = if (isDesignMode) VividVioletLight else NavyLight,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_study")
          )

          // 4: STATIC FILES (സ്റ്റാറ്റിക് ഫയൽസ്)
          NavigationBarItem(
            selected = pagerState.currentPage == 4,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(4)
              }
            },
            icon = {
              Icon(
                Icons.Default.Folder,
                contentDescription = "Static Files",
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                "Files",
                fontSize = 11.sp,
                fontWeight = if (pagerState.currentPage == 4) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyPrimary,
              selectedTextColor = NavyPrimary,
              indicatorColor = NavyLight,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_files")
          )
        }
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        // Horizontal Swipe Pager enabling smooth swipe in exact user order
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize()
        ) { page ->
          when (page) {
            0 -> HomeScreen(
              viewModel = workspaceViewModel,
              onNavigateToTab = { targetIndex ->
                coroutineScope.launch {
                  pagerState.animateScrollToPage(targetIndex)
                }
              },
              onSignOut = onSignOut
            )
            1 -> TasksScreen(viewModel = workspaceViewModel)
            2 -> MalayalamCalendarScreen(viewModel = workspaceViewModel)
            3 -> {
              if (isDesignMode) {
                FreelanceDesignScreen(viewModel = workspaceViewModel)
              } else {
                StudyScreen(
                  viewModel = workspaceViewModel,
                  onRegisterUploadTrigger = { trigger ->
                    studyPdfUploadTrigger = trigger
                  }
                )
              }
            }
            4 -> FilesScreen(viewModel = workspaceViewModel)
          }
        }

        // Floating Action Controls:
        // Rule: Study Mode (tab 3) has AI + Plus 3-dot cluster; other screens have single icon
        if (pagerState.currentPage == 3 && !isDesignMode) {
          // Study Mode: AI and Plus icon combined in a sleek vertical 3-dot line
          StudyModeThreeDotActionLine(
            onOpenAi = { showAiChatDialog = true },
            onUploadPdf = { studyPdfUploadTrigger?.invoke() },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 16.dp, bottom = 16.dp)
          )
        } else {
          // Autopilot / other screens: Only a single clean icon
          SingleAutopilotAiIcon(
            onClick = { showAiChatDialog = true },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 16.dp, bottom = 16.dp)
          )
        }
      }
    }

    // AI Assistant Dialog triggered from either icon state
    if (showAiChatDialog) {
      CustomNamedAiFloatingWidget(
        aiName = aiCustomName,
        onAiNameChange = { aiCustomName = it },
        viewModel = workspaceViewModel,
        initialOpen = true,
        onDismiss = { showAiChatDialog = false }
      )
    }
  }
}

/**
 * Study Mode Floating Action Line:
 * Combines AI Tutor + 3 dots line + Plus (+) icon in a vertical stack.
 */
@Composable
private fun StudyModeThreeDotActionLine(
  onOpenAi: () -> Unit,
  onUploadPdf: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(26.dp),
    color = Color.White,
    shadowElevation = 6.dp,
    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary.copy(alpha = 0.3f)),
    modifier = modifier.testTag("study_three_dot_cluster")
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // 1. AI Tutor Icon Button
      Box(
        modifier = Modifier
          .size(42.dp)
          .background(NavyPrimary, CircleShape)
          .clickable { onOpenAi() }
          .testTag("study_ai_tutor_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.AutoAwesome,
          contentDescription = "AI Study Tutor",
          tint = NeonMint,
          modifier = Modifier.size(20.dp)
        )
      }

      // 2. Three Vertical Dots Line Indicator
      Icon(
        Icons.Default.MoreVert,
        contentDescription = null,
        tint = NavyPrimary.copy(alpha = 0.6f),
        modifier = Modifier.size(16.dp)
      )

      // 3. Plus (+) Syllabus PDF Upload Button
      Box(
        modifier = Modifier
          .size(42.dp)
          .background(NavyPrimary, CircleShape)
          .clickable { onUploadPdf() }
          .testTag("study_plus_upload_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Add,
          contentDescription = "Upload Syllabus PDF",
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

/**
 * Autopilot / Non-Study Mode:
 * Single clean circular floating AI icon.
 */
@Composable
private fun SingleAutopilotAiIcon(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = CircleShape,
    color = Color.White,
    shadowElevation = 6.dp,
    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary.copy(alpha = 0.3f)),
    modifier = modifier
      .size(48.dp)
      .clickable { onClick() }
      .testTag("single_autopilot_ai_fab")
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        Icons.Default.AutoAwesome,
        contentDescription = "AI Copilot",
        tint = NavyPrimary,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
