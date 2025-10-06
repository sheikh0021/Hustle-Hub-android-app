package com.demoapp.feature_jobs.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.WorkflowStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ValidationChatMessage(
    val text: String,
    val isBot: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<String> = emptyList(),
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT, WEIGHT_QUESTION, BUDGET_QUESTION, TIMEFRAME_QUESTION, VALIDATION_RESULT
}

data class ValidationChatbotState(
    val messages: List<ValidationChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentStep: ValidationStep = ValidationStep.WELCOME,
    val jobData: JobData? = null,
    val validationComplete: Boolean = false,
    val validationErrors: List<String> = emptyList(),
    val extractedData: MutableMap<String, String> = mutableMapOf()
)

enum class ValidationStep {
    WELCOME,
    WEIGHT_QUESTION,
    BUDGET_QUESTION,
    TIMEFRAME_QUESTION,
    VALIDATION_COMPLETE
}

class JobValidationChatbotViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ValidationChatbotState())
    val uiState: StateFlow<ValidationChatbotState> = _uiState.asStateFlow()
    
    private val jobType: String = ""
    private val initialJobData: JobData? = null
    
    fun initialize(jobType: String, initialJobData: JobData) {
        val welcomeMessage = ValidationChatMessage(
            text = "Hi! I'm here to help you complete your ${jobType.lowercase()} job posting. Let me ask you a few quick questions to ensure everything is set up correctly.",
            isBot = true,
            messageType = MessageType.TEXT
        )
        
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage),
            currentStep = ValidationStep.WELCOME,
            jobData = initialJobData
        )
        
        // Start validation flow
        viewModelScope.launch {
            delay(1500)
            askWeightQuestion(jobType)
        }
    }
    
    fun handleUserResponse(response: String) {
        // Add user message
        val userMessage = ValidationChatMessage(
            text = response,
            isBot = false
        )
        
        val currentState = _uiState.value
        val newMessages = currentState.messages + userMessage
        
        _uiState.value = currentState.copy(
            messages = newMessages,
            isTyping = true
        )
        
        // Process response
        viewModelScope.launch {
            delay(1000)
            processUserResponse(response, currentState.currentStep)
        }
    }
    
    private suspend fun askWeightQuestion(jobType: String) {
        val question = when (jobType.uppercase()) {
            "SHOPPING", "DELIVERY" -> {
                ValidationChatMessage(
                    text = "First, let me ask about the weight of items. What's the maximum weight you expect for this ${jobType.lowercase()} job? (Our recommended limit is ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg to ensure it's manageable for workers)",
                    isBot = true,
                    quickReplies = listOf(
                        "Under 10kg",
                        "10-25kg", 
                        "25-50kg",
                        "Over 50kg",
                        "Not sure"
                    ),
                    messageType = MessageType.WEIGHT_QUESTION
                )
            }
            else -> {
                // For Survey jobs, skip weight question
                askBudgetQuestion(jobType)
                return
            }
        }
        
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            messages = currentState.messages + question,
            isTyping = false,
            currentStep = ValidationStep.WEIGHT_QUESTION
        )
    }
    
    private suspend fun askBudgetQuestion(jobType: String) {
        val question = ValidationChatMessage(
            text = "Now let me ask about the budget. What's your budget for this ${jobType.lowercase()} job? (Our minimum recommended amount is KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT} to ensure fair compensation for workers)",
            isBot = true,
            quickReplies = listOf(
                "KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}",
                "KES 25,000",
                "KES 50,000",
                "KES 100,000",
                "Custom amount"
            ),
            messageType = MessageType.BUDGET_QUESTION
        )
        
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            messages = currentState.messages + question,
            isTyping = false,
            currentStep = ValidationStep.BUDGET_QUESTION
        )
    }
    
    private suspend fun askTimeframeQuestion(jobType: String) {
        val question = ValidationChatMessage(
            text = "Finally, what's your preferred timeframe for completing this ${jobType.lowercase()} job?",
            isBot = true,
            quickReplies = listOf(
                "Same day",
                "Within 24 hours",
                "Within 3 days",
                "Within a week",
                "Flexible"
            ),
            messageType = MessageType.TIMEFRAME_QUESTION
        )
        
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            messages = currentState.messages + question,
            isTyping = false,
            currentStep = ValidationStep.TIMEFRAME_QUESTION
        )
    }
    
    private suspend fun processUserResponse(response: String, currentStep: ValidationStep) {
        val currentState = _uiState.value
        val extractedData = currentState.extractedData.toMutableMap()
        
        when (currentStep) {
            ValidationStep.WEIGHT_QUESTION -> {
                val weight = extractWeightFromResponse(response)
                extractedData["weight"] = weight
                
                // Validate weight
                val weightValue = parseWeight(weight)
                if (weightValue > OfflineJobRepository.MAX_WEIGHT_LIMIT) {
                    val warningMessage = ValidationChatMessage(
                        text = "⚠️ I notice you mentioned ${weight}. Our recommended maximum is ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg to ensure the job is manageable for workers. Would you like to adjust this?",
                        isBot = true,
                        quickReplies = listOf("Yes, adjust to ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg", "No, keep as is"),
                        messageType = MessageType.TEXT
                    )
                    
                    _uiState.value = currentState.copy(
                        messages = currentState.messages + warningMessage,
                        isTyping = false,
                        extractedData = extractedData
                    )
                    return
                }
                
                delay(1000)
                askBudgetQuestion(currentState.jobData?.jobType ?: "")
            }
            
            ValidationStep.BUDGET_QUESTION -> {
                val budget = extractBudgetFromResponse(response)
                extractedData["budget"] = budget
                
                // Validate budget
                val budgetValue = parseBudget(budget)
                if (budgetValue < OfflineJobRepository.MINIMUM_PAY_LIMIT) {
                    val warningMessage = ValidationChatMessage(
                        text = "⚠️ I notice your budget is ${budget}. Our minimum recommended amount is KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT} to ensure fair compensation for workers. Would you like to adjust this?",
                        isBot = true,
                        quickReplies = listOf("Yes, adjust to KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}", "No, keep as is"),
                        messageType = MessageType.TEXT
                    )
                    
                    _uiState.value = currentState.copy(
                        messages = currentState.messages + warningMessage,
                        isTyping = false,
                        extractedData = extractedData
                    )
                    return
                }
                
                delay(1000)
                askTimeframeQuestion(currentState.jobData?.jobType ?: "")
            }
            
            ValidationStep.TIMEFRAME_QUESTION -> {
                val timeframe = response
                extractedData["timeframe"] = timeframe
                
                delay(1000)
                completeValidation(extractedData)
            }
            
            else -> {
                // Handle other responses
                _uiState.value = currentState.copy(isTyping = false)
            }
        }
    }
    
    private suspend fun completeValidation(extractedData: Map<String, String>) {
        val currentState = _uiState.value
        val errors = mutableListOf<String>()
        
        // Perform final validation
        val weight = extractedData["weight"]?.let { parseWeight(it) }
        val budget = extractedData["budget"]?.let { parseBudget(it) }
        
        if (weight != null && weight > OfflineJobRepository.MAX_WEIGHT_LIMIT) {
            errors.add("Weight limit exceeds recommended maximum of ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg")
        }
        
        if (budget != null && budget < OfflineJobRepository.MINIMUM_PAY_LIMIT) {
            errors.add("Budget is below minimum recommended amount of KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}")
        }
        
        val completionMessage = ValidationChatMessage(
            text = if (errors.isEmpty()) {
                "✅ Perfect! Your job posting meets all our requirements:\n\n" +
                "• Weight: ${extractedData["weight"] ?: "N/A"}\n" +
                "• Budget: ${extractedData["budget"] ?: "N/A"}\n" +
                "• Timeframe: ${extractedData["timeframe"] ?: "N/A"}\n\n" +
                "You're all set to proceed with payment!"
            } else {
                "❌ I found some issues that need to be addressed:\n\n" +
                errors.joinToString("\n") { "• $it" } + "\n\n" +
                "Would you like to fix these issues or proceed anyway?"
            },
            isBot = true,
            messageType = MessageType.VALIDATION_RESULT
        )
        
        _uiState.value = currentState.copy(
            messages = currentState.messages + completionMessage,
            isTyping = false,
            currentStep = ValidationStep.VALIDATION_COMPLETE,
            validationComplete = true,
            validationErrors = errors,
            extractedData = extractedData.toMutableMap()
        )
    }
    
    private fun extractWeightFromResponse(response: String): String {
        return when {
            response.contains("under 10", ignoreCase = true) -> "Under 10kg"
            response.contains("10-25", ignoreCase = true) -> "10-25kg"
            response.contains("25-50", ignoreCase = true) -> "25-50kg"
            response.contains("over 50", ignoreCase = true) -> "Over 50kg"
            response.contains("not sure", ignoreCase = true) -> "Not sure"
            else -> response
        }
    }
    
    private fun extractBudgetFromResponse(response: String): String {
        return when {
            response.contains("${OfflineJobRepository.MINIMUM_PAY_LIMIT}", ignoreCase = true) -> "KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}"
            response.contains("25,000", ignoreCase = true) -> "KES 25,000"
            response.contains("50,000", ignoreCase = true) -> "KES 50,000"
            response.contains("100,000", ignoreCase = true) -> "KES 100,000"
            response.contains("custom", ignoreCase = true) -> "Custom amount"
            else -> response
        }
    }
    
    private fun parseWeight(weightStr: String): Double {
        return when {
            weightStr.contains("under 10", ignoreCase = true) -> 5.0
            weightStr.contains("10-25", ignoreCase = true) -> 17.5
            weightStr.contains("25-50", ignoreCase = true) -> 37.5
            weightStr.contains("over 50", ignoreCase = true) -> 75.0
            weightStr.contains("not sure", ignoreCase = true) -> 0.0
            else -> {
                // Try to extract number from string
                val regex = "\\d+(\\.\\d+)?".toRegex()
                val match = regex.find(weightStr)
                match?.value?.toDoubleOrNull() ?: 0.0
            }
        }
    }
    
    private fun parseBudget(budgetStr: String): Double {
        return when {
            budgetStr.contains("${OfflineJobRepository.MINIMUM_PAY_LIMIT}", ignoreCase = true) -> OfflineJobRepository.MINIMUM_PAY_LIMIT
            budgetStr.contains("25,000", ignoreCase = true) -> 25000.0
            budgetStr.contains("50,000", ignoreCase = true) -> 50000.0
            budgetStr.contains("100,000", ignoreCase = true) -> 100000.0
            budgetStr.contains("custom", ignoreCase = true) -> 0.0
            else -> {
                // Try to extract number from string
                val regex = "\\d+(\\.\\d+)?".toRegex()
                val match = regex.find(budgetStr)
                match?.value?.toDoubleOrNull() ?: 0.0
            }
        }
    }
    
    fun proceedWithValidation() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            validationComplete = true
        )
    }
    
    fun restartValidation() {
        _uiState.value = ValidationChatbotState()
    }
}
