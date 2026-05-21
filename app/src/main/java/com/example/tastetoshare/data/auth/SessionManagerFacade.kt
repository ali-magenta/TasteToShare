package com.example.tastetoshare.data.auth

import android.content.Context
import com.example.tastetoshare.R
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object SessionManagerFacade: AuthRepository {
    override val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")
    val _currentUserMutableStateFlow: MutableStateFlow<String?> = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid)
    override val currentUserStateFlow: StateFlow<String?> = _currentUserMutableStateFlow.asStateFlow()

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            _currentUserMutableStateFlow.value = auth.currentUser?.uid
        }
    }

    override val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    override suspend fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    override suspend fun signIn(context: Context): Result<Unit> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = CredentialManager.create(context).getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymous(context: Context): Result<Unit> {
       return try {
           FirebaseAuth.getInstance().signInAnonymously().await()
           Result.success(Unit)
       } catch (e: Exception) {
           Result.failure(e)
       }
    }

}