package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object SystemNotificationHelper {

  private const val CHANNEL_ID = "nijo_antigravity_updates"
  private const val CHANNEL_NAME = "Antigravity System Updates"
  private const val CHANNEL_DESC = "Real-time remote UI updates and system notifications from Antigravity Hub"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        enableLights(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  fun showSystemNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
  ) {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("source", "antigravity_notification")
      putExtra("notification_title", title)
      putExtra("notification_message", message)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    try {
      manager?.notify(notificationId, notification)
    } catch (_: Exception) {}
  }
}
