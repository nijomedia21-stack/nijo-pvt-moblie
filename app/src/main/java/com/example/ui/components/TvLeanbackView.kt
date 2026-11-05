package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
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
fun TvLeanbackView(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val itemsState by viewModel.itemsState.collectAsState()
  val items = when (val s = itemsState) {
    is WorkspaceUiState.Success -> s.items
    else -> emptyList()
  }

  val notes = items.filter { it.type == ItemType.NOTE.name }
  val tasks = items.filter { it.type == ItemType.TASK.name }
  val completedTasks = tasks.count { it.isCompleted }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090C12))
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Top Ambient Banner
    item {
      Surface(
        color = SlateSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, SlateOutline, RoundedCornerShape(20.dp))
          .testTag("tv_leanback_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = NeonMint.copy(alpha = 0.2f),
              modifier = Modifier.size(54.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = NeonMint, modifier = Modifier.size(28.dp))
              }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                text = "OmniSync TV Leanback Display",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Ambient multi-device workspace projection • Sub-second Firestore cloud sync",
                fontSize = 13.sp,
                color = TextMuted
              )
            }
          }

          // Stats counters
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TvStatPill(count = notes.size.toString(), label = "Notes", color = ElectricCyan)
            TvStatPill(count = "${tasks.size - completedTasks}/${tasks.size}", label = "Pending Tasks", color = VividViolet)
            TvStatPill(count = "ACTIVE", label = "Live Cloud Sync", color = NeonMint)
          }
        }
      }
    }

    // Active Notes Presentation Row
    item {
      Text(
        text = "Synchronized Workspace Notes",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = ElectricCyan
      )
    }

    if (notes.isEmpty()) {
      item {
        Text("No notes synchronized yet. Create a note on your phone or laptop to view it live here.", color = TextMuted, fontSize = 14.sp)
      }
    } else {
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(notes) { note ->
            TvNoteCard(note = note)
          }
        }
      }
    }

    // Tasks Overview Row
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Project Checklist & Milestones",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = VividViolet
      )
    }

    if (tasks.isEmpty()) {
      item {
        Text("No tasks active.", color = TextMuted, fontSize = 14.sp)
      }
    } else {
      items(tasks) { task ->
        TvTaskRow(task = task, onToggle = { viewModel.toggleTaskCompletion(task) })
      }
    }
  }
}

@Composable
private fun TvStatPill(count: String, label: String, color: Color) {
  Surface(
    color = Color(0xFF131926),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
      Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
  }
}

@Composable
private fun TvNoteCard(note: WorkspaceItem) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SlateSurface),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .width(320.dp)
      .height(200.dp)
      .border(1.dp, SlateOutline, RoundedCornerShape(18.dp))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = note.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          Icon(Icons.Default.Description, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = note.content,
          fontSize = 13.sp,
          color = TextSecondary,
          maxLines = 4,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 18.sp
        )
      }

      if (!note.aiSummary.isNullOrBlank()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = note.aiSummary, fontSize = 10.sp, color = ElectricCyan, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
      }
    }
  }
}

@Composable
private fun TvTaskRow(task: WorkspaceItem, onToggle: () -> Unit) {
  Surface(
    color = SlateSurface,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, SlateOutline, RoundedCornerShape(14.dp)),
    onClick = onToggle
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
          imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.FormatListBulleted,
          contentDescription = null,
          tint = if (task.isCompleted) NeonMint else TextMuted,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text = task.title,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (task.isCompleted) TextMuted else TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Text(
        text = task.priority,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = when (task.priority) {
          "HIGH" -> Color(0xFFFF5252)
          "LOW" -> TextMuted
          else -> ElectricCyan
        }
      )
    }
  }
}
