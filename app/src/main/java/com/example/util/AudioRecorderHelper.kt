package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

object AudioRecorderHelper {

  private var mediaRecorder: MediaRecorder? = null
  private var mediaPlayer: MediaPlayer? = null
  private var currentRecordingFile: File? = null
  private var isRecording = false
  private var isPlaying = false

  fun isCurrentlyRecording(): Boolean = isRecording
  fun isCurrentlyPlaying(): Boolean = isPlaying

  /**
   * Starts recording audio to a private voice note file in the app storage
   */
  fun startRecording(context: Context): Result<File> {
    stopRecording()
    stopPlayback()

    return try {
      val voiceDir = File(context.getExternalFilesDir(null) ?: context.cacheDir, "voice_notes")
      if (!voiceDir.exists()) {
        voiceDir.mkdirs()
      }

      val outputFile = File(voiceDir, "voice_note_${System.currentTimeMillis()}.m4a")
      currentRecordingFile = outputFile

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }

      recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(128000)
        setAudioSamplingRate(44100)
        setOutputFile(outputFile.absolutePath)
        prepare()
        start()
      }

      mediaRecorder = recorder
      isRecording = true
      Result.success(outputFile)
    } catch (e: Exception) {
      currentRecordingFile = null
      isRecording = false
      mediaRecorder?.release()
      mediaRecorder = null
      Result.failure(e)
    }
  }

  /**
   * Stops recording and returns the completed audio file
   */
  fun stopRecording(): File? {
    if (!isRecording) return currentRecordingFile
    return try {
      mediaRecorder?.apply {
        stop()
        reset()
        release()
      }
      mediaRecorder = null
      isRecording = false
      val file = currentRecordingFile
      file
    } catch (_: Exception) {
      mediaRecorder?.release()
      mediaRecorder = null
      isRecording = false
      currentRecordingFile
    }
  }

  /**
   * Plays the recorded audio file with completion callback
   */
  fun playAudio(file: File, onComplete: () -> Unit = {}): Boolean {
    stopPlayback()
    if (!file.exists()) return false

    return try {
      val player = MediaPlayer().apply {
        setDataSource(file.absolutePath)
        prepare()
        setOnCompletionListener {
          this@AudioRecorderHelper.isPlaying = false
          onComplete()
        }
        start()
      }
      mediaPlayer = player
      isPlaying = true
      true
    } catch (_: Exception) {
      isPlaying = false
      mediaPlayer?.release()
      mediaPlayer = null
      false
    }
  }

  /**
   * Stops any ongoing audio playback
   */
  fun stopPlayback() {
    try {
      if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.stop()
      }
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
    isPlaying = false
  }
}
