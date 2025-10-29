package com.demoapp.core_network.models

data class ShoppingItem(
    val item_name: String,
    val brand: String,
    val price: Double,
    val quantity: Int
)

data class CreateTaskRequest(
    val title: String,
    val task_description: String,
    val category: String,
    val store_service_location: String,
    val delivery_location: String,
    val budget_kes: Double,
    val due_date: String, // ISO 8601 format: "2025-09-25T18:00:00Z"
    val store_service_latitude: Double,
    val store_service_longitude: Double,
    val delivery_latitude: Double,
    val delivery_longitude: Double,
    val shopping_items: List<ShoppingItem>
)

data class CreateTaskResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: TaskData? = null
)

data class TaskData(
    val id: Int,
    val title: String,
    val task_description: String,
    val category: String,
    val store_service_location: String,
    val delivery_location: String,
    val budget_kes: Double,
    val due_date: String,
    val store_service_latitude: Double,
    val store_service_longitude: Double,
    val delivery_latitude: Double,
    val delivery_longitude: Double,
    val shopping_items: List<ShoppingItem>,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class MyTasksResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: List<TaskData>? = null
)

