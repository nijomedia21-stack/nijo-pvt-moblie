package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ItemType {
  NOTE,
  TASK,
  MEDIA
}

enum class ItemPriority {
  LOW,
  MEDIUM,
  HIGH
}

@IgnoreExtraProperties
data class WorkspaceItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val type: String = ItemType.NOTE.name,
  val title: String = "",
  val content: String = "",
  val isCompleted: Boolean = false,
  val priority: String = ItemPriority.MEDIUM.name,
  val tags: List<String> = emptyList(),
  val imageUrl: String? = null,
  val aiSummary: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toCreateMap(uid: String): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "userId" to uid,
      "type" to type,
      "title" to title,
      "content" to content,
      "isCompleted" to isCompleted,
      "priority" to priority,
      "tags" to tags,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (!imageUrl.isNullOrBlank()) map["imageUrl"] = imageUrl
    if (!aiSummary.isNullOrBlank()) map["aiSummary"] = aiSummary
    return map
  }

  fun toUpdateMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "title" to title,
      "content" to content,
      "isCompleted" to isCompleted,
      "priority" to priority,
      "tags" to tags,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (imageUrl != null) map["imageUrl"] = imageUrl
    if (aiSummary != null) map["aiSummary"] = aiSummary
    return map
  }
}
