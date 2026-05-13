package com.example.tastetoshare.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.data.Roles
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIRecipePreview

@Composable
fun ProfileInfo(
    user: User,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = user.fullName,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "@${user.userName}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        UserRoleRow(role = user.role)

        if (user.description.isNotBlank()) {
            Text(
                text = user.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun UserRoleRow(role: String) {
    val displayedRole = Roles.entries.toList().find { it.displayName == role }
    val icon = displayedRole?.icon ?: R.drawable.pan_pot

    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Role icon",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = role.ifBlank { "No role specified" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecipeCard(recipe: UIRecipePreview,
               onNavigateToDetails: (String) -> Unit,
               onSaveClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onNavigateToDetails(recipe.recipeId) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = recipe.photoURL,
                contentDescription = "Recipe photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Star icon",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${recipe.rating} (${recipe.numberOfRatings} reviews)",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        painter = painterResource(id = if (recipe.isSaved) R.drawable.saved_recipe else R.drawable.not_saved_recipe),
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = {onSaveClick(recipe.recipeId)}
                        )
                    )
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${recipe.preparationTime} min • ${recipe.difficulty} • ${recipe.calories} kcal",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (tag in recipe.tags) {
                        RecipeTag(tag.name)
                    }
                }
            }
        }
    }
}