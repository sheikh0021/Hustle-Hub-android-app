package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQRCodeScreen(
    navController: NavController,
    taskId: String = "task_123" // This would come from navigation arguments
) {
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PENDING) }
    var qrCodeSubmitted by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Payment",
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Payment Summary
            PaymentSummaryCard()
            
            // Payment Instructions
            PaymentInstructionsCard(
                onShowQRCode = { showQRCodeDialog = true }
            )
            
            // QR Code Submission
            if (!qrCodeSubmitted) {
                QRCodeSubmissionCard(
                    onSubmitQRCode = { 
                        qrCodeSubmitted = true
                        paymentStatus = PaymentStatus.SUBMITTED
                    }
                )
            } else {
                PaymentStatusCard(
                    status = paymentStatus,
                    onRefreshStatus = {
                        // Simulate status check
                        paymentStatus = when (paymentStatus) {
                            PaymentStatus.SUBMITTED -> PaymentStatus.CONFIRMED
                            PaymentStatus.CONFIRMED -> PaymentStatus.CONFIRMED
                            PaymentStatus.PENDING -> PaymentStatus.PENDING
                        }
                    }
                )
            }
            
            // CC Team Confirmation Process
            CCTeamConfirmationCard(status = paymentStatus)
            
            // Next Steps
            if (paymentStatus == PaymentStatus.CONFIRMED) {
                NextStepsCard(
                    onProceedToTask = {
                        // Navigate to task management or back to dashboard
                        navController.navigate("client_dashboard")
                    }
                )
            }
        }
    }
    
    // QR Code Display Dialog
    if (showQRCodeDialog) {
        QRCodeDisplayDialog(
            onDismiss = { showQRCodeDialog = false }
        )
    }
}

enum class PaymentStatus {
    PENDING,
    SUBMITTED,
    CONFIRMED
}

@Composable
fun PaymentSummaryCard() {
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
                text = "Payment Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PaymentDetailRow("Task ID", "TASK_123456")
            PaymentDetailRow("Task Title", "Buy groceries from supermarket")
            PaymentDetailRow("Total Amount", "KES 2,500.00")
            PaymentDetailRow("Payment Type", "Temporary Payment")
            PaymentDetailRow("Status", "Pending Payment")
        }
    }
}

@Composable
fun PaymentDetailRow(label: String, value: String) {
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
fun PaymentInstructionsCard(onShowQRCode: () -> Unit) {
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
                text = "Payment Instructions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "To complete your task creation, you need to make a temporary payment:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Payment steps
            PaymentStep(
                stepNumber = 1,
                title = "Make Payment",
                description = "Use your preferred payment method to pay KES 2,500.00"
            )
            
            PaymentStep(
                stepNumber = 2,
                title = "Get QR Code",
                description = "Receive a QR code confirmation from your payment provider"
            )
            
            PaymentStep(
                stepNumber = 3,
                title = "Submit QR Code",
                description = "Submit the QR code for our CC team to verify"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onShowQRCode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Payment QR Code")
            }
        }
    }
}

@Composable
fun PaymentStep(stepNumber: Int, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QRCodeSubmissionCard(onSubmitQRCode: () -> Unit) {
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
                text = "Submit QR Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Upload or scan the QR code from your payment confirmation to submit for verification.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement camera scan */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan QR")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Implement file upload */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSubmitQRCode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Submit QR Code")
            }
        }
    }
}

@Composable
fun PaymentStatusCard(
    status: PaymentStatus,
    onRefreshStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (status) {
                    PaymentStatus.SUBMITTED -> Icons.Default.Star
                    PaymentStatus.CONFIRMED -> Icons.Default.CheckCircle
                    else -> Icons.Default.Star
                },
                contentDescription = "Status",
                modifier = Modifier.size(48.dp),
                tint = when (status) {
                    PaymentStatus.SUBMITTED -> MaterialTheme.colorScheme.tertiary
                    PaymentStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when (status) {
                    PaymentStatus.SUBMITTED -> "QR Code Submitted"
                    PaymentStatus.CONFIRMED -> "Payment Confirmed"
                    else -> "Payment Pending"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (status) {
                    PaymentStatus.SUBMITTED -> MaterialTheme.colorScheme.onTertiaryContainer
                    PaymentStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = when (status) {
                    PaymentStatus.SUBMITTED -> "Waiting for CC team verification"
                    PaymentStatus.CONFIRMED -> "Payment has been verified and confirmed"
                    else -> "Please complete the payment process"
                },
                fontSize = 14.sp,
                color = when (status) {
                    PaymentStatus.SUBMITTED -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    PaymentStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center
            )
            
            if (status == PaymentStatus.SUBMITTED) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRefreshStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Status")
                }
            }
        }
    }
}

@Composable
fun CCTeamConfirmationCard(status: PaymentStatus) {
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
                text = "CC Team Confirmation Process",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Our Customer Care team will manually verify your payment:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CCConfirmationStep(
                step = "1. QR Code Review",
                description = "CC team reviews the submitted QR code",
                isCompleted = status != PaymentStatus.PENDING
            )
            
            CCConfirmationStep(
                step = "2. Payment Verification",
                description = "Payment is verified against our records",
                isCompleted = status == PaymentStatus.CONFIRMED
            )
            
            CCConfirmationStep(
                step = "3. Task Activation",
                description = "Task is activated and workers can be assigned",
                isCompleted = status == PaymentStatus.CONFIRMED
            )
        }
    }
}

@Composable
fun CCConfirmationStep(step: String, description: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
            contentDescription = "Status",
            modifier = Modifier.size(20.dp),
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun NextStepsCard(onProceedToTask: () -> Unit) {
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
                text = "Next Steps",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Your payment has been confirmed! Your task will now be visible to workers and they can start accepting it.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onProceedToTask,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Back to Dashboard")
            }
        }
    }
}

@Composable
fun QRCodeDisplayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Payment QR Code",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dummy QR Code Image
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dummy QR Code Pattern
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Create a simple QR-like pattern
                        repeat(25) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(25) { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if ((row + col) % 3 == 0) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = RoundedCornerShape(1.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Amount: KES 2,500.00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Task ID: TASK_123456",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
