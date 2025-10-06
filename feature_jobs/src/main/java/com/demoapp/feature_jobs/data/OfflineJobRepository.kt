package com.demoapp.feature_jobs.data

import android.content.Context
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.CancellationReasonType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Enhanced JobRepository with offline draft support and validation
 * Follows MVVM + Clean Architecture principles
 */
class OfflineJobRepository(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val _jobs = MutableStateFlow<List<JobData>>(emptyList())
    val jobs: StateFlow<List<JobData>> = _jobs.asStateFlow()

    private val _drafts = MutableStateFlow<List<JobData>>(emptyList())
    val drafts: StateFlow<List<JobData>> = _drafts.asStateFlow()

    private val _networkStatus = MutableStateFlow(true)
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    // Configuration constants
    companion object {
        const val MINIMUM_PAY_LIMIT = 10.0 // Minimum pay in local currency
        const val MAX_WEIGHT_LIMIT = 50.0 // Maximum weight in kg
        const val CANCELLATION_PENALTY_PERCENTAGE = 10.0 // 10% penalty for cancellation
        const val LATE_PENALTY_PERCENTAGE = 5.0 // 5% penalty for late completion
    }

    /**
     * Add a new job with validation
     */
    fun addJob(job: JobData): JobCreationResult {
        val validationResult = validateJob(job)
        if (!validationResult.isValid) {
            return JobCreationResult.Failure(validationResult.errors)
        }

        val jobWithId = job.copy(
            id = if (job.id.isEmpty()) "job_${System.currentTimeMillis()}" else job.id,
            status = if (_networkStatus.value) JobStatus.ACTIVE else JobStatus.DRAFT,
            isDraft = !_networkStatus.value,
            draftTimestamp = if (!_networkStatus.value) System.currentTimeMillis() else null,
            needsSync = !_networkStatus.value
        )

        val currentJobs = _jobs.value.toMutableList()
        currentJobs.add(jobWithId)
        _jobs.value = currentJobs

        // If offline, also add to drafts
        if (!_networkStatus.value) {
            val currentDrafts = _drafts.value.toMutableList()
            currentDrafts.add(jobWithId)
            _drafts.value = currentDrafts
        }

        return JobCreationResult.Success(jobWithId)
    }

    /**
     * Save job as draft (offline mode)
     */
    fun saveAsDraft(job: JobData): JobCreationResult {
        val jobWithId = job.copy(
            id = if (job.id.isEmpty()) "draft_${System.currentTimeMillis()}" else job.id,
            status = JobStatus.DRAFT,
            isDraft = true,
            draftTimestamp = System.currentTimeMillis(),
            needsSync = true
        )

        val currentDrafts = _drafts.value.toMutableList()
        currentDrafts.add(jobWithId)
        _drafts.value = currentDrafts

        return JobCreationResult.Success(jobWithId)
    }

    /**
     * Sync drafts when network is restored
     */
    fun syncDrafts(): List<JobData> {
        val draftsToSync = _drafts.value.filter { it.needsSync }
        val syncedJobs = mutableListOf<JobData>()

        draftsToSync.forEach { draft ->
            val syncedJob = draft.copy(
                status = JobStatus.ACTIVE,
                isDraft = false,
                needsSync = false,
                draftTimestamp = null
            )
            
            val currentJobs = _jobs.value.toMutableList()
            currentJobs.add(syncedJob)
            _jobs.value = currentJobs
            syncedJobs.add(syncedJob)
        }

        // Remove synced drafts
        val remainingDrafts = _drafts.value.filter { !it.needsSync }
        _drafts.value = remainingDrafts

        return syncedJobs
    }

    /**
     * Update network status
     */
    fun updateNetworkStatus(isConnected: Boolean) {
        _networkStatus.value = isConnected
        
        if (isConnected) {
            // Auto-sync drafts when network is restored
            coroutineScope.launch {
                syncDrafts()
            }
        }
    }

    /**
     * Cancel job with reason and penalty calculation
     */
    fun cancelJob(
        jobId: String, 
        reason: String, 
        reasonType: CancellationReasonType
    ): JobCancellationResult {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        
        if (jobIndex == -1) {
            return JobCancellationResult.Failure("Job not found")
        }

        val job = currentJobs[jobIndex]
        if (job.status != JobStatus.ACTIVE && job.status != JobStatus.IN_PROGRESS) {
            return JobCancellationResult.Failure("Job cannot be cancelled in current status")
        }

        // Calculate penalty based on job value and timing
        val penalty = calculateCancellationPenalty(job, reasonType)
        
        val cancelledJob = job.copy(
            status = JobStatus.CANCELLED,
            cancellationReason = reason,
            cancellationReasonType = reasonType,
            cancellationTimestamp = System.currentTimeMillis(),
            cancellationPenalty = penalty
        )

        currentJobs[jobIndex] = cancelledJob
        _jobs.value = currentJobs

        return JobCancellationResult.Success(cancelledJob, penalty)
    }

    /**
     * Validate job data
     */
    private fun validateJob(job: JobData): ValidationResult {
        val errors = mutableListOf<String>()

        // Check mandatory fields
        if (job.location.isBlank()) {
            errors.add("Location is required")
        }
        if (job.brand.isBlank()) {
            errors.add("Brand is required")
        }
        if (job.quantity <= 0) {
            errors.add("Quantity must be greater than 0")
        }
        if (job.price <= 0) {
            errors.add("Price must be greater than 0")
        }
        if (job.substitutes.isBlank()) {
            errors.add("Substitutes are required")
        }
        if (job.deliveryTimeFrame.isBlank()) {
            errors.add("Delivery timeframe is required")
        }

        // Check minimum pay limit
        if (job.pay < MINIMUM_PAY_LIMIT) {
            errors.add("Pay must be at least $MINIMUM_PAY_LIMIT")
        }

        // Check weight limit
        job.maxWeightLimit?.let { weightLimit ->
            if (weightLimit > MAX_WEIGHT_LIMIT) {
                errors.add("Weight limit cannot exceed ${MAX_WEIGHT_LIMIT}kg")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Calculate cancellation penalty
     */
    private fun calculateCancellationPenalty(
        job: JobData, 
        reasonType: CancellationReasonType
    ): Double {
        val basePenalty = job.pay * (CANCELLATION_PENALTY_PERCENTAGE / 100.0)
        
        // Higher penalty for immediate cancellation after acceptance
        return if (job.workerAccepted) {
            basePenalty * 1.5 // 50% higher penalty
        } else {
            basePenalty
        }
    }

    /**
     * Get jobs that need weight limit warning
     */
    fun getJobsWithWeightWarnings(): List<JobData> {
        return _jobs.value.filter { 
            it.maxWeightLimit != null && it.maxWeightLimit!! > MAX_WEIGHT_LIMIT * 0.8 
        }
    }

    /**
     * Get late jobs with penalties
     */
    fun getLateJobs(): List<JobData> {
        val currentTime = System.currentTimeMillis()
        return _jobs.value.filter { job ->
            // This is a simplified check - in real implementation, 
            // you'd parse the deadline string and compare with current time
            job.status == JobStatus.IN_PROGRESS && job.latePenalty > 0
        }
    }

    // Existing methods from original repository
    fun acceptJob(job: JobData) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == job.id }
        if (jobIndex != -1) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                status = JobStatus.APPLIED,
                workerAccepted = true
            )
            _jobs.value = currentJobs
        }
    }

    fun updateJobStatus(jobId: String, status: JobStatus) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(status = status)
            _jobs.value = currentJobs
        }
    }

    fun getJobById(jobId: String): JobData? {
        return _jobs.value.find { it.id == jobId }
    }

    fun getJobByTitle(jobTitle: String): JobData? {
        return _jobs.value.find { it.title == jobTitle }
    }

    fun initializeSampleData() {
        // Initialize with sample data including new fields
        val sampleJobs = listOf(
            JobData(
                id = "sample_grocery_1",
                title = "Grocery Shopping",
                description = "Buy groceries from the local market",
                pay = 25.0,
                distance = 2.5,
                deadline = "Today, 6:00 PM",
                jobType = "Shopping",
                status = JobStatus.ACTIVE,
                location = "Dodoma Central Market",
                brand = "Local Market",
                quantity = 5,
                price = 150.0,
                substitutes = "Any fresh vegetables if specific ones unavailable",
                deliveryTimeFrame = "2 hours",
                latePenalty = 5.0,
                latePenaltyDescription = "5% penalty for late delivery",
                maxWeightLimit = 10.0,
                nearbyLandmark = "Dodoma Central School",
                landmarkType = com.demoapp.feature_jobs.presentation.models.LandmarkType.SCHOOL,
                latitude = -6.2088,
                longitude = 35.7395,
                locationName = "Dodoma Central Market",
                deliveryAddress = "123 Main Street, Dodoma, Tanzania",
                deliveryLat = -6.1730,
                deliveryLng = 35.7419,
                shoppingList = "Milk, Bread, Eggs, Fruits, Vegetables"
            )
        )
        _jobs.value = sampleJobs
    }
}

// Result classes for better error handling
sealed class JobCreationResult {
    data class Success(val job: JobData) : JobCreationResult()
    data class Failure(val errors: List<String>) : JobCreationResult()
}

sealed class JobCancellationResult {
    data class Success(val job: JobData, val penalty: Double) : JobCancellationResult()
    data class Failure(val error: String) : JobCancellationResult()
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
