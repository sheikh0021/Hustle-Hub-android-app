package com.demoapp.feature_chatbot.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.feature_chatbot.presentation.models.ChatMessage
import com.demoapp.feature_chatbot.presentation.models.ChatbotUiState
import com.demoapp.feature_chatbot.presentation.models.JobQuestion
import com.demoapp.feature_chatbot.presentation.models.SenderType
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.WorkflowStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

class ChatbotViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()
    
    private val jobQuestions = mapOf(
        "Shopping" to listOf(
            JobQuestion("title", "🛒 Great! Let's create your shopping job. What items do you need me to buy? Please list them clearly."),
            JobQuestion("description", "📝 Please provide more details about the items (brands, sizes, quantities, any specific requirements)."),
            JobQuestion("budget", "💰 What's your budget for this shopping task? (e.g., 50,000 TZS)"),
            JobQuestion("delivery_address", "📍 Where should I deliver the items? Please provide the complete delivery address."),
            JobQuestion("payment_proof", "💳 Please upload a screenshot of your payment confirmation to proceed with posting this job.")
        ),
        "Delivery" to listOf(
            JobQuestion("title", "📦 Perfect! Let's set up your delivery job. What needs to be delivered? Please describe the parcel/item."),
            JobQuestion("description", "📋 Please provide details about the delivery item (size, weight, special handling requirements, fragile items, etc.)."),
            JobQuestion("budget", "💰 What's your budget for this delivery? (e.g., 25,000 TZS)"),
            JobQuestion("pickup_address", "📍 Where should I pick up the item from? Please provide the complete pickup address."),
            JobQuestion("dropoff_address", "🎯 Where should I deliver it to? Please provide the complete delivery address."),
            JobQuestion("payment_proof", "💳 Please upload a screenshot of your payment confirmation to proceed with posting this job.")
        ),
        "Survey" to listOf(
            JobQuestion("title", "📊 Excellent! Let's create your survey job. What type of survey do you need? Please describe the survey topic."),
            JobQuestion("description", "📝 Please describe the survey requirements and objectives in detail."),
            JobQuestion("budget", "💰 What's your budget for this survey? (e.g., 100,000 TZS)"),
            JobQuestion("target_area", "🎯 What is the target area for this survey? (location, demographics, age group, etc.)"),
            JobQuestion("survey_questions", "❓ Please provide the survey questions or upload a survey form. You can also provide a link to an external survey."),
            JobQuestion("payment_proof", "💳 Please upload a screenshot of your payment confirmation to proceed with posting this job.")
        )
    )
    
    private val quickReplyOptions = mapOf(
        "Shopping" to listOf(
            "Groceries", "Clothing", "Electronics", "Medicines", "Custom List"
        ),
        "Delivery" to listOf(
            "Documents", "Food", "Parcel", "Gift", "Other"
        ),
        "Survey" to listOf(
            "Market Research", "Customer Feedback", "Product Testing", "Social Survey", "Custom Survey"
        )
    )
    
    private val questionOptions = mapOf(
        "Shopping" to mapOf(
            "title" to listOf("Groceries", "Clothing", "Electronics", "Medicines", "Custom List"),
            "description" to listOf("Standard items", "Specific brands", "Organic products", "Bulk quantities", "Custom requirements"),
            "budget" to listOf("10,000 TZS", "25,000 TZS", "50,000 TZS", "100,000 TZS", "Custom amount"),
            "delivery_address" to listOf("Downtown", "Uptown", "Suburbs", "Airport area", "Custom address")
        ),
        "Delivery" to mapOf(
            "title" to listOf("Documents", "Food", "Parcel", "Gift", "Other"),
            "description" to listOf("Standard delivery", "Fragile items", "Urgent delivery", "Large package", "Custom requirements"),
            "budget" to listOf("5,000 TZS", "15,000 TZS", "30,000 TZS", "50,000 TZS", "Custom amount"),
            "pickup_address" to listOf("Downtown", "Uptown", "Suburbs", "Airport area", "Custom address"),
            "dropoff_address" to listOf("Downtown", "Uptown", "Suburbs", "Airport area", "Custom address")
        ),
        "Survey" to mapOf(
            "title" to listOf("Market Research", "Customer Feedback", "Product Testing", "Social Survey", "Custom Survey"),
            "description" to listOf("Online survey", "In-person interviews", "Phone surveys", "Focus groups", "Custom method"),
            "budget" to listOf("20,000 TZS", "50,000 TZS", "100,000 TZS", "200,000 TZS", "Custom amount"),
            "target_area" to listOf("Downtown", "Uptown", "Suburbs", "University area", "Custom location"),
            "survey_questions" to listOf("Use provided questions", "Create custom questions", "Use external link", "Phone interview format", "Custom format")
        )
    )
    
    private val jobAnswers = mutableMapOf<String, String>()
    private var currentJobType = ""
    
    init {
        startConversation()
    }
    
    private fun startConversation() {
        val welcomeMessage = ChatMessage(
            text = "Hi! I'm your AI job assistant. I can help you create a job posting. What type of job would you like to post today?",
            sender = SenderType.BOT
        )
        
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage),
            showQuickReplies = true,
            quickReplies = listOf("Shopping", "Delivery", "Survey"),
            currentStep = 1
        )
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        val userMessage = ChatMessage(
            text = message,
            sender = SenderType.USER
        )
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isTyping = true,
            showQuickReplies = false,
            quickReplies = emptyList(),
            currentStep = _uiState.value.currentStep + 1
        )
        
        viewModelScope.launch {
            delay(1000) // Simulate bot thinking
            handleUserResponse(message)
        }
    }
    
    fun sendQuickReply(reply: String) {
        sendMessage(reply)
    }
    
    private suspend fun handleUserResponse(message: String) {
        val currentState = _uiState.value
        
        when {
            currentState.currentQuestionIndex == 0 -> {
                // First response - job type selection
                val jobType = when {
                    message.contains("shopping", ignoreCase = true) -> "Shopping"
                    message.contains("delivery", ignoreCase = true) -> "Delivery"
                    message.contains("survey", ignoreCase = true) -> "Survey"
                    else -> "Custom"
                }
                
                currentJobType = jobType
                jobAnswers["type"] = jobType
                
                val questions = jobQuestions[jobType] ?: emptyList()
                val quickReplies = quickReplyOptions[jobType] ?: emptyList()
                
                if (questions.isNotEmpty()) {
                    val botMessage = ChatMessage(
                        text = questions[0].text,
                        sender = SenderType.BOT
                    )
                    
                    // Get quick reply options for the first question (except budget and payment_proof)
                    val questionOptionsForType = questionOptions[jobType] ?: emptyMap()
                    val firstQuestion = questions[0]
                    val optionsForFirstQuestion = questionOptionsForType[firstQuestion.id] ?: emptyList()
                    val shouldShowOptions = firstQuestion.id != "budget" && firstQuestion.id != "payment_proof" && optionsForFirstQuestion.isNotEmpty()
                    
                    _uiState.value = currentState.copy(
                        messages = currentState.messages + botMessage,
                        currentQuestionIndex = 1,
                        jobType = jobType,
                        isTyping = false,
                        showQuickReplies = shouldShowOptions,
                        quickReplies = if (shouldShowOptions) optionsForFirstQuestion else emptyList()
                    )
                }
            }
            
            else -> {
                // Handle subsequent questions
                val questions = jobQuestions[currentJobType] ?: emptyList()
                val currentQuestion = questions.getOrNull(currentState.currentQuestionIndex - 1)
                
                if (currentQuestion != null) {
                    jobAnswers[currentQuestion.id] = message
                    
                    // Check if this is the payment proof question
                    if (currentQuestion.id == "payment_proof") {
                        val paymentMessage = ChatMessage(
                            text = "Now I need you to upload a screenshot of your payment confirmation to verify your budget. Please use the Camera or Gallery buttons below to upload your payment screenshot. This ensures workers know payment is secured before they accept the job.",
                            sender = SenderType.BOT
                        )
                        
                        _uiState.value = currentState.copy(
                            messages = currentState.messages + paymentMessage,
                            showUploadSection = true,
                            showQuickReplies = true,
                            quickReplies = listOf(
                                "I'll use the Camera button",
                                "I'll use the Gallery button", 
                                "I need to take a screenshot first"
                            ),
                            isTyping = false
                        )
                        return
                    }
                    
                    // Move to next question
                    val nextQuestionIndex = currentState.currentQuestionIndex + 1
                    if (nextQuestionIndex <= questions.size) {
                        val nextQuestion = questions[nextQuestionIndex - 1]
                        val botMessage = ChatMessage(
                            text = nextQuestion.text,
                            sender = SenderType.BOT
                        )
                        
                        // Get quick reply options for this question (except budget and payment_proof)
                        val questionOptionsForType = questionOptions[currentJobType] ?: emptyMap()
                        val optionsForQuestion = questionOptionsForType[nextQuestion.id] ?: emptyList()
                        val shouldShowOptions = nextQuestion.id != "budget" && nextQuestion.id != "payment_proof" && optionsForQuestion.isNotEmpty()
                        
                        _uiState.value = currentState.copy(
                            messages = currentState.messages + botMessage,
                            currentQuestionIndex = nextQuestionIndex,
                            isTyping = false,
                            showQuickReplies = shouldShowOptions,
                            quickReplies = if (shouldShowOptions) optionsForQuestion else emptyList()
                        )
                    } else {
                        // All questions answered, create job
                        createJob()
                    }
                }
            }
        }
    }
    
    fun uploadImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            uploadedImage = bitmap,
            isUploadingImage = true
        )
        
        viewModelScope.launch {
            delay(1500) // Simulate upload time
            _uiState.value = _uiState.value.copy(
                isUploadingImage = false
            )
            
            // Automatically proceed to confirm upload after image is loaded
            delay(500)
            confirmImageUpload()
        }
    }
    
    fun uploadImageFromUri(uri: Uri) {
        // This would need context to load the image
        // For now, we'll just simulate the upload
        _uiState.value = _uiState.value.copy(
            isUploadingImage = true
        )
        
        viewModelScope.launch {
            delay(1500) // Simulate upload time
            _uiState.value = _uiState.value.copy(
                isUploadingImage = false
            )
            
            // Automatically proceed to confirm upload after image is loaded
            delay(500)
            confirmImageUpload()
        }
    }
    
    fun confirmImageUpload() {
        val currentState = _uiState.value
        
        // Add user message about payment proof
        val userMessage = ChatMessage(
            text = "✅ Here's my payment screenshot as requested!",
            sender = SenderType.USER,
            imageBitmap = currentState.uploadedImage
        )
        
        // Add bot confirmation
        val botMessage = ChatMessage(
            text = "Perfect! I can see your payment screenshot has been uploaded successfully. Let me create your job posting now.",
            sender = SenderType.BOT
        )
        
        _uiState.value = currentState.copy(
            messages = currentState.messages + userMessage + botMessage,
            showUploadSection = false,
            showQuickReplies = false,
            quickReplies = emptyList(),
            uploadedImage = null,
            showUploadSuccess = true,
            currentStep = currentState.currentStep + 1
        )
        
        viewModelScope.launch {
            delay(2000) // Show success status for 2 seconds
            _uiState.value = _uiState.value.copy(showUploadSuccess = false)
            delay(1000)
            createJob()
        }
    }
    
    private suspend fun createJob() {
        val jobData = when (currentJobType) {
            "Shopping" -> JobData(
                id = UUID.randomUUID().toString(),
                title = jobAnswers["title"] ?: "Shopping Job",
                description = jobAnswers["description"] ?: "",
                pay = (jobAnswers["budget"]?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0),
                distance = 0.0,
                deadline = "ASAP",
                jobType = currentJobType,
                status = JobStatus.ACTIVE,
                workflowStep = WorkflowStep.POSTED,
                shoppingList = jobAnswers["title"],
                deliveryAddress = jobAnswers["delivery_address"]
            )
            "Delivery" -> JobData(
                id = UUID.randomUUID().toString(),
                title = jobAnswers["title"] ?: "Delivery Job",
                description = jobAnswers["description"] ?: "",
                pay = (jobAnswers["budget"]?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0),
                distance = 0.0,
                deadline = "ASAP",
                jobType = currentJobType,
                status = JobStatus.ACTIVE,
                workflowStep = WorkflowStep.POSTED,
                parcelDescription = jobAnswers["title"],
                pickupAddress = jobAnswers["pickup_address"],
                dropoffAddress = jobAnswers["dropoff_address"]
            )
            "Survey" -> JobData(
                id = UUID.randomUUID().toString(),
                title = jobAnswers["title"] ?: "Survey Job",
                description = jobAnswers["description"] ?: "",
                pay = (jobAnswers["budget"]?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0),
                distance = 0.0,
                deadline = "ASAP",
                jobType = currentJobType,
                status = JobStatus.ACTIVE,
                workflowStep = WorkflowStep.POSTED,
                surveyQuestions = jobAnswers["survey_questions"],
                targetArea = jobAnswers["target_area"]
            )
            else -> JobData(
                id = UUID.randomUUID().toString(),
                title = jobAnswers["title"] ?: "Custom Job",
                description = jobAnswers["description"] ?: "",
                pay = (jobAnswers["budget"]?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0),
                distance = 0.0,
                deadline = "ASAP",
                jobType = currentJobType,
                status = JobStatus.ACTIVE,
                workflowStep = WorkflowStep.POSTED
            )
        }
        
        // Add job to repository
        JobRepositorySingleton.instance.addJob(jobData)
        
        val summary = getJobSummary()
        val botMessage = ChatMessage(
            text = "🎉 Your job has been created successfully!\n\n$summary\n\nYour job is now live and workers can apply for it!",
            sender = SenderType.BOT
        )
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + botMessage,
            isTyping = false,
            currentStep = _uiState.value.currentStep + 1
        )
    }
    
    private fun getJobSummary(): String {
        return buildString {
            appendLine("Job Type: ${jobAnswers["type"]}")
            appendLine("Title: ${jobAnswers["title"]}")
            appendLine("Description: ${jobAnswers["description"]}")
            appendLine("Budget: ${jobAnswers["budget"]}")
            
            when (currentJobType) {
                "Shopping" -> {
                    appendLine("Shopping List: ${jobAnswers["title"]}")
                    appendLine("Delivery Address: ${jobAnswers["delivery_address"]}")
                }
                "Delivery" -> {
                    appendLine("Parcel Description: ${jobAnswers["title"]}")
                    appendLine("Pickup Address: ${jobAnswers["pickup_address"]}")
                    appendLine("Delivery Address: ${jobAnswers["dropoff_address"]}")
                }
                "Survey" -> {
                    appendLine("Survey Topic: ${jobAnswers["title"]}")
                    appendLine("Target Area: ${jobAnswers["target_area"]}")
                    appendLine("Survey Questions: ${jobAnswers["survey_questions"]}")
                }
            }
            
            appendLine("Payment Proof: ✅ Uploaded")
            appendLine("Status: Posted - Waiting for worker to accept")
        }
    }
    
    fun handleCameraClick() {
        // This will be handled by the UI layer
    }
}
