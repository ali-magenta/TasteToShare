package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun CopiedImageIcon(recipeImage: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val outerSize = this.maxWidth
        val innerSize = outerSize * 0.9f
        AsyncImage(
            model = recipeImage,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
        )
    }
}