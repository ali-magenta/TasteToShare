package com.example.tastetoshare.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.toRoute
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.navigation.ProfileRoute
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.TagRepository
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val tagRepository: TagRepository
    ) : ViewModel() {

    val defaultTags =
        tagRepository.getTags(null, null)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val args = savedStateHandle.toRoute<ProfileRoute>()
    private val userId = args.userId

    val user: StateFlow<User> =
        userRepository.getUser(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = User()
            )

    private val currentUserId: StateFlow<String?> = SessionManagerFacade.currentUserStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionManagerFacade.currentUserStateFlow.value
        )

    private val isLoggedIn: StateFlow<Boolean> = SessionManagerFacade.currentUserStateFlow
        .map {it != null}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionManagerFacade.currentUserStateFlow.value != null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val loggedInUser: StateFlow<User> =
        currentUserId.flatMapLatest { userId ->
            if(userId != null) userRepository.getUser(userId)
            else flowOf(User())
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = User()
            )

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserId.collect { userId ->
                _uiState.update { it.copy(currentUserId = userId) }
            }
        }
    }

    val userRecipes: StateFlow<List<UIRecipePreview>> =
        recipeRepository.getRecentRecipesPreviewByUserId(userId)
            .combine(loggedInUser) { recipes, loggedInUser ->
                recipes.map { it.toUi(loggedInUser) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val recipeRatings: StateFlow<List<Float>> =
        userRecipes
            .map { recipes ->
                recipes.map { it.rating }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun toggleMyRecipesAnalyticsTab(value: Boolean) {
        _uiState.update { it.copy(isShowingMyRecipes = value) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(
            editedFullName = value,
            validation = _uiState.value.validation.copy(fullNameError = "")
        ) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(
            editedNickName = value,
            validation = _uiState.value.validation.copy(nickNameError = "")
        ) }
    }

    fun onBiographyChanged(value: String) {
        _uiState.update { it.copy(
            editedBiography = value,
            validation = _uiState.value.validation.copy(biographyError = "")
        ) }
    }

    fun onPreferencesChanged(value: List<Tag>) {
        _uiState.update { it.copy(
            editedTags = value,
            hasEditedTags = true,
            validation = _uiState.value.validation.copy(tagsError = "")
        ) }
    }

    fun onRoleChanged(value: String) {
        _uiState.update { it.copy(
            editedCookingRole = value,
            validation = _uiState.value.validation.copy(cookingRoleError = "")
        ) }
    }

    fun hideEditPhotoPopUp() {
        _uiState.update { it.copy(isShowingImageEditingPopUp = false) }
    }

    fun showEditPhotoPopUp() {
        _uiState.update { it.copy(isShowingImageEditingPopUp = true) }
    }

    fun importPhotoFromGallery() {
        _uiState.update { it.copy(isGalleryOpen = true) }
    }

    fun onPhotoSelectedFromGallery(uri: Uri?) {
        _uiState.update { it.copy(
            editedPhoto = uri?.toString(),
            isGalleryOpen = false
            ) }
    }

    fun dismissImageEditingPopUp() {
        _uiState.update { it.copy(isShowingImageEditingPopUp = false) }
    }

    fun onPhotoTaken(photo: String?) {
        _uiState.update { it.copy(editedPhoto = photo) }
    }

    fun onToggleSaved(recipeId: String, onAuthRequired: () -> Unit) {
        if (isLoggedIn.value) {
            viewModelScope.launch {
                val loggedUserId = SessionManagerFacade.currentUserStateFlow.value ?: return@launch
                val currentlySaved = recipeId in loggedInUser.value.savedRecipeIds
                if (currentlySaved) {
                    userRepository.deleteSavedRecipe(loggedUserId, recipeId)
                } else {
                    userRepository.addSavedRecipe(loggedUserId, recipeId)
                }

            }
        }
        else onAuthRequired()
    }

    fun saveChanges(onDone: (Boolean) -> Unit) {
        val isValidForm = validateProfile()
        if(isValidForm) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                userRepository.updateUser(
                    User(
                        userId = user.value.userId,
                        fullName = _uiState.value.editedFullName ?: user.value.fullName,
                        userName = _uiState.value.editedNickName ?: user.value.userName,
                        email = user.value.email,
                        role = _uiState.value.editedCookingRole ?: user.value.role,
                        description = _uiState.value.editedBiography ?: user.value.description,
                        photoURI = _uiState.value.editedPhoto ?: user.value.photoURI,
                        preferences = if (_uiState.value.hasEditedTags)
                            _uiState.value.editedTags
                        else user.value.preferences,
                        savedRecipeIds = user.value.savedRecipeIds,
                        triedRecipeIds = user.value.triedRecipeIds,
                        averageRating = user.value.averageRating,
                        numberOfRatings = user.value.numberOfRatings,
                        numberOfRecipeTriedByOthers = user.value.numberOfRecipeTriedByOthers,
                        numberOfReviews = user.value.numberOfReviews,
                    )
                )

                _uiState.update {
                    it.copy(isSaving = false)
                }
                onDone(true)
            }
        }
    }

    fun handlePhotoUri(uri:String) {
        _uiState.update { it.copy(
            editedPhoto = uri,
        ) }
        savedStateHandle["returned_uri"] = null
    }


    fun validateProfile(): Boolean {
        var currentFullNameError = ""
        var currentNickNameError = ""
        var currentBiographyError = ""
        var currentTagsError = ""

        val newName = _uiState.value.editedFullName ?: user.value.fullName
        val newNickName = _uiState.value.editedNickName ?: user.value.userName
        val newBiography = _uiState.value.editedBiography ?: user.value.description
        val newTags = if(_uiState.value.hasEditedTags) {
            _uiState.value.editedTags
        } else user.value.preferences

        if(newName.isBlank()) {
            currentFullNameError = "Name cannot be blank"
        }
        else if(newName.length < 4) {
            currentFullNameError = "Name must be at least 4 characters long"
        }

        if(newNickName.isBlank()) {
            currentNickNameError = "Username cannot be blank"
        }
        else if(newNickName.length < 4) {
            currentNickNameError = "Username must be at least 4 characters long"
        }

        if(newBiography.length > 300) {
            currentBiographyError = "Biography cannot be more than 300 characters long"
        }
        if(newTags.size < 3) {
            currentTagsError = "Please select at least three cooking preferences"
        }

        val isValidForm = currentNickNameError.isBlank() &&
                currentFullNameError.isBlank() &&
                currentBiographyError.isBlank() &&
                currentTagsError.isBlank()

        _uiState.update { it.copy(
            validation = EditProfileFormValidation(
                fullNameError = currentFullNameError,
                nickNameError = currentNickNameError,
                biographyError = currentBiographyError,
                tagsError = currentTagsError,
                isValid = isValidForm
            )
        ) }

        return isValidForm
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

                    return ProfileViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        userRepository = application.container.userRepository,
                        tagRepository = application.container.tagRepository
                    ) as T
                }
            }

    }
}

data class ProfileUIState(
    val isShowingMyRecipes: Boolean = true,
    val isShowingImageEditingPopUp: Boolean = false,
    val editedEmail: String? = null,
    val editedFullName: String? = null,
    val editedNickName: String? = null,
    val editedBiography: String? = null,
    val editedPhoto: String? = null,
    val editedCookingRole: String? = null,
    val editedTags: List<Tag> = emptyList(),
    val hasEditedTags: Boolean = false,
    val validation: EditProfileFormValidation = EditProfileFormValidation(),
    val isLoading: Boolean = false,
    val isGalleryOpen: Boolean = false,
    val isSaving: Boolean = false,
    val currentUserId: String? = null
)


data class EditProfileFormValidation(
    val fullNameError: String = "",
    val nickNameError: String = "",
    val biographyError: String = "",
    val photoError: String = "",
    val cookingRoleError: String = "",
    val tagsError: String = "",
    val isValid: Boolean = true
)