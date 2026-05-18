package com.example.tastetoshare.services.notifications

import com.example.tastetoshare.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.tastetoshare.services.notifications.NotificationService
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
class TasteToShareMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationService = NotificationService(applicationContext)

        val data = remoteMessage.data

        val typeString = data["type"] ?: return
        val title = data["title"] ?: "New Notification"
        val message = data["message"] ?: ""
        val recipeId = data["recipeId"] ?: ""
        val targetId = data["targetId"]
        val recipeTitle = data["recipeTitle"] ?: "Recipe"
        val recipePreview = Recipe(recipeId = recipeId, title = recipeTitle)

        val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID
        val intendedUserId = data["userId"] ?: ""

        // if the user connected on phone is not the one intended
        if (currentUserId != intendedUserId) {
            return
        }
        val uiNotification = UINotification(
            notificationId = System.currentTimeMillis().toString(),
            creationDate = System.currentTimeMillis(),
            userId = intendedUserId,
            recipe = recipePreview,
            type = NotificationType.valueOf(typeString),
            title = title,
            message = message,
            targetId = targetId
        )

        CoroutineScope(Dispatchers.Main).launch {
            notificationService.dispatchNotification(uiNotification)
        }
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}