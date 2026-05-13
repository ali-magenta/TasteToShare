package com.example.tastetoshare.screens.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.tastetoshare.R
import com.example.tastetoshare.navigation.HomeRoute
import com.example.tastetoshare.navigation.ProfileRoute
import com.example.tastetoshare.navigation.SavedRecipeRoute
import com.example.tastetoshare.navigation.ShoppingListRoute

@SuppressLint("RestrictedApi")
@Composable
fun TasteToShareBottomBar(
    currentDestination: NavDestination?,
    onNavigateHome: () -> Unit,
    onNavigateShopping: () -> Unit,
    onNavigateSaved: () -> Unit,
    onNavigateProfile: () -> Unit,
    onCreateRecipe: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.Transparent)
    ) {
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp
        ) {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.surfaceDim,
                unselectedTextColor = MaterialTheme.colorScheme.surfaceDim
            )

            NavigationBarItem(
                selected = currentDestination?.hasRoute<HomeRoute>() == true,
                onClick = onNavigateHome,
                icon = { Icon(painterResource(id=R.drawable.home), contentDescription = "Home", modifier = Modifier.size(22.dp)) },
                label = { Text("HOME", fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = navColors
            )

            NavigationBarItem(
                selected = currentDestination?.hasRoute<ShoppingListRoute>() == true,
                onClick = onNavigateShopping,
                icon = { Icon(painterResource(id=R.drawable.cart), contentDescription = "Shopping", modifier = Modifier.size(22.dp)) },
                label = { Text("SHOPPING", fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = navColors
            )

//            NavigationBarItem(
//                selected = false,
//                onClick = onCreateRecipe,
//                icon = {
//                    Box(
//                        modifier = Modifier
//                            .size(64.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.primary),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            painterResource(id= R.drawable.plus),
//                            contentDescription = "Add",
//                            tint = Color.White,
//                            modifier = Modifier.size(36.dp)
//                        )
//                    }
//                }
//            )

            NavigationBarItem(
                selected = false,
                onClick = onCreateRecipe,
                icon = { },
                enabled = false
            )

            NavigationBarItem(
                selected = currentDestination?.hasRoute<SavedRecipeRoute>() == true,
                onClick = onNavigateSaved,
                icon = { Icon(painterResource(id=R.drawable.not_saved_recipe), contentDescription = "Saved", modifier = Modifier.size(22.dp)) },
                label = { Text("SAVED", fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = navColors
            )

            NavigationBarItem(
                selected = currentDestination?.hasRoute<ProfileRoute>() == true,
                onClick = onNavigateProfile,
                icon = { Icon(painterResource(id=R.drawable.profile), contentDescription = "Profile", modifier = Modifier.size(22.dp)) },
                label = { Text("PROFILE", fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = navColors
            )
        }
        FloatingActionButton(
            onClick = onCreateRecipe,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .size(64.dp)
        ) {
                Icon(
                    painterResource(id=R.drawable.plus),
                    contentDescription = "New recipe icon",
                    modifier = Modifier.size(48.dp),
                )
        }
    }
}