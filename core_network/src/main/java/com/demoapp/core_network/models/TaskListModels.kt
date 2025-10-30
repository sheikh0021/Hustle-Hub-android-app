package com.demoapp.core_network.models

data class TaskListResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: TaskListData?
)

data class TaskListData(
    val tasks: List<TaskListItem> = emptyList(),
    val pagination: Pagination? = null
)

data class TaskListItem(
    val id: Int,
    val title: String,
    val task_description: String,
    val category: String,
    val category_display: String,
    val store_service_location: String,
    val delivery_location: String,
    val budget_kes: String,
    val payment_amount: String?,
    val due_date: String,
    val distance_km: Double?,
    val status: String,
    val status_display: String,
    val user_name: String?,
    val created_at: String?
)

data class Pagination(
    val current_page: Int,
    val total_pages: Int,
    val total_tasks: Int,
    val has_next: Boolean,
    val has_previous: Boolean
)


