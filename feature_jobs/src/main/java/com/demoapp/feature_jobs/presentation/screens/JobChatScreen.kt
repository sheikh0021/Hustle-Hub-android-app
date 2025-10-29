package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
    
    // Check if contractor is selected for this job
    val isSelected = remember { 
        job?.let { 
            applicationRepository.isContractorSelectedForJob(it.id, currentUserId)
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
        
        // Worker Action Buttons (only for workers)
        if (job != null && currentUserType == SenderType.WORKER) {
            WorkerActionButtons(
                jobId = job.id,
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
        if (!isSelected && currentUserType == SenderType.WORKER) {
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

        // Chat Input (only enabled if contractor is selected)
        ChatInputArea(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = { text ->
                if (text.isNotBlank() && job != null && isSelected) {
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
            isEnabled = isSelected
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

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
