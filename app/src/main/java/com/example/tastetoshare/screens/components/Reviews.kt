package com.example.tastetoshare.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.ui.theme.StarGold
import com.example.tastetoshare.data.uiClasses.UIReview
import com.example.tastetoshare.ui.theme.DangerRed
import com.example.tastetoshare.viewmodels.PhotoReviewTarget

@Composable
fun Reviews(
    reviews: List<UIReview>,
    rating: UInt,
    reviewText: String,
    onRatingChanged: (Int) -> Unit,
    onReviewTextChanged: (String) -> Unit,
    onAddReview: () -> Unit,
    userReview: UIReview?,
    showDeleteReviewPopup: (String) -> Unit,
    reviewPhotoUrl: String,
    onSetPhotoReviewTarget: (PhotoReviewTarget) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    focusReviewId: String? = null,
) {
    val listState = rememberLazyListState()

    val sortedReviews = remember(reviews, userReview) {
        val otherReviews = reviews.filter {
            it.user.userId != SessionManager.CURRENT_LOGGED_IN_USER_ID
        }
        if (userReview != null) listOf(userReview) + otherReviews
        else otherReviews
    }

    LaunchedEffect(focusReviewId, sortedReviews) {
        if (focusReviewId != null) {
            val index = sortedReviews.indexOfFirst { it.reviewId == focusReviewId }

            if (index != -1) {
                val targetIndex = if (userReview == null) index + 1 else index
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiary
            )
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Reviews and Tips",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (userReview == null) {
                item {
                    InputReviewCard(
                        rating,
                        reviewText,
                        onRatingChanged,
                        onReviewTextChanged,
                        onAddReview,
                        reviewPhotoUrl,
                        onSetPhotoReviewTarget,
                        onNavigateToPictureDialog
                    )
                }
            }
            items(sortedReviews) { review ->
                ReviewCard(review, showDeleteReviewPopup)
            }
        }
    }
}

@Composable
fun InputReviewCard(
    rating: UInt,
    reviewText: String,
    onRatingChanged: (Int) -> Unit,
    onReviewTextChanged: (String) -> Unit,
    onAddReview: () -> Unit,
    reviewPhotoUrl: String,
    onSetPhotoReviewTarget: (PhotoReviewTarget) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier) {
    //val reviewSafe = review ?: ""

    Card(
        modifier = modifier
            .width(300.dp)
            .heightIn(min = 210.dp)
            .padding(bottom=16.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column( modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Share your opinion!",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(5.dp))
                Row() {
                    for (i in 1..5) {
                        val icon = if (i.toUInt() <= rating) R.drawable.filled_star else R.drawable.outlined_star
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = "Star icon",
                            tint = StarGold,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    onRatingChanged(i)
                                }
                        )
                    }
                }
            }

            PhotoBox(
                title = "Add a photo (optional)",
                text = "Add a photo to your review",
                backgroundPhoto = reviewPhotoUrl,
                onAddPhotoClick = {
                    onSetPhotoReviewTarget(PhotoReviewTarget.REVIEW_PHOTO)
                    onNavigateToPictureDialog(false)
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextField(
                    value = reviewText,
                    onValueChange = onReviewTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    singleLine = false,
                    minLines = 5
                )
                Button(
                    onClick = onAddReview,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(48.dp),
                shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "Send review",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: UIReview, onDeleteReview: (String) -> Unit, modifier: Modifier = Modifier) {
    val isMyReview = review.user.userId == SessionManager.CURRENT_LOGGED_IN_USER_ID

    Card(
        modifier = modifier
            .width(300.dp)
            .heightIn(min = 210.dp)
            .padding(bottom=16.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        ProfileHeader(review.user, Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = review.user.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val rating = review.rating ?: 0
                    Row() {
                        for (i in 1..5) {
                            val icon =
                                if (i <= rating) R.drawable.filled_star else R.drawable.outlined_star
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = "Star icon",
                                tint = StarGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = review.body ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal,
                )

                if (review.photosUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    review.photosUrl.forEach { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Review photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (isMyReview) Spacer(modifier = Modifier.height(48.dp))
            }

            if (isMyReview) {
                Button(
                    onClick = { onDeleteReview(review.reviewId) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Delete review",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}