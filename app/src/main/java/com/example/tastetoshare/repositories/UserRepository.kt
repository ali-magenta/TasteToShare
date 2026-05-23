package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<User>
    fun getSavedRecipes(userId: String): Flow<Set<String>>

    suspend fun updateUser(user: User)
    suspend fun addUser(user: User)
    suspend fun addSavedRecipe(userId : String, recipeId : String)
    suspend fun addTriedRecipe(userId : String, recipeId : String)
    suspend fun deleteSavedRecipe(userId : String, recipeId : String)
    suspend fun deleteTriedRecipe(userId : String, recipeId : String)
    suspend fun syncFcmToken(userId : String)
    suspend fun userExists(userId: String): Boolean
}