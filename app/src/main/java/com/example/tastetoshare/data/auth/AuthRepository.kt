package com.example.tastetoshare.data.auth

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserId: String?
    val currentUserStateFlow: StateFlow<String?>
    val isLoggedIn: Boolean
    val userEmail: String?

    suspend fun logout()
    suspend fun signIn(context: Context): Result<Unit>
}