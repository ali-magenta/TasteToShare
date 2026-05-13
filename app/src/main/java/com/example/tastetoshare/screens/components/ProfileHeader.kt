package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.tastetoshare.data.entities.User

@Composable
fun ProfileHeader(user: User, modifier: Modifier = Modifier) {
    if (user.photoURI != null) {
        BoxWithConstraints(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val outerSize = this.maxWidth
            val innerSize = outerSize * 0.9f
            AsyncImage(
                model = user.photoURI,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(innerSize)
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

        BoxWithConstraints(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val outerSize = this.maxWidth
            val innerSize = outerSize * 0.9f
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
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
}