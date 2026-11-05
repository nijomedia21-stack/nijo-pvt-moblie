package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.FirebaseAiManager
import com.example.data.model.ChatMessage
import com.example.data.repository.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChatHistoryState {
  data object Loading : ChatHistoryState
  data class Success(val messages: List<ChatMessage>) : ChatHistoryState
  data class Error(val message: String) : ChatHistoryState
}

class AiChatViewModel(
  private val repository: WorkspaceRepository,
  private val aiManager: FirebaseAiManager = FirebaseAiManager()
) : ViewModel() {

  val useArgonThinking = MutableStateFlow(true) // Defaults to gemini-4-argon with ThinkingLevel.HIGH

  private val _isStreaming = MutableStateFlow(false)
  val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  private val _currentStreamingText = MutableStateFlow("")
  val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  val chatMessagesState: StateFlow<ChatHistoryState> = repository.observeChatMessages()
    .map<List<ChatMessage>, ChatHistoryState> { ChatHistoryState.Success(it) }
    .catch { emit(ChatHistoryState.Error(it.localizedMessage ?: "Failed to load chat history.")) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = ChatHistoryState.Loading
    )

  fun toggleModel() {
    useArgonThinking.value = !useArgonThinking.value
  }

  fun clearError() {
    _errorMessage.value = null
  }

  fun sendMessage(prompt: String) {
    if (prompt.isBlank() || _isStreaming.value) return

    val currentHistory = when (val state = chatMessagesState.value) {
      is ChatHistoryState.Success -> state.messages
      else -> emptyList()
    }

    viewModelScope.launch {
      val isArgon = useArgonThinking.value
      val modelName = if (isArgon) "gemini-4-argon" else "gemini-3.8-flash"

      // 1. Save user prompt to Firestore
      val userMsg = ChatMessage(
        role = "user",
        text = prompt.trim(),
        modelUsed = modelName,
        hasImage = false
      )
      repository.addChatMessage(userMsg)

      // 2. Stream AI response
      _isStreaming.value = true
      _currentStreamingText.value = ""
      var finalAiResponse = ""

      try {
        aiManager.streamChat(
          history = currentHistory + userMsg,
          userMessage = prompt.trim(),
          useArgonThinking = isArgon
        ).collect { chunk ->
          finalAiResponse = chunk
          _currentStreamingText.value = chunk
        }

        // 3. Save final model response to Firestore
        if (finalAiResponse.isNotBlank()) {
          val modelMsg = ChatMessage(
            role = "model",
            text = finalAiResponse,
            thinkingText = if (isArgon) "Synthesized with gemini-4-argon using high thinking level" else null,
            modelUsed = modelName,
            hasImage = false
          )
          repository.addChatMessage(modelMsg)
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _errorMessage.value = e.localizedMessage ?: "AI response generation failed."
      } finally {
        _isStreaming.value = false
        _currentStreamingText.value = ""
      }
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      repository.clearChatHistory()
    }
  }
}
