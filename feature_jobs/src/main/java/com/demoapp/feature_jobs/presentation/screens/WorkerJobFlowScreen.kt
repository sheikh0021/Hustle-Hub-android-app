package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobFlowScreen(
    navController: NavController,
    jobId: String = "job_123" // This would come from navigation arguments
) {
    var currentStep by remember { mutableStateOf(WorkerJobStep.NOTIFICATION) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showPictureDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Job Flow",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                JobProgressIndicator(currentStep = currentStep)
            }
            
            item {
                JobDetailsCard()
            }
            
            item {
                CurrentStepCard(
                    step = currentStep,
                    onNextStep = { 
                        currentStep = when (currentStep) {
                            WorkerJobStep.NOTIFICATION -> WorkerJobStep.ACCEPTED
                            WorkerJobStep.ACCEPTED -> WorkerJobStep.AT_STORE
                            WorkerJobStep.AT_STORE -> WorkerJobStep.BOUGHT_GOODS
                            WorkerJobStep.BOUGHT_GOODS -> WorkerJobStep.DELIVERING
                            WorkerJobStep.DELIVERING -> WorkerJobStep.COMPLETED
                            WorkerJobStep.COMPLETED -> currentStep
                        }
                    },
                    onChatWithClient = { showChatDialog = true },
                    onTakePictures = { showPictureDialog = true }
                )
            }
            
            item {
                CommunicationCard(onChatClick = { showChatDialog = true })
            }
        }
    }
    
    // Chat Dialog
    if (showChatDialog) {
        ChatWithClientDialog(
            onDismiss = { showChatDialog = false }
        )
    }
    
    // Picture Dialog
    if (showPictureDialog) {
        TakePicturesDialog(
            onDismiss = { showPictureDialog = false }
        )
    }
}

enum class WorkerJobStep {
    NOTIFICATION,
    ACCEPTED,
    AT_STORE,
    BOUGHT_GOODS,
    DELIVERING,
    COMPLETED
}

@Composable
fun JobProgressIndicator(currentStep: WorkerJobStep) {
    val steps = listOf(
        "Notification" to WorkerJobStep.NOTIFICATION,
        "Accept" to WorkerJobStep.ACCEPTED,
        "At Store" to WorkerJobStep.AT_STORE,
        "Buy Goods" to WorkerJobStep.BOUGHT_GOODS,
        "Deliver" to WorkerJobStep.DELIVERING,
        "Complete" to WorkerJobStep.COMPLETED
    )
    
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
                text = "Job Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            steps.forEachIndexed { index, (stepName, step) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step indicator
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (step.ordinal <= currentStep.ordinal) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.ordinal < currentStep.ordinal) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = if (step.ordinal <= currentStep.ordinal) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = stepName,
                        fontSize = 14.sp,
                        fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal,
                        color = if (step.ordinal <= currentStep.ordinal) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    if (step == currentStep) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Current)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun JobDetailsCard() {
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            JobDetailRow("Title", "Buy groceries from supermarket")
            JobDetailRow("Store", "Nakumatt Junction")
            JobDetailRow("Delivery", "Westlands, Nairobi")
            JobDetailRow("Payment", "KES 2,500")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Items to buy:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "• 2kg Rice - Basmati brand\n• 1L Milk - Tuzo brand\n• 500g Sugar - Mumias brand",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun JobDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CurrentStepCard(
    step: WorkerJobStep,
    onNextStep: () -> Unit,
    onChatWithClient: () -> Unit,
    onTakePictures: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = getStepTitle(step),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getStepDescription(step),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons based on current step
            when (step) {
                WorkerJobStep.NOTIFICATION -> {
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Accept Job")
                    }
                }
                
                WorkerJobStep.ACCEPTED -> {
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("I'm at the store")
                    }
                }
                
                WorkerJobStep.AT_STORE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTakePictures,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Pictures")
                        }
                        
                        Button(
                            onClick = onChatWithClient,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("I've bought the goods")
                    }
                }
                
                WorkerJobStep.BOUGHT_GOODS -> {
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Start delivery")
                    }
                }
                
                WorkerJobStep.DELIVERING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onChatWithClient,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat")
                        }
                        
                        Button(
                            onClick = onNextStep,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Complete Delivery")
                        }
                    }
                }
                
                WorkerJobStep.COMPLETED -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Job Completed!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = "Payment will be transferred to your account",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunicationCard(onChatClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Communication",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Stay in touch with the job requester throughout the process",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onChatClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with Job Requester")
            }
        }
    }
}

@Composable
fun ChatWithClientDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chat with Job Requester",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("This would open the chat interface with the job requester for real-time communication during task execution.")
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Open Chat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TakePicturesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Take Pictures",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Take pictures of the goods at the store to confirm with the job requester before purchasing.")
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Open Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getStepTitle(step: WorkerJobStep): String {
    return when (step) {
        WorkerJobStep.NOTIFICATION -> "Job Notification"
        WorkerJobStep.ACCEPTED -> "Job Accepted"
        WorkerJobStep.AT_STORE -> "At Store"
        WorkerJobStep.BOUGHT_GOODS -> "Goods Purchased"
        WorkerJobStep.DELIVERING -> "Delivering"
        WorkerJobStep.COMPLETED -> "Completed"
    }
}

fun getStepDescription(step: WorkerJobStep): String {
    return when (step) {
        WorkerJobStep.NOTIFICATION -> "You have received a new job notification. Review the details and accept if you can complete it."
        WorkerJobStep.ACCEPTED -> "You have accepted the job. Please proceed to the store location."
        WorkerJobStep.AT_STORE -> "You are at the store. Take pictures of the goods and confirm with the client before purchasing."
        WorkerJobStep.BOUGHT_GOODS -> "You have purchased the goods. Now proceed to deliver them to the client."
        WorkerJobStep.DELIVERING -> "You are delivering the goods to the client. Keep them updated on your progress."
        WorkerJobStep.COMPLETED -> "The job has been completed successfully. Payment will be transferred to your account."
    }
}
