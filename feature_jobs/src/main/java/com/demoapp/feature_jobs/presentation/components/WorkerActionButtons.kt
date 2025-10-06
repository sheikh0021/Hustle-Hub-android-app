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
    
    // Only show action buttons for workers and when job is in progress
    if (currentStage != TimelineStage.JOB_COMPLETED) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Update Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Accept Job button (when no stage is set)
                    if (currentStage == null) {
                        Button(
                            onClick = {
                                timelineRepository.updateTimelineStage(
                                    jobId = jobId,
                                    stage = TimelineStage.JOB_ACCEPTED,
                                    updatedBy = workerId,
                                    updatedByName = workerName,
                                    message = "I have accepted this job"
                                )
                                onStageUpdated(TimelineStage.JOB_ACCEPTED)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✅ Accept Job",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // On The Way button
                    if (currentStage == TimelineStage.JOB_ACCEPTED) {
                        Button(
                            onClick = {
                                timelineRepository.updateTimelineStage(
                                    jobId = jobId,
                                    stage = TimelineStage.WORKER_ON_THE_WAY,
                                    updatedBy = workerId,
                                    updatedByName = workerName,
                                    message = "I'm on my way to the location"
                                )
                                onStageUpdated(TimelineStage.WORKER_ON_THE_WAY)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🚗 On The Way",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Started Working button
                    if (currentStage == TimelineStage.WORKER_ON_THE_WAY) {
                        Button(
                            onClick = {
                                timelineRepository.updateTimelineStage(
                                    jobId = jobId,
                                    stage = TimelineStage.WORKER_STARTED_JOB,
                                    updatedBy = workerId,
                                    updatedByName = workerName,
                                    message = "I've started working on the job"
                                )
                                onStageUpdated(TimelineStage.WORKER_STARTED_JOB)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔨 Started",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Complete Job button
                    if (currentStage == TimelineStage.WORKER_STARTED_JOB) {
                        Button(
                            onClick = { showJobCompletionDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🎉 Complete",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
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

