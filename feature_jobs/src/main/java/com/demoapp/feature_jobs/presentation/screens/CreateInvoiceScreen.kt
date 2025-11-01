package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.TimelineStage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    jobId: String,
    navController: NavController
) {
    // Debug: Log the jobId received
    LaunchedEffect(jobId) {
        android.util.Log.d("CreateInvoiceScreen", "=== CREATE INVOICE SCREEN LOADED ===")
        android.util.Log.d("CreateInvoiceScreen", "Received jobId: '$jobId' (type: ${jobId::class.simpleName})")
        android.util.Log.d("CreateInvoiceScreen", "jobId.isNumeric check: ${jobId.toIntOrNull() != null}")
    }
    
    val context = LocalContext.current
    val repository = JobRepositorySingleton.instance
    val taskRepository = remember { TaskRepository.getInstance(context) }
    val jobs by repository.jobs.collectAsState()
    val job = jobs.find { it.id == jobId }
    val coroutineScope = rememberCoroutineScope()
    
    // Debug: Log job found
    LaunchedEffect(job, jobId) {
        android.util.Log.d("CreateInvoiceScreen", "Job lookup: found=${job != null}, jobId='$jobId'")
        if (job != null) {
            android.util.Log.d("CreateInvoiceScreen", "Job details: id=${job.id}, title=${job.title}, status=${job.status}")
        }
    }
    
    var amountPaid by remember { mutableStateOf("") }
    var photoProof by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    if (job == null) {
        // Job not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                    text = "Job not found",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
            )
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Create Invoice",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Job Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Job Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = job.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Job Type: ${job.jobType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Amount: KES ${String.format("%.0f", job.pay)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Amount Paid Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Amount Paid",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = { Text("Enter amount paid (USD)") },
                        placeholder = { Text("e.g., 25.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        )
                    )
                    
                    Text(
                        text = "Expected amount: $${String.format("%.0f", job.pay)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Photo Proof Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Photo Proof",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (photoProof == null) {
                        // Photo upload placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Simulate photo selection
                                    photoProof = "photo_proof_${System.currentTimeMillis()}.jpg"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Photo",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to add photo proof",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        // Photo preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Photo Added",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Photo proof added",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tap to change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Take a photo of the completed work or receipt as proof",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Submit Button
            Button(
                onClick = {
                    android.util.Log.d("CreateInvoiceScreen", "=== CREATE INVOICE BUTTON CLICKED ===")
                    android.util.Log.d("CreateInvoiceScreen", "jobId=$jobId, amountPaid=$amountPaid, photoProof=$photoProof")
                    
                    if (amountPaid.isNotBlank() && photoProof != null) {
                        android.util.Log.d("CreateInvoiceScreen", "Validation passed, proceeding with invoice creation")
                        isSubmitting = true
                        coroutineScope.launch {
                            // Check if jobId is numeric (backend job) or string (sample job)
                            val isBackendJob = jobId.toIntOrNull() != null
                            android.util.Log.d("CreateInvoiceScreen", "jobId check: isBackendJob=$isBackendJob, jobId='$jobId'")
                            
                            var invoiceId = "invoice_${System.currentTimeMillis()}"
                            
                            // Only call backend API if jobId is numeric (backend job)
                            if (isBackendJob) {
                                android.util.Log.d("CreateInvoiceScreen", "Creating invoice via backend API for taskId=$jobId")
                                android.util.Log.d("CreateInvoiceScreen", "Calling taskRepository.createInvoice($jobId)")
                                val result = taskRepository.createInvoice(jobId)
                                android.util.Log.d("CreateInvoiceScreen", "API call completed, result=${result.isSuccess}")
                                result.fold(
                                    onSuccess = { invoiceResponse ->
                                        invoiceId = invoiceResponse.data?.invoice_number ?: invoiceId
                                        android.util.Log.d("CreateInvoiceScreen", "Backend invoice created successfully: invoice_number=$invoiceId")
                                        
                                        // Update local job state
                                        updateJobAfterInvoiceCreation(invoiceId, repository, job, jobId, amountPaid)
                                        
                                        // Navigate after success
                                        kotlinx.coroutines.delay(500)
                                        isSubmitting = false
                                        navController.navigate("my_tasks/completed") {
                                            popUpTo("my_tasks") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    },
                                    onFailure = { error ->
                                        android.util.Log.e("CreateInvoiceScreen", "Backend invoice creation failed: ${error.message}")
                                        // Update local state even if backend call fails
                                        invoiceId = "invoice_${System.currentTimeMillis()}"
                                        updateJobAfterInvoiceCreation(invoiceId, repository, job, jobId, amountPaid)
                                        
                                        kotlinx.coroutines.delay(500)
                                        isSubmitting = false
                                        navController.navigate("my_tasks/completed") {
                                            popUpTo("my_tasks") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            } else {
                                // Sample job - handle locally
                                android.util.Log.d("CreateInvoiceScreen", "Sample job - creating invoice locally for jobId=$jobId")
                                updateJobAfterInvoiceCreation(invoiceId, repository, job, jobId, amountPaid)
                                
                                kotlinx.coroutines.delay(500)
                                isSubmitting = false
                                navController.navigate("my_tasks/completed") {
                                    popUpTo("my_tasks") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    } else {
                        // Validation error - show message
                        android.util.Log.w("CreateInvoiceScreen", "=== VALIDATION FAILED ===")
                        android.util.Log.w("CreateInvoiceScreen", "amountPaid.isBlank=${amountPaid.isBlank()}, photoProof is null=${photoProof == null}")
                        android.util.Log.w("CreateInvoiceScreen", "Button enabled check: enabled=${!isSubmitting && amountPaid.isNotBlank() && photoProof != null}")
                        isSubmitting = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && amountPaid.isNotBlank() && photoProof != null
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Invoice", fontWeight = FontWeight.Bold)
                }
            }
            
            // Validation message
            if (amountPaid.isBlank() || photoProof == null) {
                Text(
                    text = "Please fill in all required fields to create the invoice",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
        
        // Success Dialog (kept for potential future use)
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Invoice Created!",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(
                        text = "Invoice has been created successfully.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showSuccessDialog = false
                            navController.navigate("my_tasks/completed") {
                                popUpTo("my_tasks") { inclusive = false }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// Helper function to update job after invoice creation
private fun updateJobAfterInvoiceCreation(
    invoiceId: String,
    repository: com.demoapp.feature_jobs.data.JobRepository,
    job: com.demoapp.feature_jobs.presentation.models.JobData?,
    jobId: String,
    amountPaid: String
) {
    // Ensure job exists in repository first (create if it doesn't exist)
    val jobToUpdate = if (job != null) {
        job.copy(
            status = com.demoapp.feature_jobs.presentation.models.JobStatus.COMPLETED,
            currentTimelineStage = com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED,
            isCompleted = true,
            completedAt = java.util.Date(),
            invoiceCreated = true,
            invoiceId = invoiceId
        )
    } else {
        android.util.Log.w("CreateInvoiceScreen", "Job not found in repository for jobId: $jobId, creating new entry")
        // If job not found, create a completed job entry with minimal required fields
        com.demoapp.feature_jobs.presentation.models.JobData(
            id = jobId,
            title = "Completed Job",
            description = "",
            pay = 0.0,
            distance = 0.0,
            deadline = "",
            jobType = "unknown",
            status = com.demoapp.feature_jobs.presentation.models.JobStatus.COMPLETED,
            isCompleted = true,
            invoiceCreated = true,
            invoiceId = invoiceId,
            completedAt = java.util.Date(),
            currentTimelineStage = com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED,
            clientId = "unknown_client",
            workerId = "unknown_worker"
        )
    }
    
    // Update local repository - this ensures job is added/updated
    repository.updateJob(jobToUpdate)
    android.util.Log.d("CreateInvoiceScreen", "Updated/created job ${jobToUpdate.id} with status=${jobToUpdate.status}, invoiceCreated=${jobToUpdate.invoiceCreated}, isCompleted=${jobToUpdate.isCompleted}")
    
    // Also call createInvoiceForJob to ensure it's marked as having invoice
    val invoiceCreated = repository.createInvoiceForJob(jobId, invoiceId)
    android.util.Log.d("CreateInvoiceScreen", "Invoice created in repository: $invoiceCreated for jobId: $jobId")
    
    // Create notifications for job poster
    val notificationRepository = com.demoapp.feature_jobs.data.NotificationRepository.getInstance()
    
    // 1. Task completion notification
    notificationRepository.addNotification(
        com.demoapp.feature_jobs.presentation.models.NotificationData(
            title = "Task Completed",
            message = "Your task \"${job?.title ?: "Job"}\" has been completed. Invoice #$invoiceId has been created.",
            type = com.demoapp.feature_jobs.presentation.models.NotificationType.JOB_COMPLETED,
            recipientId = job?.clientId ?: "unknown_client",
            senderId = job?.workerId ?: "unknown_worker",
            jobId = jobId,
            actionRequired = true,
            actionType = com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_COMPLETED_JOBS,
            isRead = false
        )
    )
    
    // 2. Invoice notification
    notificationRepository.createInvoiceNotification(
        jobId = jobId,
        jobTitle = job?.title ?: "Job",
        clientId = job?.clientId ?: "unknown_client",
        workerId = job?.workerId ?: "unknown_worker",
        workerName = "Worker", // This should come from worker data
        invoiceId = invoiceId,
        amountPaid = amountPaid
    )
}