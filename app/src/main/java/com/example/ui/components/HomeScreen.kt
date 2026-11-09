package com.example.ui.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.NavyAccent
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppWorkspaceMode
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.MalayalamCalendarHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.LocalDate

@Composable
fun HomeScreen(
  viewModel: WorkspaceViewModel,
  onNavigateToTab: (Int) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val currentUser = Firebase.auth.currentUser
  val currentWorkspaceMode by viewModel.workspaceMode.collectAsState()
  val transferPin by viewModel.transferSecurityPin.collectAsState()
  val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

  var notificationsEnabled by remember { mutableStateOf(true) }

  val today = remember { LocalDate.now() }
  val malayalamToday = remember(today) {
    MalayalamCalendarHelper.getMalayalamDayInfo(today)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CleanBackground)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Sleek Minimalist Hero Profile Card (Lighter Royal Blue with Soft Gradient)
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(NavyPrimary, NavyAccent)
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .testTag("home_account_card")
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = currentUser?.displayName ?: "NIJO PVT Workspace",
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = currentUser?.email ?: "Personal Mobile Workspace",
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1
              )
            }
          }

          // Minimalist Sign Out Pill Button
          Surface(
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.clickable { onSignOut() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sign Out",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Sign Out",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Minimalist Bottom Info Row (Online Cloud Sync & Mode Pill)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(NeonMint, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (pendingSyncCount > 0) "$pendingSyncCount pending sync" else "Cloud Synced",
              fontSize = 11.sp,
              color = Color.White,
              fontWeight = FontWeight.Medium
            )
          }

          Text(
            text = if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) "Polytechnic AI/ML" else "Freelance Studio",
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 2. Minimalist Quick Access Grid (Clean, airy cards with generous touch targets)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Quick Navigation",
        fontSize = 14.sp,
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
      )
      Text(
        text = "5 Workspace Hubs",
        fontSize = 11.sp,
        color = TextMuted
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Tasks Tile (Tab 1)
      MinimalNavTile(
        title = "Tasks",
        subtitle = "To-dos & Audio",
        badge = "Daily Plan",
        icon = Icons.Default.CheckCircle,
        onClick = { onNavigateToTab(1) },
        modifier = Modifier.weight(1f)
      )

      // 2. Calendar Tile (Tab 2)
      MinimalNavTile(
        title = "Calendar",
        subtitle = "${malayalamToday.malayalamMonthName} ${malayalamToday.malayalamDayNumber}",
        badge = malayalamToday.nakshatraMalayalam,
        icon = Icons.Default.CalendarMonth,
        onClick = { onNavigateToTab(2) },
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 3. Study Tile (Tab 3)
      MinimalNavTile(
        title = "Study",
        subtitle = "AI/ML & Lab",
        badge = "Maths, PSPP",
        icon = Icons.Default.School,
        onClick = { onNavigateToTab(3) },
        modifier = Modifier.weight(1f)
      )

      // 4. Static Files Tile (Tab 4)
      MinimalNavTile(
        title = "Static Files",
        subtitle = "Wi-Fi Share",
        badge = "PIN: $transferPin",
        icon = Icons.Default.Folder,
        onClick = { onNavigateToTab(4) },
        modifier = Modifier.weight(1f)
      )
    }

    // 3. Minimalist Workspace Settings & Controls
    Text(
      text = "Workspace Controls",
      fontSize = 14.sp,
      fontFamily = ManropeFontFamily,
      fontWeight = FontWeight.Bold,
      color = NavyPrimary
    )

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = CleanSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, CleanOutline, RoundedCornerShape(16.dp))
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Mode Switcher Tile
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              val nextMode = if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) {
                AppWorkspaceMode.FREELANCE_DESIGNER
              } else {
                AppWorkspaceMode.STUDY_POLYTECHNIC
              }
              viewModel.switchWorkspaceMode(nextMode)
              Toast.makeText(context, "Mode: ${nextMode.name}", Toast.LENGTH_SHORT).show()
            }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NavyLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) Icons.Default.School else Icons.Default.Palette,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Dual Workspace Mode",
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (currentWorkspaceMode == AppWorkspaceMode.STUDY_POLYTECHNIC) "Polytechnic AI/ML Study" else "Freelance Designer Studio",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Surface(
            color = NavyLight,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("home_switch_mode_btn")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Switch",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
            }
          }
        }

        HorizontalDivider(color = CleanOutline.copy(alpha = 0.6f))

        // Wi-Fi PIN Token Tile
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              clipboardManager.setText(AnnotatedString(transferPin))
              Toast.makeText(context, "PIN $transferPin copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NavyLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Transfer PIN Token",
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Required for Wi-Fi Direct file send",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Surface(
            color = NavyLight,
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = transferPin,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = NavyPrimary,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }

        HorizontalDivider(color = CleanOutline.copy(alpha = 0.6f))

        // Device Storage Location
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NavyLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Local Storage",
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Downloads/NIJO_PVT",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Surface(
            color = Color(0xFFECFDF5),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Ready",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF047857),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        HorizontalDivider(color = CleanOutline.copy(alpha = 0.6f))

        // Notifications Toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(NavyLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Alerts & Notifications",
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Task & file transfer push banners",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Switch(
            checked = notificationsEnabled,
            onCheckedChange = {
              notificationsEnabled = it
              Toast.makeText(
                context,
                if (it) "Notifications Enabled" else "Notifications Muted",
                Toast.LENGTH_SHORT
              ).show()
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = NavyPrimary,
              uncheckedThumbColor = TextMuted,
              uncheckedTrackColor = CleanSurfaceVariant
            )
          )
        }
      }
    }

    // Sync Now Button
    Button(
      onClick = {
        viewModel.syncPendingChanges()
        Toast.makeText(context, "Workspace synchronized successfully!", Toast.LENGTH_SHORT).show()
      },
      colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("home_sync_now_btn")
    ) {
      Icon(
        imageVector = Icons.Default.CloudSync,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Sync Workspace to Cloud",
        fontSize = 13.sp,
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun MinimalNavTile(
  title: String,
  subtitle: String,
  badge: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = CleanSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
      .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(NavyLight, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NavyPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Surface(
          color = CleanSurfaceVariant,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = badge,
            fontSize = 9.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
          )
        }
      }

      Column {
        Text(
          text = title,
          fontSize = 13.sp,
          fontFamily = ManropeFontFamily,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = subtitle,
          fontSize = 10.5.sp,
          color = TextSecondary,
          maxLines = 1
        )
      }
    }
  }
}
