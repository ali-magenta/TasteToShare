package com.example.tastetoshare.data.uiClasses

import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.entities.Notification
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import kotlin.String

data class UINotification(
    val notificationId: String,
    val creationDate: Long,
    val userId: String,
    val recipe: Recipe,
    val review: Review?,
    val type: NotificationType,
    val title: String,
    val message: String,
    val targetId: String? = null
)

fun Notification.toUi(recipe: Recipe): UINotification {
    return UINotification(
        notificationId = notificationId,
        creationDate = creationDate,
        userId = userId,
        recipe = recipe,
        review = recipe.reviews.firstOrNull { it.reviewId == reviewId },
        type = type,
        title = title,
        message = message,
        targetId = targetId
    )
}