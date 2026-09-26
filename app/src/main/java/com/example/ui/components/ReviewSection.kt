package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.CourseReview
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewSection(
    course: Course,
    reviews: List<CourseReview>,
    isEnrolled: Boolean,
    onWriteReviewClick: () -> Unit,
    onHelpfulClick: (reviewId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStarFilter by remember { mutableIntStateOf(0) } // 0 means All

    val filteredReviews = remember(reviews, selectedStarFilter) {
        if (selectedStarFilter == 0) reviews else reviews.filter { it.rating == selectedStarFilter }
    }

    // Calculate rating distribution
    val totalReviewsCount = reviews.size.coerceAtLeast(1)
    val star5Count = reviews.count { it.rating == 5 }
    val star4Count = reviews.count { it.rating == 4 }
    val star3Count = reviews.count { it.rating == 3 }
    val star2Count = reviews.count { it.rating == 2 }
    val star1Count = reviews.count { it.rating == 1 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_reviews_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Student Reviews",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real feedback from enrolled students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Write Review CTA Button
            Button(
                onClick = onWriteReviewClick,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("write_review_cta_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = "Review",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEnrolled) "Write a Review" else "Leave Review",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Average Rating Summary & Graphical Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Text(
                        text = String.format("%.1f", course.rating),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = Amber500
                    )
                    Row {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = if (i < course.rating.toInt()) Amber500 else Slate400.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${reviews.size} reviews",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Distribution Progress Bars
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RatingBarRow(label = "5★", count = star5Count, total = totalReviewsCount)
                    RatingBarRow(label = "4★", count = star4Count, total = totalReviewsCount)
                    RatingBarRow(label = "3★", count = star3Count, total = totalReviewsCount)
                    RatingBarRow(label = "2★", count = star2Count, total = totalReviewsCount)
                    RatingBarRow(label = "1★", count = star1Count, total = totalReviewsCount)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips (All, 5 Stars, 4 Stars, 3 Stars)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0 to "All (${reviews.size})", 5 to "5 Stars ($star5Count)", 4 to "4 Stars ($star4Count)", 3 to "3 Stars ($star3Count)").forEach { (stars, label) ->
                val isSelected = selectedStarFilter == stars
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStarFilter = stars },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Indigo500,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_reviews_star_$stars")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of Student Reviews
        if (filteredReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reviews found for this rating tier yet. Be the first to share your thoughts!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredReviews.forEach { review ->
                    ReviewCardItem(
                        review = review,
                        onHelpfulClick = { onHelpfulClick(review.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(label: String, count: Int, total: Int) {
    val progress = (count.toFloat() / total).coerceIn(0f, 1f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Amber500,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
            modifier = Modifier.width(20.dp)
        )
    }
}

@Composable
private fun ReviewCardItem(
    review: CourseReview,
    onHelpfulClick: () -> Unit
) {
    var hasLiked by remember { mutableStateOf(false) }
    val displayHelpfulCount = review.helpfulCount + if (hasLiked) 1 else 0

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val dateString = remember(review.createdAt) { dateFormat.format(Date(review.createdAt)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_card_${review.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Student Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle with student initials
                    val initial = review.studentName.firstOrNull()?.uppercase() ?: "S"
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Indigo500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = review.studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (review.isVerifiedBuyer) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Verified Student",
                                    tint = Emerald500,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Learner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Date
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Star rating row
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Star",
                        tint = if (index < review.rating) Amber500 else Slate400.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${review.rating}.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Amber500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Written review body
            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Thumbs up / Helpful button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            if (!hasLiked) {
                                hasLiked = true
                                onHelpfulClick()
                            }
                        }
                        .background(
                            if (hasLiked) Indigo500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Helpful",
                        tint = if (hasLiked) Indigo500 else Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (displayHelpfulCount > 0) "Helpful ($displayHelpfulCount)" else "Helpful",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hasLiked) Indigo500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
