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
    val workflowStep: WorkflowStep = WorkflowStep.REQUEST_POSTED,
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
    
    // Evidence and proof tracking
    val evidenceUploaded: Boolean = false,
    val evidenceFiles: List<String> = emptyList(), // URLs of uploaded evidence files
    val evidenceMessages: List<String> = emptyList(), // Messages with evidence
    val evidencePhotos: List<String> = emptyList(), // Photo URLs
    val evidenceVideos: List<String> = emptyList(), // Video URLs for surveys
    val evidenceDocuments: List<String> = emptyList(), // Document URLs for surveys
    
    // Payment tracking
    val wasshaPaymentProcessed: Boolean = false,
    val paymentProofUploaded: Boolean = false,
    val paymentProofFiles: List<String> = emptyList(), // Payment receipt photos
    val paymentAmount: Double? = null,
    val paymentDate: Date? = null,
    
    // Receipt tracking
    val contractorReceiptConfirmed: Boolean = false,
    val receiptConfirmedDate: Date? = null,
    
    // Workflow completion
    val workflowFinalized: Boolean = false,
    val finalizedDate: Date? = null,

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
    // Request phase
    REQUEST_POSTED,   // Job posted with details, deadline, amount, proof requirements
    
    // Offer phase  
    OFFERS_RECEIVED,  // Contractors have applied/offered services
    
    // Contract phase
    CONTRACT_SELECTED, // Requester selected contractor from offers
    
    // Execution phase
    EXECUTION_STARTED, // Contractor started working
    EXECUTION_IN_PROGRESS, // Contractor is working on the task
    EVIDENCE_UPLOADED, // Contractor uploaded completion evidence
    
    // Completion phase
    COMPLETION_SUBMITTED, // Contractor marked job as completed
    
    // Confirmation phase
    CLIENT_CONFIRMED, // Client confirmed job completion without issues
    
    // Payment phase
    PAYMENT_PROCESSING, // WASSHA processing payment
    PAYMENT_PROOF_UPLOADED, // Client uploaded payment proof
    
    // Receipt phase
    CONTRACTOR_RECEIPT_CONFIRMED, // Contractor confirmed receipt
    
    // Finalized phase
    WORKFLOW_FINALIZED // Complete workflow closure
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
