package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.data.JobCancellationResult
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.CancellationReasonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCancellationScreen(
    navController: NavController,
    jobId: String,
    repository: OfflineJobRepository
) {
    var selectedReasonType by remember { mutableStateOf<CancellationReasonType?>(null) }
    var customReason by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var cancellationResult by remember { mutableStateOf<JobCancellationResult?>(null) }

    val job = repository.getJobById(jobId)

    if (job == null) {
        // Job not found
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Job not found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Cancel Job",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Job information
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Job Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Title: ${job.title}",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Pay: ${job.pay}",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Status: ${job.status}",
                            fontSize = 14.sp
                        )
                        if (job.workerAccepted) {
                            Text(
                                text = "⚠️ Worker has already accepted this job",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Penalty information
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancellation Penalty",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val basePenalty = job.pay * (OfflineJobRepository.CANCELLATION_PENALTY_PERCENTAGE / 100.0)
                        val finalPenalty = if (job.workerAccepted) basePenalty * 1.5 else basePenalty
                        
                        Text(
                            text = "Base penalty: ${String.format("%.2f", basePenalty)} (${OfflineJobRepository.CANCELLATION_PENALTY_PERCENTAGE}%)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        if (job.workerAccepted) {
                            Text(
                                text = "Worker accepted penalty: +50% = ${String.format("%.2f", finalPenalty)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "Total penalty: ${String.format("%.2f", finalPenalty)}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Reason selection
            item {
                Text(
                    text = "Select cancellation reason:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reason options
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CancellationReasonType.values().forEach { reasonType ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedReasonType == reasonType) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = { selectedReasonType = reasonType }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReasonType == reasonType,
                                    onClick = { selectedReasonType = reasonType }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = getReasonTitle(reasonType),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = getReasonDescription(reasonType),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom reason input (for "Other")
            if (selectedReasonType == CancellationReasonType.OTHER) {
                item {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Please explain") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Cancel job button
            item {
                Button(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = selectedReasonType != null && 
                             (selectedReasonType != CancellationReasonType.OTHER || customReason.isNotBlank())
                ) {
                    Text(
                        text = "Cancel Job",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Confirmation dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirm Cancellation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel this job?",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reason: ${getReasonTitle(selectedReasonType!!)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedReasonType == CancellationReasonType.OTHER) {
                        Text(
                            text = "Details: $customReason",
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = if (selectedReasonType == CancellationReasonType.OTHER) {
                            customReason
                        } else {
                            getReasonTitle(selectedReasonType!!)
                        }
                        
                        cancellationResult = repository.cancelJob(jobId, reason, selectedReasonType!!)
                        showConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Cancel Job")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("No, Keep Job")
                }
            }
        )
    }

    // Show cancellation result
    cancellationResult?.let { result ->
        when (result) {
            is JobCancellationResult.Success -> {
                LaunchedEffect(result) {
                    // Show success message and navigate back
                    navController.popBackStack()
                }
            }
            is JobCancellationResult.Failure -> {
                AlertDialog(
                    onDismissRequest = { cancellationResult = null },
                    title = { Text("Cancellation Failed") },
                    text = { Text(result.error) },
                    confirmButton = {
                        Button(onClick = { cancellationResult = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

private fun getReasonTitle(reasonType: CancellationReasonType): String {
    return when (reasonType) {
        CancellationReasonType.TRANSPORT_ISSUES -> "Transport Issues"
        CancellationReasonType.LOW_PAYMENT -> "Low Payment"
        CancellationReasonType.PERSONAL_CIRCUMSTANCES -> "Personal Circumstances"
        CancellationReasonType.OTHER -> "Other"
    }
}

private fun getReasonDescription(reasonType: CancellationReasonType): String {
    return when (reasonType) {
        CancellationReasonType.TRANSPORT_ISSUES -> "Unable to complete due to transportation problems"
        CancellationReasonType.LOW_PAYMENT -> "Payment amount is too low for the work required"
        CancellationReasonType.PERSONAL_CIRCUMSTANCES -> "Personal or family emergency"
        CancellationReasonType.OTHER -> "Other reason not listed above"
    }
}
