package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
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
import java.util.UUID

class WorkspaceRepository(
  private val db: FirebaseFirestore
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      FirebaseApp.getInstance(),
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google to access workspace items.")
  }

  fun observeItems(): Flow<List<WorkspaceItem>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/items"
    emitAll(
      db.collection("users").document(uid).collection("items")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(WorkspaceItem::class.java) }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
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
    return try {
      val uid = requireUserId()
      val itemId = if (item.id.isNotBlank()) item.id else "item_${UUID.randomUUID()}"
      val path = "users/$uid/items/$itemId"
      val docRef = db.collection("users").document(uid).collection("items").document(itemId)
      val itemWithId = item.copy(id = itemId)
      docRef.set(itemWithId.toCreateMap(uid)).await()
      Result.success(itemId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "anonymous"
      handleFirestoreError(e, OperationType.CREATE, "users/$uid/items")
      Result.failure(e)
    }
  }

  suspend fun updateItem(item: WorkspaceItem): Result<Unit> {
    return try {
      val uid = requireUserId()
      require(item.id.isNotBlank()) { "Item ID must not be blank for update" }
      val docRef = db.collection("users").document(uid).collection("items").document(item.id)
      docRef.update(item.toUpdateMap()).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "anonymous"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid/items/${item.id}")
      Result.failure(e)
    }
  }

  suspend fun deleteItem(itemId: String): Result<Unit> {
    return try {
      val uid = requireUserId()
      val path = "users/$uid/items/$itemId"
      db.collection("users").document(uid).collection("items").document(itemId).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "anonymous"
      handleFirestoreError(e, OperationType.DELETE, "users/$uid/items/$itemId")
      Result.failure(e)
    }
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
}
