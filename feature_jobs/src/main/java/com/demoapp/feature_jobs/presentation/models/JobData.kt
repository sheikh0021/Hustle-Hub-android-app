package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class JobData(
    val id: String = "",
    val title: String,
    val description: String,
    val pay: Double,
    val distance: Double,
    val deadline: String,
    val jobType: String, // "Shopping", "Delivery", "Survey"
    val clientId: String = "client_id_placeholder",
    val clientPhoneNumber: String = "+254700000000", // Job poster's phone number
    val workerId: String? = null,
    val status: JobStatus = JobStatus.ACTIVE,
    val workflowStep: WorkflowStep = WorkflowStep.POSTED,
    val invoiceCreated: Boolean = false,
    val invoiceId: String? = null,
    val cancellationReason: String? = null,
    val workerAccepted: Boolean = false,

    // Timeline and progress tracking
    val currentTimelineStage: TimelineStage? = null,
    val timelineId: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,

    // Location coordinates for map functionality
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,

    // Shopping specific fields
    val shoppingList: String? = null,
    val deliveryAddress: String? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,

    // Delivery specific fields
    val pickupAddress: String? = null,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val dropoffAddress: String? = null,
    val dropoffLat: Double? = null,
    val dropoffLng: Double? = null,
    val parcelDescription: String? = null,

    // Survey specific fields
    val surveyQuestions: String? = null,
    val targetArea: String? = null,
    val surveyLat: Double? = null,
    val surveyLng: Double? = null,
    val surveyLink: String? = null,

    // Workflow tracking
    val escrowConfirmed: Boolean = false,
    val workerConfirmed: Boolean = false,
    val proofUploaded: Boolean = false,
    val deliveryProofUploaded: Boolean = false,
    val clientConfirmed: Boolean = false,

    // New mandatory fields
    val location: String = "", // Required location field
    val brand: String = "", // Required brand field
    val quantity: Int = 1, // Required quantity field
    val price: Double = 0.0, // Required price field
    val substitutes: String = "", // Required substitutes field

    // Enhanced location with landmarks
    val nearbyLandmark: String? = null, // Optional nearby landmark
    val landmarkType: LandmarkType? = null, // Type of landmark

    // Time frames and penalties
    val deliveryTimeFrame: String = "", // Required delivery timeframe
    val latePenalty: Double = 0.0, // Penalty for late completion
    val latePenaltyDescription: String = "", // Description of late penalty

    // Weight limit for jobs
    val maxWeightLimit: Double? = null, // Maximum weight limit in kg
    val weightLimitExceeded: Boolean = false, // Flag if weight limit is exceeded

    // Cancellation tracking
    val cancellationTimestamp: Long? = null, // When job was cancelled
    val cancellationPenalty: Double = 0.0, // Penalty for cancellation
    val cancellationReasonType: CancellationReasonType? = null, // Type of cancellation reason

    // Offline draft support
    val isDraft: Boolean = false, // Whether this is a draft
    val draftTimestamp: Long? = null, // When draft was created
    val needsSync: Boolean = false, // Whether draft needs to be synced

    // Payment guidance
    val paymentGuidanceCompleted: Boolean = false, // Whether payment guidance was completed
    val paymentGuidanceStep: Int = 0 // Current step in payment guidance
)

enum class JobStatus {
    ACTIVE,
    APPLIED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    DRAFT // New status for offline drafts
}

enum class WorkflowStep {
    POSTED,           // Job posted, waiting for worker
    CHAT_INITIATED,   // Worker accepted, chat started
    ESCROW_PENDING,   // Waiting for client to confirm and escrow
    WORKER_CONFIRMED, // Worker confirmed to take task
    IN_PROGRESS,      // Worker is doing the task
    PROOF_UPLOADED,   // Worker uploaded proof
    DELIVERY_PROOF,   // Worker uploaded delivery proof (for shopping/delivery)
    CLIENT_REVIEW,    // Client reviewing submission
    COMPLETED         // Job completed
}

enum class LandmarkType {
    SCHOOL,
    CHURCH,
    MOSQUE,
    BUS_STATION,
    HOSPITAL,
    MALL,
    MARKET,
    OTHER
}

enum class CancellationReasonType {
    TRANSPORT_ISSUES,
    LOW_PAYMENT,
    PERSONAL_CIRCUMSTANCES,
    OTHER
}
