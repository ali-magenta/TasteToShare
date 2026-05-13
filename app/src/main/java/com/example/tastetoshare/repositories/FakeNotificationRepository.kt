package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.entities.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNotificationRepository: NotificationRepository {

    private val _placeHolderNotifications = listOf(
        Notification(
            notificationId = "1",
            creationDate = System.currentTimeMillis(),
            userId = "101",
            recipeId = "100",
            type = NotificationType.RECOMMENDED_RECIPE,
            title = "Recipe recommendation",
            message = "We think you'll love this creamy mushroom risotto!"
        ),
        Notification(
            notificationId = "2",
            creationDate = System.currentTimeMillis() - 60000,
            userId = "101",
            recipeId = "101",
            type = NotificationType.NEW_REVIEW,
            title = "New review received",
            message = "Your lasagna recipe just received a 5-star review."
        ),
        Notification(
            notificationId = "3",
            creationDate = System.currentTimeMillis() - 120000,
            userId = "101",
            recipeId = "102",
            type = NotificationType.DUPLICATED_RECIPE,
            title = "Duplicate recipe detected",
            message = "A similar carbonara recipe already exists in the system."
        ),
        Notification(
            notificationId = "4",
            creationDate = System.currentTimeMillis() - 180000,
            userId = "user_789",
            recipeId = "101",
            type = NotificationType.RECOMMENDED_RECIPE,
            title = "New recommendation",
            message = "Check out this spicy ramen recipe picked for you."
        ),
        Notification(
            notificationId = "5",
            creationDate = System.currentTimeMillis() - 240000,
            userId = "user_123",
            recipeId = "100",
            type = NotificationType.NEW_REVIEW,
            title = "Another review is in",
            message = "Someone commented on your chocolate cake recipe."
        )
    )

    private val _notifications = MutableStateFlow(_placeHolderNotifications)


    override fun getNotifications(userId: String): Flow<List<Notification>> {
        return _notifications.map { notifications ->
            notifications
                .filter { it.userId == userId }
        }
    }
}