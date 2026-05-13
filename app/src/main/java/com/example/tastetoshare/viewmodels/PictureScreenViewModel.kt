package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.tastetoshare.navigation.PictureScreenRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class PictureScreenViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<PictureScreenRoute>()

    private val _isFrontCamera = MutableStateFlow(args.isFrontCamera)
    val isFrontCamera = _isFrontCamera.asStateFlow()

    fun toggleCamera(){
        _isFrontCamera.update { !it }
    }

    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PictureScreenViewModel(savedStateHandle)
            }
        }
    }
}
