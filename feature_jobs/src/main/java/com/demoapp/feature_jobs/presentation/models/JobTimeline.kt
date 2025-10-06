package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class JobTimeline(
    val id: String = "",
    val jobId: String,
    val currentStage: TimelineStage,
    val stages: List<TimelineStageData> = emptyList(),
    val lastUpdated: Date = Date()
)

data class TimelineStageData(
    val stage: TimelineStage,
    val status: TimelineStatus,
    val timestamp: Date,
    val message: String? = null,
    val updatedBy: String, // workerId or clientId
    val updatedByName: String // worker name or client name
)

enum class TimelineStage {
    JOB_ACCEPTED,
    WORKER_ON_THE_WAY,
    WORKER_STARTED_JOB,
    JOB_COMPLETED
}

enum class TimelineStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}

// Helper functions for timeline management
fun TimelineStage.getDisplayName(): String {
    return when (this) {
        TimelineStage.JOB_ACCEPTED -> "Job Accepted"
        TimelineStage.WORKER_ON_THE_WAY -> "On The Way"
        TimelineStage.WORKER_STARTED_JOB -> "Started Working"
        TimelineStage.JOB_COMPLETED -> "Job Completed"
    }
}

fun TimelineStage.getDescription(): String {
    return when (this) {
        TimelineStage.JOB_ACCEPTED -> "Worker has accepted the job"
        TimelineStage.WORKER_ON_THE_WAY -> "Worker is heading to the job location"
        TimelineStage.WORKER_STARTED_JOB -> "Worker has started working on the job"
        TimelineStage.JOB_COMPLETED -> "Job has been completed successfully"
    }
}

fun TimelineStage.getIcon(): String {
    return when (this) {
        TimelineStage.JOB_ACCEPTED -> "✅"
        TimelineStage.WORKER_ON_THE_WAY -> "🚗"
        TimelineStage.WORKER_STARTED_JOB -> "🔨"
        TimelineStage.JOB_COMPLETED -> "🎉"
    }
}
