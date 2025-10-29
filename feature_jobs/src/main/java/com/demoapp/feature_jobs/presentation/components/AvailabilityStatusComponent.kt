package com.demoapp.feature_jobs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AvailabilityStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}

@Composable
fun AvailabilityStatusComponent(
    status: AvailabilityStatus,
    showLabel: Boolean = true,
    size: AvailabilitySize = AvailabilitySize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon, statusText) = when (status) {
        AvailabilityStatus.AVAILABLE -> Triple(
            Color(0xFF4CAF50), // Green
            Icons.Default.CheckCircle,
            "Available"
        )
        AvailabilityStatus.BUSY -> Triple(
            Color(0xFFFF9800), // Orange
            Icons.Default.Warning,
            "Busy"
        )
        AvailabilityStatus.OFFLINE -> Triple(
            Color(0xFF9E9E9E), // Gray
            Icons.Default.Close,
            "Offline"
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status indicator dot
        Box(
            modifier = Modifier
                .size(if (size == AvailabilitySize.SMALL) 8.dp else 12.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        
        // Status icon
        Icon(
            imageVector = statusIcon,
            contentDescription = statusText,
            tint = statusColor,
            modifier = Modifier.size(
                when (size) {
                    AvailabilitySize.SMALL -> 16.dp
                    AvailabilitySize.MEDIUM -> 20.dp
                    AvailabilitySize.LARGE -> 24.dp
                }
            )
        )
        
        // Status text
        if (showLabel) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = when (size) {
                    AvailabilitySize.SMALL -> 12.sp
                    AvailabilitySize.MEDIUM -> 14.sp
                    AvailabilitySize.LARGE -> 16.sp
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AvailabilityStatusCard(
    status: AvailabilityStatus,
    contractorName: String,
    lastSeen: String? = null,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon, statusText) = when (status) {
        AvailabilityStatus.AVAILABLE -> Triple(
            Color(0xFF4CAF50), // Green
            Icons.Default.CheckCircle,
            "Available"
        )
        AvailabilityStatus.BUSY -> Triple(
            Color(0xFFFF9800), // Orange
            Icons.Default.Warning,
            "Busy"
        )
        AvailabilityStatus.OFFLINE -> Triple(
            Color(0xFF9E9E9E), // Gray
            Icons.Default.Close,
            "Offline"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = contractorName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (lastSeen != null && status == AvailabilityStatus.OFFLINE) {
                        Text(
                            text = "Last seen: $lastSeen",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

enum class AvailabilitySize {
    SMALL,
    MEDIUM,
    LARGE
}
