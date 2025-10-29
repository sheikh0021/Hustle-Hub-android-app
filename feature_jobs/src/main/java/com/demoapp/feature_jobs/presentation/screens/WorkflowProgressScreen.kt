package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.WorkflowManager
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.WorkflowStep

@Composable
fun WorkflowProgressScreen(
    jobId: String,
    job: JobData,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val workflowManager = WorkflowManager.getInstance()
    val currentStep = job.workflowStep
    val progress = workflowManager.getWorkflowProgress(currentStep)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Workflow Progress",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Job Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = job.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Job ID: $jobId",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Current Status: ${getWorkflowStepDisplayName(currentStep)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Workflow Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$progress% Complete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Workflow Steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Workflow Steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // All workflow steps
                val allSteps = listOf(
                    WorkflowStep.REQUEST_POSTED to "Request Posted",
                    WorkflowStep.OFFERS_RECEIVED to "Offers Received",
                    WorkflowStep.CONTRACT_SELECTED to "Contract Selected",
                    WorkflowStep.EXECUTION_STARTED to "Execution Started",
                    WorkflowStep.EXECUTION_IN_PROGRESS to "Execution In Progress",
                    WorkflowStep.EVIDENCE_UPLOADED to "Evidence Uploaded",
                    WorkflowStep.COMPLETION_SUBMITTED to "Completion Submitted",
                    WorkflowStep.CLIENT_CONFIRMED to "Client Confirmed",
                    WorkflowStep.PAYMENT_PROCESSING to "Payment Processing",
                    WorkflowStep.PAYMENT_PROOF_UPLOADED to "Payment Proof Uploaded",
                    WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED to "Receipt Confirmed",
                    WorkflowStep.WORKFLOW_FINALIZED to "Workflow Finalized"
                )
                
                allSteps.forEachIndexed { index, (step, displayName) ->
                    WorkflowStepItem(
                        step = step,
                        displayName = displayName,
                        isCompleted = isStepCompleted(step, currentStep),
                        isCurrent = step == currentStep,
                        isUpcoming = isStepUpcoming(step, currentStep)
                    )
                    
                    if (index < allSteps.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Action Buttons based on current step
        when (currentStep) {
            WorkflowStep.EXECUTION_STARTED -> {
                Button(
                    onClick = { /* TODO: Navigate to execution in progress */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Working")
                }
            }
            WorkflowStep.EXECUTION_IN_PROGRESS -> {
                Button(
                    onClick = { 
                        navController.navigate("evidence_upload/$jobId")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload Evidence")
                }
            }
            WorkflowStep.EVIDENCE_UPLOADED -> {
                Button(
                    onClick = { /* TODO: Mark as completed */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mark as Completed")
                }
            }
            WorkflowStep.CLIENT_CONFIRMED -> {
                Button(
                    onClick = { /* TODO: Process payment */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Process Payment")
                }
            }
            WorkflowStep.PAYMENT_PROCESSING -> {
                Button(
                    onClick = { /* TODO: Upload payment proof */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload Payment Proof")
                }
            }
            WorkflowStep.PAYMENT_PROOF_UPLOADED -> {
                Button(
                    onClick = { /* TODO: Confirm receipt */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Receipt")
                }
            }
            else -> {
                // No action button for other steps
            }
        }
    }
}

@Composable
private fun WorkflowStepItem(
    step: WorkflowStep,
    displayName: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isUpcoming: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Icon
        Icon(
            imageVector = when {
                isCompleted -> Icons.Default.CheckCircle
                isCurrent -> Icons.Default.PlayArrow
                else -> Icons.Default.Add
            },
            contentDescription = null,
            tint = when {
                isCompleted -> Color(0xFF4CAF50) // Green
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            },
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Step Text
        Text(
            text = displayName,
            fontSize = 14.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCompleted -> Color(0xFF4CAF50) // Green
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        )
    }
}

private fun isStepCompleted(step: WorkflowStep, currentStep: WorkflowStep): Boolean {
    val stepOrder = listOf(
        WorkflowStep.REQUEST_POSTED,
        WorkflowStep.OFFERS_RECEIVED,
        WorkflowStep.CONTRACT_SELECTED,
        WorkflowStep.EXECUTION_STARTED,
        WorkflowStep.EXECUTION_IN_PROGRESS,
        WorkflowStep.EVIDENCE_UPLOADED,
        WorkflowStep.COMPLETION_SUBMITTED,
        WorkflowStep.CLIENT_CONFIRMED,
        WorkflowStep.PAYMENT_PROCESSING,
        WorkflowStep.PAYMENT_PROOF_UPLOADED,
        WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED,
        WorkflowStep.WORKFLOW_FINALIZED
    )
    
    val currentIndex = stepOrder.indexOf(currentStep)
    val stepIndex = stepOrder.indexOf(step)
    
    return stepIndex < currentIndex
}

private fun isStepUpcoming(step: WorkflowStep, currentStep: WorkflowStep): Boolean {
    val stepOrder = listOf(
        WorkflowStep.REQUEST_POSTED,
        WorkflowStep.OFFERS_RECEIVED,
        WorkflowStep.CONTRACT_SELECTED,
        WorkflowStep.EXECUTION_STARTED,
        WorkflowStep.EXECUTION_IN_PROGRESS,
        WorkflowStep.EVIDENCE_UPLOADED,
        WorkflowStep.COMPLETION_SUBMITTED,
        WorkflowStep.CLIENT_CONFIRMED,
        WorkflowStep.PAYMENT_PROCESSING,
        WorkflowStep.PAYMENT_PROOF_UPLOADED,
        WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED,
        WorkflowStep.WORKFLOW_FINALIZED
    )
    
    val currentIndex = stepOrder.indexOf(currentStep)
    val stepIndex = stepOrder.indexOf(step)
    
    return stepIndex > currentIndex
}

private fun getWorkflowStepDisplayName(step: WorkflowStep): String {
    return when (step) {
        WorkflowStep.REQUEST_POSTED -> "Request Posted"
        WorkflowStep.OFFERS_RECEIVED -> "Offers Received"
        WorkflowStep.CONTRACT_SELECTED -> "Contract Selected"
        WorkflowStep.EXECUTION_STARTED -> "Execution Started"
        WorkflowStep.EXECUTION_IN_PROGRESS -> "Execution In Progress"
        WorkflowStep.EVIDENCE_UPLOADED -> "Evidence Uploaded"
        WorkflowStep.COMPLETION_SUBMITTED -> "Completion Submitted"
        WorkflowStep.CLIENT_CONFIRMED -> "Client Confirmed"
        WorkflowStep.PAYMENT_PROCESSING -> "Payment Processing"
        WorkflowStep.PAYMENT_PROOF_UPLOADED -> "Payment Proof Uploaded"
        WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED -> "Receipt Confirmed"
        WorkflowStep.WORKFLOW_FINALIZED -> "Workflow Finalized"
    }
}
