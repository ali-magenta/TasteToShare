package com.example.tastetoshare.services.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.tastetoshare.R
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.navigation.RecipeDetailsRoute

class NotificationService(
    private val context: Context
)
{
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    suspend fun dispatchNotifications(notification: UINotification)
    {
        showNotification(notification)

        val snackbarEvent = buildSnackbarEvent(notification)
        SnackbarController.sendEvent(snackbarEvent)
    }

    private fun buildSnackbarEvent(notification: UINotification): SnackbarEvent {
        return when (notification.type) {
            NotificationType.RECOMMENDED_RECIPE -> SnackbarEvent(
                message = "We think you could enjoy: ${notification.recipe.title}",
                action = SnackbarAction(
                    name = "See recipe",
                    routeToNavigate = RecipeDetailsRoute(recipeId = notification.recipe.recipeId)
                )
            )
            NotificationType.NEW_REVIEW -> SnackbarEvent(
                message = "Someone reviewed your recipe: ${notification.recipe.title} ⭐️",
                action = SnackbarAction(
                    name = "See review",
                    routeToNavigate = RecipeDetailsRoute(recipeId = notification.recipe.recipeId)
                )
            )
            NotificationType.DUPLICATED_RECIPE -> SnackbarEvent(
                message = "Your recipe ${notification.recipe.title} was duplicated! 🔄",
                action = SnackbarAction(
                    name = "See recipe",
                    routeToNavigate = RecipeDetailsRoute(recipeId = notification.recipe.recipeId)
                )
            )
        }
    }

    fun showNotification(notification: UINotification) {
        val channel = when (notification.type) {
            NotificationType.NEW_REVIEW, NotificationType.DUPLICATED_RECIPE -> "social_channel_id"
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