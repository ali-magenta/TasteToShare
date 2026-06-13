package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.RecipeIngredient
import com.example.tastetoshare.data.entities.User
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.collections.map

class PaperDbUserRepository: UserRepository {
    private val bookName = "users"

    private val _users = MutableStateFlow<List<User>>(
        Paper.book(bookName).allKeys.mapNotNull { key ->
            Paper.book(bookName).read<User>(key)
        }
    )

    override fun getUser(userId: String): Flow<User> {
        return _users.map { users ->  users.find { it.userId == userId }?:  User() }
    }

    override fun getSavedRecipes(userId: String): Flow<Set<String>> {
        return _users.map { users ->
            users.find { it.userId == userId }?.savedRecipeIds?.toSet() ?: emptySet()
        }
    }

    override fun getShoppingListByUserId(userId: String): Flow<List<RecipeIngredient>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteIngredientFromShoppingList(
        userId: String,
        recipeIngredientId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun addIngredientToShoppingList(
        userId: String,
        recipeIngredient: RecipeIngredient
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(
        user: User
    ) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).write(user.userId, user)
            _users.update { users ->
                users.map { existingUser ->
                    if (existingUser.userId == user.userId) user else existingUser
                }
            }
        }
    }

    override suspend fun addUser(user: User) {
        withContext(Dispatchers.IO) {
            if (Paper.book(bookName).contains(user.userId)) {
                return@withContext
            }

            Paper.book(bookName).write(user.userId, user)

            _users.update { currentUsers ->
                currentUsers + user
            }
        }
    }

    override suspend fun addSavedRecipe(userId: String, recipeId: String) {
        withContext(Dispatchers.IO){
            val user = Paper.book(bookName).read<User>(userId)

            if(user != null){
                val updatedUser = user.copy(
                    savedRecipeIds =  user.savedRecipeIds + recipeId
                )
                Paper.book(bookName).write(userId, updatedUser)

                _users.update { users ->
                    users.map {
                        if (it.userId == userId) updatedUser else it
                    }
                }
            }
        }
    }

    override suspend fun addTriedRecipe(userId: String, recipeId: String) {
        withContext(Dispatchers.IO){
            val user = Paper.book(bookName).read<User>(userId)

            if(user != null){
                val updatedUser = user.copy(
                    triedRecipeIds =  user.triedRecipeIds + recipeId
                )
                Paper.book(bookName).write(userId, updatedUser)

                _users.update { users ->
                    users.map {
                        if (it.userId == userId) updatedUser else it
                    }
                }
            }
        }
    }

    override suspend fun deleteSavedRecipe(userId: String, recipeId: String) {
        withContext(Dispatchers.IO){
            val user = Paper.book(bookName).read<User>(userId)

            if(user != null){
                val updatedUser = user.copy(
                    savedRecipeIds =  user.savedRecipeIds.filter { it != recipeId }
                )
                Paper.book(bookName).write(userId, updatedUser)

                _users.update { users ->
                    users.map {
                        if (it.userId == userId) updatedUser else it
                    }
                }
            }
        }
    }


    override suspend fun deleteTriedRecipe(userId: String, recipeId: String){
        withContext(Dispatchers.IO){
            val user = Paper.book(bookName).read<User>(userId)

            if(user != null){
                val updatedUser = user.copy(
                    triedRecipeIds =  user.triedRecipeIds.filter { it != recipeId }
                )
                Paper.book(bookName).write(userId, updatedUser)

                _users.update { users ->
                    users.map {
                        if (it.userId == userId) updatedUser else it
                    }
                }
            }
        }
    }

    override suspend fun syncFcmToken(userId: String) {
        // Only there because of the interface
    }

    override suspend fun userExists(userId: String): Boolean {
        TODO("Not yet implemented")
    }
}

