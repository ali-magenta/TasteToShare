package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.tastetoshare.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun NextStep(onNextClick: () -> Unit, text: String = "Next Step") {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNextClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecipeInput(
    title: String,
    value: String,
    placeholder: String = "",
    isSingleLine: Boolean = false,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    error: String = "",
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = isSingleLine,
            placeholder = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            isError = isError,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            )
        )
        if(!error.isEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AutocompleteSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<T>,
    itemText: (T) -> String,
    onItemSelect: (T) -> Unit,
    onCreateNewItem: ((String) -> Unit)? = null,
    placeholderText: String = "Search..."
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            placeholder = { Text(placeholderText, color = Color.Gray) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F6),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        val exactMatchExists = searchResults.any {
            itemText(it).equals(query, ignoreCase = true)
        }
        val showCreateOption = onCreateNewItem != null && query.isNotBlank() && !exactMatchExists

        val shouldShowMenu = expanded && (searchResults.isNotEmpty() || showCreateOption)

        if (shouldShowMenu) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp),
                properties = PopupProperties(focusable = false)
            ) {
                searchResults.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = {
                            onItemSelect(item)
                            onQueryChange("")
                            expanded = false
                        }
                    )
                }

                if (onCreateNewItem != null && query.isNotBlank()) {
                    val exactMatchExists = searchResults.any {
                        itemText(it).equals(query, ignoreCase = true)
                    }

                    if (!exactMatchExists) {
                        if (searchResults.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Create new: \"$query\"",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.search),
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                onCreateNewItem(query)
                                onQueryChange("")
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OutlinedTextFieldWithList(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    itemList: List<T>,
    itemText: (T) -> String,
    onItemSelect: (T) -> Unit,
    placeholderText: String = "Search..."
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                onTextChange(it)
                expanded = true
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            placeholder = { Text(placeholderText, color = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        val shouldShowMenu = expanded && (itemList.isNotEmpty())

        if (shouldShowMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp),
                properties = PopupProperties(focusable = false)
            ) {
                itemList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = {
                            onItemSelect(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun PhotoBox(
    title: String,
    onAddPhotoClick: () -> Unit,
    text: String,
    backgroundPhoto: String = ""
) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
    val outline = MaterialTheme.colorScheme.outline

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val borderModifier = if (backgroundPhoto.isBlank()) {
            Modifier.drawBehind {
                drawRoundRect(
                    color = outline,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.height(120.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onAddPhotoClick() }
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            if (backgroundPhoto.isNotBlank()) {
                AsyncImage(
                    model = backgroundPhoto,
                    contentDescription = "Selected photo",
                    modifier =  Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Icon(
                        painter = painterResource(R.drawable.photo_camera),
                        contentDescription = "Camera icon",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun errorAlertBox(error:String)
{
    if(error.isNotBlank())
    {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically)
        {

            Icon(
                painter= painterResource(id=R.drawable.alert),
                contentDescription = "Alert icon",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}