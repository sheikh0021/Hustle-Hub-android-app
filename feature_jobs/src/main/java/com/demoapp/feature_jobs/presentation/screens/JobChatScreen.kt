package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.data.FirebaseChatRepository
import com.demoapp.feature_jobs.data.FirebaseChatMessage
import com.demoapp.feature_jobs.data.SenderType
import com.demoapp.feature_jobs.data.JobApplicationRepository
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import com.demoapp.feature_jobs.presentation.components.JobTimelineComponent
import com.demoapp.feature_jobs.presentation.components.WorkerActionButtons
import com.demoapp.feature_jobs.presentation.models.TimelineStage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobChatScreen(
    navController: NavController,
    jobTitle: String,
    currentUserId: String = "worker_john_kamau", // Default to worker
    currentUserType: SenderType = SenderType.WORKER, // Default to worker
    currentUserName: String = "John Kamau" // Default to worker name
) {
    val context = LocalContext.current
    val jobRepository = JobRepositorySingleton.instance
    val firebaseChatRepository = FirebaseChatRepository.getInstance()
    val applicationRepository = JobApplicationRepository.getInstance()
    val jobs by jobRepository.jobs.collectAsState()
    val job = jobs.find { it.title == jobTitle }
    
    // Observe applications to react to selection changes
    val applications by applicationRepository.applications.collectAsState()
    
    // Check if contractor is selected for this job - reactive to application changes and job updates
    val isSelected = remember(job?.id, currentUserId, applications, job?.workerId, job?.workerAccepted, job?.status) { 
        job?.let { 
            android.util.Log.d("JobChatScreen", "Checking isSelected for job ${it.id}, currentUserId=$currentUserId, job.workerId=${it.workerId}, workerAccepted=${it.workerAccepted}, status=${it.status}")
            
            // Check multiple sources:
            // 1. Application repository status
            val isSelectedInApp = applicationRepository.isContractorSelectedForJob(it.id, currentUserId)
            android.util.Log.d("JobChatScreen", "isSelectedInApp: $isSelectedInApp")
            
            // 2. Job's workerId field (updated when worker is assigned) - more flexible matching
            val normalizedCurrentUserId = currentUserId.lowercase().trim()
            val normalizedWorkerId = it.workerId?.lowercase()?.trim() ?: ""
            val isAssignedToWorker = normalizedWorkerId == normalizedCurrentUserId || 
                                      normalizedWorkerId == "worker_${normalizedCurrentUserId}" ||
                                      normalizedWorkerId.contains(normalizedCurrentUserId) ||
                                      normalizedCurrentUserId.contains(normalizedWorkerId) ||
                                      (normalizedWorkerId.isNotBlank() && normalizedCurrentUserId.contains(normalizedWorkerId)) ||
                                      // Also check if workerId ends with currentUserId (for formats like "worker_john_kamau" vs "john_kamau")
                                      (normalizedWorkerId.isNotBlank() && normalizedCurrentUserId.endsWith(normalizedWorkerId.replace("worker_", ""))) ||
                                      // Check reverse pattern
                                      (normalizedWorkerId.replace("worker_", "") == normalizedCurrentUserId.replace("worker_", ""))
            
            android.util.Log.d("JobChatScreen", "isAssignedToWorker: $isAssignedToWorker (job.workerId=${it.workerId})")
            
            // 3. Worker accepted flag - check for IN_PROGRESS status which means worker is selected
            val isWorkerAccepted = (it.workerAccepted == true) || 
                                  (it.status == com.demoapp.feature_jobs.presentation.models.JobStatus.IN_PROGRESS) ||
                                  (it.status == com.demoapp.feature_jobs.presentation.models.JobStatus.COMPLETED && it.workerId != null)
            
            // 4. If job has a workerId set and status is IN_PROGRESS or workerAccepted, assume worker is selected
            // This handles cases where the backend assigns a worker
            val hasWorkerAssigned = it.workerId != null && 
                                   (it.status == com.demoapp.feature_jobs.presentation.models.JobStatus.IN_PROGRESS || 
                                    it.workerAccepted == true ||
                                    it.currentTimelineStage != null) // If timeline stage is set, worker is working
            
            android.util.Log.d("JobChatScreen", "isWorkerAccepted: $isWorkerAccepted, hasWorkerAssigned: $hasWorkerAssigned")
            
            val result = isSelectedInApp || isAssignedToWorker || isWorkerAccepted || (hasWorkerAssigned && currentUserType == SenderType.WORKER)
            android.util.Log.d("JobChatScreen", "Final isSelected result: $result")
            result
        } ?: false
    }
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Function to make a phone call
    fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }
    
    // Get messages from Firebase in real-time
    val jobMessages by firebaseChatRepository.getMessagesForJob(job?.id ?: "").collectAsState(initial = emptyList())
    
    // Initialize chat room if it doesn't exist
    LaunchedEffect(job) {
        if (job != null) {
            firebaseChatRepository.createChatRoom(
                jobId = job.id,
                jobTitle = job.title,
                clientId = "client_sarah", // Default client for demo
                clientName = "Sarah Johnson"
            )
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(jobMessages.size) {
        if (jobMessages.isNotEmpty()) {
            listState.animateScrollToItem(jobMessages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text(
                        text = job?.title ?: "Job Chat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (job != null) "$${String.format("%.0f", job.pay)} • ${job.jobType}" else "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Phone call button
                IconButton(
                    onClick = {
                        // Get phone number from job data
                        val phoneNumber = job?.clientPhoneNumber ?: "+254700000000"
                        makePhoneCall(phoneNumber)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Job Poster",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Job Timeline (if job exists and has timeline)
        if (job != null && job.currentTimelineStage != null) {
            JobTimelineComponent(
                jobId = job.id,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Check if worker needs to accept the job
        // Worker is selected (workerId matches) but hasn't accepted yet
        val needsWorkerAcceptance = remember(job?.workerId, job?.workerAccepted, currentUserId, currentUserType) {
            job != null && 
            currentUserType == SenderType.WORKER &&
            job.workerId != null &&
            (job.workerId == currentUserId || job.workerId.contains(currentUserId) || currentUserId.contains(job.workerId)) &&
            !job.workerAccepted &&
            job.status != com.demoapp.feature_jobs.presentation.models.JobStatus.COMPLETED &&
            !job.isCompleted
        }
        
        var showAcceptJobDialog by remember { mutableStateOf(false) }
        
        // Accept Job Card (shown when worker is selected but hasn't accepted)
        if (needsWorkerAcceptance) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 You've been selected!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have been selected for this job. Please accept to begin working.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAcceptJobDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept Job", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Worker Action Buttons - Show for workers who have been assigned OR accepted
        // Show buttons whenever worker is viewing the chat (after being selected)
        val shouldShowWorkerButtons = job != null && 
            currentUserType == SenderType.WORKER && 
            (
                // Worker has been assigned to this job (by ID match)
                (job.workerId != null && (
                    job.workerId == currentUserId || 
                    job.workerId.contains(currentUserId) || 
                    currentUserId.contains(job.workerId)
                )) ||
                // OR worker has explicitly accepted
                job.workerAccepted ||
                // OR worker is selected in applications
                isSelected
            )
        
        // Debug logging
        LaunchedEffect(job?.id, job?.workerAccepted, job?.workerId, currentUserId, isSelected) {
            android.util.Log.d("JobChatScreen", "Worker buttons check - jobId=${job?.id}, title=${job?.title}, workerId=${job?.workerId}, currentUserId=$currentUserId, workerAccepted=${job?.workerAccepted}, isSelected=$isSelected, shouldShow=$shouldShowWorkerButtons")
            android.util.Log.d("JobChatScreen", "Job status: ${job?.status}, timelineStage=${job?.currentTimelineStage}")
        }
        
        // Show buttons if worker is viewing - temporarily show for ALL workers to debug
        // TODO: Make this more specific once we confirm buttons render
        val forceShowButtons = job != null && currentUserType == SenderType.WORKER
        
        if (shouldShowWorkerButtons || forceShowButtons) {
            android.util.Log.d("JobChatScreen", "DEBUG: Showing WorkerActionButtons - shouldShow=$shouldShowWorkerButtons, forceShow=$forceShowButtons, jobId=${job?.id}")
            WorkerActionButtons(
                jobId = job!!.id,
                workerId = currentUserId,
                workerName = currentUserName,
                currentStage = job.currentTimelineStage,
                jobTitle = job.title,
                onStageUpdated = { newStage ->
                    // Update job with new timeline stage in repository
                    jobRepository.updateJobTimelineStage(job.id, newStage)
                },
                onCreateInvoice = {
                    // Navigate to invoice creation screen
                    navController.navigate("create_invoice/${job.id}")
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Accept Job Dialog with Questions
        if (showAcceptJobDialog) {
            AcceptJobDialog(
                jobTitle = job?.title ?: "Job",
                onConfirm = {
                    // Worker accepts the job
                    if (job != null) {
                        jobRepository.acceptJob(job.id, currentUserId)
                        
                        // Send acceptance message to chat
                        coroutineScope.launch {
                            val acceptanceMessage = FirebaseChatMessage(
                                jobId = job.id,
                                text = "I accept this job and will complete it as per the requirements.",
                                senderId = currentUserId,
                                senderName = currentUserName,
                                senderType = SenderType.WORKER
                            )
                            firebaseChatRepository.sendMessage(acceptanceMessage)
                        }
                    }
                    showAcceptJobDialog = false
                },
                onDismiss = { showAcceptJobDialog = false }
            )
        }
        
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(jobMessages) { message ->
                ChatMessageBubble(
                    message = message,
                    currentUserType = currentUserType
                )
            }
        }
        
        // Communication restriction banner for non-selected contractors
        // Only show if job exists, user is worker, worker is truly not selected, and job hasn't been completed
        val shouldShowRestriction = !isSelected && 
                                   currentUserType == SenderType.WORKER && 
                                   job != null && 
                                   job?.status != com.demoapp.feature_jobs.presentation.models.JobStatus.COMPLETED &&
                                   job?.isCompleted != true &&
                                   job?.workerId == null &&
                                   !needsWorkerAcceptance // Don't show if worker needs to accept
        
        if (shouldShowRestriction) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Communication is only available after you are selected for this job.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Chat Input (only enabled if contractor is selected OR worker has been assigned to job)
        // Allow chat once worker is selected (even before accepting), or if worker has accepted, or if it's the job poster
        val canChat = isSelected || 
                     (currentUserType == SenderType.WORKER && job?.workerId != null && (job.workerId == currentUserId || job.workerId.contains(currentUserId) || currentUserId.contains(job.workerId))) ||
                     (currentUserType == SenderType.CLIENT && job != null)
        ChatInputArea(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = { text ->
                if (text.isNotBlank() && job != null && canChat) {
                    val newMessage = FirebaseChatMessage(
                        jobId = job.id,
                        text = text,
                        senderId = currentUserId,
                        senderName = currentUserName,
                        senderType = currentUserType
                    )
                    messageText = ""
                    // Send message to Firebase in a coroutine
                    coroutineScope.launch {
                        firebaseChatRepository.sendMessage(newMessage)
                    }
                }
            },
            isEnabled = canChat
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: FirebaseChatMessage,
    currentUserType: SenderType
) {
    val isFromCurrentUser = message.senderType == currentUserType
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            // Avatar for other user's messages
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (message.senderType == SenderType.WORKER) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    color = if (message.senderType == SenderType.WORKER) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            // Sender name
            Text(
                text = message.senderName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            // Message bubble
            Box(
                modifier = Modifier
                    .background(
                        color = if (isFromCurrentUser) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isFromCurrentUser) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // Timestamp
            Text(
                text = formatTimestamp(message.timestamp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar for current user's messages
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (currentUserType == SenderType.WORKER) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    color = if (currentUserType == SenderType.WORKER) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        if (isEnabled) 
                            "Type your message..." 
                        else 
                            "Chat available after selection"
                    ) 
                },
                singleLine = true,
                enabled = isEnabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { onSendMessage(messageText) },
                enabled = messageText.isNotBlank() && isEnabled
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank() && isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AcceptJobDialog(
    jobTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var question1 by remember { mutableStateOf("") }
    var question2 by remember { mutableStateOf("") }
    var question3 by remember { mutableStateOf("") }
    var canAccept by remember { mutableStateOf(false) }
    
    // Validate that all questions are answered
    LaunchedEffect(question1, question2, question3) {
        canAccept = question1.isNotBlank() && question2.isNotBlank() && question3.isNotBlank()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Accept Job: $jobTitle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Please answer these questions to accept the job:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = question1,
                    onValueChange = { question1 = it },
                    label = { Text("Do you understand the job requirements?") },
                    placeholder = { Text("Yes, I understand...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
                
                OutlinedTextField(
                    value = question2,
                    onValueChange = { question2 = it },
                    label = { Text("Can you complete this job within the deadline?") },
                    placeholder = { Text("Yes, I can...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
                
                OutlinedTextField(
                    value = question3,
                    onValueChange = { question3 = it },
                    label = { Text("Any questions or clarifications needed?") },
                    placeholder = { Text("Type your questions here...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canAccept
            ) {
                Text("Accept & Start", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
