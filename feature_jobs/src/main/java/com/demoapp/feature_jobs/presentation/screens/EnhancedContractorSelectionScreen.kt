package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.demoapp.feature_jobs.data.WorkflowManager
import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedContractorSelectionScreen(
    jobId: String,
    jobTitle: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedContractor by remember { mutableStateOf<String?>(null) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val workflowManager = WorkflowManager.getInstance()
    
    // Sample applications for demonstration
    val sampleApplications = remember {
        listOf(
            JobApplication(
                id = "app_1",
                jobId = jobId,
                workerId = "worker_1",
                workerName = "John Mwangi",
                workerPhone = "+254712345678",
                workerRating = 4.8f,
                workerCompletedTasks = 45,
                applicationMessage = "I have 3 years experience in delivery services. I'm available immediately and have a reliable motorcycle.",
                appliedAt = Date(),
                status = ApplicationStatus.PENDING
            ),
            JobApplication(
                id = "app_2",
                jobId = jobId,
                workerId = "worker_2",
                workerName = "Sarah Wanjiku",
                workerPhone = "+254723456789",
                workerRating = 4.9f,
                workerCompletedTasks = 78,
                applicationMessage = "I specialize in shopping tasks and know the area well. Can complete this efficiently.",
                appliedAt = Date(),
                status = ApplicationStatus.PENDING
            ),
            JobApplication(
                id = "app_3",
                jobId = jobId,
                workerId = "worker_3",
                workerName = "Peter Kimani",
                workerPhone = "+254734567890",
                workerRating = 4.6f,
                workerCompletedTasks = 32,
                applicationMessage = "Available for immediate start. Have all necessary equipment and transportation.",
                appliedAt = Date(),
                status = ApplicationStatus.PENDING
            ),
            JobApplication(
                id = "app_4",
                jobId = jobId,
                workerId = "worker_4",
                workerName = "Grace Akinyi",
                workerPhone = "+254745678901",
                workerRating = 4.7f,
                workerCompletedTasks = 56,
                applicationMessage = "Experienced in this type of work. Can provide references if needed.",
                appliedAt = Date(),
                status = ApplicationStatus.PENDING
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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
                text = "Select Contractor",
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Job ID: $jobId",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Applications Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Applications Received",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${sampleApplications.size} contractors have applied for this job",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Average Rating: ${String.format("%.1f", sampleApplications.map { it.workerRating }.average())}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "Total Experience: ${sampleApplications.sumOf { it.workerCompletedTasks }} tasks",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Contractors List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleApplications) { application: JobApplication ->
                ContractorApplicationCard(
                    application = application,
                    isSelected = selectedContractor == application.workerId,
                    onSelect = { selectedContractor = application.workerId },
                    onViewProfile = {
                        // TODO: Navigate to contractor profile
                    }
                )
            }
        }
        
        // Selection Actions
        if (selectedContractor != null) {
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
                        text = "Selected Contractor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val selectedApp = sampleApplications.find { it.workerId == selectedContractor }
                    selectedApp?.let { app ->
                        Text(
                            text = app.workerName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Rating: ${app.workerRating} • ${app.workerCompletedTasks} completed tasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = { showSelectionDialog = true },
                modifier = Modifier.weight(1f),
                enabled = selectedContractor != null && !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isProcessing) "Processing..." else "Select Contractor"
                )
            }
        }
    }
    
    // Selection Confirmation Dialog
    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = {
                Text(
                    text = "Confirm Selection",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val selectedApp = sampleApplications.find { it.workerId == selectedContractor }
                Column {
                    Text("You are about to select:")
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedApp?.let { app ->
                        Text(
                            text = "• ${app.workerName}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("• Rating: ${app.workerRating}")
                        Text("• Experience: ${app.workerCompletedTasks} tasks")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This will notify the contractor and begin the workflow.")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isProcessing = true
                        showSelectionDialog = false
                        
                        // Process contractor selection through workflow
                        selectedContractor?.let { workerId ->
                            val updatedJob = workflowManager.processContractSelected(jobId, workerId)
                            if (updatedJob != null) {
                                showSuccessDialog = true
                            }
                        }
                        isProcessing = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSelectionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "Contractor Selected!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("The contractor has been selected and notified. The workflow will now proceed to the execution phase.")
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

@Composable
private fun ContractorApplicationCard(
    application: JobApplication,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Contractor Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = application.workerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = application.workerPhone,
                        fontSize = 12.sp,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rating and Experience
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${application.workerCompletedTasks} tasks",
                        fontSize = 14.sp,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Application Message
            Text(
                text = application.applicationMessage ?: "",
                fontSize = 14.sp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                    else MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Profile")
                }
                
                Button(
                    onClick = onSelect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isSelected) "Selected" else "Select")
                }
            }
        }
    }
}
