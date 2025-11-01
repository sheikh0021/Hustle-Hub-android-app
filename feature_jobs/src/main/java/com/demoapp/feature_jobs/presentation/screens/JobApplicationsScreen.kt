package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.FirebaseChatRepository
import com.demoapp.feature_jobs.data.JobApplicationRepository
import com.demoapp.feature_jobs.data.NotificationRepository
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.components.AvailabilityStatusComponent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApplicationsScreen(
    jobId: String,
    jobTitle: String,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val taskRepository = remember { TaskRepository.getInstance(context) }
    val firebaseChatRepository = remember { FirebaseChatRepository.getInstance() }
    val applicationRepository = JobApplicationRepository.getInstance()
    val notificationRepository = NotificationRepository.getInstance()
    val jobRepository = JobRepositorySingleton.instance
    val jobs by jobRepository.jobs.collectAsState()
    
    val job = jobs.find { it.id == jobId }
    val applications by applicationRepository.applications.collectAsState()
    var backendCount by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(jobId) {
        coroutineScope.launch {
            val result = taskRepository.getTaskApplications(jobId)
            result.onSuccess { resp ->
                backendCount = resp.data?.applications?.size
            }.onFailure {
                backendCount = null
            }
        }
    }
    val jobApplications = applications.filter { it.jobId == jobId }
    
    var selectedApplication by remember { mutableStateOf<JobApplication?>(null) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Applications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Job Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Job Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Applications: ${backendCount ?: jobApplications.size}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (job != null) {
                        Text(
                            text = "Payment: KES ${job.pay}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Deadline: ${job.deadline}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (jobApplications.isEmpty()) {
                // No applications
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No applications yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Applications will appear here when contractors apply",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Applications List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobApplications) { application ->
                        ApplicationCard(
                            application = application,
                            onSelectClick = {
                                selectedApplication = application
                                showSelectionDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Selection Confirmation Dialog
    if (showSelectionDialog && selectedApplication != null) {
        AlertDialog(
            onDismissRequest = { 
                showSelectionDialog = false
                selectedApplication = null
            },
            title = { Text("Select Contractor") },
            text = { 
                Text("Are you sure you want to select ${selectedApplication?.workerName} for this job? This will notify the contractor and other applicants will be automatically rejected.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedApplication?.let { app ->
                            coroutineScope.launch {
                                try {
                                    // Check if both jobId and workerId are numeric (backend IDs)
                                    val isBackendJob = jobId.toIntOrNull() != null
                                    val isBackendWorker = app.workerId.toIntOrNull() != null
                                    
                                    var apiSuccess = false
                                    
                                    // Only call API if both are backend IDs (numeric)
                                    if (isBackendJob && isBackendWorker) {
                                        android.util.Log.d("JobApplicationsScreen", "Calling accept API: taskId=$jobId, userId=${app.workerId}")
                                        val result = taskRepository.acceptApplication(jobId, app.workerId)
                                        
                                        result.onSuccess { response ->
                                            android.util.Log.d("JobApplicationsScreen", "Accept API success: ${response.message}")
                                            apiSuccess = true
                                        }.onFailure { exception ->
                                            android.util.Log.e("JobApplicationsScreen", "Accept API failed: ${exception.message}")
                                            // Continue with local handling even if API fails
                                        }
                                    } else {
                                        android.util.Log.d("JobApplicationsScreen", "Sample job/worker - handling locally: jobId=$jobId, workerId=${app.workerId}")
                                        // For sample jobs/workers, skip API and handle locally
                                        apiSuccess = true // Consider it successful for local handling
                                    }
                                    
                                    // Always update local state and send notifications
                                    // (whether API succeeded or we're handling sample jobs)
                                    applicationRepository.updateApplicationStatus(app.id, ApplicationStatus.SELECTED)
                                    
                                    // Reject all other applications for this job
                                    jobApplications.forEach { otherApp ->
                                        if (otherApp.id != app.id && otherApp.status == ApplicationStatus.PENDING) {
                                            applicationRepository.updateApplicationStatus(otherApp.id, ApplicationStatus.REJECTED)
                                            // Send rejection notifications to other applicants
                                            notificationRepository.createJobRejectionNotification(
                                                jobId = jobId,
                                                jobTitle = jobTitle,
                                                workerId = otherApp.workerId
                                            )
                                        }
                                    }
                                    
                                    // Send selection notification to the selected worker
                                    notificationRepository.createJobSelectionNotification(
                                        jobId = jobId,
                                        jobTitle = jobTitle,
                                        workerId = app.workerId,
                                        workerName = app.workerName
                                    )
                                    
                                    // Update job with assigned worker
                                    jobRepository.assignWorkerToJob(jobId, app.workerId)
                                    
                                    // Ensure chat room has the assigned worker set for poster↔worker chat
                                    firebaseChatRepository.assignWorkerToChatRoom(jobId, app.workerId, app.workerName)
                                    
                                    showSelectionDialog = false
                                    selectedApplication = null
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    android.util.Log.e("JobApplicationsScreen", "Error selecting worker: ${e.message}", e)
                                    showSelectionDialog = false
                                    selectedApplication = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSelectionDialog = false
                        selectedApplication = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ApplicationCard(
    application: JobApplication,
    onSelectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with worker info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = application.workerName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.workerPhone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (application.status) {
                            ApplicationStatus.SELECTED -> MaterialTheme.colorScheme.primaryContainer
                            ApplicationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                            ApplicationStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                            ApplicationStatus.WITHDRAWN -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = application.status.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (application.status) {
                            ApplicationStatus.SELECTED -> MaterialTheme.colorScheme.onPrimaryContainer
                            ApplicationStatus.REJECTED -> MaterialTheme.colorScheme.onErrorContainer
                            ApplicationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            ApplicationStatus.WITHDRAWN -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rating and completed tasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${application.workerRating}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${application.workerCompletedTasks} tasks completed",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Application message
            if (!application.applicationMessage.isNullOrBlank()) {
                Text(
                    text = "Message:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = application.applicationMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Applied date
            Text(
                text = "Applied: ${SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(application.appliedAt)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action button
            if (application.status == ApplicationStatus.PENDING) {
                Button(
                    onClick = onSelectClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select This Contractor")
                }
            }
        }
    }
}
