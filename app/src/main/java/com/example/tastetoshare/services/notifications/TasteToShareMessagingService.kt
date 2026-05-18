package com.example.tastetoshare.services.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.tastetoshare.services.notifications.NotificationService
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
class TasteToShareMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationService = NotificationService(applicationContext)

        val data = remoteMessage.data

//        val typeString = data["type"] ?: return
//        val title = data["title"] ?: "New Notification"
//        val message = data["message"] ?: ""
//        val recipeId = data["recipeId"] ?: ""
//        val recipeTitle = data["recipeTitle"] ?: "Recipe"
//        val targetId = data["targetId"]
//
//        val recipePreview = UIRecipePreview(
//            recipeId = recipeId,
//            title = recipeTitle,
//        )
//        val uiNotification = UINotification(
//            notificationId = System.currentTimeMillis().toString(),
//            type = NotificationType.valueOf(typeString),
//            title = title,
//            message = message,
//            recipe = recipePreview,
//            targetId = targetId
//        )
//
//        CoroutineScope(Dispatchers.Main).launch {
//            notificationService.dispatchNotification(uiNotification)
//        }
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}