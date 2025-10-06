package com.demoapp.feature_chatbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.feature_chatbot.presentation.models.JobChatMessage
import com.demoapp.feature_chatbot.presentation.models.JobChatUiState
import com.demoapp.feature_chatbot.presentation.models.SenderType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class JobChatViewModel(
    private val jobTitle: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JobChatUiState())
    val uiState: StateFlow<JobChatUiState> = _uiState.asStateFlow()
    
    init {
        initializeChat()
    }
    
    private fun initializeChat() {
        val welcomeMessage = JobChatMessage(
            id = UUID.randomUUID().toString(),
            text = "Hello! I'm here to help you with your job: $jobTitle. How can I assist you today?",
            sender = SenderType.BOT
        )
        
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage)
        )
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        val userMessage = JobChatMessage(
            id = UUID.randomUUID().toString(),
            text = message,
            sender = SenderType.USER
        )
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isTyping = true
        )
        
        viewModelScope.launch {
            delay(1000) // Simulate bot thinking
            generateBotResponse(message)
        }
    }
    
    private suspend fun generateBotResponse(userMessage: String) {
        val botResponse = when {
            userMessage.contains("status", ignoreCase = true) -> 
                "Your job '$jobTitle' is currently active and receiving applications."
            userMessage.contains("payment", ignoreCase = true) -> 
                "Payment details for '$jobTitle' will be processed upon job completion."
            userMessage.contains("help", ignoreCase = true) -> 
                "I can help you with job status updates, payment information, and general questions about '$jobTitle'."
            else -> 
                "Thank you for your message about '$jobTitle'. I'm here to help with any questions you may have."
        }
        
        val botMessage = JobChatMessage(
            id = UUID.randomUUID().toString(),
            text = botResponse,
            sender = SenderType.BOT
        )
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + botMessage,
            isTyping = false
        )
    }
}
