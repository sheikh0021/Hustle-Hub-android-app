package com.demoapp.feature_jobs.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demoapp.feature_jobs.data.JobTimelineRepository
import com.demoapp.feature_jobs.presentation.models.TimelineStage

@Composable
fun WorkerActionButtons(
    jobId: String,
    workerId: String,
    workerName: String,
    currentStage: TimelineStage?,
    jobTitle: String = "Job",
    onStageUpdated: (TimelineStage) -> Unit = {},
    onCreateInvoice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timelineRepository = JobTimelineRepository.getInstance()
    var showCompletionDialog by remember { mutableStateOf(false) }
    var showJobCompletionDialog by remember { mutableStateOf(false) }
    
    // Debug logging
    LaunchedEffect(jobId, currentStage) {
        android.util.Log.d("WorkerActionButtons", "Rendering buttons - jobId=$jobId, currentStage=$currentStage, workerId=$workerId")
    }
    
    // Always show action buttons for workers
    // This allows workers to update status and see their progress
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Update Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Show all status buttons so worker can update to any status at any time
                // On The Way button - always show
                StatusButton(
                    label = "🚗 On The Way",
                    isActive = currentStage == TimelineStage.WORKER_ON_THE_WAY,
                    onClick = {
                        android.util.Log.d("WorkerActionButtons", "On The Way clicked for jobId=$jobId")
                        timelineRepository.updateTimelineStage(
                            jobId = jobId,
                            stage = TimelineStage.WORKER_ON_THE_WAY,
                            updatedBy = workerId,
                            updatedByName = workerName,
                            message = "I'm on my way to the location"
                        )
                        onStageUpdated(TimelineStage.WORKER_ON_THE_WAY)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Started Working button - always show
                StatusButton(
                    label = "🔨 Started Working",
                    isActive = currentStage == TimelineStage.WORKER_STARTED_JOB,
                    onClick = {
                        android.util.Log.d("WorkerActionButtons", "Started Working clicked for jobId=$jobId")
                        timelineRepository.updateTimelineStage(
                            jobId = jobId,
                            stage = TimelineStage.WORKER_STARTED_JOB,
                            updatedBy = workerId,
                            updatedByName = workerName,
                            message = "I've started working on the job"
                        )
                        onStageUpdated(TimelineStage.WORKER_STARTED_JOB)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Complete button - always show
                StatusButton(
                    label = "🎉 Complete",
                    isActive = currentStage == TimelineStage.JOB_COMPLETED,
                    onClick = { 
                        android.util.Log.d("WorkerActionButtons", "Complete clicked for jobId=$jobId")
                        showJobCompletionDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF4CAF50),
                    textColor = Color.White
                )
                
                // Create Invoice button - show after completion
                if (currentStage == TimelineStage.JOB_COMPLETED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusButton(
                        label = "💰 Create Invoice",
                        isActive = false,
                        onClick = {
                            android.util.Log.d("WorkerActionButtons", "Create Invoice clicked for jobId=$jobId")
                            onCreateInvoice()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    
    // Job completion dialog
    if (showJobCompletionDialog) {
        JobCompletionDialog(
            jobTitle = jobTitle,
            onDismiss = { showJobCompletionDialog = false },
            onCreateInvoice = {
                timelineRepository.markJobCompleted(
                    jobId = jobId,
                    completedBy = workerId,
                    completedByName = workerName,
                    completionMessage = "Job completed successfully!"
                )
                onStageUpdated(TimelineStage.JOB_COMPLETED)
                onCreateInvoice()
            },
            onClose = {
                timelineRepository.markJobCompleted(
                    jobId = jobId,
                    completedBy = workerId,
                    completedByName = workerName,
                    completionMessage = "Job completed successfully!"
                )
                onStageUpdated(TimelineStage.JOB_COMPLETED)
            }
        )
    }
}

@Composable
private fun StatusButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    // Update time when button becomes active
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(isActive) {
        if (isActive) {
            currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
        }
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) {
                backgroundColor.copy(alpha = 0.8f)
            } else {
                backgroundColor
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            if (isActive) {
                Text(
                    text = "✓ $currentTime",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

