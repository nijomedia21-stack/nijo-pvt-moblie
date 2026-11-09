package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import com.example.util.AudioRecorderHelper
import com.example.util.DriveBackupHelper
import java.io.File
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
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
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppWorkspaceMode
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.ui.viewmodel.WorkspaceViewModel

enum class TaskFilter {
  ALL,
  TODAY,
  UPCOMING,
  DONE
}

@Composable
fun TasksScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val workspaceMode by viewModel.workspaceMode.collectAsState()
  val isDesignMode = workspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER

  var currentFilter by remember { mutableStateOf(TaskFilter.ALL) }
  var selectedFolder by remember(workspaceMode) {
    mutableStateOf(if (isDesignMode) "LIBRA" else "ALL")
  }
  var showCreateDialog by remember { mutableStateOf(false) }
  var showVoiceDialog by remember { mutableStateOf(false) }
  var itemToEdit by remember { mutableStateOf<WorkspaceItem?>(null) }

  fun triggerVibration(durationMs: Long = 80L) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(
          VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(durationMs)
        }
      }
    } catch (_: Exception) {}
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CleanBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      // Clean Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isDesignMode) "Tasks" else "Tasks & Schedule",
            fontSize = 20.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Text(
            text = if (isDesignMode) "Freelance deliverables & SMM projects" else "Academic studies & personal deadlines",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          IconButton(
            onClick = {
              val backupList = (uiState as? WorkspaceUiState.Success)?.items ?: emptyList()
              DriveBackupHelper.exportToGoogleDrive(context, backupList)
            },
            modifier = Modifier
              .size(36.dp)
              .background(NavyLight, CircleShape)
              .testTag("google_drive_backup_btn")
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = "Backup to Google Drive", tint = NavyPrimary, modifier = Modifier.size(18.dp))
          }

          IconButton(
            onClick = {
              triggerVibration(60L)
              showVoiceDialog = true
            },
            modifier = Modifier
              .size(36.dp)
              .background(NavyLight, CircleShape)
              .testTag("voice_dictate_button")
          ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice Dictation", tint = NavyPrimary, modifier = Modifier.size(18.dp))
          }

          Button(
            onClick = {
              triggerVibration(50L)
              showCreateDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("create_task_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Task", tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("New Task", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Filters (All / Today / Upcoming / Done)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        TaskFilter.entries.forEach { filter ->
          val isSelected = currentFilter == filter
          Box(
            modifier = Modifier
              .testTag("filter_${filter.name.lowercase()}")
              .background(
                if (isSelected) NavyLight else CleanSurface,
                RoundedCornerShape(8.dp)
              )
              .border(
                1.dp,
                if (isSelected) NavyPrimary else CleanOutline,
                RoundedCornerShape(8.dp)
              )
              .clickable { currentFilter = filter }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = filter.name.replaceFirstChar { it.uppercase() },
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) NavyPrimary else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Folder Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val folders = listOf(
          Pair("ALL", "All"),
          Pair("STUDY", "🎓 Study"),
          Pair("PERSONAL", "👤 Personal"),
          Pair("PROFESSIONAL", "💼 Professional"),
          Pair("LIBRA", "Libra Brand"),
          Pair("MATHS", "Maths I")
        )

        items(folders) { (fId, fName) ->
          val isSelected = selectedFolder == fId
          FilterChip(
            selected = isSelected,
            onClick = { selectedFolder = fId },
            label = { Text(fName, fontSize = 11.sp) },
            leadingIcon = {
              Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(13.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NavyLight,
              selectedLabelColor = NavyPrimary,
              selectedLeadingIconColor = NavyPrimary,
              containerColor = CleanSurface,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) NavyPrimary else CleanOutline,
              borderWidth = if (isSelected) 1.5.dp else 1.dp,
              enabled = true,
              selected = isSelected
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("folder_chip_${fId.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Task Items List
      when (val state = uiState) {
        is WorkspaceUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavyPrimary)
          }
        }
        is WorkspaceUiState.Error -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error: ${state.message}", color = TextSecondary, fontSize = 13.sp)
          }
        }
        is WorkspaceUiState.Success -> {
          val allTasks = state.items.filter { it.type == ItemType.TASK.name }
          val statusFiltered = when (currentFilter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.TODAY -> allTasks.filter { !it.isCompleted && (it.tags.contains("today") || it.priority == ItemPriority.HIGH.name) }
            TaskFilter.UPCOMING -> allTasks.filter { !it.isCompleted && !it.tags.contains("today") }
            TaskFilter.DONE -> allTasks.filter { it.isCompleted }
          }
          val folderFiltered = when (selectedFolder) {
            "STUDY" -> statusFiltered.filter { it.tags.contains("cat_study") || it.tags.any { t -> t.contains("study") || t.contains("math") || t.contains("chem") || t.contains("pspp") } }
            "PERSONAL" -> statusFiltered.filter { it.tags.contains("cat_personal") || it.tags.contains("personal") || it.tags.contains("general") }
            "PROFESSIONAL" -> statusFiltered.filter { it.tags.contains("cat_professional") || it.tags.any { t -> t.contains("professional") || t.contains("libra") || t.contains("smm") || t.contains("motion") } }
            "LIBRA" -> statusFiltered.filter { it.tags.any { t -> t.contains("libra") || t.contains("client") } }
            "MATHS" -> statusFiltered.filter { it.tags.any { t -> t.contains("math") } }
            else -> statusFiltered
          }

          if (folderFiltered.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CleanSurface, RoundedCornerShape(12.dp))
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "No tasks listed",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextMuted
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(bottom = 80.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(folderFiltered, key = { it.id }) { task ->
                TaskItemRow(
                  task = task,
                  onToggleDone = { updated ->
                    triggerVibration(60L)
                    viewModel.updateItem(updated)
                  },
                  onEdit = { itemToEdit = task },
                  onDelete = {
                    triggerVibration(40L)
                    viewModel.deleteItem(task.id)
                  }
                )
              }
            }
          }
        }
      }
    }

    // Voice Recording & Playback Dialog (Records real voice, allows playback, saves audio note)
    if (showVoiceDialog) {
      var isRecording by remember { mutableStateOf(false) }
      var recordedAudioFile by remember { mutableStateOf<File?>(null) }
      var isPlayingAudio by remember { mutableStateOf(false) }
      var elapsedSeconds by remember { androidx.compose.runtime.mutableIntStateOf(0) }
      var noteTitle by remember { mutableStateOf("") }

      // Timer effect when recording
      androidx.compose.runtime.LaunchedEffect(isRecording) {
        if (isRecording) {
          elapsedSeconds = 0
          while (isRecording) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds++
          }
        }
      }

      val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
      ) { granted ->
        if (granted) {
          val res = AudioRecorderHelper.startRecording(context)
          if (res.isSuccess) {
            isRecording = true
            triggerVibration(70L)
          } else {
            Toast.makeText(context, "Could not start mic recording", Toast.LENGTH_SHORT).show()
          }
        } else {
          Toast.makeText(context, "Microphone permission needed to record voice note", Toast.LENGTH_SHORT).show()
        }
      }

      AlertDialog(
        onDismissRequest = {
          if (isRecording) {
            AudioRecorderHelper.stopRecording()
          }
          AudioRecorderHelper.stopPlayback()
          showVoiceDialog = false
        },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Voice Note & Recording", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Record your voice to create an audio note. You can listen back to your recording before saving.",
              fontSize = 12.sp,
              color = TextSecondary
            )

            // Recording status & visual timer
            Surface(
              color = if (isRecording) Color(0x22E11D48) else CleanSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRecording) Color(0xFFE11D48) else CleanOutline
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(10.dp)
                      .background(
                        if (isRecording) Color(0xFFE11D48) else if (recordedAudioFile != null) Color(0xFF007A50) else Color.Gray,
                        CircleShape
                      )
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = when {
                      isRecording -> "Recording (${String.format(java.util.Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)})"
                      recordedAudioFile != null -> "Recorded Audio Saved (${recordedAudioFile?.name?.takeLast(12)})"
                      else -> "Mic Ready to Record"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Color(0xFFE11D48) else TextPrimary
                  )
                }

                Button(
                  onClick = {
                    if (isRecording) {
                      val file = AudioRecorderHelper.stopRecording()
                      isRecording = false
                      recordedAudioFile = file
                      triggerVibration(50L)
                      if (noteTitle.isBlank()) {
                        noteTitle = "Voice Task " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                      }
                    } else {
                      try {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                      } catch (_: Exception) {
                        val res = AudioRecorderHelper.startRecording(context)
                        if (res.isSuccess) isRecording = true
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFE11D48) else NavyPrimary
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(34.dp).testTag("record_toggle_btn")
                ) {
                  Text(if (isRecording) "Stop ⏹" else "Record 🎙", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
            }

            // Playback controls if audio recorded
            if (recordedAudioFile != null && !isRecording) {
              Surface(
                color = NavyLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                      contentDescription = null,
                      tint = NavyPrimary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (isPlayingAudio) "Playing audio..." else "Listen to your recording",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = NavyPrimary
                    )
                  }

                  Button(
                    onClick = {
                      val file = recordedAudioFile ?: return@Button
                      if (isPlayingAudio) {
                        AudioRecorderHelper.stopPlayback()
                        isPlayingAudio = false
                      } else {
                        isPlayingAudio = true
                        AudioRecorderHelper.playAudio(file) {
                          isPlayingAudio = false
                        }
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("play_recording_btn")
                  ) {
                    Text(if (isPlayingAudio) "Stop" else "▶ Play", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            OutlinedTextField(
              value = noteTitle,
              onValueChange = { noteTitle = it },
              label = { Text("Task / Audio Note Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("voice_input_field"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
              )
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (isRecording) {
                AudioRecorderHelper.stopRecording()
              }
              AudioRecorderHelper.stopPlayback()
              val titleToSave = noteTitle.trim().ifBlank {
                "Voice Task " + java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
              }
              triggerVibration(100L)
              val newItem = WorkspaceItem(
                type = ItemType.TASK.name,
                title = titleToSave,
                content = if (recordedAudioFile != null) "Audio voice note recorded" else "Voice captured task",
                imageUrl = recordedAudioFile?.absolutePath,
                priority = ItemPriority.HIGH.name,
                tags = listOf("task", "audio_note", "voice")
              )
              viewModel.createItem(newItem)
              showVoiceDialog = false
              Toast.makeText(context, "Saved voice task: $titleToSave", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.testTag("confirm_voice_task_btn")
          ) {
            Text("Save Task", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = {
            if (isRecording) AudioRecorderHelper.stopRecording()
            AudioRecorderHelper.stopPlayback()
            showVoiceDialog = false
          }) {
            Text("Cancel", color = TextMuted)
          }
        }
      )
    }

    // Create Task Dialog
    if (showCreateDialog) {
      TaskModalDialog(
        initialItem = null,
        onDismiss = { showCreateDialog = false },
        onSave = { newItem ->
          triggerVibration(60L)
          viewModel.createItem(newItem)
          showCreateDialog = false
        }
      )
    }

    // Edit Task Dialog
    itemToEdit?.let { task ->
      TaskModalDialog(
        initialItem = task,
        onDismiss = { itemToEdit = null },
        onSave = { updated ->
          triggerVibration(60L)
          viewModel.updateItem(updated)
          itemToEdit = null
        }
      )
    }
  }
}

@Composable
private fun TaskItemRow(
  task: WorkspaceItem,
  onToggleDone: (WorkspaceItem) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val priorityColor = when (task.priority.uppercase()) {
    ItemPriority.HIGH.name, "HIGH" -> Color(0xFFDC2626)
    ItemPriority.MEDIUM.name, "MEDIUM" -> Color(0xFFD97706)
    ItemPriority.LOW.name, "LOW" -> Color(0xFF16A34A)
    else -> Color(0xFF2563EB)
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (task.isCompleted) Color(0xFFF8FAFC) else CleanSurface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.5.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = if (task.isCompleted) CleanOutline.copy(alpha = 0.5f) else CleanOutline,
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("task_card_${task.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Priority Accent Strip
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(38.dp)
          .background(
            if (task.isCompleted) Color(0xFFCBD5E1) else priorityColor,
            RoundedCornerShape(2.dp)
          )
      )

      Spacer(modifier = Modifier.width(10.dp))

      // Touch-Friendly Checkbox Target (>= 48dp)
      Box(
        modifier = Modifier
          .size(48.dp)
          .clickable { onToggleDone(task.copy(isCompleted = !task.isCompleted)) },
        contentAlignment = Alignment.Center
      ) {
        Checkbox(
          checked = task.isCompleted,
          onCheckedChange = { onToggleDone(task.copy(isCompleted = it)) },
          colors = CheckboxDefaults.colors(
            checkedColor = NavyPrimary,
            uncheckedColor = CleanOutline
          ),
          modifier = Modifier.testTag("checkbox_${task.id}")
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Center Task Details
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = task.title,
          fontSize = 14.sp,
          fontFamily = ManropeFontFamily,
          fontWeight = FontWeight.SemiBold,
          color = if (task.isCompleted) TextMuted else TextPrimary,
          textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          maxLines = 2
        )

        if (task.content.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = task.content,
            fontSize = 11.5.sp,
            color = if (task.isCompleted) TextMuted.copy(alpha = 0.7f) else TextSecondary,
            maxLines = 1
          )
        }

        // Voice Note Player Pill
        if (task.tags.contains("audio_note") && !task.imageUrl.isNullOrBlank()) {
          var isPlayingThis by remember { mutableStateOf(false) }
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            color = NavyLight,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable {
              val audioFile = File(task.imageUrl)
              if (isPlayingThis) {
                AudioRecorderHelper.stopPlayback()
                isPlayingThis = false
              } else {
                isPlayingThis = true
                AudioRecorderHelper.playAudio(audioFile) {
                  isPlayingThis = false
                }
              }
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isPlayingThis) "Playing voice..." else "Voice Memo",
                fontSize = 10.5.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
            }
          }
        }

        // Tag Badges
        if (task.tags.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            task.tags.take(3).forEach { tag ->
              Surface(
                color = CleanSurfaceVariant,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "#$tag",
                  fontSize = 9.5.sp,
                  fontFamily = ManropeFontFamily,
                  color = TextSecondary,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                )
              }
            }
          }
        }
      }

      // Actions: Edit & Delete with minimum 48dp touch targets
      IconButton(
        onClick = onEdit,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Edit",
          tint = TextSecondary,
          modifier = Modifier.size(17.dp)
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = TextMuted,
          modifier = Modifier.size(17.dp)
        )
      }
    }
  }
}

