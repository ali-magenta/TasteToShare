package com.example.tastetoshare.data.uiClasses

import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.entities.Notification
import com.example.tastetoshare.data.entities.Recipe
import kotlin.String

data class UINotification(
    val notificationId: String,
    val creationDate: Long,
    val userId: String,
    val recipe: Recipe,
    val type: NotificationType,
    val title: String,
    val message: String
)

fun Notification.toUi(recipe: Recipe): UINotification {
    return UINotification(
        notificationId = notificationId,
        creationDate = creationDate,
        userId = userId,
        recipe = recipe,
        type = type,
        title = title,
        message = message
    )
}