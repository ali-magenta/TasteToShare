package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.data.entities.Recipe


@Composable
fun RecipeHeader(recipe: Recipe, isSaved: Boolean, toggleSavedRecipe: (() -> Unit) -> Unit, onAuthRequired: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val boxHeight = if (this.maxHeight < this.maxWidth) this.maxHeight else this.maxWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight)

            ) {
                AsyncImage(
                    model = recipe.photoURL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    Box( // slight gradient to highlight text and button at the bottom
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            )
                    ) {}
                }
                TitleRatingSavedOverlay(recipe, isSaved, toggleSavedRecipe, onAuthRequired, Modifier.matchParentSize())
            }
        }
    }
}


@Composable
fun TitleRatingSavedOverlay(
    recipe: Recipe,
    isSaved: Boolean,
    toggleSavedRecipe: (() -> Unit) -> Unit,
    onAuthRequired: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
    ) {
        Rating(recipe.rating, Modifier.align(Alignment.TopEnd))
        TitleSave(recipe, isSaved, toggleSavedRecipe, onAuthRequired, Modifier.align(Alignment.BottomStart))
    }
}

@Composable
fun Rating(rating: Float, modifier: Modifier) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.25f),
                shape = CircleShape,
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.star),
            contentDescription = "rating",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable {}
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "$rating",
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun TitleSave(recipe: Recipe, isSaved: Boolean, toggleSavedRecipe: (() -> Unit) -> Unit, onAuthRequired: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = recipe.title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 35.sp,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )
        Button(
            onClick = {
                toggleSavedRecipe(onAuthRequired)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.25f), // semi-transparent background
                contentColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isSaved) R.drawable.saved_recipe else R.drawable.not_saved_recipe),
                contentDescription = "Save",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
        }
    }
}