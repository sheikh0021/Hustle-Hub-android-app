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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryFlowScreen(
    navController: NavController,
    taskId: String
) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    var task by remember { mutableStateOf<TaskData?>(null) }
    var currentStep by remember { mutableStateOf(DeliveryStep.PICKUP) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isDelivered by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        task = taskRepository.getTaskById(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Flow: ${task?.title ?: "Loading..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Summary Card
            task?.let { currentTask ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delivery Task Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("From: ${currentTask.location.storeLocation.address}")
                        Text("To: ${currentTask.location.deliveryLocation.address}")
                        Text("Budget: KES ${currentTask.budget}")
                        Text("Status: ${currentTask.status.name}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Steps
                Text(
                    text = "Delivery Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Step 1: Pickup
                DeliveryStepCard(
                    step = DeliveryStep.PICKUP,
                    title = "Pickup Items",
                    description = "Collect items from the pickup location",
                    isCompleted = currentStep.ordinal > DeliveryStep.PICKUP.ordinal,
                    isCurrent = currentStep == DeliveryStep.PICKUP,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == DeliveryStep.PICKUP) {
                            currentStep = DeliveryStep.EN_ROUTE
                        }
                    }
                )

                // Step 2: En Route
                DeliveryStepCard(
                    step = DeliveryStep.EN_ROUTE,
                    title = "En Route to Delivery",
                    description = "Traveling to the delivery location",
                    isCompleted = currentStep.ordinal > DeliveryStep.EN_ROUTE.ordinal,
                    isCurrent = currentStep == DeliveryStep.EN_ROUTE,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == DeliveryStep.EN_ROUTE) {
                            currentStep = DeliveryStep.ARRIVED
                        }
                    }
                )

                // Step 3: Arrived
                DeliveryStepCard(
                    step = DeliveryStep.ARRIVED,
                    title = "Arrived at Destination",
                    description = "Confirm arrival at delivery location",
                    isCompleted = currentStep.ordinal > DeliveryStep.ARRIVED.ordinal,
                    isCurrent = currentStep == DeliveryStep.ARRIVED,
                    icon = Icons.Default.LocationOn,
                    onAction = {
                        if (currentStep == DeliveryStep.ARRIVED) {
                            currentStep = DeliveryStep.DELIVERED
                            isDelivered = true
                        }
                    }
                )

                // Step 4: Delivered
                DeliveryStepCard(
                    step = DeliveryStep.DELIVERED,
                    title = "Items Delivered",
                    description = "Confirm successful delivery",
                    isCompleted = isDelivered,
                    isCurrent = currentStep == DeliveryStep.DELIVERED,
                    icon = Icons.Default.CheckCircle,
                    onAction = {
                        if (currentStep == DeliveryStep.DELIVERED) {
                            navController.navigate("payment_completion/$taskId")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Communication Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Communication",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { showChatDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Chat")
                            }
                            
                            Button(
                                onClick = { showLocationDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Location")
                            }
                        }
                    }
                }

                // Complete Delivery Button
                if (currentStep == DeliveryStep.DELIVERED && isDelivered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("payment_completion/$taskId") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Complete Delivery & Process Payment", fontSize = 18.sp)
                    }
                }
            } ?: run {
                Text("Loading delivery details...", fontSize = 18.sp)
            }
        }
    }

    // Chat Dialog
    if (showChatDialog) {
        ChatDialog(
            onDismiss = { showChatDialog = false },
            onSendMessage = { message ->
                // Handle sending message
                coroutineScope.launch {
                    // Simulate message sending
                    delay(1000)
                }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        LocationDialog(
            onDismiss = { showLocationDialog = false },
            onShareLocation = {
                // Handle location sharing
                coroutineScope.launch {
                    // Simulate location sharing
                    delay(1000)
                }
            }
        )
    }
}

@Composable
fun DeliveryStepCard(
    step: DeliveryStep,
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
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isCompleted) Color.Green else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color.Green else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isCurrent) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Confirm", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ChatDialog(
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat with Job Requester") },
        text = {
            Column {
                Text("Send a message to the job requester about delivery status.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        onSendMessage(message)
                        onDismiss()
                    }
                },
                enabled = message.isNotBlank()
            ) {
                Text("Send")
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
fun LocationDialog(
    onDismiss: () -> Unit,
    onShareLocation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Location") },
        text = {
            Text("Share your current location with the job requester for delivery tracking.")
        },
        confirmButton = {
            Button(onClick = {
                onShareLocation()
                onDismiss()
            }) {
                Text("Share Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class DeliveryStep {
    PICKUP,
    EN_ROUTE,
    ARRIVED,
    DELIVERED
}
