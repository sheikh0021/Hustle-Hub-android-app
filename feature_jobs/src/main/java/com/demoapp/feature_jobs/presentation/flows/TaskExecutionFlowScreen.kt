package com.demoapp.feature_jobs.presentation.flows

import androidx.compose.foundation.layout.*
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
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskExecutionFlowScreen(
    navController: NavController,
    taskId: String
) {
    val taskRepository = remember { TaskRepository.getInstance() }
    var task by remember { mutableStateOf<TaskData?>(null) }
    
    LaunchedEffect(taskId) {
        task = taskRepository.getTaskById(taskId)
    }
    var currentStep by remember { mutableStateOf(TaskStep.AT_STORE) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var photosTaken by remember { mutableStateOf(false) }
    var clientConfirmed by remember { mutableStateOf(false) }

    if (task == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Task not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Task Header
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
                            text = "Task Execution",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = task!!.title,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    Text(
                        text = "KES ${task!!.budget}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TaskInfoItem(
                        icon = Icons.Default.Home,
                        label = "Store",
                        value = task?.location?.storeLocation?.address ?: "N/A"
                    )
                    
                    TaskInfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Delivery",
                        value = task?.location?.deliveryLocation?.address ?: "N/A"
                    )
                    
                    TaskInfoItem(
                        icon = Icons.Default.Star,
                        label = "Type",
                        value = task?.category?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"
                    )
                }
            }
        }

        // Execution Steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Execution Steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Step 1: At Store
                ExecutionStep(
                    step = TaskStep.AT_STORE,
                    title = "Arrive at Store",
                    description = "Navigate to the specified store location",
                    isCompleted = currentStep.ordinal > TaskStep.AT_STORE.ordinal,
                    isCurrent = currentStep == TaskStep.AT_STORE,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == TaskStep.AT_STORE) {
                            currentStep = TaskStep.TAKE_PHOTOS
                        }
                    }
                )
                
                // Step 2: Take Photos
                ExecutionStep(
                    step = TaskStep.TAKE_PHOTOS,
                    title = "Take Photos",
                    description = "Take pictures of items and send to client for confirmation",
                    isCompleted = currentStep.ordinal > TaskStep.TAKE_PHOTOS.ordinal,
                    isCurrent = currentStep == TaskStep.TAKE_PHOTOS,
                    icon = Icons.Default.Star,
                    onAction = {
                        if (currentStep == TaskStep.TAKE_PHOTOS) {
                            showPhotoDialog = true
                        }
                    }
                )
                
                // Step 3: Client Confirmation
                ExecutionStep(
                    step = TaskStep.CLIENT_CONFIRMATION,
                    title = "Client Confirmation",
                    description = "Wait for client to confirm items before purchase",
                    isCompleted = currentStep.ordinal > TaskStep.CLIENT_CONFIRMATION.ordinal,
                    isCurrent = currentStep == TaskStep.CLIENT_CONFIRMATION,
                    icon = Icons.Default.Email,
                    onAction = {
                        if (currentStep == TaskStep.CLIENT_CONFIRMATION) {
                            showConfirmationDialog = true
                        }
                    }
                )
                
                // Step 4: Buy Goods
                ExecutionStep(
                    step = TaskStep.BUY_GOODS,
                    title = "Purchase Items",
                    description = "Buy the confirmed items from the store",
                    isCompleted = currentStep.ordinal > TaskStep.BUY_GOODS.ordinal,
                    isCurrent = currentStep == TaskStep.BUY_GOODS,
                    icon = Icons.Default.ShoppingCart,
                    onAction = {
                        if (currentStep == TaskStep.BUY_GOODS) {
                            currentStep = TaskStep.DELIVER_GOODS
                        }
                    }
                )
                
                // Step 5: Deliver
                ExecutionStep(
                    step = TaskStep.DELIVER_GOODS,
                    title = "Deliver Items",
                    description = "Deliver the purchased items to the client",
                    isCompleted = currentStep.ordinal > TaskStep.DELIVER_GOODS.ordinal,
                    isCurrent = currentStep == TaskStep.DELIVER_GOODS,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == TaskStep.DELIVER_GOODS) {
                            currentStep = TaskStep.COMPLETE_TASK
                        }
                    }
                )
                
                // Step 6: Complete
                ExecutionStep(
                    step = TaskStep.COMPLETE_TASK,
                    title = "Complete Task",
                    description = "Mark task as completed and wait for CC to release payment",
                    isCompleted = false,
                    isCurrent = currentStep == TaskStep.COMPLETE_TASK,
                    icon = Icons.Default.CheckCircle,
                    onAction = {
                        if (currentStep == TaskStep.COMPLETE_TASK) {
                            // Navigate to payment completion flow
                            navController.navigate("payment_completion/$taskId")
                        }
                    }
                )
            }
        }

        // Communication Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Client Communication",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { /* Open chat with client */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with Job Requester")
                }
                
                if (currentStep == TaskStep.CLIENT_CONFIRMATION) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Waiting for client confirmation...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Photo Dialog
    if (showPhotoDialog) {
        PhotoCaptureDialog(
            onPhotosTaken = {
                photosTaken = true
                showPhotoDialog = false
                currentStep = TaskStep.CLIENT_CONFIRMATION
            },
            onDismiss = { showPhotoDialog = false }
        )
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        ClientConfirmationDialog(
            onConfirmed = {
                clientConfirmed = true
                showConfirmationDialog = false
                currentStep = TaskStep.BUY_GOODS
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }
}

@Composable
fun TaskInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ExecutionStep(
    step: TaskStep,
    title: String,
    description: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isCurrent) onAction() },
        color = when {
            isCompleted -> MaterialTheme.colorScheme.primaryContainer
            isCurrent -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isCurrent) 
                                MaterialTheme.colorScheme.onSecondary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                        isCurrent -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        isCurrent -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            
            if (isCurrent) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun PhotoCaptureDialog(
    onPhotosTaken: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Take Photos")
        },
        text = {
            Column {
                Text("Take photos of the items and send them to the client for confirmation.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Instructions:",
                    fontWeight = FontWeight.Bold
                )
                Text("1. Take clear photos of each item")
                Text("2. Include price tags if visible")
                Text("3. Send photos via chat to client")
                Text("4. Wait for client confirmation before purchasing")
            }
        },
        confirmButton = {
            Button(onClick = onPhotosTaken) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Photos Taken")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClientConfirmationDialog(
    onConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Client Confirmation")
        },
        text = {
            Column {
                Text("Has the client confirmed the items and given approval to proceed with purchase?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Make sure you have:",
                    fontWeight = FontWeight.Bold
                )
                Text("• Clear photos of all items")
                Text("• Client's approval message")
                Text("• Confirmed quantities and prices")
            }
        },
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text("Client Confirmed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Yet")
            }
        }
    )
}

enum class TaskStep {
    AT_STORE,
    TAKE_PHOTOS,
    CLIENT_CONFIRMATION,
    BUY_GOODS,
    DELIVER_GOODS,
    COMPLETE_TASK
}
