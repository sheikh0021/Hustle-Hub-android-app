package com.demoapp.feature_jobs.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGuidanceScreen(
    navController: NavController
) {
    var currentStep by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isTyping by remember { mutableStateOf(false) }

    val paymentSteps = listOf(
        PaymentStep(
            title = "Welcome to Payment Guidance",
            message = "Hi! I'm your payment assistant. I'll guide you through the payment process step by step. Are you ready to begin?",
            options = listOf("Yes, let's start", "I need help first")
        ),
        PaymentStep(
            title = "Payment Method Selection",
            message = "Great! First, let's choose your preferred payment method. Which option would you like to use?",
            options = listOf("Mobile Money (M-Pesa)", "Bank Transfer", "Credit Card", "Cash on Delivery")
        ),
        PaymentStep(
            title = "Payment Amount Confirmation",
            message = "Please confirm the payment amount for your job posting. The amount is calculated based on your job details.",
            options = listOf("Confirm Amount", "Recalculate", "View Breakdown")
        ),
        PaymentStep(
            title = "Payment Processing",
            message = "Now I'll help you process the payment. Please follow the instructions for your selected payment method.",
            options = listOf("I'm ready to pay", "Need more time", "Change payment method")
        ),
        PaymentStep(
            title = "Payment Verification",
            message = "Excellent! Your payment has been processed. Please wait while we verify the transaction.",
            options = listOf("Check Status", "View Receipt", "Continue to Job Posting")
        ),
        PaymentStep(
            title = "Payment Complete",
            message = "🎉 Congratulations! Your payment has been successfully verified. Your job is now live and ready to receive applications from workers.",
            options = listOf("View My Job", "Post Another Job", "Go to Dashboard")
        )
    )

    // Initialize with welcome message
    LaunchedEffect(Unit) {
        if (chatMessages.isEmpty()) {
            chatMessages = listOf(
                ChatMessage(
                    text = paymentSteps[0].message,
                    isBot = true,
                    step = 0
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Payment Assistant",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
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

        // Progress indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Payment Progress",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / paymentSteps.size,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Step ${currentStep + 1} of ${paymentSteps.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(chatMessages.reversed()) { message ->
                ChatBubble(
                    message = message,
                    onOptionClick = { option ->
                        handleUserResponse(
                            option = option,
                            currentStep = currentStep,
                            onStepChange = { currentStep = it },
                            onMessagesUpdate = { chatMessages = it },
                            onTypingChange = { isTyping = it },
                            paymentSteps = paymentSteps
                        )
                    }
                )
            }
        }

        // Typing indicator
        if (isTyping) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistant is typing...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("Type your message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isTyping
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            handleUserResponse(
                                option = userInput,
                                currentStep = currentStep,
                                onStepChange = { currentStep = it },
                                onMessagesUpdate = { chatMessages = it },
                                onTypingChange = { isTyping = it },
                                paymentSteps = paymentSteps
                            )
                            userInput = ""
                        }
                    },
                    enabled = !isTyping && userInput.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onOptionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End
    ) {
        if (message.isBot) {
            Icon(
                Icons.Default.Person,
                contentDescription = "AI Assistant",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isBot) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isBot) 4.dp else 16.dp,
                    bottomEnd = if (message.isBot) 16.dp else 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (message.isBot) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimary
                    )
                    
                    // Show options for bot messages
                    if (message.isBot && message.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        message.options.forEach { option ->
                            OutlinedButton(
                                onClick = { onOptionClick(option) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(option)
                            }
                        }
                    }
                }
            }
            
            if (message.isBot) {
                Spacer(modifier = Modifier.width(32.dp))
            }
        }
    }
}

private fun handleUserResponse(
    option: String,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onMessagesUpdate: (List<ChatMessage>) -> Unit,
    onTypingChange: (Boolean) -> Unit,
    paymentSteps: List<PaymentStep>
) {
    // Add user message
    val currentMessages = mutableListOf<ChatMessage>()
    currentMessages.addAll(getCurrentMessages())
    currentMessages.add(ChatMessage(text = option, isBot = false))
    
    onMessagesUpdate(currentMessages)
    onTypingChange(true)
    
    // Simulate bot response (simplified without coroutines for now)
    val nextStep = when (currentStep) {
        0 -> if (option.contains("Yes") || option.contains("start")) 1 else 0
        1 -> 2 // Payment method selected
        2 -> if (option.contains("Confirm")) 3 else 2
        3 -> if (option.contains("ready")) 4 else 3
        4 -> if (option.contains("Continue")) 5 else 4
        5 -> 5 // Complete
        else -> currentStep
    }
    
    onStepChange(nextStep)
    
    // Add bot response
    val updatedMessages = mutableListOf<ChatMessage>()
    updatedMessages.addAll(currentMessages)
    updatedMessages.add(
        ChatMessage(
            text = paymentSteps[nextStep].message,
            isBot = true,
            step = nextStep,
            options = paymentSteps[nextStep].options
        )
    )
    
    onMessagesUpdate(updatedMessages)
    onTypingChange(false)
}

private fun getCurrentMessages(): List<ChatMessage> {
    // This would typically come from a state management solution
    return emptyList()
}

data class ChatMessage(
    val text: String,
    val isBot: Boolean,
    val step: Int = 0,
    val options: List<String> = emptyList()
)

data class PaymentStep(
    val title: String,
    val message: String,
    val options: List<String>
)
