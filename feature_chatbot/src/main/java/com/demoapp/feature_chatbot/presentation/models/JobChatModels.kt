package com.demoapp.feature_chatbot.presentation.models

import android.graphics.Bitmap

data class JobChatMessage(
    val id: String,
    val text: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBitmap: Bitmap? = null
)

data class JobChatRoom(
    val jobTitle: String,
    val messages: List<JobChatMessage> = emptyList()
)

data class JobChatUiState(
    val messages: List<JobChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val isLoading: Boolean = false
)
