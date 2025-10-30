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
import com.demoapp.feature_jobs.data.JobRepository
import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import com.demoapp.feature_jobs.presentation.components.AvailabilityStatusComponent
import com.demoapp.feature_jobs.presentation.components.AvailabilityStatus
import java.util.Date

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
    val jobRepository = JobRepository()
    
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
                    text = "Job Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = jobTitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Job ID: $jobId",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "This shows your current status to clients",
                        fontSize = 12.sp,
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
                if (applicationMessage.isNotBlank() && proposedPrice != null) {
                    isSubmitting = true
                    coroutineScope.launch {
                        val result = taskRepository.applyForTask(jobId, proposedPrice, applicationMessage)
                        result.fold(
                            onSuccess = {
                                showSuccessDialog = true
                            },
                            onFailure = {
                                // Optionally show snackbar/toast in real app
                            }
                        )
                        isSubmitting = false
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
            onDismissRequest = { showSuccessDialog = false },
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
}
