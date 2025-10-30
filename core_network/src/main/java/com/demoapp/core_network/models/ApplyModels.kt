package com.demoapp.core_network.models

data class ApplyForTaskRequest(
    val proposed_price: Double,
    val message: String?
)

data class ApplyForTaskResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: ApplicationData?
)

data class ApplicationData(
    val id: Int,
    val task: Int,
    val task_title: String,
    val applicant: Int,
    val applicant_name: String,
    val proposed_price: String,
    val message: String?,
    val status: String,
    val created_at: String
)

data class GetTaskApplicationsResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: ApplicationsListData?
)

data class ApplicationsListData(
    val applications: List<ApplicationData> = emptyList()
)

data class AcceptApplicationResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: AcceptApplicationData?
)

data class AcceptApplicationData(
    val task_id: Int,
    val assigned_to: Int,
    val status: String
)


