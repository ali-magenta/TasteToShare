package com.example.tastetoshare.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastetoshare.data.Roles
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.screens.components.AutocompleteSearchBar
import com.example.tastetoshare.screens.components.TagsList
import com.example.tastetoshare.screens.components.errorAlertBox
import com.example.tastetoshare.viewmodels.LoginRegisterUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    uiState: LoginRegisterUIState,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBiographyChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onPhotoSelectedFromGallery: (Uri?) -> Unit,
    onCreateAccount: () -> Unit,
    onCuisineSearchChange: (String) -> Unit,
    onDietarySearchChange: (String) -> Unit,
    onOtherSearchChange: (String) -> Unit,
    onCuisineTagClick: (Tag) -> Unit,
    onDietaryTagClick: (Tag) -> Unit,
    onOtherTagClick: (Tag) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 17.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Join Kitchen People",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your account to start your culinary journey.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        SectionContainer {
            SectionHeader(title = "Profile Picture", icon = Icons.Outlined.CameraAlt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { onNavigateToPictureDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.editedPhoto != null) {
                        AsyncImage(
                            model = uiState.editedPhoto,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        SectionContainer {
            SectionHeader(title = "Personal Information", icon = Icons.Outlined.Person)
            SignUpPersonalInformation(
                uiState = uiState,
                onNameChange = onNameChange,
                onUsernameChange = onUsernameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onBiographyChange = onBiographyChange
            )
        }

        SectionContainer {
            SectionHeader(title = "Cooking Preferences", icon = Icons.Outlined.RestaurantMenu)
            SignUpPreferences(
                uiState = uiState,
                onCuisineSearchChange = onCuisineSearchChange,
                onDietarySearchChange = onDietarySearchChange,
                onOtherSearchChange = onOtherSearchChange,
                onCuisineTagClick = onCuisineTagClick,
                onDietaryTagClick = onDietaryTagClick,
                onOtherTagClick = onOtherTagClick
            )
            errorAlertBox(uiState.validation.tagsError)
        }

        SectionContainer {
            SectionHeader(title = "Cooking role", icon = Icons.Outlined.RestaurantMenu)

            SignUpRoleSelection(
                selectedRole = uiState.editedCookingRole,
                onRoleChange = onRoleChange
            )

            errorAlertBox(uiState.validation.cookingRoleError)
        }

        // Create account button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 17.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onCreateAccount,
                enabled = !uiState.isCreatingAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val termsText = buildAnnotatedString {
                append("By signing up, you agree to our ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Terms of Service")
                }
                append(" and\n")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Privacy Policy.")
                }
            }
            Text(
                text = termsText,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
    if (uiState.isCreatingAccount) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Saving review...")
                }
            }
        }
    }
}

@Composable
fun SectionContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(start = 17.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SignUpPersonalInformation(
    uiState: LoginRegisterUIState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBiographyChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SignUpTextField(
            label = "Full Name*",
            value = uiState.editedFullName ?: "",
            onValueChange = onNameChange,
            placeholder = "Enter your full name",
            error = uiState.validation.fullNameError
        )

        SignUpTextField(
            label = "Username*",
            value = uiState.editedUsername ?: "",
            onValueChange = onUsernameChange,
            placeholder = "Enter your username",
            error = uiState.validation.usernameError
        )

        SignUpTextField(
            label = "Email Address*",
            value = uiState.editedEmail ?: "",
            onValueChange = onEmailChange,
            placeholder = "email@example.com",
            error = uiState.validation.emailError
        )

        SignUpTextField(
            label = "Password*",
            value = uiState.editedPassword,
            onValueChange = onPasswordChange,
            placeholder = "Min 8 characters",
            isPassword = true,
            error = uiState.validation.passwordError
        )

        Column {
            Text(
                text = "Biography",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.editedBiography ?: "",
                onValueChange = { if (it.length <= 140) onBiographyChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                supportingText = {
                    Text(
                        text = "${(uiState.editedBiography ?: "").length}/140",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = Color.Gray
                    )
                }
            )
        }
    }
}

@Composable
fun SignUpTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    error: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = error.isNotBlank(),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

@Composable
fun SignUpPreferences(
    uiState: LoginRegisterUIState,
    onCuisineSearchChange: (String) -> Unit,
    onDietarySearchChange: (String) -> Unit,
    onOtherSearchChange: (String) -> Unit,
    onCuisineTagClick: (Tag) -> Unit,
    onDietaryTagClick: (Tag) -> Unit,
    onOtherTagClick: (Tag) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PreferenceCategory(
            title = "Cuisine Type",
            placeholder = "Search cuisine (e.g. Italian)...",
            fieldSearch = uiState.fieldSearchCuisineTypeTags,
            onSearchChange = onCuisineSearchChange,
            results = uiState.searchResultCuisineTypeTags,
            onTagClick = onCuisineTagClick,
            suggestedTags = uiState.suggestedCuisineTypeTags,
            selectedTags = uiState.selectedCuisineTypeTags,
        )

        PreferenceCategory(
            title = "Dietary Restrictions",
            placeholder = "Search diet (e.g. Vegan)...",
            fieldSearch = uiState.fieldSearchDietaryRestrictionTags,
            onSearchChange = onDietarySearchChange,
            results = uiState.searchResultDietaryRestrictionTags,
            onTagClick = onDietaryTagClick,
            suggestedTags = uiState.suggestedDietaryRestrictionTags,
            selectedTags = uiState.selectedDietaryRestrictionTags,
        )

        PreferenceCategory(
            title = "Other",
            placeholder = "Search tags (e.g. Quick, Spicy)...",
            fieldSearch = uiState.fieldSearchOtherTags,
            onSearchChange = onOtherSearchChange,
            results = uiState.searchResultOtherTags,
            onTagClick = onOtherTagClick,
            suggestedTags = uiState.suggestedOtherTags,
            selectedTags = uiState.selectedOtherTags,
        )
    }
}

@Composable
fun PreferenceCategory(
    title: String,
    placeholder: String,
    fieldSearch: String,
    onSearchChange: (String) -> Unit,
    results: List<Tag>,
    onTagClick: (Tag) -> Unit,
    suggestedTags: List<Tag>,
    selectedTags: List<Tag>,
) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AutocompleteSearchBar(
            query = fieldSearch,
            onQueryChange = onSearchChange,
            searchResults = results,
            itemText = { tag -> tag.name },
            onItemSelect = onTagClick,
            placeholderText = placeholder
        )

        Spacer(modifier = Modifier.height(8.dp))

        TagsList(
            tagsToDisplay = (suggestedTags + selectedTags).distinctBy { it.tagId },
            selectedTags = selectedTags,
            onTagClick = onTagClick
        )
    }
}
@Composable
fun SignUpRoleSelection(
    selectedRole: String,
    onRoleChange: (String) -> Unit
) {
    val roles = Roles.entries.toList()

    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        roles.forEach { role ->
            val isSelected = selectedRole == role.displayName
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .selectable(
                        selected = isSelected,
                        onClick = { onRoleChange(role.displayName) },
                        role = Role.RadioButton
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = role.icon),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = role.displayName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = role.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}