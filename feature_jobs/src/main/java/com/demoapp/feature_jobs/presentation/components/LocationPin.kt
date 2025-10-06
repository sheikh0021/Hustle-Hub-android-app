package com.demoapp.feature_jobs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demoapp.feature_jobs.presentation.models.JobData

@Composable
fun LocationPin(
    job: JobData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Pin Icon
            Text(
                text = "📍",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Location Name or Distance
            val locationText = when (job.jobType) {
                "Shopping" -> job.deliveryAddress ?: "Delivery location"
                "Delivery" -> job.pickupAddress ?: "Pickup location"
                "Survey" -> job.targetArea ?: "Survey area"
                else -> job.locationName ?: "${String.format("%.1f", job.distance)} km away"
            }

            Text(
                text = locationText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        // Distance Badge
        Text(
            text = "${String.format("%.1f", job.distance)} km",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
