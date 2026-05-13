package com.example.tastetoshare.screens.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.viewmodels.CreateRecipeUIState

@Composable
fun TagsStep(
    uiState: CreateRecipeUIState,
    onCuisineTagClick: (Tag) -> Unit,
    onDietaryTagClick: (Tag) -> Unit,
    onOtherTagClick: (Tag) -> Unit,
    onCuisineSearchChange: (String) -> Unit,
    onDietarySearchChange: (String) -> Unit,
    onOtherSearchChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onOriginSearchChange: (String) -> Unit,
    onOriginTagClick: (Tag) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = "Tags",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        item {
            Text(
                text = "List the tags according to your recipe.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp
            )
        }

        item {
            Text(
                text = "Cuisine Type",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AutocompleteSearchBar(
                query = uiState.fieldSearchCuisineTypeTags,
                onQueryChange = onCuisineSearchChange,
                searchResults = uiState.searchResultCuisineTypeTags,
                itemText = { tag -> tag.name },
                onItemSelect = onCuisineTagClick,
                placeholderText = "Search cuisine (e.g. Italian)..."
            )
            TagsList(
                tagsToDisplay = (uiState.suggestedCuisineTypeTags + uiState.selectedCuisineTypeTags).distinct(),
                selectedTags = uiState.selectedCuisineTypeTags,
                onTagClick = onCuisineTagClick
            )
        }

        item {
            Text(
                text = "Dietary Restrictions",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AutocompleteSearchBar(
                query = uiState.fieldSearchDietaryRestrictionTags,
                onQueryChange = onDietarySearchChange,
                searchResults = uiState.searchResultDietaryRestrictionTags,
                itemText = { tag -> tag.name },
                onItemSelect = onDietaryTagClick,
                placeholderText = "Search diet (e.g. Vegan)..."
            )
            TagsList(
                tagsToDisplay = (uiState.suggestedDietaryRestrictionTags + uiState.selectedDietaryRestrictionTags).distinct(),
                selectedTags = uiState.selectedDietaryRestrictionTags,
                onTagClick = onDietaryTagClick
            )
        }

        item {
            Text(
                text = "Origin country",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AutocompleteSearchBar(
                query = uiState.fieldSearchCountryOfOriginTag,
                onQueryChange = onOriginSearchChange,
                searchResults = uiState.searchResultCountryTags,
                itemText = { tag -> tag.name },
                onItemSelect = onOriginTagClick,
                placeholderText = "Search country (e.g. France)..."
            )
            TagsList(
                tagsToDisplay = (uiState.suggestedCountryTags + listOfNotNull(uiState.selectedCountryOfOrigin)).distinct(),
                selectedTags = listOfNotNull(uiState.selectedCountryOfOrigin),
                onTagClick = onOriginTagClick
            )
        }

        item {
            Text(
                text = "Other",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AutocompleteSearchBar(
                query = uiState.fieldSearchOtherTags,
                onQueryChange = onOtherSearchChange,
                searchResults = uiState.searchResultOtherTags,
                itemText = { tag -> tag.name },
                onItemSelect = onOtherTagClick,
                placeholderText = "Search other..."
            )
            TagsList(
                tagsToDisplay = (uiState.suggestedOtherTags + uiState.selectedOtherTags).distinct(),
                selectedTags = uiState.selectedOtherTags,
                onTagClick = onOtherTagClick
            )
        }

        item {
            NextStep(onNextClick, text = "Next Step")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsList(
    tagsToDisplay: List<Tag>,
    selectedTags: List<Tag>,
    onTagClick: (Tag) -> Unit,
) {
    if (tagsToDisplay.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            tagsToDisplay.forEach { tag ->
                val isSelected = selectedTags.contains(tag)

                FilterChip(
                    onClick = { onTagClick(tag) },
                    label = {
                        Text(
                            text = tag.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        labelColor = MaterialTheme.colorScheme.onTertiary,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.Transparent,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    )
                )
            }
        }
    }
}