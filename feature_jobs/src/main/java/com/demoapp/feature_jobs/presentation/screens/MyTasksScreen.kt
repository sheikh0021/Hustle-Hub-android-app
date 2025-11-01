package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.data.JobApplicationRepository
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import com.demoapp.feature_jobs.presentation.components.WithdrawalConfirmationDialog
import com.demoapp.feature_jobs.presentation.viewmodels.MyTasksViewModel

@Composable
fun MyTasksScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    workerId: String = "worker_1", // Default worker ID for demo
    initialTabIndex: Int = 0
) {
    val context = LocalContext.current
    val viewModel: MyTasksViewModel = viewModel { MyTasksViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val localJobRepository = JobRepositorySingleton.instance
    val localJobs by localJobRepository.jobs.collectAsState()
    
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    
    // Refresh tasks when screen comes into focus and merge with local completed jobs
    LaunchedEffect(Unit) {
        viewModel.refreshTasks()
    }
    
    // Also refresh when navigating to completed tab initially
    LaunchedEffect(initialTabIndex) {
        if (initialTabIndex == 2) { // Completed tab
            android.util.Log.d("MyTasksScreen", "Initial tab is Completed, refreshing immediately and after delay...")
            // Refresh immediately
            viewModel.refreshTasks()
            // Also refresh after a delay to ensure repository updates are reflected
            kotlinx.coroutines.delay(500)
            viewModel.refreshTasks()
        }
    }
    
    // Also refresh when local jobs change (in case invoice was just created)
    // Use a more reactive key that detects when jobs are marked as completed
    val completedJobsCount = localJobs.count { it.invoiceCreated == true || it.status == JobStatus.COMPLETED || it.isCompleted == true }
    LaunchedEffect(completedJobsCount) {
        // Refresh whenever completed jobs count changes (e.g., when invoice is created)
        android.util.Log.d("MyTasksScreen", "Completed jobs count changed: $completedJobsCount, refreshing...")
        viewModel.refreshTasks()
    }
    
    // Refresh when completed tab is selected to ensure latest data
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2) { // Completed tab is index 2
            android.util.Log.d("MyTasksScreen", "Completed tab selected, refreshing...")
            viewModel.refreshTasks()
        }
    }
    val tabs = listOf("Active", "Applied", "Completed", "Cancelled")
    var cancellationReason by remember { mutableStateOf("") }
    var showCancellationDialog by remember { mutableStateOf(false) }
    var jobToCancel by remember { mutableStateOf<JobData?>(null) }
    
    // Get job applications for the current worker
    val applicationRepository = JobApplicationRepository.getInstance()
    val allApplications by applicationRepository.applications.collectAsState()
    val myApplications = allApplications.filter { it.workerId == "worker_current_user" }
    
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Error will be shown via UI state
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Compact Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Refresh Button
            IconButton(
                onClick = { viewModel.refreshTasks() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compact Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tabs.size) { index ->
                val title = tabs[index]
                val isSelected = selectedTabIndex == index
                
                Box(
                    modifier = Modifier
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    // Underline for selected tab
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(1.dp)
                                )
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Loading State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading tasks...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Error State
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading tasks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refreshTasks() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> ActiveJobsTab(uiState.activeTasks, navController, onCancelJob = { job ->
                    jobToCancel = job
                    showCancellationDialog = true
                })
                1 -> AppliedJobsTab(
                    myApplications, 
                    navController, 
                    applicationRepository,
                    context = context,
                    viewModel = viewModel
                )
                2 -> CompletedJobsTab(uiState.completedTasks, navController)
                3 -> CancelledJobsTab(uiState.cancelledTasks, navController)
            }
        }
    }
    
    // Cancellation Dialog
    if (showCancellationDialog && jobToCancel != null) {
        JobCancellationDialog(
            onDismiss = { 
                showCancellationDialog = false
                jobToCancel = null
                cancellationReason = ""
            },
            onConfirm = { reason ->
                val job = jobToCancel
                if (job != null) {
                    coroutineScope.launch {
                        val result = viewModel.cancelTask(job.id)
                        // Close dialog regardless; errors could be surfaced via uiState.error if desired
                        showCancellationDialog = false
                        jobToCancel = null
                        cancellationReason = ""
                    }
                } else {
                    showCancellationDialog = false
                    jobToCancel = null
                    cancellationReason = ""
                }
            }
        )
    }
}

