package com.example.tastetoshare.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUIState())
    val uiState: StateFlow<WelcomeUIState> = _uiState.asStateFlow()

    fun onGoogleSignInSuccess() {
        viewModelScope.launch {
            val userId = SessionManagerFacade.currentUserStateFlow.value ?: return@launch
            val email = SessionManagerFacade.userEmail
            val userExists = userRepository.userExists(userId)
            _uiState.update {
                it.copy(
                    isLoginSuccessful = userExists,
                    needsRegistration = !userExists,
                    googleEmail = email
                )
            }
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

                    return WelcomeViewModel(
                        userRepository = application.container.userRepository,
                    ) as T
                }
            }

    }
}

data class WelcomeUIState(
    val isLoginSuccessful: Boolean = false,
    val needsRegistration: Boolean = false,
    val googleEmail: String? = null
)