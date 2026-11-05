package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SlateOutline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun TvFilesScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var actionStatusMessage by remember { mutableStateOf("TV File Sharing Ready. Press OK on remote to Send or Receive.") }
  var isReceivingMode by remember { mutableStateOf(true) }
  var tvBeamPin by remember { mutableStateOf("849-201") }
  var beamNotification by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(28.dp) // Large TV safe margin
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top TV Bar with Privacy Locked Badge & D-pad Guide
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "</>",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = ElectricCyan
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "NIJO PVT - Smart TV File Receiver",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .background(VividViolet.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                  .border(1.dp, VividViolet, RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("TV 10-FOOT UI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VividViolet)
              }
            }
            Text(
              text = "Android TV 14 (1080p) • 100% D-pad Remote Driven (Arrows & OK) • Wi-Fi & QuickShare",
              fontSize = 13.sp,
              color = TextMuted
            )
          }
        }

        // Privacy locked badge & QuickShare status
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
              .border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Sensors, contentDescription = "QuickShare", tint = ElectricCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("QuickShare TV Beam Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(SlateSurface, RoundedCornerShape(8.dp))
              .border(1.dp, NeonMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Lock, contentDescription = "Privacy locked", tint = NeonMint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Privacy locked",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NeonMint
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // TV QuickShare Hero Beam Card (Highlight for College Teachers/Tutors)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier
          .fillMaxWidth()
          .border(2.dp, if (isReceivingMode) ElectricCyan else SlateOutline, RoundedCornerShape(16.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Sensors, contentDescription = "Beam", tint = ElectricCyan, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "TV QuickShare Receiver PIN: ",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextSecondary
                )
                Text(
                  text = tvBeamPin,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp,
                  color = ElectricCyan
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isReceivingMode)
                  "Listening for files from Android Phone (Quick Share) or Windows 11 Laptop. Enter PIN $tvBeamPin or beam directly."
                else
                  "Receiver paused. Press 'Receive via QuickShare' to activate listener.",
                fontSize = 12.sp,
                color = if (isReceivingMode) NeonMint else TextMuted
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = {
                val p1 = (100..999).random()
                val p2 = (100..999).random()
                tvBeamPin = "$p1-$p2"
                actionStatusMessage = "New TV Beam PIN generated: $tvBeamPin"
              },
              colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Refresh PIN", fontSize = 11.sp, color = TextPrimary)
            }

            TvStatusPill(label = "Host: Laptop", isConnected = true)
            TvStatusPill(label = "Phone: Linked", isConnected = true)
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Primary D-pad Action Buttons: Send File & Receive via QuickShare
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        TvDpadButton(
          title = if (isReceivingMode) "QuickShare Receiver: ACTIVE" else "Activate QuickShare Receiver",
          subtitle = if (isReceivingMode) "Accepting files & clipboard pushed from Phone or Laptop" else "Press OK on remote to turn on TV receiver",
          icon = if (isReceivingMode) Icons.Default.Sensors else Icons.Default.FileDownload,
          accentColor = if (isReceivingMode) NeonMint else ElectricCyan,
          modifier = Modifier
            .weight(1.2f)
            .testTag("tv_receive_button"),
          onClick = {
            isReceivingMode = !isReceivingMode
            actionStatusMessage = if (isReceivingMode) {
              "QuickShare receiver active. Awaiting files from Phone/Laptop."
            } else {
              "Receiver stopped."
            }
          }
        )

        TvDpadButton(
          title = "Send TV Capture / Log",
          subtitle = "Push TV screen recording or debug report to Laptop",
          icon = Icons.Default.FileUpload,
          accentColor = VividViolet,
          modifier = Modifier
            .weight(1f)
            .testTag("tv_send_button"),
          onClick = {
            viewModel.createItem(
              WorkspaceItem(
                type = ItemType.MEDIA.name,
                title = "tv_display_capture_${System.currentTimeMillis() % 10000}.mp4",
                content = "Transmitted from Android TV 14 via QuickShare Beam",
                tags = listOf("file", "tv", "quickshare", "wifi_share")
              )
            )
            actionStatusMessage = "Dispatched TV capture to Laptop storage via QuickShare."
            beamNotification = "Sent tv_display_capture to Laptop Host!"
          }
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Beam notification if active
      beamNotification?.let { note ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(NeonMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, NeonMint, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(note, fontSize = 12.sp, color = NeonMint, fontWeight = FontWeight.Bold)
            Button(
              onClick = { beamNotification = null },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
              Text("Dismiss", fontSize = 11.sp, color = TextMuted)
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // Recent Shared Files (Focusable Rows with D-pad navigation)
      Text(
        text = "TV Shared File Stream (D-Pad Select & View)",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      when (val state = uiState) {
        is WorkspaceUiState.Loading -> {
          Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Loading shared files...", color = TextMuted, fontSize = 14.sp)
          }
        }
        is WorkspaceUiState.Error -> {
          Text("Transfer error: ${state.message}", color = Color.Red, fontSize = 13.sp)
        }
        is WorkspaceUiState.Success -> {
          val files = state.items.filter { it.type == ItemType.MEDIA.name }

          if (files.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SlateSurface, RoundedCornerShape(12.dp))
                .border(1.dp, SlateOutline, RoundedCornerShape(12.dp))
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No files received yet. Beam files from Phone Quick Share or Windows Laptop.",
                color = TextMuted,
                fontSize = 14.sp
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(files, key = { it.id }) { file ->
                TvFileRow(file = file)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TvDpadButton(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  var isFocused by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFocused) accentColor.copy(alpha = 0.25f) else SlateSurface
    ),
    modifier = modifier
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .border(
        width = if (isFocused) 3.dp else 1.dp,
        color = if (isFocused) accentColor else SlateOutline,
        shape = RoundedCornerShape(16.dp)
      )
  ) {
    Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) accentColor else TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }
  }
}

@Composable
private fun TvFileRow(file: WorkspaceItem) {
  var isFocused by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFocused) ElectricCyan.copy(alpha = 0.2f) else SlateSurface
    ),
    modifier = Modifier
      .fillMaxWidth()
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .border(
        width = if (isFocused) 2.dp else 1.dp,
        color = if (isFocused) ElectricCyan else SlateOutline,
        shape = RoundedCornerShape(10.dp)
      )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = file.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = if (isFocused) ElectricCyan else TextPrimary
        )
        if (file.content.isNotBlank()) {
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = file.content,
            fontSize = 12.sp,
            color = TextMuted,
            maxLines = 1
          )
        }
      }

      Box(
        modifier = Modifier
          .background(SlateSurfaceVariant, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "Local Wi-Fi Verified",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = NeonMint
        )
      }
    }
  }
}

@Composable
private fun TvStatusPill(label: String, isConnected: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(SlateSurfaceVariant, RoundedCornerShape(6.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(if (isConnected) NeonMint else TextMuted, RoundedCornerShape(4.dp))
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(label, fontSize = 11.sp, color = TextPrimary)
  }
}
