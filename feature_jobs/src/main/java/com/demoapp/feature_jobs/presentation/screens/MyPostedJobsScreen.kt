package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.data.TaskRepository
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyPostedJobsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    val localRepository = JobRepositorySingleton.instance
    val localJobs by localRepository.jobs.collectAsState()
    
    val backendJobsState = remember { mutableStateOf<List<JobData>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(true) }
    val errorMessageState = remember { mutableStateOf<String?>(null) }
    
    val backendJobs by backendJobsState
    var isLoading by isLoadingState
    var errorMessage by errorMessageState
    
    // Fetch jobs from backend on screen load
    LaunchedEffect(Unit) {
        isLoading = true
        // Fetch tasks from backend - tasks/list should return tasks posted by current user when authenticated
        val result = taskRepository.getTaskList(status = null, category = null, page = 1, pageSize = 50)
        result.fold(
            onSuccess = { response ->
                val tasks = response.data?.tasks ?: emptyList()
                // Convert backend TaskData to JobData for display
                val jobs = tasks.map { task ->
                    JobData(
                        id = task.id.toString(),
                        title = task.title ?: "Untitled Task",
                        description = task.task_description ?: "",
                        pay = task.budget_kes?.toDoubleOrNull() ?: 0.0,
                        distance = task.distance_km ?: 0.0,
                        deadline = task.due_date ?: "",
                        jobType = task.category ?: "",
                        clientId = "client_mary_johnson", // Backend should have user_id but we use this for now
                        status = when (task.status) {
                            "active" -> JobStatus.ACTIVE
                            "in_progress" -> JobStatus.IN_PROGRESS
                            "completed" -> JobStatus.COMPLETED
                            "cancelled" -> JobStatus.CANCELLED
                            else -> JobStatus.ACTIVE
                        },
                        deliveryAddress = task.delivery_location
                    )
                }
                backendJobsState.value = jobs
                isLoading = false
                android.util.Log.d("MyPostedJobsScreen", "Fetched ${jobs.size} jobs from backend")
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Unknown error"
                // Check for authentication errors and redirect to login
                if (errorMsg.contains("Authentication", ignoreCase = true) || 
                    errorMsg.contains("token", ignoreCase = true) ||
                    errorMsg.contains("token not found", ignoreCase = true) ||
                    errorMsg.contains("session expired", ignoreCase = true)) {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    errorMessage = errorMsg
                }
                isLoading = false
                android.util.Log.e("MyPostedJobsScreen", "Failed to fetch jobs from backend: ${error.message}")
            }
        )
    }
    
    // Combine local and backend jobs, prioritizing backend
    val allJobs = remember(backendJobs, localJobs) {
        val backendIds = backendJobs.map { it.id }.toSet()
        val localOnly = localJobs.filter { it.id !in backendIds && it.clientId == "client_mary_johnson" }
        (backendJobs + localOnly).distinctBy { it.id }
    }
    
    // Filter jobs posted by the current client
    val postedJobs = allJobs.filter { 
        it.clientId == "client_mary_johnson" && (
            it.status == JobStatus.ACTIVE || it.status == JobStatus.APPLIED || 
            it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.COMPLETED 
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
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
                text = "My Posted Jobs",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show loading or error state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading your posted jobs...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else if (errorMessage != null) {
            // Show error but still allow viewing local jobs
            Text(
                text = "Note: ${errorMessage}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Jobs List
        if (postedJobs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📋",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No posted jobs yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first job to get started",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(postedJobs) { job ->
                    PostedJobCardDetailed(job = job, navController = navController)
                }
            }
        }
    }
}

@Composable
fun PostedJobCardDetailed(
    job: JobData,
    navController: NavController
) {
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.0f", job.pay)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = when (job.status) {
                            JobStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                            JobStatus.APPLIED -> MaterialTheme.colorScheme.secondaryContainer
                            JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                            JobStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                            JobStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when (job.status) {
                                JobStatus.ACTIVE -> "Active"
                                JobStatus.APPLIED -> "Applied"
                                JobStatus.IN_PROGRESS -> "In Progress"
                                JobStatus.COMPLETED -> "Completed"
                                JobStatus.CANCELLED -> "Cancelled"
                                else -> "Unknown"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Job Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Type: ${job.jobType}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Deadline: ${formatDateWithoutTime(job.deadline)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (job.workerId != null) {
                        Text(
                            text = "Worker Assigned",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "No Worker Yet",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Applicants Button (always show for active jobs)
                if (job.status == JobStatus.ACTIVE) {
                    TextButton(
                        onClick = { 
                            navController.navigate("job_applicants/${job.id}")
                        },
                        modifier = Modifier.weight(1f)
                            .height(40.dp) // Larger button height
                    ) {
                        Text(
                            text = "👥 View Applicants",
                            fontSize = 14.sp, // Increased from 12.sp
                            fontWeight = FontWeight.Medium // Make it slightly bolder
                        )
                    }
                }
                
                // Chat Button (if worker is assigned)
                if (job.workerId != null) {
                    TextButton(
                        onClick = { 
                            navController.navigate("job_chat/${job.title}")
                        },
                        modifier = Modifier.weight(1f)
                            .height(40.dp) // Larger button height
                    ) {
                        Text(
                            text = "💬 Chat",
                            fontSize = 14.sp, // Increased from 12.sp
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // View Details Button
                TextButton(
                    onClick = { 
                        navController.navigate("job_details/${job.id}")
                    },
                    modifier = Modifier.weight(1f)
                        .height(40.dp) // Larger button height
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 14.sp, // Increased from 12.sp
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Format date string to remove time component
 * Handles ISO 8601 format dates (with time) and returns date only
 */
private fun formatDateWithoutTime(dateString: String): String {
    if (dateString.isEmpty()) return "Not specified"
    
    return try {
        // Try parsing ISO 8601 format (e.g., "2025-11-01T16:30:00Z")
        val instant = Instant.parse(dateString)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        // If parsing fails, try simple date format or return as-is
        try {
            // Try other common formats
            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
            )
            for (format in formats) {
                try {
                    val parsed = SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
                    if (parsed != null) {
                        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(parsed)
                    }
                } catch (e2: Exception) {
                    continue
                }
            }
            // If all parsing fails, return original string without time if possible
            dateString.split("T").firstOrNull() ?: dateString.split(" ").firstOrNull() ?: dateString
        } catch (e2: Exception) {
            dateString.split("T").firstOrNull() ?: dateString.split(" ").firstOrNull() ?: dateString
        }
    }
}