@Composable
private fun JobCancellationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Job") },
        text = {
            Column {
                Text("Please provide a reason for cancelling this job:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text("Cancel Job")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Keep Job")
            }
        }
    )
}

@Composable
private fun ActiveJobsTab(
    jobs: List<JobData>,
    navController: NavController,
    onCancelJob: (JobData) -> Unit
) {
    val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE || it.status == JobStatus.IN_PROGRESS }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (activeJobs.isEmpty()) {
            item {
                Text(
                    text = "No active jobs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        } else {
            items(activeJobs) { job ->
                JobCard(
                    job = job,
                    navController = navController,
                    onCancelJob = onCancelJob
                )
            }
        }
    }
}

@Composable
private fun AppliedJobsTab(
    applications: List<JobApplication>,
    navController: NavController,
    applicationRepository: JobApplicationRepository,
    context: android.content.Context,
    viewModel: MyTasksViewModel
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedApplicationToWithdraw by remember { mutableStateOf<JobApplication?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val taskRepository = remember { TaskRepository.getInstance(context) }
    val jobRepository = JobRepositorySingleton.instance
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (applications.isEmpty()) {
            item {
                Text(
                    text = "No applications yet",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        } else {
            items(applications) { application ->
                ApplicationCard(
                    application = application,
                    navController = navController,
                    onWithdraw = {
                        selectedApplicationToWithdraw = application
                        showWithdrawDialog = true
                    }
                )
            }
        }
    }
    
    // Withdrawal Confirmation Dialog
    selectedApplicationToWithdraw?.let { application ->
        WithdrawalConfirmationDialog(
            jobTitle = "Job Application", // This should come from job data
            onConfirm = {
                coroutineScope.launch {
                    isCancelling = true
                    try {
                        val jobId = application.jobId
                        val isBackendJob = jobId.toIntOrNull() != null
                        
                        // For backend jobs, call the cancel API
                        if (isBackendJob) {
                            val result = taskRepository.cancelTask(jobId)
                            if (result.isFailure) {
                                // If API call fails, still withdraw locally but show error
                                android.util.Log.e("AppliedJobsTab", "Failed to cancel task via API: ${result.exceptionOrNull()?.message}")
                                // Continue with local withdrawal
                            }
                        }
                        
                        // Withdraw the application locally
                        applicationRepository.withdrawApplication(jobId, application.workerId)
                        
                        // Update job status to CANCELLED in local repository
                        val job = jobRepository.getJobById(jobId)
                        if (job != null) {
                            jobRepository.updateJobStatus(jobId, JobStatus.CANCELLED)
                        } else {
                            // If job doesn't exist locally, try to get it from ViewModel and mark as cancelled
                            // For now, we'll refresh tasks which should pick up the cancelled status from backend
                        }
                        
                        // Refresh tasks to update the UI
                        viewModel.refreshTasks()
                        
                        showWithdrawDialog = false
                        selectedApplicationToWithdraw = null
                    } catch (e: Exception) {
                        android.util.Log.e("AppliedJobsTab", "Error withdrawing application: ${e.message}")
                    } finally {
                        isCancelling = false
                    }
                }
            },
            onDismiss = { 
                if (!isCancelling) {
                    showWithdrawDialog = false
                    selectedApplicationToWithdraw = null
                }
            }
        )
    }
}


@Composable
private fun CompletedJobsTab(
    jobs: List<JobData>,
    navController: NavController
) {
    // Show all jobs that are completed (by status or isCompleted flag) OR have invoices
    val completedJobs = jobs.filter { 
        val isCompleted = (it.invoiceCreated == true) || 
                         (it.status == JobStatus.COMPLETED || it.isCompleted == true)
        if (isCompleted) {
            android.util.Log.d("CompletedJobsTab", "Including job: id=${it.id}, title=${it.title}, status=${it.status}, invoiceCreated=${it.invoiceCreated}, isCompleted=${it.isCompleted}")
        }
        isCompleted
    }
    
    android.util.Log.d("CompletedJobsTab", "Displaying ${completedJobs.size} completed jobs out of ${jobs.size} total jobs")
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            if (completedJobs.isEmpty()) {
            item {
                Text(
                    text = "No completed jobs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        } else {
            items(completedJobs) { job ->
                JobCard(
                    job = job,
                    navController = navController,
                    onCancelJob = { }
                )
            }
        }
    }
}

@Composable
private fun CancelledJobsTab(
    jobs: List<JobData>,
    navController: NavController
) {
    val cancelledJobs = jobs.filter { it.status == JobStatus.CANCELLED }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (cancelledJobs.isEmpty()) {
            item {
                Text(
                    text = "No cancelled jobs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        } else {
            items(cancelledJobs) { job ->
                CancelledJobCard(
                    job = job,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun JobCard(
    job: JobData,
    navController: NavController,
    onCancelJob: (JobData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = job.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Location Row
            com.demoapp.feature_jobs.presentation.components.LocationPin(job = job)

            Spacer(modifier = Modifier.height(12.dp))

            // Status and Timeframe Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Status",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = when (job.status) {
                            JobStatus.ACTIVE -> "Active"
                            JobStatus.APPLIED -> "Applied"
                            JobStatus.IN_PROGRESS -> "In Progress"
                            JobStatus.COMPLETED -> "Completed"
                            JobStatus.CANCELLED -> "Cancelled"
                            JobStatus.DRAFT -> "Draft"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Time",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = when (job.status) {
                            JobStatus.ACTIVE -> "Last week"
                            JobStatus.APPLIED -> "Applied"
                            JobStatus.IN_PROGRESS -> "This week"
                            JobStatus.COMPLETED -> "Completed"
                            JobStatus.CANCELLED -> "Cancelled"
                            JobStatus.DRAFT -> "Draft"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Budget
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "KES ${String.format("%.0f", job.pay)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Details Button (Dark Green)
                Button(
                    onClick = { 
                        navController.navigate("job_details/${job.id}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Chat Button (Light Green)
                Button(
                    onClick = { 
                        navController.navigate("job_chat/${job.title}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Chat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Third Button - varies based on job status
                if (job.status == JobStatus.COMPLETED || job.isCompleted == true) {
                    if (job.invoiceCreated) {
                        // Show "Invoice Created" button (disabled)
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Invoice Created",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        // Show "Create Invoice" button
                        Button(
                            onClick = { 
                                navController.navigate("create_invoice/${job.title}")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Create Invoice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    // Cancel Button (Red) for non-completed jobs
                    Button(
                        onClick = { onCancelJob(job) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onError,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelledJobCard(
    job: JobData,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = job.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Job Type",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = job.jobType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "KES ${String.format("%.0f", job.pay)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${job.distance} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deadline: ${job.deadline}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            navController.navigate("job_details/${job.title}")
                        },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp
                        )
                    }
                    
                }
            }
            
            if (job.cancellationReason != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Cancellation Reason: ${job.cancellationReason}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: JobApplication,
    navController: NavController,
    onWithdraw: (() -> Unit)?
) {
    val (statusColor, statusText, statusIcon) = when (application.status) {
        ApplicationStatus.PENDING -> Triple(Color(0xFFFFC107), "Pending", Icons.Default.Warning)
        ApplicationStatus.SELECTED -> Triple(Color(0xFF4CAF50), "Selected", Icons.Default.CheckCircle)
        ApplicationStatus.REJECTED -> Triple(Color(0xFFF44336), "Rejected", Icons.Default.Close)
        ApplicationStatus.WITHDRAWN -> Triple(Color(0xFF9E9E9E), "Withdrawn", Icons.Default.Close)
        else -> Triple(MaterialTheme.colorScheme.primary, "Unknown", Icons.Default.Settings)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Job Application",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Applied: ${getTimeAgo(application.appliedAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Status Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusText,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            application.applicationMessage?.let {
                Text(
                    text = "Your Message: \"$it\"",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Withdraw button for pending applications
            if (onWithdraw != null && application.status == ApplicationStatus.PENDING) {
                Button(
                    onClick = onWithdraw,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Withdraw Application")
                }
            }
        }
    }
}

// Helper function for time ago calculation
private fun getTimeAgo(date: java.util.Date): String {
    val now = java.util.Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (1000 * 60)
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24

    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "$diffInMinutes minutes ago"
        diffInHours < 24 -> "$diffInHours hours ago"
        diffInDays < 7 -> "$diffInDays days ago"
        else -> "${diffInDays / 7} weeks ago"
    }
}
