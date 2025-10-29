package com.demoapp.feature_jobs.presentation.flows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCompletionFlowScreen(
    navController: NavController,
    taskId: String
) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    var task by remember { mutableStateOf<TaskData?>(null) }
    
    LaunchedEffect(taskId) {
        task = taskRepository.getTaskById(taskId)
    }
    var paymentStatus by remember { mutableStateOf(PaymentCompletionStatus.PROCESSING) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Simulate payment processing
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // 5 seconds processing time
        paymentStatus = PaymentCompletionStatus.COMPLETED
        showSuccessDialog = true
    }

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
        // Success Header
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Task Completed Successfully!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Customer Care team will verify completion and release your payment",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Task Summary
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
                    text = "Task Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TaskSummaryRow(
                    label = "Task",
                    value = task!!.title
                )
                
                TaskSummaryRow(
                    label = "Category",
                    value = task!!.category.name.lowercase().replaceFirstChar { it.uppercase() }
                )
                
                TaskSummaryRow(
                    label = "Store Location",
                    value = task?.location?.storeLocation?.address ?: "N/A"
                )
                
                TaskSummaryRow(
                    label = "Delivery Location",
                    value = task?.location?.deliveryLocation?.address ?: "N/A"
                )
                
                TaskSummaryRow(
                    label = "Payment Amount",
                    value = "KES ${task!!.budget}",
                    isHighlighted = true
                )
            }
        }

        // Payment Processing Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (paymentStatus) {
                    PaymentCompletionStatus.PROCESSING -> MaterialTheme.colorScheme.secondaryContainer
                    PaymentCompletionStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                    PaymentCompletionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> Icons.Default.Info
                        PaymentCompletionStatus.COMPLETED -> Icons.Default.CheckCircle
                        PaymentCompletionStatus.FAILED -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> MaterialTheme.colorScheme.secondary
                        PaymentCompletionStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        PaymentCompletionStatus.FAILED -> MaterialTheme.colorScheme.error
                    }
                )
                
                Text(
                    text = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> "CC Team Verification"
                        PaymentCompletionStatus.COMPLETED -> "Payment Released"
                        PaymentCompletionStatus.FAILED -> "Payment Failed"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> MaterialTheme.colorScheme.onSecondaryContainer
                        PaymentCompletionStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
                        PaymentCompletionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> "Customer Care team is verifying your task completion. Payment will be released once confirmed."
                        PaymentCompletionStatus.COMPLETED -> "KES ${task!!.budget} has been released to your account by CC team."
                        PaymentCompletionStatus.FAILED -> "There was an issue with payment release. Please contact support."
                    },
                    fontSize = 14.sp,
                    color = when (paymentStatus) {
                        PaymentCompletionStatus.PROCESSING -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        PaymentCompletionStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        PaymentCompletionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                if (paymentStatus == PaymentCompletionStatus.PROCESSING) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Payment Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Payment Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PaymentDetailRow(
                    label = "Task Payment",
                    amount = task!!.budget,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                PaymentDetailRow(
                    label = "Platform Fee (5%)",
                    amount = task!!.budget * 0.05,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
                )
                
                PaymentDetailRow(
                    label = "Net Amount",
                    amount = task!!.budget * 0.95,
                    isHighlighted = true,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Action Buttons
        if (paymentStatus == PaymentCompletionStatus.COMPLETED) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate("worker_dashboard") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Dashboard")
                }
                
                OutlinedButton(
                    onClick = { navController.navigate("wallet") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Wallet")
                }
            }
        }
        
        if (paymentStatus == PaymentCompletionStatus.FAILED) {
            Button(
                onClick = { /* Contact support */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Support")
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        PaymentSuccessDialog(
            amount = task!!.budget,
            onDismiss = { showSuccessDialog = false },
            onContinue = { 
                showSuccessDialog = false
                navController.navigate("worker_dashboard")
            }
        )
    }
}

@Composable
fun TaskSummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PaymentDetailRow(
    label: String,
    amount: Double,
    isHighlighted: Boolean = false,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = color.copy(alpha = if (isHighlighted) 1f else 0.8f),
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        
        Text(
            text = "KES ${String.format("%.2f", amount)}",
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
fun PaymentSuccessDialog(
    amount: Double,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment Successful!")
            }
        },
        text = {
            Column {
                Text("Congratulations! Your task has been completed successfully.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Payment Details:",
                    fontWeight = FontWeight.Bold
                )
                Text("• Task Payment: KES $amount")
                Text("• Platform Fee: KES ${String.format("%.2f", amount * 0.05)}")
                Text("• Net Amount: KES ${String.format("%.2f", amount * 0.95)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The payment has been transferred to your account and should be available within 24 hours.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

enum class PaymentCompletionStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}
