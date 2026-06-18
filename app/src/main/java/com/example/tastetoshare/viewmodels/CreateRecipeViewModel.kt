package com.example.tastetoshare.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.toRoute
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Notification
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.RecipeIngredient
import com.example.tastetoshare.data.entities.Step
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.navigation.CreateRecipeGraphRoute
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.NotificationRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.TagRepository
import com.example.tastetoshare.services.notifications.NotificationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

class CreateRecipeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationService: NotificationService,
) : ViewModel() {
    private var isInitialized = false
    private val _uiState = MutableStateFlow(CreateRecipeUIState())

    private val args = savedStateHandle.toRoute<CreateRecipeGraphRoute>()
    private val parentRecipeId = args.parentRecipeId
    private val editedRecipeId = args.editedRecipeId

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _cuisineTypeTags: StateFlow<List<Tag>> = _uiState.flatMapLatest { uiState ->
        tagRepository.getTags(uiState.fieldSearchCuisineTypeTags, "Cuisine Type")
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _dietaryRestrictionTags: StateFlow<List<Tag>> = _uiState.flatMapLatest { uiState ->
        tagRepository.getTags(uiState.fieldSearchDietaryRestrictionTags, "Dietary Restrictions")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _otherTags: StateFlow<List<Tag>> = _uiState.flatMapLatest { uiState ->
        tagRepository.getTags(uiState.fieldSearchOtherTags, "Other")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _countryOfOriginTags: StateFlow<List<Tag>> = _uiState.flatMapLatest { uiState ->
        tagRepository.getTags(uiState.fieldSearchCountryOfOriginTag, "Country")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _ingredients: StateFlow<List<Ingredient>> = _uiState.flatMapLatest { uiState ->
        ingredientRepository.getAppIngredientsSortedByName(uiState.fieldSearchIngredient)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Initialize the suggested tags
    init {
        viewModelScope.launch {
            val defaultCuisines = tagRepository.getTags("", "Cuisine Type").first()
            val defaultDiets = tagRepository.getTags("", "Dietary Restrictions").first()
            val defaultCountries = tagRepository.getTags("", "Country").first()
            val defaultOthers = tagRepository.getTags("", "Other").first()

            _uiState.update { currentState ->
                currentState.copy(
                    suggestedCuisineTypeTags = defaultCuisines.take(10),
                    suggestedDietaryRestrictionTags = defaultDiets.take(10),
                    suggestedCountryTags = defaultCountries.take(10),
                    suggestedOtherTags = defaultOthers.take(10)
                )
            }
        }
        initializeRecipe(parentRecipeId, editedRecipeId)
    }

    val uiState = combine(combine(_uiState, _ingredients, _countryOfOriginTags, ::Triple),
        combine(_otherTags, _dietaryRestrictionTags, _cuisineTypeTags, ::Triple)){t1, t2 ->
        val uiState = t1.first
        val ingredients = t1.second
        val countries = t1.third
        val otherTags = t2.first
        val dietRestrictions = t2.second
        val cuisineTypeTags = t2.third
        CreateRecipeUIState(
            recipeIdFromEdit = uiState.recipeIdFromEdit,
            parentRecipeId = uiState.parentRecipeId,
            title = uiState.title,
            description = uiState.description,
            photoURL = uiState.photoURL,
            preparationTime = uiState.preparationTime,
            servings = uiState.servings,
            difficulty = uiState.difficulty,
            cost = uiState.cost,
            calories = uiState.calories,
            recipeIngredients = uiState.recipeIngredients,
            steps = uiState.steps,
            savedRecipeId = uiState.savedRecipeId,
            suggestedCuisineTypeTags = uiState.suggestedCuisineTypeTags,
            searchResultCuisineTypeTags = cuisineTypeTags.filterNot { it in uiState.selectedCuisineTypeTags }, //The displayed tags correspond to the unselected ones
            selectedCuisineTypeTags = uiState.selectedCuisineTypeTags,
            fieldSearchCuisineTypeTags = uiState.fieldSearchCuisineTypeTags,
            suggestedDietaryRestrictionTags = uiState.suggestedDietaryRestrictionTags,
            searchResultDietaryRestrictionTags = dietRestrictions.filterNot { it in uiState.selectedDietaryRestrictionTags },
            selectedDietaryRestrictionTags = uiState.selectedDietaryRestrictionTags,
            fieldSearchDietaryRestrictionTags = uiState.fieldSearchDietaryRestrictionTags,
            suggestedOtherTags = uiState.suggestedOtherTags,
            searchResultOtherTags = otherTags.filterNot { it in uiState.selectedOtherTags },
            selectedOtherTags = uiState.selectedOtherTags,
            fieldSearchOtherTags = uiState.fieldSearchOtherTags,
            suggestedCountryTags = uiState.suggestedCountryTags,
            searchResultCountryTags = countries,
            selectedCountryOfOrigin = uiState.selectedCountryOfOrigin,
            fieldSearchCountryOfOriginTag = uiState.fieldSearchCountryOfOriginTag,
            displayedIngredients = ingredients,
            fieldSearchIngredient = uiState.fieldSearchIngredient,
            selectedIngredient = uiState.selectedIngredient,
            fieldIngredientQuantity = uiState.fieldIngredientQuantity,
            fieldIngredientUnit = uiState.fieldIngredientUnit,
            fieldNewStepDescription = uiState.fieldNewStepDescription,
            newStepPhotoURL = uiState.newStepPhotoURL,
            photoError = uiState.photoError,
            ingredientError = uiState.ingredientError,
            titleError = uiState.titleError,
            preparationTimeError = uiState.preparationTimeError,
            servingsError = uiState.servingsError,
            caloriesError = uiState.caloriesError,
            difficultyError = uiState.difficultyError,
            costError = uiState.costError,
            recipeIngredientsError = uiState.recipeIngredientsError,
            stepsError = uiState.stepsError,
            selectedCuisineTypeTagsError = uiState.selectedCuisineTypeTagsError,
            selectedDietaryRestrictionTagsError = uiState.selectedDietaryRestrictionTagsError,
            selectedOtherTagsError = uiState.selectedOtherTagsError,
            selectedCountryOfOriginError = uiState.selectedCountryOfOriginError,
            hasErrors = uiState.hasErrors,
            currentPhotoTarget = uiState.currentPhotoTarget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateRecipeUIState()
    )

    fun handlePhotoUri(uri:String) {
        if(_uiState.value.currentPhotoTarget == PhotoTarget.RECIPE_PHOTO){
            setPhotoURL(uri)
        }
        if(_uiState.value.currentPhotoTarget == PhotoTarget.STEP_PHOTO){
            setNewStepPhotoURL(uri)
        }
        savedStateHandle["returned_uri"] = null
    }

    fun initializeRecipe(parentRecipeId: String?, editedRecipeId: String?){
        if(parentRecipeId != null && parentRecipeId == _uiState.value.parentRecipeId) return
        if(editedRecipeId != null && editedRecipeId == _uiState.value.recipeIdFromEdit) return
        if(parentRecipeId != null && parentRecipeId != _uiState.value.parentRecipeId) isInitialized = false
        if(editedRecipeId != null && editedRecipeId != _uiState.value.recipeIdFromEdit) isInitialized = false
        if (isInitialized) return

        viewModelScope.launch {
            isInitialized = true
            _uiState.update { CreateRecipeUIState(
                suggestedOtherTags = _uiState.value.suggestedOtherTags,
                suggestedCountryTags = _uiState.value.suggestedCountryTags,
                suggestedCuisineTypeTags = _uiState.value.suggestedCuisineTypeTags,
                suggestedDietaryRestrictionTags = _uiState.value.suggestedDietaryRestrictionTags) }
            if(parentRecipeId != null){
                val parentRecipe = recipeRepository.getRecipeById(parentRecipeId).first()
                _uiState.update { uiState ->
                    uiState.copy(
                        recipeIdFromEdit = null,
                        parentRecipeId = parentRecipe.recipeId,
                        title = parentRecipe.title,
                        description = parentRecipe.description,
                        photoURL = parentRecipe.photoURL,
                        preparationTime = parentRecipe.preparationTime,
                        servings = parentRecipe.servings,
                        difficulty = parentRecipe.difficulty,
                        cost = parentRecipe.cost,
                        calories = parentRecipe.calories,
                        recipeIngredients = parentRecipe.ingredients,
                        steps = parentRecipe.steps,
                        savedRecipeId = null,
                        selectedCuisineTypeTags = parentRecipe.tags.filter { it.type == "Cuisine Type"},
                        selectedDietaryRestrictionTags = parentRecipe.tags.filter { it.type == "Dietary Restrictions" },
                        selectedOtherTags = parentRecipe.tags.filter { it.type == "Other" },
                        selectedCountryOfOrigin = parentRecipe.tags.firstOrNull  { it.type == "Country" }
                    )
                }
            }
            if(editedRecipeId != null){
                val recipe = recipeRepository.getRecipeById(editedRecipeId).first()
                Log.d("debug", recipe.toString())
                _uiState.update { uiState ->
                    uiState.copy(
                        recipeIdFromEdit = editedRecipeId,
                        title = recipe.title,
                        description = recipe.description,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        servings = recipe.servings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        recipeIngredients = recipe.ingredients,
                        steps = recipe.steps,
                        savedRecipeId = null,
                        selectedCuisineTypeTags = recipe.tags.filter { it.type == "Cuisine Type"},
                        selectedDietaryRestrictionTags = recipe.tags.filter { it.type == "Dietary Restrictions" },
                        selectedOtherTags = recipe.tags.filter { it.type == "Other" },
                        selectedCountryOfOrigin = recipe.tags.firstOrNull  { it.type == "Country" }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveRecipe(onAuthRequired: () -> Unit){
        if (!validateStepsStep())
            return

        val userId = SessionManagerFacade.currentUserStateFlow.value
        if (userId.isNullOrBlank()) onAuthRequired()
        else {

            var hasErrors = false
            if (_uiState.value.title.isBlank()) {
                _uiState.update { it.copy(titleError = "You must provide a title for the recipe") }
                hasErrors = true
            }
            if (_uiState.value.preparationTime == null) {
                _uiState.update { it.copy(preparationTimeError = "You must provide a preparation time for the recipe") }
                hasErrors = true
            }
            if (_uiState.value.recipeIngredients.isEmpty()) {
                _uiState.update { it.copy(recipeIngredientsError = "You must provide ingredients for the recipe") }
                hasErrors = true
            }
            if (_uiState.value.steps.isEmpty()) {
                _uiState.update { it.copy(stepsError = "You must provide steps for the recipe") }
                hasErrors = true
            }
            Log.d("debugRecipe", "HasErrors: $hasErrors")

            if (hasErrors) {
                _uiState.update { it.copy(hasErrors = true) }
            } else {
                _uiState.update { it.copy(isSaving = true) }
                // Compute calories before saving
                var computedCalories: Int? = 0
                for (recipeIngredient in _uiState.value.recipeIngredients) {
                    val cal =
                        recipeIngredient.ingredient.unitOfMeasurementToCaloryPerUnit[recipeIngredient.customUnitOfMeasurement]
                    if (cal == null) {
                        computedCalories = null
                        break
                    }
                    computedCalories = (computedCalories ?: 0) +
                            cal * recipeIngredient.quantity.toInt()
                }

                setCalories(computedCalories)

                viewModelScope.launch {
                    Log.d("STATE", "saving=${_uiState.value.isSaving}")

                    var newRecipe: Recipe
                    val recipeIdFromEdit = _uiState.value.recipeIdFromEdit
                    if (recipeIdFromEdit != null) {
                        Log.d(
                            "parent recipe when editing",
                            "_uiState.value.parentRecipeId=${_uiState.value.parentRecipeId}"
                        )
                        // 1 : Edited
                        val recipe = recipeRepository.getRecipeById(recipeIdFromEdit).first()
                        newRecipe = Recipe(
                            creationDate = recipe.creationDate,
                            recipeId = recipe.recipeId,
                            originalRecipeId = _uiState.value.parentRecipeId,
                            authorId = userId,
                            title = _uiState.value.title,
                            description = _uiState.value.description,
                            photoURL = _uiState.value.photoURL ?: "",
                            preparationTime = _uiState.value.preparationTime!!,
                            servings = _uiState.value.servings,
                            rating = recipe.rating,
                            numberOfRatings = recipe.numberOfRatings,
                            difficulty = _uiState.value.difficulty,
                            cost = _uiState.value.cost,
                            calories = computedCalories,
                            ingredients = _uiState.value.recipeIngredients,
                            steps = _uiState.value.steps,
                            tags = _uiState.value.selectedOtherTags +
                                    _uiState.value.selectedDietaryRestrictionTags +
                                    _uiState.value.selectedCuisineTypeTags +
                                    listOfNotNull(_uiState.value.selectedCountryOfOrigin),
                            reviews = recipe.reviews
                        )

                    } else {
                        // 2: Created or duplicated
                        newRecipe = Recipe(
                            originalRecipeId = parentRecipeId,
                            authorId = userId,
                            title = _uiState.value.title,
                            description = _uiState.value.description,
                            photoURL = _uiState.value.photoURL ?: "",
                            preparationTime = _uiState.value.preparationTime!!,
                            servings = _uiState.value.servings,
                            rating = 0.0f,
                            numberOfRatings = 0,
                            difficulty = _uiState.value.difficulty,
                            cost = _uiState.value.cost,
                            calories = computedCalories,
                            ingredients = _uiState.value.recipeIngredients,
                            steps = _uiState.value.steps,
                            tags = _uiState.value.selectedOtherTags +
                                    _uiState.value.selectedDietaryRestrictionTags +
                                    _uiState.value.selectedCuisineTypeTags +
                                    listOfNotNull(_uiState.value.selectedCountryOfOrigin),
                            reviews = emptyList()
                        )
                    }

                    recipeRepository.addOrOverrideRecipe(newRecipe)
                    //_uiState.update { it.copy(savedRecipeId = newRecipe.recipeId) }
                    newRecipe.ingredients.forEach {
                        if (it.ingredient.isUserCreated) {
                            ingredientRepository.addOrOverrideIngredient(it.ingredient)
                        }
                    }

                    // To change by a firebase script
                    val isDuplicated = recipeIdFromEdit == null && parentRecipeId != null
                    if (isDuplicated) {
                        val originalRecipeId = parentRecipeId
                        val originalRecipe =
                            recipeRepository.getRecipeById(originalRecipeId).firstOrNull()

                        // remove this if for testing on the logged account
                        // (while we don't have firebase repository for notifications and can't test on 2 devices simultaneously)
                        if (originalRecipe != null && originalRecipe.authorId != userId) {
                            val notification = Notification(
                                userId = originalRecipe.authorId,
                                recipeId = newRecipe.recipeId,
                                type = NotificationType.DUPLICATED_RECIPE,
                                title = "Recipe Duplicated! \uD83D\uDD04",
                                message = "Someone was inspired by your ${originalRecipe?.title} and created their own version."
                            )
                            notificationRepository.addNotification(notification)
                            //notificationService.dispatchNotification(notification.toUi(newRecipe))
                        }
                    }

                    Log.d("debugRecipe", "Recipe saved $newRecipe")

                    isInitialized = false
                    _uiState.update {
                        it.copy(
                            savedRecipeId = newRecipe.recipeId,
                            isSaving = false
                        )
                    }
                    Log.d("STATE", "saving=${_uiState.value.isSaving}")
                }
            }
        }
    }

    fun navigationHandled() {
        _uiState.update { it.copy(savedRecipeId = null) }
    }

    fun setTitle(title : String){
        _uiState.update { it.copy(title = title, titleError = "") }
    }

    fun setDescription(description : String){
        _uiState.update { it.copy(description = description) }
    }

    fun setPreparationTime(preparationTime: Int?){
        _uiState.update { it.copy(preparationTime = preparationTime, preparationTimeError = "") }
    }

    fun setServings(servings: Int){
        _uiState.update { it.copy(servings = servings, servingsError = "") }
    }

    fun setDifficulty(difficulty: Difficulty){
        _uiState.update { it.copy(difficulty = difficulty,  difficultyError= "") }
    }

    fun setCost(cost: Cost){
        _uiState.update { it.copy(cost = cost, costError = "") }
    }

    fun setCalories(calories: Int?){
        _uiState.update { it.copy(calories = calories, caloriesError = "") }
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

    fun moveStepUpOrDown(stepIndex: Int, moveUp: Boolean){
        if(moveUp && stepIndex > 0){
            _uiState.update {state -> state.copy(steps = state.steps.mapIndexed { index, step ->
                if(index == stepIndex){
                    val element = state.steps.elementAt(stepIndex - 1)
                    element.copy(order = element.order + 1)
                }
                else if (index == stepIndex - 1){
                    val element = state.steps.elementAt(stepIndex)
                    element.copy(order = element.order - 1)
                }
                else{
                    step
                }
            }) }
        }
        else if(!moveUp && stepIndex < _uiState.value.steps.size - 1){
            _uiState.update {state -> state.copy(steps = state.steps.mapIndexed { index, step ->
                if(index == stepIndex){
                    val element = state.steps.elementAt(stepIndex + 1)
                    element.copy(order = element.order - 1)
                }
                else if (index == stepIndex + 1){
                    val element = state.steps.elementAt(stepIndex)
                    element.copy(order = element.order + 1)
                }
                else{
                    step
                }
            }) }
        }

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

    fun removeIngredient(recipeIngredientId: String){
        _uiState.update { uiState ->
            uiState.copy(recipeIngredients = uiState.recipeIngredients.filter { it.recipeIngredientId != recipeIngredientId })
        }
    }

    fun addStep(){
        if(_uiState.value.fieldNewStepDescription != "")
        _uiState.update { uiState ->
            uiState.copy(
                steps = uiState.steps + Step(photoURL = uiState.newStepPhotoURL, description = uiState.fieldNewStepDescription, order = uiState.steps.size+1),
                newStepPhotoURL = "",
                fieldNewStepDescription = "",
                stepsError = ""
            )
        }
    }

    fun removeStep(stepId: String){
        _uiState.update { uiState ->
            val newSteps = uiState.steps.filter { it.stepId != stepId }
            val updatedSteps = newSteps.mapIndexed { index, step -> step.copy(order = index+1) }
            uiState.copy(
                steps = updatedSteps
            )
        }
    }

    fun setFieldSearchCuisineTypeTags(fieldSearchCuisineTypeTags: String){
        _uiState.update { it.copy(fieldSearchCuisineTypeTags = fieldSearchCuisineTypeTags) }
    }

    fun setFieldSearchDietaryRestrictionTags(fieldSearchDietaryRestrictionTags: String){
        _uiState.update { it.copy(fieldSearchDietaryRestrictionTags = fieldSearchDietaryRestrictionTags) }
    }

    fun setFieldSearchOtherTags(fieldSearchOtherTags: String){
        _uiState.update { it.copy(fieldSearchOtherTags = fieldSearchOtherTags) }
    }

    fun setFieldSearchCountryOfOriginTag(fieldSearchCountryOfOriginTag: String){
        _uiState.update { it.copy(fieldSearchCountryOfOriginTag = fieldSearchCountryOfOriginTag) }
    }

    fun validateBasicInfoStep(): Boolean {
        var hasErrors = false
        if(_uiState.value.title.isBlank()){
            _uiState.update { it.copy(titleError = "You must provide a title for the recipe") }
            hasErrors = true
        }
        if(_uiState.value.preparationTime == null){
            _uiState.update { it.copy(preparationTimeError = "You must provide a preparation time for the recipe") }
            hasErrors = true
        }
        return !hasErrors
    }

    fun validateTagsStep(): Boolean {
        return true
    }

    fun validateIngredientStep(): Boolean {
        var hasErrors = false
        if(_uiState.value.recipeIngredients.isEmpty()){
            _uiState.update { it.copy(recipeIngredientsError = "You must provide ingredients for the recipe") }
            hasErrors = true
        }

        return !hasErrors
    }

    fun validateStepsStep(): Boolean {
        var hasErrors = false

        if(_uiState.value.steps.isEmpty()){
            _uiState.update { it.copy(stepsError = "You must provide steps for the recipe") }
            hasErrors = true
        }

        return !hasErrors
    }


    fun setPhotoURL(photoURL: String){
        _uiState.update { it.copy(photoURL = photoURL) }
    }

    fun setNewStepPhotoURL(photoURL: String){
        _uiState.update { it.copy(newStepPhotoURL = photoURL) }
    }

    fun setNewStepDescription(description: String) {
        _uiState.update { it.copy(fieldNewStepDescription = description) }
    }
    
    fun toggleSelectedCuisineTag(tag: Tag) {
        _uiState.update { state ->
            if (tag in state.selectedCuisineTypeTags) {
                state.copy(selectedCuisineTypeTags = state.selectedCuisineTypeTags.filter { it != tag })
            } else {
                state.copy(selectedCuisineTypeTags = state.selectedCuisineTypeTags + tag)
            }
        }
    }

    fun toggleSelectedDietaryTag(tag: Tag) {
        _uiState.update { state ->
            if (tag in state.selectedDietaryRestrictionTags) {
                state.copy(selectedDietaryRestrictionTags = state.selectedDietaryRestrictionTags.filter { it != tag })
            } else {
                state.copy(selectedDietaryRestrictionTags = state.selectedDietaryRestrictionTags + tag)
            }
        }
    }

    fun toggleSelectedOtherTag(tag: Tag) {
        _uiState.update { state ->
            if (tag in state.selectedOtherTags) {
                state.copy(selectedOtherTags = state.selectedOtherTags.filter { it != tag })
            } else {
                state.copy(selectedOtherTags = state.selectedOtherTags + tag)
            }
        }
    }

    fun toggleSelectedCountryTag(tag: Tag) {
        _uiState.update { state ->
            if (state.selectedCountryOfOrigin == tag) {
                state.copy(selectedCountryOfOrigin = null)
            } else {
                state.copy(selectedCountryOfOrigin = tag)
            }
        }
    }

    fun setPhotoTarget(target: PhotoTarget){
        _uiState.update {
            it.copy(
                currentPhotoTarget = target
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {

                    val application =
                        checkNotNull(extras[APPLICATION_KEY]) as TasteToShareApp

                    return CreateRecipeViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        tagRepository = application.container.tagRepository,
                        ingredientRepository = application.container.ingredientRepository,
                        recipeRepository = application.container.recipeRepository,
                        notificationRepository = application.container.notificationRepository,
                        notificationService = NotificationService(application),
                    ) as T
                }
            }

    }
}

data class CreateRecipeUIState(
    val isSaving: Boolean = false,
    val recipeIdFromEdit: String? = null,
    val parentRecipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val photoURL: String? = null,
    val preparationTime: Int? = null,
    val servings : Int = 1,
    val difficulty : Difficulty = Difficulty.MEDIUM,
    val cost: Cost = Cost.AFFORDABLE,
    val calories: Int? = null,
    val recipeIngredients : List<RecipeIngredient> = emptyList(),
    val steps : List<Step> = emptyList(),
    val savedRecipeId: String? = null,

    //CuisineTypeTags
    val searchResultCuisineTypeTags: List<Tag> = emptyList(),
    val selectedCuisineTypeTags: List<Tag> = emptyList(),
    val suggestedCuisineTypeTags: List<Tag> = emptyList(),
    val fieldSearchCuisineTypeTags: String = "",

    //DietaryRestrictionTags
    val searchResultDietaryRestrictionTags: List<Tag> = emptyList(),
    val selectedDietaryRestrictionTags: List<Tag> = emptyList(),
    val suggestedDietaryRestrictionTags: List<Tag> = emptyList(),
    val fieldSearchDietaryRestrictionTags: String = "",

    //OtherTags
    val searchResultOtherTags: List<Tag> = emptyList(),
    val selectedOtherTags: List<Tag> = emptyList(),
    val suggestedOtherTags: List<Tag> = emptyList(),
    val fieldSearchOtherTags: String = "",

    //CountryTag
    val searchResultCountryTags: List<Tag> = emptyList(),
    val selectedCountryOfOrigin: Tag? = null,
    val suggestedCountryTags: List<Tag> = emptyList(),
    val fieldSearchCountryOfOriginTag: String = "",

    val displayedIngredients: List<Ingredient> = emptyList(),
    val fieldSearchIngredient: String = "",
    val selectedIngredient: Ingredient? = null,
    val fieldIngredientQuantity: String = "",
    val fieldIngredientUnit: String = "",

    val fieldNewStepDescription: String = "",
    val newStepPhotoURL: String? = null,

    val photoError: String = "",
    val titleError: String = "",
    val preparationTimeError: String = "",
    val servingsError: String = "",
    val caloriesError: String = "",
    val difficultyError: String = "",
    val costError: String = "",
    val recipeIngredientsError: String = "",
    val ingredientError: String = "",
    val stepsError: String = "",
    val selectedCuisineTypeTagsError: String = "",
    val selectedDietaryRestrictionTagsError: String = "",
    val selectedOtherTagsError: String = "",
    val selectedCountryOfOriginError: String = "",
    val hasErrors: Boolean = false,
    val currentPhotoTarget: PhotoTarget = PhotoTarget.RECIPE_PHOTO
)

enum class PhotoTarget {
    RECIPE_PHOTO,
    STEP_PHOTO
}