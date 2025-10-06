package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.WorkflowStep
import com.demoapp.feature_jobs.presentation.viewmodels.JobValidationChatbotViewModel
import com.demoapp.feature_jobs.presentation.viewmodels.ValidationChatMessage
import com.demoapp.feature_jobs.presentation.viewmodels.ValidationChatbotState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobValidationChatbotScreen(
    navController: NavController,
    jobType: String,
    initialJobData: JobData
) {
    val viewModel = remember { JobValidationChatbotViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Initialize chatbot
    LaunchedEffect(Unit) {
        viewModel.initialize(jobType, initialJobData)
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Job Validation Assistant",
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
        
        // Chat Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.messages) { message ->
                ChatMessageBubble(message = message, onQuickReplyClick = { reply ->
                    viewModel.handleUserResponse(reply)
                })
            }
            
            // Typing indicator
            if (uiState.isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        // Input Area
        if (!uiState.validationComplete) {
            ChatInputArea(
                currentInput = currentInput,
                onInputChange = { currentInput = it },
                onSendClick = {
                    if (currentInput.isNotBlank()) {
                        viewModel.handleUserResponse(currentInput)
                        currentInput = ""
                    }
                }
            )
        } else {
            // Validation Complete - Show Results
            ValidationCompleteCard(
                jobData = uiState.jobData,
                errors = uiState.validationErrors,
                onProceed = {
                    // Navigate to WhatsApp payment screen
                    val taskId = uiState.jobData?.id ?: "default_task"
                    navController.navigate("payment_qr/$taskId") {
                        // Clear the back stack to prevent going back to validation
                        popUpTo("client_dashboard") { inclusive = false }
                    }
                },
                onFixErrors = {
                    // Restart validation
                    viewModel.restartValidation()
                }
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ValidationChatMessage,
    onQuickReplyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End
    ) {
        if (message.isBot) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    
                    // Quick Replies
                    if (message.quickReplies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(message.quickReplies) { reply ->
                                QuickReplyButton(
                                    text = reply,
                                    onClick = { onQuickReplyClick(reply) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun QuickReplyButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(2.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Typing...",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your answer...") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ValidationCompleteCard(
    jobData: JobData?,
    errors: List<String>,
    onProceed: () -> Unit,
    onFixErrors: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (errors.isEmpty()) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (errors.isEmpty()) "✅ Validation Complete!" else "❌ Validation Issues Found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (errors.isEmpty()) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (errors.isEmpty()) {
                Text(
                    text = "Your job posting meets all requirements and is ready to be published!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onProceed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proceed to Payment")
                }
            } else {
                Text(
                    text = "Please fix the following issues:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                errors.forEach { error ->
                    Text(
                        text = "• $error",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onFixErrors,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fix Issues")
                    }
                    
                    Button(
                        onClick = onProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Proceed Anyway")
                    }
                }
            }
        }
    }
}

