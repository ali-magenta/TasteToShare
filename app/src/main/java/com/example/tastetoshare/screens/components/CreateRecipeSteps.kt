package com.example.tastetoshare.screens.components

import android.provider.ContactsContract
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.viewmodels.CreateRecipeUIState
import com.example.tastetoshare.viewmodels.PhotoTarget

@Composable
fun InstructionsStep(
    uiState: CreateRecipeUIState,
    onNewStepDescriptionChange: (String) -> Unit,
    onAddStepClick: () -> Unit,
    onRemoveStepClick: (String) -> Unit,
    onNextClick: () -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    onSetPhotoTarget: (PhotoTarget) -> Unit,
    ) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Step Instructions",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        item {
            Text(
                text = "Explain how to prepare your dish step-by-step.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp
            )
        }

        items(
            items = uiState.steps,
            key = { step -> step.stepId }
        ) { step ->
            InstructionStepCard(
                stepNumber = step.order,
                instructionText = step.description,
                imageUrl = step.photoURL,
                onEditClick = {},
                onDeleteClick = { onRemoveStepClick(step.stepId) },
            )
        }

        item {
            NewStepCard(
                stepNumber = uiState.steps.size + 1,
                descriptionValue = uiState.fieldNewStepDescription,
                onDescriptionChange = onNewStepDescriptionChange,
                onNavigateToPictureDialog = onNavigateToPictureDialog,
                onSetPhotoTarget = onSetPhotoTarget,
                onAddStepClick = onAddStepClick,
                stepPhoto = uiState.newStepPhotoURL ?: ""
            )
        }

        item {
            errorAlertBox(uiState.stepsError)
            NextStep(onNextClick, text="Confirm & Finish Recipe")
        }
    }

}

@Composable
fun InstructionStepCard(
    stepNumber: Int,
    instructionText: String,
    imageUrl: String?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = instructionText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                imageUrl?.let {
                    if (it.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = it,
                            contentDescription = "Selected photo",
                            modifier =  Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id= R.drawable.dropdownpoints),
                        contentDescription = "Dropdown menu points",
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modify") },
                        onClick = {
                            expanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NewStepCard(
    stepNumber: Int,
    descriptionValue: String,
    onDescriptionChange: (String) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    onAddStepClick: () -> Unit,
    stepPhoto: String,
    onSetPhotoTarget: (PhotoTarget) -> Unit
) {
    val lightGreenBg = MaterialTheme.colorScheme.primaryContainer
    val primaryGreen = MaterialTheme.colorScheme.primary
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(lightGreenBg)
            .drawBehind {
                drawRoundRect(
                    color = primaryGreen,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "New Step",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            RecipeInput(title = "Step Description",
                value = descriptionValue,
                placeholder = "e.g., Gently fold in the blueberries...",
                isSingleLine = false,
                onValueChange = onDescriptionChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            PhotoBox(title="Photo (Optional)",
                onAddPhotoClick = {
                    onSetPhotoTarget(PhotoTarget.STEP_PHOTO)
                    onNavigateToPictureDialog(false)
                },
                text= "Add a step-by-step photo",
                backgroundPhoto = stepPhoto
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onAddStepClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, primaryGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.addstep),
                    contentDescription = "Add step icon",
                    tint = primaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Step",
                    color = primaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}