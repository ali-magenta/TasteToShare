package com.example.tastetoshare.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.tastetoshare.data.auth.SessionManagerFacade
import kotlinx.coroutines.launch

@Composable
fun LoginDialogDestination(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
)
{
    LoginDialogScreen(
        onDismiss,
        onLoginSuccess
    )
}

@Composable
fun LoginDialogScreen(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login Required") },
        text = { Text("Join us to explore recipes.") },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val result = SessionManagerFacade.signIn(context)
                    if (result.isSuccess) onLoginSuccess()
                }
            }) {
                Text("Sign in with Google")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}