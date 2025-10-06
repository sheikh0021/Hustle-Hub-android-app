package com.demoapp.feature_jobs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demoapp.feature_jobs.data.JobTimelineRepository
import com.demoapp.feature_jobs.presentation.models.TimelineStage
import com.demoapp.feature_jobs.presentation.models.TimelineStatus
import com.demoapp.feature_jobs.presentation.models.getIcon
import com.demoapp.feature_jobs.presentation.models.getDisplayName
import com.demoapp.feature_jobs.presentation.models.getDescription
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JobTimelineComponent(
    jobId: String,
    modifier: Modifier = Modifier
) {
    val timelineRepository = JobTimelineRepository.getInstance()
    val timelines by timelineRepository.timelines.collectAsState()
    
    val timeline = timelines.find { it.jobId == jobId }
    
    if (timeline != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Job Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Timeline stages
                TimelineStage.values().forEachIndexed { index, stage ->
                    val stageData = timeline.stages.find { it.stage == stage }
                    val isCompleted = stageData?.status == TimelineStatus.COMPLETED
                    val isCurrent = timeline.currentStage == stage
                    val isUpcoming = !isCompleted && !isCurrent
                    
                    TimelineStageItem(
                        stage = stage,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        isUpcoming = isUpcoming,
                        stageData = stageData,
                        showConnector = index < TimelineStage.values().size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineStageItem(
    stage: TimelineStage,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isUpcoming: Boolean,
    stageData: com.demoapp.feature_jobs.presentation.models.TimelineStageData?,
    showConnector: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF4CAF50) // Green
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else stage.getIcon(),
                    fontSize = 12.sp,
                    color = if (isCompleted || isCurrent) Color.White else MaterialTheme.colorScheme.outline
                )
            }
            
            // Connector line
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Stage content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stage.getDisplayName(),
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCompleted -> Color(0xFF4CAF50)
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            
            if (stageData != null) {
                Text(
                    text = stageData.message ?: stage.getDescription(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = formatTimeAgo(stageData.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else if (isUpcoming) {
                Text(
                    text = stage.getDescription(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun formatTimeAgo(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
