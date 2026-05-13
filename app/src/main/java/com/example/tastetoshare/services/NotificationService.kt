package com.example.tastetoshare.services

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.tastetoshare.R
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.uiClasses.UINotification

class NotificationService(
    private val context: Context
)
{
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(notification: UINotification) {
        val channel = when (notification.type) {
            NotificationType.NEW_REVIEW -> "social_channel_id"
            NotificationType.DUPLICATED_RECIPE -> "social_channel_id"
            NotificationType.RECOMMENDED_RECIPE -> "recommendations_channel_id"
        }

        val icon = when (notification.type) {
            NotificationType.NEW_REVIEW -> R.drawable.comment
            NotificationType.DUPLICATED_RECIPE -> R.drawable.saved_recipe
            NotificationType.RECOMMENDED_RECIPE -> R.drawable.pan_pot
        }

        val notificationBuilder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(icon)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setAutoCancel(true)

        notificationManager.notify(notification.notificationId.hashCode(), notificationBuilder.build())
    }
}