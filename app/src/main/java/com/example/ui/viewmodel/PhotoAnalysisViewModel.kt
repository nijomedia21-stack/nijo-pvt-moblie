package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AnalysisResult
import com.example.ai.FirebaseAiManager
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhotoAnalysisUiState {
  data object Idle : PhotoAnalysisUiState
  data object Analyzing : PhotoAnalysisUiState
  data class Success(val result: AnalysisResult) : PhotoAnalysisUiState
  data class Error(val message: String) : PhotoAnalysisUiState
}

class PhotoAnalysisViewModel(
  private val repository: WorkspaceRepository,
  private val aiManager: FirebaseAiManager = FirebaseAiManager()
) : ViewModel() {

  private val _uiState = MutableStateFlow<PhotoAnalysisUiState>(PhotoAnalysisUiState.Idle)
  val uiState: StateFlow<PhotoAnalysisUiState> = _uiState.asStateFlow()

  private val _saveStatus = MutableStateFlow<String?>(null)
  val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()

  fun reset() {
    _uiState.value = PhotoAnalysisUiState.Idle
    _saveStatus.value = null
  }

  fun analyzePhoto(bitmap: Bitmap, prompt: String) {
    viewModelScope.launch {
      _uiState.value = PhotoAnalysisUiState.Analyzing
      aiManager.analyzeImageWithArgon(bitmap, prompt)
        .onSuccess { result ->
          _uiState.value = PhotoAnalysisUiState.Success(result)
        }
        .onFailure { error ->
          _uiState.value = PhotoAnalysisUiState.Error(error.localizedMessage ?: "Image analysis failed.")
        }
    }
  }

  fun saveAsSyncedNote(title: String, analysisText: String, userPrompt: String) {
    viewModelScope.launch {
      val item = WorkspaceItem(
        type = ItemType.NOTE.name,
        title = title.ifBlank { "Gemini 4 Argon Photo Analysis" },
        content = "PROMPT:\n$userPrompt\n\nANALYSIS:\n$analysisText",
        priority = ItemPriority.HIGH.name,
        tags = listOf("photo-analysis", "gemini-4-argon", "high-thinking"),
        aiSummary = "Deep multimodal reasoning inspection"
      )
      repository.createItem(item)
        .onSuccess { _saveStatus.value = "Saved and synchronized across devices!" }
        .onFailure { _saveStatus.value = "Failed to save: ${it.localizedMessage}" }
    }
  }
}
