package com.example.tastetoshare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tastetoshare.R
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.viewmodels.NotificationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationDestination(
    onNavigateToProfile: (userId: String) -> Unit,
    onNavigateToRecipeDetails: (recipeId: String) -> Unit,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory)
)
{
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    NotificationScreen(
        navigateToProfile = onNavigateToProfile,
        navigateToRecipeDetails = onNavigateToRecipeDetails,
        notifications = notifications
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigateToProfile: (userId: String) -> Unit,
    navigateToRecipeDetails: (recipeId: String) -> Unit,
    notifications: List<UINotification>
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                title = {
                    Text("Recipe", fontWeight = FontWeight.Bold)
                },
            )
        }
    ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (notifications.isNotEmpty()){
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(notifications) { index, notification ->
                            Spacer(modifier = Modifier.height(6.dp))
                            NotificationCard(
                                notification = notification,
                                navigateToProfile = navigateToProfile,
                                navigateToRecipeDetails = navigateToRecipeDetails
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (index < notifications.size-1) {
                                HorizontalDivider(
                                    thickness = 2.dp,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun NotificationCard(
    notification: UINotification,
    navigateToProfile: (userId: String) -> Unit,
    navigateToRecipeDetails: (recipeId: String) -> Unit
) {
    val icon = when (notification.type) {
        NotificationType.RECOMMENDED_RECIPE -> {
            R.drawable.servings
        }
        NotificationType.DUPLICATED_RECIPE -> {
            R.drawable.duplicate
        }
        else -> {
            R.drawable.comment
        }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable{
                navigateToRecipeDetails(notification.recipe.recipeId)
            }
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Column() {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Star icon",
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = Instant.ofEpochMilli(notification.creationDate)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                        ,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = notification.message,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = notification.recipe.photoURL,
                    contentDescription = "Recipe photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(7f / 4f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}