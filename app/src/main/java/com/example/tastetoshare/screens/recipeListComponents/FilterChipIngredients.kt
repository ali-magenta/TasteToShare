package com.example.tastetoshare.screens.recipeListComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastetoshare.data.entities.Ingredient
import kotlin.collections.forEach

@Composable
fun FilterChipIngredients(
    ingredients: List<Ingredient>,
    searchIngredient: String,
    selectedIngredients: List<Ingredient>,
    onSearchIngredientChanged: (String) -> Unit,
    onIngredientsSelected: (List<Ingredient>) -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val isSelected = selectedIngredients.isNotEmpty()

    Box {
        FilterChip(
            selected = isSelected,
            onClick = {
                isMenuOpen = !isMenuOpen
            },
            label = {
                Text(
                    if(!isSelected) "Ingredients"
                    else "${selectedIngredients.first().name}${if (selectedIngredients.size > 1) " +${selectedIngredients.size - 1}" else ""}",
                    modifier = Modifier.padding(vertical = 10.dp),
                ) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open menu",
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = { isMenuOpen = false }
        ) {
            DropdownMenuItem(
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = searchIngredient,
                            onValueChange = onSearchIngredientChanged,
                            label = { Text("Search") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchIngredient.isNotEmpty()) {
                                    IconButton(onClick = { onSearchIngredientChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isSelected) {
                            TextButton(onClick = { onIngredientsSelected(emptyList()) }) {
                                Text("Clear selected")
                            }
                        }
                    }
                },
                onClick = {}
            )

            ingredients.forEach { ingredient ->
                val isIngredientSelected = ingredient in selectedIngredients

                DropdownMenuItem(
                    text = { Text(ingredient.name) },
                    leadingIcon = {
                        Checkbox(
                            checked = isIngredientSelected,
                            onCheckedChange = null
                        )
                    },
                    onClick = {
                        val newSelection = if(isIngredientSelected) {
                            selectedIngredients - ingredient
                        } else {
                            selectedIngredients + ingredient
                        }
                        onIngredientsSelected(newSelection)
                    }
                )
            }
        }

    }
}