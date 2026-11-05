package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.CoralWarning
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
fun WorkspaceDashboard(
  viewModel: WorkspaceViewModel,
  onOpenPhotoAnalysis: () -> Unit,
  onOpenAiChat: () -> Unit,
  modifier: Modifier = Modifier
) {
  val itemsState by viewModel.itemsState.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val actionMessage by viewModel.actionMessage.collectAsState()
  val isAiProcessing by viewModel.isAiProcessing.collectAsState()

  var showNewNoteDialog by remember { mutableStateOf(false) }
  var showNewTaskDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { viewModel.searchQuery.value = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("workspace_search_input"),
      placeholder = { Text("Search synced notes, tasks, or tags...", color = TextMuted, fontSize = 14.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { viewModel.searchQuery.value = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElectricCyan,
        unfocusedBorderColor = SlateOutline,
        focusedContainerColor = SlateSurface,
        unfocusedContainerColor = SlateSurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Action Toast / Message Banner
    AnimatedVisibility(visible = actionMessage != null) {
      actionMessage?.let { msg ->
        Surface(
          color = Color(0xFF0F261E),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, NeonMint.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Check, contentDescription = null, tint = NeonMint, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = msg, fontSize = 12.sp, color = NeonMint)
            }
            IconButton(
              onClick = { viewModel.clearActionMessage() },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = NeonMint, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }

    // Quick Action Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Button(
        onClick = { showNewNoteDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = ElectricCyan),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("add_note_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Note", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }

      Button(
        onClick = { showNewTaskDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231C38), contentColor = VividViolet),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("add_task_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Task", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }

      Button(
        onClick = onOpenPhotoAnalysis,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3034), contentColor = ElectricCyan),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1.1f)
          .height(44.dp)
          .testTag("open_photo_analysis_button")
      ) {
        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Scan AI", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("ALL" to "All", "NOTE" to "Notes", "TASK" to "Tasks", "MEDIA" to "Media").forEach { (tabKey, label) ->
        val isSelected = selectedTab == tabKey
        FilterChip(
          selected = isSelected,
          onClick = { viewModel.selectedTab.value = tabKey },
          label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
            selectedLabelColor = ElectricCyan,
            containerColor = SlateSurface,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = SlateOutline,
            selectedBorderColor = ElectricCyan
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Content List
    when (val state = itemsState) {
      is WorkspaceUiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(36.dp))
        }
      }
      is WorkspaceUiState.Error -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
      }
      is WorkspaceUiState.Success -> {
        if (state.items.isEmpty()) {
          EmptyWorkspaceView(
            onAddNote = { showNewNoteDialog = true },
            onAddTask = { showNewTaskDialog = true }
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(state.items, key = { it.id }) { item ->
              WorkspaceItemCard(
                item = item,
                isAiProcessing = isAiProcessing,
                onToggleCompleted = { viewModel.toggleTaskCompletion(item) },
                onDelete = { viewModel.deleteItem(item.id) },
                onSummarize = { viewModel.summarizeNoteWithAi(item) },
                onExtractTasks = { viewModel.extractChecklistTasks(item) }
              )
            }
            item {
              Spacer(modifier = Modifier.height(72.dp))
            }
          }
        }
      }
    }
  }

  // Create Note Dialog
  if (showNewNoteDialog) {
    CreateNoteDialog(
      onDismiss = { showNewNoteDialog = false },
      onConfirm = { title, content, tags ->
        viewModel.createNote(title, content, tags)
        showNewNoteDialog = false
      }
    )
  }

  // Create Task Dialog
  if (showNewTaskDialog) {
    CreateTaskDialog(
      onDismiss = { showNewTaskDialog = false },
      onConfirm = { title, priority ->
        viewModel.createTask(title, priority)
        showNewTaskDialog = false
      }
    )
  }
}

