package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastetoshare.R

@Composable
fun ServingsField(
    servings: Int,
    selectedServings: Int?,
    calories: Int?,
    onIncreaseServingClicked: () -> Unit,
    onDecreaseServingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column() {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.servings),
                    contentDescription = "group icon",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Servings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.food),
                    contentDescription = "calories icon",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$calories  KCAL/servings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Column() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .padding(6.dp)
            ) {
                Button(
                    onClick = onDecreaseServingClicked,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "decrement servings icon"
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "${selectedServings ?: servings}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(7.dp))
                Button(
                    onClick = onIncreaseServingClicked,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus),
                        contentDescription = "increment servings icon"
                    )
                }
            }
        }
    }
}