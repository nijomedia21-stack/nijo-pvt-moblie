package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.FirebaseAiManager
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WorkspaceUiState {
  data object Loading : WorkspaceUiState
  data class Success(val items: List<WorkspaceItem>) : WorkspaceUiState
  data class Error(val message: String) : WorkspaceUiState
}

enum class AppWorkspaceMode {
  STUDY_POLYTECHNIC, // AI & ML Semester 1 (Maths, Chemistry, Physics, Python) - Target Nov 18
  FREELANCE_DESIGNER // SMM, Branding Assets, Libra Client, Trending AI Video prompts
}

class WorkspaceViewModel(
  private val repository: WorkspaceRepository,
  private val aiManager: FirebaseAiManager = FirebaseAiManager()
) : ViewModel() {

  val selectedTab = MutableStateFlow("ALL") // "ALL", "NOTE", "TASK", "MEDIA"
  val searchQuery = MutableStateFlow("")

  // Instagram-style Dual Mode Switcher
  private val _workspaceMode = MutableStateFlow(AppWorkspaceMode.STUDY_POLYTECHNIC)
  val workspaceMode: StateFlow<AppWorkspaceMode> = _workspaceMode.asStateFlow()

  // 99.99% Transfer Security PIN
  private val _transferSecurityPin = MutableStateFlow("8492")
  val transferSecurityPin: StateFlow<String> = _transferSecurityPin.asStateFlow()

  private val _actionMessage = MutableStateFlow<String?>(null)
  val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

  private val _isAiProcessing = MutableStateFlow(false)
  val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

  val itemsState: StateFlow<WorkspaceUiState> = combine(
    repository.observeItems(),
    selectedTab,
    searchQuery
  ) { allItems, tab, query ->
    val filtered = allItems.filter { item ->
      val matchesTab = when (tab) {
        "ALL" -> true
        "NOTE" -> item.type == ItemType.NOTE.name
        "TASK" -> item.type == ItemType.TASK.name
        "MEDIA" -> item.type == ItemType.MEDIA.name
        else -> true
      }
      val matchesQuery = if (query.isBlank()) true else {
        item.title.contains(query, ignoreCase = true) ||
          item.content.contains(query, ignoreCase = true) ||
          item.tags.any { it.contains(query, ignoreCase = true) }
      }
      matchesTab && matchesQuery
    }
    val resultState: WorkspaceUiState = WorkspaceUiState.Success(filtered)
    resultState
  }
    .catch { emit(WorkspaceUiState.Error(it.localizedMessage ?: "Failed to observe workspace items.")) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = WorkspaceUiState.Loading
    )

  val uiState: StateFlow<WorkspaceUiState>
    get() = itemsState

  fun switchWorkspaceMode(mode: AppWorkspaceMode) {
    _workspaceMode.value = mode
    _actionMessage.value = when (mode) {
      AppWorkspaceMode.STUDY_POLYTECHNIC -> "🎓 switched to Polytechnic AI/ML Study Mode"
      AppWorkspaceMode.FREELANCE_DESIGNER -> "🎨 Switched to Freelance Designer Mode (Libra)"
    }
  }

  fun updateTransferSecurityPin(newPin: String) {
    if (newPin.length >= 4) {
      _transferSecurityPin.value = newPin
      _actionMessage.value = "🔒 Security PIN updated to $newPin"
    }
  }

  fun createItem(item: WorkspaceItem) {
    viewModelScope.launch {
      repository.createItem(item)
        .onSuccess { _actionMessage.value = "Synced across Phone, Laptop & TV" }
        .onFailure { _actionMessage.value = "Sync failed: ${it.localizedMessage}" }
    }
  }

  fun clearActionMessage() {
    _actionMessage.value = null
  }

  fun createNote(title: String, content: String, tags: List<String> = emptyList()) {
    viewModelScope.launch {
      val item = WorkspaceItem(
        type = ItemType.NOTE.name,
        title = title.ifBlank { "Untitled Note" },
        content = content,
        priority = ItemPriority.MEDIUM.name,
        tags = tags
      )
      repository.createItem(item)
        .onSuccess { _actionMessage.value = "Note synced across devices" }
        .onFailure { _actionMessage.value = "Failed to sync note: ${it.localizedMessage}" }
    }
  }

  fun createTask(
    title: String,
    priority: ItemPriority = ItemPriority.MEDIUM,
    folder: String = "general",
    tags: List<String> = emptyList()
  ) {
    viewModelScope.launch {
      val item = WorkspaceItem(
        type = ItemType.TASK.name,
        title = title.ifBlank { "New Task" },
        content = "Folder: $folder",
        isCompleted = false,
        priority = priority.name,
        tags = listOf("task", folder.lowercase()) + tags
      )
      repository.createItem(item)
        .onSuccess { _actionMessage.value = "Task created and synced" }
        .onFailure { _actionMessage.value = "Failed to create task: ${it.localizedMessage}" }
    }
  }

  fun toggleTaskCompletion(item: WorkspaceItem) {
    viewModelScope.launch {
      val updated = item.copy(isCompleted = !item.isCompleted)
      repository.updateItem(updated)
    }
  }

  fun updateItem(item: WorkspaceItem) {
    viewModelScope.launch {
      repository.updateItem(item)
        .onSuccess { _actionMessage.value = "Changes saved to cloud" }
        .onFailure { _actionMessage.value = "Update failed: ${it.localizedMessage}" }
    }
  }

  fun deleteItem(itemId: String) {
    viewModelScope.launch {
      repository.deleteItem(itemId)
        .onSuccess { _actionMessage.value = "Item removed" }
        .onFailure { _actionMessage.value = "Delete failed: ${it.localizedMessage}" }
    }
  }

  fun summarizeNoteWithAi(item: WorkspaceItem) {
    viewModelScope.launch {
      _isAiProcessing.value = true
      aiManager.quickSummarize(item.content.ifBlank { item.title })
        .onSuccess { summary ->
          val updated = item.copy(aiSummary = summary)
          repository.updateItem(updated)
          _actionMessage.value = "AI Summary added and synced"
        }
        .onFailure {
          _actionMessage.value = "AI summary failed: ${it.localizedMessage}"
        }
      _isAiProcessing.value = false
    }
  }

  fun extractChecklistTasks(item: WorkspaceItem) {
    viewModelScope.launch {
      _isAiProcessing.value = true
      aiManager.extractTasks(item.content)
        .onSuccess { tasks ->
          tasks.forEach { taskTitle ->
            val task = WorkspaceItem(
              type = ItemType.TASK.name,
              title = taskTitle,
              content = "Extracted from '${item.title}'",
              isCompleted = false,
              priority = ItemPriority.MEDIUM.name,
              tags = listOf("ai-extracted")
            )
            repository.createItem(task)
          }
          _actionMessage.value = "Extracted and synced ${tasks.size} tasks"
        }
        .onFailure {
          _actionMessage.value = "Task extraction failed: ${it.localizedMessage}"
        }
      _isAiProcessing.value = false
    }
  }
}
