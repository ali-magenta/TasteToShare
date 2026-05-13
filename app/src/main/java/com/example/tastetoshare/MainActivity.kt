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
import com.example.tastetoshare.data.WrapperPaperDB
import com.example.tastetoshare.navigation.AppNavigation
import com.example.tastetoshare.repositories.FakeNotificationRepository
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.PaperDbIngredientRepository
import com.example.tastetoshare.repositories.PaperDbRecipeRepository
import com.example.tastetoshare.repositories.PaperDbTagRepository
import com.example.tastetoshare.repositories.PaperDbUserRepository
import com.example.tastetoshare.ui.theme.TasteToShareTheme
import com.google.gson.Gson
import io.paperdb.Paper


object SessionManager {
    var CURRENT_LOGGED_IN_USER_ID: String = "101"
}

class AppContainer {
    val tagRepository = PaperDbTagRepository()
    val ingredientRepository = PaperDbIngredientRepository()
    val userRepository = PaperDbUserRepository()
    val recipeRepository = PaperDbRecipeRepository()
    val notificationRepository = FakeNotificationRepository()
}

class TasteToShareApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        Paper.book().destroy()
        val jsonString =
            applicationContext.assets.open("seed_data.json").bufferedReader().use { it.readText() }
        val wrapperPaperDB = Gson().fromJson(jsonString, WrapperPaperDB::class.java)

        Log.d("PAPER", "$wrapperPaperDB")

        val seeded: Boolean = Paper.book().read("seeded") ?: false
        if (!seeded) {
            Paper.book().write("seeded", true)
            wrapperPaperDB.recipes.forEach { recipe ->
                Paper.book("recipes").write(recipe.recipeId, recipe)
            }

            wrapperPaperDB.users.forEach { user ->
                Paper.book("users").write(user.userId, user)
            }

            wrapperPaperDB.ingredients.forEach {
                Paper.book("ingredients").write(it.ingredientId, it)
            }

            wrapperPaperDB.tags.forEach {
                Paper.book("tags").write(it.tagId, it)
            }
        }
        container = AppContainer()
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
        enableEdgeToEdge()
        setContent {
            TasteToShareTheme {
                AppNavigation()
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
