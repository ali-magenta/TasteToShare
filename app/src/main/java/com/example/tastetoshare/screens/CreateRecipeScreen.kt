package com.example.tastetoshare.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeSharedLayout(
    currentStep: Int,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        StepIndicator(currentStep, 4)

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int = 4,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().background(Color.Transparent),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isSelected = (index + 1) == currentStep

            val width = if (isSelected) 28.dp else 10.dp

            val color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.LightGray
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(10.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}