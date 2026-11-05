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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SlateOutline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.ChatHistoryState

@Composable
fun AiChatPane(
  viewModel: AiChatViewModel,
  modifier: Modifier = Modifier
) {
  val chatState by viewModel.chatMessagesState.collectAsState()
  val useArgonThinking by viewModel.useArgonThinking.collectAsState()
  val isStreaming by viewModel.isStreaming.collectAsState()
  val currentStreamingText by viewModel.currentStreamingText.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  var inputMessage by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val messages = when (val s = chatState) {
    is ChatHistoryState.Success -> s.messages
    else -> emptyList()
  }

  // Scroll to bottom when new messages arrive or when streaming updates
  LaunchedEffect(messages.size, currentStreamingText.length) {
    val totalCount = messages.size + if (isStreaming) 1 else 0
    if (totalCount > 0) {
      listState.animateScrollToItem(totalCount - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0C0F15))
      .padding(12.dp)
  ) {
    // Header & Model Selector
    Surface(
      color = SlateSurface,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, SlateOutline, RoundedCornerShape(14.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (useArgonThinking) Icons.Default.Psychology else Icons.Default.SmartToy,
            contentDescription = null,
            tint = if (useArgonThinking) VividViolet else ElectricCyan,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = if (useArgonThinking) "Gemini 4 Argon" else "Gemini 3.8 Flash",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = if (useArgonThinking) "High Thinking Mode (ThinkingLevel.HIGH)" else "Standard Fast Multi-Turn Chat",
              fontSize = 10.sp,
              color = if (useArgonThinking) VividViolet else ElectricCyan
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          FilterChip(
            selected = useArgonThinking,
            onClick = { viewModel.toggleModel() },
            label = { Text(if (useArgonThinking) "Argon" else "Flash", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VividViolet.copy(alpha = 0.25f),
              selectedLabelColor = VividViolet,
              containerColor = Color.Transparent,
              labelColor = TextMuted
            ),
            modifier = Modifier.testTag("chat_model_toggle")
          )

          IconButton(
            onClick = { viewModel.clearHistory() },
            modifier = Modifier
              .size(32.dp)
              .testTag("clear_chat_button")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = TextMuted, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Error Alert if any
    AnimatedVisibility(visible = errorMessage != null) {
      errorMessage?.let { err ->
        Surface(
          color = Color(0xFF381515),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = err, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
              Text("✕", color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (messages.isEmpty() && !isStreaming) {
        item {
          EmptyChatSuggestions(
            onSuggestionClicked = { suggestion ->
              inputMessage = suggestion
              viewModel.sendMessage(suggestion)
              inputMessage = ""
            }
          )
        }
      }

      items(messages, key = { it.id }) { msg ->
        ChatMessageBubble(message = msg)
      }

      // Streaming bubble for assistant currently responding
      if (isStreaming) {
        item {
          StreamingMessageBubble(
            streamingText = currentStreamingText,
            isArgonThinking = useArgonThinking
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Chat Input Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputMessage,
        onValueChange = { inputMessage = it },
        placeholder = {
          Text(
            if (useArgonThinking) "Ask a complex reasoning query..." else "Ask OmniSync Co-Pilot...",
            color = TextMuted,
            fontSize = 13.sp
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = if (useArgonThinking) VividViolet else ElectricCyan,
          unfocusedBorderColor = SlateOutline,
          focusedContainerColor = SlateSurface,
          unfocusedContainerColor = SlateSurface,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        maxLines = 3
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          val textToSend = inputMessage
          inputMessage = ""
          viewModel.sendMessage(textToSend)
        },
        enabled = inputMessage.isNotBlank() && !isStreaming,
        modifier = Modifier
          .size(48.dp)
          .background(
            if (inputMessage.isNotBlank() && !isStreaming) (if (useArgonThinking) VividViolet else ElectricCyan) else Color(0xFF1E2636),
            CircleShape
          )
          .testTag("chat_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = if (inputMessage.isNotBlank() && !isStreaming) Color(0xFF0E1117) else TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.role == "user"
  var isThinkingExpanded by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Surface(
        shape = CircleShape,
        color = if (message.modelUsed.contains("argon")) VividViolet.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f),
        modifier = Modifier
          .size(28.dp)
          .padding(top = 4.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (message.modelUsed.contains("argon")) Icons.Default.Psychology else Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = if (message.modelUsed.contains("argon")) VividViolet else ElectricCyan,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(modifier = Modifier.fillMaxWidth(0.85f)) {
      // High thinking badge & trace toggle
      if (!isUser && !message.thinkingText.isNullOrBlank()) {
        Surface(
          color = Color(0xFF1F1A33),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .padding(bottom = 4.dp)
            .clickable { isThinkingExpanded = !isThinkingExpanded }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = VividViolet, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("High Thinking Trace", fontSize = 10.sp, color = VividViolet, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = if (isThinkingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = VividViolet,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        if (isThinkingExpanded) {
          Surface(
            color = Color(0xFF151224),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          ) {
            Text(
              text = message.thinkingText,
              fontSize = 11.sp,
              color = Color(0xFFD1C4E9),
              modifier = Modifier.padding(8.dp),
              lineHeight = 15.sp
            )
          }
        }
      }

      Surface(
        color = if (isUser) Color(0xFF1E3A4A) else SlateSurfaceVariant,
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        modifier = Modifier.border(
          width = 1.dp,
          color = if (isUser) ElectricCyan.copy(alpha = 0.4f) else SlateOutline,
          shape = RoundedCornerShape(16.dp)
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = message.text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (isUser) "You" else message.modelUsed,
            fontSize = 9.sp,
            color = if (isUser) ElectricCyan.copy(alpha = 0.8f) else TextMuted
          )
        }
      }
    }
  }
}

@Composable
private fun StreamingMessageBubble(
  streamingText: String,
  isArgonThinking: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start
  ) {
    Surface(
      shape = CircleShape,
      color = (if (isArgonThinking) VividViolet else ElectricCyan).copy(alpha = 0.2f),
      modifier = Modifier
        .size(28.dp)
        .padding(top = 4.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          color = if (isArgonThinking) VividViolet else ElectricCyan,
          strokeWidth = 2.dp,
          modifier = Modifier.size(16.dp)
        )
      }
    }
    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      color = SlateSurfaceVariant,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .border(1.dp, (if (isArgonThinking) VividViolet else ElectricCyan).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        if (streamingText.isBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              if (isArgonThinking) "Engaging gemini-4-argon deep reasoning..." else "Thinking...",
              fontSize = 12.sp,
              color = if (isArgonThinking) VividViolet else ElectricCyan,
              fontWeight = FontWeight.Medium
            )
          }
        } else {
          Text(
            text = streamingText,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyChatSuggestions(
  onSuggestionClicked: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = CircleShape,
      color = Color(0xFF1E2638),
      modifier = Modifier.size(56.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(28.dp))
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text("OmniSync AI Co-Pilot", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      "Synchronized conversational assistant across mobile, laptop, and TV screens.",
      fontSize = 12.sp,
      color = TextMuted,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))

    val suggestions = listOf(
      "Explain how real-time Firestore sync operates between TV and mobile",
      "Plan a synchronized project sprint for our cross-device workspace",
      "Structure a multi-device research note with actionable tasks"
    )

    suggestions.forEach { prompt ->
      Surface(
        color = SlateSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .border(1.dp, SlateOutline, RoundedCornerShape(12.dp))
          .clickable { onSuggestionClicked(prompt) }
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(prompt, fontSize = 12.sp, color = TextSecondary)
        }
      }
    }
  }
}
