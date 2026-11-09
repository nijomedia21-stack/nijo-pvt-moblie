package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WorkspaceRepository(
  private val db: FirebaseFirestore,
  private val context: Context? = null
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      FirebaseApp.getInstance(),
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    context.applicationContext
  )

  private val auth = Firebase.auth

  // In-memory local cache for instant offline access
  private val localItemsMap = LinkedHashMap<String, WorkspaceItem>()

  // SharedPreferences for persistent on-device offline storage across restarts
  private val sharedPrefs: SharedPreferences? by lazy {
    context?.getSharedPreferences("nijo_offline_workspace_cache", Context.MODE_PRIVATE)
  }

  init {
    loadPersistedLocalItems()
  }

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google to access workspace items.")
  }

  private fun loadPersistedLocalItems() {
    val jsonStr = sharedPrefs?.getString("cached_workspace_items_json", null) ?: return
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val item = jsonToWorkspaceItem(obj)
        if (item.id.isNotBlank()) {
          localItemsMap[item.id] = item
        }
      }
    } catch (_: Exception) {}
  }

  private fun persistLocalItems() {
    val prefs = sharedPrefs ?: return
    try {
      val jsonArray = JSONArray()
      for (item in localItemsMap.values) {
        jsonArray.put(workspaceItemToJson(item))
      }
      prefs.edit().putString("cached_workspace_items_json", jsonArray.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun getPendingSyncQueue(): MutableList<JSONObject> {
    val jsonStr = sharedPrefs?.getString("pending_sync_queue", "[]") ?: "[]"
    val list = mutableListOf<JSONObject>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        list.add(array.getJSONObject(i))
      }
    } catch (_: Exception) {}
    return list
  }

  private fun savePendingSyncQueue(queue: List<JSONObject>) {
    val prefs = sharedPrefs ?: return
    val array = JSONArray()
    for (obj in queue) {
      array.put(obj)
    }
    prefs.edit().putString("pending_sync_queue", array.toString()).apply()
  }

  private fun enqueuePendingOperation(action: String, item: WorkspaceItem) {
    val queue = getPendingSyncQueue()
    val op = JSONObject().apply {
      put("action", action)
      put("itemId", item.id)
      put("timestamp", System.currentTimeMillis())
      put("itemJson", workspaceItemToJson(item))
    }
    queue.add(op)
    savePendingSyncQueue(queue)
  }

  fun getPendingSyncCount(): Int {
    return getPendingSyncQueue().size
  }

  /**
   * Observes items with offline-first strategy:
   * 1. If signed in, streams from Firestore and updates local cache.
   * 2. If unauthenticated / offline, emits local cached items so user never loses data.
   */
  fun observeItems(): Flow<List<WorkspaceItem>> = flow {
    val currentUser = auth.currentUser
    if (currentUser == null) {
      // Offline mode or not yet signed in: emit local cached items
      emit(localItemsMap.values.toList())
      return@flow
    }

    val uid = currentUser.uid
    val path = "users/$uid/items"

    // Emit initial cached items immediately for zero-latency screen load
    if (localItemsMap.isNotEmpty()) {
      emit(localItemsMap.values.toList())
    }

    emitAll(
      db.collection("users").document(uid).collection("items")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
          val cloudItems = snapshot.toObjects(WorkspaceItem::class.java)
          // Update local cache with latest cloud items
          for (ci in cloudItems) {
            localItemsMap[ci.id] = ci
          }
          persistLocalItems()
          localItemsMap.values.toList()
        }
        .catch { error ->
          // If Firestore network fails (offline), gracefully fall back to local cached items
          if (localItemsMap.isNotEmpty()) {
            emit(localItemsMap.values.toList())
          } else {
            if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
            throw error
          }
        }
    )
  }

  fun observeChatMessages(): Flow<List<ChatMessage>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/chat_messages"
    emitAll(
      db.collection("users").document(uid).collection("chat_messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun createItem(item: WorkspaceItem): Result<String> {
    val itemId = if (item.id.isNotBlank()) item.id else "item_${UUID.randomUUID()}"
    val itemWithId = item.copy(id = itemId)

    // Optimistically store in local cache immediately
    localItemsMap[itemId] = itemWithId
    persistLocalItems()

    val currentUser = auth.currentUser
    if (currentUser == null) {
      // Offline mode: queue for later cloud synchronization
      enqueuePendingOperation("CREATE", itemWithId)
      return Result.success(itemId)
    }

    return try {
      val uid = currentUser.uid
      val docRef = db.collection("users").document(uid).collection("items").document(itemId)
      docRef.set(itemWithId.toCreateMap(uid)).await()
      Result.success(itemId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = currentUser.uid
      handleFirestoreError(e, OperationType.CREATE, "users/$uid/items")
      // If network failure, queue for sync
      enqueuePendingOperation("CREATE", itemWithId)
      Result.failure(e)
    }
  }

  suspend fun updateItem(item: WorkspaceItem): Result<Unit> {
    require(item.id.isNotBlank()) { "Item ID must not be blank for update" }

    // Update in local cache immediately
    localItemsMap[item.id] = item
    persistLocalItems()

    val currentUser = auth.currentUser
    if (currentUser == null) {
      // Offline mode: queue for later sync
      enqueuePendingOperation("UPDATE", item)
      return Result.success(Unit)
    }

    return try {
      val uid = currentUser.uid
      val docRef = db.collection("users").document(uid).collection("items").document(item.id)
      docRef.update(item.toUpdateMap()).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = currentUser.uid
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid/items/${item.id}")
      enqueuePendingOperation("UPDATE", item)
      Result.failure(e)
    }
  }

  suspend fun deleteItem(itemId: String): Result<Unit> {
    val removedItem = localItemsMap.remove(itemId)
    persistLocalItems()

    val currentUser = auth.currentUser
    if (currentUser == null) {
      if (removedItem != null) {
        enqueuePendingOperation("DELETE", removedItem)
      }
      return Result.success(Unit)
    }

    return try {
      val uid = currentUser.uid
      val path = "users/$uid/items/$itemId"
      db.collection("users").document(uid).collection("items").document(itemId).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = currentUser.uid
      handleFirestoreError(e, OperationType.DELETE, "users/$uid/items/$itemId")
      if (removedItem != null) {
        enqueuePendingOperation("DELETE", removedItem)
      }
      Result.failure(e)
    }
  }

  /**
   * Intelligently merges and replays pending offline modifications to Firestore
   * when reconnecting to the internet.
   */
  suspend fun syncPendingOfflineChanges(): Result<Int> {
    val currentUser = auth.currentUser ?: return Result.failure(Exception("Cannot sync without authentication"))
    val uid = currentUser.uid
    val queue = getPendingSyncQueue()
    if (queue.isEmpty()) return Result.success(0)

    val remainingQueue = mutableListOf<JSONObject>()
    var syncedCount = 0

    for (op in queue) {
      val action = op.optString("action", "CREATE")
      val itemId = op.optString("itemId", "")
      val itemJson = op.optJSONObject("itemJson")
      val item = if (itemJson != null) jsonToWorkspaceItem(itemJson) else null

      try {
        when (action) {
          "CREATE" -> {
            if (item != null) {
              val docRef = db.collection("users").document(uid).collection("items").document(item.id)
              docRef.set(item.toCreateMap(uid)).await()
              syncedCount++
            }
          }
          "UPDATE" -> {
            if (item != null) {
              val docRef = db.collection("users").document(uid).collection("items").document(item.id)
              docRef.update(item.toUpdateMap()).await()
              syncedCount++
            }
          }
          "DELETE" -> {
            if (itemId.isNotBlank()) {
              db.collection("users").document(uid).collection("items").document(itemId).delete().await()
              syncedCount++
            }
          }
        }
      } catch (e: Exception) {
        // Keep in queue if it still failed
        remainingQueue.add(op)
      }
    }

    savePendingSyncQueue(remainingQueue)
    return Result.success(syncedCount)
  }

  suspend fun addChatMessage(message: ChatMessage): Result<String> {
    return try {
      val uid = requireUserId()
      val msgId = if (message.id.isNotBlank()) message.id else "msg_${UUID.randomUUID()}"
      val docRef = db.collection("users").document(uid).collection("chat_messages").document(msgId)
      val msgWithId = message.copy(id = msgId)
      docRef.set(msgWithId.toCreateMap(uid)).await()
      Result.success(msgId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "anonymous"
      handleFirestoreError(e, OperationType.CREATE, "users/$uid/chat_messages")
      Result.failure(e)
    }
  }

  suspend fun clearChatHistory(): Result<Unit> {
    return try {
      val uid = requireUserId()
      val collectionRef = db.collection("users").document(uid).collection("chat_messages")
      val snapshot = collectionRef.get().await()
      for (doc in snapshot.documents) {
        doc.reference.delete().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "anonymous"
      handleFirestoreError(e, OperationType.DELETE, "users/$uid/chat_messages")
      Result.failure(e)
    }
  }

  companion object {
    private fun workspaceItemToJson(item: WorkspaceItem): JSONObject {
      return JSONObject().apply {
        put("id", item.id)
        put("userId", item.userId)
        put("type", item.type)
        put("title", item.title)
        put("content", item.content)
        put("isCompleted", item.isCompleted)
        put("priority", item.priority)
        put("tags", JSONArray(item.tags))
        if (item.imageUrl != null) put("imageUrl", item.imageUrl)
        if (item.aiSummary != null) put("aiSummary", item.aiSummary)
      }
    }

    private fun jsonToWorkspaceItem(obj: JSONObject): WorkspaceItem {
      val tagsList = mutableListOf<String>()
      val tagsArray = obj.optJSONArray("tags")
      if (tagsArray != null) {
        for (i in 0 until tagsArray.length()) {
          tagsList.add(tagsArray.getString(i))
        }
      }
      return WorkspaceItem(
        id = obj.optString("id", ""),
        userId = obj.optString("userId", ""),
        type = obj.optString("type", ItemType.NOTE.name),
        title = obj.optString("title", ""),
        content = obj.optString("content", ""),
        isCompleted = obj.optBoolean("isCompleted", false),
        priority = obj.optString("priority", ItemPriority.MEDIUM.name),
        tags = tagsList,
        imageUrl = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) obj.optString("imageUrl") else null,
        aiSummary = if (obj.has("aiSummary") && !obj.isNull("aiSummary")) obj.optString("aiSummary") else null
      )
    }
  }
}
