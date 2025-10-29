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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.WorkflowManager
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.WorkflowStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceUploadScreen(
    jobId: String,
    job: JobData,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var evidenceMessage by remember { mutableStateOf("") }
    var uploadedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadedVideos by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadedDocuments by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val workflowManager = WorkflowManager.getInstance()
    
    // Determine job type for specific evidence requirements
    val isDeliveryJob = job.jobType.contains("delivery", ignoreCase = true)
    val isShoppingJob = job.jobType.contains("shopping", ignoreCase = true)
    val isSurveyJob = job.jobType.contains("survey", ignoreCase = true)
    
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
                text = "Upload Evidence",
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
                    text = "Job Completion Evidence",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = job.title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Job Type: ${job.jobType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Evidence Requirements Card
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
                    text = "Evidence Requirements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    isDeliveryJob -> {
                        Text(
                            text = "• Photos of delivered package at destination\n• Photo of recipient (if applicable)\n• Delivery confirmation message\n• Any delivery notes or issues",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    isShoppingJob -> {
                        Text(
                            text = "• Photos of purchased items\n• Receipt photos\n• Delivery confirmation (if applicable)\n• Shopping notes or special instructions",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    isSurveyJob -> {
                        Text(
                            text = "• Photos of survey location\n• Video recordings (if applicable)\n• Survey results or findings\n• Documents or reports\n• Any supporting evidence",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    else -> {
                        Text(
                            text = "• Photos of completed work\n• Any relevant documentation\n• Completion notes or messages\n• Proof of task completion",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Evidence Upload Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Upload Evidence",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Evidence Message
                OutlinedTextField(
                    value = evidenceMessage,
                    onValueChange = { evidenceMessage = it },
                    label = { Text("Evidence Description *") },
                    placeholder = { Text("Describe what you completed and any important details...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Photo Upload Section
                Text(
                    text = "Photos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { /* TODO: Implement photo picker */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Photos")
                }
                
                if (uploadedPhotos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Uploaded: ${uploadedPhotos.size} photos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Video Upload Section (for surveys)
                if (isSurveyJob) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { /* TODO: Implement video picker */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Videos")
                    }
                    
                    if (uploadedVideos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Uploaded: ${uploadedVideos.size} videos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Document Upload Section (for surveys)
                if (isSurveyJob) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Documents",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { /* TODO: Implement document picker */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Documents")
                    }
                    
                    if (uploadedDocuments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Uploaded: ${uploadedDocuments.size} documents",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Submit Evidence Button
        Button(
            onClick = {
                if (evidenceMessage.isNotBlank()) {
                    isSubmitting = true
                    
                    // Process evidence upload
                    val updatedJob = workflowManager.processEvidenceUploaded(
                        jobId = jobId,
                        evidenceFiles = emptyList(), // TODO: Implement file upload
                        evidenceMessages = listOf(evidenceMessage),
                        evidencePhotos = uploadedPhotos,
                        evidenceVideos = uploadedVideos,
                        evidenceDocuments = uploadedDocuments
                    )
                    
                    if (updatedJob != null) {
                        showSuccessDialog = true
                    }
                    isSubmitting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = evidenceMessage.isNotBlank() && !isSubmitting,
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
                text = if (isSubmitting) "Uploading Evidence..." else "Submit Evidence",
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
                    text = "Evidence Uploaded!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your evidence has been uploaded successfully. The client will review your submission.")
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