@Composable
private fun TaskModalDialog(
  initialItem: WorkspaceItem?,
  onDismiss: () -> Unit,
  onSave: (WorkspaceItem) -> Unit
) {
  var title by remember { mutableStateOf(initialItem?.title ?: "") }
  var content by remember { mutableStateOf(initialItem?.content ?: "") }
  var priority by remember { mutableStateOf(initialItem?.priority ?: ItemPriority.MEDIUM.name) }
  var folderInput by remember { mutableStateOf(if (initialItem?.tags?.contains("libra") == true) "Libra" else "General") }
  var selectedCategory by remember {
    mutableStateOf(
      when {
        initialItem?.tags?.contains("cat_personal") == true -> "Personal"
        initialItem?.tags?.contains("cat_professional") == true -> "Professional"
        else -> "Study"
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialItem == null) "New Task" else "Edit Task",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Category Selector (Study, Personal, Professional)
        Text("Task Category (Kanban Column):", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
          listOf("Study", "Personal", "Professional").forEach { cat ->
            val isCatSelected = selectedCategory == cat
            Surface(
              color = if (isCatSelected) NavyPrimary else CleanSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isCatSelected) NavyPrimary else CleanOutline),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedCategory = cat }
                .testTag("cat_selector_${cat.lowercase()}")
            ) {
              Text(
                text = when (cat) {
                  "Study" -> "🎓 Study"
                  "Personal" -> "👤 Personal"
                  else -> "💼 Professional"
                },
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCatSelected) Color.White else TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            focusedLabelColor = NavyPrimary
          )
        )

        OutlinedTextField(
          value = folderInput,
          onValueChange = { folderInput = it },
          label = { Text("Folder / Tag (e.g. Maths, Libra, Home)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("task_folder_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            focusedLabelColor = NavyPrimary
          )
        )

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Notes / Description") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            focusedLabelColor = NavyPrimary
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val tags = listOf(
              "cat_${selectedCategory.lowercase()}",
              folderInput.trim().lowercase()
            )
            val item = initialItem?.copy(
              title = title.trim(),
              content = content.trim(),
              priority = priority,
              tags = tags
            ) ?: WorkspaceItem(
              type = ItemType.TASK.name,
              title = title.trim(),
              content = content.trim(),
              priority = priority,
              tags = tags
            )
            onSave(item)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        modifier = Modifier.testTag("save_task_button")
      ) {
        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted)
      }
    }
  )
}
