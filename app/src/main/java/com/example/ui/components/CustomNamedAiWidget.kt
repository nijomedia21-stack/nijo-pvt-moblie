package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.FirebaseAiManager
import com.example.data.model.ChatMessage
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.CleanBackground
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
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.MalayalamCalendarHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AiAssistantMessage(
  val sender: String, // "user" or "assistant"
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CustomNamedAiFloatingWidget(
  aiName: String,
  onAiNameChange: (String) -> Unit,
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }
  var isEditingName by remember { mutableStateOf(false) }
  var inputName by remember { mutableStateOf(aiName) }
  var userPrompt by remember { mutableStateOf("") }

  val coroutineScope = rememberCoroutineScope()
  val aiManager = remember { FirebaseAiManager() }
  var isAiGenerating by remember { mutableStateOf(false) }

  val messages = remember {
    mutableStateListOf(
      AiAssistantMessage(
        sender = "assistant",
        text = "Hello Nijo! I am $aiName, your personal AI Copilot. I help manage your AI&ML Polytechnic studies, freelance graphic design & SMM client deliveries, Malayalam calendar, and QuickShare file transfers."
      )
    )
  }

  val uiState by viewModel.uiState.collectAsState()

  // Floating trigger button - Optimized Navy Blue & White Design
  Box(modifier = modifier) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 6.dp,
      modifier = Modifier
        .border(1.dp, NavyPrimary.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
        .clickable { isExpanded = true }
        .testTag("ai_assistant_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .background(NavyPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.AutoAwesome,
            contentDescription = aiName,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = aiName,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(6.dp)
            .background(NeonMint, CircleShape)
        )
      }
    }
  }

  // AI Assistant Dialog - Clean Navy Blue & White Theme
  if (isExpanded) {
    Dialog(onDismissRequest = { isExpanded = false }) {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp)
          .border(1.dp, CleanOutline, RoundedCornerShape(22.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Header with customizable name
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .background(NavyPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))

              if (isEditingName) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    singleLine = true,
                    modifier = Modifier.width(130.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedTextColor = TextPrimary,
                      unfocusedTextColor = TextPrimary,
                      focusedBorderColor = NavyPrimary,
                      unfocusedBorderColor = CleanOutline
                    )
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Button(
                    onClick = {
                      if (inputName.isNotBlank()) {
                        onAiNameChange(inputName.trim())
                      }
                      isEditingName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text("OK", fontSize = 10.sp, color = Color.White)
                  }
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Column {
                    Text(text = aiName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Text(text = "AI&ML Studies • Freelance & SMM Copilot", fontSize = 10.sp, color = TextSecondary)
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  IconButton(onClick = { isEditingName = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename AI", tint = TextMuted, modifier = Modifier.size(14.dp))
                  }
                }
              }
            }

            IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Context Summary Pill (Tasks, Calendar date, Files)
          val totalTasks = (uiState as? WorkspaceUiState.Success)?.items?.count { it.type == ItemType.TASK.name } ?: 0
          val totalFiles = (uiState as? WorkspaceUiState.Success)?.items?.count { it.type == ItemType.MEDIA.name } ?: 0
          val todayCal = MalayalamCalendarHelper.getMalayalamDayInfo(LocalDate.now())

          Surface(
            color = NavyLight,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Context: $totalTasks tasks active • $totalFiles files • ${todayCal.malayalamMonthName} ${todayCal.malayalamDayNumber}",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NavyPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Chat log
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(messages) { msg ->
              val isMe = msg.sender == "user"
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 4.dp),
                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
              ) {
                Box(
                  modifier = Modifier
                    .background(
                      if (isMe) NavyPrimary else CleanSurfaceVariant,
                      RoundedCornerShape(12.dp)
                    )
                    .border(
                      1.dp,
                      if (isMe) NavyPrimary else CleanOutline,
                      RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
                ) {
                  Text(
                    text = msg.text,
                    fontSize = 12.sp,
                    color = if (isMe) Color.White else TextPrimary
                  )
                }
              }
            }
            if (isAiGenerating) {
              item {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = NavyPrimary,
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("$aiName is reasoning with Gemini...", fontSize = 11.sp, color = NavyPrimary)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Commands Tailored for Nijo's Polytechnic & Freelance workflow
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              QuickCommandChip("Maths I Formulas") {
                userPrompt = "Explain Unit 1 Matrices & Differentiation for Semester 1 Maths."
              }
              QuickCommandChip("Chemistry Concepts") {
                userPrompt = "Summarize Water Technology & Polymer Chemistry for Semester 1 exam."
              }
              QuickCommandChip("Check Calendar") {
                userPrompt = "What is today's Malayalam date, star, and feast?"
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              QuickCommandChip("Freelance SMM Plan") {
                userPrompt = "Suggest 3 Instagram reels hooks for Libra Brand Master Kit."
              }
              QuickCommandChip("Study Exam Plan") {
                userPrompt = "Create a study plan for Semester 1 exam deadline on Nov 18."
              }
              QuickCommandChip("Send & Receive Files") {
                userPrompt = "How do I send and receive files to Laptop, Mobile, and Smart TV?"
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = userPrompt,
              onValueChange = { userPrompt = it },
              placeholder = { Text("Ask $aiName (Maths, Chemistry, Design)...", fontSize = 11.sp) },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = CleanOutline
              )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
              onClick = {
                if (userPrompt.isNotBlank() && !isAiGenerating) {
                  val prompt = userPrompt.trim()
                  messages.add(AiAssistantMessage(sender = "user", text = prompt))
                  userPrompt = ""

                  val lower = prompt.lowercase()

                  if (lower.contains("share") || lower.contains("beam") || lower.contains("send") || lower.contains("receive") || lower.contains("routing")) {
                    messages.add(
                      AiAssistantMessage(
                        sender = "assistant",
                        text = "File Transfer: In the 'Files' tab, tap SEND or RECEIVE! You can choose Wi-Fi, QuickShare, or Bluetooth and select destinations (Laptop, Mobile, Smart TV) individually or together."
                      )
                    )
                  } else if (lower.contains("calendar") || lower.contains("festival") || lower.contains("date") || lower.contains("star")) {
                    messages.add(
                      AiAssistantMessage(
                        sender = "assistant",
                        text = "Today's Malayalam date: ${todayCal.malayalamMonthName} ${todayCal.malayalamDayNumber}. Nakshatra: ${todayCal.nakshatraMalayalam}. Special: ${todayCal.festival ?: "Regular Academic/Work Day"}."
                      )
                    )
                  } else if (lower.contains("task") || lower.contains("schedule")) {
                    viewModel.createItem(
                      WorkspaceItem(
                        type = ItemType.TASK.name,
                        title = prompt.replace("add task:", "").trim(),
                        content = "Auto-scheduled via $aiName",
                        tags = listOf("today", "ai_assistant")
                      )
                    )
                    messages.add(
                      AiAssistantMessage(
                        sender = "assistant",
                        text = "Added task to your Today manager: '$prompt'"
                      )
                    )
                  } else {
                    // Real Gemini Reasoning call via FirebaseAiManager with fallback
                    isAiGenerating = true
                    coroutineScope.launch {
                      try {
                        val historyList = messages.takeLast(6).map {
                          ChatMessage(role = it.sender, text = it.text)
                        }
                        var botReply = ""
                        aiManager.streamChat(historyList, prompt, useArgonThinking = false).collect { chunk ->
                          botReply = chunk
                        }
                        if (botReply.isNotBlank()) {
                          messages.add(AiAssistantMessage(sender = "assistant", text = botReply))
                        } else {
                          messages.add(
                            AiAssistantMessage(
                              sender = "assistant",
                              text = "NIJO PVT Copilot: Ready to assist with Semester 1 Polytechnic studies, freelance design deliverables, and device routing."
                            )
                          )
                        }
                      } catch (e: Exception) {
                        // Intelligent academic/freelance context fallback
                        val fallback = when {
                          lower.contains("math") || lower.contains("calculus") ->
                            "Engineering Maths I (Sem 1): Key focus areas: 1) Determinants & Matrices (Cramer's rule, inverse), 2) Trigonometry compound angles, 3) Differential Calculus limits and standard derivatives. Regular practice before Nov 18 is recommended!"
                          lower.contains("chem") || lower.contains("polymer") ->
                            "Applied Chemistry (Sem 1): Focus on Water Technology (hardness, EDTA method, boiler feed), Electrochemistry (corrosion prevention), and Engineering Materials (polymers, lubricants)."
                          lower.contains("smm") || lower.contains("freelance") || lower.contains("libra") ->
                            "Freelance Design (Libra Studio): Focus on high-retention reel formats: 3-second hook, visual contrast, brand color accent, and direct call-to-action."
                          else ->
                            "Request acknowledged by $aiName. Ready to support your AI&ML studies, freelance design deliveries, and device file sharing."
                        }
                        messages.add(AiAssistantMessage(sender = "assistant", text = fallback))
                      } finally {
                        isAiGenerating = false
                      }
                    }
                  }
                }
              },
              modifier = Modifier
                .background(NavyPrimary, CircleShape)
                .size(40.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuickCommandChip(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .background(CleanSurfaceVariant, RoundedCornerShape(6.dp))
      .border(1.dp, CleanOutline, RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 4.dp)
  ) {
    Text(text = label, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
  }
}
