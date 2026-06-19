package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RecipeIngredient

@Composable
fun IngredientRow(recipeIngredient: RecipeIngredient, modifier: Modifier = Modifier) {
    val displayUnit = if (recipeIngredient.customUnitOfMeasurement == "NO UNIT") "" else recipeIngredient.customUnitOfMeasurement
    val formattedQty = java.text.DecimalFormat("#.##").format(recipeIngredient.quantity)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IngredientPhoto(
                ingredient = recipeIngredient.ingredient,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${formattedQty} ${displayUnit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = recipeIngredient.ingredient.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun IngredientPhoto(ingredient: Ingredient, modifier: Modifier = Modifier) {
    if (ingredient.photoURL != null) {
        BoxWithConstraints(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val outerSize = this.maxWidth
            val innerSize = outerSize * 0.9f
            AsyncImage(
                model = ingredient.photoURL,
                contentDescription = "Ingredient picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape)
            )
        }
    } else {
        val initial = ingredient.name.trim().take(1).uppercase()
        IconWIthChar(initial, modifier)
    }
}

