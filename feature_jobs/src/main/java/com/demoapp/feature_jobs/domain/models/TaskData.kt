package com.demoapp.feature_jobs.domain.models

import java.util.Date
import java.util.UUID

data class TaskData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val refinedDescription: String = "", // AI-refined description
    val category: TaskCategory,
    val status: TaskStatus,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val location: TaskLocation,
    val goods: List<GoodsItem>,
    val budget: Double,
    val paymentStatus: PaymentStatus,
    val jobRequesterId: String,
    val assignedWorkerId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val deadline: Date? = null,
    val completionDate: Date? = null,
    val rating: Float? = null,
    val feedback: String? = null
)

data class GoodsItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String,
    val description: String,
    val quantity: Int,
    val price: Double,
    val category: String,
    val specifications: Map<String, String> = emptyMap(),
    val images: List<String> = emptyList()
)

data class TaskLocation(
    val storeLocation: LocationDetails,
    val deliveryLocation: LocationDetails,
    val distance: Double? = null,
    val estimatedDeliveryTime: Int? = null // in minutes
)

data class LocationDetails(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String = "Kenya",
    val landmark: String? = null
)

enum class TaskCategory {
    SHOPPING,
    DELIVERY,
    SURVEY,
    PHOTOGRAPHY,
    OTHER
}

enum class TaskStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAYMENT_VERIFIED,
    AVAILABLE,
    ASSIGNED,
    IN_PROGRESS,
    AWAITING_APPROVAL,
    COMPLETED,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class PaymentStatus {
    PENDING,
    SUBMITTED,
    VERIFIED,
    CONFIRMED,
    FAILED
}

data class WorkerData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String? = null,
    val rating: Float = 0f,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val location: LocationDetails,
    val skills: List<String> = emptyList(),
    val availability: WorkerAvailability,
    val earnings: Double = 0.0,
    val joinedDate: Date = Date(),
    val isActive: Boolean = true,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING
)

enum class WorkerAvailability {
    AVAILABLE,
    BUSY,
    OFFLINE
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}

data class JobRequesterData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String? = null,
    val location: LocationDetails,
    val totalTasksPosted: Int = 0,
    val completedTasks: Int = 0,
    val rating: Float = 0f,
    val joinedDate: Date = Date(),
    val isActive: Boolean = true
)

data class TaskProgress(
    val taskId: String,
    val workerId: String,
    val status: TaskStatus,
    val currentStep: WorkerStep,
    val photos: List<String> = emptyList(),
    val messages: List<TaskMessage> = emptyList(),
    val locationUpdates: List<LocationUpdate> = emptyList(),
    val lastUpdated: Date = Date()
)

enum class WorkerStep {
    NOTIFICATION_RECEIVED,
    TASK_ACCEPTED,
    EN_ROUTE_TO_STORE,
    AT_STORE,
    GOODS_CONFIRMED,
    PURCHASE_COMPLETED,
    EN_ROUTE_TO_DELIVERY,
    DELIVERY_COMPLETED,
    TASK_COMPLETED
}

data class TaskMessage(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val senderId: String,
    val senderType: MessageSenderType,
    val message: String,
    val timestamp: Date = Date(),
    val attachments: List<String> = emptyList(),
    val isRead: Boolean = false
)

enum class MessageSenderType {
    JOB_REQUESTER,
    WORKER,
    SYSTEM
}

data class LocationUpdate(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val workerId: String,
    val location: LocationDetails,
    val timestamp: Date = Date(),
    val step: WorkerStep
)

data class PaymentTransaction(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val jobRequesterId: String,
    val amount: Double,
    val status: PaymentStatus,
    val qrCodeImage: String? = null,
    val verificationNotes: String? = null,
    val verifiedBy: String? = null,
    val verifiedAt: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class WorkerEarning(
    val id: String = UUID.randomUUID().toString(),
    val workerId: String,
    val taskId: String,
    val amount: Double,
    val status: EarningStatus,
    val transferDate: Date? = null,
    val transferReference: String? = null,
    val createdAt: Date = Date()
)

enum class EarningStatus {
    PENDING,
    PROCESSING,
    TRANSFERRED,
    FAILED
}
