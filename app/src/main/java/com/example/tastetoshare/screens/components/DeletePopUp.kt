package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tastetoshare.data.Screen
import com.example.tastetoshare.ui.theme.DangerRed


@Composable
fun DeletePopup(
    element: String,
    onNavigateToHome: () -> Unit,
    deleteRecipe: () -> Unit,
    dismissDeletePopUp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center

    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        )

        Card(
            modifier = Modifier
                .padding(20.dp, 220.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally ,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 20.dp)
                    //.fillMaxSize()
            ) {
                Text(
                    text = "Are you sure you want to delete the $element?",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier.weight(1F),
                        onClick = {
                            dismissDeletePopUp()
                        },
                    ) {
                        Text("No")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1F),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            deleteRecipe()
                            onNavigateToHome()
                                  },
                    ) {
                        Text("Yes")
                    }
                }

            }
        }
    }

}

@Composable
fun DeleteReviewPopup(
    deleteReview: () -> Unit,
    dismissDeletePopUp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center

    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        )

        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally ,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
                //.fillMaxSize()
            ) {
                Text(
                    text = "Are you sure you want to delete the review?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1F),
                        onClick = {
                            dismissDeletePopUp()
                        },
                    ) {
                        Text("No")
                    }
                    Button(
                        modifier = Modifier.weight(1F),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = { deleteReview() }
                    ) {
                        Text("Yes")
                    }
                }

            }
        }
    }

}