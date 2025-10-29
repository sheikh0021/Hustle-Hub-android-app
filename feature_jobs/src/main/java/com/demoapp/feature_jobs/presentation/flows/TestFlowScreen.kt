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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.demoapp.feature_jobs.data.TaskRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestFlowScreen(navController: NavController) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test All Flows") },
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
            Text(
                text = "Task & Worker Platform - Test All Flows",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Test all implemented flows to ensure they work end-to-end",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Registration Flows
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Registration Flows",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { navController.navigate("register_worker") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Worker Registration")
                        }
                        
                        Button(
                            onClick = { navController.navigate("register_job_request") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Job Requester Registration")
                        }
                    }
                }
            }
            
            // Task Request Flow
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Task Request & Payment Flow",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { navController.navigate("task_request_flow") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Task Request & WhatsApp Payment")
                    }
                }
            }
            
            // Worker Notification Flow
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Worker Notification Flow",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { navController.navigate("worker_notification_flow") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Worker Notifications & Task Acceptance")
                    }
                }
            }
            
            // Task Execution Flows
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Task Execution Flows",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("task_execution_flow/test_task_1") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test General Task Execution")
                        }
                        
                        Button(
                            onClick = { navController.navigate("shopping_flow/test_shopping_task") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Shopping Flow")
                        }
                        
                        Button(
                            onClick = { navController.navigate("delivery_flow/test_delivery_task") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Delivery Flow")
                        }
                    }
                }
            }
            
            // Payment Completion Flow
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Completion Flow",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { navController.navigate("payment_completion/test_payment_task") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Payment Completion & Transfer")
                    }
                }
            }
            
            // End-to-End Test
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "End-to-End Test",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Complete workflow: Registration → Task Request → Payment → Worker Acceptance → Execution → Completion",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            // Initialize sample data for testing
                            taskRepository.initializeSampleData()
                            navController.navigate("task_request_flow")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Start End-to-End Test", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "All flows are now implemented and ready for testing!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
