package com.demoapp.feature_chatbot.presentation.models

import android.graphics.Bitmap

data class ChatMessage(
    val text: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBitmap: Bitmap? = null
)

enum class SenderType {
    USER, BOT
}

data class JobQuestion(
    val id: String,
    val text: String,
    val isRequired: Boolean = true
)

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val jobType: String = "",
    val isUploadingImage: Boolean = false,
    val uploadedImage: Bitmap? = null,
    val showUploadSection: Boolean = false,
    val showQuickReplies: Boolean = false,
    val quickReplies: List<String> = emptyList(),
    val showUploadSuccess: Boolean = false,
    val currentStep: Int = 1
)
