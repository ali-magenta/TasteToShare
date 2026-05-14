package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
): UserRepository {

    suspend fun initializeData(users: List<User>) {

    }

    override fun getUser(userId: String): Flow<User> {
        TODO("Not yet implemented")
    }

    override fun getSavedRecipes(userId: String): Flow<Set<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun addUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun addSavedRecipe(userId: String, recipeId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun addTriedRecipe(userId: String, recipeId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSavedRecipe(userId: String, recipeId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTriedRecipe(userId: String, recipeId: String) {
        TODO("Not yet implemented")
    }
}