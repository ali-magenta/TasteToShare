package com.example.tastetoshare.data.entities

import com.example.tastetoshare.data.NotificationType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// review id not saved for simplicity,
// even if the notification is for a new review just open the recipe and if possible auto scroll down

data class Notification @OptIn(ExperimentalUuidApi::class) constructor(
    val notificationId: String = Uuid.random().toString(),
    val creationDate: Long = System.currentTimeMillis(),
    val userId: String = "",
    val recipeId: String = "",
    val type: NotificationType = NotificationType.RECOMMENDED_RECIPE,
    val title: String = "",
    val message: String = ""
)