@Composable
private fun WorkspaceItemCard(
  item: WorkspaceItem,
  isAiProcessing: Boolean,
  onToggleCompleted: () -> Unit,
  onDelete: () -> Unit,
  onSummarize: () -> Unit,
  onExtractTasks: () -> Unit
) {
  val isTask = item.type == ItemType.TASK.name
  val isNote = item.type == ItemType.NOTE.name
  val isMedia = item.type == ItemType.MEDIA.name

  Card(
    colors = CardDefaults.cardColors(containerColor = SlateSurface),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, SlateOutline, RoundedCornerShape(16.dp))
      .testTag("item_card_${item.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Type Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(
                when {
                  isTask -> VividViolet.copy(alpha = 0.2f)
                  isMedia -> NeonMint.copy(alpha = 0.2f)
                  else -> ElectricCyan.copy(alpha = 0.2f)
                },
                RoundedCornerShape(6.dp)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.type,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = when {
                isTask -> VividViolet
                isMedia -> NeonMint
                else -> ElectricCyan
              }
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Priority badge
          val priorityColor = when (item.priority) {
            ItemPriority.HIGH.name -> CoralWarning
            ItemPriority.LOW.name -> TextMuted
            else -> ElectricCyan
          }
          Text(
            text = "• ${item.priority}",
            fontSize = 11.sp,
            color = priorityColor,
            fontWeight = FontWeight.Medium
          )
        }

        // Delete button
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = TextMuted, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Title & Task Checkbox
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        if (isTask) {
          Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggleCompleted() },
            colors = CheckboxDefaults.colors(
              checkedColor = NeonMint,
              uncheckedColor = TextSecondary,
              checkmarkColor = Color.Black
            ),
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
          text = item.title,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (item.isCompleted) TextMuted else TextPrimary,
          textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
      }

      // Content preview
      if (item.content.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = item.content,
          fontSize = 13.sp,
          color = TextSecondary,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 18.sp
        )
      }

      // AI Summary Box if present
      if (!item.aiSummary.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          color = SlateSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = ElectricCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Gemini Summary", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
              Text(item.aiSummary, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
            }
          }
        }
      }

      // Tags
      if (item.tags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          item.tags.take(4).forEach { tag ->
            Box(
              modifier = Modifier
                .background(Color(0xFF1E2433), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("#$tag", fontSize = 10.sp, color = TextMuted)
            }
          }
        }
      }

      // Note Quick Actions (Summarize / Tasks)
      if (isNote && item.content.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = onSummarize,
            enabled = !isAiProcessing,
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("AI Summarize", fontSize = 11.sp, color = ElectricCyan)
          }

          Spacer(modifier = Modifier.width(8.dp))

          TextButton(
            onClick = onExtractTasks,
            enabled = !isAiProcessing,
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = VividViolet, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Extract Tasks", fontSize = 11.sp, color = VividViolet)
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyWorkspaceView(
  onAddNote: () -> Unit,
  onAddTask: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = Color(0xFF1A2234),
        modifier = Modifier.size(64.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Default.Description, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(32.dp))
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text("No items in workspace yet", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        "Create a note or task. Everything you create syncs instantly across your mobile, laptop, and TV screens.",
        fontSize = 13.sp,
        color = TextMuted,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        lineHeight = 18.sp
      )
      Spacer(modifier = Modifier.height(20.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
          onClick = onAddNote,
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0E1117)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("+ Create Note", fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = onAddTask,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D42), contentColor = VividViolet),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("+ New Task", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun CreateNoteDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, content: String, tags: List<String>) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var tagInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Synced Note", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("note_title_input"),
          singleLine = true
        )
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Content / Notes") },
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("note_content_input"),
          maxLines = 6
        )
        OutlinedTextField(
          value = tagInput,
          onValueChange = { tagInput = it },
          label = { Text("Tags (comma separated, e.g. project, tv, sync)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val tags = tagInput.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
          onConfirm(title, content, tags)
        },
        enabled = title.isNotBlank() || content.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0E1117))
      ) {
        Text("Save & Sync")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = SlateSurface
  )
}

@Composable
private fun CreateTaskDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, priority: ItemPriority) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var priority by remember { mutableStateOf(ItemPriority.MEDIUM) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Synced Task", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task description") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("task_title_input"),
          singleLine = true
        )

        Text("Priority Level:", fontSize = 13.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ItemPriority.entries.forEach { p ->
            FilterChip(
              selected = priority == p,
              onClick = { priority = p },
              label = { Text(p.name, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = when (p) {
                  ItemPriority.HIGH -> CoralWarning.copy(alpha = 0.2f)
                  ItemPriority.LOW -> Color(0xFF262C38)
                  else -> ElectricCyan.copy(alpha = 0.2f)
                },
                selectedLabelColor = when (p) {
                  ItemPriority.HIGH -> CoralWarning
                  ItemPriority.LOW -> TextPrimary
                  else -> ElectricCyan
                }
              )
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(title, priority) },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = VividViolet, contentColor = Color.White)
      ) {
        Text("Add Task")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = SlateSurface
  )
}
