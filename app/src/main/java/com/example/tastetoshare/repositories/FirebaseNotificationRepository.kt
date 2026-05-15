package com.example.tastetoshare.repositories

import android.util.Log
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore
): NotificationRepository {

    private val notificationCollection = firestore.collection(FirebaseCollections.NOTIFICATIONS)

    suspend fun initializeData(notifications: List<Notification>) {
        val existing = notificationCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                notifications.forEach { notification ->
                    val docRef = notificationCollection.document(notification.notificationId)
                    batch.set(docRef, notification)
                }
            }.await()
        }
    }
    override fun getNotifications(userId: String): Flow<List<Notification>> {
        return notificationCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Notification::class.java)
            }
    }

    override suspend fun addNotification(notification: Notification) {
        notificationCollection.document(notification.notificationId).set( notification )
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "Success addNotification")
            }
            .addOnFailureListener{
                Log.e("FirebaseRepository", "Error addNotification")
            }
    }
}