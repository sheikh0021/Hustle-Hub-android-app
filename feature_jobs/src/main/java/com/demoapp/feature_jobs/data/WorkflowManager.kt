package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.WorkflowStep
import com.demoapp.feature_jobs.presentation.models.JobStatus
import java.util.Date

/**
 * Manages the complete workflow from Request to Finalized
 * Implements: Request → Offer → Contract → Execution → Completion → Confirmation → Payment → Receipt → Finalized
 */
class WorkflowManager {
    
    companion object {
        @Volatile
        private var INSTANCE: WorkflowManager? = null

        fun getInstance(): WorkflowManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkflowManager().also { INSTANCE = it }
            }
        }
    }
    
    private val jobRepository = JobRepositorySingleton.instance
    private val applicationRepository = JobApplicationRepository.getInstance()
    private val notificationRepository = NotificationRepository.getInstance()
    
    /**
     * PHASE 1: REQUEST - Job posted with details, deadline, amount, proof requirements
     */
    fun processRequestPosted(job: JobData): JobData {
        return job.copy(
            workflowStep = WorkflowStep.REQUEST_POSTED,
            status = JobStatus.ACTIVE
        )
    }
    
    /**
     * PHASE 2: OFFER - Contractors have applied/offered services
     */
    fun processOffersReceived(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.OFFERS_RECEIVED
        )
    }
    
    /**
     * PHASE 3: CONTRACT - Requester selected contractor from offers
     */
    fun processContractSelected(jobId: String, workerId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        if (job != null) {
            // Update job with selected worker
            val updatedJob = job.copy(
                workflowStep = WorkflowStep.CONTRACT_SELECTED,
                workerId = workerId,
                status = JobStatus.IN_PROGRESS
            )
            
            // Update job in repository
            // TODO: Implement updateJob method in JobRepository
            
            // Notify contractor
            notificationRepository.createJobSelectionNotification(
                jobId = jobId,
                jobTitle = job.title,
                workerId = workerId,
                workerName = "Selected Contractor"
            )
            
            return updatedJob
        }
        return null
    }
    
    /**
     * PHASE 4: EXECUTION - Contractor started working
     */
    fun processExecutionStarted(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.EXECUTION_STARTED
        )
    }
    
    /**
     * PHASE 4: EXECUTION - Contractor is working on the task
     */
    fun processExecutionInProgress(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.EXECUTION_IN_PROGRESS
        )
    }
    
    /**
     * PHASE 4: EXECUTION - Contractor uploaded completion evidence
     */
    fun processEvidenceUploaded(
        jobId: String, 
        evidenceFiles: List<String> = emptyList(),
        evidenceMessages: List<String> = emptyList(),
        evidencePhotos: List<String> = emptyList(),
        evidenceVideos: List<String> = emptyList(),
        evidenceDocuments: List<String> = emptyList()
    ): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.EVIDENCE_UPLOADED,
            evidenceUploaded = true,
            evidenceFiles = evidenceFiles,
            evidenceMessages = evidenceMessages,
            evidencePhotos = evidencePhotos,
            evidenceVideos = evidenceVideos,
            evidenceDocuments = evidenceDocuments
        )
    }
    
    /**
     * PHASE 5: COMPLETION - Contractor marked job as completed
     */
    fun processCompletionSubmitted(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.COMPLETION_SUBMITTED,
            isCompleted = true,
            completedAt = Date()
        )
    }
    
    /**
     * PHASE 6: CONFIRMATION - Client confirmed job completion without issues
     */
    fun processClientConfirmed(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.CLIENT_CONFIRMED,
            clientConfirmed = true
        )
    }
    
    /**
     * PHASE 7: PAYMENT - WASSHA processing payment
     */
    fun processPaymentProcessing(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.PAYMENT_PROCESSING,
            wasshaPaymentProcessed = true,
            paymentAmount = job?.pay,
            paymentDate = Date()
        )
    }
    
    /**
     * PHASE 7: PAYMENT - Client uploaded payment proof
     */
    fun processPaymentProofUploaded(jobId: String, paymentProofFiles: List<String>): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.PAYMENT_PROOF_UPLOADED,
            paymentProofUploaded = true,
            paymentProofFiles = paymentProofFiles
        )
    }
    
    /**
     * PHASE 8: RECEIPT - Contractor confirmed receipt
     */
    fun processContractorReceiptConfirmed(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED,
            contractorReceiptConfirmed = true,
            receiptConfirmedDate = Date()
        )
    }
    
    /**
     * PHASE 9: FINALIZED - Complete workflow closure
     */
    fun processWorkflowFinalized(jobId: String): JobData? {
        val job = jobRepository.getJobById(jobId)
        return job?.copy(
            workflowStep = WorkflowStep.WORKFLOW_FINALIZED,
            workflowFinalized = true,
            finalizedDate = Date(),
            status = JobStatus.COMPLETED
        )
    }
    
    /**
     * Get the next workflow step for a job
     */
    fun getNextWorkflowStep(currentStep: WorkflowStep): WorkflowStep? {
        return when (currentStep) {
            WorkflowStep.REQUEST_POSTED -> WorkflowStep.OFFERS_RECEIVED
            WorkflowStep.OFFERS_RECEIVED -> WorkflowStep.CONTRACT_SELECTED
            WorkflowStep.CONTRACT_SELECTED -> WorkflowStep.EXECUTION_STARTED
            WorkflowStep.EXECUTION_STARTED -> WorkflowStep.EXECUTION_IN_PROGRESS
            WorkflowStep.EXECUTION_IN_PROGRESS -> WorkflowStep.EVIDENCE_UPLOADED
            WorkflowStep.EVIDENCE_UPLOADED -> WorkflowStep.COMPLETION_SUBMITTED
            WorkflowStep.COMPLETION_SUBMITTED -> WorkflowStep.CLIENT_CONFIRMED
            WorkflowStep.CLIENT_CONFIRMED -> WorkflowStep.PAYMENT_PROCESSING
            WorkflowStep.PAYMENT_PROCESSING -> WorkflowStep.PAYMENT_PROOF_UPLOADED
            WorkflowStep.PAYMENT_PROOF_UPLOADED -> WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED
            WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED -> WorkflowStep.WORKFLOW_FINALIZED
            WorkflowStep.WORKFLOW_FINALIZED -> null // End of workflow
        }
    }
    
    /**
     * Check if a workflow step can be advanced
     */
    fun canAdvanceWorkflow(job: JobData): Boolean {
        return when (job.workflowStep) {
            WorkflowStep.REQUEST_POSTED -> true // Can advance when offers are received
            WorkflowStep.OFFERS_RECEIVED -> true // Can advance when contractor is selected
            WorkflowStep.CONTRACT_SELECTED -> true // Can advance when execution starts
            WorkflowStep.EXECUTION_STARTED -> true // Can advance when execution is in progress
            WorkflowStep.EXECUTION_IN_PROGRESS -> job.evidenceUploaded // Can advance when evidence is uploaded
            WorkflowStep.EVIDENCE_UPLOADED -> true // Can advance when completion is submitted
            WorkflowStep.COMPLETION_SUBMITTED -> true // Can advance when client confirms
            WorkflowStep.CLIENT_CONFIRMED -> true // Can advance when payment processing starts
            WorkflowStep.PAYMENT_PROCESSING -> true // Can advance when payment proof is uploaded
            WorkflowStep.PAYMENT_PROOF_UPLOADED -> true // Can advance when contractor confirms receipt
            WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED -> true // Can advance to finalized
            WorkflowStep.WORKFLOW_FINALIZED -> false // End of workflow
        }
    }
    
    /**
     * Get workflow progress percentage
     */
    fun getWorkflowProgress(workflowStep: WorkflowStep): Int {
        return when (workflowStep) {
            WorkflowStep.REQUEST_POSTED -> 10
            WorkflowStep.OFFERS_RECEIVED -> 20
            WorkflowStep.CONTRACT_SELECTED -> 30
            WorkflowStep.EXECUTION_STARTED -> 40
            WorkflowStep.EXECUTION_IN_PROGRESS -> 50
            WorkflowStep.EVIDENCE_UPLOADED -> 60
            WorkflowStep.COMPLETION_SUBMITTED -> 70
            WorkflowStep.CLIENT_CONFIRMED -> 80
            WorkflowStep.PAYMENT_PROCESSING -> 85
            WorkflowStep.PAYMENT_PROOF_UPLOADED -> 90
            WorkflowStep.CONTRACTOR_RECEIPT_CONFIRMED -> 95
            WorkflowStep.WORKFLOW_FINALIZED -> 100
        }
    }
}
