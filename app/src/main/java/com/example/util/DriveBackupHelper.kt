package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DriveBackupHelper {

  /**
   * Compiles history of Tasks & Study items (excluding shared/received file blobs as requested)
   * into a formatted JSON document and triggers the Android Share / Google Drive upload intent.
   */
  fun exportToGoogleDrive(context: Context, items: List<WorkspaceItem>): Result<File> {
    return try {
      // Filter ONLY tasks, study notes, syllabus topics, and calendar items.
      // EXCLUDE file transfers ("Never store it again. Don't store shared/received files").
      val backupItems = items.filter { item ->
        val isFileTransfer = item.type == ItemType.MEDIA.name ||
          item.tags.contains("file") ||
          item.tags.contains("sender_mobile") ||
          item.tags.contains("sender_laptop") ||
          item.tags.contains("auto_downloaded")

        !isFileTransfer
      }

      val rootJson = JSONObject().apply {
        put("app", "NIJO PVT Antigravity")
        put("version", "2.0")
        put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        put("description", "Workspace Tasks, Study Syllabus & Notes History (Excludes file transfer blobs)")
        put("totalItems", backupItems.size)

        val itemsArray = JSONArray()
        for (it in backupItems) {
          val obj = JSONObject().apply {
            put("id", it.id)
            put("type", it.type)
            put("title", it.title)
            put("content", it.content)
            put("priority", it.priority)
            put("isCompleted", it.isCompleted)
            put("createdAt", it.createdAt?.toDate()?.toString() ?: "")
            put("imageUrl", it.imageUrl ?: "")
          }
          itemsArray.put(obj)
        }
        put("items", itemsArray)
      }

      val backupDir = File(context.cacheDir, "backups")
      if (!backupDir.exists()) {
        backupDir.mkdirs()
      }

      val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val backupFile = File(backupDir, "NIJO_Workspace_Backup_$dateStr.json")

      FileOutputStream(backupFile).use { fos ->
        fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
        fos.flush()
      }

      // Launch Android Share Intent allowing user to pick "Save to Drive", Gmail, or Files
      val fileUri: Uri = try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
      } catch (_: Exception) {
        Uri.fromFile(backupFile)
      }

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_SUBJECT, "NIJO Workspace History Backup ($dateStr)")
        putExtra(Intent.EXTRA_TEXT, "Backup of tasks, study notes, and syllabus history from NIJO PVT app.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = Intent.createChooser(intent, "Save Backup to Google Drive / Files").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)

      Result.success(backupFile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
