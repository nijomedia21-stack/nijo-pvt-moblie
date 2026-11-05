package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val userId: String = "",
  val role: String = "user", // "user" or "model"
  val text: String = "",
  val thinkingText: String? = null,
  val modelUsed: String = "gemini-3.8-flash",
  val hasImage: Boolean = false,
  @ServerTimestamp val timestamp: Timestamp? = null
) {
  fun toCreateMap(uid: String): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "userId" to uid,
      "role" to role,
      "text" to text,
      "modelUsed" to modelUsed,
      "hasImage" to hasImage,
      "timestamp" to FieldValue.serverTimestamp()
    )
    if (!thinkingText.isNullOrBlank()) {
      map["thinkingText"] = thinkingText
    }
    return map
  }
}

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val userId: String = "",
  val email: String = "",
  val displayName: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
