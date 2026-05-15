package com.example.tastetoshare.services.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tastetoshare.R
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.navigation.RecipeDetailsRoute
import androidx.core.net.toUri

class NotificationService(
    private val context: Context
)
{
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    suspend fun dispatchNotification(notification: UINotification)
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
                    routeToNavigate = RecipeDetailsRoute(recipeId = notification.recipe.recipeId, focusReviewId = notification.review?.reviewId)
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

        val uri = (
                "app://tastetoshare/recipe/${notification.recipe.recipeId}" +
                        notification.review?.let { "?focusReviewId=${it.reviewId}" }.orEmpty()
                ).toUri()

        val intent = Intent(
            Intent.ACTION_VIEW,
            uri
        )
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(icon)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notification.notificationId.hashCode(), notificationBuilder.build())
    }
}