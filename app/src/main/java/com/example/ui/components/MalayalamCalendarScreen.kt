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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.MalayalamCalendarHelper
import com.example.util.MalayalamDayInfo
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

// Modern Lighter Royal Navy & Platinum Theme Palette (Softened for optimal clarity)
val NavyBar = Color(0xFF1E3A8A) // Refined Lighter Royal Navy
val NavyAccent = Color(0xFF2563EB) // Modern Accent Cobalt Blue
val NavyLightTint = Color(0xFFEFF6FF) // Crisp Light Sky Tint
val HolidayRed = Color(0xFFDC2626) // Sundays, Second Saturdays & Gazetted Holidays Red
val DarkNavyText = Color(0xFF0F172A) // Main Text
val SlateSubText = Color(0xFF475569) // Subtitle Text
val CalendarBorder = Color(0xFFCBD5E1) // Crisp Border
val YellowSatHighlight = Color(0xFFFEF3C7) // Special Saturday Highlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MalayalamCalendarScreen(
  viewModel: WorkspaceViewModel? = null,
  onOpenModeSwitcher: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var displayDate by remember { mutableStateOf(LocalDate.of(2026, Month.NOVEMBER, 1)) }
  val daysInMonth = remember(displayDate.year, displayDate.month) {
    MalayalamCalendarHelper.getDaysInMonth(displayDate.year, displayDate.month)
  }
  val eraInfo = remember(displayDate.year, displayDate.month) {
    MalayalamCalendarHelper.getMonthEraInfo(displayDate.year, displayDate.month)
  }

  var selectedDayInfo by remember {
    mutableStateOf<MalayalamDayInfo?>(null)
  }
  var showDetailSheet by remember { mutableStateOf(false) }

  // Comprehensive Date Picker Dialog (Year, Month, Date selection)
  var showFullDatePicker by remember { mutableStateOf(false) }
  var pickerYear by remember { mutableIntStateOf(displayDate.year) }
  var pickerMonth by remember { mutableStateOf(displayDate.month) }
  var pickerDay by remember { mutableIntStateOf(displayDate.dayOfMonth) }

  // Add Task Dialog
  var showAddTaskDialog by remember { mutableStateOf(false) }
  var taskTitleInput by remember { mutableStateOf("") }
  var taskTimeInput by remember { mutableStateOf("09:00 AM") }
  var enableVibrationAlarm by remember { mutableStateOf(true) }

  val firstDayOfWeek = LocalDate.of(displayDate.year, displayDate.month, 1).dayOfWeek.value % 7

  fun triggerVibration(durationMs: Long = 40L) {
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

  val englishMonthName = displayDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase(Locale.ENGLISH)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    // 1. TOP NAVY BLUE APP BAR (No 'കലണ്ടർ' text; displays Month & Year or 2026 cleanly)
    Surface(
      color = NavyBar,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("calendar_top_bar"),
      shadowElevation = 3.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              triggerVibration(30L)
              if (onOpenModeSwitcher != null) {
                onOpenModeSwitcher()
              } else {
                pickerYear = displayDate.year
                pickerMonth = displayDate.month
                pickerDay = displayDate.dayOfMonth
                showFullDatePicker = true
              }
            },
            modifier = Modifier.testTag("calendar_menu_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Menu",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Top Title: Replaced 'കലണ്ടർ' with Month and Year (e.g. "SEPTEMBER 2026" / "NOVEMBER 2026")
          Text(
            text = "$englishMonthName ${displayDate.year}",
            color = Color.White,
            fontFamily = ManropeFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
              pickerYear = displayDate.year
              pickerMonth = displayDate.month
              pickerDay = displayDate.dayOfMonth
              showFullDatePicker = true
            }
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // Select Date Button (Year, Month, Date Picker)
          IconButton(
            onClick = {
              pickerYear = displayDate.year
              pickerMonth = displayDate.month
              pickerDay = displayDate.dayOfMonth
              showFullDatePicker = true
            },
            modifier = Modifier.testTag("select_date_dialog_btn")
          ) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = "Select Date",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          // Jump to Today
          IconButton(
            onClick = {
              triggerVibration(30L)
              displayDate = LocalDate.now()
              selectedDayInfo = MalayalamCalendarHelper.getMalayalamDayInfo(LocalDate.now())
            },
            modifier = Modifier.testTag("calendar_today_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Today,
              contentDescription = "Today",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Scrollable Calendar Body
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 24.dp)
    ) {
      // 2. CENTRAL MONTH HEADER: English only in center, Malayalam month info underneath
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White)
          .padding(top = 12.dp, bottom = 8.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Navigation row: <  SEPTEMBER 2026  >
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous Month Button
          IconButton(
            onClick = {
              triggerVibration(25L)
              displayDate = displayDate.minusMonths(1)
            },
            modifier = Modifier
              .size(36.dp)
              .border(1.dp, CalendarBorder, CircleShape)
              .testTag("prev_month_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Previous Month",
              tint = NavyAccent,
              modifier = Modifier.size(16.dp)
            )
          }

          // Central Heading: English Only
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
              pickerYear = displayDate.year
              pickerMonth = displayDate.month
              pickerDay = displayDate.dayOfMonth
              showFullDatePicker = true
            }
          ) {
            Text(
              text = "$englishMonthName ${displayDate.year}",
              fontSize = 20.sp,
              fontFamily = ManropeFontFamily,
              fontWeight = FontWeight.Bold,
              color = NavyBar,
              letterSpacing = 0.5.sp
            )

            // Malayalam Month Name Underneath (e.g. "കൊല്ലവർഷം 1202 തുലാം - വൃശ്ചികം")
            Text(
              text = "കൊല്ലവർഷം ${eraInfo.kollamEraLine}",
              fontSize = 12.5.sp,
              fontFamily = ManropeFontFamily,
              fontWeight = FontWeight.SemiBold,
              color = NavyAccent,
              textAlign = TextAlign.Center
            )
          }

          // Next Month Button
          IconButton(
            onClick = {
              triggerVibration(25L)
              displayDate = displayDate.plusMonths(1)
            },
            modifier = Modifier
              .size(36.dp)
              .border(1.dp, CalendarBorder, CircleShape)
              .testTag("next_month_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Next Month",
              tint = NavyAccent,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 3. WEEKDAY HEADER ROW (Sunday in Red, Monday to Saturday in Navy/Slate)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF8FAFC))
          .padding(horizontal = 4.dp, vertical = 7.dp)
          .border(1.dp, Color(0xFFE2E8F0)),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        val weekdays = listOf(
          Pair("SUN", true),
          Pair("MON", false),
          Pair("TUE", false),
          Pair("WED", false),
          Pair("THU", false),
          Pair("FRI", false),
          Pair("SAT", false)
        )
        weekdays.forEach { (dayName, isSunday) ->
          Text(
            text = dayName,
            color = if (isSunday) HolidayRed else NavyBar,
            fontFamily = ManropeFontFamily,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // 4. CALENDAR WALL GRID: Strict avoidance of '...' ellipsis, no Tamil, no Hijri
      Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 2.dp)
          .border(1.dp, CalendarBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          val totalCells = firstDayOfWeek + daysInMonth.size
          val rowCount = (totalCells + 6) / 7

          for (rowIndex in 0 until rowCount) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
            ) {
              for (colIndex in 0 until 7) {
                val cellIndex = rowIndex * 7 + colIndex
                val dayNumber = cellIndex - firstDayOfWeek + 1

                if (dayNumber in 1..daysInMonth.size) {
                  val dayInfo = daysInMonth[dayNumber - 1]
                  val isSelected = selectedDayInfo?.gregorianDate == dayInfo.gregorianDate

                  NavyCalendarDayCell(
                    dayInfo = dayInfo,
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .clickable {
                        triggerVibration(35L)
                        selectedDayInfo = dayInfo
                        showDetailSheet = true
                      }
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(82.dp)
                      .background(Color(0xFFFAFAFA))
                      .border(0.5.dp, CalendarBorder)
                  )
                }
              }
            }
          }
        }
      }

      // 5. SELECTED DAY QUICK SUMMARY CARD
      selectedDayInfo?.let { day ->
        Spacer(modifier = Modifier.height(10.dp))
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .border(1.dp, CalendarBorder, RoundedCornerShape(10.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "${day.gregorianDate.dayOfMonth} ${day.gregorianDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${day.gregorianDate.year} (${day.gregorianDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)})",
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = DarkNavyText
                )
                Text(
                  text = "കൊല്ലവർഷം ${day.kollamYear} ${day.malayalamMonthName} ${day.malayalamDayNumber} | നക്ഷത്രം: ${day.nakshatraMalayalam}",
                  fontFamily = PlusJakartaFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NavyAccent
                )
              }

              Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBar),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_calendar_task_alarm_btn")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task", fontSize = 11.5.sp, fontFamily = OutfitFontFamily, color = Color.White, fontWeight = FontWeight.Bold)
              }
            }

            if (day.festival != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "വിശേഷദിവസം: ${day.festival}",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = HolidayRed
              )
            }
          }
        }
      }
    }

    // 6. DETAILED PANCHANGAM BOTTOM SHEET
    if (showDetailSheet && selectedDayInfo != null) {
      val day = selectedDayInfo!!
      ModalBottomSheet(
        onDismissRequest = { showDetailSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${day.gregorianDate.dayOfMonth} ${day.gregorianDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${day.gregorianDate.year}",
                fontFamily = OutfitFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (day.isHolidayRed) HolidayRed else DarkNavyText
              )
              Text(
                text = "${day.gregorianDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} | കൊല്ലവർഷം ${day.kollamYear} ${day.malayalamMonthName} ${day.malayalamDayNumber}",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyAccent
              )
            }

            IconButton(onClick = { showDetailSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkNavyText)
            }
          }

          if (day.festival != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              color = Color(0xFFFEF2F2),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "★ ${day.festival}",
                color = HolidayRed,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = CalendarBorder)
          Spacer(modifier = Modifier.height(10.dp))

          // Key Panchangam rows (No Tamil, No Hijri)
          NavyDetailRow(title = "മലയാള മാസം", value = "${day.malayalamMonthName} ${day.malayalamDayNumber}")
          NavyDetailRow(title = "നക്ഷത്രം (Star)", value = "${day.nakshatraMalayalam} (${day.nakshatraEnglish})")
          NavyDetailRow(title = "തിഥി (Tithi)", value = "${day.thithiMalayalam} (${day.thithiEnglish})")
          NavyDetailRow(title = "രാഹുകാലം (Rahu Kalam)", value = day.rahuKalam)
          NavyDetailRow(title = "യമകണ്ടകം (Yamakandakam)", value = day.yamakandakam)
          NavyDetailRow(title = "ഗുളികകാലം (Gulikakalam)", value = day.gulikakalam)
          NavyDetailRow(title = "സൂര്യോദയം / അസ്തമയം", value = "${day.sunrise} / ${day.sunset}")

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              showDetailSheet = false
              showAddTaskDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBar),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Reminder / Task for This Date", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }

    // 7. COMPREHENSIVE DATE SELECTOR DIALOG (Year, Month, Date Picker)
    if (showFullDatePicker) {
      AlertDialog(
        onDismissRequest = { showFullDatePicker = false },
        title = {
          Text(
            "Select Date (മാസം, ഇയർ, ഡേറ്റ്)",
            fontFamily = OutfitFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBar
          )
        },
        text = {
          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
          ) {
            // Year Input (1950 - 2100)
            Text("1. Select Year (1950 - 2100):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkNavyText)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedButton(
                onClick = { if (pickerYear > 1950) pickerYear-- },
                contentPadding = PaddingValues(horizontal = 8.dp)
              ) {
                Text("-1", fontWeight = FontWeight.Bold, color = NavyBar)
              }

              OutlinedTextField(
                value = pickerYear.toString(),
                onValueChange = { input ->
                  val parsed = input.filter { c -> c.isDigit() }.toIntOrNull()
                  if (parsed != null) pickerYear = parsed
                },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("date_picker_year_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NavyBar,
                  focusedLabelColor = NavyBar
                )
              )

              OutlinedButton(
                onClick = { if (pickerYear < 2100) pickerYear++ },
                contentPadding = PaddingValues(horizontal = 8.dp)
              ) {
                Text("+1", fontWeight = FontWeight.Bold, color = NavyBar)
              }
            }

            // Month Selection Grid
            Text("2. Select Month:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkNavyText)
            LazyVerticalGrid(
              columns = GridCells.Fixed(3),
              modifier = Modifier.height(120.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              items(Month.entries) { m ->
                val isSelected = pickerMonth == m
                Box(
                  modifier = Modifier
                    .background(if (isSelected) NavyBar else Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    .clickable { pickerMonth = m }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = m.name.take(3),
                    fontSize = 11.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else DarkNavyText
                  )
                }
              }
            }

            // Day Selection Grid (1 to lengthOfMonth)
            val maxDaysInMonth = pickerMonth.length(LocalDate.of(pickerYear, 1, 1).isLeapYear)
            if (pickerDay > maxDaysInMonth) pickerDay = maxDaysInMonth

            Text("3. Select Day (1 - $maxDaysInMonth):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkNavyText)
            LazyVerticalGrid(
              columns = GridCells.Fixed(7),
              modifier = Modifier.height(130.dp),
              verticalArrangement = Arrangement.spacedBy(3.dp),
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              items((1..maxDaysInMonth).toList()) { d ->
                val isSelected = pickerDay == d
                Box(
                  modifier = Modifier
                    .background(if (isSelected) NavyAccent else Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                    .border(0.5.dp, if (isSelected) NavyAccent else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .clickable { pickerDay = d }
                    .padding(vertical = 4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$d",
                    fontSize = 11.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else DarkNavyText
                  )
                }
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val clampedYear = pickerYear.coerceIn(1950, 2100)
              val maxDays = pickerMonth.length(LocalDate.of(clampedYear, 1, 1).isLeapYear)
              val clampedDay = pickerDay.coerceIn(1, maxDays)
              val targetDate = LocalDate.of(clampedYear, pickerMonth, clampedDay)
              displayDate = targetDate
              selectedDayInfo = MalayalamCalendarHelper.getMalayalamDayInfo(targetDate)
              showFullDatePicker = false
              triggerVibration(40L)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBar),
            modifier = Modifier.testTag("apply_full_date_picker_btn")
          ) {
            Text("Jump to Date", color = Color.White, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showFullDatePicker = false }) {
            Text("Cancel", color = SlateSubText)
          }
        }
      )
    }

    // 8. ADD TASK / REMINDER DIALOG
    if (showAddTaskDialog && selectedDayInfo != null) {
      val day = selectedDayInfo!!
      AlertDialog(
        onDismissRequest = { showAddTaskDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NavyBar)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Task / Alarm", fontFamily = OutfitFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Date: ${day.gregorianDate} (കൊല്ലവർഷം ${day.kollamYear} ${day.malayalamMonthName} ${day.malayalamDayNumber})",
              fontSize = 12.sp,
              color = SlateSubText,
              fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
              value = taskTitleInput,
              onValueChange = { taskTitleInput = it },
              label = { Text("Task Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("calendar_task_title_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBar,
                focusedLabelColor = NavyBar
              )
            )

            OutlinedTextField(
              value = taskTimeInput,
              onValueChange = { taskTimeInput = it },
              label = { Text("Time (e.g. 05:00 PM)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBar,
                focusedLabelColor = NavyBar
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
                  checkedThumbColor = NavyBar,
                  checkedTrackColor = NavyLightTint
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
                  content = "Scheduled for ${day.gregorianDate} at $taskTimeInput | ${day.malayalamMonthName} ${day.malayalamDayNumber}",
                  priority = ItemPriority.HIGH.name,
                  tags = listOf("calendar", "date:${day.gregorianDate}", "alarm:$taskTimeInput")
                )
                viewModel?.createItem(newItem)
                Toast.makeText(context, "Task scheduled with vibration alert", Toast.LENGTH_SHORT).show()
                showAddTaskDialog = false
                taskTitleInput = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBar),
            modifier = Modifier.testTag("save_calendar_task_btn")
          ) {
            Text("Save", color = Color.White, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddTaskDialog = false }) {
            Text("Cancel", color = SlateSubText)
          }
        }
      )
    }
  }
}

/**
 * Clean Navy Blue & White Day Cell.
 * - Bold Red for Sundays, Second Saturdays, and Gazetted Holidays
 * - Nakshatra (Star) and Malayalam month day included
 * - No Tamil, no Hijri
 * - Strict avoidance of '...' ellipsis
 */
@Composable
fun NavyCalendarDayCell(
  dayInfo: MalayalamDayInfo,
  isSelected: Boolean,
  modifier: Modifier = Modifier
) {
  val cellBg = when {
    isSelected -> NavyLightTint
    dayInfo.isYellowHighlight -> YellowSatHighlight
    else -> Color.White
  }

  val dateColor = if (dayInfo.isHolidayRed) HolidayRed else DarkNavyText

  Box(
    modifier = modifier
      .height(82.dp)
      .background(cellBg)
      .border(
        width = if (isSelected) 1.5.dp else 0.5.dp,
        color = if (isSelected) NavyAccent else CalendarBorder
      )
      .padding(horizontal = 3.dp, vertical = 2.dp)
      .testTag("day_cell_${dayInfo.gregorianDate}")
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Top Section: Malayalam Month Transition Badge or Corner Number
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        if (dayInfo.isMonthTransition && dayInfo.transitionMonthLabel != null) {
          Box(
            modifier = Modifier
              .background(NavyAccent, RoundedCornerShape(3.dp))
              .padding(horizontal = 2.dp, vertical = 0.5.dp)
          ) {
            Text(
              text = dayInfo.transitionMonthLabel,
              fontSize = 5.5.sp,
              fontFamily = ManropeFontFamily,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        // Malayalam Day Number on top-right (e.g. 15, 16...)
        Text(
          text = "${dayInfo.malayalamDayNumber}",
          fontSize = 7.5.sp,
          fontFamily = ManropeFontFamily,
          fontWeight = FontWeight.Bold,
          color = NavyAccent
        )
      }

      // 2. Middle Section: Prominent Gregorian Date (Red for Holiday/Sunday/2nd Saturday) + Holiday Label
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "${dayInfo.gregorianDate.dayOfMonth}",
          fontSize = 21.sp,
          fontFamily = ManropeFontFamily,
          fontWeight = FontWeight.ExtraBold,
          color = dateColor,
          lineHeight = 22.sp
        )

        if (dayInfo.festival != null) {
          Text(
            text = dayInfo.festival,
            fontSize = 6.sp,
            lineHeight = 7.5.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (dayInfo.isHolidayRed) HolidayRed else DarkNavyText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Clip // No '...' ellipsis
          )
        }
      }

      // 3. Bottom Section: Nakshatra (Star) clearly visible (No Tamil, No Hijri, No '...' ellipsis)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 1.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = dayInfo.nakshatraMalayalam,
          fontSize = 6.8.sp,
          fontFamily = ManropeFontFamily,
          fontWeight = FontWeight.Bold,
          color = DarkNavyText,
          maxLines = 1,
          overflow = TextOverflow.Clip,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Composable
fun NavyDetailRow(title: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 12.sp,
      fontFamily = OutfitFontFamily,
      fontWeight = FontWeight.Bold,
      color = NavyAccent
    )
    Text(
      text = value,
      fontSize = 12.sp,
      fontFamily = PlusJakartaFontFamily,
      fontWeight = FontWeight.SemiBold,
      color = DarkNavyText,
      textAlign = TextAlign.End
    )
  }
}
