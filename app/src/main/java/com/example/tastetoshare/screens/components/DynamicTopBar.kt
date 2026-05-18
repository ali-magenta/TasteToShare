package com.example.tastetoshare.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.tastetoshare.navigation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopBar(
    currentDestination: NavDestination?,
    onNavigateBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    if (currentDestination == null || currentDestination.hasRoute<WelcomeRoute>()) {
        return
    }

    val isCreateRecipeFlow = currentDestination?.hierarchy?.any {
        it.hasRoute<CreateRecipeGraphRoute>()
    } == true

    var title = ""
    var showBackButton = true
    var showNotificationsButton = false
    var topBarColor = MaterialTheme.colorScheme.primaryContainer

    when {
        currentDestination.hasRoute<HomeRoute>() -> {
            title = "Home"
            showBackButton = false
            showNotificationsButton = true
        }
        currentDestination.hasRoute<ProfileRoute>() -> {
            title = "Profile"
            showBackButton = false
        }
        isCreateRecipeFlow -> {
            title = "Create Recipe"
            showBackButton = true
        }
        currentDestination.hasRoute<SavedRecipeRoute>() -> {
            title = "Saved Recipes"
            showBackButton = false
        }
        currentDestination.hasRoute<ShoppingListRoute>() -> {
            title = "Shopping List"
            showBackButton = false
        }
        currentDestination.hasRoute<LoginRoute>() -> {
            title = "Sign In"
            showBackButton = true
            topBarColor = MaterialTheme.colorScheme.background
        }
        currentDestination.hasRoute<RegisterRoute>() -> {
            title = "Sign Up"
            showBackButton = true
            topBarColor = MaterialTheme.colorScheme.background
        }
        currentDestination.hasRoute<EditProfileRoute>() -> {
            title = "Edit Profile"
            showBackButton = true
        }
        currentDestination.hasRoute<NotificationRoute>() -> {
            title = "Notifications"
            showBackButton = true
        }
        currentDestination.hasRoute<RecipeDetailsRoute>() -> {
            title = "Recipe"
            showBackButton = true
        }
        currentDestination.hasRoute<RecipeListRoute>() -> {
            title = "Recipes"
            showBackButton = true
        }
        else -> return
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        actions = {
            if (showNotificationsButton) {
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = topBarColor
        )
    )
}