package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class NotificationData(
    val id: String = "",
    val title: String,
    val message: String,
    val type: NotificationType,
    val recipientId: String, // Job poster's ID
    val senderId: String? = null, // Worker's ID (if applicable)
    val jobId: String? = null, // Related job ID
    val isRead: Boolean = false,
    val createdAt: Date = Date(),
    val actionRequired: Boolean = false,
    val actionType: NotificationActionType? = null
)

enum class NotificationType {
    JOB_APPLICATION,    // Worker applied to job
    JOB_SELECTED,       // Worker was selected for job
    JOB_REJECTED,       // Worker was rejected for job
    JOB_COMPLETED,      // Job was completed
    JOB_CANCELLED,      // Job was cancelled
    PAYMENT_RECEIVED,   // Payment was received
    MESSAGE_RECEIVED,   // New message received
    SYSTEM_UPDATE,      // System/App updates
    INVOICE_CREATED,    // Invoice was created for completed job
    JOB_START_REQUIRED  // Worker needs to start job with chatbot validation
}

enum class NotificationActionType {
    VIEW_APPLICANTS,    // Go to applicants screen
    VIEW_JOB,          // Go to job details
    VIEW_MESSAGE,      // Go to chat
    VIEW_PAYMENT,      // Go to payment screen
    VIEW_INVOICE,      // Go to invoice details
    VIEW_COMPLETED_JOBS, // Go to completed jobs section
    START_JOB_CHATBOT, // Start job with chatbot validation
    NO_ACTION          // No specific action needed
}
