package com.example.tastetoshare.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RecipeIngredient
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ShoppingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUIState())
    val uiState: StateFlow<ShoppingListUIState> = _uiState.asStateFlow()

    private val currentUserId: StateFlow<String?> = SessionManagerFacade.currentUserStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionManagerFacade.currentUserStateFlow.value
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val loggedInUser: StateFlow<User> =
        currentUserId.flatMapLatest{ userId ->
            if (userId != null) userRepository.getUser(userId)
            else flowOf(User())
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = User()
            )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val ingredients: StateFlow<List<Ingredient>> =
        uiState
            .debounce(300)
            .flatMapLatest { uiState ->
                ingredientRepository.getAppIngredientsSortedByName(searchString = uiState.fieldSearchIngredient)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    //@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    //val shoppingList: StateFlow<List<RecipeIngredient>> =
    //    uiState
    //        .debounce(300)
    //        .flatMapLatest { state ->
    //            userRepository.getShoppingListByUserId(
    //                currentUserId
    //            )
    //        }
    //        .stateIn(
    //            scope = viewModelScope,
    //            started = SharingStarted.WhileSubscribed(5000),
    //            initialValue = emptyList<RawRecipePreview>()
    //        )

    fun removeIngredient(recipeIngredientId: String){
        _uiState.update { uiState ->
            uiState.copy(recipeIngredients = uiState.recipeIngredients.filter { it.recipeIngredientId != recipeIngredientId })
        }
    }

    fun validateIngredientStep(): Boolean {
        var hasErrors = false
        if(_uiState.value.recipeIngredients.isEmpty()){
            _uiState.update { it.copy(recipeIngredientsError = "You must provide ingredients for the recipe") }
            hasErrors = true
        }

        return !hasErrors
    }

    fun setFieldSearchIngredient(fieldSearchIngredient: String){
        _uiState.update { it.copy(
            fieldSearchIngredient = fieldSearchIngredient,
            recipeIngredientsError = "",
            ingredientError = ""
        )
        }
    }

    fun setSelectedIngredient(selectedIngredient: Ingredient){
        _uiState.update { it.copy(selectedIngredient = selectedIngredient,
            fieldIngredientUnit = selectedIngredient.unitOfMeasurementToCaloryPerUnit.keys.firstOrNull()?: "",
            recipeIngredientsError = "",
            ingredientError = ""
        ) }
    }

    fun addIngredient(){
        _uiState.update {
            val error = when {
                it.selectedIngredient == null -> "Missing ingredient"
                it.fieldIngredientQuantity.toFloatOrNull() == null || it.fieldIngredientQuantity.toFloat() <= 0f -> "Invalid quantity"
                it.fieldIngredientUnit.isBlank() -> "Missing unit"
                else -> ""
            }
            if(error != ""){
                it.copy(ingredientError = error)
            }
            else{
                val newRecipeIngredient = RecipeIngredient(
                    ingredient = it.selectedIngredient!!,
                    quantity = it.fieldIngredientQuantity.toFloat(),
                    customUnitOfMeasurement = if (it.fieldIngredientUnit != "NO UNIT") it.fieldIngredientUnit else ""
                )

                it.copy(
                    recipeIngredients = it.recipeIngredients + newRecipeIngredient,
                    recipeIngredientsError = "",
                    selectedIngredient = null,
                    fieldIngredientUnit = "",
                    fieldIngredientQuantity = "",
                    fieldSearchIngredient = ""
                )
            }
        }
    }

    fun setFieldIngredientQuantity(fieldIngredientQuantity: String){
        _uiState.update { it.copy(fieldIngredientQuantity = fieldIngredientQuantity,
            ingredientError = ""
        ) }
    }

    fun setFieldIngredientUnit(fieldIngredientUnit: String){
        Log.d("DEBUG", "setFieldIngredientUnit(\"$fieldIngredientUnit\")")
        _uiState.update { it.copy(fieldIngredientUnit = fieldIngredientUnit,
            ingredientError = ""
        ) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application = checkNotNull(extras[APPLICATION_KEY]) as TasteToShareApp

                    return ShoppingListViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        userRepository = application.container.userRepository,
                        ingredientRepository = application.container.ingredientRepository,
                    ) as T
                }
            }
    }

}

data class ShoppingListUIState(
    val recipeIngredients : List<RecipeIngredient> = emptyList(),

    val recipeIngredientsError: String = "",
    val ingredientError: String = "",

    val displayedIngredients: List<Ingredient> = emptyList(),
    val fieldSearchIngredient: String = "",
    val selectedIngredient: Ingredient? = null,
    val fieldIngredientQuantity: String = "",
    val fieldIngredientUnit: String = ""
)