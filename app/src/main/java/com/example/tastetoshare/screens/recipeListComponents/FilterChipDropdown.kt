package com.example.tastetoshare.screens.recipeListComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> FilterChipDropdown(
    label:String,
    selectedOption: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T?) -> Unit) {
    // using mutable state because dropdown menu open or not doesn't need to  survive and use viewModel ui states
    var isMenuOpen by remember { mutableStateOf(false) }
    val isSelected = selectedOption != null

    Box {
        FilterChip(
            selected = isSelected,
            onClick = {
                isMenuOpen = !isMenuOpen
            },
            label = {
                Text(
                    if(!isSelected) label
                    else optionLabel(selectedOption),
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        isMenuOpen = false
                    }
                )
            }

            if (isSelected) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {Text("Clear")},
                    onClick = {
                        onOptionSelected(null)
                        isMenuOpen = false
                    }
                )
            }

        }

    }
}