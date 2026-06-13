package com.example.tastetoshare.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.viewmodels.CreateRecipeUIState

@Composable
fun IngredientsStep(
    uiState: CreateRecipeUIState,
    onDeleteClick: (String) -> Unit,
    onNextClick: () -> Unit,
    onIngredientSearchChange: (String) -> Unit,
    onIngredientClick: (Ingredient) -> Unit,
    onAddIngredientClick: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = "Shopping list",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        item {
            Text(
                text = "List the items needed for your recipe.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp
            )
        }
        item{
            AutocompleteSearchBar<Ingredient>(
                query = uiState.fieldSearchIngredient,
                onQueryChange = onIngredientSearchChange,
                searchResults = uiState.displayedIngredients,
                itemText = { ingredient -> ingredient.name },
                onItemSelect = onIngredientClick,
                onCreateNewItem = {
                    ingredientName ->
                    val newIngredient = Ingredient(name = ingredientName, isUserCreated = true)
                    onIngredientClick(newIngredient)
                },
                placeholderText = "Search or create (e.g. Fresh Basil)...",
                modifier = Modifier.padding(bottom = 0.dp)
            )
            if(uiState.selectedIngredient != null)
            {
                Text(
                    text = "Selected ingredient : ${uiState.selectedIngredient.name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        if(uiState.selectedIngredient != null){
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(
                            text = "Quantity",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = uiState.fieldIngredientQuantity,
                            onValueChange = onQuantityChange,
                            placeholder = { Text("e.g. 2.5", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(0.6f)) {
                        Text(
                            text = "Unit",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextFieldWithList<String>(
                            text = uiState.fieldIngredientUnit,
                            onTextChange = onUnitChange,
                            itemList = uiState.selectedIngredient?.unitOfMeasurementToCaloryPerUnit?.keys?.toList()?:emptyList(),
                            itemText = { it },
                            onItemSelect = onUnitChange,
                            placeholderText = "e.g. cups, tbsp, g",
                            modifier = Modifier.padding(bottom = 0.dp).visible(uiState.selectedIngredient != null)
                        )
                    }
                }
            }

            item{
                OutlinedButton(
                    onClick = onAddIngredientClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.addstep),
                        contentDescription = "Add ingredient icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add ingredient",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                errorAlertBox(uiState.ingredientError)
            }
        }


        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Added Ingredients",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${uiState.recipeIngredients.size} ITEMS",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        uiState.recipeIngredients.forEach({ ingredient ->
            item{
                IngredientItem(
                    name = ingredient.ingredient.name,
                    quantity = ingredient.quantity.toString(),
                    imageUrl = ingredient.ingredient.photoURL,
                    unit = ingredient.customUnitOfMeasurement,
                    onDeleteClick = { onDeleteClick(ingredient.recipeIngredientId) }
                )
            }
        })

        item {
            errorAlertBox(uiState.recipeIngredientsError)
            NextStep(onNextClick)
        }
    }
}
@Composable
fun IngredientItem(
    name: String,
    quantity: String,
    unit: String,
    imageUrl: String? = null,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if(imageUrl != null)
            {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${name} image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop

                )
            }
            else
            {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = "Ingredient image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = "${quantity} ${unit}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.trash),
                contentDescription = "trash icon",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}