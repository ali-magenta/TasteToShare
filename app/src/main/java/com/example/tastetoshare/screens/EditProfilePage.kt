package com.example.tastetoshare.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.data.Roles
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.viewmodels.EditProfileFormValidation
import com.example.tastetoshare.viewmodels.ProfileUIState
import com.example.tastetoshare.viewmodels.ProfileViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun EditProfileDestination(
    onNavigateBack: () -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    getReturnedUri : () -> Flow<String?>,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {

    val user by viewModel.user.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val defaultTags by viewModel.defaultTags.collectAsStateWithLifecycle()

    val returnedUri by getReturnedUri().collectAsStateWithLifecycle(null)

    LaunchedEffect(returnedUri) {
        val uri = returnedUri
        if (uri != null) {
            viewModel.handlePhotoUri(uri)
        }
    }

    EditProfilePage(
        user = user,
        defaultTags = defaultTags,
        uiState = uiState,
        onNavigateToPictureDialog = onNavigateToPictureDialog,
        onNameChange = viewModel::onNameChange,
        onUsernameChange = viewModel::onUsernameChanged,
        onBiographyChange = viewModel::onBiographyChanged,
        onPreferencesChange = viewModel::onPreferencesChanged,
        onRoleChange = viewModel::onRoleChanged,
        onPasswordChange =  viewModel::onPasswordChanged,
        saveChanges = {
            viewModel.saveChanges()
            onNavigateBack()
        },
        goBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    user: User,
    defaultTags: List<Tag>,
    uiState: ProfileUIState,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBiographyChange: (String) -> Unit,
    onPreferencesChange: (List<Tag>) -> Unit,
    onRoleChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    saveChanges: () -> Unit,
    goBack: () -> Unit
) {
    BackHandler {
        goBack()
    }

    val user = checkNotNull(user) { "User must not be null in EditProfilePage" }
    val scrollState = rememberScrollState()     // make the whole page scrollable

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            EditProfileHeader(user, uiState.editedPhoto, { onNavigateToPictureDialog(true) })
            Icon(
                painter = painterResource(R.drawable.photo_camera),
                contentDescription = "Plus icon",
                modifier = Modifier
                    .size(80.dp)
                    .alpha(0.6f),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Personal Information", R.drawable.profile)
            EditPersonalInformation(user, uiState.editedFullName, uiState.validation, uiState.editedNickName, uiState.editedBiography, uiState.editedPassword, onNameChange, onUsernameChange, onBiographyChange, onPasswordChange, )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Cooking Preferences", R.drawable.local_dining)
            EditPreferences(user, defaultTags, uiState.hasEditedTags, uiState.editedTags, uiState.validation, onPreferencesChange)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Cooking Role", R.drawable.pan_pot)
            EditRole(user, uiState.editedCookingRole, onRoleChange)

        }

        SaveChangesButton(saveChanges)
    }
}

@Composable
fun EditProfileHeader(user: User?, editedPhoto: String?, onImageClick: () -> Unit) {
    if (user!!.photoURI != null || editedPhoto != null) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = editedPhoto ?: user.photoURI,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }
    } else {
        val initials = user.fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onImageClick() }
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }

}

@Composable
fun EditPersonalInformation(
    user: User,
    editedFullName: String?,
    validation: EditProfileFormValidation,
    editedNickName: String?,
    editedBiography: String?,
    editedPassword: String,
    onNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onBiographyChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Name
        Text(
            text = "Full name",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = editedFullName ?: user.fullName,
            onValueChange = { newName ->
                onNameChanged(newName)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            ),
            isError = validation.fullNameError.isNotBlank(),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text
            )
        )
        if(validation.fullNameError.isNotBlank()) {
            Text(validation.fullNameError, color = MaterialTheme.colorScheme.error)
        }
        
        // Username
        Text(
            text = "Username",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = editedNickName ?: user.userName,
            onValueChange = { newUsername ->
                onUsernameChanged(newUsername)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            ),
            isError = validation.nickNameError.isNotBlank(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )
        if(validation.nickNameError.isNotBlank()) {
            Text(validation.nickNameError, color = MaterialTheme.colorScheme.error)
        }

        // Biography
        Text(
            text = "Biography",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = editedBiography ?: user.description,
            onValueChange = { newBiography ->
                onBiographyChanged(newBiography)
            },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            ),
            isError = validation.biographyError.isNotBlank(),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text
            )
        )
        if(validation.biographyError.isNotBlank()) {
            Text(validation.biographyError, color = MaterialTheme.colorScheme.error)
        }

        // TODO: add limit of characters in description field

        // Email: cannot modify
        Text(
            text = "Email",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "The email field cannot be modified.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = user.email,
            onValueChange = {  },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            ),
            readOnly = true,
        )

        // Password (partial placeholder)
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = editedPassword,
            onValueChange = { newPassword ->
                onPasswordChanged(newPassword)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            ),
            isError = validation.passwordError.isNotBlank(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
        )
        if(validation.passwordError.isNotBlank()) {
            Text(validation.passwordError, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: Int) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Personal Information icon",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPreferences(user: User, tags: List<Tag>, hasEditedTags: Boolean, editedTags: List<Tag>, validation: EditProfileFormValidation, onPreferencesChanged: (List<Tag>) -> Unit) {
    val currentSelectedTags = if(hasEditedTags) {
        editedTags
    } else user.preferences
    val allTags = (user.preferences + tags).distinctBy { it.name }

    val tagMap: MutableMap<String, MutableList<Tag>> = mutableMapOf()
    allTags.forEach{ tag ->
        if (!tagMap.containsKey(tag.type)) {
            tagMap[tag.type] = mutableListOf(tag)
        }
        else {
            tagMap[tag.type]?.add(tag)
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (validation.tagsError.isNotBlank()) {
            Text(validation.tagsError, color = MaterialTheme.colorScheme.error)
        }
        for ((title, tags) in tagMap) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    CookingTags(tag = tag,
                        isSelected = currentSelectedTags.any { it.name == tag.name },
                        currentSelectedTags = currentSelectedTags,
                        onPreferencesChanged = onPreferencesChanged)
                }
            }
        }
    }

}

@Composable
fun CookingTags(tag: Tag, isSelected: Boolean, currentSelectedTags: List<Tag>, onPreferencesChanged: (List<Tag>) -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = {
            val newSelection = if(isSelected) {
                currentSelectedTags.filter {it.name != tag.name}
            } else currentSelectedTags + tag

            onPreferencesChanged(newSelection)
        },
        label = { Text(tag.name) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
        trailingIcon = {
            if(isSelected) {
                Icon(
                    painter = painterResource(R.drawable.close), contentDescription = "remove tag"
                )
            }
        },
    )
}

@Composable
fun EditRole(user: User, editedCookingRole: String?, onRoleChanged: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val radioOptions = Roles.entries.toList()
        val selectedRole = editedCookingRole ?: user.role

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.selectableGroup()) {
                radioOptions.forEach { role ->
                    val isSelected = role.displayName == selectedRole
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (isSelected),
                                onClick = {
                                    onRoleChanged(role.displayName)
                                },
                                role = Role.RadioButton,
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(role.icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = role.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }

                        RadioButton(
                            selected = (role.displayName == selectedRole),
                            onClick = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaveChangesButton(saveChanges: () -> Unit) {
    Button(
        onClick = saveChanges,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ) {
        Text(
            text = "Save Changes",
            style = MaterialTheme.typography.titleMedium,
        )
    }}

