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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppWorkspaceMode
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.MalayalamCalendarHelper
import com.example.util.MalayalamDayInfo
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MalayalamCalendarScreen(
  viewModel: WorkspaceViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val workspaceMode by (viewModel?.workspaceMode?.collectAsState() ?: remember { mutableStateOf(AppWorkspaceMode.STUDY_POLYTECHNIC) })

  val isDesignMode = workspaceMode == AppWorkspaceMode.FREELANCE_DESIGNER

  var displayDate by remember { mutableStateOf(LocalDate.now()) }
  val daysInMonth = remember(displayDate.year, displayDate.month) {
    MalayalamCalendarHelper.getDaysInMonth(displayDate.year, displayDate.month)
  }
  var selectedDayInfo by remember {
    mutableStateOf(MalayalamCalendarHelper.getMalayalamDayInfo(LocalDate.now()))
  }

  // Jump to Year & Month Dialog (up to year 2100 as requested)
  var showYearMonthPicker by remember { mutableStateOf(false) }
  var pickerYearInput by remember { mutableStateOf(displayDate.year.toString()) }
  var pickerSelectedMonth by remember { mutableStateOf(displayDate.month) }

  // Add Task Dialog
  var showAddTaskDialog by remember { mutableStateOf(false) }
  var taskTitleInput by remember { mutableStateOf("") }
  var taskTimeInput by remember { mutableStateOf("09:00 AM") }
  var enableVibrationAlarm by remember { mutableStateOf(true) }

  val firstDayOfWeek = LocalDate.of(displayDate.year, displayDate.month, 1).dayOfWeek.value % 7

  fun triggerVibration(durationMs: Long = 60L) {
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
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Clean Navy Blue & White Header with 3-line quick jumper (up to 2100)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              // Large English Month & Year
              val monthFullName = displayDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
              Text(
                text = "$monthFullName ${displayDate.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )

              // Subtle Subheading showing Malayalam Month Name
              val sampleMid = MalayalamCalendarHelper.getMalayalamDayInfo(
                LocalDate.of(displayDate.year, displayDate.month, 15)
              )
              if (!isDesignMode) {
                Text(
                  text = "${sampleMid.malayalamMonthEnglish} (${sampleMid.malayalamMonthName})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NavySecondary
                )
              } else {
                Text(
                  text = "Google Calendar View",
                  fontSize = 12.sp,
                  color = TextMuted
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              // Prev Month
              IconButton(
                onClick = { displayDate = displayDate.minusMonths(1) },
                modifier = Modifier.size(34.dp).testTag("prev_month_btn")
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = NavyPrimary, modifier = Modifier.size(18.dp))
              }

              // Next Month
              IconButton(
                onClick = { displayDate = displayDate.plusMonths(1) },
                modifier = Modifier.size(34.dp).testTag("next_month_btn")
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = NavyPrimary, modifier = Modifier.size(18.dp))
              }

              // 3-Line Menu Button: Jump to any Year (up to 2100) and Month
              IconButton(
                onClick = {
                  pickerYearInput = displayDate.year.toString()
                  pickerSelectedMonth = displayDate.month
                  showYearMonthPicker = true
                },
                modifier = Modifier
                  .size(34.dp)
                  .background(NavyLight, RoundedCornerShape(8.dp))
                  .testTag("calendar_year_jump_btn")
              ) {
                Icon(Icons.Default.Menu, contentDescription = "Jump Year/Month", tint = NavyPrimary, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Calendar Grid Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          // Weekday Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEachIndexed { index, dow ->
              Text(
                text = dow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (index == 0) NavySecondary else TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // 7-column Grid
          Box(modifier = Modifier.height(310.dp)) {
            LazyVerticalGrid(
              columns = GridCells.Fixed(7),
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              userScrollEnabled = false
            ) {
              items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(0.92f))
              }

              items(daysInMonth) { dayInfo ->
                val isSelected = dayInfo.gregorianDate == selectedDayInfo.gregorianDate
                val isToday = dayInfo.gregorianDate == LocalDate.now()
                val isSunday = dayInfo.gregorianDate.dayOfWeek.value == 7

                Box(
                  modifier = Modifier
                    .aspectRatio(0.92f)
                    .testTag("day_cell_${dayInfo.gregorianDate}")
                    .background(
                      when {
                        isSelected -> NavyLight
                        isToday -> NavyLight.copy(alpha = 0.5f)
                        else -> CleanSurfaceVariant
                      },
                      RoundedCornerShape(8.dp)
                    )
                    .border(
                      width = if (isSelected) 1.5.dp else 0.5.dp,
                      color = when {
                        isSelected -> NavyPrimary
                        isToday -> NavySecondary
                        dayInfo.isSpecialDay -> StudioTeal
                        else -> CleanOutline
                      },
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      selectedDayInfo = dayInfo
                      triggerVibration(40L)
                    }
                    .padding(3.dp),
                  contentAlignment = Alignment.TopCenter
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                  ) {
                    // English Date Number (Prominent as requested)
                    Text(
                      text = "${dayInfo.gregorianDate.dayOfMonth}",
                      fontSize = 13.sp,
                      fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.SemiBold,
                      color = TextPrimary
                    )

                    // In Study Mode, show Star name. In Design Mode, keep it clean normal calendar.
                    if (!isDesignMode) {
                      Text(
                        text = dayInfo.nakshatraMalayalam,
                        fontSize = 8.sp,
                        color = NavySecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                      )
                    }

                    // Dot for special feast or task
                    if (dayInfo.isSpecialDay) {
                      Box(
                        modifier = Modifier
                          .size(4.dp)
                          .background(NavySecondary, CircleShape)
                      )
                    } else {
                      Spacer(modifier = Modifier.height(4.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Selected Day Details Card (Clean English with Star & Christian Feasts, No Rahukalam)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
          .testTag("calendar_detail_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Date Title
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${selectedDayInfo.gregorianDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, ${selectedDayInfo.gregorianDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${selectedDayInfo.gregorianDate.dayOfMonth}, ${selectedDayInfo.gregorianDate.year}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              if (!isDesignMode) {
                Text(
                  text = "${selectedDayInfo.malayalamMonthEnglish} ${selectedDayInfo.malayalamDayNumber} (${selectedDayInfo.malayalamMonthName})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NavySecondary
                )
              }
            }

            Button(
              onClick = { showAddTaskDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("add_calendar_task_alarm_btn")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Task", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Star Details (Hindu Nakshathram)
          if (!isDesignMode) {
            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Star / Nakshatra: ",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
                Text(
                  text = "${selectedDayInfo.nakshatraEnglish} (${selectedDayInfo.nakshatraMalayalam})",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }
          }

          // Christian Observance / Feast / Memorial Day if any
          if (selectedDayInfo.festival != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              color = NavyLight,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "✝ Feast / Observance: ",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
                Text(
                  text = selectedDayInfo.festival ?: "",
                  fontSize = 11.sp,
                  color = TextPrimary
                )
              }
            }
          }
        }
      }
    }

    // Jump to Year & Month Dialog (1950 - 2100)
    if (showYearMonthPicker) {
      AlertDialog(
        onDismissRequest = { showYearMonthPicker = false },
        title = { Text("Jump to Year & Month", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Enter or select any year (1950 - 2100):", fontSize = 12.sp, color = TextSecondary)

            OutlinedTextField(
              value = pickerYearInput,
              onValueChange = { pickerYearInput = it.filter { char -> char.isDigit() }.take(4) },
              label = { Text("Year (e.g. 2026, 2030, 2100)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("year_picker_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
              )
            )

            Text("Select Month:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            LazyVerticalGrid(
              columns = GridCells.Fixed(3),
              modifier = Modifier.height(130.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              items(Month.entries) { m ->
                val isSelected = pickerSelectedMonth == m
                Box(
                  modifier = Modifier
                    .background(if (isSelected) NavyPrimary else CleanSurfaceVariant, RoundedCornerShape(6.dp))
                    .clickable { pickerSelectedMonth = m }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = m.name.take(3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextSecondary
                  )
                }
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val parsedYear = pickerYearInput.toIntOrNull() ?: displayDate.year
              val clampedYear = parsedYear.coerceIn(1950, 2100)
              displayDate = LocalDate.of(clampedYear, pickerSelectedMonth, 1)
              selectedDayInfo = MalayalamCalendarHelper.getMalayalamDayInfo(displayDate)
              showYearMonthPicker = false
              triggerVibration(50L)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.testTag("apply_year_jump_btn")
          ) {
            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showYearMonthPicker = false }) {
            Text("Cancel", color = TextMuted)
          }
        }
      )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
      AlertDialog(
        onDismissRequest = { showAddTaskDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Task for Date", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Date: ${selectedDayInfo.gregorianDate}",
              fontSize = 12.sp,
              color = TextSecondary,
              fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
              value = taskTitleInput,
              onValueChange = { taskTitleInput = it },
              label = { Text("Task Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("calendar_task_title_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
              )
            )

            OutlinedTextField(
              value = taskTimeInput,
              onValueChange = { taskTimeInput = it },
              label = { Text("Time (e.g. 05:00 PM)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
              )
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Vibration Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Switch(
                checked = enableVibrationAlarm,
                onCheckedChange = { enableVibrationAlarm = it },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = NavyPrimary,
                  checkedTrackColor = NavyLight
                )
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (taskTitleInput.isNotBlank()) {
                triggerVibration(150L)
                val newItem = WorkspaceItem(
                  type = ItemType.TASK.name,
                  title = taskTitleInput.trim(),
                  content = "Scheduled for ${selectedDayInfo.gregorianDate} at $taskTimeInput",
                  priority = ItemPriority.HIGH.name,
                  tags = listOf("calendar", "date:${selectedDayInfo.gregorianDate}", "alarm:$taskTimeInput")
                )
                viewModel?.createItem(newItem)
                Toast.makeText(context, "Task scheduled with vibration alert", Toast.LENGTH_SHORT).show()
                showAddTaskDialog = false
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.testTag("save_calendar_task_btn")
          ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddTaskDialog = false }) {
            Text("Cancel", color = TextMuted)
          }
        }
      )
    }
  }
}
