package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppWorkspaceMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class DeviceDisplayMode {
  MOBILE,
  TABLET_LAPTOP,
  TV_LEANBACK
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceSyncStatusHeader(
  currentMode: DeviceDisplayMode,
  workspaceMode: AppWorkspaceMode,
  onModeChanged: (DeviceDisplayMode) -> Unit,
  onOpenModeSwitcher: () -> Unit,
  onOpenSettings: () -> Unit,
  onSignOutClicked: () -> Unit
) {
  val currentUser = Firebase.auth.currentUser
  var showNotificationsDialog by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Surface(
    color = NavyPrimary,
    shape = RoundedCornerShape(14.dp),
    shadowElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, NavySecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Brand Logo: </> with Long Press / Click Instagram-style trigger
        Box(
          modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .combinedClickable(
              onClick = onOpenModeSwitcher,
              onLongClick = onOpenModeSwitcher
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("app_logo_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "</>",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "NIJO PVT",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .scale(pulseScale)
                .background(NeonMint, CircleShape)
            )
          }
          Text(
            text = currentUser?.email ?: "neecho39@gmail.com",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.75f),
            maxLines = 1
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Settings Button
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.size(32.dp).testTag("header_settings_button")
        ) {
          Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(17.dp))
        }

        // Notifications Button
        IconButton(
          onClick = { showNotificationsDialog = true },
          modifier = Modifier.size(32.dp).testTag("notifications_button")
        ) {
          Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(17.dp))
        }
      }
    }
  }

  // Fleet Notifications Dialog (Strictly English)
  if (showNotificationsDialog) {
    AlertDialog(
      onDismissRequest = { showNotificationsDialog = false },
      title = { Text("System Notifications", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          NotificationItemRow("Cloud Sync", "Mobile workspace synchronized with Google account.")
          NotificationItemRow("Study Schedule", "Exam prep active: Maths I, Chemistry, PSPP, Physics.")
          NotificationItemRow("File Transfer", "Local Wi-Fi and QuickShare transfer ready.")
        }
      },
      confirmButton = {
        Button(
          onClick = { showNotificationsDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun NotificationItemRow(title: String, desc: String) {
  Surface(
    color = CleanSurfaceVariant,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
      Text(desc, fontSize = 10.5.sp, color = TextSecondary)
    }
  }
}
