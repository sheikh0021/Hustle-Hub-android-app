package com.demoapp.core_network.models

data class ShoppingItemRequest(
    val item_name: String,
    val brand: String? = null,
    val price: Double,
    val quantity: Int
)

data class PackageItemRequest(
    val package_description: String,
    val package_type: String,
    val weight: Double,
    val quantity: Int
)

data class SurveyItemRequest(
    val survey_topic: String,
    val audience: String,
    val duration: Int,
    val number_of_people: Int
)

data class TaskCreateRequest(
    val title: String? = null,
    val task_description: String,
    val category: String,
    val store_service_location: String,
    val delivery_location: String,
    val budget_kes: Double,
    val due_date: String,
    val store_service_latitude: Double? = null,
    val store_service_longitude: Double? = null,
    val delivery_latitude: Double? = null,
    val delivery_longitude: Double? = null,
    val shopping_items: List<ShoppingItemRequest>? = null,
    val package_items: List<PackageItemRequest>? = null,
    val survey_items: List<SurveyItemRequest>? = null
)

// Response models
data class CreatedShoppingItem(
    val id: Int,
    val item_name: String,
    val brand: String?,
    val price: String,
    val quantity: Int
)

data class CreatedPackageItem(
    val id: Int,
    val package_description: String,
    val package_type: String,
    val weight: String,
    val quantity: Int
)

data class CreatedSurveyItem(
    val id: Int,
    val survey_topic: String,
    val audience: String,
    val duration: Int,
    val number_of_people: Int
)

data class TaskCreateResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: CreatedTaskData?
)

data class CreatedTaskData(
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
    val shopping_items: List<CreatedShoppingItem> = emptyList(),
    val package_items: List<CreatedPackageItem> = emptyList(),
    val survey_items: List<CreatedSurveyItem> = emptyList()
)


