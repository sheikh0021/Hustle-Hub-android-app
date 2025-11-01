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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.JobApplicationRepository
import com.demoapp.feature_jobs.data.NotificationRepository
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import com.demoapp.feature_jobs.presentation.components.AvailabilityStatusComponent
import com.demoapp.feature_jobs.presentation.components.AvailabilityStatus
import java.util.Date
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorApplicationScreen(
    jobId: String,
    jobTitle: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val taskRepository = remember { TaskRepository.getInstance(context) }
    var applicationMessage by remember { mutableStateOf("") }
    var relevantSkills by remember { mutableStateOf("") }
    var proposedPriceInput by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var expectedCompletionTime by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Check if contractor has a profile (in real app, this would check user session)
    var hasContractorProfile by remember { mutableStateOf(false) }
    var contractorName by remember { mutableStateOf("") }
    var contractorPhone by remember { mutableStateOf("") }
    
    // Determine if this is a delivery task
    val isDeliveryTask = jobTitle.contains("delivery", ignoreCase = true) || 
                        jobTitle.contains("package", ignoreCase = true) ||
                        jobTitle.contains("deliver", ignoreCase = true)
    
    // Availability dropdown options
    val availabilityOptions = listOf(
        "Available immediately",
        "Available in 2 hours",
        "Available in 4 hours",
        "Available tomorrow",
        "Available this weekend"
    )
    var expanded by remember { mutableStateOf(false) }
    
    // Mock contractor availability status (in real app, this would come from user session/profile)
    val contractorAvailability = remember { AvailabilityStatus.AVAILABLE }
    
    val applicationRepository = JobApplicationRepository.getInstance()
    val notificationRepository = NotificationRepository.getInstance()
    val jobRepository = JobRepositorySingleton.instance
    val jobs by jobRepository.jobs.collectAsState()
    
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
                text = "Apply for Job",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
                    text = "Job Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = jobTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Job ID: $jobId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isDeliveryTask) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🚚 Delivery Task - Skills field hidden",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Contractor Availability Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Column {
                    Text(
                        text = "Your Availability Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "This shows your current status to clients",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                AvailabilityStatusComponent(
                    status = contractorAvailability,
                    showLabel = true,
                    size = com.demoapp.feature_jobs.presentation.components.AvailabilitySize.MEDIUM
                )
            }
        }
        
        // Application Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Application Form",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cover Letter/Application Message
                OutlinedTextField(
                    value = applicationMessage,
                    onValueChange = { applicationMessage = it },
                    label = { Text("Cover Letter / Application Message") },
                    placeholder = { Text("Tell the client why you're the best fit for this job...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Relevant Skills (only show for non-delivery tasks)
                if (!isDeliveryTask) {
                    OutlinedTextField(
                        value = relevantSkills,
                        onValueChange = { relevantSkills = it },
                        label = { Text("Relevant Skills") },
                        placeholder = { Text("e.g., Shopping, Customer Service, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Proposed Price
                OutlinedTextField(
                    value = proposedPriceInput,
                    onValueChange = { proposedPriceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Proposed Price (KES)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Availability Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = availability,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Availability") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availabilityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    availability = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Expected Completion Time
                OutlinedTextField(
                    value = expectedCompletionTime,
                    onValueChange = { expectedCompletionTime = it },
                    label = { Text("Expected Completion Time") },
                    placeholder = { Text("e.g., 2 hours, Same day, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Additional Notes
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("Additional Notes (Optional)") },
                    placeholder = { Text("Any additional information you'd like to share...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        // Submit Button
        Button(
            onClick = {
                val proposedPrice = proposedPriceInput.toDoubleOrNull()
                if (proposedPrice == null) {
                    errorMessage = "Please enter a valid proposed price"
                    showErrorDialog = true
                    return@Button
                }
                if (applicationMessage.isBlank()) {
                    errorMessage = "Please enter an application message"
                    showErrorDialog = true
                    return@Button
                }
                
                isSubmitting = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        // Check if jobId is numeric (backend job) or string (sample job)
                        val isBackendJob = jobId.toIntOrNull() != null && jobId.toIntOrNull()!! > 0
                        
                        if (isBackendJob) {
                            // For backend jobs, call the API
                            val result = taskRepository.applyForTask(jobId, proposedPrice, applicationMessage)
                            result.fold(
                                onSuccess = { response ->
                                    android.util.Log.d("ContractorApplicationScreen", "Application submitted successfully: ${response.message}")
                                    
                                    // Notify the job poster locally
                                    val job = jobs.find { it.id == jobId }
                                    val clientId = job?.clientId ?: "client_unknown"
                                    
                                    android.util.Log.d("ContractorApplicationScreen", "Creating notification for clientId: $clientId, jobId: $jobId")
                                    
                                    // Create notification for the job poster
                                    notificationRepository.createJobApplicationNotification(
                                        jobId = jobId,
                                        jobTitle = jobTitle,
                                        clientId = clientId,
                                        workerId = "worker_current_user", // Replace with actual current worker id from session when available
                                        workerName = "Current Worker" // Replace with actual worker name
                                    )
                                    
                                    android.util.Log.d("ContractorApplicationScreen", "Notification created successfully")
                                    
                                    isSubmitting = false
                                    showSuccessDialog = true
                                },
                                onFailure = { exception ->
                                    android.util.Log.e("ContractorApplicationScreen", "Application submission failed", exception)
                                    // Extract a cleaner error message from the exception
                                    val errorMsg = exception.message ?: "Failed to submit application"
                                    // Check for authentication errors and redirect
                                    if (errorMsg.contains("Authentication", ignoreCase = true) || 
                                        errorMsg.contains("token", ignoreCase = true) ||
                                        errorMsg.contains("token not found", ignoreCase = true) ||
                                        errorMsg.contains("session expired", ignoreCase = true)) {
                                        // Redirect to login screen
                                        navController.navigate("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                        errorMessage = "Your session has expired. Redirecting to login..."
                                    } else {
                                        errorMessage = when {
                                            errorMsg.contains("404") || errorMsg.contains("Page not found") -> 
                                                "This job is no longer available. Please try applying for a different job."
                                            errorMsg.contains("Task is not available for applications", ignoreCase = true) ||
                                            errorMsg.contains("not available for applications", ignoreCase = true) ->
                                                "This task is not currently accepting applications.\n\nPossible reasons:\n• Payment verification pending\n• Task already assigned to another worker\n• Task has been completed or cancelled\n\nPlease try applying for a different task or check back later."
                                            errorMsg.contains("400") && errorMsg.contains("not available") ->
                                                "This task is not currently accepting applications.\n\nPossible reasons:\n• Payment verification pending\n• Task already assigned to another worker\n• Task has been completed or cancelled\n\nPlease try applying for a different task or check back later."
                                            errorMsg.length > 200 -> 
                                                "Failed to submit application. Please check your internet connection and try again."
                                            else -> errorMsg
                                        }
                                    }
                                    isSubmitting = false
                                    showErrorDialog = true
                                }
                            )
                        } else {
                            // For sample jobs, just apply locally without API call
                            android.util.Log.d("ContractorApplicationScreen", "Applying for sample job locally: $jobId")
                            
                            // Find the job and apply locally
                            val job = jobs.find { it.id == jobId }
                            if (job != null) {
                                // Create local application with proposed price in the message
                                val messageWithPrice = "Proposed Price: KES $proposedPrice\n\n${applicationMessage}"
                                val application = JobApplication(
                                    id = "app_${System.currentTimeMillis()}",
                                    jobId = jobId,
                                    workerId = "worker_current_user",
                                    workerName = "Current Worker",
                                    workerPhone = "+254700000000", // Default phone, replace with actual user phone
                                    applicationMessage = messageWithPrice,
                                    appliedAt = Date(),
                                    status = ApplicationStatus.PENDING
                                )
                                applicationRepository.submitApplication(application)
                                
                                // Notify the job poster locally
                                val clientId = job.clientId ?: "client_unknown"
                                notificationRepository.createJobApplicationNotification(
                                    jobId = jobId,
                                    jobTitle = jobTitle,
                                    clientId = clientId,
                                    workerId = "worker_current_user",
                                    workerName = "Current Worker"
                                )
                                
                                android.util.Log.d("ContractorApplicationScreen", "Sample job application submitted locally")
                                isSubmitting = false
                                showSuccessDialog = true
                            } else {
                                android.util.Log.e("ContractorApplicationScreen", "Sample job not found: $jobId")
                                errorMessage = "Job not found. Please try again."
                                isSubmitting = false
                                showErrorDialog = true
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ContractorApplicationScreen", "Unexpected error submitting application", e)
                        errorMessage = "An unexpected error occurred. Please try again."
                        isSubmitting = false
                        showErrorDialog = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = applicationMessage.isNotBlank() && proposedPriceInput.toDoubleOrNull() != null && !isSubmitting,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isSubmitting) "Submitting..." else "Submit Application",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Cancel Button
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = "Application Submitted!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your application has been submitted successfully. The client will review your application and get back to you.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Text(
                    text = "Application Failed",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "An unknown error occurred",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
