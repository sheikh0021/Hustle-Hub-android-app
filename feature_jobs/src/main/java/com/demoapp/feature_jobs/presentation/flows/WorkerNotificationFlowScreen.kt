package com.demoapp.feature_jobs.presentation.flows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
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
import com.demoapp.feature_jobs.domain.models.*
import androidx.compose.ui.platform.LocalContext
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerNotificationFlowScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    var availableTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        availableTasks = taskRepository.getAvailableTasks()
    }
    var workerStats by remember { mutableStateOf(WorkerStats()) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskData?>(null) }

    // Simulate new task notifications
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // Check for new tasks every 30 seconds
            val newTasks = taskRepository.getAvailableTasks()
            if (newTasks.size > availableTasks.size) {
                showNotificationDialog = true
                availableTasks = newTasks
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Worker Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Worker Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Welcome back, John Kamau",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    IconButton(
                        onClick = { /* Show profile */ }
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WorkerStatCard(
                        title = "Active Tasks",
                        value = workerStats.activeTasks.toString(),
                        icon = Icons.Default.Star
                    )
                    
                    WorkerStatCard(
                        title = "Completed",
                        value = workerStats.completedTasks.toString(),
                        icon = Icons.Default.CheckCircle
                    )
                    
                    WorkerStatCard(
                        title = "Earnings",
                        value = "KES ${workerStats.totalEarnings}",
                        icon = Icons.Default.Star
                    )
                }
            }
        }

        // Notifications Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.error,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3",
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationItem(
                    title = "New Task Available",
                    message = "Shopping task in Westlands - KES 2,500",
                    time = "2 minutes ago",
                    isRead = false,
                    onClick = { /* Show task details */ }
                )
                
                NotificationItem(
                    title = "Task Reminder",
                    message = "Don't forget to take pictures at the store",
                    time = "1 hour ago",
                    isRead = true,
                    onClick = { /* Show reminder */ }
                )
                
                NotificationItem(
                    title = "Payment Received",
                    message = "KES 1,800 payment for completed delivery task",
                    time = "3 hours ago",
                    isRead = true,
                    onClick = { /* Show payment details */ }
                )
            }
        }

        // Available Tasks Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = { /* Refresh tasks */ }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (availableTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No tasks available at the moment",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Check back later for new opportunities",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTasks) { task ->
                            AvailableTaskCard(
                                task = task,
                                onAccept = {
                                    selectedTask = task
                                    showNotificationDialog = true
                                },
                                onViewDetails = {
                                    navController.navigate("task_details/${task.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Task Acceptance Dialog
    if (showNotificationDialog && selectedTask != null) {
        TaskAcceptanceDialog(
            task = selectedTask!!,
            onAccept = {
                showNotificationDialog = false
                // Assign worker to task and navigate to task execution
                coroutineScope.launch {
                    val updatedTask = selectedTask!!.copy(
                        assignedWorkerId = "worker_john_kamau",
                        status = TaskStatus.ASSIGNED
                    )
                    taskRepository.updateTask(updatedTask)
                }
                navController.navigate("task_execution_flow/${selectedTask!!.id}")
                selectedTask = null
            },
            onDismiss = {
                showNotificationDialog = false
                selectedTask = null
            }
        )
    }
}

@Composable
fun WorkerStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun NotificationItem(
    title: String,
    message: String,
    time: String,
    isRead: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isRead) 
            MaterialTheme.colorScheme.surface 
        else 
            MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isRead) Icons.Default.Notifications else Icons.Default.Notifications,
                contentDescription = "Notification",
                modifier = Modifier.size(24.dp),
                tint = if (isRead) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else 
                    MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                    color = if (isRead) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = if (isRead) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = if (isRead) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
            
            if (!isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {}
            }
        }
    }
}

@Composable
fun AvailableTaskCard(
    task: TaskData,
    onAccept: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                        Text(
                            text = "Store: ${task.location.storeLocation.address}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    
                        Text(
                            text = "Delivery: ${task.location.deliveryLocation.address}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "KES ${task.budget}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Surface(
                        color = when (task.category) {
                            TaskCategory.SHOPPING -> MaterialTheme.colorScheme.primaryContainer
                            TaskCategory.DELIVERY -> MaterialTheme.colorScheme.secondaryContainer
                            TaskCategory.SURVEY -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Details")
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept Task")
                }
            }
        }
    }
}

@Composable
fun TaskAcceptanceDialog(
    task: TaskData,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Accept Task")
        },
        text = {
            Column {
                Text("Are you sure you want to accept this task?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Task: ${task.title}",
                    fontWeight = FontWeight.Bold
                )
                Text("Budget: KES ${task.budget}")
                Text("Category: ${task.category.name.lowercase().replaceFirstChar { it.uppercase() }}")
                Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The job requester has already paid. You'll execute the task and get paid after completion.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Once accepted, this task will be locked to you and you'll be responsible for completing it.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class WorkerStats(
    val activeTasks: Int = 2,
    val completedTasks: Int = 45,
    val totalEarnings: Int = 12500
)
