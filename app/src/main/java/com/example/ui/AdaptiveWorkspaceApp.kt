package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.components.DeviceDisplayMode
import com.example.ui.components.DeviceSyncStatusHeader
import com.example.ui.components.FilesScreen
import com.example.ui.components.FreelanceDesignScreen
import com.example.ui.components.MalayalamCalendarScreen
import com.example.ui.components.StudyScreen
import com.example.ui.components.TasksScreen
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.theme.VividVioletLight
import com.example.ui.viewmodel.AppWorkspaceMode
import com.example.ui.viewmodel.WorkspaceViewModel

enum class NijoTab {
  TODAY,
  CALENDAR,
  FILES,
  STUDY
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdaptiveWorkspaceApp(
  workspaceViewModel: WorkspaceViewModel,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(NijoTab.TODAY) }
  var aiCustomName by remember { mutableStateOf("Nijo Copilot") }

  val currentWorkspaceMode by workspaceViewModel.workspaceMode.collectAsState()
  val transferPin by workspaceViewModel.transferSecurityPin.collectAsState()
  val isDesignMode = currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER

  var showModeSwitcherSheet by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(CleanBackground)
      .statusBarsPadding()
      .navigationBarsPadding(),
    containerColor = CleanBackground,
    topBar = {
      // Top Status Header with Logo </>, Dual Mode Switcher pill, and Settings button
      DeviceSyncStatusHeader(
        currentMode = DeviceDisplayMode.MOBILE,
        workspaceMode = currentWorkspaceMode,
        onModeChanged = { /* Dedicated Mobile App */ },
        onOpenModeSwitcher = { showModeSwitcherSheet = true },
        onOpenSettings = { showSettingsDialog = true },
        onSignOutClicked = onSignOut
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = CleanSurface,
        contentColor = TextPrimary,
        modifier = Modifier
          .border(1.dp, CleanOutline)
          .testTag("mobile_bottom_nav")
      ) {
        NavigationBarItem(
          selected = selectedTab == NijoTab.TODAY,
          onClick = { selectedTab = NijoTab.TODAY },
          icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
          label = { Text("Today", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavyPrimary,
            selectedTextColor = NavyPrimary,
            indicatorColor = NavyLight,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
          ),
          modifier = Modifier.testTag("nav_tab_today")
        )

        NavigationBarItem(
          selected = selectedTab == NijoTab.CALENDAR,
          onClick = { selectedTab = NijoTab.CALENDAR },
          icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
          label = { Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavyPrimary,
            selectedTextColor = NavyPrimary,
            indicatorColor = NavyLight,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
          ),
          modifier = Modifier.testTag("nav_tab_calendar")
        )

        NavigationBarItem(
          selected = selectedTab == NijoTab.FILES,
          onClick = { selectedTab = NijoTab.FILES },
          icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
          label = { Text("Files", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavyPrimary,
            selectedTextColor = NavyPrimary,
            indicatorColor = NavyLight,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
          ),
          modifier = Modifier.testTag("nav_tab_files")
        )

        NavigationBarItem(
          selected = selectedTab == NijoTab.STUDY,
          onClick = { selectedTab = NijoTab.STUDY },
          icon = {
            Box(
              modifier = Modifier.combinedClickable(
                onClick = { selectedTab = NijoTab.STUDY },
                onLongClick = { showModeSwitcherSheet = true }
              )
            ) {
              Icon(
                if (isDesignMode) Icons.Default.Palette else Icons.Default.School,
                contentDescription = if (isDesignMode) "Design" else "Study"
              )
            }
          },
          label = {
            Text(
              if (isDesignMode) "Design" else "Study",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = if (isDesignMode) VividViolet else NavyPrimary,
            selectedTextColor = if (isDesignMode) VividViolet else NavyPrimary,
            indicatorColor = if (isDesignMode) VividVioletLight else NavyLight,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
          ),
          modifier = Modifier
            .combinedClickable(
              onClick = { selectedTab = NijoTab.STUDY },
              onLongClick = { showModeSwitcherSheet = true }
            )
            .testTag("nav_tab_study")
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Main Content Switching across the 4 modules
      when (selectedTab) {
        NijoTab.TODAY -> TasksScreen(viewModel = workspaceViewModel)
        NijoTab.CALENDAR -> MalayalamCalendarScreen(viewModel = workspaceViewModel)
        NijoTab.FILES -> FilesScreen(viewModel = workspaceViewModel)
        NijoTab.STUDY -> {
          if (isDesignMode) {
            FreelanceDesignScreen(viewModel = workspaceViewModel)
          } else {
            StudyScreen(viewModel = workspaceViewModel)
          }
        }
      }

      // Floating Custom-Named AI Assistant Widget
      CustomNamedAiFloatingWidget(
        aiName = aiCustomName,
        onAiNameChange = { aiCustomName = it },
        viewModel = workspaceViewModel,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = 16.dp)
      )
    }
  }

  // Instagram-Style Dual Mode Switcher Modal Dialog
  if (showModeSwitcherSheet) {
    AlertDialog(
      onDismissRequest = { showModeSwitcherSheet = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Switch Workspace (Dual Mode)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Switch instantly between Polytechnic AI&ML Study Mode and Freelance Designer Studio:",
            fontSize = 12.sp,
            color = TextSecondary
          )

          // Account 1: Polytechnic AI/ML Study Mode
          Surface(
            color = if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) NavyLight else CleanSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
              if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) 2.dp else 1.dp,
              if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) NavyPrimary else CleanOutline
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  workspaceViewModel.switchWorkspaceMode(AppWorkspaceMode.STUDY_POLYTECHNIC)
                  Toast.makeText(context, "Switched to Polytechnic Study Mode", Toast.LENGTH_SHORT).show()
                  showModeSwitcherSheet = false
                }
              )
              .testTag("switch_to_study_mode")
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(NavyPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text("🎓", fontSize = 18.sp)
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Nijo — Polytechnic AI & ML Study",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Sem 1: Maths I, Chemistry, PSPP, Physics (Deadline: Nov 18)",
                  fontSize = 11.sp,
                  color = NavyPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              }

              if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = NavyPrimary)
              }
            }
          }

          // Account 2: Freelance Designer & SMM Mode
          Surface(
            color = if (currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER) VividVioletLight else CleanSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
              if (currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER) 2.dp else 1.dp,
              if (currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER) VividViolet else CleanOutline
            ),
            modifier = Modifier
              .fillMaxWidth()
              .combinedClickable(
                onClick = {
                  workspaceViewModel.switchWorkspaceMode(AppWorkspaceMode.FREELANCE_DESIGNER)
                  Toast.makeText(context, "Switched to Freelance Designer (Libra) Studio", Toast.LENGTH_SHORT).show()
                  showModeSwitcherSheet = false
                }
              )
              .testTag("switch_to_designer_mode")
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(VividViolet, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text("🎨", fontSize = 18.sp)
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Nijo — Freelance Designer & SMM",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Client: Libra Brand Master Kit | Instagram Reels & AI Prompts",
                  fontSize = 11.sp,
                  color = VividViolet,
                  fontWeight = FontWeight.SemiBold
                )
              }

              if (currentWorkspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = VividViolet)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showModeSwitcherSheet = false }) {
          Text("Close", color = TextMuted, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Clean Minimalist Settings Dialog (Strictly English & Navy Blue)
  if (showSettingsDialog) {
    var pinToEdit by remember { mutableStateOf(transferPin) }

    AlertDialog(
      onDismissRequest = { showSettingsDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Settings, contentDescription = null, tint = NavyPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Settings & Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Security Transfer PIN:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          OutlinedTextField(
            value = pinToEdit,
            onValueChange = { pinToEdit = it },
            label = { Text("4-digit PIN (e.g. 8492)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = CleanOutline
            ),
            modifier = Modifier.fillMaxWidth().testTag("settings_pin_input")
          )

          Button(
            onClick = {
              workspaceViewModel.updateTransferSecurityPin(pinToEdit.trim())
              Toast.makeText(context, "Security PIN updated", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth().testTag("save_settings_pin_btn")
          ) {
            Text("Save PIN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text("App Information:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

          Surface(
            color = CleanSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("NIJO PVT Workspace (Mobile Edition)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("Version 2.4.0 • Dedicated Personal App", fontSize = 10.5.sp, color = TextSecondary)
              Text("Modules: Today (Tasks) · Calendar · Files · Study & Design", fontSize = 10.5.sp, color = TextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          OutlinedButton(
            onClick = {
              onSignOut()
              showSettingsDialog = false
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth().testTag("settings_sign_out_btn")
          ) {
            Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSettingsDialog = false }) {
          Text("Done", color = NavyPrimary, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
