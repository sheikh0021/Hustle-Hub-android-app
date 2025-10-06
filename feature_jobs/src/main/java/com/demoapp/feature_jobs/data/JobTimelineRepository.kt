package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.JobTimeline
import com.demoapp.feature_jobs.presentation.models.TimelineStage
import com.demoapp.feature_jobs.presentation.models.TimelineStageData
import com.demoapp.feature_jobs.presentation.models.TimelineStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class JobTimelineRepository {
    private val _timelines = MutableStateFlow<List<JobTimeline>>(emptyList())
    val timelines: StateFlow<List<JobTimeline>> = _timelines.asStateFlow()

    init {
        initializeSampleData()
    }

    /**
     * Create a new timeline for a job when worker is assigned
     */
    fun createTimelineForJob(
        jobId: String,
        workerId: String,
        workerName: String,
        clientId: String,
        clientName: String
    ): JobTimeline {
        val timelineId = "timeline_${System.currentTimeMillis()}"
        val now = Date()
        
        val initialStage = TimelineStageData(
            stage = TimelineStage.JOB_ACCEPTED,
            status = TimelineStatus.COMPLETED,
            timestamp = now,
            message = "Worker has accepted the job",
            updatedBy = workerId,
            updatedByName = workerName
        )

        val timeline = JobTimeline(
            id = timelineId,
            jobId = jobId,
            currentStage = TimelineStage.JOB_ACCEPTED,
            stages = listOf(initialStage),
            lastUpdated = now
        )

        val currentTimelines = _timelines.value.toMutableList()
        currentTimelines.add(timeline)
        _timelines.value = currentTimelines

        println("DEBUG: Timeline created for job $jobId with initial stage: ${TimelineStage.JOB_ACCEPTED}")
        return timeline
    }

    /**
     * Update timeline stage
     */
    fun updateTimelineStage(
        jobId: String,
        stage: TimelineStage,
        message: String? = null,
        updatedBy: String,
        updatedByName: String
    ): Boolean {
        val currentTimelines = _timelines.value.toMutableList()
        val timelineIndex = currentTimelines.indexOfFirst { it.jobId == jobId }
        
        if (timelineIndex != -1) {
            val timeline = currentTimelines[timelineIndex]
            val now = Date()
            
            // Create new stage data
            val newStageData = TimelineStageData(
                stage = stage,
                status = TimelineStatus.COMPLETED,
                timestamp = now,
                message = message,
                updatedBy = updatedBy,
                updatedByName = updatedByName
            )
            
            // Update timeline
            val updatedStages = timeline.stages.toMutableList()
            updatedStages.add(newStageData)
            
            val updatedTimeline = timeline.copy(
                currentStage = stage,
                stages = updatedStages,
                lastUpdated = now
            )
            
            currentTimelines[timelineIndex] = updatedTimeline
            _timelines.value = currentTimelines
            
            println("DEBUG: Timeline updated for job $jobId to stage: $stage")
            return true
        }
        return false
    }

    /**
     * Get timeline for a specific job
     */
    fun getTimelineForJob(jobId: String): JobTimeline? {
        return _timelines.value.find { it.jobId == jobId }
    }

    /**
     * Get all timelines for a user (worker or client)
     */
    fun getTimelinesForUser(userId: String): List<JobTimeline> {
        // This would need to be enhanced to filter by user role
        // For now, return all timelines
        return _timelines.value
    }

    /**
     * Mark job as completed
     */
    fun markJobCompleted(
        jobId: String,
        completedBy: String,
        completedByName: String,
        completionMessage: String? = null
    ): Boolean {
        return updateTimelineStage(
            jobId = jobId,
            stage = TimelineStage.JOB_COMPLETED,
            message = completionMessage ?: "Job has been completed successfully",
            updatedBy = completedBy,
            updatedByName = completedByName
        )
    }

    /**
     * Initialize with sample data for demo
     */
    private fun initializeSampleData() {
        val now = Date()
        val sampleTimelines = listOf(
            JobTimeline(
                id = "timeline_1",
                jobId = "sample_grocery_1",
                currentStage = TimelineStage.WORKER_ON_THE_WAY,
                stages = listOf(
                    TimelineStageData(
                        stage = TimelineStage.JOB_ACCEPTED,
                        status = TimelineStatus.COMPLETED,
                        timestamp = Date(now.time - 3600000), // 1 hour ago
                        message = "Worker has accepted the job",
                        updatedBy = "worker_1",
                        updatedByName = "John Mwangi"
                    ),
                    TimelineStageData(
                        stage = TimelineStage.WORKER_ON_THE_WAY,
                        status = TimelineStatus.COMPLETED,
                        timestamp = Date(now.time - 1800000), // 30 minutes ago
                        message = "I'm on my way to the location",
                        updatedBy = "worker_1",
                        updatedByName = "John Mwangi"
                    )
                ),
                lastUpdated = Date(now.time - 1800000)
            )
        )
        
        _timelines.value = sampleTimelines
    }

    companion object {
        @Volatile
        private var INSTANCE: JobTimelineRepository? = null

        fun getInstance(): JobTimelineRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JobTimelineRepository().also { INSTANCE = it }
            }
        }
    }
}
