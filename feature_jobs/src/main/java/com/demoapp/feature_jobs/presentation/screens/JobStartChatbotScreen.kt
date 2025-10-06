package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.JobStartChatbotRepository
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.presentation.models.ChatbotQuestion
import com.demoapp.feature_jobs.presentation.models.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobStartChatbotScreen(
    jobId: String,
    workerId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val chatbotRepository = JobStartChatbotRepository.getInstance()
    val jobRepository = JobRepositorySingleton.instance
    val job = jobRepository.getJobById(jobId)
    
    var chatbot by remember { mutableStateOf(chatbotRepository.getChatbotForJob(jobId, workerId)) }
    var currentAnswer by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("") }
    var showCompletionDialog by remember { mutableStateOf(false) }
    
    // Start chatbot if not already started
    LaunchedEffect(jobId, workerId) {
        if (chatbot == null && job != null) {
            chatbot = chatbotRepository.startJobChatbot(jobId, workerId, job.jobType)
        }
    }
    
    // Listen for chatbot updates
    val chatbots by chatbotRepository.chatbots.collectAsState()
    LaunchedEffect(chatbots) {
        chatbot = chatbotRepository.getChatbotForJob(jobId, workerId)
        if (chatbot?.isCompleted == true) {
            showCompletionDialog = true
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Job Start Validation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        if (chatbot != null) {
            val currentQuestion = chatbot!!.questions.getOrNull(chatbot!!.currentQuestionIndex)
            
            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (chatbot!!.currentQuestionIndex + 1).toFloat() / chatbot!!.questions.size,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Question ${chatbot!!.currentQuestionIndex + 1} of ${chatbot!!.questions.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Job info
                    job?.let { jobData ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = jobData.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = jobData.jobType,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pay: $${String.format("%.0f", jobData.pay)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    // Current question
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = currentQuestion.question,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Question input based on type
                            when (currentQuestion.questionType) {
                                QuestionType.YES_NO -> {
                                    YesNoQuestionInput(
                                        selectedOption = selectedOption,
                                        onOptionSelected = { selectedOption = it }
                                    )
                                }
                                QuestionType.MULTIPLE_CHOICE -> {
                                    MultipleChoiceQuestionInput(
                                        options = currentQuestion.options,
                                        selectedOption = selectedOption,
                                        onOptionSelected = { selectedOption = it }
                                    )
                                }
                                QuestionType.TEXT_INPUT -> {
                                    TextInputQuestion(
                                        answer = currentAnswer,
                                        onAnswerChange = { currentAnswer = it }
                                    )
                                }
                                QuestionType.NUMBER_INPUT -> {
                                    NumberInputQuestion(
                                        answer = currentAnswer,
                                        onAnswerChange = { currentAnswer = it }
                                    )
                                }
                                QuestionType.LOCATION_INPUT -> {
                                    LocationInputQuestion(
                                        answer = currentAnswer,
                                        onAnswerChange = { currentAnswer = it }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Submit button
                            Button(
                                onClick = {
                                    val answer = when (currentQuestion.questionType) {
                                        QuestionType.YES_NO, QuestionType.MULTIPLE_CHOICE -> selectedOption
                                        else -> currentAnswer
                                    }
                                    
                                    if (answer.isNotEmpty() || !currentQuestion.isRequired) {
                                        chatbotRepository.submitAnswer(chatbot!!.id, currentQuestion.id, answer)
                                        currentAnswer = ""
                                        selectedOption = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = when (currentQuestion.questionType) {
                                    QuestionType.YES_NO, QuestionType.MULTIPLE_CHOICE -> selectedOption.isNotEmpty()
                                    else -> currentAnswer.isNotEmpty() || !currentQuestion.isRequired
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = if (chatbot!!.currentQuestionIndex == chatbot!!.questions.size - 1) "Complete" else "Next",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Completion dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = {
                Text(
                    text = "Job Start Complete!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("You have successfully completed the job start validation. You can now begin working on the job.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletionDialog = false
                        navController.navigate("my_tasks")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Working")
                }
            }
        )
    }
}

@Composable
private fun YesNoQuestionInput(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .selectable(
                    selected = selectedOption == "Yes",
                    onClick = { onOptionSelected("Yes") }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedOption == "Yes",
                onClick = { onOptionSelected("Yes") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Yes",
                fontSize = 16.sp
            )
        }
        
        Row(
            modifier = Modifier
                .weight(1f)
                .selectable(
                    selected = selectedOption == "No",
                    onClick = { onOptionSelected("No") }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedOption == "No",
                onClick = { onOptionSelected("No") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MultipleChoiceQuestionInput(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == option,
                        onClick = { onOptionSelected(option) }
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TextInputQuestion(
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter your answer...") },
        maxLines = 3,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun NumberInputQuestion(
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter number...") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun LocationInputQuestion(
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter location or use GPS...") },
        shape = RoundedCornerShape(8.dp)
    )
}
