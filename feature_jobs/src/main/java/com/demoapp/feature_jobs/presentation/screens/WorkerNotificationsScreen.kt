package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.NotificationRepository
import com.demoapp.feature_jobs.presentation.models.NotificationData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerNotificationsScreen(
    workerId: String = "worker_1", // Default worker ID for demo
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val notificationRepository = NotificationRepository.getInstance()
    val notifications by notificationRepository.notifications.collectAsState()
    
    // Filter notifications for this specific worker
    val userNotifications = notifications.filter { it.recipientId == workerId }
    val unreadCount = notificationRepository.getUnreadCount(workerId)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Mark all as read button
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { notificationRepository.markAllAsRead(workerId) }
                        ) {
                            Text("Mark all read")
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (userNotifications.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No notifications yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You'll receive notifications when you're selected for jobs or when there are updates.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            // Notifications list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userNotifications) { notification ->
                    WorkerNotificationCard(
                        notification = notification,
                        onNotificationClick = { 
                            notificationRepository.markAsRead(notification.id)
                            // Handle navigation based on action type
                            when (notification.actionType) {
                                com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_JOB -> {
                                    notification.jobId?.let { jobId ->
                                        navController.navigate("job_details/$jobId")
                                    }
                                }
                                com.demoapp.feature_jobs.presentation.models.NotificationActionType.START_JOB_CHATBOT -> {
                                    notification.jobId?.let { jobId ->
                                        navController.navigate("job_start_chatbot/$jobId")
                                    }
                                }
                                com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_MESSAGE -> {
                                    notification.jobId?.let { jobId ->
                                        navController.navigate("job_chat/$jobId")
                                    }
                                }
                                else -> { /* No action */ }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerNotificationCard(
    notification: NotificationData,
    onNotificationClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onNotificationClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(notification.createdAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            // Action hint for actionable notifications
            if (notification.actionRequired && notification.actionType != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (notification.actionType) {
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.START_JOB_CHATBOT -> "Tap to start job validation"
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_JOB -> "Tap to view job details"
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_MESSAGE -> "Tap to open chat"
                        else -> "Tap for more details"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
