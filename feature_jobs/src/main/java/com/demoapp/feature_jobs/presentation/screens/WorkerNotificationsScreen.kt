package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
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
        // Top App Bar - Clean and Modern with Neutral Colors
        TopAppBar(
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$unreadCount unread",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                // Mark all as read button
                if (unreadCount > 0) {
                    TextButton(
                        onClick = { notificationRepository.markAllAsRead(workerId) }
                    ) {
                        Text(
                            text = "Mark all read",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        if (userNotifications.isEmpty()) {
            // Empty state - Improved Design
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "You'll receive notifications when you're selected for jobs or when there are updates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = onNotificationClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (notification.isRead) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.actionType) {
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_JOB -> Icons.Default.Build
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.START_JOB_CHATBOT -> Icons.Default.Settings
                        com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_MESSAGE -> Icons.Default.Settings
                        else -> Icons.Default.Star
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (notification.isRead) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    }
                )
            }
            
            // Notification Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bottom Row: Time and Action Hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    // Action hint for actionable notifications
                    if (notification.actionRequired && notification.actionType != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).rotate(180f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (notification.actionType) {
                                    com.demoapp.feature_jobs.presentation.models.NotificationActionType.START_JOB_CHATBOT -> "Start"
                                    com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_JOB -> "View"
                                    com.demoapp.feature_jobs.presentation.models.NotificationActionType.VIEW_MESSAGE -> "Open"
                                    else -> "Details"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
