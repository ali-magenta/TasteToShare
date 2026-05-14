package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>

    suspend fun addNotification(notification: Notification)
}