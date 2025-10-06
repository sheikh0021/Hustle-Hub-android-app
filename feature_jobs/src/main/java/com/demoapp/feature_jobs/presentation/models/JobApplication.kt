package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class JobApplication(
    val id: String = "",
    val jobId: String,
    val workerId: String,
    val workerName: String,
    val workerPhone: String,
    val workerRating: Float = 0f,
    val workerCompletedTasks: Int = 0,
    val applicationMessage: String? = null,
    val appliedAt: Date = Date(),
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val selectedAt: Date? = null
)

enum class ApplicationStatus {
    PENDING,    // Worker applied, waiting for client decision
    SELECTED,   // Client selected this worker
    REJECTED,   // Client rejected this worker
    WITHDRAWN   // Worker withdrew their application
}

data class WorkerProfile(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val profileImage: String? = null,
    val rating: Float = 0f,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val location: String? = null,
    val skills: List<String> = emptyList(),
    val joinedDate: Date = Date(),
    val isVerified: Boolean = false
)
