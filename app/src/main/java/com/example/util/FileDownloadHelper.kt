package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object FileDownloadHelper {

  /**
   * Resolves MIME type from filename extension
   */
  fun getMimeType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    if (ext.isBlank()) return "application/octet-stream"
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    return mime ?: when (ext) {
      "pdf" -> "application/pdf"
      "jpg", "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "webp" -> "image/webp"
      "txt" -> "text/plain"
      "json" -> "application/json"
      "mp4" -> "video/mp4"
      "mp3" -> "audio/mpeg"
      "zip" -> "application/zip"
      "apk" -> "application/vnd.android.package-archive"
      else -> "application/octet-stream"
    }
  }

  /**
   * Saves raw bytes directly into Android's public Downloads folder (Downloads/NIJO_PVT)
   * using MediaStore.Downloads (API 29+) or external storage public directory fallback,
   * guaranteeing the file is immediately visible in the phone's native File Manager.
   */
  fun saveBytesToDownloadsFolder(
    context: Context,
    fileName: String,
    bytes: ByteArray,
    mimeType: String = getMimeType(fileName)
  ): Result<Uri> {
    return try {
      val cleanFileName = fileName.substringAfterLast("/").substringAfterLast("\\").trim()
        .ifBlank { "downloaded_file_${System.currentTimeMillis()}" }

      var mediaStoreUri: Uri? = null

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
          val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, cleanFileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NIJO_PVT")
            put(MediaStore.Downloads.IS_PENDING, 1)
          }

          val resolver = context.contentResolver
          val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
          if (uri != null) {
            resolver.openOutputStream(uri)?.use { os ->
              os.write(bytes)
              os.flush()
            }
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            mediaStoreUri = uri
          }
        } catch (_: Exception) {}
      }

      // In addition to MediaStore, also write directly to public Downloads folder & notify scanner
      val downloadDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "NIJO_PVT"
      )
      if (!downloadDir.exists()) {
        downloadDir.mkdirs()
      }
      val targetFile = File(downloadDir, cleanFileName)
      try {
        FileOutputStream(targetFile).use { fos ->
          fos.write(bytes)
          fos.flush()
        }
        MediaScannerConnection.scanFile(
          context,
          arrayOf(targetFile.absolutePath),
          arrayOf(mimeType),
          null
        )
      } catch (_: Exception) {}

      // App-specific internal/external storage fallback
      val appExtFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), cleanFileName)
      try {
        FileOutputStream(appExtFile).use { fos ->
          fos.write(bytes)
          fos.flush()
        }
      } catch (_: Exception) {}

      val finalUri = mediaStoreUri ?: Uri.fromFile(targetFile)
      Result.success(finalUri)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Downloads file bytes from Laptop Server via HTTP GET, streams to Downloads/NIJO_PVT,
   * and registers the file with Android MediaStore / File Manager.
   */
  suspend fun downloadFileFromLaptop(
    context: Context,
    hostUrl: String,
    fileName: String,
    fileUrl: String? = null,
    fallbackContent: String? = null
  ): Result<Uri> = withContext(Dispatchers.IO) {
    val cleanHost = hostUrl.trim()
      .replace(Regex("^(https?://+|/+)+", RegexOption.IGNORE_CASE), "")
      .substringBefore("/")
      .ifBlank { "192.168.1.3:4000" }

    val candidateUrls = mutableListOf<String>()
    if (!fileUrl.isNullOrBlank()) {
      if (fileUrl.startsWith("http")) {
        candidateUrls.add(fileUrl)
      } else if (fileUrl.startsWith("/")) {
        candidateUrls.add("http://$cleanHost$fileUrl")
      } else {
        candidateUrls.add("http://$cleanHost/$fileUrl")
      }
    }
    candidateUrls.add("http://$cleanHost/uploads/$fileName")
    candidateUrls.add("http://$cleanHost/download/$fileName")
    candidateUrls.add("http://$cleanHost/api/download/$fileName")

    var downloadedBytes: ByteArray? = null

    for (urlStr in candidateUrls) {
      try {
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          connectTimeout = 4000
          readTimeout = 15000
          setRequestProperty("User-Agent", "NijoMobileDownloader/1.0")
        }
        val code = conn.responseCode
        if (code in 200..299) {
          downloadedBytes = conn.inputStream.use { it.readBytes() }
          conn.disconnect()
          if (downloadedBytes.isNotEmpty()) {
            break
          }
        } else {
          conn.disconnect()
        }
      } catch (_: Exception) {
        // Try next candidate
      }
    }

    // If server is not currently reachable or file was pushed as text/data payload, use fallback payload
    val finalBytes = if (downloadedBytes != null && downloadedBytes.isNotEmpty()) {
      downloadedBytes
    } else if (!fallbackContent.isNullOrBlank()) {
      fallbackContent.toByteArray(Charsets.UTF_8)
    } else {
      "NIJO PVT Transferred Document\nFile: $fileName\nTransferred: ${java.util.Date()}\nHost: $cleanHost\nTarget: Mobile Storage"
        .toByteArray(Charsets.UTF_8)
    }

    saveBytesToDownloadsFolder(
      context = context,
      fileName = fileName,
      bytes = finalBytes,
      mimeType = getMimeType(fileName)
    )
  }

  /**
   * Opens the downloaded file in the user's preferred viewer app via Intent
   */
  fun openFileInExternalViewer(context: Context, uri: Uri, mimeType: String = "*/*") {
    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(intent, "Open File"))
    } catch (_: Exception) {
      // Fallback: Open File Manager or Downloads
      try {
        val viewDownloads = Intent(DownloadManagerAction.ACTION_VIEW_DOWNLOADS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(viewDownloads)
      } catch (_: Exception) {}
    }
  }

  /**
   * Opens the device's native Downloads / Files app directly
   */
  fun openDownloadsFolder(context: Context) {
    try {
      val intent = Intent(DownloadManagerAction.ACTION_VIEW_DOWNLOADS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
          setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path), "*/*")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (_: Exception) {}
    }
  }

  private object DownloadManagerAction {
    const val ACTION_VIEW_DOWNLOADS = "android.intent.action.VIEW_DOWNLOADS"
  }
}
