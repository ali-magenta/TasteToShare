package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore
): NotificationRepository {

    suspend fun initializeData(notifications: List<Notification>) {

    }

    override fun getNotifications(userId: String): Flow<List<Notification>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNotification(notification: Notification) {
        TODO("Not yet implemented")
    }
}