package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.NotificationData
import com.demoapp.feature_jobs.presentation.models.NotificationType
import com.demoapp.feature_jobs.presentation.models.NotificationActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: NotificationRepository? = null

        fun getInstance(): NotificationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationRepository().also { INSTANCE = it }
            }
        }
    }

    /**
     * Add a new notification
     */
    fun addNotification(notification: NotificationData) {
        val notificationWithId = notification.copy(
            id = if (notification.id.isEmpty()) "notif_${System.currentTimeMillis()}" else notification.id
        )
        
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.add(0, notificationWithId) // Add to top of list
        _notifications.value = currentNotifications
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        val currentNotifications = _notifications.value.toMutableList()
        val notificationIndex = currentNotifications.indexOfFirst { it.id == notificationId }
        
        if (notificationIndex != -1) {
            currentNotifications[notificationIndex] = currentNotifications[notificationIndex].copy(isRead = true)
            _notifications.value = currentNotifications
        }
    }

    /**
     * Mark all notifications as read for a specific user
     */
    fun markAllAsRead(recipientId: String) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.forEachIndexed { index, notification ->
            if (notification.recipientId == recipientId && !notification.isRead) {
                currentNotifications[index] = notification.copy(isRead = true)
            }
        }
        _notifications.value = currentNotifications
    }

    /**
     * Get notifications for a specific user
     */
    fun getNotificationsForUser(userId: String): List<NotificationData> {
        return _notifications.value.filter { it.recipientId == userId }
    }

    /**
     * Get unread notification count for a specific user
     */
    fun getUnreadCount(userId: String): Int {
        return _notifications.value.count { it.recipientId == userId && !it.isRead }
    }

    /**
     * Remove a notification
     */
    fun removeNotification(notificationId: String) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.removeAll { it.id == notificationId }
        _notifications.value = currentNotifications
    }

    /**
     * Clear all notifications for a specific user
     */
    fun clearAllNotifications(userId: String) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.removeAll { it.recipientId == userId }
        _notifications.value = currentNotifications
    }

    /**
     * Create a job application notification
     */
    fun createJobApplicationNotification(
        jobId: String,
        jobTitle: String,
        clientId: String,
        workerId: String,
        workerName: String
    ) {
        val notification = NotificationData(
            title = "New Job Application",
            message = "$workerName applied to your job: \"$jobTitle\"",
            type = NotificationType.JOB_APPLICATION,
            recipientId = clientId,
            senderId = workerId,
            jobId = jobId,
            actionRequired = true,
            actionType = NotificationActionType.VIEW_APPLICANTS
        )
        addNotification(notification)
    }

    /**
     * Create a job selection notification
     */
    fun createJobSelectionNotification(
        jobId: String,
        jobTitle: String,
        workerId: String,
        workerName: String
    ) {
        val notification = NotificationData(
            title = "Job Application Accepted",
            message = "You have been selected for the job: \"$jobTitle\"",
            type = NotificationType.JOB_SELECTED,
            recipientId = workerId,
            jobId = jobId,
            actionRequired = true,
            actionType = NotificationActionType.VIEW_JOB
        )
        addNotification(notification)
    }

    /**
     * Create a job rejection notification
     */
    fun createJobRejectionNotification(
        jobId: String,
        jobTitle: String,
        workerId: String
    ) {
        val notification = NotificationData(
            title = "Job Application Update",
            message = "Your application for \"$jobTitle\" was not selected this time",
            type = NotificationType.JOB_REJECTED,
            recipientId = workerId,
            jobId = jobId,
            actionRequired = false,
            actionType = NotificationActionType.NO_ACTION
        )
        addNotification(notification)
    }

    /**
     * Create an invoice notification for job poster
     */
    fun createInvoiceNotification(
        jobId: String,
        jobTitle: String,
        clientId: String,
        workerId: String,
        workerName: String,
        invoiceId: String,
        amountPaid: String
    ) {
        val notification = NotificationData(
            title = "Invoice Created",
            message = "Invoice #$invoiceId has been created for your completed job: \"$jobTitle\". Amount: $${amountPaid}",
            type = NotificationType.INVOICE_CREATED,
            recipientId = clientId,
            senderId = workerId,
            jobId = jobId,
            actionRequired = true,
            actionType = NotificationActionType.VIEW_COMPLETED_JOBS,
            isRead = false
        )
        addNotification(notification)
        println("DEBUG: Invoice notification created for client $clientId: Invoice #$invoiceId for job $jobTitle")
    }

    /**
     * Create a job start notification for selected worker
     */
    fun createJobStartNotification(
        jobId: String,
        jobTitle: String,
        workerId: String,
        workerName: String
    ) {
        val notification = NotificationData(
            title = "Start Your Job",
            message = "You've been selected for \"$jobTitle\". Please complete the job start validation to begin working.",
            type = NotificationType.JOB_START_REQUIRED,
            recipientId = workerId,
            jobId = jobId,
            actionRequired = true,
            actionType = NotificationActionType.START_JOB_CHATBOT,
            isRead = false
        )
        addNotification(notification)
        println("DEBUG: Job start notification created for worker $workerId: $jobTitle")
    }

    /**
     * Initialize with sample notifications for demo purposes
     */
    fun initializeSampleNotifications() {
        val sampleNotifications = listOf(
            // Client notifications
            NotificationData(
                id = "notif_1",
                title = "New Job Application",
                message = "John Mwangi applied to your job: \"Grocery Shopping\"",
                type = NotificationType.JOB_APPLICATION,
                recipientId = "client_mary_johnson", // Specific client who posted grocery job
                senderId = "worker_1",
                jobId = "sample_grocery_1",
                actionRequired = true,
                actionType = NotificationActionType.VIEW_APPLICANTS,
                isRead = false
            ),
            NotificationData(
                id = "notif_2",
                title = "New Job Application",
                message = "Sarah Wanjiku applied to your job: \"Grocery Shopping\"",
                type = NotificationType.JOB_APPLICATION,
                recipientId = "client_mary_johnson", // Same client who posted grocery job
                senderId = "worker_2",
                jobId = "sample_grocery_1",
                actionRequired = true,
                actionType = NotificationActionType.VIEW_APPLICANTS,
                isRead = false
            ),
            NotificationData(
                id = "notif_3",
                title = "New Job Application",
                message = "Peter Kimani applied to your job: \"Package Delivery\"",
                type = NotificationType.JOB_APPLICATION,
                recipientId = "client_david_wilson", // Specific client who posted delivery job
                senderId = "worker_3",
                jobId = "sample_delivery_1",
                actionRequired = true,
                actionType = NotificationActionType.VIEW_APPLICANTS,
                isRead = true
            ),
            
            // Worker notifications
            NotificationData(
                id = "worker_notif_1",
                title = "Job Application Accepted",
                message = "You have been selected for the job: \"Grocery Shopping\"",
                type = NotificationType.JOB_SELECTED,
                recipientId = "worker_1",
                jobId = "sample_grocery_1",
                actionRequired = true,
                actionType = NotificationActionType.VIEW_JOB,
                isRead = false
            ),
            NotificationData(
                id = "worker_notif_2",
                title = "Start Your Job",
                message = "You've been selected for \"Grocery Shopping\". Please complete the job start validation to begin working.",
                type = NotificationType.JOB_START_REQUIRED,
                recipientId = "worker_1",
                jobId = "sample_grocery_1",
                actionRequired = true,
                actionType = NotificationActionType.START_JOB_CHATBOT,
                isRead = false
            ),
            NotificationData(
                id = "worker_notif_3",
                title = "Job Application Update",
                message = "Your application for \"Package Delivery\" was not selected this time",
                type = NotificationType.JOB_REJECTED,
                recipientId = "worker_2",
                jobId = "sample_delivery_1",
                actionRequired = false,
                actionType = NotificationActionType.NO_ACTION,
                isRead = true
            ),
            NotificationData(
                id = "worker_notif_4",
                title = "New Message",
                message = "You have a new message from the client for job: \"House Cleaning\"",
                type = NotificationType.MESSAGE_RECEIVED,
                recipientId = "worker_3",
                jobId = "sample_cleaning_1",
                actionRequired = true,
                actionType = NotificationActionType.VIEW_MESSAGE,
                isRead = false
            )
        )
        
        _notifications.value = sampleNotifications
        println("DEBUG: Initialized ${sampleNotifications.size} sample notifications (${sampleNotifications.count { it.recipientId.startsWith("client_") }} for clients, ${sampleNotifications.count { it.recipientId.startsWith("worker_") }} for workers)")
    }
}
