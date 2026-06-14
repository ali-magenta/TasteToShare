package com.example.tastetoshare.services.notifications

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SnackbarController {
    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}

data class SnackbarAction(
    val name: String,
    val routeToNavigate: Any? = null,
    val action: (suspend () -> Unit)? = null
)
data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Long
)