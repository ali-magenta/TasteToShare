package com.example.tastetoshare

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.tastetoshare.data.WrapperDB
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.navigation.AppNavigation
import com.example.tastetoshare.repositories.FirebaseIngredientRepository
import com.example.tastetoshare.repositories.FirebaseNotificationRepository
import com.example.tastetoshare.repositories.FirebaseRecipeRepository
import com.example.tastetoshare.repositories.FirebaseTagRepository
import com.example.tastetoshare.repositories.FirebaseUserRepository
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.NotificationRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.TagRepository
import com.example.tastetoshare.repositories.UserRepository
import com.example.tastetoshare.ui.theme.TasteToShareTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.gson.Gson
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


object SessionManager {
    var CURRENT_LOGGED_IN_USER_ID: String = "unlogged"
}

class AppContainer {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }

    val tagRepository: TagRepository by lazy {
        FirebaseTagRepository(firestore)
    }
    val ingredientRepository: IngredientRepository by lazy {
        FirebaseIngredientRepository(firestore, storage)
    }
    val userRepository: UserRepository by lazy {
        FirebaseUserRepository(firestore, storage)
    }
    val recipeRepository: RecipeRepository by lazy {
        FirebaseRecipeRepository(firestore, storage)
    }
    val notificationRepository: NotificationRepository by lazy {
        FirebaseNotificationRepository(firestore)
    }
}

class TasteToShareApp : Application() {
    lateinit var container: AppContainer
    lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        Paper.book().destroy()
        val jsonString =
            applicationContext.assets.open("seed_data.json").bufferedReader().use { it.readText() }
        val wrapperDB = Gson().fromJson(jsonString, WrapperDB::class.java)

        container = AppContainer()
        auth = Firebase.auth


        // Initialize Firebase data if necessary
        MainScope().launch(Dispatchers.IO) {
            (container.tagRepository as? FirebaseTagRepository)?.initializeData(wrapperDB.tags)
        }
        MainScope().launch(Dispatchers.IO) {
            (container.userRepository as? FirebaseUserRepository)?.initializeData(wrapperDB.users)
        }
        MainScope().launch(Dispatchers.IO) {
            (container.recipeRepository as? FirebaseRecipeRepository)?.initializeData(wrapperDB.recipes)
        }
        MainScope().launch(Dispatchers.IO) {
            (container.ingredientRepository as? FirebaseIngredientRepository)?.initializeData(wrapperDB.ingredients)
        }
        MainScope().launch(Dispatchers.IO) {
            (container.notificationRepository as? FirebaseNotificationRepository)?.initializeData(wrapperDB.notifications)
        }


        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val socialChannel = NotificationChannel(
                "social_channel_id",
                "Social & interractions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for reviews and recipe duplicates"
            }

            val recommendationChannel = NotificationChannel(
                "recommendations_channel_id",
                "Recommendations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly recipe recommendations"
            }

            val generalChannel = NotificationChannel(
                "general_channel_id",
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General infos about the app"
            }

            notificationManager.createNotificationChannels(listOf(socialChannel, recommendationChannel))        }
    }
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        fetchAndSaveFCMToken()
        enableEdgeToEdge()
        setContent {
            TasteToShareTheme {
                AppNavigation()
            }
        }
    }

    private fun fetchAndSaveFCMToken() {
        lifecycleScope.launch {
            try {
                val userId = SessionManagerFacade.currentUserStateFlow.value

                if (userId != null) {
                    val app = application as TasteToShareApp
                    app.container.userRepository.syncFcmToken(userId)
                    Log.d("FCM_DEBUG", "Token synchronized at launch for user : $userId")
                } else {
                    Log.d("FCM_DEBUG", "User not connected, token save on device.")
                }

            } catch (e: Exception) {
                Log.e("FCM_DEBUG", "Token error", e)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) ==
                PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
    }
}
