package com.example.tastetoshare.screens.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastetoshare.R
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.viewmodels.CreateRecipeUIState
import com.example.tastetoshare.viewmodels.PhotoTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoStep(
    uiState: CreateRecipeUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPrepTimeChange: (Int) -> Unit,
    onCostChange: (Cost) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onServingsChange: (Int) -> Unit,
    onNextClick: () -> Unit,
    onSetPhotoTarget: (PhotoTarget) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = uiState.preparationTime?.div(60) ?: 0,
        initialMinute = uiState.preparationTime?.rem(60) ?: 0,
        is24Hour = true
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val totalMinutes = timeState.hour * 60 + timeState.minute
                    onPrepTimeChange(totalMinutes)
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Set preparation time")
            },
            text = {
                TimeInput(state = timeState)
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize())
    {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    text = "Basic info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            item {
                Text(
                    text = "Let's start with the fundamentals of your dish.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 14.sp
                )
            }
            item {

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            item {
                RecipeInput(
                    title = "Title (*)",
                    value = uiState.title,
                    placeholder = "e.g., Chocolate Chip Cookies",
                    onValueChange = onTitleChange,
                    isError = uiState.titleError.isNotBlank(),
                    error = uiState.titleError
                )
            }

            // Photo
            item {
                PhotoBox(
                    title = "Recipe Photo",
                    text = "Add a cover photo",
                    backgroundPhoto = uiState.photoURL ?: "",
                    onAddPhotoClick = {
                        Log.d("DEBUG", "onAddPhotoClick")
                        onSetPhotoTarget(PhotoTarget.RECIPE_PHOTO)
                        onNavigateToPictureDialog(false)
                        //onPhotoSelected("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=1000")
                    }
                )
            }

            // Description
            val descriptionBoxSize = 150.dp
            item {
                RecipeInput(
                    title = "Description",
                    value = uiState.description,
                    placeholder = "e.g., Delicious chocolate chip cookies...", false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp).height(descriptionBoxSize),
                    onValueChange = onDescriptionChange
                )
            }

            // Cost & difficulty
            item {
                val costOptions = Cost.entries
                val difficultyOptions = Difficulty.entries

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    // Cost
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cost (*)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        CustomDropdownMenu(
                            options = costOptions.map {
                                it.displayName
                            },
                            selectedOption = uiState.cost.displayName,
                            onOptionSelected = { selectedString ->
                                onCostChange(Cost.fromDisplayName(selectedString)?: Cost.AFFORDABLE)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.budget),
                                    contentDescription = "Cost Icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        )
                        errorAlertBox(uiState.costError)
                    }

                    // Difficulty
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Difficulty (*)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        CustomDropdownMenu(
                            options = difficultyOptions.map {
                                it.displayName
                            },
                            selectedOption = uiState.difficulty.displayName,
                            onOptionSelected = { selectedString ->
                                onDifficultyChange(Difficulty.fromDisplayName(selectedString)?: Difficulty.MEDIUM)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.timer),
                                    contentDescription = "Difficulty Icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        )
                        errorAlertBox(uiState.difficultyError)
                    }
                }
            }

            // Preparation time
            item {
                Text(
                    text = "Preparation time (*)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val totalMinutes = uiState.preparationTime ?: 0
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60

                val timeDisplay = when {
                    hours > 0 -> "${hours}h ${mins}m"
                    mins > 0 -> "$mins mins"
                    else -> "Set time"
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = timeDisplay,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.time),
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray,
                            disabledContainerColor = MaterialTheme.colorScheme.background
                        ),
                        isError = uiState.preparationTimeError.isNotBlank(),
                        supportingText = {
                            if (uiState.preparationTimeError.isNotBlank()) {
                                Text(
                                    text = uiState.preparationTimeError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .clickable { showTimePicker = true },
                        color = Color.Transparent
                    ) {}
                }
            }

            // Servings
            item {
                Text(
                    text = "Servings (*)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ServingsButton(
                    value = uiState.servings,
                    onIncrement = { onServingsChange(uiState.servings + 1) },
                    onDecrement = {
                        if ((uiState.servings) > 1) {
                            onServingsChange(uiState.servings - 1)
                        }
                    }
                )

                errorAlertBox(uiState.servingsError)
            }

            // Next step
            item {
                NextStep(onNextClick)
            }
        }
    }

}

@Composable
fun ServingsButton(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.servings),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Number of servings",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // -
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("-")
            }

            Text(
                text = value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // +
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("+")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = { },
            leadingIcon = leadingIcon,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(50.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedBorderColor = Color(0xFFE0F2E9),
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            ),
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}