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
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.navigation.AuthGraph
import com.example.tastetoshare.repositories.TagRepository
import com.example.tastetoshare.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LoginRegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository
) : ViewModel()
{
    private val _uiState = MutableStateFlow(LoginRegisterUIState())

    private val args = savedStateHandle.toRoute<AuthGraph>()

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

    init {
        viewModelScope.launch {
            val defaultCuisines = tagRepository.getTags("", "Cuisine Type").first()
            val defaultDiets = tagRepository.getTags("", "Dietary Restrictions").first()
            val defaultOthers = tagRepository.getTags("", "Other").first()

            _uiState.update { currentState ->
                currentState.copy(
                    suggestedCuisineTypeTags = defaultCuisines.take(10),
                    suggestedDietaryRestrictionTags = defaultDiets.take(10),
                    suggestedOtherTags = defaultOthers.take(10)
                )
            }
        }
    }

    val uiState: StateFlow<LoginRegisterUIState> = combine(
        _uiState,
        _cuisineTypeTags,
        _dietaryRestrictionTags,
        _otherTags
    ) { state, cuisines, diets, others ->

        state.copy(
            searchResultCuisineTypeTags = cuisines.filterNot { it in state.selectedCuisineTypeTags },
            searchResultDietaryRestrictionTags = diets.filterNot { it in state.selectedDietaryRestrictionTags },
            searchResultOtherTags = others.filterNot { it in state.selectedOtherTags }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoginRegisterUIState()
    )

    fun handlePhotoUri(uri:String) {
        _uiState.update { it.copy(
            editedPhoto = uri,
        ) }
        savedStateHandle["returned_uri"] = null
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(
            editedFullName = value,
            validation = _uiState.value.validation.copy(fullNameError = "")
        ) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(
            editedUsername = value,
            validation = _uiState.value.validation.copy(usernameError = "")
        ) }
    }

    fun onBiographyChanged(value: String) {
        _uiState.update { it.copy(
            editedBiography = value,
            validation = _uiState.value.validation.copy(biographyError = "")
        ) }
    }

    fun onRoleChanged(value: String) {
        _uiState.update { it.copy(
            editedCookingRole = value,
            validation = _uiState.value.validation.copy(cookingRoleError = "")
        ) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(
            editedPassword = value,
            validation = _uiState.value.validation.copy(passwordError = "")
        ) }
    }
    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(
            editedEmail = value,
            validation = _uiState.value.validation.copy(emailError = "")
        ) }
    }


    fun onPhotoSelectedFromGallery(uri: Uri?) {
        _uiState.update { it.copy(
            editedPhoto = uri?.toString(),
        ) }
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

    @OptIn(ExperimentalUuidApi::class)
    fun validateProfile(): Boolean {
        val state = _uiState.value

        var currentFullNameError = ""
        var currentUsernameError = ""
        var currentEmailError = ""
        var currentPasswordError = ""
        var currentBiographyError = ""
        var currentRoleError = ""
        var currentTagsError = ""

        val name = state.editedFullName.trim()
        val username = state.editedUsername.trim()
        val email = state.editedEmail.trim()
        val biography = state.editedBiography?.trim() ?: ""
        val password = state.editedPassword
        val role = state.editedCookingRole
        val allSelectedTags = state.selectedCuisineTypeTags +
                state.selectedDietaryRestrictionTags +
                state.selectedOtherTags

        if (name.isBlank()) {
            currentFullNameError = "Name cannot be blank"
        } else if (name.length < 4) {
            currentFullNameError = "Name must be at least 4 characters long"
        }

        if (username.isBlank()) {
            currentUsernameError = "Username cannot be blank"
        } else if (username.length < 4) {
            currentUsernameError = "Username must be at least 4 characters long"
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (email.isBlank()) {
            currentEmailError = "Email cannot be blank"
        } else if (!email.matches(emailRegex)) {
            currentEmailError = "Please enter a valid email address"
        }

        if (password.isBlank()) {
            currentPasswordError = "Password cannot be blank"
        } else if (password.length < 8) {
            currentPasswordError = "Password must be at least 8 characters long"
        } else if (!password.contains(Regex("[0-9]"))) {
            currentPasswordError = "Password must contain at least a digit"
        } else if (!password.contains(Regex("[A-Z]"))) {
            currentPasswordError = "Password must contain at least a capital letter"
        }

        if (biography.length > 140) {
            currentBiographyError = "Biography cannot exceed 140 characters"
        }

        if (role.isBlank()) {
            currentRoleError = "Please select a cooking role"
        }

        if (allSelectedTags.size < 3) {
            currentTagsError = "Please select at least three cooking preferences"
        }

        val isValidForm = currentFullNameError.isBlank() &&
                currentUsernameError.isBlank() &&
                currentEmailError.isBlank() &&
                currentPasswordError.isBlank() &&
                currentBiographyError.isBlank() &&
                currentRoleError.isBlank() &&
                currentTagsError.isBlank()

        _uiState.update { it.copy(
            validation = LoginRegisterFormValidation(
                fullNameError = currentFullNameError,
                usernameError = currentUsernameError,
                emailError = currentEmailError,
                passwordError = currentPasswordError,
                biographyError = currentBiographyError,
                cookingRoleError = currentRoleError,
                tagsError = currentTagsError,
                isValid = isValidForm
            )
        ) }

        if (isValidForm) {
            val newUserId = Uuid.random().toString()

            viewModelScope.launch {
                userRepository.addUser(
                    User(
                        userId =newUserId,
                        fullName = name,
                        userName = username,
                        email = email,
                        role = role,
                        description = biography,
                        photoURI = state.editedPhoto,
                        preferences = allSelectedTags,
                        savedRecipeIds = emptyList(),
                        triedRecipeIds = emptyList(),
                        averageRating = 0.0f,
                        numberOfRatings = 0,
                        numberOfRecipeTriedByOthers = 0,
                        numberOfReviews = 0,
                    )
                )
                userRepository.syncFcmToken(newUserId)
                SessionManager.CURRENT_LOGGED_IN_USER_ID = newUserId
                _uiState.update { it.copy(password = password, isSignUpSuccessful = true) }
            }
        }

        return isValidForm
    }

    fun loginUser() {
        val email = _uiState.value.editedEmail.trim()
        val password = _uiState.value.editedPassword

        if (true/*TODO add actual input check once we have authentication      email.isNotBlank() && password.isNotBlank()*/) {
            viewModelScope.launch {

                // Change this by the fetch of the actual user from the database
                // and update the SessionManager
                if (SessionManager.CURRENT_LOGGED_IN_USER_ID == "unlogged") {
                    SessionManager.CURRENT_LOGGED_IN_USER_ID = "101"
                }
                userRepository.syncFcmToken(SessionManager.CURRENT_LOGGED_IN_USER_ID)

                _uiState.update { it.copy(
                    isLoginSuccessful = true,
                    validation = _uiState.value.validation.copy(loginError = "")
                ) }
            }
        } else {
            _uiState.update { it.copy(
                validation = _uiState.value.validation.copy(loginError = "Please enter your email and password.")
            ) }
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

                    return LoginRegisterViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        tagRepository = application.container.tagRepository,
                        userRepository = application.container.userRepository
                    ) as T
                }
            }

    }
}


data class LoginRegisterUIState(
    val password: String = "",
    val editedEmail: String = "",
    val editedPassword: String = "",
    val editedFullName: String = "",
    val editedUsername: String = "",
    val editedBiography: String? = null,
    val editedPhoto: String? = null,
    val editedCookingRole: String = "",
    val validation: LoginRegisterFormValidation = LoginRegisterFormValidation(),
    val isSignUpSuccessful: Boolean = false,
    val isLoginSuccessful: Boolean = false,

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
    )


data class LoginRegisterFormValidation(
    val fullNameError: String = "",
    val usernameError: String = "",
    val biographyError: String = "",
    val photoError: String = "",
    val cookingRoleError: String = "",
    val tagsError: String = "",
    val passwordError: String = "",
    val emailError: String = "",
    val loginError: String = "",
    val isValid: Boolean = true,
)