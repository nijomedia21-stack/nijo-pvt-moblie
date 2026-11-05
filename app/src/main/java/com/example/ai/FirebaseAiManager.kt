package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "FirebaseAiManager"

data class AnalysisResult(
  val text: String,
  val thinkingSummary: String?,
  val modelUsed: String
)

class FirebaseAiManager {

  // gemini-4-argon with high thinking mode for complex reasoning and image understanding
  val argonModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-4-argon",
      generationConfig = generationConfig {
        thinkingConfig = thinkingConfig {
          thinkingLevel = ThinkingLevel.HIGH
        }
      },
      systemInstruction = content {
        text("You are OmniSync High-Intelligence Architect, an advanced multimodal reasoning engine synchronized across mobile, tablet, and TV devices. Analyze queries, documents, and images deeply with rigorous structural logic.")
      }
    )
  }

  // gemini-3.8-flash for versatile multi-turn workspace conversations
  val flashModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      systemInstruction = content {
        text("You are OmniSync Workspace Co-Pilot, an agile productivity assistant helping users coordinate notes, manage tasks, and streamline work across their devices.")
      }
    )
  }

  // gemini-3.1-flash-lite for rapid one-shot summarization and task extraction
  val liteModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.1-flash-lite",
      systemInstruction = content {
        text("You are OmniSync Quick-Action Engine. Provide ultra-concise summaries, title suggestions, and actionable task extractions.")
      }
    )
  }

  suspend fun analyzeImageWithArgon(bitmap: Bitmap, prompt: String): Result<AnalysisResult> {
    return try {
      val effectivePrompt = if (prompt.isNotBlank()) prompt else "Analyze this image thoroughly, explain the key contents, and suggest actionable workspace next steps."
      val response = withTimeout(60_000L) {
        argonModel.generateContent(
          content {
            image(bitmap)
            text(effectivePrompt)
          }
        )
      }
      val text = response.text ?: "No analysis text generated."
      val thoughts = response.thoughtSummary
      Result.success(AnalysisResult(text = text, thinkingSummary = thoughts, modelUsed = "gemini-4-argon"))
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Image analysis timed out", e)
      Result.failure(Exception("Image analysis timed out after 60 seconds."))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Image analysis with gemini-4-argon failed", e)
      Result.failure(e)
    }
  }

  fun streamChat(
    history: List<ChatMessage>,
    userMessage: String,
    useArgonThinking: Boolean
  ): Flow<String> = flow {
    val activeModel = if (useArgonThinking) argonModel else flashModel

    // Map conversation history into Firebase AI Content objects
    val historyContents: List<Content> = history.takeLast(16).map { msg ->
      content(msg.role) {
        text(msg.text)
      }
    }

    val chatSession = activeModel.startChat(history = historyContents)
    val accumulated = StringBuilder()

    withTimeout(60_000L) {
      chatSession.sendMessageStream(userMessage).collect { chunk ->
        val chunkText = chunk.text.orEmpty()
        accumulated.append(chunkText)
        emit(accumulated.toString())
      }
    }
  }

  suspend fun quickSummarize(content: String): Result<String> {
    return try {
      val response = withTimeout(30_000L) {
        liteModel.generateContent("Summarize the following workspace content in 2-3 concise bullet points:\n\n$content")
      }
      Result.success(response.text ?: "Summary generated.")
    } catch (e: TimeoutCancellationException) {
      Result.failure(Exception("Quick summary timed out."))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Quick summary failed", e)
      Result.failure(e)
    }
  }

  suspend fun extractTasks(content: String): Result<List<String>> {
    return try {
      val response = withTimeout(30_000L) {
        liteModel.generateContent("Extract 3-5 distinct actionable checklist tasks from this content. Return each task on a new line without bullet symbols:\n\n$content")
      }
      val lines = response.text?.lines()
        ?.map { it.trim().trimStart('-', '*', '•', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' ') }
        ?.filter { it.isNotBlank() } ?: emptyList()
      Result.success(lines)
    } catch (e: TimeoutCancellationException) {
      Result.failure(Exception("Task extraction timed out."))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Task extraction failed", e)
      Result.failure(e)
    }
  }
}
