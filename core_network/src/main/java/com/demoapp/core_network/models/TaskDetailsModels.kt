package com.demoapp.core_network.models

data class TaskDetailsResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: TaskDetailsData?
)

data class TaskDetailsData(
    val id: Int,
    val title: String,
    val task_description: String,
    val category: String,
    val status: String,
    val store_service_location: String,
    val delivery_location: String,
    val budget_kes: String,
    val payment_amount: String,
    val due_date: String,
    val distance_km: String?,
    val store_service_latitude: String?,
    val store_service_longitude: String?,
    val delivery_latitude: String?,
    val delivery_longitude: String?,
    val assigned_to: Int?,
    val assigned_at: String?,
    val payment_status: String,
    val invoice_created: Boolean,
    val invoice_created_at: String?,
    val created_at: String,
    val updated_at: String,
    val shopping_items: List<TaskDetailsShoppingItem> = emptyList(),
    val package_items: List<TaskDetailsPackageItem> = emptyList(),
    val survey_items: List<TaskDetailsSurveyItem> = emptyList()
)

data class TaskDetailsShoppingItem(
    val id: Int,
    val item_name: String,
    val brand: String?,
    val price: String,
    val quantity: Int
)

data class TaskDetailsPackageItem(
    val id: Int,
    val package_description: String,
    val package_type: String,
    val weight: String,
    val quantity: Int
)

data class TaskDetailsSurveyItem(
    val id: Int,
    val survey_topic: String,
    val audience: String,
    val duration: Int,
    val number_of_people: Int
)
